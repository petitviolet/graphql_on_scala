package net.petitviolet.graphql

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import net.petitviolet.graphql.commons.WithLogger
import net.petitviolet.graphql.commons.exceptions.{ AuthenticationError, NotFoundException }
import net.petitviolet.graphql.models.daos
import net.petitviolet.graphql.schemas.resolvers.{ ProjectResolver, TaskResolver, UserResolver }
import net.petitviolet.graphql.schemas.{ GraphQLContext, Middlewares }
import sangria.ast.Document
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ ExceptionHandler, _ }
import sangria.marshalling.ResultMarshaller
import sangria.marshalling.sprayJson._
import sangria.parser.{ QueryParser, SyntaxError }
import sangria.schema.Schema
import sangria.validation.Violation
import spray.json.{ JsObject, JsString, JsValue }

import scala.concurrent._
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.control.NonFatal

object GraphQLServer extends WithLogger {
  type Ctx = GraphQLContext

  private lazy val schema: Schema[Ctx, Unit] = schemas.schema

  def showSchema: String = {
    schema.renderPretty
  }

  private implicit val executionContext: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(16))

  private lazy val deferredResolver = DeferredResolver.fetchers(
    UserResolver.userFetcher,
    TaskResolver.taskFetcher,
    TaskResolver.taskFetcherForProject,
    TaskResolver.taskFetcherForAssignedTo,
    TaskResolver.taskFetcherForCreatedBy,
  )

  def execute(userIdOpt: Option[String], jsValue: JsValue): Future[(StatusCode, JsValue)] = {
    val JsObject(fields) = jsValue
    val operation = fields.get("operationName") collect {
      case JsString(op) => op
    }

    val vars = fields.get("variables") match {
      case Some(obj: JsObject) => obj
      case _                   => JsObject.empty
    }

    val Some(JsString(document)) = fields.get("query")

    Future.fromTry(QueryParser.parse(document)) zip GraphQLContext.create(userIdOpt) flatMap {
      case (queryDocument: Document, context: GraphQLContext) =>
        val result: Future[JsValue] =
          Executor.execute[Ctx, Unit, JsObject](
            schema,
            queryDocument,
            context,
            exceptionHandler = exceptionHandler,
            operationName = operation,
            deferredResolver = deferredResolver,
            variables = vars,
            middleware = Middlewares.value
          )
        result
          .map { jsValue =>
            OK -> jsValue
          }
          .recover {
            case error: QueryAnalysisError =>
              BadRequest -> error.resolveError
            case error: ErrorWithResolver =>
              InternalServerError -> error.resolveError
          }
    } recover {
      case v: SyntaxError =>
        BadRequest -> new Exception(v.getMessage()) with ErrorWithResolver with WithViolations {
          override def exceptionHandler: ExceptionHandler = GraphQLServer.exceptionHandler
          override def violations: Vector[Violation] = Vector.empty
        }.resolveError
      case v: ValidationError =>
        BadRequest -> v.resolveError
      case NonFatal(t) =>
        InternalServerError -> InternalError(t, GraphQLServer.exceptionHandler).resolveError
    }

  }

  case class InternalError(throwable: Throwable, override val exceptionHandler: ExceptionHandler)
      extends ExecutionError(throwable.getMessage, exceptionHandler)

  sealed abstract class ErrorType(val value: String)
  object ErrorType {
    case object BadRequest extends ErrorType("BadRequest")
    case object ServerError extends ErrorType("ServerError")
    // auth N/Z
    case object AuthError extends ErrorType("AuthError")
  }
  implicit val errorType = sangria.macros.derive.deriveEnumType[ErrorType]()

  private val exceptionHandler = {
    def handleException(m: ResultMarshaller, msg: String, tp: ErrorType) =
      HandledException(
        msg,
        additionalFields = Map("type" -> m.enumNode(tp.value, errorType.name)),
        addFieldsInExtensions = false,
        addFieldsInError = true
      )

    val onException: PartialFunction[(ResultMarshaller, Throwable), HandledException] = {
      case (m, qa: QueryAnalysisError) =>
        logger.warn(s"QueryAnalysisError occurred. msg = ${qa.getMessage}")
        handleException(m, qa.getMessage, ErrorType.BadRequest)

      case (m, se: SyntaxError) =>
        logger.info(s"SyntaxError occurred. msg = ${se.getMessage()}")
        handleException(m, se.getMessage, ErrorType.BadRequest)

      case (m, ewr: ErrorWithResolver) =>
        logger.error(s"ErrorWithResolver occurred. msg = ${ewr.getMessage}", ewr)
        handleException(m, ewr.getMessage, ErrorType.ServerError)

      case (m, AuthenticationError(msg)) =>
        logger.info(s"AuthenticationError occurred. msg = ${msg}")
        handleException(m, msg, ErrorType.BadRequest)

      case (m, NotFoundException(msg)) =>
        logger.info(s"NotFoundException occurred. msg = ${msg}")
        handleException(m, msg, ErrorType.BadRequest)

      case (m, t: Throwable) =>
        logger.error(s"unknown server error occurred. msg = ${t.getMessage}", t)
        handleException(m, t.getMessage, ErrorType.ServerError)
    }

    val onViolation: PartialFunction[(ResultMarshaller, Violation), HandledException] = {
      case (m, v) =>
        logger.warn(s"Violation error occurred. msg = ${v.errorMessage}")
        handleException(m, v.errorMessage, ErrorType.BadRequest)
    }

    val onUserFacingError
      : PartialFunction[(ResultMarshaller, UserFacingError), HandledException] = {
      case (m, v: WithViolations) =>
        logger.warn(s"WithViolations occurred. msg = ${v.getMessage()}")
        handleException(m, v.getMessage, ErrorType.BadRequest)

      case (m, ie: InternalError) =>
        logger.error(s"InternalError occurred. msg = ${ie.getMessage()}", ie)
        handleException(m, ie.getMessage, ErrorType.ServerError)

      case (m, ee: ExecutionError) =>
        logger.error(s"ExecutionError occurred. msg = ${ee.getMessage()}", ee)
        handleException(m, ee.getMessage, ErrorType.ServerError)

      case (m, ufe) =>
        logger.warn(s"UserFacingError occurred. msg = ${ufe.getMessage()}")
        handleException(m, ufe.getMessage, ErrorType.BadRequest)
    }

    ExceptionHandler(onException, onViolation, onUserFacingError)
  }
}

abstract class GraphQLApplication {
  def main(args: Array[String]): Unit = {
    val server = GraphQLServer

    daos.init()

    implicit val system: ActorSystem = ActorSystem("graphql-prac")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    implicit val executionContext =
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(sys.runtime.availableProcessors()))

    val route: Route = logRequest("[Request]") {
      (post & path("graphql")) {
        (optionalHeaderValueByName("X-User-Id") & entity(as[JsValue])) { (userIdOpt, jsValue) =>
          complete(server.execute(userIdOpt, jsValue))
        }
      } ~
        (get & path("schema")) {
          complete(server.showSchema)
        } ~
        get {
          logRequestResult("/graphiql.html", Logging.InfoLevel) {
            getFromResource("graphiql.html")
          }
        }
    }

    val host = sys.props.get("http.host") getOrElse "0.0.0.0"
    val port = sys.props.get("http.port").fold(9999)(_.toInt)

    val f = Http().bindAndHandle(route, host, port)

    println(s"server at [$host:$port]")

    val _ = StdIn.readLine("\ninput something\n")

    println("\nshutdown...\n")
    val x = f.flatMap { b =>
      b.unbind()
        .flatMap { _ =>
          materializer.shutdown()
          system.terminate()
        }(ExecutionContext.global)
    }(ExecutionContext.global)

    Await.ready(x, 5.seconds)
    sys.runtime.gc()
    println(s"shutdown completed!\n")
  }
}

package net.petitviolet.graphql.schemas

import net.petitviolet.graphql.commons.WithLogger
import net.petitviolet.graphql.commons.exceptions.AuthenticationError
import sangria.execution._
import sangria.schema.Context
import sangria.slowlog.SlowLog
import scala.concurrent.duration._

object Middlewares extends WithLogger {
  lazy val value: List[Middleware[Ctx]] = AuthenticationFilter :: slowLog :: Nil

  type Ctx = GraphQLContext

  case object RequireAuthentication extends FieldTag

  object AuthenticationFilter extends Middleware[Ctx] with MiddlewareBeforeField[Ctx] {
    override type QueryVal = Unit
    override type FieldVal = Unit
    private type MCtx = MiddlewareQueryContext[Ctx, _, _]

    override def beforeQuery(context: MCtx) = ()

    override def afterQuery(queryVal: QueryVal, context: MCtx) = ()

    override def beforeField(queryVal: QueryVal, mctx: MCtx, ctx: Context[Ctx, _]) = {
      if (ctx.field.tags contains RequireAuthentication) {
        if (!ctx.ctx.isLoggedIn) {
          throw AuthenticationError("must logged in!")
        }
      }

      continue
    }
  }

  private lazy val slowLog = SlowLog(logger, threshold = 10.millis)
}

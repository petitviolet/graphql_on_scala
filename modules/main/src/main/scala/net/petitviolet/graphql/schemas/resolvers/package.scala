package net.petitviolet.graphql.schemas

import net.petitviolet.graphql.commons.exceptions.NotFoundException

import scala.concurrent.{ ExecutionContext, Future }

package object resolvers {

  private[resolvers] implicit def executionContext(implicit ctx: GraphQLContext): ExecutionContext =
    ctx.ec

  private[resolvers] implicit class GetFromOption[A](val f: Future[Option[A]]) extends AnyVal {
    def forceGetOr(msg: => String)(implicit ec: ExecutionContext): Future[A] = f flatMap {
      case Some(a) => Future.successful(a)
      case None    => Future.failed(NotFoundException(msg))
    }
  }
}

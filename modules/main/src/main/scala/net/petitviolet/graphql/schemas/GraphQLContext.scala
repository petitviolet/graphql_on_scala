package net.petitviolet.graphql.schemas

import java.time.ZonedDateTime

import net.petitviolet.graphql.commons.exceptions.AuthenticationError
import net.petitviolet.graphql.models.User
import net.petitviolet.graphql.models.daos.UserDao

import scala.concurrent.{ ExecutionContext, Future }

class GraphQLContext private (userOpt: Option[User])(implicit val ec: ExecutionContext) {
  val dateTime: ZonedDateTime = ZonedDateTime.now()
  def viewer: User = userOpt getOrElse { throw AuthenticationError("must logged-in!") }
}

object GraphQLContext {
  private def withoutAuthentication(implicit ec: ExecutionContext) =
    Future.successful(new GraphQLContext(None))

  def create(userIdOpt: Option[String])(implicit ec: ExecutionContext): Future[GraphQLContext] = {
    userIdOpt.fold(withoutAuthentication) { userId =>
      UserDao.authenticate(userId) map { userOpt =>
        new GraphQLContext(userOpt)
      }
    }
  }
}

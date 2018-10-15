package net.petitviolet.graphql.schemas

import java.time.ZonedDateTime

import net.petitviolet.graphql.commons.exceptions.AuthenticationError
import net.petitviolet.graphql.models.User
import net.petitviolet.graphql.models.daos.UserDao

import scala.concurrent.{ ExecutionContext, Future }

class GraphQLContext private (userOpt: Option[User])(implicit val ec: ExecutionContext) {
  val dateTime: ZonedDateTime = ZonedDateTime.now()

  def loggedInUser: User = userOpt getOrElse { throw AuthenticationError("must logged-in!") }

  def isLoggedIn = userOpt.isDefined
}

object GraphQLContext {
  private def skipAuthentication(implicit ec: ExecutionContext) =
    Future.successful(new GraphQLContext(None))

  private def authentication(userId: String)(implicit ec: ExecutionContext) =
    UserDao.authenticate(userId) map { userOpt =>
      new GraphQLContext(userOpt)
    }

  def create(userIdOpt: Option[String])(implicit ec: ExecutionContext): Future[GraphQLContext] = {
    userIdOpt map authentication getOrElse skipAuthentication
  }
}

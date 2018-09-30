package net.petitviolet.graphql.schemas.resolvers

import net.petitviolet.graphql.models.daos.UserDao
import net.petitviolet.graphql.models.{ ProjectId, User, UserId, UserStatus }
import net.petitviolet.graphql.schemas.GraphQLContext

import scala.concurrent.Future

object UserResolver {
  def all()(implicit ctx: GraphQLContext): Future[Seq[User]] = {
    UserDao.findAll()
  }

  def byId(userId: UserId)(implicit ctx: GraphQLContext): Future[User] = {
    UserDao.findById(userId).forceGetOr(s"user(${userId.value}) not found.")
  }

  def byProjectId(projectId: ProjectId, statusOpt: Option[UserStatus])(
      implicit ctx: GraphQLContext): Future[Seq[User]] = {
    val f: Future[Seq[User]] = UserDao.findByProjectId(projectId)

    statusOpt.fold(f) { status =>
      f map { users =>
        users.filter { user =>
          user.status == status
        }
      }
    }
  }

}

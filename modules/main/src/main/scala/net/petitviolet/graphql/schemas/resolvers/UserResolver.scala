package net.petitviolet.graphql.schemas.resolvers

import net.petitviolet.graphql.models.daos.UserDao
import net.petitviolet.graphql.models.{ ProjectId, User, UserId }
import net.petitviolet.graphql.schemas.GraphQLContext

import scala.concurrent.Future

object UserResolver {
  def byId(userId: UserId)(implicit ctx: GraphQLContext): Future[User] = {
    UserDao.findById(userId).forceGet
  }

  def byProjectId(projectId: ProjectId)(implicit ctx: GraphQLContext): Future[Seq[User]] = {
    UserDao.findByProjectId(projectId)
  }

}

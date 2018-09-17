package net.petitviolet.graphql.schemas.resolvers

import net.petitviolet.graphql.models.daos.ProjectDao
import net.petitviolet.graphql.models.{ Project, ProjectId, TaskId, UserId }
import net.petitviolet.graphql.schemas.GraphQLContext

import scala.concurrent.Future

object ProjectResolver {
  def all()(implicit ctx: GraphQLContext): Future[Seq[Project]] = {
    ProjectDao.findAll()
  }

  def byId(taskId: ProjectId)(implicit ctx: GraphQLContext): Future[Project] = {
    ProjectDao.findById(taskId).forceGetOr(s"task(${taskId.value}) not found.")
  }

}

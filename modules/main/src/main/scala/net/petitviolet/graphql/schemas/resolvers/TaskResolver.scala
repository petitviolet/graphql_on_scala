package net.petitviolet.graphql.schemas.resolvers

import net.petitviolet.graphql.models.daos.TaskDao
import net.petitviolet.graphql.models.{ ProjectId, Task, TaskId, UserId }
import net.petitviolet.graphql.schemas.GraphQLContext

import scala.concurrent.Future

object TaskResolver {
  def all()(implicit ctx: GraphQLContext): Future[Seq[Task]] = {
    TaskDao.findAll()
  }

  def byId(taskId: TaskId)(implicit ctx: GraphQLContext): Future[Task] = {
    TaskDao.findById(taskId).forceGetOr(s"task(${taskId.value}) not found.")
  }

  def byProjectId(projectId: ProjectId)(implicit ctx: GraphQLContext): Future[Seq[Task]] = {
    TaskDao.findByProjectId(projectId)
  }

  def byAssignedTo(userId: UserId)(implicit ctx: GraphQLContext): Future[Seq[Task]] = {
    TaskDao.findByAssignedTo(userId)
  }

  def byCreatedBy(userId: UserId)(implicit ctx: GraphQLContext): Future[Seq[Task]] = {
    TaskDao.findByCreatedBy(userId)
  }

}

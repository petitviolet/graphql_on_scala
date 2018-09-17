package net.petitviolet.graphql.models.daos

import net.petitviolet.graphql.models._

import scala.concurrent.{ ExecutionContext, Future }

object TaskDao extends Dao[TaskId, Task] {

  def findByProjectId(projectId: ProjectId)(implicit ec: ExecutionContext): Future[Seq[Task]] = {
    filterBy {
      case (_, task) => task.projectId == projectId
    }
  }

  def findByAssignedTo(userId: UserId)(implicit ec: ExecutionContext): Future[Seq[Task]] = {
    filterBy {
      case (_, task) => task.assignedTo == userId
    }
  }

  def findByCreatedBy(userId: UserId)(implicit ec: ExecutionContext): Future[Seq[Task]] = {
    filterBy {
      case (_, task) => task.createdBy == userId
    }
  }
}

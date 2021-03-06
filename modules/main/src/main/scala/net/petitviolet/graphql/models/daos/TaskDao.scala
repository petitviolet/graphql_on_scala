package net.petitviolet.graphql.models.daos

import java.time.ZonedDateTime

import net.petitviolet.graphql.models._

import scala.concurrent.{ ExecutionContext, Future }

object TaskDao extends Dao[Task] {
  private[daos] def init(): Unit = {
    this.data ++= List(
      Task(
        TaskId("t1"),
        ProjectId("p1"),
        UserId("u1"),
        UserId("u1"),
        TaskName("task-1"),
        TaskDescription("task-1-description"),
        TaskStatus.Todo,
        ZonedDateTime.now()
      ),
      Task(
        TaskId("t2"),
        ProjectId("p1"),
        UserId("u2"),
        UserId("u1"),
        TaskName("task-2"),
        TaskDescription("task-2-description"),
        TaskStatus.Todo,
        ZonedDateTime.now()
      ),
      Task(
        TaskId("t3"),
        ProjectId("p1"),
        UserId("u1"),
        UserId("u2"),
        TaskName("task-3"),
        TaskDescription("task-3-description"),
        TaskStatus.Doing,
        ZonedDateTime.now()
      ),
      Task(
        TaskId("t4"),
        ProjectId("p2"),
        UserId("u3"),
        UserId("u3"),
        TaskName("task-4"),
        TaskDescription("task-4-description"),
        TaskStatus.Done,
        ZonedDateTime.now()
      ),
      Task(
        TaskId("t5"),
        ProjectId("p1"),
        UserId("u2"),
        UserId("u1"),
        TaskName("task-5"),
        TaskDescription("task-5-description"),
        TaskStatus.Done,
        ZonedDateTime.now()
      ),
    ).map { t =>
      t.id -> t
    }
  }

  def findByProjectIds(projectIds: Seq[ProjectId])(
      implicit ec: ExecutionContext): Future[Seq[Task]] = {
    logger.debug(s"[$tag]findByProjectIds($projectIds)")
    filterBy {
      case (_, task) => projectIds contains task.projectId
    }
  }

  def findByProjectId(projectId: ProjectId)(implicit ec: ExecutionContext): Future[Seq[Task]] = {
    logger.debug(s"[$tag]findByProjectId($projectId)")
    filterBy {
      case (_, task) => task.projectId == projectId
    }
  }

  def findByAssignedTos(userIds: Seq[UserId])(implicit ec: ExecutionContext): Future[Seq[Task]] = {
    logger.debug(s"[$tag]findByAssignedTos($userIds)")
    filterBy {
      case (_, task) => userIds contains task.assignedTo
    }
  }
  def findByAssignedTo(userId: UserId)(implicit ec: ExecutionContext): Future[Seq[Task]] = {
    logger.debug(s"[$tag]findByAssignedTo($userId)")
    filterBy {
      case (_, task) => task.assignedTo == userId
    }
  }

  def findByCreatedBys(userIds: Seq[UserId])(implicit ec: ExecutionContext): Future[Seq[Task]] = {
    logger.debug(s"[$tag]findByCreatedBys($userIds)")
    filterBy {
      case (_, task) => userIds contains task.createdBy
    }
  }

  def findByCreatedBy(userId: UserId)(implicit ec: ExecutionContext): Future[Seq[Task]] = {
    logger.debug(s"[$tag]findByCreatedBy($userId)")
    filterBy {
      case (_, task) => task.createdBy == userId
    }
  }
}

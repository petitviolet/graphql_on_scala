package net.petitviolet.graphql.models

import java.time.ZonedDateTime
import java.util.UUID

case class Task(id: TaskId,
                projectId: ProjectId,
                assignedTo: UserId,
                createdBy: UserId,
                name: TaskName,
                description: TaskDescription,
                status: TaskStatus,
                createdAt: ZonedDateTime)
    extends EntityWithId[TaskId]

case class TaskId(value: String) extends Id

object TaskId {
  def generate: TaskId = apply(UUID.randomUUID().toString)
}

case class TaskName(value: String)
case class TaskDescription(value: String)

sealed abstract class TaskStatus(val value: Int)

object TaskStatus {
  case object Done extends TaskStatus(1)
  case object Doing extends TaskStatus(2)
  case object Todo extends TaskStatus(3)
}

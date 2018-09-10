package net.petitviolet.graphql.models

import java.time.ZonedDateTime

case class Task(id: TaskId, projectId: ProjectId,
  assignedTo: UserId,
  createdBy: UserId,
  name: TaskName, description: TaskDescription, status: TaskStatus, createdAt: ZonedDateTime)

case class TaskId(value: String)
case class TaskName(value:String)
case class TaskDescription(value: String)
sealed abstract class TaskStatus(val value: Int)

object TaskStatus {
  case object Done extends TaskStatus(1)
  case object Doing extends TaskStatus(2)
  case object Todo extends TaskStatus(3)
}

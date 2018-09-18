package net.petitviolet.graphql.schemas.mutations

import net.petitviolet.graphql.commons.exceptions.NotFoundException
import net.petitviolet.graphql.models._
import net.petitviolet.graphql.models.daos.{ TaskDao, UserDao }
import net.petitviolet.graphql.schemas.Ctx
import net.petitviolet.graphql.schemas.types.ObjectTypes
import sangria.macros.derive
import sangria.schema.{ Argument, Context, Field }
import spray.json.RootJsonFormat

import scala.concurrent.Future

object UpdateTask extends Mutation {
  case class UpdateTaskParam(taskId: String,
                             taskName: Option[String],
                             taskDescription: Option[String],
                             assignedTo: Option[String])
  private lazy val paramType = derive.deriveInputObjectType[UpdateTaskParam]()
  private implicit lazy val paramTypeJ: RootJsonFormat[UpdateTaskParam] = jsonFormat4(
    UpdateTaskParam.apply)

  val arg = Argument("attributes", paramType)

  override def field: Field[Ctx, Unit] = Field(
    "UpdateTask",
    ObjectTypes.taskType,
    arguments = List(arg),
    resolve = { implicit ctx: Context[Ctx, Unit] =>
      val viewer = ctx.ctx.viewer
      val UpdateTaskParam(taskId, taskName, taskDescription, assignedTo) = ctx arg arg
      TaskDao.findById(TaskId(taskId)).flatMap {
        case Some(task) if task.projectId == viewer.projectId =>
          assignedTo.map { id =>
            UserDao.findById(UserId(id)).flatMap {
              case Some(user) if user.projectId == viewer.projectId =>
                Future.successful(user.id)
              case _                                                =>
                Future.failed(NotFoundException(s"user($id) not found."))
            }
          } getOrElse { Future.successful(task.assignedTo) } flatMap { userId: UserId =>
            val newTask = task.copy(
              name = taskName.fold(task.name)(TaskName.apply),
              description = taskDescription.fold(task.description)(TaskDescription.apply),
              assignedTo = userId
            )
            TaskDao.store(newTask)
          }
        case _ => Future.failed(NotFoundException(s"task(${taskId}) not found."))
      }
    }
  )

}

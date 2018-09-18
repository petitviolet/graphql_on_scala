package net.petitviolet.graphql.schemas.mutations

import net.petitviolet.graphql.models._
import net.petitviolet.graphql.models.daos.TaskDao
import net.petitviolet.graphql.schemas.Ctx
import net.petitviolet.graphql.schemas.types.ObjectTypes
import sangria.macros.derive
import sangria.schema.{ Argument, Field }
import spray.json.RootJsonFormat

object CreateTask extends Mutation {
  case class CreateTaskParam(taskName: String, taskDescription: String, assignedTo: String)
  private lazy val paramType = derive.deriveInputObjectType[CreateTaskParam]()
  private implicit lazy val paramTypeJ: RootJsonFormat[CreateTaskParam] = jsonFormat3(
    CreateTaskParam.apply)

  val arg = Argument("attributes", paramType)

  override def field: Field[Ctx, Unit] = Field(
    "CreateTask",
    ObjectTypes.taskType,
    arguments = List(arg),
    resolve = { ctx =>
      val user = ctx.ctx.viewer
      val CreateTaskParam(taskName, taskDescription, assignedTo) = ctx arg arg
      val task = Task(
        TaskId.generate,
        user.projectId,
        UserId(assignedTo),
        user.id,
        TaskName(taskName),
        TaskDescription(taskDescription),
        TaskStatus.Todo,
        ctx.ctx.dateTime
      )
      TaskDao.store(task)(ctx.ctx.ec)
    }
  )

}

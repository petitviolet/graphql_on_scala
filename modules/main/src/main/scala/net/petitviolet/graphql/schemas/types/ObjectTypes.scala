package net.petitviolet.graphql.schemas.types

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import net.petitviolet.graphql.models._
import net.petitviolet.graphql.schemas.Ctx
import net.petitviolet.graphql.schemas.resolvers.{ ProjectResolver, TaskResolver, UserResolver }
import sangria.macros.derive
import sangria.schema._

import scala.util.Try

object ObjectTypes {
  private implicit val dateTimeType: ScalarType[ZonedDateTime] = {
    import sangria.validation._
    case object ZonedDateTimeCoercionViolation
        extends ValueCoercionViolation("ZonedDateTime expected")

    val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val convertString: String => Either[Violation, ZonedDateTime] = str =>
      Try { ZonedDateTime.parse(str, format) }.toEither.left.flatMap { _ =>
        Left(ZonedDateTimeCoercionViolation)
    }

    ScalarType[ZonedDateTime](
      "ZonedDateTime",
      description = Some("ZonedDateTime scalar type. Unix Time(milliseconds from 1970/01/01)"),
      coerceOutput = { (date, _) =>
        date.format(format)
      },
      coerceUserInput = { input =>
        StringType.coerceUserInput(input) flatMap convertString
      },
      coerceInput = { input =>
        StringType.coerceInput(input) flatMap convertString
      }
    )
  }

  private val entityInterface = InterfaceType[Ctx, Entity](
    "Entity",
    "Entity",
    () =>
      fields[Ctx, Entity](
        Field("id", StringType, resolve = { _.value.id.value }),
        Field("createdAt", dateTimeType, resolve = { _.value.createdAt }),
    )
  )

//  implicit lazy val userType = derive.deriveObjectType[Ctx, User](
//    derive.Interfaces(entityInterface),
//    derive.AddFields(
//      Field("assignedTasks", ListType(taskType), resolve = { ctx =>
//        TaskResolver.byAssignedTo(ctx.value.id)(ctx.ctx)
//      }),
//      Field("createdTasks", ListType(taskType), resolve = { ctx =>
//        TaskResolver.byCreatedBy(ctx.value.id)(ctx.ctx)
//      }),
//      Field("project", projectType, resolve = { ctx =>
//        ProjectResolver.byId(ctx.value.projectId)(ctx.ctx)
//      })
//    )
//  )

  implicit lazy val userStatusType = derive.deriveEnumType[UserStatus]()

  implicit lazy val userType: ObjectType[Ctx, User] = ObjectType[Ctx, User](
    "User",
    interfaces = interfaces[Ctx, User](entityInterface),
    () =>
      fields[Ctx, User](
        Field("id", StringType, resolve = { _.value.id.value }),
        Field("createdAt", dateTimeType, resolve = { _.value.createdAt }),
        Field("name", StringType, resolve = { _.value.name.value }),
        Field("status", userStatusType, resolve = { _.value.status }),
        Field("projectId", StringType, resolve = { _.value.projectId.value }),
        Field("assignedTasks", ListType(taskType), resolve = { ctx =>
          TaskResolver.byAssignedTo(ctx.value.id)(ctx.ctx)
        }),
        Field("createdTasks", ListType(taskType), resolve = { ctx =>
          TaskResolver.byCreatedBy(ctx.value.id)(ctx.ctx)
        }),
        Field("project", projectType, resolve = { ctx =>
          ProjectResolver.byId(ctx.value.projectId)(ctx.ctx)
        })
    )
  )

  private implicit lazy val projectPlanType = {
    val free = derive.deriveObjectType[Ctx, Plan.Free]()
    val standard = derive.deriveObjectType[Ctx, Plan.Standard]()
    val enterprise = derive.deriveObjectType[Ctx, Plan.Enterprise]()

    UnionType("Plan", types = free :: standard :: enterprise :: Nil)
  }

  implicit lazy val projectType: ObjectType[Ctx, Project] = ObjectType[Ctx, Project](
    "Project",
    interfaces = interfaces[Ctx, Project](entityInterface),
    () =>
      fields[Ctx, Project](
        Field("id", StringType, resolve = { _.value.id.value }),
        Field("createdAt", dateTimeType, resolve = { _.value.createdAt }),
        Field("name", StringType, resolve = { _.value.name.value }),
        Field("plan", projectPlanType, resolve = { _.value.plan }),
        Field("users", ListType(userType), resolve = { ctx =>
          UserResolver.byProjectId(ctx.value.id)(ctx.ctx)
        }),
        Field("tasks", ListType(taskType), resolve = { ctx =>
          TaskResolver.byProjectId(ctx.value.id)(ctx.ctx)
        })
    )
  )

  implicit lazy val taskType: ObjectType[Ctx, Task] = ObjectType.apply[Ctx, Task](
    "Task",
    interfaces = interfaces[Ctx, Task](entityInterface),
    () =>
      fields[Ctx, Task](
        Field("name", StringType, resolve = { _.value.name.value }),
        Field("description", StringType, resolve = { _.value.description.value }),
        Field("status", taskStatusType, resolve = { _.value.status }),
        Field("projectId", StringType, resolve = { _.value.projectId.value }),
        Field("project", projectType, resolve = { ctx =>
          ProjectResolver.byId(ctx.value.projectId)(ctx.ctx)
        }),
        Field("createdBy", StringType, resolve = { _.value.createdBy.value }),
        Field("createdUser", userType, resolve = { ctx =>
          UserResolver.byId(ctx.value.createdBy)(ctx.ctx)
        }),
        Field("assignedTo", StringType, resolve = { _.value.assignedTo.value }),
        Field("assignedUser", userType, resolve = { ctx =>
          UserResolver.byId(ctx.value.assignedTo)(ctx.ctx)
        })
    )
  )

  private implicit lazy val taskStatusType: EnumType[TaskStatus] =
    derive.deriveEnumType[TaskStatus]()
}

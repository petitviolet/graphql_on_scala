package net.petitviolet.graphql.schemas.types

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import net.petitviolet.graphql.models._
import net.petitviolet.graphql.schemas.Ctx
import sangria.macros.derive
import sangria.schema._

import scala.util.Try

object ObjectTypes {
  implicit val dateTimeType: ScalarType[ZonedDateTime] = {
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

  val entityInterface = InterfaceType[Ctx, Entity](
    "Entity",
    "Entity",
    () =>
      fields[Ctx, Entity](
        Field("id", StringType, resolve = { _.value.id.value }),
        Field("createdAt", dateTimeType, resolve = { _.value.createdAt }),
    )
  )

  implicit lazy val userType: ObjectType[Ctx, User] = ObjectType[Ctx, User](
    "User",
    interfaces = interfaces[Ctx, User](entityInterface),
    () =>
      fields[Ctx, User](
        Field("id", StringType, resolve = { _.value.id.value }),
        Field("createdAt", dateTimeType, resolve = { _.value.createdAt }),
        Field("name", StringType, resolve = { _.value.name.value }),
        Field("projectId", StringType, resolve = { _.value.projectId.value }),
        Field("assignedTasks", ListType(taskType), resolve = { ctx =>
          ???
        }),
        Field("project", projectType, resolve = { ctx =>
          ???
        })
    )
  )

  implicit lazy val projectPlanType = {
    val trial = ObjectType[Ctx, Plan](
      "Trial",
      () => fields[Ctx, Plan](Field("type", StringType, resolve = _ => "Trial"))
    )
    val premium = derive.deriveObjectType[Ctx, Plan.Premium](
      )

    UnionType("Plan", types = trial :: premium :: Nil)
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
          ???
        }),
        Field("tasks", ListType(taskType), resolve = { ctx =>
          ???
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
          ???
        }),
        Field("createdUser", userType, resolve = { ctx =>
          ???
        }),
        Field("assignedUser", userType, resolve = { ctx =>
          ???
        })
    )
  )

  implicit lazy val taskStatusType: EnumType[TaskStatus] = derive.deriveEnumType[TaskStatus]()
}

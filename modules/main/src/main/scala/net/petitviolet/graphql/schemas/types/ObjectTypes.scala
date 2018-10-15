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
  case class Episode()
//  private implicit val episodeType: ObjectType[Unit, Episode] = ???
//
//  case class Character(name: String, appearsIn: Seq[Option[Episode]])
//
//  val characterType: ObjectType[Unit, Character] = ObjectType(
//    "Character",
//    fields[Unit, Character](
//      Field("name", StringType, resolve = { ctx: Context[Unit, Character] =>
//        ctx.value.name
//      }),
//      Field("appearsIn", ListType(OptionType(episodeType)), resolve = {
//        ctx: Context[Unit, Character] =>
//          ctx.value.appearsIn
//      })
//    )
//  )

  private implicit val dateTimeType: ScalarType[ZonedDateTime] = {
    import sangria.validation._
    case object ZonedDateTimeCoercionViolation extends ValueCoercionViolation("DateTime expected")

    val formatStr = "yyyy-MM-dd HH:mm:ss"
    val format = DateTimeFormatter.ofPattern(formatStr)

    val convertString: String => Either[Violation, ZonedDateTime] = str =>
      Try { ZonedDateTime.parse(str, format) }.toEither.left.flatMap { _ =>
        Left(ZonedDateTimeCoercionViolation)
    }

    ScalarType[ZonedDateTime](
      "DateTime",
      description = Some(s"DateTime scalar type. format = $formatStr"),
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
        Field("id", IDType, resolve = { _.value.id.value }),
        Field("createdAt", dateTimeType, resolve = { _.value.createdAt }),
    )
  )

  val userStatusType: EnumType[UserStatus] =
    derive.deriveEnumType[UserStatus]()

  implicit lazy val userType: ObjectType[Ctx, User] = ObjectType[Ctx, User](
    "User",
    interfaces = interfaces[Ctx, User](entityInterface),
    () =>
      fields[Ctx, User](
        Field("id", IDType, resolve = { _.value.id.value }),
        Field("createdAt", dateTimeType, resolve = { _.value.createdAt }),
        Field("name", StringType, resolve = { _.value.name.value }),
        Field("status", userStatusType, resolve = { _.value.status }),
        Field("assignedTasks", ListType(taskType), resolve = { ctx =>
          TaskResolver.byAssignedTo(ctx.value.id)(ctx.ctx)
        }),
        Field("createdTasks", ListType(taskType), resolve = { ctx =>
          TaskResolver.byCreatedBy(ctx.value.id)(ctx.ctx)
        }),
        Field("projectIds", ListType(StringType), resolve = { _.value.projectIds.map { _.value } }),
        Field("projects", ListType(projectType), resolve = { ctx =>
          ProjectResolver.byIds(ctx.value.projectIds)(ctx.ctx)
        })
    )
  )

  implicit lazy val projectType: ObjectType[Ctx, Project] = {
    val usersArgument = Argument("status", OptionInputType(userStatusType))

    implicit val projectPlanType: UnionType[Ctx] = {
      val free = derive.deriveObjectType[Ctx, Plan.Free]()
      val standard = derive.deriveObjectType[Ctx, Plan.Standard]()
      val enterprise = derive.deriveObjectType[Ctx, Plan.Enterprise]()

      UnionType("Plan", types = free :: standard :: enterprise :: Nil)
    }

    ObjectType[Ctx, Project](
      "Project",
      interfaces = interfaces[Ctx, Project](entityInterface),
      () =>
        fields[Ctx, Project](
          Field("id", IDType, resolve = { _.value.id.value }),
          Field("createdAt", dateTimeType, resolve = { _.value.createdAt }),
          Field("name", StringType, resolve = { _.value.name.value }),
          Field("plan", projectPlanType, resolve = { _.value.plan }),
          Field(
            "users",
            ListType(userType),
            arguments = List(usersArgument),
            resolve = { ctx =>
              ctx.withArgs(usersArgument) { statusOpt: Option[UserStatus] =>
                UserResolver.byProjectId(ctx.value.id, statusOpt)(ctx.ctx)
              }
            }
          ),
          Field("tasks", ListType(taskType), resolve = { ctx =>
            TaskResolver.byProjectId(ctx.value.id)(ctx.ctx)
          })
      )
    )
  }

  implicit lazy val taskType: ObjectType[Ctx, Task] = ObjectType.apply[Ctx, Task](
    "Task",
    interfaces = interfaces[Ctx, Task](entityInterface),
    () =>
      fields[Ctx, Task](
        Field("id", IDType, resolve = { _.value.id.value }),
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

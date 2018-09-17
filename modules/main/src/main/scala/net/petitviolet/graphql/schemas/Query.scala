package net.petitviolet.graphql.schemas

import net.petitviolet.graphql.schemas.resolvers.{ ProjectResolver, TaskResolver, UserResolver }
import sangria.schema._
import net.petitviolet.graphql.schemas.types.ObjectTypes._

object Query {
  lazy val forAll: List[Field[Ctx, Unit]] = fields[Ctx, Unit](
    Field("projects", ListType(projectType), resolve = { ctx =>
      ProjectResolver.all()(ctx.ctx)
    }),
    Field("tasks", ListType(taskType), resolve = { ctx =>
      TaskResolver.all()(ctx.ctx)
    }),
    Field("users", ListType(userType), resolve = { ctx =>
      UserResolver.all()(ctx.ctx)
    }),
  )

  lazy val viewer: List[Field[Ctx, Unit]] = {
    val viewerType = ObjectType[Ctx, Unit](
      "ViewerQuery",
      fields[Ctx, Unit](
        Field("user", userType, resolve = { ctx =>
          ctx.ctx.viewer
        }),
        Field("assignedTasks", ListType(taskType), resolve = { ctx =>
          TaskResolver.byAssignedTo(ctx.ctx.viewer.id)(ctx.ctx)
        }),
        Field("createdTasks", ListType(taskType), resolve = { ctx =>
          TaskResolver.byCreatedBy(ctx.ctx.viewer.id)(ctx.ctx)
        })
      ),
    )
    fields[Ctx, Unit](
      Field("viewer", viewerType, resolve = { _ =>
        ()
      })
    )
  }
}

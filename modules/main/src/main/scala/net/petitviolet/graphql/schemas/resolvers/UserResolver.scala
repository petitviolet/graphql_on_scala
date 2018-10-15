package net.petitviolet.graphql.schemas.resolvers

import net.petitviolet.graphql.models.daos.UserDao
import net.petitviolet.graphql.models.{ ProjectId, User, UserId, UserStatus }
import net.petitviolet.graphql.schemas.GraphQLContext
import sangria.execution.deferred._
import sangria.schema.DeferredValue

import scala.concurrent.Future

object UserResolver {
  private val usersByProjects: Relation[User, User, ProjectId] =
    Relation[User, ProjectId]("byProjects", { u: User =>
      u.projectIds
    })

  lazy val userFetcher: Fetcher[GraphQLContext, User, User, UserId] = {
    Fetcher.relCaching(
      { (ctx: GraphQLContext, ids: Seq[UserId]) =>
        UserDao.findByIds(ids)(ctx.ec)
      }, { (ctx: GraphQLContext, rel: RelationIds[User]) =>
        val ids: Seq[ProjectId] = rel.apply(usersByProjects)
        UserDao.findByProjectIds(ids)(ctx.ec)
      }
    )(HasId(_.id))
  }

  def all()(implicit ctx: GraphQLContext): Future[Seq[User]] = {
    UserDao.findAll()
  }

  def byId(userId: UserId)(
      implicit ctx: GraphQLContext): FetcherDeferredOne[GraphQLContext, User, User, UserId] = {
    userFetcher.defer(userId)
  }

  def byProjectId(projectId: ProjectId, statusOpt: Option[UserStatus])(
      implicit ctx: GraphQLContext): DeferredValue[GraphQLContext, Seq[User]] = {
    DeferredValue(userFetcher.deferRelSeq(usersByProjects, projectId)).map { users =>
      statusOpt.fold(users) { status =>
        users.filter { user =>
          user.status == status
        }
      }
    }
  }

}

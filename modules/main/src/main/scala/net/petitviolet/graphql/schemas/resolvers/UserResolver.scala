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

  private implicit lazy val hasId: HasId[User, UserId] = HasId(_.id)

  lazy val userFetcher: Fetcher[GraphQLContext, User, User, UserId] = {
    Fetcher.caching { (ctx: GraphQLContext, ids: Seq[UserId]) =>
      UserDao.findByIds(ids)(ctx.ec)
    }
  }

  lazy val userFetcherByProject: Fetcher[GraphQLContext, User, User, UserId] = {
    Fetcher.relOnlyCaching { (ctx: GraphQLContext, rel: RelationIds[User]) =>
      val ids: Seq[ProjectId] = rel.apply(usersByProjects)
      UserDao.findByProjectIds(ids)(ctx.ec)
    }
  }

  def all()(implicit ctx: GraphQLContext): Future[Seq[User]] = {
    UserDao.findAll()
  }

  def byId(userId: UserId)(
    implicit ctx: GraphQLContext): DeferredValue[GraphQLContext, User] = {
    DeferredValue(userFetcher.defer(userId))
  }

  def byProjectId(projectId: ProjectId, statusOpt: Option[UserStatus])(
      implicit ctx: GraphQLContext): DeferredValue[GraphQLContext, Seq[User]] = {
    DeferredValue(userFetcherByProject.deferRelSeq(usersByProjects, projectId)).map { users =>
      statusOpt.fold(users) { status =>
        users.filter { user =>
          user.status == status
        }
      }
    }
  }

}

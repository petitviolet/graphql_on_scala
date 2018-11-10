package net.petitviolet.graphql.schemas.resolvers

import net.petitviolet.graphql.models.daos.UserDao
import net.petitviolet.graphql.models.{ ProjectId, User, UserId, UserStatus }
import net.petitviolet.graphql.schemas.GraphQLContext
import sangria.execution.deferred._
import sangria.schema.DeferredValue

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

object UserResolver {
  private val cache = new mutable.HashMap[String, Seq[User]]()

  private val usersByProjects: Relation[User, User, ProjectId] =
    Relation[User, ProjectId]("byProjects", { u: User =>
      u.projectIds
    })

  private implicit lazy val hasId: HasId[User, UserId] = HasId(_.id)

  lazy val userFetcher: Fetcher[GraphQLContext, User, User, UserId] = {
    Fetcher.apply { (ctx: GraphQLContext, ids: Seq[UserId]) =>
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
    withCache("user#all") {
      UserDao.findAll()
    }
  }

  private def withCache(key: String)(f: => Future[Seq[User]])(implicit ec: ExecutionContext): Future[Seq[User]] = {
    cache.get(key) map { Future.successful } getOrElse {
      val result = f
      result foreach { res => cache.update(key, res) }
      result
    }
  }

  def byId(userId: UserId)(implicit ctx: GraphQLContext): DeferredValue[GraphQLContext, User] = {
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

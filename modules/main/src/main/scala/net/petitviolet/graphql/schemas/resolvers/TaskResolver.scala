package net.petitviolet.graphql.schemas.resolvers

import net.petitviolet.graphql.models.daos.TaskDao
import net.petitviolet.graphql.models.{ ProjectId, Task, TaskId, UserId }
import net.petitviolet.graphql.schemas.GraphQLContext
import sangria.execution.deferred._
import sangria.schema.DeferredValue

import scala.concurrent.Future

object TaskResolver {
  def all()(implicit ctx: GraphQLContext): Future[Seq[Task]] = {
    TaskDao.findAll()
  }

  private val taskByProject = Relation[Task, ProjectId]("byProject", { task =>
    task.projectId :: Nil
  })

  private val taskByAssignedTo = Relation[Task, UserId]("byAssignedTo", { task =>
    task.assignedTo :: Nil
  })

  private val taskByCreatedBy = Relation[Task, UserId]("byCreatedBy", { task =>
    task.createdBy :: Nil
  })

  implicit val hasId: HasId[Task, TaskId] = HasId(_.id)

  lazy val taskFetcher: Fetcher[GraphQLContext, Task, Task, TaskId] =
    Fetcher.caching { (ctx: GraphQLContext, ids: Seq[TaskId]) =>
      TaskDao.findByIds(ids)(ctx.ec)
    }

  lazy val taskFetcherForProject: Fetcher[GraphQLContext, Task, Task, TaskId] =
    Fetcher.relOnlyCaching { (ctx: GraphQLContext, rel: RelationIds[Task]) =>
      val ids: Seq[ProjectId] = rel(taskByProject)
      TaskDao.findByProjectIds(ids)(ctx.ec)
    }

  lazy val taskFetcherForAssignedTo: Fetcher[GraphQLContext, Task, Task, TaskId] =
    Fetcher.relOnlyCaching { (ctx: GraphQLContext, rel: RelationIds[Task]) =>
      val ids: Seq[UserId] = rel(taskByAssignedTo)
      TaskDao.findByAssignedTos(ids)(ctx.ec)
    }

  lazy val taskFetcherForCreatedBy: Fetcher[GraphQLContext, Task, Task, TaskId] =
    Fetcher.relOnlyCaching { (ctx: GraphQLContext, rel: RelationIds[Task]) =>
      val ids: Seq[UserId] = rel(taskByCreatedBy)
      TaskDao.findByCreatedBys(ids)(ctx.ec)
    }

  def byId(taskId: TaskId)(implicit ctx: GraphQLContext): DeferredValue[GraphQLContext, Task] = {
    DeferredValue(taskFetcher.defer(taskId))
  }

  def byProjectId(projectId: ProjectId)(
      implicit ctx: GraphQLContext): DeferredValue[GraphQLContext, Seq[Task]] = {
    DeferredValue(taskFetcherForProject.deferRelSeq(taskByProject, projectId))
  }

  def byAssignedTo(userId: UserId)(
      implicit ctx: GraphQLContext): DeferredValue[GraphQLContext, Seq[Task]] = {
    DeferredValue(taskFetcherForAssignedTo.deferRelSeq(taskByAssignedTo, userId))
  }

  def byCreatedBy(userId: UserId)(
      implicit ctx: GraphQLContext): DeferredValue[GraphQLContext, Seq[Task]] = {
    DeferredValue(taskFetcherForCreatedBy.deferRelSeq(taskByCreatedBy, userId))
  }

}

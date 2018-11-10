package net.petitviolet.graphql.schemas.resolvers

import net.petitviolet.graphql.models.daos.ProjectDao
import net.petitviolet.graphql.models._
import net.petitviolet.graphql.schemas.GraphQLContext

import scala.concurrent.Future

object ProjectResolver {
  def all()(implicit ctx: GraphQLContext): Future[Seq[Project]] = {
    ProjectDao.findAll()
  }

  def byId(projectId: ProjectId)(implicit ctx: GraphQLContext): Future[Project] = {
    ProjectDao.findById(projectId).forceGetOr(s"project(${projectId.value}) not found.")
  }

  def byIds(projectIds: Seq[ProjectId])(implicit ctx: GraphQLContext): Future[Seq[Project]] = {
    ProjectDao.findByIds(projectIds)
  }
}

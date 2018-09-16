package net.petitviolet.graphql.models.daos

import net.petitviolet.graphql.models._

import scala.concurrent.{ ExecutionContext, Future }

object UserDao extends Dao[UserId, User] {
  def findByProjectId(projectId: ProjectId)(implicit ec: ExecutionContext): Future[Seq[User]] = {
    Future {
      data.collect({ case (_, user) if user.projectId == projectId => user }).toList
    }
  }
}

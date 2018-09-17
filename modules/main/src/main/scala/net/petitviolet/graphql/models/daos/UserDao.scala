package net.petitviolet.graphql.models.daos

import net.petitviolet.graphql.models._

import scala.concurrent.{ ExecutionContext, Future }

object UserDao extends Dao[UserId, User] {
  def authenticate(userId: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    findBy { case (_, user) => user.id.value == userId }
  }

  def findByProjectId(projectId: ProjectId)(implicit ec: ExecutionContext): Future[Seq[User]] = {
    filterBy { case (_, user) => user.projectId == projectId }
  }
}

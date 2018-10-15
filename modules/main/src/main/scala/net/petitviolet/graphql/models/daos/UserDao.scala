package net.petitviolet.graphql.models.daos

import java.time.ZonedDateTime

import net.petitviolet.graphql.models._

import scala.concurrent.{ ExecutionContext, Future }

object UserDao extends Dao[User] {
  private[daos] def init(): Unit = {
    this.data ++= List(
      User(
        UserId("u1"),
        ProjectId("p1") :: Nil,
        UserName("alice"),
        UserStatus.Active,
        ZonedDateTime.now()
      ),
      User(
        UserId("u2"),
        ProjectId("p1") :: Nil,
        UserName("bob"),
        UserStatus.Active,
        ZonedDateTime.now()
      ),
      User(
        UserId("u3"),
        ProjectId("p2") :: Nil,
        UserName("charlie"),
        UserStatus.Paused,
        ZonedDateTime.now()
      ),
    ).map { u =>
      u.id -> u
    }
  }

  // just a toy authn
  def authenticate(userId: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    logger.debug(s"[$tag]authenticate($userId)")
    findBy { case (_, user) => user.id.value == userId }
  }

  def findByProjectId(projectId: ProjectId)(implicit ec: ExecutionContext): Future[Seq[User]] = {
    logger.debug(s"[$tag]findByProjectId($projectId)")
    filterBy { case (_, user) => user.projectIds contains projectId }
  }

  def findByProjectIds(projectIds: Seq[ProjectId])(
      implicit ec: ExecutionContext): Future[Seq[User]] = {
    logger.debug(s"[$tag]findByProjectIds($projectIds)")
    filterBy { case (_, user) => user.projectIds exists projectIds.contains }
  }
}

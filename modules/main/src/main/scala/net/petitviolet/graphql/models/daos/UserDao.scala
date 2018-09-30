package net.petitviolet.graphql.models.daos

import java.time.ZonedDateTime

import net.petitviolet.graphql.models._

import scala.concurrent.{ ExecutionContext, Future }

object UserDao extends Dao[User] {
  private[daos] def init(): Unit = {
    this.data ++= List(
      User(
        UserId("u1"),
        ProjectId("p1"),
        UserName("alice"),
        UserStatus.Active,
        ZonedDateTime.now()
      ),
      User(
        UserId("u2"),
        ProjectId("p1"),
        UserName("bob"),
        UserStatus.Active,
        ZonedDateTime.now()
      ),
      User(
        UserId("u3"),
        ProjectId("p2"),
        UserName("charlie"),
        UserStatus.Paused,
        ZonedDateTime.now()
      ),
    ).map { u =>
      u.id -> u
    }
  }

  def authenticate(userId: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    findBy { case (_, user) => user.id.value == userId }
  }

  def findByProjectId(projectId: ProjectId)(implicit ec: ExecutionContext): Future[Seq[User]] = {
    filterBy { case (_, user) => user.projectId == projectId }
  }
}

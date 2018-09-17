package net.petitviolet.graphql.models.daos

import java.time.{ ZoneId, ZonedDateTime }

import net.petitviolet.graphql.models._

object ProjectDao extends Dao[ProjectId, Project] {
  private[daos] def init(): Unit = {
    this.data ++= List(
      Project(ProjectId("p1"), ProjectName("first-project"), Plan.Trial, ZonedDateTime.now()),
      Project(
        ProjectId("p2"),
        ProjectName("second-project"),
        Plan.Premium(ZonedDateTime.of(2000, 11, 23, 3, 4, 5, 0, ZoneId.systemDefault())),
        ZonedDateTime.now()
      ),
    ).map { p =>
      p.id -> p
    }
  }
}

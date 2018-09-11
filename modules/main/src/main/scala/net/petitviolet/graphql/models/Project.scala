package net.petitviolet.graphql.models

import java.time.ZonedDateTime

case class Project(id: ProjectId, name: ProjectName, plan: Plan, createdAt: ZonedDateTime)
    extends Entity

case class ProjectId(value: String) extends Id

case class ProjectName(value: String)

sealed trait Plan

object Plan {
  case object Trial extends Plan
  case class Premium(contractDate: ZonedDateTime)
}

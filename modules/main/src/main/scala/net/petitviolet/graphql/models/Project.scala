package net.petitviolet.graphql.models

import java.time.ZonedDateTime

case class Project(id: ProjectId, name: ProjectName, plan: Plan, createdAt: ZonedDateTime)
    extends EntityWithId[ProjectId]

case class ProjectId(value: String) extends Id

case class ProjectName(value: String)

sealed trait Plan

object Plan {
  case class Free(startDate: ZonedDateTime) extends Plan
  case class Standard(contractDate: ZonedDateTime)
  case class Enterprise(userLimit: Int, contractDate: ZonedDateTime) extends Plan
}

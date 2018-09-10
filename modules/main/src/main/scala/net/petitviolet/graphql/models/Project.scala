package net.petitviolet.graphql.models

import java.time.ZonedDateTime

case class Project(id: ProjectId, name: ProjectName, createdAt: ZonedDateTime)

case class ProjectId(value: String)

case class ProjectName(value: String)

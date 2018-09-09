package net.petitviolet.graphql.models.users

import java.time.ZonedDateTime

import net.petitviolet.graphql.models.projects.ProjectId

case class User(id: UserId, projectId: ProjectId, name: UserName, createdAt: ZonedDateTime)

case class UserId(value: String)
case class UserName(value: String)

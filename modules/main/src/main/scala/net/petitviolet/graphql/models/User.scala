package net.petitviolet.graphql.models

import java.time.ZonedDateTime

case class User(id: UserId, projectId: ProjectId, name: UserName, createdAt: ZonedDateTime)

case class UserId(value: String)
case class UserName(value: String)

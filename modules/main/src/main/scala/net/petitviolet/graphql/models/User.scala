package net.petitviolet.graphql.models

import java.time.ZonedDateTime

case class User(id: UserId, projectId: ProjectId, name: UserName, createdAt: ZonedDateTime)
    extends EntityWithId[UserId] {}

case class UserId(value: String) extends Id
case class UserName(value: String)

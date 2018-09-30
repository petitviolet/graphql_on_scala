package net.petitviolet.graphql.models

import java.time.ZonedDateTime

case class User(id: UserId,
                projectId: ProjectId,
                name: UserName,
                status: UserStatus,
                createdAt: ZonedDateTime)
    extends EntityWithId[UserId] {}

case class UserId(value: String) extends Id
case class UserName(value: String)

sealed abstract class UserStatus(val value: Int)

object UserStatus {
  case object Active extends UserStatus(1)
  case object Paused extends UserStatus(2)
}

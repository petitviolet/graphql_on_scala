package net.petitviolet.graphql.models

import java.time.ZonedDateTime

trait Id {
  def value: String
}
trait Entity {
  type ID <: Id
  def id: ID
  def createdAt: ZonedDateTime
}

trait EntityWithId[id <: Id] extends Entity {
  override type ID = id
}

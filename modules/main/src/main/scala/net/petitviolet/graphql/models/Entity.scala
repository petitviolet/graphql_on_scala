package net.petitviolet.graphql.models

import java.time.ZonedDateTime

trait Id {
  def value: String
}
trait Entity {
  def id: Id
  def createdAt: ZonedDateTime
}

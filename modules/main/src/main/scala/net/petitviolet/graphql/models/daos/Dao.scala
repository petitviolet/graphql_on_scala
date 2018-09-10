package net.petitviolet.graphql.models.daos

import scala.collection.mutable

trait Dao[Id, E] {
  protected lazy val data: mutable.Map[Id, E] = new mutable.HashMap

  def findById(id: Id): Option[E] = data.get(id)

  def findByIds(ids: Seq[Id]): Seq[E] = ids collect data
}

package net.petitviolet.graphql.models.daos

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

trait Dao[Id, E] {
  protected lazy val data: mutable.Map[Id, E] = new mutable.HashMap

  def findById(id: Id)(implicit ec: ExecutionContext): Future[Option[E]] = Future { data.get(id) }

  def findByIds(ids: Seq[Id])(implicit ec: ExecutionContext): Future[Seq[E]] = Future {
    ids collect data
  }
}

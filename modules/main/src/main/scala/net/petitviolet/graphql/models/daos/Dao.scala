package net.petitviolet.graphql.models.daos

import net.petitviolet.graphql.models.Entity

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

trait Dao[E <: Entity] {
  protected lazy val data: mutable.Map[E#ID, E] = new mutable.HashMap

  protected def filterBy(f: ((E#ID, E)) => Boolean)(
      implicit ec: ExecutionContext): Future[List[E]] =
    Future { data.collect { case kv if f(kv) => kv._2 }.toList }

  protected def findBy(f: ((E#ID, E)) => Boolean)(
      implicit ec: ExecutionContext): Future[Option[E]] =
    Future { data.collectFirst { case kv if f(kv) => kv._2 } }

  def findAll()(implicit ec: ExecutionContext): Future[Seq[E]] = Future { data.values.toList }

  def findById(id: E#ID)(implicit ec: ExecutionContext): Future[Option[E]] = Future { data.get(id) }

  def findByIds(ids: Seq[E#ID])(implicit ec: ExecutionContext): Future[Seq[E]] = Future {
    ids collect data
  }

  def store(entity: E)(implicit ec: ExecutionContext): Future[E] = {
    Future { data.update(entity.id, entity) } map { _ =>
      entity
    }
  }
}

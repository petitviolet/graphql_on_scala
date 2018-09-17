package net.petitviolet.graphql.models.daos

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

trait Dao[Id, E] {
  protected lazy val data: mutable.Map[Id, E] = new mutable.HashMap

  protected def filterBy(f: ((Id, E)) => Boolean)(implicit ec: ExecutionContext): Future[List[E]] =
    Future { data.collect { case kv if f(kv) => kv._2 }.toList }

  protected def findBy(f: ((Id, E)) => Boolean)(implicit ec: ExecutionContext): Future[Option[E]] =
    Future { data.collectFirst { case kv if f(kv) => kv._2 } }

  def findAll()(implicit ec: ExecutionContext): Future[Seq[E]] = Future { data.values.toList }

  def findById(id: Id)(implicit ec: ExecutionContext): Future[Option[E]] = Future { data.get(id) }

  def findByIds(ids: Seq[Id])(implicit ec: ExecutionContext): Future[Seq[E]] = Future {
    ids collect data
  }
}

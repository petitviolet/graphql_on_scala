package net.petitviolet.graphql

import java.util.concurrent.Executors

import sangria.macros.derive
import sangria.marshalling._
import sangria.schema._

import scala.concurrent.{ ExecutionContext, Future }

object sample extends SampleApp(GraphQLServer)

private object GraphQLServer extends GraphQLServerBase {
  override type Ctx = SchemaSample.GraphQLContext

  override protected def schema = SchemaSample.schema

  override protected def context = new SchemaSample.GraphQLContext()

}

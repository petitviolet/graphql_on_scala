package net.petitviolet.graphql

import net.petitviolet.graphql.schemas.GraphQLContext

object sample extends SampleApp(GraphQLServer)

private object GraphQLServer extends GraphQLServerBase {
  override type Ctx = GraphQLContext

  override protected def schema = schemas.schema

  override protected def context = new GraphQLContext()

}

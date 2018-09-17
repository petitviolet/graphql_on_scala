package net.petitviolet.graphql

import sangria.schema.{ ObjectType, Schema }

package object schemas {
  type Ctx = GraphQLContext

  private lazy val query: ObjectType[Ctx, Unit] = ObjectType(
    "Query",
    "Query",
    fields = Query.forAll ++ Query.viewer
  )

  private lazy val mutation: ObjectType[Ctx, Unit] = ObjectType(
    "Mutation",
    "Mutation",
    fields = ???
  )

  // lazy val schema: Schema[Ctx, Unit] = Schema(query, Some(mutation))
  lazy val schema: Schema[Ctx, Unit] = Schema(query, None)
}

package net.petitviolet.graphql.schemas

import sangria.schema.Field

object Query {
  lazy val forAll: List[Field[Ctx, Unit]] = ???

  lazy val viewer: List[Field[Ctx, Unit]] = ???
}

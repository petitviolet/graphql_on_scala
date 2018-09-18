package net.petitviolet.graphql.schemas

import net.petitviolet.graphql.schemas.mutations._
import sangria.schema._

object Mutations {
  lazy val field: List[Field[Ctx, Unit]] =
    mutations.foldLeft(List.empty[Field[Ctx, Unit]]) { (l, m) =>
      m.field :: l
    }

  private def mutations: List[Mutation] = List(
    CreateTask,
    UpdateTask
  )
}

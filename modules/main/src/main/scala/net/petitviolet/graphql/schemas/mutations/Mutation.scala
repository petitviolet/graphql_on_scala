package net.petitviolet.graphql.schemas.mutations

import net.petitviolet.graphql.schemas.Ctx
import sangria.marshalling.{ sprayJson, FromInput, ResultMarshaller }
import sangria.schema.Field
import spray.json.{ DefaultJsonProtocol, JsValue, JsonReader }

trait Mutation extends DefaultJsonProtocol {
  def field: Field[Ctx, Unit]

  protected implicit def fromInput[A: JsonReader]: FromInput[A] = new FromInput[A] {
    val marshaller: ResultMarshaller = sprayJson.SprayJsonResultMarshaller

    override def fromResult(node: marshaller.Node): A = {
      node.asInstanceOf[JsValue].convertTo[A]
    }
  }
}

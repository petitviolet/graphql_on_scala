package net.petitviolet.graphql.schemas.mutations

import net.petitviolet.graphql.schemas.Ctx
import sangria.marshalling.{ sprayJson, FromInput, ResultMarshaller }
import sangria.schema.{ Context, Field }
import spray.json.{ DefaultJsonProtocol, JsValue, JsonReader }

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

trait Mutation extends DefaultJsonProtocol {

  protected implicit def ec(implicit ctx: Context[Ctx, Unit]): ExecutionContext = ctx.ctx.ec

  def field: Field[Ctx, Unit]

  protected implicit def fromInput[A: JsonReader]: FromInput[A] = new FromInput[A] {
    val marshaller: ResultMarshaller = sprayJson.SprayJsonResultMarshaller

    override def fromResult(node: marshaller.Node): A = {
      node.asInstanceOf[JsValue].convertTo[A]
    }
  }
}

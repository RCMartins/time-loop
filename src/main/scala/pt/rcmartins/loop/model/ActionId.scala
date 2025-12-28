package pt.rcmartins.loop.model

import zio.json.{JsonDecoder, JsonEncoder}

case class ActionId(id: Long)

object ActionId {

  implicit val decoder: JsonDecoder[ActionId] = JsonDecoder.long.map(ActionId(_))
  implicit val encoder: JsonEncoder[ActionId] = JsonEncoder.long.contramap[ActionId](_.id)

}

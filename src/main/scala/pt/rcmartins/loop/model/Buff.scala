package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

sealed trait Buff {

  val id: Long

}

object Buff {

  case class TirednessMultiplier(id: Long, multiplier: Double) extends Buff

  implicit val encoder: JsonEncoder[Buff] = DeriveJsonEncoder.gen[Buff]
  implicit val decoder: JsonDecoder[Buff] = DeriveJsonDecoder.gen[Buff]

}

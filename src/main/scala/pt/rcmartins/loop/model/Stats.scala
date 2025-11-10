package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Stats(
    loopNumber: Int,
)

object Stats {

  val initial: Stats = Stats(
    loopNumber = 0,
  )

  implicit val decoder: JsonDecoder[Stats] = DeriveJsonDecoder.gen[Stats]
  implicit val encoder: JsonEncoder[Stats] = DeriveJsonEncoder.gen[Stats]

}

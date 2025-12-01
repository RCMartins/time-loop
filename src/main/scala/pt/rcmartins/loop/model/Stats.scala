package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Stats(
    loopNumber: Int,
    usedCheats: Boolean,
) {

  def resetForNewLoop: Stats =
    this.copy(
      loopNumber = this.loopNumber + 1,
    )

}

object Stats {

  val initial: Stats =
    Stats(
      loopNumber = 1,
      usedCheats = false,
    )

  implicit val decoder: JsonDecoder[Stats] = DeriveJsonDecoder.gen[Stats]
  implicit val encoder: JsonEncoder[Stats] = DeriveJsonEncoder.gen[Stats]

}

package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class StatsSaved(
    loopNumber: Int,
    totalElapedTimeMicro: Long,
    usedCheats: Boolean,
    loopActionCount: Seq[(ActionDataType, Int)],
    globalActionCount: Seq[(ActionDataType, Int)],
) {
  def toStats: Stats =
    Stats(
      loopNumber = loopNumber,
      totalElapedTimeMicro = totalElapedTimeMicro,
      usedCheats = usedCheats,
      loopActionCount = loopActionCount.toMap,
      globalActionCount = globalActionCount.toMap,
    )

}

object StatsSaved {

  def fromStats(stats: Stats): StatsSaved =
    StatsSaved(
      loopNumber = stats.loopNumber,
      totalElapedTimeMicro = stats.totalElapedTimeMicro,
      usedCheats = stats.usedCheats,
      loopActionCount = stats.loopActionCount.toSeq,
      globalActionCount = stats.globalActionCount.toSeq,
    )

  implicit val decoder: JsonDecoder[StatsSaved] = DeriveJsonDecoder.gen[StatsSaved]
  implicit val encoder: JsonEncoder[StatsSaved] = DeriveJsonEncoder.gen[StatsSaved]

}

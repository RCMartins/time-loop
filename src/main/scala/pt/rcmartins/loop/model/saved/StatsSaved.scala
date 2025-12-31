package pt.rcmartins.loop.model.saved

import pt.rcmartins.loop.model.{ActionId, Stats}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class StatsSaved(
    loopNumber: Int,
    totalElapedTimeMicro: Long,
    usedCheats: Boolean,
    loopActionCount: Seq[(ActionId, Int)],
    globalActionCount: Seq[(ActionId, Int)],
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

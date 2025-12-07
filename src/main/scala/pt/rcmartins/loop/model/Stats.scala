package pt.rcmartins.loop.model

case class Stats(
    loopNumber: Int,
    totalElapedTimeMicro: Long,
    usedCheats: Boolean,
    loopActionCount: Map[ActionDataType, Int],
    globalActionCount: Map[ActionDataType, Int],
) {

  println((loopNumber, usedCheats, loopActionCount, globalActionCount))

  def resetForNewLoop: Stats =
    this.copy(
      loopNumber = this.loopNumber + 1,
      loopActionCount = Map.empty,
    )

  def getLoopCount(actionDataType: ActionDataType): Int =
    loopActionCount.getOrElse(actionDataType, 0)

}

object Stats {

  val initial: Stats =
    Stats(
      loopNumber = 1,
      totalElapedTimeMicro = 0L,
      usedCheats = false,
      loopActionCount = Map.empty,
      globalActionCount = Map.empty,
    )

}

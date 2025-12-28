package pt.rcmartins.loop.model

case class Stats(
    loopNumber: Int,
    totalElapedTimeMicro: Long,
    usedCheats: Boolean,
    loopActionCount: Map[ActionId, Int],
    globalActionCount: Map[ActionId, Int],
) {

  def resetForNewLoop: Stats =
    this.copy(
      loopNumber = this.loopNumber + 1,
      loopActionCount = Map.empty,
    )

  def getLoopCount(actionId: ActionId): Int =
    loopActionCount.getOrElse(actionId, 0)

  def getGlobalCount(actionId: ActionId): Int =
    globalActionCount.getOrElse(actionId, 0)

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

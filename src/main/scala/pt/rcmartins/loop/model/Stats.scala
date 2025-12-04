package pt.rcmartins.loop.model

case class Stats(
    loopNumber: Int,
    usedCheats: Boolean,
    loopActionCount: Map[ActionDataType, Int],
    globalActionCount: Map[ActionDataType, Int],
) {

  def resetForNewLoop: Stats =
    this.copy(
      loopNumber = this.loopNumber + 1,
      loopActionCount = Map.empty,
    )

}

object Stats {

  val initial: Stats =
    Stats(
      loopNumber = 1,
      usedCheats = false,
      loopActionCount = Map.empty,
      globalActionCount = Map.empty,
    )

}

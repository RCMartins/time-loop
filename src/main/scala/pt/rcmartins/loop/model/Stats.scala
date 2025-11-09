package pt.rcmartins.loop.model

case class Stats(
    loopNumber: Int,
)

object Stats {

  val initial: Stats = Stats(
    loopNumber = 0,
  )

}

package pt.rcmartins.loop.model

sealed trait ActionTime {

  def baseTimeSec: Long

}

object ActionTime {

  case class Standard(baseTimeSec: Long) extends ActionTime

  case class LinearTime(baseTimeSec: Long, increaseTimeSec: Long) extends ActionTime

  case class ReduzedXP(baseTimeSec: Long, xpMultiplier: Double) extends ActionTime

}

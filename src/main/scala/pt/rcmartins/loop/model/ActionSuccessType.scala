package pt.rcmartins.loop.model

sealed trait ActionSuccessType

object ActionSuccessType {

  case object Always extends ActionSuccessType

  case class WithFailure(baseChance: Double, increase: Double) extends ActionSuccessType

}

package pt.rcmartins.loop.model

sealed trait PermanentBonus

object PermanentBonus {

  case object HalfTiredness extends PermanentBonus

}

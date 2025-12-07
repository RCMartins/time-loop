package pt.rcmartins.loop.model

sealed trait PermanentBonus {

  def description: String

}

object PermanentBonus {

  case object HalfTiredness extends PermanentBonus {
    override val description: String =
      "Reduced tiredness by half.\n(does not prevent from it increasing over time)."

  }

}

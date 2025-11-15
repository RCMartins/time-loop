package pt.rcmartins.loop.model

sealed trait AmountOfActions {

  def moreThanOne: Boolean

  def reduceOne: AmountOfActions

}

object AmountOfActions {

  case class Standard(amount: Int) extends AmountOfActions {
    def moreThanOne: Boolean = amount > 1
    def reduceOne: AmountOfActions = Standard(amount - 1)
  }

  case object Unlimited extends AmountOfActions {
    val moreThanOne: Boolean = true
    val reduceOne: AmountOfActions = Unlimited
  }

}

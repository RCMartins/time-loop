package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

sealed trait AmountOfActions {

  def moreThanOne: Boolean

  def reduceOne: AmountOfActions

  def singleAction: Boolean

}

object AmountOfActions {

  case class Standard(amount: Int) extends AmountOfActions {
    def moreThanOne: Boolean = amount > 1
    def reduceOne: AmountOfActions = Standard(amount - 1)
    def singleAction: Boolean = amount == 1
  }

  case object Unlimited extends AmountOfActions {
    val moreThanOne: Boolean = true
    val reduceOne: AmountOfActions = Unlimited
    val singleAction: Boolean = false
  }


  implicit val decoder: JsonDecoder[AmountOfActions] = DeriveJsonDecoder.gen[AmountOfActions]
  implicit val encoder: JsonEncoder[AmountOfActions] = DeriveJsonEncoder.gen[AmountOfActions]

}

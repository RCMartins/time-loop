package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

sealed trait ActionKind {
  def name: String
}

object ActionKind {
  case object Agility extends ActionKind { val name: String = "Agility" }
  case object Exploring extends ActionKind { val name: String = "Exploring" }
  case object Foraging extends ActionKind { val name: String = "Foraging" }
  case object Social extends ActionKind { val name: String = "Social" }
  case object Crafting extends ActionKind { val name: String = "Crafting" }
  case object Gardening extends ActionKind { val name: String = "Gardening" }
  case object Cooking extends ActionKind { val name: String = "Cooking" }
  case object Magic extends ActionKind { val name: String = "Magic" }

  implicit val decoder: JsonDecoder[ActionKind] = DeriveJsonDecoder.gen[ActionKind]
  implicit val encoder: JsonEncoder[ActionKind] = DeriveJsonEncoder.gen[ActionKind]

}

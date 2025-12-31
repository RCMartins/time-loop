package pt.rcmartins.loop.model

import pt.rcmartins.loop.Constants
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

sealed trait ActionKind {
  def name: String
  def icon: String
}

object ActionKind {
  case object Agility extends ActionKind {
    val name: String = "Agility"
    val icon: String = Constants.SkillIcons.Agility
  }

  case object Exploring extends ActionKind {
    val name: String = "Exploring"
    val icon: String = Constants.SkillIcons.Exploring
  }

  case object Foraging extends ActionKind {
    val name: String = "Foraging"
    val icon: String = Constants.SkillIcons.Foraging
  }

  case object Social extends ActionKind {
    val name: String = "Social"
    val icon: String = Constants.SkillIcons.Social
  }

  case object Crafting extends ActionKind {
    val name: String = "Crafting"
    val icon: String = Constants.SkillIcons.Crafting
  }

  case object Gardening extends ActionKind {
    val name: String = "Gardening"
    val icon: String = Constants.SkillIcons.Gardening
  }

  case object Cooking extends ActionKind {
    val name: String = "Cooking"
    val icon: String = Constants.SkillIcons.Cooking
  }

  case object Magic extends ActionKind {
    val name: String = "Magic"
    val icon: String = Constants.SkillIcons.Magic
  }

  implicit val decoder: JsonDecoder[ActionKind] = DeriveJsonDecoder.gen[ActionKind]
  implicit val encoder: JsonEncoder[ActionKind] = DeriveJsonEncoder.gen[ActionKind]

}

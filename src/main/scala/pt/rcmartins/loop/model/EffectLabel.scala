package pt.rcmartins.loop.model

sealed trait EffectLabel {

  def label: String

}

object EffectLabel {

  case object Bug extends EffectLabel {
    override val label: String = "Bug: No effect label defined."
  }

  case object Empty extends EffectLabel {
    override val label: String = ""
  }

  case object Explore extends EffectLabel {
    override val label: String = "Explore area for activities/resources."
  }

  case object Movement extends EffectLabel {
    override val label: String = "Move to a new location."
  }

  case object TalkAboutSoap extends EffectLabel {
    override val label: String = "Talk with people about your soaps."
  }

  case object SellSoap extends EffectLabel {
    override val label: String = "Sell your soaps."
  }

  case class Cooking(itemType: ItemType, amount: Int) extends EffectLabel {
    override val label: String = s"Cook $amount ${itemType.name}."
  }

  case class GetItem(itemType: ItemType, amount: Int) extends EffectLabel {
    override val label: String = s"Get $amount ${itemType.name}."
  }

  case class BuyItem(itemType: ItemType, amount: Int, cost: Int) extends EffectLabel {
    override val label: String = s"Buy $amount ${itemType.name} for $cost coins."
  }

  case class BuyEmptyStore(cost: Int) extends EffectLabel {
    override val label: String = s"Buy Empty Store for $cost coins."
  }

  case class CraftItem(itemType: ItemType, amount: Int, cost: Seq[(ItemType, Int)])
      extends EffectLabel {
    override val label: String = {
      val costStr: String =
        cost.map { case (it, ct) => s"$ct ${it.name}" }.mkString(" and ")
      s"Buy $amount ${itemType.name} for $costStr."
    }

  }

}

package pt.rcmartins.loop.model

import pt.rcmartins.loop.model.ItemType.Coins

sealed trait EffectLabel {

  def label: String

}

object EffectLabel {

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

  case object GetSoapMold extends EffectLabel {
    override val label: String = "Allows making soaps from glycerin."
  }

  case class Cooking(itemType: ItemType, amount: Int) extends EffectLabel {
    override val label: String = s"Cook ${itemType.amountFormatInv(amount)} ${itemType.name}."
  }

  case class GetInventoryIncrease(name: String, maxSize: Int) extends EffectLabel {
    override val label: String = s"Get $name, max size $maxSize."
  }

  case class BuyInventoryIncrease(name: String, cost: Int, maxSize: Int) extends EffectLabel {
    override val label: String = s"Cost: ${Coins.amountFormatInv(cost)}, max size $maxSize."
  }

  case class GetItem(itemType: ItemType, amount: Int) extends EffectLabel {
    override val label: String = s"Get ${itemType.amountFormatInv(amount)} ${itemType.name}."
  }

  case class BuyItem(itemType: ItemType, amount: Int, cost: Int) extends EffectLabel {
    override val label: String = s"Cost: ${Coins.amountFormatInv(cost)}"
  }

  case class SellItem(itemType: ItemType, amount: Int, coinsGain: Int) extends EffectLabel {
    override val label: String = s"Sell for ${Coins.amountFormatInv(coinsGain)}."
  }

  case class BuyUpgrade(cost: Int) extends EffectLabel {
    override val label: String = s"Cost: ${Coins.amountFormatInv(cost)}"
  }

  case class CraftItem(itemType: ItemType, amount: Int, cost: Seq[(ItemType, Int)])
      extends EffectLabel {
    override val label: String = {
      if (cost.isEmpty)
        s"Get $amount ${itemType.name}."
      else {
        val costStr: String =
          cost.map { case (it, ct) => s"$ct ${it.name}" }.mkString(" and ")
        s"Cost: $costStr."
      }
    }

  }

  private def plural(n: Int): String = if (n == 1) "" else "s"

}

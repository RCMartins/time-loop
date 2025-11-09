package pt.rcmartins.loop.model

sealed trait EffectLabel {

  def label: String

}

object EffectLabel {

  case object Bug extends EffectLabel {
    override def label: String = "Bug: No effect label defined."
  }

  case object Explore extends EffectLabel {
    override def label: String = "Explore new areas for activities/resources."
  }

  case object Movement extends EffectLabel {
    override def label: String = "Move to a new location."
  }

  case class GetItem(itemType: ItemType, amount: Int) extends EffectLabel {
    override def label: String = s"Get $amount ${itemType.name}"
  }

}

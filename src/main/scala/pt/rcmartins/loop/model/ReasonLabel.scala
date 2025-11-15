package pt.rcmartins.loop.model

sealed trait ReasonLabel {

  def label: String

}

object ReasonLabel {

  case object Empty extends ReasonLabel {
    override def label: String = ""
  }

  case object InventoryFull extends ReasonLabel {
    override def label: String = "Inventory is full."
  }

  case object NotEnoughCoins extends ReasonLabel {
    override def label: String = "Not enough coins."
  }

  case object NotEnoughResources extends ReasonLabel {
    override def label: String = "Not enough resources."
  }

  case object NotEnoughSoapToSell extends ReasonLabel {
    override def label: String = "Not enough soap to sell."
  }

  case object MustExploreHouseFirst extends ReasonLabel {
    override def label: String = "You need to explore other areas of the house first."
  }

}

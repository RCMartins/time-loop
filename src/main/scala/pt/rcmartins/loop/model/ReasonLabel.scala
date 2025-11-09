package pt.rcmartins.loop.model

sealed trait ReasonLabel {

  def label: String

}

object ReasonLabel {

  case object InventoryFull extends ReasonLabel {
    override def label: String = "Inventory is full."
  }

  case object MustExploreHouseFirst extends ReasonLabel {
    override def label: String = "You need to explore other areas of the house first."
  }

}

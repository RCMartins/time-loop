package pt.rcmartins.loop.model

case class InventoryState(
    itemsFood: Seq[(ItemType, Int)],
    others: Seq[(ItemType, Int)],
) {

  def allItems: Seq[(ItemType, Int)] = itemsFood ++ others

}

object InventoryState {

  val initial: InventoryState = InventoryState(
    itemsFood = Seq(),
    others = Seq(),
  )

}

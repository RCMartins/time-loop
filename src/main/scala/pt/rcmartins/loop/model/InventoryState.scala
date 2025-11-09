package pt.rcmartins.loop.model

case class InventoryState(
    maximumSize: Int,
    items: Seq[(ItemType, Int)],
) {

  def getItemAmount(itemType: ItemType): Int =
    items.find(_._1 == itemType).map(_._2).getOrElse(0)

  def canAddItem(itemType: ItemType, amount: Int): Boolean =
    itemType.noInventoyLimit ||
      getItemAmount(itemType) + amount <= maximumSize

  def addItem(itemType: ItemType, amount: Int): InventoryState =
    if (canAddItem(itemType, amount))
      this.copy(
        items = items.filterNot(_._1 == itemType) :+ (itemType, getItemAmount(itemType) + amount)
      )
    else
      this

  def increaseInventorySize(amount: Int): InventoryState =
    this.copy(maximumSize = maximumSize + amount)

}

object InventoryState {

  val initial: InventoryState = InventoryState(
    maximumSize = 1,
    items = Seq(),
  )

}

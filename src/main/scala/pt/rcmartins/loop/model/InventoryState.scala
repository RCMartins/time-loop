package pt.rcmartins.loop.model

case class InventoryState(
    maximumSize: Int,
    items: Seq[(ItemType, Int, Long)],
) {

  private def getItemAmount(itemType: ItemType): Int =
    items.find(_._1 == itemType).map(_._2).getOrElse(0)

  def canAddItem(itemType: ItemType, amount: Int): Boolean =
    itemType.noInventoyLimit ||
      getItemAmount(itemType) + amount <= maximumSize

  def canRemoveItem(itemType: ItemType, amount: Int): Boolean =
    getItemAmount(itemType) - amount >= 0

  def addItem(itemType: ItemType, amount: Int): InventoryState =
    if (canAddItem(itemType, amount)) {
      items.find(_._1 == itemType) match {
        case None =>
          this.copy(
            items = (items :+ (itemType, amount, 0L)).sortBy(_._1.inventoryOrder)
          )
        case Some(_) =>
          this.copy(
            items = items.map {
              case (`itemType`, currentAmount, cooldown) =>
                (itemType, currentAmount + amount, cooldown)
              case other =>
                other
            }
          )
      }
    } else
      this

  def removeItem(itemType: ItemType, amount: Int): InventoryState =
    items.find(_._1 == itemType) match {
      case None =>
        this // Item not found, nothing to remove ?
      case Some(_) =>
        this.copy(
          items = items.map {
            case (`itemType`, currentAmount, cooldown) =>
              (itemType, currentAmount - amount, cooldown)
            case other =>
              other
          }
        )
    }

  def increaseInventorySizeTo(amount: Int): InventoryState =
    this.copy(maximumSize = Math.max(maximumSize, amount))

}

object InventoryState {

  val initial: InventoryState = InventoryState(
    maximumSize = 1,
    items = Seq(),
  )

}

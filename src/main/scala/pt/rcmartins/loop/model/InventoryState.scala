package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

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

  def addMultipleItems(newItems: Seq[(ItemType, Int)]): InventoryState =
    newItems.foldLeft(this) { case (inventory, (itemType, amount)) =>
      inventory.addItem(itemType, amount)
    }

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

  def removeMultipleItems(itemsToRemove: Seq[(ItemType, Int)]): InventoryState =
    itemsToRemove.foldLeft(this) { case (inventory, (itemType, amount)) =>
      inventory.removeItem(itemType, amount)
    }

  def increaseInventorySizeTo(amount: Int): InventoryState =
    this.copy(maximumSize = Math.max(maximumSize, amount))

}

object InventoryState {

  val initial: InventoryState = InventoryState(
    maximumSize = 5,
    items = Seq(),
  )

  implicit val decoder: JsonDecoder[InventoryState] = DeriveJsonDecoder.gen[InventoryState]
  implicit val encoder: JsonEncoder[InventoryState] = DeriveJsonEncoder.gen[InventoryState]

}

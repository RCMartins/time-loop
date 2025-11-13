package pt.rcmartins.loop.model

sealed trait ItemType {
  val visible: Boolean = true
  val noInventoyLimit: Boolean = false
  val name: String
  val inventoryOrder: Int

  val foodValueLong: Long
  def isFoodItem: Boolean = foodValueLong > 0
  def foodValueMicro: Long = foodValueLong * 1_000_000L
}

object ItemType {

  // Misc Items
  case object Backpack extends ItemType {
    val name: String = "Backpack"
    override val visible: Boolean = false
    val inventoryOrder: Int = 0
    val foodValueLong: Long = 0L
  }

  // Coins
  case object Coins extends ItemType {
    val name: String = "Coins"
    override val noInventoyLimit: Boolean = true
    val inventoryOrder: Int = 1
    val foodValueLong: Long = 0L
  }

  // Food Items
  case object Rice extends ItemType {
    val name: String = "Rice"
    val inventoryOrder: Int = 2
    val foodValueLong: Long = 5L
  }

  case object Momo extends ItemType {
    val name: String = "Momo"
    val inventoryOrder: Int = 3
    val foodValueLong: Long = 8L
  }

  case object Curry extends ItemType {
    val name: String = "Curry"
    val inventoryOrder: Int = 4
    val foodValueLong: Long = 12L
  }

  case object Chatpate extends ItemType {
    val name: String = "Chatpate"
    val inventoryOrder: Int = 5
    val foodValueLong: Long = 15L
  }

  case object Panipuri extends ItemType {
    val name: String = "Panipuri"
    val inventoryOrder: Int = 6
    val foodValueLong: Long = 20L
  }

}

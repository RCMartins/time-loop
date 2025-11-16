package pt.rcmartins.loop.model

sealed trait ItemType {
  val visible: Boolean = true
  val noInventoyLimit: Boolean = false
  val name: String
  val inventoryOrder: Int

  def foodValueLong: Long
  lazy val isFoodItem: Boolean = foodValueLong > 0
  lazy val foodValueMicro: Long = foodValueLong * 1_000_000L
}

object ItemType {

  // Misc Items
  case object Backpack extends ItemType {
    val name: String = "Backpack"
    override val visible: Boolean = false
    val inventoryOrder: Int = 0
    val foodValueLong: Long = 0L
  }

  case object Coins extends ItemType {
    val name: String = "Coins"
    override val noInventoyLimit: Boolean = true
    val inventoryOrder: Int = 1
    val foodValueLong: Long = 0L
  }

  case object Glycerin extends ItemType {
    val name: String = "Glycerin Based Soap"
    val inventoryOrder: Int = 2
    val foodValueLong: Long = 0L
  }

  case object MeltedGlycerin extends ItemType {
    val name: String = "Melted Glycerin"
    val inventoryOrder: Int = 3
    val foodValueLong: Long = 0L
  }

  case object GardenHerb extends ItemType {
    val name: String = "Garden Herb"
    val inventoryOrder: Int = 5
    val foodValueLong: Long = 0L
  }

  case object HotMoldedSoap extends ItemType {
    val name: String = "Hot Molded Soap"
    val inventoryOrder: Int = 4
    val foodValueLong: Long = 0L
  }

  case object HerbSoap extends ItemType {
    val name: String = "Herb Soap"
    val inventoryOrder: Int = 4
    val foodValueLong: Long = 0L
  }

  // Raw Food Items

  case object RawMomo extends ItemType {
    val name: String = "Raw Momo"
    val inventoryOrder: Int = 11
    val foodValueLong: Long = 0L
  }

  // Food Items
  case object Rice extends ItemType {
    val name: String = "Rice"
    val inventoryOrder: Int = 21
    val foodValueLong: Long = 5L
  }

  case object Momo extends ItemType {
    val name: String = "Momo"
    val inventoryOrder: Int = 22
    val foodValueLong: Long = 8L
  }

  case object Curry extends ItemType {
    val name: String = "Curry"
    val inventoryOrder: Int = 23
    val foodValueLong: Long = 12L
  }

  case object Chatpate extends ItemType {
    val name: String = "Chatpate"
    val inventoryOrder: Int = 24
    val foodValueLong: Long = 15L
  }

  case object Panipuri extends ItemType {
    val name: String = "Panipuri"
    val inventoryOrder: Int = 25
    val foodValueLong: Long = 20L
  }

}

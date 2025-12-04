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

  // Coins

  case object Coins extends ItemType {
    val name: String = "Coins"
    override val noInventoyLimit: Boolean = true
    val inventoryOrder: Int = 1
    val foodValueLong: Long = 0L
  }

  // Misc Items

  case object SimpleSoapMold extends ItemType {
    val name: String = "Simple Soap Mold"
    val inventoryOrder: Int = 1
    val foodValueLong: Long = 0L
  }

  case object GoodSoapMold extends ItemType {
    val name: String = "Good Soap Mold"
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

  case object Mint extends ItemType {
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

  case object PrettyFlower extends ItemType {
    val name: String = "Pretty Flower"
    val inventoryOrder: Int = 5
    val foodValueLong: Long = 0L
  }

  // Raw/Frozen Food Items

  case object FrozenMomo extends ItemType {
    val name: String = "Frozen Momo"
    val inventoryOrder: Int = 11
    val foodValueLong: Long = 0L
  }

  // Food Items

  case object Rice extends ItemType {
    val name: String = "Rice"
    val inventoryOrder: Int = 21
    val foodValueLong: Long = 5L
  }

  case object Berries extends ItemType {
    val name: String = "Berries"
    val inventoryOrder: Int = 22
    val foodValueLong: Long = 6L
  }

  case object Momo extends ItemType {
    val name: String = "Momo"
    val inventoryOrder: Int = 23
    val foodValueLong: Long = 10L
  }

  case object Curry extends ItemType {
    val name: String = "Curry"
    val inventoryOrder: Int = 24
    val foodValueLong: Long = 12L
  }

  case object Chatpate extends ItemType {
    val name: String = "Chatpate"
    val inventoryOrder: Int = 25
    val foodValueLong: Long = 15L
  }

  case object Panipuri extends ItemType {
    val name: String = "Panipuri"
    val inventoryOrder: Int = 26
    val foodValueLong: Long = 20L
  }

}

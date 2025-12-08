package pt.rcmartins.loop.model

import pt.rcmartins.loop.Constants

sealed trait ItemType {
  val name: String
  val iconPath: String = ""
  val iconColor: String = "text-stone-50"
  val visible: Boolean = true
  val noInventoyLimit: Boolean = false
  val inventoryOrder: Int
  val amountFormat: Int => String = n => s"$n"
  val amountFormatInv: Int => String = n => s"$n"

  def foodValueLong: Long
  lazy val isFoodItem: Boolean = foodValueLong > 0
  lazy val foodValueMicro: Long = foodValueLong * 1_000_000L
}

object ItemType {

  // Coins

  case object Coins extends ItemType {
    val name: String = "Coins"
    override val iconPath: String = Constants.Icons.TwoCoins
    override val iconColor: String = "text-yellow-300"
    override val noInventoyLimit: Boolean = true
    val inventoryOrder: Int = 1
    val foodValueLong: Long = 0L
    override val amountFormat: Int => String = n => f"$$${n / 100.0}%.2f"
    override val amountFormatInv: Int => String = n => amountFormat(n)
  }

  // Misc Items

  case object Rosemary extends ItemType {
    val name: String = "Rosemary"
    override val iconPath: String = Constants.Icons.Olive
    override val iconColor: String = "text-green-500"
    val inventoryOrder: Int = 2
    val foodValueLong: Long = 0L
  }

  case object Glycerin extends ItemType {
    val name: String = "Glycerin Based Soap"
    override val iconPath: String = Constants.Icons.StoneBlock
    val inventoryOrder: Int = 3
    val foodValueLong: Long = 0L
  }

  case object MeltedGlycerin extends ItemType {
    val name: String = "Melted Glycerin"
    override val iconPath: String = Constants.Icons.BubblingBowl
    override val iconColor: String = "text-neutral-300"
    val inventoryOrder: Int = 4
    val foodValueLong: Long = 0L
  }

  case object HerbSoap extends ItemType {
    val name: String = "Herb Soap"
    override val iconPath: String = Constants.Icons.Soap
    override val iconColor: String = "text-green-300"
    val inventoryOrder: Int = 5
    val foodValueLong: Long = 0L
  }

  case object PrettyFlower extends ItemType {
    val name: String = "Pretty Flower"
    val inventoryOrder: Int = 6
    val foodValueLong: Long = 0L
  }

  // Raw/Frozen Food Items

  case object FrozenMomo extends ItemType {
    val name: String = "Frozen Momo"
    override val iconPath: String = Constants.Icons.DumplingBao
    override val iconColor: String = "text-cyan-500"
    val inventoryOrder: Int = 21
    val foodValueLong: Long = 0L
  }

  // Food Items

  case object Rice extends ItemType {
    val name: String = "Rice"
    override val iconPath: String = Constants.Icons.BowlOfRice
    val inventoryOrder: Int = 31
    val foodValueLong: Long = 5L
  }

  case object Berries extends ItemType {
    val name: String = "Berries"
    val inventoryOrder: Int = 32
    val foodValueLong: Long = 6L
  }

  case object Momo extends ItemType {
    val name: String = "Momo"
    override val iconPath: String = Constants.Icons.DumplingBao
    override val iconColor: String = "text-yellow-100"
    val inventoryOrder: Int = 33
    val foodValueLong: Long = 10L
  }

  case object Curry extends ItemType {
    val name: String = "Curry"
    val inventoryOrder: Int = 34
    val foodValueLong: Long = 12L
  }

  case object Chatpate extends ItemType {
    val name: String = "Chatpate"
    val inventoryOrder: Int = 35
    val foodValueLong: Long = 15L
  }

  case object Panipuri extends ItemType {
    val name: String = "Panipuri"
    val inventoryOrder: Int = 36
    val foodValueLong: Long = 20L
  }

}

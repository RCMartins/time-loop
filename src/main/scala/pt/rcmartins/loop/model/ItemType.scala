package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

sealed trait ItemType {
  val visible: Boolean = true
  val noInventoyLimit: Boolean = false
  val name: String
  val inventoryOrder: Int
}

object ItemType {

  // Misc Items
  case object Backpack extends ItemType {
    val name: String = "Backpack"
    override val visible: Boolean = false
    val inventoryOrder: Int = 0
  }

  // Coins
  case object Coins extends ItemType {
    val name: String = "Coins"
    override val noInventoyLimit: Boolean = true
    val inventoryOrder: Int = 1
  }

  // Food Items
  case object Rice extends ItemType {
    val name: String = "Rice"
    val inventoryOrder: Int = 2
  } // 5 energy

  case object Momo extends ItemType {
    val name: String = "Momo"
    val inventoryOrder: Int = 3
  } // 8 energy

  case object Curry extends ItemType {
    val name: String = "Curry"
    val inventoryOrder: Int = 4
  } // 12 energy

  case object Chatpate extends ItemType {
    val name: String = "Chatpate"
    val inventoryOrder: Int = 5
  } // 15 energy

  case object Panipuri extends ItemType {
    val name: String = "Panipuri"
    val inventoryOrder: Int = 6
  } // 20 energy

}

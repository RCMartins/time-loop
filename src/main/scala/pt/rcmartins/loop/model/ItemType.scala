package pt.rcmartins.loop.model

import pt.rcmartins.loop.Constants
import zio.json.{JsonDecoder, JsonEncoder}

import scala.collection.mutable

trait ItemType {
  val id: Int
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

  def buffItem: Option[(Buff, Long)] = None
}

object ItemType {

  private val allMutable: mutable.Map[Int, ItemType] = mutable.Map.empty

  // Coins

  val Coins: ItemType = addNewItem(new ItemType {
    val id: Int = 1
    val name: String = "Coins"
    override val iconPath: String = Constants.Icons.TwoCoins
    override val iconColor: String = "text-yellow-300"
    override val noInventoyLimit: Boolean = true
    val inventoryOrder: Int = 1
    val foodValueLong: Long = 0L
    override val amountFormat: Int => String = n => f"$$${n / 100.0}%.2f"
    override val amountFormatInv: Int => String = n => amountFormat(n)
  })

  // Misc Items

  val Rosemary: ItemType = addNewItem(new ItemType {
    val id: Int = 2
    val name: String = "Rosemary"
    override val iconPath: String = Constants.Icons.Olive
    override val iconColor: String = "text-green-500"
    val inventoryOrder: Int = 2
    val foodValueLong: Long = 0L
  })

  val Glycerin: ItemType = addNewItem(new ItemType {
    val id: Int = 3
    val name: String = "Glycerin Based Soap"
    override val iconPath: String = Constants.Icons.StoneBlock
    val inventoryOrder: Int = 3
    val foodValueLong: Long = 0L
  })

  val MeltedGlycerin: ItemType = addNewItem(new ItemType {
    val id: Int = 4
    val name: String = "Melted Glycerin"
    override val iconPath: String = Constants.Icons.BubblingBowl
    override val iconColor: String = "text-neutral-300"
    val inventoryOrder: Int = 4
    val foodValueLong: Long = 0L
  })

  val RosemarySoap: ItemType = addNewItem(new ItemType {
    val id: Int = 5
    val name: String = "Rosemary Soap"
    override val iconPath: String = Constants.Icons.Soap
    override val iconColor: String = "text-green-300"
    val inventoryOrder: Int = 5
    val foodValueLong: Long = 0L
  })

  val MagicLavender: ItemType = addNewItem(new ItemType {
    val id: Int = 6
    val name: String = "Magic Lavender"
    val inventoryOrder: Int = 6
    val foodValueLong: Long = 0L
  })

  val MagicLavenderSoap: ItemType = addNewItem(new ItemType {
    val id: Int = 14
    val name: String = "Magic Lavender Soap"
    override val iconPath: String = Constants.Icons.Soap
    override val iconColor: String = "text-purple-300"
    val inventoryOrder: Int = 7
    val foodValueLong: Long = 0L

    override val buffItem: Option[(Buff, Long)] = Some(
      (Buff.TirednessMultiplier(1L, 0.5), 5_000_000L)
    )
  })

  // Raw/Frozen Food Items

  val FrozenMomo: ItemType = addNewItem(new ItemType {
    val id: Int = 7
    val name: String = "Frozen Momo"
    override val iconPath: String = Constants.Icons.DumplingBao
    override val iconColor: String = "text-cyan-500"
    val inventoryOrder: Int = 21
    val foodValueLong: Long = 0L
  })

  // Food Items

  val Rice: ItemType = addNewItem(new ItemType {
    val id: Int = 8
    val name: String = "Rice"
    override val iconPath: String = Constants.Icons.BowlOfRice
    val inventoryOrder: Int = 31
    val foodValueLong: Long = 5L
  })

  val WildCherries: ItemType = addNewItem(new ItemType {
    val id: Int = 9
    val name: String = "Cherries"
    val inventoryOrder: Int = 32
    val foodValueLong: Long = 6L
    override val iconPath: String = Constants.Icons.Cherry
    override val iconColor: String = "text-red-600"
  })

  val Momo: ItemType = addNewItem(new ItemType {
    val id: Int = 10
    val name: String = "Momo"
    override val iconPath: String = Constants.Icons.DumplingBao
    override val iconColor: String = "text-yellow-100"
    val inventoryOrder: Int = 33
    val foodValueLong: Long = 10L
  })

  val Noodles: ItemType = addNewItem(new ItemType {
    val id: Int = 11
    override val iconPath: String = Constants.Icons.Noodles
    override val iconColor: String = "text-yellow-400"
    val name: String = "Noodles"
    val inventoryOrder: Int = 34
    val foodValueLong: Long = 12L
  })

  private def addNewItem(itemType: ItemType): ItemType = {
    if (allMutable.contains(itemType.id))
      throw new IllegalStateException(s"Duplicate ItemType id found: ${itemType.id}")
    allMutable.put(itemType.id, itemType)
    itemType
  }

  implicit val decoder: JsonDecoder[ItemType] =
    JsonDecoder.int.mapOrFail { id =>
      allMutable.get(id) match {
        case Some(itemType) => Right(itemType)
        case None           => Left(s"Unknown ItemType id: $id")
      }
    }

  implicit val encoder: JsonEncoder[ItemType] =
    JsonEncoder.int.contramap[ItemType](_.id)

}

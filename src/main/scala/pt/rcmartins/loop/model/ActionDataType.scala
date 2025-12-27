package pt.rcmartins.loop.model

import zio.json.{JsonDecoder, JsonEncoder}

import scala.collection.mutable

trait ActionDataType {
  val id: ActionId
}

object ActionDataType {

  private val allMutable: mutable.Map[ActionId, ActionDataType] = mutable.Map.empty

  object Arc1DataType {

    val WakeUp: ActionDataType = addNewDataType(1L)

    val SearchLivingRoom: ActionDataType = addNewDataType(2L)

    val PickupSimpleSoapMold: ActionDataType = addNewDataType(3L)

    val ExploreForestForLavender: ActionDataType = addNewDataType(4L)

    val FindMysteriousSorcerer: ActionDataType = addNewDataType(5L)

    val TalkMysteriousSorcerer: ActionDataType = addNewDataType(6L)

    val FirstLoopFadingAway: ActionDataType = addNewDataType(7L)

    val PickupCoins: ActionDataType = addNewDataType(8L)

    val SearchKitchen: ActionDataType = addNewDataType(9L)

    val CookRice: ActionDataType = addNewDataType(10L)

    val SearchGarden: ActionDataType = addNewDataType(11L)

    val PickRosemaryGarden: ActionDataType = addNewDataType(12L)

    val GoToGeneralStore: ActionDataType = addNewDataType(13L)

    val GoToBackHome: ActionDataType = addNewDataType(14L)

    val BuyGlycerin: ActionDataType = addNewDataType(15L)

    val BuyFrozenMomo: ActionDataType = addNewDataType(16L)

    val SpeakToShopKeeper: ActionDataType = addNewDataType(35L)

    val TradeSoapsForBag: ActionDataType = addNewDataType(36L)

    val GoToNeighborhood: ActionDataType = addNewDataType(37L)

    val SellSoapInNeighborhood: ActionDataType = addNewDataType(38L)

    val NeighborhoodTalkAboutMarket: ActionDataType = addNewDataType(43L)

    val UNUSED_2: ActionDataType = addNewDataType(17L)

    val CookMomo: ActionDataType = addNewDataType(18L)

    val MeltGlycerin: ActionDataType = addNewDataType(19L)

    val UNUSED: ActionDataType = addNewDataType(20L)

    val CreateRomesarySoap: ActionDataType = addNewDataType(21L)

    val GoToMarket: ActionDataType = addNewDataType(22L)

    val SetupSoapStallInMarket: ActionDataType = addNewDataType(23L)

    val SellSoapToPeopleInMarket: ActionDataType = addNewDataType(24L)

    val ExploreMarket: ActionDataType = addNewDataType(25L)

    val GoToEquipamentStore: ActionDataType = addNewDataType(26L)

    val BuyBigBag: ActionDataType = addNewDataType(27L)

    val BuyHugeBag: ActionDataType = addNewDataType(28L)

    val BuyEmptyShop: ActionDataType = addNewDataType(33L)

    val PrepareShopForBusiness: ActionDataType = addNewDataType(34L)

    val GoToMySoapShop: ActionDataType = addNewDataType(41L)

    val SellSoapToPeopleInSoapShop: ActionDataType = addNewDataType(39L)

    val GoToForest: ActionDataType = addNewDataType(29L)

    val ForestSearchAreaAroundSorcererPosition: ActionDataType = addNewDataType(42L)

    val PickupWildCherries: ActionDataType = addNewDataType(30L)

    val PickupLavender: ActionDataType = addNewDataType(40L)

    val MakeMagicLavenderSoap: ActionDataType = addNewDataType(31L)

    val FollowHardToFindFootprintsPath: ActionDataType = addNewDataType(32L)

  }

  object Arc2DataType {}

  private def addNewDataType(id: Long): ActionDataType = {
    val actionId = ActionId(id)
    if (allMutable.contains(actionId))
      throw new IllegalStateException(s"Duplicate ActionDataType id found: ${actionId}")
    val actionDataType = new ActionDataType { val id: ActionId = actionId }
    allMutable.put(actionId, actionDataType)
    actionDataType
  }

  implicit val decoder: JsonDecoder[ActionDataType] =
    JsonDecoder.long.mapOrFail { idLong =>
      val actionId = ActionId(idLong)
      allMutable.get(actionId) match {
        case Some(actionDataType) => Right(actionDataType)
        case None                 => Left(s"Unknown ActionDataType id: $idLong")
      }
    }

  implicit val encoder: JsonEncoder[ActionDataType] =
    JsonEncoder.long.contramap[ActionDataType](_.id.id)

}

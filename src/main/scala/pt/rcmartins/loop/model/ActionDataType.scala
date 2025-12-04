package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

sealed trait ActionDataType

object ActionDataType {

  sealed trait Arc1DataType extends ActionDataType

  object Arc1DataType {

    case object WakeUp extends Arc1DataType

    case object SearchLivingRoom extends Arc1DataType

    case object PickupSimpleSoapMold extends Arc1DataType

    case object ExploreForestForLavender extends Arc1DataType

    case object FindMysteriousSorcerer extends Arc1DataType

    case object TalkMysteriousSorcerer extends Arc1DataType

    case object FirstLoopFadingAway extends Arc1DataType

    case object PickupCoins extends Arc1DataType

    case object SearchKitchen extends Arc1DataType

    case object CookRice extends Arc1DataType

    case object SearchGarden extends Arc1DataType

    case object PickMintGarden extends Arc1DataType

    case object GoToGeneralStore extends Arc1DataType

    case object GoToBackHome extends Arc1DataType

    case object BuyGlycerin extends Arc1DataType

    case object BuyFrozenMomo extends Arc1DataType

    case object BuyGoodSoapMold extends Arc1DataType

    case object CookMomo extends Arc1DataType

    case object MeltGlycerin extends Arc1DataType

    case object MoldSoap extends Arc1DataType

    case object CreateSoap extends Arc1DataType

    case object GoToTown extends Arc1DataType

    case object TalkWithPeopleInTown extends Arc1DataType

    case object SellSoapToPeople extends Arc1DataType

    case object ExploreTown extends Arc1DataType

    case object GoToEquipamentStore extends Arc1DataType

    case object BuyBigBag extends Arc1DataType

    case object BuyHugeBag extends Arc1DataType

    case object GoToForest extends Arc1DataType

    case object PickupBerries extends Arc1DataType

    case object PickupPrettyFlower extends Arc1DataType

    case object SellFlowerInStore extends Arc1DataType

    case object BuyEmptyStore extends Arc1DataType

    case object PrepareShopForBusiness extends Arc1DataType

  }

  object Arc2DataType {

    case object GoToMySoapShop extends Arc1DataType

  }

  implicit val decoder: JsonDecoder[ActionDataType] = DeriveJsonDecoder.gen[ActionDataType]
  implicit val encoder: JsonEncoder[ActionDataType] = DeriveJsonEncoder.gen[ActionDataType]

}

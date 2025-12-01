package pt.rcmartins.loop.model

sealed trait ActionDataType

object ActionDataType {

  sealed trait Level1DataType extends ActionDataType

  object Level1DataType {

    case object WakeUp extends Level1DataType

    case object SearchLivingRoom extends Level1DataType

    case object PickupSimpleSoapMold extends Level1DataType

    case object ExploreForestForLavender extends Level1DataType

    case object FindMysteriousSorcerer extends Level1DataType

    case object TalkMysteriousSorcerer extends Level1DataType

    case object FirstLoopFadingAway extends Level1DataType

    case object PickupCoins extends Level1DataType

    case object SearchKitchen extends Level1DataType

    case object CookRice extends Level1DataType

    case object SearchGarden extends Level1DataType

    case object PickMintGarden extends Level1DataType

    case object GoToGeneralStore extends Level1DataType

    case object GoToBackToHouse extends Level1DataType

    case object BuyGlycerin extends Level1DataType

    case object BuyRawMomo extends Level1DataType

    case object BuyGoodSoapMold extends Level1DataType

    case object CookMomo extends Level1DataType

    case object MeltGlycerin extends Level1DataType

    case object MoldSoap extends Level1DataType

    case object CreateSoap extends Level1DataType

    case object GoToTown extends Level1DataType

    case object TalkWithPeopleInTown extends Level1DataType

    case object SellSoapToPeople extends Level1DataType

    case object ExploreTown extends Level1DataType

    case object GoToEquipamentStore extends Level1DataType

    case object BuyBigBag extends Level1DataType

    case object BuyHugeBag extends Level1DataType

    case object GoToForest extends Level1DataType

    case object PickupBerries extends Level1DataType

    case object PickupPrettyFlower extends Level1DataType

    case object SellFlowerInStore extends Level1DataType

    case object BuyEmptyStore extends Level1DataType

    case object PrepareStoreForBusiness extends Level1DataType

  }

}

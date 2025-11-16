package pt.rcmartins.loop.model

sealed trait ActionDataType

object ActionDataType {

//  sealed trait Area1DataType extends ActionDataType
//
//  object Area1DataType {
//
//    case object WakeUp extends Area1DataType
//
//    case object ExploreHouse extends Area1DataType
//
//    case object GoToLivingRoom extends Area1DataType
//
//    case object ExploreLivingRoom extends Area1DataType
//
//    case object PickupBackpack extends Area1DataType
//
//    case object PickupCoins extends Area1DataType
//
//    case object GoToKitchen extends Area1DataType
//
//    case object ExploreKitchen extends Area1DataType
//
//    case object PickupMomo extends Area1DataType
//
//    case object GoToBackyard extends Area1DataType
//
//  }
//
//  sealed trait Area2DataType extends ActionDataType
//
//  object Area2DataType {
//
//    case object ExploreTown extends Area2DataType
//
//  }

  sealed trait Level1DataType extends ActionDataType

  object Level1DataType {

    case object WakeUp extends Level1DataType

    case object SearchLivingRoom extends Level1DataType

    case object PickupBackpack extends Level1DataType

    case object PickupCoins extends Level1DataType

    case object SearchKitchen extends Level1DataType

    case object CookRice extends Level1DataType

    case object GoToGarden extends Level1DataType

    case object PickHerbsGarden extends Level1DataType

    case object GoToGeneralStore extends Level1DataType

    case object GoToBackToHouse extends Level1DataType

    case object BuyGlycerin extends Level1DataType

    case object BuyRawMomo extends Level1DataType

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

    case object BuyEmptyStore extends Level1DataType

    case object PrepareStoreForBusiness extends Level1DataType

    case object FinishLevel1 extends Level1DataType

  }

}

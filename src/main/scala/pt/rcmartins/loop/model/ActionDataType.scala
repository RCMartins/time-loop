package pt.rcmartins.loop.model

sealed trait ActionDataType

object ActionDataType {

  sealed trait Area1DataType extends ActionDataType

  object Area1DataType {

    case object WakeUp extends Area1DataType

    case object ExploreHouse extends Area1DataType

    case object GoToLivingRoom extends Area1DataType

    case object ExploreLivingRoom extends Area1DataType

    case object PickupBackpack extends Area1DataType

    case object PickupCoins extends Area1DataType

    case object GoToKitchen extends Area1DataType

    case object ExploreKitchen extends Area1DataType

    case object PickupMomo extends Area1DataType

    case object GoToBackyard extends Area1DataType

  }

  sealed trait Area2DataType extends ActionDataType

  object Area2DataType {

    case object ExploreTown extends Area2DataType

  }

}

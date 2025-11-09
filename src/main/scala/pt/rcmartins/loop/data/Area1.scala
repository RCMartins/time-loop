package pt.rcmartins.loop.data

import com.raquo.airstream.state.Val
import pt.rcmartins.loop.model.{ActionData, ActionDataType, ActionKind}
import pt.rcmartins.loop.model.ActionDataType.Area1DataType

object Area1 {

  object Types {

    case object WakeUp extends Area1DataType
    case object ExploreHouse extends Area1DataType

    case object GoToLivingRoom extends Area1DataType
    case object SearchLivingRoom extends Area1DataType
    case object PickupBackpack extends Area1DataType
    case object PickupCoins extends Area1DataType

    case object GoToKitchen extends Area1DataType
    case object SearchKichen extends Area1DataType
    case object PickupMomo extends Area1DataType

    case object GoToBackyard extends Area1DataType

  }

  object Data {

    def WakeUp: ActionData = ActionData(
      actionDataType = Types.WakeUp,
      title = "Wake Up from bed",
      subtitle = "",
      kind = ActionKind.Agility,
      baseTimeSec = 7,
      unlocksActions = Seq(ExploreHouse),
//      enable = Val(true),
    )

    def ExploreHouse: ActionData = ActionData(
      actionDataType = Types.ExploreHouse,
      title = "Explore the House",
      subtitle = "",
      kind = ActionKind.Agility,
      baseTimeSec = 12,
      unlocksActions = Seq(GoToLivingRoom, GoToKitchen, GoToBackyard),
    )

    def GoToLivingRoom: ActionData = ActionData(
      actionDataType = Types.GoToLivingRoom,
      title = "Go to the Living Room",
      subtitle = "",
      kind = ActionKind.Agility,
      baseTimeSec = 5,
      unlocksActions = Seq(SearchLivingRoom),
    )

    def SearchLivingRoom: ActionData = ActionData(
      actionDataType = Types.SearchLivingRoom,
      title = "Search the Living Room",
      subtitle = "",
      kind = ActionKind.Exploring,
      baseTimeSec = 10,
      unlocksActions = Seq(PickupBackpack, PickupCoins),
    )

    def PickupBackpack: ActionData = ActionData(
      actionDataType = Types.PickupBackpack,
      title = "Pick up the Backpack",
      subtitle = "",
      kind = ActionKind.Foraging,
      baseTimeSec = 3,
    )

    def PickupCoins: ActionData = ActionData(
      actionDataType = Types.PickupCoins,
      title = "Pick up 5 the Coins",
      subtitle = "",
      kind = ActionKind.Foraging,
      baseTimeSec = 2,
    )

    def GoToKitchen: ActionData = ActionData(
      actionDataType = Types.GoToKitchen,
      title = "Go to the Kitchen",
      subtitle = "",
      kind = ActionKind.Agility,
      baseTimeSec = 5,
      unlocksActions = Seq(SearchKichen),
    )

    def SearchKichen: ActionData = ActionData(
      actionDataType = Types.SearchKichen,
      title = "Search the Kitchen",
      subtitle = "",
      kind = ActionKind.Exploring,
      baseTimeSec = 10,
      unlocksActions = Seq(PickupMomo),
    )

    def PickupMomo: ActionData = ActionData(
      actionDataType = Types.PickupMomo,
      title = "Pick up Momo",
      subtitle = "",
      kind = ActionKind.Foraging,
      baseTimeSec = 5,
    )

    def GoToBackyard: ActionData = ActionData(
      actionDataType = Types.GoToBackyard,
      title = "Go to the Backyard",
      subtitle = "",
      kind = ActionKind.Agility,
      baseTimeSec = 25,
    )

    def load(dataType: ActionDataType): ActionData =
      dataType match {
        case Types.WakeUp => WakeUp
      }

  }

}

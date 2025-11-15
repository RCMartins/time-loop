//package pt.rcmartins.loop.data
//
//import pt.rcmartins.loop.model.ActionDataType.Area1DataType
//import pt.rcmartins.loop.model._
//
//object Area1 {
//
//  object Data {
//
//    val InitialActionData: Seq[ActionData] = Seq(
//      WakeUp,
////      PickupCoins,
////      GoToBackyard,
//    )
//
//    def WakeUp: ActionData = ActionData(
//      actionDataType = Area1DataType.WakeUp,
//      title = "Wake Up from bed",
//      effectLabel = EffectLabel.Movement,
//      kind = ActionKind.Agility,
//      baseTimeSec = 7,
//      unlocksActions = Seq(ExploreHouse),
//    )
//
//    def ExploreHouse: ActionData = ActionData(
//      actionDataType = Area1DataType.ExploreHouse,
//      title = "Explore the House",
//      effectLabel = EffectLabel.Explore,
//      kind = ActionKind.Agility,
//      baseTimeSec = 12,
//      unlocksActions = Seq(GoToLivingRoom, GoToKitchen, GoToBackyard),
//    )
//
//    def GoToLivingRoom: ActionData = ActionData(
//      actionDataType = Area1DataType.GoToLivingRoom,
//      title = "Go to the Living Room",
//      effectLabel = EffectLabel.Movement,
//      kind = ActionKind.Agility,
//      baseTimeSec = 5,
//      unlocksActions = Seq(ExploreLivingRoom),
//    )
//
//    def ExploreLivingRoom: ActionData = ActionData(
//      actionDataType = Area1DataType.ExploreLivingRoom,
//      title = "Search the Living Room",
//      effectLabel = EffectLabel.Explore,
//      kind = ActionKind.Exploring,
//      baseTimeSec = 10,
//      unlocksActions = Seq(PickupBackpack, PickupCoins),
//    )
//
//    def PickupBackpack: ActionData = ActionData(
//      actionDataType = Area1DataType.PickupBackpack,
//      title = "Pick up the Backpack",
//      effectLabel = EffectLabel.GetItem(ItemType.Backpack, 1),
//      kind = ActionKind.Foraging,
//      baseTimeSec = 5,
//      changeInventory = _.increaseInventorySize(+4)
//    )
//
//    def PickupCoins: ActionData = ActionData(
//      actionDataType = Area1DataType.PickupCoins,
//      title = "Pick up Coins",
//      effectLabel = EffectLabel.GetItem(ItemType.Coins, 5),
//      kind = ActionKind.Foraging,
//      baseTimeSec = 2,
//      changeInventory = _.addItem(ItemType.Coins, 5),
//      invalidReason = state =>
//        Option.unless(state.inventory.canAddItem(ItemType.Coins, 5))(ReasonLabel.InventoryFull)
//    )
//
//    def GoToKitchen: ActionData = ActionData(
//      actionDataType = Area1DataType.GoToKitchen,
//      title = "Go to the Kitchen",
//      effectLabel = EffectLabel.Movement,
//      kind = ActionKind.Agility,
//      baseTimeSec = 5,
//      unlocksActions = Seq(ExploreKitchen),
//    )
//
//    def ExploreKitchen: ActionData = ActionData(
//      actionDataType = Area1DataType.ExploreKitchen,
//      title = "Search the Kitchen",
//      effectLabel = EffectLabel.Explore,
//      kind = ActionKind.Exploring,
//      baseTimeSec = 10,
//      unlocksActions = Seq(PickupMomo),
//    )
//
//    def PickupMomo: ActionData = ActionData(
//      actionDataType = Area1DataType.PickupMomo,
//      title = "Pick up Momo",
//      effectLabel = EffectLabel.GetItem(ItemType.Momo, 1),
//      kind = ActionKind.Foraging,
//      baseTimeSec = 5,
//      initialAmountOfActions = 3,
//      changeInventory = _.addItem(ItemType.Momo, 1),
//      invalidReason = state =>
//        Option.unless(state.inventory.canAddItem(ItemType.Momo, 1))(ReasonLabel.InventoryFull),
//    )
//
//    def GoToBackyard: ActionData = ActionData(
//      actionDataType = Area1DataType.GoToBackyard,
//      title = "Go to the Backyard",
//      effectLabel = EffectLabel.Movement,
//      kind = ActionKind.Agility,
//      baseTimeSec = 25,
//      invalidReason = state =>
//        Option.unless(
//          state.actionsHistory.exists(_.actionDataType == Area1DataType.ExploreKitchen) &&
//            state.actionsHistory.exists(_.actionDataType == Area1DataType.ExploreLivingRoom)
//        )(ReasonLabel.MustExploreHouseFirst),
//      showWhenInvalid = false,
//      unlocksActions = Seq(Area2.Data.ExploreTown),
//    )
//
//  }
//
//}

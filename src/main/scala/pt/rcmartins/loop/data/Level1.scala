package pt.rcmartins.loop.data

import pt.rcmartins.loop.data.LevelUtils._
import pt.rcmartins.loop.model.ActionDataType.Level1DataType
import pt.rcmartins.loop.model.CharacterArea._
import pt.rcmartins.loop.model._

object Level1 {

  object Data {

    val InitialCharacterArea: CharacterArea = Area1_House
    val InitialActionData: Seq[ActionData] = Seq(
      WakeUp,
    )

    def WakeUp: ActionData = ActionData(
      actionDataType = Level1DataType.WakeUp,
      area = Seq(Area1_House),
      title = "Wake Up from bed",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.Standard(7),
      firstTimeUnlocksActions = _ => Seq(SearchLivingRoom, SearchKitchen, SearchGarden),
      moveToArea = Some(Area1_House),
    )

    def SearchLivingRoom: ActionData = ActionData(
      actionDataType = Level1DataType.SearchLivingRoom,
      area = Seq(Area1_House),
      title = "Search Living Room",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(5),
      firstTimeUnlocksActions = _ => Seq(PickupSimpleSoapMold, PickupCoins),
    )

    def PickupSimpleSoapMold: ActionData = pickupToItem(
      actionDataType = Level1DataType.PickupSimpleSoapMold,
      area = Seq(Area1_House),
      itemType = ItemType.SimpleSoapMold,
      amount = 1,
      actionTime = ActionTime.Standard(1),
      initialAmountOfActions = AmountOfActions.Standard(10),
    )

    def PickupCoins: ActionData = pickupToItem(
      actionDataType = Level1DataType.PickupCoins,
      area = Seq(Area1_House),
      itemType = ItemType.Coins,
      amount = 1,
      actionTime = ActionTime.Standard(1),
      initialAmountOfActions = AmountOfActions.Standard(10),
    )

    def SearchKitchen: ActionData = ActionData(
      actionDataType = Level1DataType.SearchKitchen,
      area = Seq(Area1_House),
      title = "Search the Kitchen",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(7),
      firstTimeUnlocksActions = _ => Seq(CookRice),
    )

    def CookRice: ActionData = cookingAction(
      actionDataType = Level1DataType.CookRice,
      area = Seq(Area1_House),
      itemType = ItemType.Rice,
      amount = 1,
      cost = Seq(),
      actionTime = ActionTime.Standard(4),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      showWhenInvalid = false,
    )

    def SearchGarden: ActionData = ActionData(
      actionDataType = Level1DataType.SearchGarden,
      area = Seq(Area1_House),
      title = "Explore the Garden",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(20),
      firstTimeUnlocksActions = _ => Seq(PickupHerbs, GoToGeneralStore),
      invalidReason = state =>
        Option.unless(
          state.actionsHistory.exists(_.actionDataType == Level1DataType.SearchKitchen) &&
            state.actionsHistory.exists(_.actionDataType == Level1DataType.SearchLivingRoom) &&
            state.actionsHistory.exists(_.actionDataType == Level1DataType.PickupSimpleSoapMold)
        )(ReasonLabel.Empty),
      showWhenInvalid = false,
    )

    def PickupHerbs: ActionData = gardeningAction(
      actionDataType = Level1DataType.PickHerbsGarden,
      area = Seq(Area1_House),
      itemType = ItemType.GardenHerb,
      amount = 1,
      actionTime = ActionTime.Standard(8),
      initialAmountOfActions = AmountOfActions.Unlimited,
    )

    def GoToGeneralStore: ActionData = ActionData(
      actionDataType = Level1DataType.GoToGeneralStore,
      area = Seq(Area1_House, Area2_Town, Area4_EquipmentStore),
      title = "Go to the General Store",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(20, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ =>
        Seq(
          BuyGlycerin,
          BuyRawMomo,
          GoToBackToHouse
        ),
      moveToArea = Some(Area3_Store),
    )

    def BuyGlycerin: ActionData = buyItemAction(
      actionDataType = Level1DataType.GoToGeneralStore,
      area = Seq(Area3_Store),
      itemType = ItemType.Glycerin,
      amount = 1,
      cost = 5,
      actionTime = ActionTime.Standard(20),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(MeltGlycerin),
    )

    def BuyRawMomo: ActionData = buyItemAction(
      actionDataType = Level1DataType.BuyRawMomo,
      area = Seq(Area3_Store),
      itemType = ItemType.RawMomo,
      amount = 1,
      cost = 1,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(CookMomo),
    )

    // TODO how should this work?
    def BuyGoodSoapMold: ActionData = buyItemAction(
      actionDataType = Level1DataType.BuyRawMomo,
      area = Seq(Area3_Store),
      itemType = ItemType.GoodSoapMold,
      amount = 5,
      cost = 1,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(),
      changeInventoryExtra = _.removeItem(ItemType.SimpleSoapMold, 1),
    )

    def CookMomo: ActionData = cookingAction(
      actionDataType = Level1DataType.CookMomo,
      area = Seq(Area1_House),
      itemType = ItemType.Momo,
      amount = 1,
      cost = Seq(ItemType.RawMomo -> 1),
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      showWhenInvalid = false,
    )

    def GoToBackToHouse: ActionData = ActionData(
      actionDataType = Level1DataType.GoToBackToHouse,
      area = Seq(Area2_Town, Area3_Store, Area4_EquipmentStore),
      title = "Go back to the house",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(20, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      moveToArea = Some(Area1_House),
    )

    def MeltGlycerin: ActionData = craftItem(
      actionDataType = Level1DataType.MeltGlycerin,
      area = Seq(Area1_House),
      itemType = ItemType.MeltedGlycerin,
      amount = 5,
      cost = Seq(ItemType.Glycerin -> 1),
      actionTime = ActionTime.Standard(15),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(MoldSoap),
      showWhenInvalid = false,
    )

    def MoldSoap: ActionData = craftItem(
      actionDataType = Level1DataType.MoldSoap,
      area = Seq(Area1_House),
      itemType = ItemType.HotMoldedSoap,
      amount = 1,
      cost = Seq(ItemType.MeltedGlycerin -> 1, ItemType.GardenHerb -> 1),
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(CreateSoap),
    )

    def CreateSoap: ActionData = craftItem(
      actionDataType = Level1DataType.CreateSoap,
      area = Seq(Area1_House),
      itemType = ItemType.HerbSoap,
      amount = 1,
      actionSuccessType = ActionSuccessType.WithFailure(0.5, 0.05),
      cost = Seq(ItemType.HotMoldedSoap -> 1),
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(GoToTown),
      showWhenInvalid = false,
    )

    def GoToTown: ActionData = ActionData(
      actionDataType = Level1DataType.GoToTown,
      area = Seq(Area1_House, Area3_Store, Area4_EquipmentStore, Area5_Forest),
      title = "Go to town",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(30, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(TalkWithPeopleInTown),
      moveToArea = Some(Area2_Town),
    )

    def TalkWithPeopleInTown: ActionData = ActionData(
      actionDataType = Level1DataType.TalkWithPeopleInTown,
      area = Seq(Area2_Town),
      title = "Talk with people in town",
      effectLabel = EffectLabel.TalkAboutSoap,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(20),
      firstTimeUnlocksActions = _ => Seq(SellSoapToPeople),
      moveToArea = Some(Area2_Town),
    )

    def SellSoapToPeople: ActionData = ActionData(
      actionDataType = Level1DataType.TalkWithPeopleInTown,
      area = Seq(Area2_Town),
      title = "Try to sell Soap to people",
      effectLabel = EffectLabel.SellSoap,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(25),
      initialAmountOfActions = AmountOfActions.Unlimited,
      actionSuccessType = ActionSuccessType.WithFailure(0.5, 0.05),
      changeInventory = _.addItem(ItemType.Coins, 2).removeItem(ItemType.HerbSoap, 1),
      invalidReason = state =>
        Option.unless(state.inventory.canRemoveItem(ItemType.HerbSoap, 1))(
          ReasonLabel.NotEnoughSoapToSell
        ),
      firstTimeUnlocksActions = _ => Seq(ExploreTown),
    )

    def ExploreTown: ActionData = ActionData(
      actionDataType = Level1DataType.ExploreTown,
      area = Seq(Area2_Town),
      title = "Explore Town",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(150),
      initialAmountOfActions = AmountOfActions.Standard(3),
      everyTimeUnlocksActions = {
        case 1 => Seq(GoToEquipmentStore)
        case 2 => Seq(GoToForest)
        case 3 => Seq(BuyEmptyStore)
        case _ => Seq()
      }
    )

    def GoToEquipmentStore: ActionData = ActionData(
      actionDataType = Level1DataType.GoToEquipamentStore,
      area = Seq(Area2_Town),
      title = "Go to the Equipment Store",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(50, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ =>
        Seq(
          BuyBigBag,
        ),
      moveToArea = Some(Area4_EquipmentStore),
    )

    def BuyBigBag: ActionData = buyInventoryIncrease(
      actionDataType = Level1DataType.BuyBigBag,
      area = Seq(Area4_EquipmentStore),
      name = "Big Bag",
      cost = 10,
      inventoryMaxSize = 10,
      actionTime = ActionTime.Standard(20),
      firstTimeUnlocksActions = _ => Seq(BuyHugeBag),
    )

    def BuyHugeBag: ActionData = buyInventoryIncrease(
      actionDataType = Level1DataType.BuyHugeBag,
      area = Seq(Area4_EquipmentStore),
      name = "Huge Bag",
      cost = 20,
      inventoryMaxSize = 15,
      actionTime = ActionTime.Standard(20),
      firstTimeUnlocksActions = _ => Seq(),
    )

    def GoToForest: ActionData = ActionData(
      actionDataType = Level1DataType.GoToForest,
      area = Seq(Area2_Town),
      title = "Go to the Forest",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(50, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(PickupBerries, PickupPrettyFlower),
      moveToArea = Some(Area5_Forest),
    )

    def PickupBerries: ActionData = pickupToItem(
      actionDataType = Level1DataType.PickupBerries,
      area = Seq(Area1_House),
      itemType = ItemType.Berries,
      amount = 1,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Unlimited,
    )

    def PickupPrettyFlower: ActionData = pickupToItem(
      actionDataType = Level1DataType.PickupPrettyFlower,
      area = Seq(Area1_House),
      itemType = ItemType.PrettyFlower,
      amount = 1,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(SellFlowerInGeneralStore),
    )

    def SellFlowerInGeneralStore: ActionData = sellItemAction(
      actionDataType = Level1DataType.SellFlowerInStore,
      area = Seq(Area3_Store),
      itemType = ItemType.PrettyFlower,
      amount = 1,
      coinsGain = 1,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Standard(10),
      firstTimeUnlocksActions = _ => Seq(),
    )

    def BuyEmptyStore: ActionData =
      ActionData(
        actionDataType = Level1DataType.BuyEmptyStore,
        area = Seq(Area2_Town),
        title = s"Buy Empty Store",
        effectLabel = EffectLabel.BuyEmptyStore(50),
        kind = ActionKind.Social,
        actionTime = ActionTime.Standard(60),
        invalidReason = state =>
          Option.unless(state.inventory.canRemoveItem(ItemType.Coins, 25))(
            ReasonLabel.NotEnoughCoins
          ),
        firstTimeUnlocksActions = _ => Seq(PrepareStoreForBusiness),
      )

    def PrepareStoreForBusiness: ActionData =
      ActionData(
        actionDataType = Level1DataType.PrepareStoreForBusiness,
        area = Seq(Area2_Town),
        title = s"Prepare Store for Soap Business",
        effectLabel = EffectLabel.Empty,
        kind = ActionKind.Crafting,
        actionTime = ActionTime.Standard(60),
        firstTimeUnlocksActions = _ => Seq(FinishLevel1),
      )

    def FinishLevel1: ActionData =
      ActionData(
        actionDataType = Level1DataType.FinishLevel1,
        area = Seq(Area2_Town),
        title = s"Win Level 1",
        effectLabel = EffectLabel.Empty,
        kind = ActionKind.Agility,
        actionTime = ActionTime.Standard(1),
        firstTimeUnlocksActions = _ => Seq(),
      )

  }

}

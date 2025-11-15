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
      area = None,
      title = "Wake Up from bed",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.Standard(7),
      firstTimeUnlocksActions = _ => Seq(SearchLivingRoom, SearchKitchen, SearchGarden),
      moveToArea = Some(Area1_House),
    )

    def SearchLivingRoom: ActionData = ActionData(
      actionDataType = Level1DataType.SearchLivingRoom,
      area = Some(Area1_House),
      title = "Search Living Room",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(5),
      firstTimeUnlocksActions = _ => Seq(PickupBackpack, PickupCoins),
    )

    def PickupBackpack: ActionData = ActionData(
      actionDataType = Level1DataType.PickupBackpack,
      area = Some(Area1_House),
      title = "Pick up the Backpack",
      effectLabel = EffectLabel.GetItem(ItemType.Backpack, 1),
      kind = ActionKind.Foraging,
      actionTime = ActionTime.Standard(5),
      changeInventory = _.increaseInventorySize(+4)
    )

    def PickupCoins: ActionData = pickupToItem(
      actionDataType = Level1DataType.PickupCoins,
      area = Some(Area1_House),
      itemType = ItemType.Coins,
      amount = 1,
      actionTime = ActionTime.Standard(1),
      initialAmountOfActions = AmountOfActions.Standard(5),
    )

    def SearchKitchen: ActionData = ActionData(
      actionDataType = Level1DataType.SearchKitchen,
      area = Some(Area1_House),
      title = "Search the Kitchen",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(7),
      firstTimeUnlocksActions = _ => Seq(PickupMomo),
    )

    def PickupMomo: ActionData = pickupToItem(
      actionDataType = Level1DataType.PickupMomo,
      area = Some(Area1_House),
      itemType = ItemType.Momo,
      amount = 1,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Standard(10),
    )

    def SearchGarden: ActionData = ActionData(
      actionDataType = Level1DataType.GoToGarden,
      area = Some(Area1_House),
      title = "Go to the Garden",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.Standard(20),
      firstTimeUnlocksActions = _ => Seq(PickupHerbs, GoToGeneralStore),
      invalidReason = state =>
        Option.unless(
          state.actionsHistory.exists(_.actionDataType == Level1DataType.SearchKitchen) &&
            state.actionsHistory.exists(_.actionDataType == Level1DataType.SearchLivingRoom) &&
            state.actionsHistory.exists(_.actionDataType == Level1DataType.PickupBackpack)
        )(ReasonLabel.Empty),
      showWhenInvalid = false,
    )

    def PickupHerbs: ActionData = pickupToItem(
      actionDataType = Level1DataType.PickHerbsGarden,
      area = Some(Area1_House),
      itemType = ItemType.SoapHerb,
      amount = 1,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Standard(25),
    )

    def GoToGeneralStore: ActionData = ActionData(
      actionDataType = Level1DataType.GoToGeneralStore,
      area = Some(Area1_House),
      title = "Go to the General Store",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(20, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ =>
        Seq(
          BuyGlycerin,
          BuyRawMomo,
          GoToBackToHouse_FromStore
        ),
      moveToArea = Some(Area3_Store),
    )

    def BuyGlycerin: ActionData = buyItemAction(
      actionDataType = Level1DataType.GoToGeneralStore,
      area = Some(Area3_Store),
      itemType = ItemType.Glycerin,
      amount = 1,
      cost = 5,
      actionTime = ActionTime.Standard(20),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(MeltGlycerin),
    )

    def BuyRawMomo: ActionData = buyItemAction(
      actionDataType = Level1DataType.BuyRawMomo,
      area = Some(Area3_Store),
      itemType = ItemType.RawMomo,
      amount = 1,
      cost = 1,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(CookMomo),
    )

    def CookMomo: ActionData = ActionData(
      actionDataType = Level1DataType.CookMomo,
      area = Some(Area1_House),
      title = "Cook Momo",
      effectLabel = EffectLabel.Cooking(ItemType.Momo, 1),
      kind = ActionKind.Cooking,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
    )

    def GoToBackToHouse_FromStore: ActionData = ActionData(
      actionDataType = Level1DataType.GoToBackToHouse,
      area = Some(Area3_Store),
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
      area = Some(Area1_House),
      itemType = ItemType.MeltedGlycerin,
      amount = 5,
      cost = Seq(ItemType.Glycerin -> 1),
      actionTime = ActionTime.Standard(15),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(MoldSoap),
    )

    def MoldSoap: ActionData = craftItem(
      actionDataType = Level1DataType.MoldSoap,
      area = Some(Area1_House),
      itemType = ItemType.HotMoldedSoap,
      amount = 1,
      cost = Seq(ItemType.MeltedGlycerin -> 1, ItemType.SoapHerb -> 1),
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(MoldSoap),
    )

    def CreateSoap: ActionData = craftItem(
      actionDataType = Level1DataType.CreateSoap,
      area = Some(Area1_House),
      itemType = ItemType.HerbSoap,
      amount = 1,
      actionSuccessType = ActionSuccessType.WithFailure(0.5, 0.05),
      cost = Seq(ItemType.HotMoldedSoap -> 1),
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(GoToTown),
    )

    def GoToTown: ActionData = ActionData(
      actionDataType = Level1DataType.GoToTown,
      area = Some(Area1_House),
      title = "Go to town",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(30, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(TalkWithPeopleInTown, GoToBackToHouse_FromTown),
      moveToArea = Some(Area2_Town),
    )

    def TalkWithPeopleInTown: ActionData = ActionData(
      actionDataType = Level1DataType.TalkWithPeopleInTown,
      area = Some(Area2_Town),
      title = "Talk with people in town",
      effectLabel = EffectLabel.TalkAboutSoap,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(20),
      firstTimeUnlocksActions = _ => Seq(SellSoapToPeople),
      moveToArea = Some(Area2_Town),
    )

    def SellSoapToPeople: ActionData = ActionData(
      actionDataType = Level1DataType.TalkWithPeopleInTown,
      area = Some(Area2_Town),
      title = "Try to sell Soap to people",
      effectLabel = EffectLabel.SellSoap,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(15),
      actionSuccessType = ActionSuccessType.WithFailure(0.5, 0.1),
      changeInventory = _.addItem(ItemType.Coins, 2).removeItem(ItemType.HerbSoap, 1),
      invalidReason = state =>
        Option.unless(state.inventory.canRemoveItem(ItemType.HerbSoap, 1))(
          ReasonLabel.NotEnoughSoapToSell
        ),
    )

    def GoToBackToHouse_FromTown: ActionData = ActionData(
      actionDataType = Level1DataType.GoToBackToHouse,
      area = Some(Area2_Town),
      title = "Go back to the house",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(30, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      moveToArea = Some(Area1_House),
    )

    def ExploreTown: ActionData = ActionData(
      actionDataType = Level1DataType.ExploreTown,
      area = Some(Area2_Town),
      title = "Explore Town",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(300),
      firstTimeUnlocksActions = _ => Seq(BuyEmptyStore),
    )

    def BuyEmptyStore: ActionData =
      ActionData(
        actionDataType = Level1DataType.BuyEmptyStore,
        area = Some(Area2_Town),
        title = s"Buy Empty Store",
        effectLabel = EffectLabel.BuyEmptyStore(50),
        kind = ActionKind.Social,
        actionTime = ActionTime.Standard(60),
        invalidReason = state =>
          Option.unless(state.inventory.canRemoveItem(ItemType.Coins, 50))(
            ReasonLabel.NotEnoughCoins
          ),
        firstTimeUnlocksActions = _ => Seq(PrepareStoreForBusiness),
      )

    def PrepareStoreForBusiness: ActionData =
      ActionData(
        actionDataType = Level1DataType.PrepareStoreForBusiness,
        area = Some(Area2_Town),
        title = s"Prepare Store for Soap Business",
        effectLabel = EffectLabel.Empty,
        kind = ActionKind.Crafting,
        actionTime = ActionTime.Standard(60),
        firstTimeUnlocksActions = _ => Seq(FinishLevel1),
      )

    def FinishLevel1: ActionData =
      ActionData(
        actionDataType = Level1DataType.FinishLevel1,
        area = Some(Area2_Town),
        title = s"Win Level 1",
        effectLabel = EffectLabel.Empty,
        kind = ActionKind.Agility,
        actionTime = ActionTime.Standard(1),
        firstTimeUnlocksActions = _ => Seq(),
      )

  }

}

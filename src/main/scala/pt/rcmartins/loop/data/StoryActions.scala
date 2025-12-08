package pt.rcmartins.loop.data

import pt.rcmartins.loop.data.ActionsUtils._
import pt.rcmartins.loop.model.ActionDataType._
import pt.rcmartins.loop.model.CharacterArea._
import pt.rcmartins.loop.model.GameState._
import pt.rcmartins.loop.model._

object StoryActions {

  object Data {

    val InitialCharacterArea: CharacterArea = Area1_Home
    val InitialActions: Seq[ActionData] = Seq(WakeUp)
    val InitialMoveActions: Seq[ActionData] = Seq.empty

    def WakeUp: ActionData = ActionData(
      actionDataType = Arc1DataType.WakeUp,
      area = Seq(Area1_Home),
      title = "Wake Up from bed",
      effectLabel = EffectLabel.Empty,
      kind = ActionKind.Agility,
      actionTime = ActionTime.Standard(7),
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(SearchLivingRoom, SearchKitchen)
        case _            => Seq(SearchLivingRoom, SearchKitchen, SearchGarden)
      },
      addStory = {
        case LoopCount(1) => Some(Story.FirstLoop.FirstWakeup)
        case LoopCount(2) => Some(Story.OtherLoops.SecondWakeup)
        case LoopCount(3) => Some(Story.OtherLoops.ThirdOrMoreWakeup)
        case _            => None
      },
    )

    def SearchLivingRoom: ActionData = ActionData(
      actionDataType = Arc1DataType.SearchLivingRoom,
      area = Seq(Area1_Home),
      title = "Search Living Room",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(5),
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(PickupSimpleSoapMold)
        case _            => Seq(PickupSimpleSoapMold, PickupCoins)
      },
    )

    def PickupSimpleSoapMold: ActionData = ActionData(
      actionDataType = Arc1DataType.PickupSimpleSoapMold,
      area = Seq(Area1_Home),
      title = "Get Soap Mold",
      effectLabel = EffectLabel.GetSoapMold,
      kind = ActionKind.Foraging,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(PickupCoins)
        case _            => Seq.empty
      },
      addStory = {
        case LoopCount(1) => Some(Story.FirstLoop.NoGlycerinShouldbuyFromStore)
        case _            => None
      },
    )

    def PickupCoins: ActionData = pickupToItem(
      actionDataType = Arc1DataType.PickupCoins,
      area = Seq(Area1_Home),
      itemType = ItemType.Coins,
      amount = 100,
      actionTime = ActionTime.Standard(1),
      initialAmountOfActions = AmountOfActions.Standard(10),
      everyTimeUnlocksActions = {
        case (LoopCount(1), 10) => Seq(GoToGeneralStore)
        case _                  => Seq()
      },
    )

    def SearchKitchen: ActionData = ActionData(
      actionDataType = Arc1DataType.SearchKitchen,
      area = Seq(Area1_Home),
      title = "Search the Kitchen",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(7),
      firstTimeUnlocksActions = _ => Seq(CookRice),
    )

    def CookRice: ActionData = cookingAction(
      actionDataType = Arc1DataType.CookRice,
      area = Seq(Area1_Home),
      itemType = ItemType.Rice,
      amount = 1,
      cost = Seq(),
      actionTime = ActionTime.Standard(4),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
    )

    def SearchGarden: ActionData = ActionData(
      actionDataType = Arc1DataType.SearchGarden,
      area = Seq(Area1_Home),
      title = "Explore the Garden",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(17),
      firstTimeUnlocksActions = _ => Seq(PickupRosemary, GoToGeneralStore),
      invalidReason = state =>
        Option.unless(
          state.actionsHistory.exists(_.actionDataType == Arc1DataType.SearchKitchen) &&
            state.actionsHistory.exists(_.actionDataType == Arc1DataType.SearchLivingRoom) &&
            state.actionsHistory.exists(_.actionDataType == Arc1DataType.PickupSimpleSoapMold)
        )(ReasonLabel.Empty),
      showWhenInvalid = false,
    )

    def PickupRosemary: ActionData = gardeningAction(
      actionDataType = Arc1DataType.PickRosemaryGarden,
      area = Seq(Area1_Home),
      itemType = ItemType.Rosemary,
      amount = 1,
      actionTime = ActionTime.Standard(6),
      initialAmountOfActions = AmountOfActions.Unlimited,
    )

    def GoToGeneralStore: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToGeneralStore,
      area = Seq(Area1_Home, Area2_Town, Area4_EquipmentStore),
      title = "Go to the General Store",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(20, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(BuyGlycerinFirstLoop)
        case _            => Seq(BuyGlycerin, BuyFrozenMomo, GoToBackHome)
      },
      moveToArea = Some(Area3_Store),
    )

    def BuyGlycerinFirstLoop: ActionData = buyItemAction(
      actionDataType = Arc1DataType.BuyGlycerin,
      area = Seq(Area3_Store),
      itemType = ItemType.Glycerin,
      amount = 1,
      cost = 500,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(FirstLoopGoToForest),
      addStory = _ => Some(Story.FirstLoop.GoToForestGetLavender),
    )

    def BuyGlycerin: ActionData = buyItemAction(
      actionDataType = Arc1DataType.BuyGlycerin,
      area = Seq(Area3_Store),
      itemType = ItemType.Glycerin,
      amount = 1,
      cost = 500,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(MeltGlycerin)
    )

    def FirstLoopGoToForest: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToForest,
      area = Seq(Area3_Store),
      title = "Go to the Forest",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.Standard(25),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(ExploreForestForLavender),
      moveToArea = Some(Area5_Forest),
    )

    def ExploreForestForLavender: ActionData = ActionData(
      actionDataType = Arc1DataType.ExploreForestForLavender,
      area = Seq(Area5_Forest),
      title = "Explore the forest for lavender",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(25),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(FindMysteriousSorcerer),
      addStory = _ => Some(Story.FirstLoop.MysteriousSorcererInTheForest),
    )

    def FindMysteriousSorcerer: ActionData = ActionData(
      actionDataType = Arc1DataType.FindMysteriousSorcerer,
      area = Seq(Area5_Forest),
      title = "Aproach Sorcerer",
      effectLabel = EffectLabel.Empty,
      kind = ActionKind.Agility,
      actionTime = ActionTime.Standard(15),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(TalkMysteriousSorcerer),
      addStory = _ => Some(Story.FirstLoop.FirstLineSorcerer),
    )

    def TalkMysteriousSorcerer: ActionData = ActionData(
      actionDataType = Arc1DataType.TalkMysteriousSorcerer,
      area = Seq(Area5_Forest),
      title = """Ask what is he talking about?""",
      effectLabel = EffectLabel.Empty,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(),
      addStory = _ => Some(Story.FirstLoop.FinalLineSorcerer),
    )

    def FirstLoopFadingAway: ActionData = ActionData(
      actionDataType = Arc1DataType.FirstLoopFadingAway,
      area = Seq(Area5_Forest),
      title = """Fading away""",
      effectLabel = EffectLabel.Empty,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(30),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(),
      difficultyModifier = ActionDifficultyModifier(increaseTirednessAbsoluteMicro = 15_000_000),
      addStory = _ => Some(Story.FirstLoop.FadingAway),
    )

    def BuyFrozenMomo: ActionData = buyItemAction(
      actionDataType = Arc1DataType.BuyFrozenMomo,
      area = Seq(Area3_Store),
      itemType = ItemType.FrozenMomo,
      amount = 1,
      cost = 50,
      actionTime = ActionTime.Standard(3),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(CookMomo),
    )

//    // TODO how should this work?
//    def BuyGoodSoapMold: ActionData = buyItemAction(
//      actionDataType = Level1DataType.BuyRawMomo,
//      area = Seq(Area3_Store),
//      itemType = ItemType.GoodSoapMold,
//      amount = 5,
//      cost = 1,
//      actionTime = ActionTime.Standard(10),
//      initialAmountOfActions = AmountOfActions.Standard(1),
//      firstTimeUnlocksActions = _ => Seq(),
//      changeInventoryExtra = _.removeItem(ItemType.SimpleSoapMold, 1),
//    )

    def CookMomo: ActionData = cookingAction(
      actionDataType = Arc1DataType.CookMomo,
      area = Seq(Area1_Home),
      itemType = ItemType.Momo,
      amount = 1,
      cost = Seq(ItemType.FrozenMomo -> 1),
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      showWhenInvalid = false,
    )

    def GoToBackHome: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToBackHome,
      area = Seq(Area2_Town, Area3_Store, Area4_EquipmentStore),
      title = "Go back home",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(20, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      moveToArea = Some(Area1_Home),
    )

    def MeltGlycerin: ActionData = craftItem(
      actionDataType = Arc1DataType.MeltGlycerin,
      area = Seq(Area1_Home),
      itemType = ItemType.MeltedGlycerin,
      amount = 5,
      cost = Seq(ItemType.Glycerin -> 1),
      actionTime = ActionTime.Standard(15),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(CreateSoap),
      showWhenInvalid = false,
    )

    def CreateSoap: ActionData = craftItem(
      actionDataType = Arc1DataType.CreateSoap,
      area = Seq(Area1_Home),
      itemType = ItemType.HerbSoap,
      amount = 1,
      actionSuccessType = ActionSuccessType.WithFailure(0.5, 0.05),
      cost = Seq(ItemType.MeltedGlycerin -> 1, ItemType.Rosemary -> 1),
      actionTime = ActionTime.Standard(15),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(GoToTown),
      permanentBonusUnlocks =
        Seq(PermanentBonusUnlockType.ProgressiveActionCount(PermanentBonus.HalfTiredness, 1, 10)),
      showWhenInvalid = false,
    )

    def GoToTown: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToTown,
      area = Seq(Area1_Home, Area3_Store, Area4_EquipmentStore, Area5_Forest, Area6_My_Soap_Shop),
      title = "Go to town",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(25, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(TalkWithPeopleInTown),
      moveToArea = Some(Area2_Town),
    )

    def TalkWithPeopleInTown: ActionData = ActionData(
      actionDataType = Arc1DataType.TalkWithPeopleInTown,
      area = Seq(Area2_Town),
      title = "Talk with people in town",
      effectLabel = EffectLabel.TalkAboutSoap,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(20),
      firstTimeUnlocksActions = _ => Seq(SellSoapToPeople),
    )

    def SellSoapToPeople: ActionData = ActionData(
      actionDataType = Arc1DataType.SellSoapToPeople,
      area = Seq(Area2_Town),
      title = "Try to sell Soap to people",
      effectLabel = EffectLabel.SellSoap,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(12),
      initialAmountOfActions = AmountOfActions.Unlimited,
      actionSuccessType = ActionSuccessType.WithFailure(0.5, 0.05),
      changeInventory = _.addItem(ItemType.Coins, 200).removeItem(ItemType.HerbSoap, 1),
      invalidReason = state =>
        Option.unless(state.inventory.canRemoveItem(ItemType.HerbSoap, 1))(
          ReasonLabel.NotEnoughSoapToSell
        ),
      firstTimeUnlocksActions = _ => Seq(ExploreTown),
    )

    def ExploreTown: ActionData = ActionData(
      actionDataType = Arc1DataType.ExploreTown,
      area = Seq(Area2_Town),
      title = "Explore Town",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(100),
      initialAmountOfActions = AmountOfActions.Standard(3),
      forceMaxAmountOfActionsIs1 = true,
      everyTimeUnlocksActions = {
        case (_, 1) => Seq(GoToEquipmentStore)
        case (_, 2) => Seq(GoToForest)
        case (_, 3) => Seq(BuyEmptyShop)
        case _      => Seq()
      },
    )

    def GoToEquipmentStore: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToEquipamentStore,
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
      actionDataType = Arc1DataType.BuyBigBag,
      area = Seq(Area4_EquipmentStore),
      name = "Big Bag",
      cost = 10,
      inventoryMaxSize = 10,
      actionTime = ActionTime.Standard(10),
      firstTimeUnlocksActions = _ => Seq(BuyHugeBag),
    )

    def BuyHugeBag: ActionData = buyInventoryIncrease(
      actionDataType = Arc1DataType.BuyHugeBag,
      area = Seq(Area4_EquipmentStore),
      name = "Huge Bag",
      cost = 20,
      inventoryMaxSize = 15,
      actionTime = ActionTime.Standard(10),
      firstTimeUnlocksActions = _ => Seq(),
    )

    def GoToForest: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToForest,
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
      actionDataType = Arc1DataType.PickupBerries,
      area = Seq(Area5_Forest),
      itemType = ItemType.Berries,
      amount = 1,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Unlimited,
    )

    def PickupPrettyFlower: ActionData = pickupToItem(
      actionDataType = Arc1DataType.PickupPrettyFlower,
      area = Seq(Area5_Forest),
      itemType = ItemType.PrettyFlower,
      amount = 1,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(SellFlowerInGeneralStore),
    )

    def SellFlowerInGeneralStore: ActionData = sellItemAction(
      actionDataType = Arc1DataType.SellFlowerInStore,
      area = Seq(Area3_Store),
      itemType = ItemType.PrettyFlower,
      amount = 1,
      coinsGain = 100,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Standard(10),
      firstTimeUnlocksActions = _ => Seq(),
    )

    def BuyEmptyShop: ActionData =
      ActionData(
        actionDataType = Arc1DataType.BuyEmptyShop,
        area = Seq(Area2_Town),
        title = s"Buy Empty Store",
        effectLabel = EffectLabel.BuyUpgrade(2500),
        kind = ActionKind.Social,
        actionTime = ActionTime.Standard(60),
        invalidReason = state =>
          Option.unless(state.inventory.canRemoveItem(ItemType.Coins, 2500))(
            ReasonLabel.NotEnoughCoins
          ),
        firstTimeUnlocksActions = _ => Seq(PrepareStoreForBusiness),
        addStory = state =>
          Option.when(state.stats.globalActionCount.getOrElse(Arc1DataType.BuyEmptyShop, 0) == 1)(
            Story.OtherLoops.BuyEmptyStop
          )
      )

    def PrepareStoreForBusiness: ActionData =
      ActionData(
        actionDataType = Arc1DataType.PrepareShopForBusiness,
        area = Seq(Area2_Town),
        title = s"Prepare Store for Soap Business",
        effectLabel = EffectLabel.Empty,
        kind = ActionKind.Crafting,
        actionTime = ActionTime.Standard(60),
        firstTimeUnlocksActions = _ => Seq(GoToMySoapStore),
        addStory = state =>
          Option.when(
            state.stats.globalActionCount.getOrElse(Arc1DataType.PrepareShopForBusiness, 0) == 1
          )(
            Story.OtherLoops.PrepareShopForBusiness
          ),
      )

    def GoToMySoapStore: ActionData = ActionData(
      actionDataType = Arc2DataType.GoToMySoapShop,
      area = Seq(Area2_Town),
      title = "Go to my Soap Shop",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(25, 0.5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      moveToArea = Some(Area6_My_Soap_Shop),
    )

  }

  object Story {

    object FirstLoop {

      val FirstWakeup: StoryLine =
        StoryLine.simple("""I wake up feeling unusually well-rested…
                           |Let's make a lavander soap today!""".stripMargin)

      val NoGlycerinShouldbuyFromStore: StoryLine =
        StoryLine.simple(
          """I don't have soap glycerin, I should pickup some coins and go buy it in the nearby general store.""".stripMargin
        )

      val GoToForestGetLavender: StoryLine =
        StoryLine.simple(
          """I have the soap glycerin, maybe I can pick some lavender from the nearby forest?""".stripMargin
        )

      val MysteriousSorcererInTheForest: StoryLine =
        StoryLine.simple(
          """I see a mysterious man, looks like a Sorcerer!""".stripMargin
        )

      val FirstLineSorcerer: StoryLine =
        StoryLine.simple(
          """A tall sorcerer appears out of nowhere, balancing a teacup with suspicious elegance.
            |“You weren’t supposed to see me yet” he mutters.
            |""".stripMargin
        )

      val FinalLineSorcerer: StoryLine =
        StoryLine
          .simple(
            """He scrambles for a spell — something about “resetting the timeline,” maybe?
              |Reality starts quietly peeling away like badly applied wallpaper.
              |You grab his arm; the teacup flips, dumping its contents all over you.
              |The sorcerer freezes. “Oh. That’s… not ideal.”
              |Before you can complain, everything politely fades to black.
              |""".stripMargin
          )
          .join(
            StoryLine.StoryPart.ForceAction(StoryActions.Data.FirstLoopFadingAway)
          )

      val FadingAway: StoryLine =
        StoryLine.simple(
          """You get very tired and go to sleep""".stripMargin
        )

    }

    object OtherLoops {

      val SecondWakeup: StoryLine =
        StoryLine.simple("She opens her eyes. The day looks… suspiciously familiar.")

      val ThirdOrMoreWakeup: StoryLine =
        StoryLine.simple("She wakes up with a single thought: “Not this loop again… or is it?”")

      val BuyEmptyStop: StoryLine =
        StoryLine.simple("Finally gathered enough money to buy my own soap where I can sell soap!")

      val PrepareShopForBusiness: StoryLine =
        StoryLine.simple("The shop is ready to receive clients!")

    }

    object Arc2Story {}

  }

}

package pt.rcmartins.loop.data

import pt.rcmartins.loop.data.LevelUtils._
import pt.rcmartins.loop.model.ActionDataType.Level1DataType
import pt.rcmartins.loop.model.CharacterArea._
import pt.rcmartins.loop.model.GameState.LoopCount
import pt.rcmartins.loop.model._

object Level1 {

  object Data {

    val InitialCharacterArea: CharacterArea = Area1_House
    val InitialActions: Seq[ActionData] = Seq(WakeUp)
    val InitialMoveActions: Seq[ActionData] = Seq.empty

    def WakeUp: ActionData = ActionData(
      actionDataType = Level1DataType.WakeUp,
      area = Seq(Area1_House),
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
//      case LoopCount(2) => Some(Story.OtherLoops.SecondWakeup)
//      case _ =>            Some(Story.OtherLoops.ThirdOrMoreWakeup)
        case _ => None
      },
    )

    def SearchLivingRoom: ActionData = ActionData(
      actionDataType = Level1DataType.SearchLivingRoom,
      area = Seq(Area1_House),
      title = "Search Living Room",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(5),
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(PickupSimpleSoapMold)
        case _            => Seq(PickupSimpleSoapMold, PickupCoins)
      },
    )

    def PickupSimpleSoapMold: ActionData = pickupToItem(
      actionDataType = Level1DataType.PickupSimpleSoapMold,
      area = Seq(Area1_House),
      itemType = ItemType.SimpleSoapMold,
      amount = 1,
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
      actionDataType = Level1DataType.PickupCoins,
      area = Seq(Area1_House),
      itemType = ItemType.Coins,
      amount = 1,
      actionTime = ActionTime.Standard(1),
      initialAmountOfActions = AmountOfActions.Standard(10),
      everyTimeUnlocksActions = {
        case (LoopCount(1), 10) => Seq(GoToGeneralStore)
        case _                  => Seq()
      },
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
      firstTimeUnlocksActions = _ => Seq(PickupMint, GoToGeneralStore),
      invalidReason = state =>
        Option.unless(
          state.actionsHistory.exists(_.actionDataType == Level1DataType.SearchKitchen) &&
            state.actionsHistory.exists(_.actionDataType == Level1DataType.SearchLivingRoom) &&
            state.actionsHistory.exists(_.actionDataType == Level1DataType.PickupSimpleSoapMold)
        )(ReasonLabel.Empty),
      showWhenInvalid = false,
    )

    def PickupMint: ActionData = gardeningAction(
      actionDataType = Level1DataType.PickMintGarden,
      area = Seq(Area1_House),
      itemType = ItemType.Mint,
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
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(BuyGlycerinFirstLoop)
        case _            => Seq(BuyGlycerin, BuyRawMomo, GoToBackToHouse)
      },
      moveToArea = Some(Area3_Store),
    )

    def BuyGlycerinFirstLoop: ActionData = buyItemAction(
      actionDataType = Level1DataType.BuyGlycerin,
      area = Seq(Area3_Store),
      itemType = ItemType.Glycerin,
      amount = 1,
      cost = 5,
      actionTime = ActionTime.Standard(20),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(FirstLoopGoToForest),
      addStory = _ => Some(Story.FirstLoop.GoToForestGetLavender),
    )

    def BuyGlycerin: ActionData = buyItemAction(
      actionDataType = Level1DataType.BuyGlycerin,
      area = Seq(Area3_Store),
      itemType = ItemType.Glycerin,
      amount = 1,
      cost = 5,
      actionTime = ActionTime.Standard(20),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(MeltGlycerin)
    )

    def FirstLoopGoToForest: ActionData = ActionData(
      actionDataType = Level1DataType.GoToForest,
      area = Seq(Area3_Store),
      title = "Go to the Forest",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.Standard(30),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(ExploreForestForLavender),
      moveToArea = Some(Area5_Forest),
    )

    def ExploreForestForLavender: ActionData = ActionData(
      actionDataType = Level1DataType.ExploreForestForLavender,
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
      actionDataType = Level1DataType.FindMysteriousSorcerer,
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
      actionDataType = Level1DataType.TalkMysteriousSorcerer,
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
      actionDataType = Level1DataType.FirstLoopFadingAway,
      area = Seq(Area5_Forest),
      title = """Fading away""",
      effectLabel = EffectLabel.Empty,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(30),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(),
      difficultyModifier = ActionDifficultyModifier(increaseTirednessAbsoluteMicro = 10_000_000),
      addStory = _ => Some(Story.FirstLoop.FadingAway),
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
      cost = Seq(ItemType.MeltedGlycerin -> 1, ItemType.Mint -> 1),
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
      actionDataType = Level1DataType.SellSoapToPeople,
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
      forceMaxAmountOfActions = Some(1),
      everyTimeUnlocksActions = {
        case (_, 1) => Seq(GoToEquipmentStore)
        case (_, 2) => Seq(GoToForest)
        case (_, 3) => Seq(BuyEmptyStore)
        case _      => Seq()
      },
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
      area = Seq(Area5_Forest),
      itemType = ItemType.Berries,
      amount = 1,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Unlimited,
    )

    def PickupPrettyFlower: ActionData = pickupToItem(
      actionDataType = Level1DataType.PickupPrettyFlower,
      area = Seq(Area5_Forest),
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
        effectLabel = EffectLabel.BuyEmptyStore(25),
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
        firstTimeUnlocksActions = _ => Seq(),
      )

  }

  object Story {

    case class StoryLineWithCondition(
        storyLine: StoryLine,
        actionDataType: ActionDataType,
        loopCond: Int => Boolean,
        stateCond: GameState => Boolean = _ => true,
    )

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
            StoryLine.StoryPart.ForceAction(Level1.Data.FirstLoopFadingAway)
          )

      val FadingAway: StoryLine =
        StoryLine.simple(
          """You get very tired and go to sleep""".stripMargin
        )

    }

    object OtherLoops {

      val SecondWakeup: StoryLineWithCondition =
        StoryLineWithCondition(
          StoryLine.simple("She opens her eyes. The day looks… suspiciously familiar."),
          Level1DataType.WakeUp,
          _ == 2,
        )

      val ThirdOrMoreWakeup: StoryLineWithCondition =
        StoryLineWithCondition(
          StoryLine.simple("She wakes up with a single thought: “Not this loop again… or is it?”"),
          Level1DataType.WakeUp,
          _ >= 3,
        )

    }

  }

}

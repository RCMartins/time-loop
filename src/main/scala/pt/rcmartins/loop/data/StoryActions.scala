package pt.rcmartins.loop.data

import pt.rcmartins.loop.data.ActionsUtils._
import pt.rcmartins.loop.model.ActionDataType._
import pt.rcmartins.loop.model.CharacterArea._
import pt.rcmartins.loop.model.GameState._
import pt.rcmartins.loop.model._

import scala.util.chaining.scalaUtilChainingOps

object StoryActions {

  object Data {

    val InitialCharacterArea: CharacterArea = Area1_Home
    val InitialActions: Seq[ActionData] = Seq(WakeUp)
    val InitialMoveActions: Seq[ActionData] = Seq.empty

    def WakeUp: ActionData = ActionData(
      actionDataType = Arc1DataType.WakeUp,
      area = _ => Seq(Area1_Home),
      title = "Wake up from bed",
      effectLabel = EffectLabel.Empty,
      kind = ActionKind.Agility,
      actionTime = ActionTime.Standard(7),
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(SearchLivingRoom, SearchKitchen)
        case _            => Seq(SearchLivingRoom, SearchKitchen, SearchGarden)
      },
      addStory = {
        case (LoopCount(1), _) => Some(Story.FirstLoop.FirstWakeup)
        case (LoopCount(2), _) => Some(Story.OtherLoops.SecondWakeup)
        case (LoopCount(3), _) => Some(Story.OtherLoops.ThirdOrMoreWakeup)
        case _                 => None
      },
    )

    def SearchLivingRoom: ActionData = ActionData(
      actionDataType = Arc1DataType.SearchLivingRoom,
      area = _ => Seq(Area1_Home),
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
      area = _ => Seq(Area1_Home),
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
        case (LoopCount(1), _) => Some(Story.FirstLoop.NoGlycerinShouldbuyFromStore)
        case _                 => None
      },
    )

    def PickupCoins: ActionData = pickupToItem(
      actionDataType = Arc1DataType.PickupCoins,
      area = _ => Seq(Area1_Home),
      itemType = ItemType.Coins,
      amount = 1.euro,
      actionTime = ActionTime.Standard(1),
      initialAmountOfActions = AmountOfActions.Standard(10),
      everyTimeUnlocksActions = {
        case (LoopCount(1), 10) => Seq(GoToGeneralStore)
        case _                  => Seq()
      },
    )

    def SearchKitchen: ActionData = ActionData(
      actionDataType = Arc1DataType.SearchKitchen,
      area = _ => Seq(Area1_Home),
      title = "Search the Kitchen",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(7),
      firstTimeUnlocksActions = _ => Seq(CookRice),
    )

    def CookRice: ActionData = cookingAction(
      actionDataType = Arc1DataType.CookRice,
      area = _ => Seq(Area1_Home),
      itemType = ItemType.Rice,
      amount = 1,
      cost = Seq(),
      actionTime = ActionTime.Standard(4),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
    )

    def SearchGarden: ActionData = ActionData(
      actionDataType = Arc1DataType.SearchGarden,
      area = _ => Seq(Area1_Home),
      title = "Explore the Garden",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(17),
      firstTimeUnlocksActions = _ => Seq(PickupRosemary, GoToGeneralStore),
      invalidReason = state =>
        Option.unless(
          state.actionsHistory.contains(Arc1DataType.SearchKitchen) &&
            state.actionsHistory.contains(Arc1DataType.SearchLivingRoom) &&
            state.actionsHistory.contains(Arc1DataType.PickupSimpleSoapMold)
        )(ReasonLabel.Empty),
      showWhenInvalid = false,
    )

    def PickupRosemary: ActionData = gardeningAction(
      actionDataType = Arc1DataType.PickRosemaryGarden,
      area = _ => Seq(Area1_Home),
      itemType = ItemType.Rosemary,
      amount = 1,
      actionTime = ActionTime.Standard(6),
      initialAmountOfActions = AmountOfActions.Unlimited,
    )

    def GoToGeneralStore: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToGeneralStore,
      area = {
        case LoopCount(1) => Seq(Area1_Home)
        case _            => Area3_GeneralStore.allConnections
      },
      title = "Go to the General Store",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(15, 0.8),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(BuyGlycerin, BuyFrozenMomo),
      everyTimeUnlocksActions = {
        case (LoopCount(n), _) if n > 1 => Seq(SpeakToShopKeeper, GoToBackHome)
        case _                          => Seq()
      },
      moveToArea = Some(Area3_GeneralStore),
    )

    def BuyGlycerin: ActionData = buyItemAction(
      actionDataType = Arc1DataType.BuyGlycerin,
      area = _ => Seq(Area3_GeneralStore),
      itemType = ItemType.Glycerin,
      amount = 1,
      cost = 5.euros,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(GoToForest)
        case _            => Seq(MeltGlycerin)
      },
      addStory = { case (LoopCount(1), _) => Some(Story.FirstLoop.GoToForestGetLavender) },
    )

    def GoToForest: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToForest,
      area = {
        case LoopCount(1) => Seq(Area3_GeneralStore)
        case _            => Area5_Forest.allConnections
      },
      title = "Go to the Forest",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(15, 0.8),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(ExploreForestForLavender)
        case _            => Seq(ExploreForestForLavender)
      },
      moveToArea = Some(Area5_Forest),
    )

    def ExploreForestForLavender: ActionData = ActionData(
      actionDataType = Arc1DataType.ExploreForestForLavender,
      area = _ => Seq(Area5_Forest),
      title = "Explore the forest for lavender",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(25),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = {
        case LoopCount(1) => Seq(FindMysteriousSorcerer)
        case _            => Seq(ForestSearchAreaAroundSorcererPosition)
      },
      addStory = {
        case (_, 1) => Some(Story.FirstLoop.MysteriousSorcererInTheForest)
        case (_, 2) => Some(Story.OtherLoops.NoSorcererThisTime)
      },
    )

    def FindMysteriousSorcerer: ActionData = ActionData(
      actionDataType = Arc1DataType.FindMysteriousSorcerer,
      area = _ => Seq(Area5_Forest),
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
      area = _ => Seq(Area5_Forest),
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
      area = _ => Seq(Area5_Forest),
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
      area = _ => Seq(Area3_GeneralStore),
      itemType = ItemType.FrozenMomo,
      amount = 1,
      cost = 50,
      actionTime = ActionTime.Standard(3),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(CookMomo),
    )

    def SpeakToShopKeeper: ActionData = ActionData(
      actionDataType = Arc1DataType.SpeakToShopKeeper,
      area = _ => Seq(Area3_GeneralStore),
      title = """Show your soaps""",
      effectLabel = EffectLabel.Empty,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(15),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(TradeSoapsForBag),
      addStory = { case (_, 1) => Some(Story.OtherLoops.SpeakWithGeneralShopKeeper) },
    )

    def TradeSoapsForBag: ActionData = tradeItemForBag(
      actionDataType = Arc1DataType.TradeSoapsForBag,
      area = _ => Seq(Area3_GeneralStore),
      title = """Trade 5 Herb Soaps for a Small Bag + 1 Glycerin""",
      effectLabel = EffectLabel.Custom("Note: Small Bag has 10 spaces"),
      cost = Seq(ItemType.RosemarySoap -> 5),
      bonus = Seq(ItemType.Glycerin -> 1),
      inventoryMaxSize = 10,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions = _ => Seq(GoToNeighborhood),
      addStory = { case (_, 1) =>
        Some(Story.OtherLoops.GeneralShopKeeperToldSellSoapsInNeighborhood)
      },
    )

    def CookMomo: ActionData = cookingAction(
      actionDataType = Arc1DataType.CookMomo,
      area = _ => Seq(Area1_Home),
      itemType = ItemType.Momo,
      amount = 1,
      cost = Seq(ItemType.FrozenMomo -> 1),
      actionTime = ActionTime.Standard(4),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      showWhenInvalid = false,
    )

    def GoToBackHome: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToBackHome,
      area = _ => Area1_Home.allConnections,
      title = "Go back home",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(15, 0.8),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      moveToArea = Some(Area1_Home),
    )

    def MeltGlycerin: ActionData = craftItem(
      actionDataType = Arc1DataType.MeltGlycerin,
      area = _ => Seq(Area1_Home, Area6_MySoapShop),
      itemType = ItemType.MeltedGlycerin,
      amount = 5,
      cost = Seq(ItemType.Glycerin -> 1),
      actionTime = ActionTime.Standard(15),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(CreateRosemarySoap),
      showWhenInvalid = false,
    )

    def CreateRosemarySoap: ActionData = craftItem(
      actionDataType = Arc1DataType.CreateRomesarySoap,
      area = _ => Seq(Area1_Home, Area6_MySoapShop),
      itemType = ItemType.RosemarySoap,
      amount = 1,
      actionSuccessType = ActionSuccessType.WithFailure(0.65, 0.05),
      cost = Seq(ItemType.MeltedGlycerin -> 1, ItemType.Rosemary -> 1),
      actionTime = ActionTime.Standard(15),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(GoToMarket),
      permanentBonusUnlocks =
        Seq(PermanentBonusUnlockType.ProgressiveActionCount(PermanentBonus.HalfTiredness, 1, 10)),
      showWhenInvalid = false,
    )

    def GoToNeighborhood: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToNeighborhood,
      area = _ => Area7_Neighborhood.allConnections,
      title = "Go to neighborhood",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(15, 0.8),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(SellSoapToPeopleInNeighborhood),
      moveToArea = Some(Area7_Neighborhood),
    )

    def SellSoapToPeopleInNeighborhood: ActionData = ActionData(
      actionDataType = Arc1DataType.SellSoapInNeighborhood,
      area = _ => Seq(Area7_Neighborhood),
      title = "Sell soap to your neighbors",
      effectLabel = EffectLabel.SellSoap(2.euros),
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Standard(20),
      actionSuccessType = ActionSuccessType.WithFailure(0.75, 0.05),
      changeInventory = _.addItem(ItemType.Coins, 2.euros).removeItem(ItemType.RosemarySoap, 1),
      invalidReason = state =>
        Option.unless(state.inventory.canRemoveItem(ItemType.RosemarySoap, 1))(
          ReasonLabel.NotEnoughSoapToSell
        ),
      everyTimeUnlocksActions = { case (_, 20) => Seq(GoToMarket) },
    )

    def GoToMarket: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToMarket,
      area = _ => Area2_Market.allConnections,
      title = "Go to Market",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(15, 0.8),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(TalkWithPeopleInMarket),
      moveToArea = Some(Area2_Market),
    )

    def TalkWithPeopleInMarket: ActionData = ActionData(
      actionDataType = Arc1DataType.SetupSoapStallInMarket,
      area = _ => Seq(Area2_Market),
      title = "Setup a stall in the market to sell soap",
      effectLabel = EffectLabel.Empty,
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(20),
      firstTimeUnlocksActions = _ => Seq(SellSoapToPeopleInMarket),
    )

    def SellSoapToPeopleInMarket: ActionData = ActionData(
      actionDataType = Arc1DataType.SellSoapToPeopleInMarket,
      area = _ => Seq(Area2_Market),
      title = "Sell Soap to people in the market",
      effectLabel = EffectLabel.SellSoap(3.euros),
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      actionSuccessType = ActionSuccessType.WithFailure(0.5, 0.05),
      changeInventory = _.addItem(ItemType.Coins, 3.euros).removeItem(ItemType.RosemarySoap, 1),
      invalidReason = state =>
        Option.unless(state.inventory.canRemoveItem(ItemType.RosemarySoap, 1))(
          ReasonLabel.NotEnoughSoapToSell
        ),
      firstTimeUnlocksActions = _ => Seq(ExploreMarket),
      everyTimeUnlocksActions = { case (_, 20) => Seq(BuyEmptyShop) },
      addStory = { case (_, 20) => Some(Story.OtherLoops.PleopleInMarketLikeMySoap) },
    )

    def ExploreMarket: ActionData = ActionData(
      actionDataType = Arc1DataType.ExploreMarket,
      area = _ => Seq(Area2_Market),
      title = "Explore Market area",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(30),
      initialAmountOfActions = AmountOfActions.Standard(1),
      forceMaxAmountOfActionsIs1 = true,
      everyTimeUnlocksActions = { case (_, 1) => Seq(GoToEquipmentStore) },
    )

    def GoToEquipmentStore: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToEquipamentStore,
      area = _ => Area4_EquipmentStore.allConnections,
      title = "Go to the Equipment Store",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(15, 0.8),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(BuyBigBag),
      moveToArea = Some(Area4_EquipmentStore),
    )

    def BuyBigBag: ActionData = buyInventoryIncrease(
      actionDataType = Arc1DataType.BuyBigBag,
      area = _ => Seq(Area4_EquipmentStore),
      name = "Big Bag",
      cost = 15.euros,
      inventoryMaxSize = 15,
      actionTime = ActionTime.Standard(10),
      firstTimeUnlocksActions = _ => Seq(BuyHugeBag),
    )

    def BuyHugeBag: ActionData = buyInventoryIncrease(
      actionDataType = Arc1DataType.BuyHugeBag,
      area = _ => Seq(Area4_EquipmentStore),
      name = "Huge Bag",
      cost = 25.euros,
      inventoryMaxSize = 20,
      actionTime = ActionTime.Standard(10),
      firstTimeUnlocksActions = _ => Seq(),
    )

    def BuyEmptyShop: ActionData =
      ActionData(
        actionDataType = Arc1DataType.BuyEmptyShop,
        area = _ => Seq(Area2_Market),
        title = s"Buy Empty Soap Shop",
        effectLabel = EffectLabel.BuyUpgrade(40.euros),
        kind = ActionKind.Social,
        actionTime = ActionTime.Standard(30),
        invalidReason = state =>
          Option.unless(state.inventory.canRemoveItem(ItemType.Coins, 40.euros))(
            ReasonLabel.NotEnoughCoins
          ),
        firstTimeUnlocksActions = _ => Seq(PrepareStoreForBusiness),
        addStory = { case (_, 1) => Some(Story.OtherLoops.BuyEmptyShop) },
      )

    def PrepareStoreForBusiness: ActionData =
      ActionData(
        actionDataType = Arc1DataType.PrepareShopForBusiness,
        area = _ => Seq(Area2_Market),
        title = s"Prepare Store for Soap Business",
        effectLabel = EffectLabel.Empty,
        kind = ActionKind.Crafting,
        actionTime = ActionTime.Standard(60),
        firstTimeUnlocksActions = _ => Seq(GoToMySoapStore),
        addStory = { case (_, 1) => Some(Story.OtherLoops.PrepareShopForBusiness) },
      )

    def GoToMySoapStore: ActionData = ActionData(
      actionDataType = Arc1DataType.GoToMySoapShop,
      area = _ => Area6_MySoapShop.allConnections,
      title = "Go to my Soap Shop",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      actionTime = ActionTime.ReduzedXP(15, 0.8),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      moveToArea = Some(Area6_MySoapShop),
    )

    def SellSoapToPeopleInSoapShop: ActionData = ActionData(
      actionDataType = Arc1DataType.SellSoapToPeopleInSoapShop,
      area = _ => Seq(Area6_MySoapShop),
      title = "Sell Soap to people in your shop",
      effectLabel = EffectLabel.SellSoap(4.euros),
      kind = ActionKind.Social,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      actionSuccessType = ActionSuccessType.WithFailure(0.5, 0.05),
      changeInventory = _.addItem(ItemType.Coins, 4.euros).removeItem(ItemType.RosemarySoap, 1),
      invalidReason = state =>
        Option.unless(state.inventory.canRemoveItem(ItemType.RosemarySoap, 1))(
          ReasonLabel.NotEnoughSoapToSell
        ),
      everyTimeUnlocksActions = { case (_, 20) => Seq(GoToForest) },
      addStory = { case (_, 20) => Some(Story.OtherLoops.ShouldGoToForest) },
    )

    def ForestSearchAreaAroundSorcererPosition: ActionData = ActionData(
      actionDataType = Arc1DataType.ForestSearchAreaAroundSorcererPosition,
      area = _ => Seq(Area5_Forest),
      title = "Explore area around last kwown sorcerer position",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.LinearTime(20, 10), // 20+30+40+50+60
      initialAmountOfActions = AmountOfActions.Standard(5),
      forceMaxAmountOfActionsIs1 = true,
      everyTimeUnlocksActions = {
        case (_, 1) => Seq(PickupBerries)
        case (_, 3) => Seq(PickupLavender)
        case (_, 5) => Seq(FollowHardToFindFootprintsPath)
      },
      addStory = {
        case (_, 1)     => Some(Story.OtherLoops.ForestSomeBerries)
        case (_, 2 | 4) => Some(Story.OtherLoops.ForestNothingUseful)
        case (_, 3)     => Some(Story.OtherLoops.ForestMagicLavender)
        case (_, 5)     => Some(Story.OtherLoops.ForestFootprintsPath)
      },
    )

    def PickupBerries: ActionData = pickupToItem(
      actionDataType = Arc1DataType.PickupBerries,
      area = _ => Seq(Area5_Forest),
      itemType = ItemType.Berries,
      amount = 1,
      actionTime = ActionTime.Standard(5),
      initialAmountOfActions = AmountOfActions.Unlimited,
    )

    def PickupLavender: ActionData = pickupToItem(
      actionDataType = Arc1DataType.PickupLavender,
      area = _ => Seq(Area5_Forest),
      itemType = ItemType.MagicLavender,
      amount = 1,
      actionTime = ActionTime.Standard(10),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(MakeMagicLavenderSoap),
      addStory = { case (_, 1) => Some(Story.OtherLoops.FoundMagicalLavender) },
    )

    def MakeMagicLavenderSoap: ActionData = craftItem(
      actionDataType = Arc1DataType.MakeMagicLavenderSoap,
      area = _ => Seq(Area1_Home, Area6_MySoapShop),
      itemType = ItemType.MagicLavenderSoap,
      amount = 1,
      actionSuccessType = ActionSuccessType.WithFailure(0.65, 0.05),
      cost = Seq(ItemType.MeltedGlycerin -> 1, ItemType.MagicLavender -> 1),
      actionTime = ActionTime.Standard(15),
      initialAmountOfActions = AmountOfActions.Unlimited,
      firstTimeUnlocksActions = _ => Seq(),
      addStory = { case (_, 1) => Some(Story.OtherLoops.MadeMagicalLavenderSoap) },
      showWhenInvalid = false,
    )

    def FollowHardToFindFootprintsPath: ActionData = ActionData(
      actionDataType = Arc1DataType.FollowHardToFindFootprintsPath,
      area = _ => Seq(Area5_Forest),
      title = "Follow hard to find footprints path",
      effectLabel = EffectLabel.Explore,
      kind = ActionKind.Exploring,
      actionTime = ActionTime.Standard(60),
      initialAmountOfActions = AmountOfActions.Standard(1),
      difficultyModifier = ActionDifficultyModifier(increaseTirednessAbsoluteMicro = 5_000_000)
    )

  }

  private object Story {

    object FirstLoop {

      val FirstWakeup: StoryLine =
        StoryLine.simple("""I wake up feeling unusually well-rested…
                           |Let's make a lavender soap today!""".stripMargin)

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
        StoryLine.simple("""I open my eyes. The day looks… suspiciously familiar.
                           |I don't think I should go to the forest today...""".stripMargin)

      val ThirdOrMoreWakeup: StoryLine =
        StoryLine.simple("I wake up with a single thought: “Not this loop again… or is it?”")

      val SpeakWithGeneralShopKeeper: StoryLine =
        StoryLine.simple(
          "The skop keeper is interested in my homemade soaps, maybe I should show her some?"
        )

      val GeneralShopKeeperToldSellSoapsInNeighborhood: StoryLine =
        StoryLine.simple(
          "The shop keeper likes my soaps. She told me that I could try selling them in the neighborhood."
        )

      val PleopleInMarketLikeMySoap: StoryLine =
        StoryLine.simple(
          "People in the market really like my soaps, I should try to have my own soap shop!"
        )

      val BuyEmptyShop: StoryLine =
        StoryLine.simple(
          "I finally gathered enough money to buy my own soap shop where I can sell soap!"
        )

      val PrepareShopForBusiness: StoryLine =
        StoryLine.simple("The shop is ready to receive clients!")

      val ShouldGoToForest: StoryLine =
        StoryLine.simple(
          "I should explore the forest again, maybe I can find something useful there."
        )

      val NoSorcererThisTime: StoryLine =
        StoryLine.simple(
          "I don't see the sorcerer this time, maybe I imagined him?"
        )

      val ForestSomeBerries: StoryLine =
        StoryLine.simple(
          "I found some berries in the forest, they might be useful later on."
        )

      val ForestNothingUseful: StoryLine =
        StoryLine.simple(
          "I found some footprints but nothing useful."
        )

      val ForestMagicLavender: StoryLine =
        StoryLine.simple(
          """I found some lavender, but it looks different from the usual one.
            |It seems to be glowing like the teacup the sorcerer had.
            |""".stripMargin
        )

      val ForestFootprintsPath: StoryLine =
        StoryLine.simple(
          """I found a path of footprints that seems to go deeper into the forest.
            |I should prepare myself better before following them.
            |""".stripMargin
        )

      val FoundMagicalLavender: StoryLine =
        StoryLine.simple(
          """This lavender looks magical, maybe I can make a special soap with it.
            |I hope it has some special properties.
            |""".stripMargin
        )

      val MadeMagicalLavenderSoap: StoryLine =
        StoryLine.simple(
          """I managed to make a magical lavender soap!
            |It seems to reduce tiredness increase dramatically for a while.
            |""".stripMargin
        )

    }

    object Arc2Story {}

  }

  val allActions: Map[ActionId, ActionData] = Seq(
    Data.WakeUp,
    Data.SearchLivingRoom,
    Data.PickupSimpleSoapMold,
    Data.PickupCoins,
    Data.SearchKitchen,
    Data.CookRice,
    Data.SearchGarden,
    Data.PickupRosemary,
    Data.GoToGeneralStore,
    Data.BuyGlycerin,
    Data.ExploreForestForLavender,
    Data.FindMysteriousSorcerer,
    Data.TalkMysteriousSorcerer,
    Data.FirstLoopFadingAway,
    Data.BuyFrozenMomo,
    Data.SpeakToShopKeeper,
    Data.TradeSoapsForBag,
    Data.CookMomo,
    Data.GoToBackHome,
    Data.MeltGlycerin,
    Data.CreateRosemarySoap,
    Data.GoToMarket,
    Data.GoToNeighborhood,
    Data.SellSoapToPeopleInNeighborhood,
    Data.TalkWithPeopleInMarket,
    Data.SellSoapToPeopleInMarket,
    Data.ExploreMarket,
    Data.GoToEquipmentStore,
    Data.BuyBigBag,
    Data.BuyHugeBag,
    Data.GoToForest,
    Data.ForestSearchAreaAroundSorcererPosition,
    Data.PickupBerries,
    Data.PickupLavender,
    Data.MakeMagicLavenderSoap,
    Data.FollowHardToFindFootprintsPath,
    Data.BuyEmptyShop,
    Data.PrepareStoreForBusiness,
    Data.GoToMySoapStore,
  ).pipe { seq =>
    val result = seq.map(a => a.actionDataType.id -> a).toMap
    if (result.size != seq.size)
      throw new IllegalStateException("Duplicate ActionData ids found")
    result
  }

}

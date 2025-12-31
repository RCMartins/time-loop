package pt.rcmartins.loop

import pt.rcmartins.loop.model.ActionDataType.Arc1DataType
import pt.rcmartins.loop.model.CharacterArea.Area1_Home
import pt.rcmartins.loop.model._
import zio.Scope
import zio.test.{assertTrue, Spec, ZIOSpecDefault}

object GameLogicTest extends ZIOSpecDefault {

  private def GameStateEmpty: GameState =
    GameState
      .initial(0L)
      .copy(
        visibleNextActions = Seq(),
        visibleMoveActions = Seq(),
      )

  private def gameUtils = new GameUtils()
  private def saveLoad = new SaveLoadMock()
  private def GameLogicBasic = new GameLogic(gameUtils, saveLoad)

  private val DummyWakeUpAction: ActionData = ActionData(
    actionDataType = Arc1DataType.WakeUp,
    area = _ => Seq(Area1_Home),
    title = "Test dummy action",
    effectLabel = EffectLabel.Empty,
    kind = ActionKind.Agility,
    actionTime = ActionTime.Standard(5),
  )

  private val DummyBuyNoodlesInMarket: ActionData = ActionData(
    actionDataType = Arc1DataType.BuyNoodlesInMarket,
    area = _ => Seq(Area1_Home),
    title = "Test unlimited action",
    effectLabel = EffectLabel.Empty,
    kind = ActionKind.Agility,
    actionTime = ActionTime.Standard(5),
    initialAmountOfActions = AmountOfActions.Unlimited,
    changeInventory = inventory => inventory.addItem(ItemType.Noodles, 1),
    invalidReason = state =>
      Option.unless(
        state.inventory.canAddItem(ItemType.Noodles, 1)
      )(
        ReasonLabel.InventoryFull
      ),
  )

  override def spec: Spec[Scope, Any] =
    suite("GameLogic")(
      suite("update")(
        test("update action completions after action finish") {
          val res: GameState =
            GameLogicBasic.update(
              GameStateEmpty.copy(currentAction = Some(DummyWakeUpAction.toActiveAction)),
              5_000L,
            )
          assertTrue(
            res.stats.loopActionCount.get(Arc1DataType.WakeUp.id).contains(1),
            res.stats.globalActionCount.get(Arc1DataType.WakeUp.id).contains(1),
          )
        },
        test("update only the time of the action") {
          val res: GameState =
            GameLogicBasic.update(
              GameStateEmpty.copy(currentAction =
                Some(DummyWakeUpAction.copy(actionTime = ActionTime.Standard(10)).toActiveAction)
              ),
              12_000L,
            )
          assertTrue(
            res.timeElapsedMicro == 10_000_000L,
            res.stats.totalElapedTimeMicro == 10_000_000L,
            res.skills.agility.loopLevel == 1,
          )
        },
        test("update only the time of the action with speed multiplier from level up") {
          val res: GameState =
            GameLogicBasic.update(
              GameStateEmpty.copy(currentAction =
                Some(DummyWakeUpAction.copy(actionTime = ActionTime.Standard(15)).toActiveAction)
              ),
              15_000L,
            )
          val actualExpectedtimePassed: Long =
            10_000_000 + // time to complete while at level 0
              Math.floor(5_000_000.0 / 1.05).toLong // time to complete while at level 1

          assertTrue(
            res.timeElapsedMicro == actualExpectedtimePassed,
            res.stats.totalElapedTimeMicro == actualExpectedtimePassed,
            res.skills.agility.loopLevel == 1,
          )
        },
        test("Check if rounding issues don't affect time calculation") {
          val res: GameState =
            GameLogicBasic.update(
              GameStateEmpty.copy(
                currentAction = Some(
                  DummyWakeUpAction
                    .copy(actionTime = ActionTime.Standard(15))
                    .toActiveAction
                    .copy(xpMultiplier = 0.722)
                ),
                skills = SkillsState.initial.copy(
                  agility = SkillState(
                    kind = ActionKind.Agility,
                    loopLevel = 13,
                    loopXPMicro = 21028824L,
                    permLevel = 48,
                    permXPMicro = 54493649L,
                    initialBonusMultiplier = 1.0,
                    currentBonusMultiplier = 1.0,
                  )
                )
              ),
              5_000L,
            )
          assertTrue(
            res.currentAction.isEmpty,
          )
        },
        test("update food actions/cooldowns properly") {
          val res: GameState =
            GameLogicBasic.update(
              GameStateEmpty.copy(
                currentAction = Some(
                  DummyWakeUpAction.copy(actionTime = ActionTime.Standard(1000)).toActiveAction
                ),
                inventory = InventoryState(20, Seq((ItemType.Rice, 20, 5_000_000L))),
                maxEnergyInt = 1000,
                energyMicro = 900_000_000L,
              ),
              105_000L,
            )
          assertTrue(
            res.inventory.items == Seq((ItemType.Rice, 0, 105_000_000L))
          )
        },
        test("finishing unlimited action make it go back to available") {
          val res: GameState =
            GameLogicBasic.update(
              GameStateEmpty.copy(
                visibleNextActions = Seq(
                  DummyBuyNoodlesInMarket
                    .copy(
                      actionTime = ActionTime.Standard(1),
                    )
                    .toActiveAction
                ),
                selectedNextAction = Some(ActionId(44L) -> None),
                inventory = InventoryState(5, Seq.empty),
              ),
              5_000L,
            )
          assertTrue(
            res.visibleNextActions.map(_.id) == Seq(ActionId(44L))
          )
        },
      )
    )

}

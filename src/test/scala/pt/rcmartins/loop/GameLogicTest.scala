package pt.rcmartins.loop

import pt.rcmartins.loop.model.ActionDataType.Arc1DataType
import pt.rcmartins.loop.model.CharacterArea.Area1_Home
import pt.rcmartins.loop.model._
import zio.Scope
import zio.test.{assertTrue, Spec, ZIOSpecDefault}

object GameLogicTest extends ZIOSpecDefault {

  private val GameStateEmpty = GameState.initial.copy(
    visibleNextActions = Seq(),
    visibleMoveActions = Seq(),
  )

  private val GameLogicBasic = new GameLogic(0L)

  private val GameDataEmpty = new GameData(GameStateEmpty, GameLogicBasic)

  private val DummyWakeUpAction: ActionData = ActionData(
    actionDataType = Arc1DataType.WakeUp,
    area = _ => Seq(Area1_Home),
    title = "Test wake Up",
    effectLabel = EffectLabel.Empty,
    kind = ActionKind.Agility,
    actionTime = ActionTime.Standard(5),
  )

  override def spec: Spec[Scope, Any] =
    suite("GameLogic")(
      suite("update")(
        test("update action completions after action finish") {
          val res: GameState =
            updateInSmallIncrements(
              GameLogicBasic,
              GameStateEmpty.copy(currentAction = Some(DummyWakeUpAction.toActiveAction)),
              5_000_000L,
            )
          assertTrue(
            res.stats.loopActionCount.get(Arc1DataType.WakeUp).contains(1),
            res.stats.globalActionCount.get(Arc1DataType.WakeUp).contains(1),
          )
        }
      )
    )

  private def updateInSmallIncrements(
      gameLogic: GameLogic,
      gameState: GameState,
      totalMicro: Long,
  ): GameState = {
    var currentState = gameState
    val increment = 100_000L
    var microLeft = totalMicro
    while (microLeft > 0) {
      val step = Math.min(increment, microLeft)
      currentState = gameLogic.update(currentState, currentState.timeElapsedMicro + step)
      microLeft -= step
    }
    currentState
  }

}

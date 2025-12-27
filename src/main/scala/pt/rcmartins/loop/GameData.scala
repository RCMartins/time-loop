package pt.rcmartins.loop

import com.raquo.airstream.state.Var
import com.raquo.laminar.api.L.Signal
import com.softwaremill.quicklens.ModifyPimp
import pt.rcmartins.loop.model._

class GameData(
    constructorGameState: GameState,
    gameLogic: GameLogic,
    val utils: GameUtils,
) {

  private val gameStateVar: Var[GameState] = Var(constructorGameState)

  // TODO should this be private?
  val gameState: Signal[GameState] = gameStateVar.signal

  val timeElapsedMicro: Signal[Long] = gameState.map(_.timeElapsedMicro).distinct
  val timeElapsedLong: Signal[Long] = timeElapsedMicro.map(_ / 1_000_000L).distinct
  private val globalTimeElapsedMicro: Signal[Long] =
    gameState.map(_.stats.totalElapedTimeMicro).distinct

  val globalTimeElapsedLong: Signal[Long] = globalTimeElapsedMicro.map(_ / 1_000_000L).distinct
  val loopNumber: Signal[Int] = gameState.map(_.stats.loopNumber).distinct

  val energyMicro: Signal[Long] = gameState.map(_.energyMicro).distinct
  val energyLong: Signal[Long] = energyMicro.map(e => Math.floor(e / 1e6).toLong).distinct
  val maxEnergyInt: Signal[Int] = gameState.map(_.maxEnergyInt).distinct
  val energyRatio: Signal[Double] =
    energyMicro
      .combineWith(maxEnergyInt)
      .map { case (energyMicro, maxEnergyInt) => (energyMicro / 1e6) / maxEnergyInt.toDouble }
      .distinct

  val currentTiredSecond: Signal[Double] = gameState.map(_.currentTiredSecond).distinct
  val currentTiredMultSecond: Signal[Double] = gameState.map(_.currentTiredMultSecond).distinct

  val skills: Signal[SkillsState] = gameState.map(_.skills).distinct
  val inventory: Signal[InventoryState] = gameState.map(_.inventory).distinct
  val currentAction: Signal[Option[ActiveActionData]] = gameState.map(_.currentAction).distinct
  val currentActionMovingArea: Signal[Option[CharacterArea]] =
    currentAction.map(_.flatMap(_.data.moveToArea))

  val nextActions: Signal[Seq[ActiveActionData]] = gameState.map(_.visibleNextActions).distinct
  val nextMoveActions: Signal[Seq[ActiveActionData]] = gameState.map(_.visibleMoveActions).distinct
  val deckActions: Signal[Seq[ActiveActionData]] = gameState.map(_.deckActions).distinct
  val selectedNextAction: Signal[Option[(ActionId, Option[Int])]] =
    gameState.map(_.selectedNextAction).distinct

  val storyActionsHistory: Signal[Seq[StoryLineHistory]] =
    gameState.map(_.storyActionsHistory).distinct

  val characterArea: Signal[CharacterArea] =
    gameState.map(_.characterArea).distinct

  val stats: Signal[Stats] = gameState.map(_.stats).distinct

  val showMapUI: Signal[Boolean] =
    stats.map(_.getGlobalCount(ActionDataType.Arc1DataType.PickupCoins) >= 10).distinct

  def runUpdateGameState(): Unit = {
    val initialGameState = gameStateVar.now()
    val currentTimeMicro = System.nanoTime() / 1000L
    val newState = gameLogic.update(initialGameState, currentTimeMicro)
    gameStateVar.set(newState)
  }

  def loadGameState(newGameState: GameState): Unit =
    gameStateVar.set(newGameState)

  def selectNextAction(actionId: ActionId, limitOfActions: Option[Int]): Unit =
    gameStateVar.update(
      _.modify(_.selectedNextAction).setTo(Some((actionId, limitOfActions)))
    )

  def selectNextMoveAction(area: CharacterArea): Unit = {
    if (gameStateVar.now().visibleMoveActions.exists(_.data.moveToArea.contains(area))) {
      val actionIdOpt: Option[ActionId] =
        gameStateVar
          .now()
          .visibleMoveActions
          .find(_.data.moveToArea.contains(area))
          .map(_.id)
      actionIdOpt.foreach { actionId =>
        selectNextAction(actionId, None)
      }
    }
  }

  def DebugLoopNow(): Unit = {
    gameStateVar.update { state =>
      val newState: GameState =
        state.resetForNewLoop
          .modify(_.stats.usedCheats)
          .setTo(true)
      SaveLoad.saveToLocalStorage(newState)
      newState
    }
  }

  def DebugHardReset(): Unit = {
    val newState = GameState.initial
    SaveLoad.saveToLocalStorage(newState)
    gameStateVar.set(newState)
  }

}

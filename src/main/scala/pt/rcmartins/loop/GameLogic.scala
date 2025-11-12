package pt.rcmartins.loop

import com.softwaremill.quicklens.ModifyPimp
import pt.rcmartins.loop.model._

import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

object GameLogic {

  private var lastTimeMicro: Long = System.nanoTime() / 1000L

  def update(initialGameState: GameState): GameState = {
    val currentTimeMicro = System.nanoTime() / 1000L
    val elapsedTimeMicro = Math.min(1_000_000L, currentTimeMicro - lastTimeMicro)
    lastTimeMicro = currentTimeMicro
    auxUpdate(initialGameState, elapsedTimeMicro)
  }

  @inline
  private def auxUpdate(initialGameState: GameState, elapsedTimeMicro: Long): GameState = {
    updateTiredness(updateAction(initialGameState, elapsedTimeMicro))
  }

  private def updateAction(initialGameState: GameState, elapsedTimeMicro: Long): GameState = {
    initialGameState.currentAction match {
      case None =>
        initialGameState.selectedNextAction.flatMap(id =>
          initialGameState.visibleNextActions.find(_.id == id)
        ) match {
          case Some(nextAction) =>
            initialGameState
              .modify(_.selectedNextAction)
              .setTo(None)
              .modify(_.currentAction)
              .setTo(Some(nextAction))
              .modify(_.visibleNextActions)
              .using(actions => actions.filterNot(_.id == nextAction.id))
          case None =>
            initialGameState
        }
      case Some(currentAction) =>
        val initialSkillState: SkillState = initialGameState.skills.get(currentAction.data.kind)
        val currentActionMicroSoFar = currentAction.microSoFar
        val elapsedWithMultiplier: Long =
          Math.floor(elapsedTimeMicro.toDouble * initialSkillState.finalSpeedMulti).toLong

        val currentActionElapsedMicro: Long =
          Math.min(
            currentAction.data.baseTimeMicro,
            currentActionMicroSoFar + elapsedWithMultiplier
          )

        val actualElapsedMicro = currentActionElapsedMicro - currentActionMicroSoFar

        val skillsUpdated: SkillsState =
          initialGameState.skills.update(
            currentAction.data.kind,
            skillState => {
              val newLoopXPMicro: Long = skillState.loopXPMicro + actualElapsedMicro
              val newPermXPMicro: Long = skillState.permXPMicro + actualElapsedMicro

              val (updatedLoopLevel, updatedLoopXPMicro) =
                if (newLoopXPMicro >= skillState.nextLoopXPMicro)
                  (skillState.loopLevel + 1, newLoopXPMicro - skillState.nextLoopXPMicro)
                else
                  (skillState.loopLevel, newLoopXPMicro)

              val (updatedPermLevel, updatedPermXPMicro) =
                if (newPermXPMicro >= skillState.nextPermXPMicro)
                  (skillState.permLevel + 1, newPermXPMicro - skillState.nextPermXPMicro)
                else
                  (skillState.permLevel, newPermXPMicro)

              skillState.copy(
                loopXPMicro = updatedLoopXPMicro,
                loopLevel = updatedLoopLevel,
                permLevel = updatedPermLevel,
                permXPMicro = updatedPermXPMicro,
              )
            }
          )

        val currentActionIsComplete: Boolean =
          currentActionElapsedMicro == currentAction.data.baseTimeMicro

        initialGameState
          .modify(_.currentAction)
          .using(_.map(_.copy(microSoFar = currentActionElapsedMicro)))
          .modify(_.timeElapsedMicro)
          .using(_ + actualElapsedMicro)
          .modify(_.skills)
          .setTo(skillsUpdated)
          .pipe(applyCurrentActionIfComplete(_, currentActionIsComplete, currentAction))
    }
  }

  private def applyCurrentActionIfComplete(
      state: GameState,
      currentActionIsComplete: Boolean,
      currentAction: ActiveActionData
  ): GameState =
    if (currentActionIsComplete)
      state
        .modify(_.actionsHistory)
        .using(_ :+ currentAction.data)
        .modify(_.deckActions)
        .using(_ ++ currentAction.data.unlocksActions.map(_.toActiveAction))
        .modify(_.inventory)
        .using(currentAction.data.changeInventory)
        .pipe(checkMultiAction(_, currentAction))
    else
      state

  private def checkMultiAction(
      state: GameState,
      justCompletedAction: ActiveActionData
  ): GameState = {
    if (justCompletedAction.amountOfActionsLeft > 1) {
      val updatedAction: ActiveActionData =
        justCompletedAction
          .modify(_.microSoFar)
          .setTo(0L)
          .modify(_.amountOfActionsLeft)
          .using(_ - 1)

      updatedAction.data.invalidReason(state) match {
        case Some(_) =>
          state
            .modify(_.currentAction)
            .setTo(None)
            .modify(_.deckActions)
            .using(_ :+ updatedAction)
            .pipe(drawNewCardsFromDeck)
        case None =>
          state
            .modify(_.currentAction)
            .setTo(Some(updatedAction))
      }
    } else {
      state
        .modify(_.currentAction)
        .setTo(None)
        .pipe(drawNewCardsFromDeck)
    }
  }

  private val MaximumAmountOfVisibleActions = 2

  private def drawNewCardsFromDeck(
      state: GameState
  ): GameState = {
    // TODO stable shuffle based on seed (with a stable random generator)
    val allAvailableActions: Seq[ActiveActionData] =
      Random.shuffle(state.deckActions ++ state.visibleNextActions)
    val (invisibleInvalid, visibleActions) =
      allAvailableActions.partition(action =>
        action.data.invalidReason(state).nonEmpty && !action.data.showWhenInvalid
      )

//    println(("state.deckActions", state.deckActions))
//    println(("state.visibleNextActions", state.visibleNextActions))
//    println(("visibleActions", visibleActions))

    val (nextActions, remainingDeckActions) =
      visibleActions.find(_.data.invalidReason(state).isEmpty) match {
        case None =>
          (
            visibleActions.take(MaximumAmountOfVisibleActions),
            visibleActions.drop(MaximumAmountOfVisibleActions),
          )
        case Some(validAction) =>
          val others: Seq[ActiveActionData] =
            visibleActions.filterNot(_.id == validAction.id)
          (
            Random.shuffle(validAction +: others.take(MaximumAmountOfVisibleActions - 1)),
            others.drop(MaximumAmountOfVisibleActions - 1)
          )
      }

//    println(("nextActions", nextActions))

    state
      .modify(_.visibleNextActions)
      .setTo(nextActions)
      .modify(_.deckActions)
      .setTo(remainingDeckActions ++ invisibleInvalid)
  }

  private def updateTiredness(initialGameState: GameState): GameState =
    if (initialGameState.timeElapsedMicro >= initialGameState.nextTiredIncreaseMicro) {
      initialGameState
        .modify(_.currentTiredSecond)
        .using(_ * initialGameState.currentTiredMultSecond)
        .modify(_.nextTiredIncreaseMicro)
        .using(_ + 1_000_000L)
        .modify(_.energyMicro)
        .using(energy => Math.max(0, energy - initialGameState.currentTiredSecondMicro))
        .pipe(checkDeathDueToTiredness)
    } else {
      initialGameState
    }

  private def checkDeathDueToTiredness(state: GameState): GameState =
    if (state.energyMicro == 0L) {
      val newState = state.resetForNewLoop
      SaveLoad.saveToLocalStorage(newState)
      newState
    } else {
      state
    }

}

package pt.rcmartins.loop

import com.softwaremill.quicklens.ModifyPimp
import pt.rcmartins.loop.model.GameState._
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
    val (newState, actualElapsedMicro) = updateAction(initialGameState, elapsedTimeMicro)
    updateTiredness(newState, actualElapsedMicro)
  }

  private def updateAction(
      initialGameState: GameState,
      elapsedTimeMicro: Long,
  ): (GameState, Long) = {
    initialGameState.currentAction match {
      case None =>
        initialGameState.selectedNextAction.flatMap { case (id, limit) =>
          initialGameState.visibleNextActions.find(_.id == id).map((_, limit))
        } match {
          case Some((nextAction, limit)) =>
            (
              initialGameState
                .modify(_.selectedNextAction)
                .setTo(None)
                .modify(_.currentAction)
                .setTo(Some(nextAction.copy(limitOfActions = limit)))
                .modify(_.visibleNextActions)
                .using(actions => actions.filterNot(_.id == nextAction.id)),
              0L
            )
          case None =>
            (initialGameState, 0L)
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

        val percentActualTimePassed: Double =
          (currentActionMicroSoFar + elapsedWithMultiplier) / currentActionElapsedMicro.toDouble
        val actualElapsedMicro = (elapsedTimeMicro * percentActualTimePassed).toLong

//        println("percentSec", percentSec)
//        println("actualElapsedMicro", actualElapsedMicro)
//        println("actualElapsedWithMultiplierMicro", actualElapsedWithMultiplierMicro)

        val skillsUpdated: SkillsState =
          initialGameState.skills.update(
            currentAction.data.kind,
            skillState => {
              val actualXPGainMicro: Long =
                Math
                  .ceil(
                    currentAction.xpMultiplier * elapsedWithMultiplier * percentActualTimePassed
                  )
                  .toLong
              val newLoopXPMicro: Long = skillState.loopXPMicro + actualXPGainMicro
              val newPermXPMicro: Long = skillState.permXPMicro + actualXPGainMicro

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
        val firstTimeComplete: Boolean =
          currentAction.numberOfCompletions == 0

        (
          initialGameState
            .modify(_.currentAction)
            .using(_.map(_.copy(microSoFar = currentActionElapsedMicro)))
            .modify(_.timeElapsedMicro)
            .using(_ + actualElapsedMicro)
            .modify(_.skills)
            .setTo(skillsUpdated)
            .pipe(
              applyCurrentActionIfComplete(
                _,
                currentActionIsComplete,
                firstTimeComplete,
                currentAction
              )
            ),
          actualElapsedMicro
        )
    }
  }

  private def applyCurrentActionIfComplete(
      state: GameState,
      currentActionIsComplete: Boolean,
      firstTimeComplete: Boolean,
      currentAction: ActiveActionData
  ): GameState =
    if (currentActionIsComplete) {
      val actionSuccess: Boolean = currentAction.currentActionSuccessChance >= Random.nextDouble()

      if (!actionSuccess)
        state
          .modify(_.currentAction)
          .setTo(
            Some(
              currentAction
                .modify(_.microSoFar)
                .setTo(0L)
                .modify(_.currentActionSuccessChance)
                .using(chance => Math.max(1.0, chance + currentAction.actionSuccessChanceIncrease))
            )
          )
      else
        state
          .modify(_.actionsHistory)
          .using(_ :+ currentAction.data)
          .modify(_.deckActions)
          .usingIf(firstTimeComplete)(
            _ ++ currentAction.data.firstTimeUnlocksActions(()).map(_.toActiveAction)
          )
          .modify(_.deckActions)
          .using(
            _ ++
              currentAction.data
                .everyTimeUnlocksActions(currentAction.numberOfCompletions + 1)
                .map(_.toActiveAction)
          )
          .modify(_.inventory)
          .using(currentAction.data.changeInventory)
          .modify(_.characterArea)
          .using(currentAction.data.moveToArea.getOrElse(_))
          .pipe(checkMultiAction(_, currentAction))
    } else
      state

  private def checkMultiAction(
      state: GameState,
      justCompletedAction: ActiveActionData
  ): GameState = {
    if (justCompletedAction.amountOfActionsLeft.moreThanOne) {
      val updatedAction: ActiveActionData =
        justCompletedAction
          .modify(_.microSoFar)
          .setTo(0L)
          .modify(_.amountOfActionsLeft)
          .using(_.reduceOne)
          .modify(_.xpMultiplier)
          .using { currentMultiplier =>
            justCompletedAction.data.actionTime match {
              case ActionTime.Standard(_)                => currentMultiplier
              case ActionTime.ReduzedXP(_, xpMultiplier) => currentMultiplier * xpMultiplier
            }
          }
          .modify(_.numberOfCompletions)
          .using(_ + 1)
          .modify(_.limitOfActions)
          .using {
            case Some(limit) if limit >= 1 => Some(limit - 1)
            case other                     => other
          }

      if (updatedAction.isInvalid(state) || updatedAction.limitOfActions.contains(0)) {
        state
          .modify(_.currentAction)
          .setTo(None)
          .modify(_.deckActions)
          .using(_ :+ updatedAction)
          .pipe(drawNewCardsFromDeck)
      } else {
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

  private def drawNewCardsFromDeck(
      state: GameState
  ): GameState = {
    // TODO stable shuffle based on seed (with a stable random generator)
    val allAvailableActions: Seq[ActiveActionData] =
      Random.shuffle(state.deckActions ++ state.visibleNextActions)
//    val (invisibleInvalid, visibleActions) =
//      allAvailableActions.partition(action =>
//        action.isInvalid(state) &&
//          (!action.data.showWhenInvalid || !action.areaIsValid(state))
//      )
    val (invalid, valid) =
      allAvailableActions.partition(_.isInvalid(state))
    val (invisibleInvalid, visibleInvalid) =
      invalid.partition(action => !action.data.showWhenInvalid || !action.areaIsValid(state))

//    println(("state.deckActions", state.deckActions))
//    println(("state.visibleNextActions", state.visibleNextActions))
//    println(("visibleActions", visibleActions))

    val (nextActions, remainingDeckActions) = {
      val (takenVisible, remainingVisible) =
        (valid ++ visibleInvalid).splitAt(MaximumAmountOfVisibleActions)
      (
        takenVisible,
        remainingVisible ++ invisibleInvalid
      )
    }

    //    println(("nextActions", nextActions))

    state
      .modify(_.visibleNextActions)
      .setTo(nextActions)
      .modify(_.deckActions)
      .setTo(remainingDeckActions)
  }

  private def updateTiredness(
      initialGameState: GameState,
      actualElapsedMicro: Long,
  ): GameState = {
    if (initialGameState.timeElapsedMicro >= initialGameState.nextTiredIncreaseMicro) {
      initialGameState
        .modify(_.currentTiredSecond)
        .using(_ * initialGameState.currentTiredMultSecond)
        .modify(_.nextTiredIncreaseMicro)
        .using(_ + 1_000_000L)
    } else {
      initialGameState
    }
  }
    .modify(_.energyMicro)
    .using(energy =>
      Math.max(
        0,
        energy - (initialGameState.currentTiredSecondMicro * (actualElapsedMicro / 1e6)).toLong
      )
    )
    .pipe(checkFoodConsumption)
    .pipe(checkDeathDueToTiredness)

  private def checkFoodConsumption(state: GameState): GameState =
    if (state.energyMicro == 0L) {
      state
    } else {
      val currentTime = state.timeElapsedMicro
      var energyMicro = state.energyMicro
      var items = state.inventory.items
      var anyChange: Boolean = false
      for (i <- items.indices) {
        val (itemType, amount, cooldown) = items(i)
        if (amount > 0 && itemType.isFoodItem && currentTime > cooldown) {
          if ((state.maxEnergyMicro - energyMicro) >= itemType.foodValueMicro) {
            energyMicro += itemType.foodValueMicro
            items = items.updated(
              i,
              (itemType, amount - 1, currentTime + FoodConsumptionIntervalMicro)
            )
            anyChange = true
          }
        }
      }
      if (!anyChange)
        state
      else
        state.copy(
          inventory = state.inventory.copy(items = items),
          energyMicro = energyMicro,
        )
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

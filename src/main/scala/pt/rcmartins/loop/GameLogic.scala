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

  private def auxUpdate(initialGameState: GameState, elapsedTimeMicro: Long): GameState = {
    initialGameState.currentAction match {
      case None =>
        println(s"No current action: selecting next action, ${initialGameState.selectedNextAction}")
        initialGameState.selectedNextAction.flatMap(id =>
          initialGameState.visibleNextActions.find(_.id == id)
        ) match {
          case Some(nextAction) =>
            println("Starting action: " + nextAction.data.title)
            initialGameState
              .modify(_.currentAction)
              .setTo(Some(nextAction))
              .modify(_.visibleNextActions)
              .using(actions => actions.filterNot(_.id == nextAction.id))
          case None =>
            println("No action selected")
            initialGameState
        }
      case Some(currentAction) =>
        val initialSkillState: SkillState = initialGameState.skills.get(currentAction.data.kind)
        val currentActionMicroSoFar = currentAction.microSoFar.now()
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

        currentAction.microSoFar.set(currentActionElapsedMicro)

        initialGameState
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
        .modify(_.deckActions)
        .using(_ ++ currentAction.data.unlocksActions.map(_.toActiveAction))
        .modify(_.currentAction)
        .setTo(None)
        .pipe(drawNewCardsFromDeck)
    else
      state

  private def drawNewCardsFromDeck(
      state: GameState
  ): GameState = {
    // TODO stable shuffle based on seed (with a stable random generator)
    val allAvailableActions: Seq[ActiveActionData] =
      Random.shuffle(state.deckActions ++ state.visibleNextActions)

    val nextActions: Seq[ActiveActionData] =
      allAvailableActions.find(_.data.isValid(state)) match {
        case None =>
//          Seq(BugActionData)
          Seq()
        case Some(validAction) =>
          val others: Seq[ActiveActionData] =
            allAvailableActions.filterNot(_.id == validAction.id).take(1)
          Random.shuffle(validAction +: others)
      }

    state
      .modify(_.visibleNextActions)
      .setTo(nextActions)

  }

  private val BugActionData =
    ActionData(
      actionDataType = ActionDataType.Bug,
      title = "Bug in action selection",
      subtitle = "No valid actions available",
      kind = ActionKind.Exploring,
      baseTimeSec = 10,
    ).toActiveAction

}

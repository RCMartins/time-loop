package pt.rcmartins.loop.model

import pt.rcmartins.loop.data.StoryActions
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GameStateSaved(
    version: Int,
    seed: Long,
    maxEnergyInt: Int,
    initialTiredSecond: Double,
    initialTiredMultSecond: Double,
    stats: StatsSaved,
    skills: SkillsState,
    storyActionsHistory: Seq[String],
) {

  def toGameState: GameState =
    GameState(
      version = version,
      seed = seed,
      timeElapsedMicro = 0L,
      energyMicro = maxEnergyInt * 1_000_000L,
      maxEnergyInt = maxEnergyInt,
      initialTiredSecond = initialTiredSecond,
      initialTiredMultSecond = initialTiredMultSecond,
      currentTiredSecond = initialTiredSecond,
      currentTiredMultSecond = initialTiredMultSecond,
      nextTiredIncreaseMicro = 1_000_000L,
      characterArea = StoryActions.Data.InitialCharacterArea,
      stats = stats.toStats,
      skills = skills.resetLoopProgress,
      inventory = InventoryState.initial,
      currentAction = None,
      visibleNextActions = StoryActions.Data.InitialActions.map(_.toActiveAction),
      visibleMoveActions = StoryActions.Data.InitialMoveActions.map(_.toActiveAction),
      selectedNextAction = None,
      deckActions = Seq.empty,
      actionsHistory = Seq.empty,
      storyActionsHistory = storyActionsHistory.map(StoryLineHistory.apply),
      inProgressStoryActions = Seq.empty,
    )

}

object GameStateSaved {

  def fromGameState(gameState: GameState): GameStateSaved =
    GameStateSaved(
      version = gameState.version,
      seed = gameState.seed,
      maxEnergyInt = gameState.maxEnergyInt,
      initialTiredSecond = gameState.initialTiredSecond,
      initialTiredMultSecond = gameState.initialTiredMultSecond,
      stats = StatsSaved.fromStats( gameState.stats),
      skills = gameState.skills,
      storyActionsHistory = gameState.storyActionsHistory.map(_.line),
    )

  implicit val decoder: JsonDecoder[GameStateSaved] = DeriveJsonDecoder.gen[GameStateSaved]
  implicit val encoder: JsonEncoder[GameStateSaved] = DeriveJsonEncoder.gen[GameStateSaved]

}

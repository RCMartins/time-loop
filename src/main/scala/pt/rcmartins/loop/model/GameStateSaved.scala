package pt.rcmartins.loop.model

import pt.rcmartins.loop.data.StoryActions
import pt.rcmartins.loop.model.migrations.GameSatedSavedVersion
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GameStateSaved(
    version: Int,
    seed: Long,
    timeElapsedMicro: Long,
    energyMicro: Long,
    maxEnergyInt: Int,
    initialTiredSecond: Double,
    initialTiredMultSecond: Double,
    currentTiredSecond: Double,
    currentTiredMultSecond: Double,
    nextTiredIncreaseMicro: Long,
    characterArea: CharacterArea,
    stats: StatsSaved,
    skills: SkillsState,
    inventory: InventoryState,
    visibleNextActions: Seq[ActiveActionData],
    visibleMoveActions: Seq[ActiveActionData],
    deckActions: Seq[ActiveActionData],
    actionsHistory: Seq[ActionDataType],
    storyActionsHistory: Seq[String],
) extends GameSatedSavedVersion {

  // TODO here
  def toGameState: GameState =
    GameState(
      version = version,
      seed = seed,
      timeElapsedMicro = timeElapsedMicro,
      energyMicro = energyMicro,
      maxEnergyInt = maxEnergyInt,
      initialTiredSecond = initialTiredSecond,
      initialTiredMultSecond = initialTiredMultSecond,
      currentTiredSecond = currentTiredSecond,
      currentTiredMultSecond = currentTiredMultSecond,
      nextTiredIncreaseMicro = nextTiredIncreaseMicro,
      characterArea = characterArea,
      stats = stats.toStats,
      skills = skills,
      inventory = inventory,
      currentAction = None,
      visibleNextActions = visibleNextActions,
      visibleMoveActions = visibleMoveActions,
      selectedNextAction = None,
      deckActions = deckActions,
      actionsHistory = actionsHistory,
      storyActionsHistory = storyActionsHistory.map(StoryLineHistory.apply),
      inProgressStoryActions = Seq.empty,
    )

}

object GameStateSaved {

  def fromGameState(gameState: GameState): GameStateSaved =
    GameStateSaved(
      version = gameState.version,
      seed = gameState.seed,
      timeElapsedMicro = gameState.timeElapsedMicro,
      energyMicro = gameState.energyMicro,
      maxEnergyInt = gameState.maxEnergyInt,
      initialTiredSecond = gameState.initialTiredSecond,
      initialTiredMultSecond = gameState.initialTiredMultSecond,
      currentTiredSecond = gameState.currentTiredSecond,
      currentTiredMultSecond = gameState.currentTiredMultSecond,
      nextTiredIncreaseMicro = gameState.nextTiredIncreaseMicro,
      characterArea = gameState.characterArea,
      stats = StatsSaved.fromStats(gameState.stats),
      skills = gameState.skills,
      inventory = gameState.inventory,
      visibleNextActions = gameState.visibleNextActions,
      visibleMoveActions = gameState.visibleMoveActions,
      deckActions = gameState.deckActions,
      actionsHistory = gameState.actionsHistory,
      storyActionsHistory = gameState.storyActionsHistory.map(_.line),
    )

  implicit val decoder: JsonDecoder[GameStateSaved] = DeriveJsonDecoder.gen[GameStateSaved]
  implicit val encoder: JsonEncoder[GameStateSaved] = DeriveJsonEncoder.gen[GameStateSaved]

}

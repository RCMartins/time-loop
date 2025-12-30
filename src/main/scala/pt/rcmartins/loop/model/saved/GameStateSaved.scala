package pt.rcmartins.loop.model.saved

import pt.rcmartins.loop.model.migrations.GameSatedSavedVersion
import pt.rcmartins.loop.model._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GameStateSaved(
    version: Int,
    seed: Long,
    updateLastTimeEpoch: Long,
    timeElapsedMicro: Long,
    extraTimeMicro: Long = 0L,
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
    currentAction: Option[ActiveActionData],
    visibleNextActions: Seq[ActiveActionData],
    visibleMoveActions: Seq[ActiveActionData],
    deckActions: Seq[ActiveActionData],
    storyActionsHistory: Seq[String],
    inProgressStoryActions: Seq[RunTimeStoryAction],
    buffs: Buffs,
    preferencesSaved: PreferencesSaved,
) extends GameSatedSavedVersion {

  def toGameState(currentTimeMillis: Long): GameState =
    GameState(
      version = version,
      seed = seed,
      updateLastTimeEpoch = updateLastTimeEpoch,
      timeElapsedMicro = timeElapsedMicro,
      timeElapsedMicroLastSave = timeElapsedMicro,
      extraTimeMicro = extraTimeMicro,
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
      currentAction = currentAction,
      visibleNextActions = visibleNextActions,
      visibleMoveActions = visibleMoveActions,
      selectedNextAction = None,
      deckActions = deckActions,
      storyActionsHistory = storyActionsHistory.map(StoryLineHistory.apply),
      inProgressStoryActions = inProgressStoryActions,
      buffs = buffs,
      preferences = preferencesSaved.toPreferences,
    )

}

object GameStateSaved {

  def fromGameState(gameState: GameState): GameStateSaved =
    GameStateSaved(
      version = gameState.version,
      seed = gameState.seed,
      updateLastTimeEpoch = gameState.updateLastTimeEpoch,
      timeElapsedMicro = gameState.timeElapsedMicro,
      extraTimeMicro = gameState.extraTimeMicro,
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
      currentAction = gameState.currentAction,
      visibleNextActions = gameState.visibleNextActions,
      visibleMoveActions = gameState.visibleMoveActions,
      deckActions = gameState.deckActions,
      storyActionsHistory = gameState.storyActionsHistory.map(_.line),
      inProgressStoryActions = gameState.inProgressStoryActions,
      buffs = gameState.buffs,
      preferencesSaved = PreferencesSaved.fromPreferences(gameState.preferences),
    )

  implicit val decoder: JsonDecoder[GameStateSaved] = DeriveJsonDecoder.gen[GameStateSaved]
  implicit val encoder: JsonEncoder[GameStateSaved] = DeriveJsonEncoder.gen[GameStateSaved]

}

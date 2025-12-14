package pt.rcmartins.loop.model.migrations

import pt.rcmartins.loop.data.StoryActions
import pt.rcmartins.loop.model._
import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class GameStateMinimal(
    version: Int,
    seed: Long,
    maxEnergyInt: Int,
    initialTiredSecond: Double,
    initialTiredMultSecond: Double,
    stats: StatsSaved,
    skills: SkillsState,
    storyActionsHistory: Seq[String],
) extends GameSatedSavedVersion {

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

object GameStateMinimal {

  implicit val decoder: JsonDecoder[GameStateMinimal] =
    DeriveJsonDecoder.gen[GameStateMinimal]

}

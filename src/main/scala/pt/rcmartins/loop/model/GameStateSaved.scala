package pt.rcmartins.loop.model

import pt.rcmartins.loop.data.Area1
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GameStateSaved(
    version: Int,
    seed: Long,
    maxEnergyInt: Int,
    initialTiredSecond: Double,
    initialTiredMultSecond: Double,
    stats: Stats,
    skills: SkillsState,
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
      stats = stats,
      skills = skills.resetLoopProgress,
      inventory = InventoryState.initial,
      currentAction = None,
      visibleNextActions = Area1.Data.InitialActionData.map(_.toActiveAction),
      selectedNextAction = None,
      deckActions = Seq.empty,
      actionsHistory = Seq.empty,
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
      stats = gameState.stats,
      skills = gameState.skills,
    )

  implicit val decoder: JsonDecoder[GameStateSaved] = DeriveJsonDecoder.gen[GameStateSaved]
  implicit val encoder: JsonEncoder[GameStateSaved] = DeriveJsonEncoder.gen[GameStateSaved]

}

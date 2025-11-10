package pt.rcmartins.loop.model

import pt.rcmartins.loop.data.Area1
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GameStateSaved(
    version: Int,
    seed: Long,
    maxEnergyInt: Int,
    tiredBaseSecond: Double,
    tiredMultSecond: Double,
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
      tiredBaseSecond = tiredBaseSecond,
      tiredMultSecond = tiredMultSecond,
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
      tiredBaseSecond = gameState.tiredBaseSecond,
      tiredMultSecond = gameState.tiredMultSecond,
      stats = gameState.stats,
      skills = gameState.skills,
    )

  implicit val decoder: JsonDecoder[GameStateSaved] = DeriveJsonDecoder.gen[GameStateSaved]
  implicit val encoder: JsonEncoder[GameStateSaved] = DeriveJsonEncoder.gen[GameStateSaved]

}

package pt.rcmartins.loop.model.migrations

import pt.rcmartins.loop.model.{GameState, SkillsState}
import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class GameStateSkillsOnly(
    version: Int,
    seed: Long,
    skills: SkillsState,
) extends GameSatedSavedVersion {

  def toGameState: GameState =
    GameState.initial.copy(
      version = version,
      seed = seed,
      skills = skills.resetLoopProgress,
    )

}

object GameStateSkillsOnly {

  implicit val decoder: JsonDecoder[GameStateSkillsOnly] =
    DeriveJsonDecoder.gen[GameStateSkillsOnly]

}

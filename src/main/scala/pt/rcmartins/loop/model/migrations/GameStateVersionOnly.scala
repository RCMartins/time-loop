package pt.rcmartins.loop.model.migrations

import pt.rcmartins.loop.model.GameState
import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class GameStateVersionOnly(
    version: Int
) extends GameSatedSavedVersion {

  def toGameState: GameState =
    GameState.initial.copy(
      version = version,
    )

}

object GameStateVersionOnly {

  implicit val decoder: JsonDecoder[GameStateVersionOnly] =
    DeriveJsonDecoder.gen[GameStateVersionOnly]

}

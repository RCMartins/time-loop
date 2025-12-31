package pt.rcmartins.loop.model.migrations

import pt.rcmartins.loop.model.GameState
import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class GameStateVersionOnly(
    version: Int,
    seed: Long,
) extends GameSatedSavedVersion {

  def toGameState(currentTimeMillis: Long): GameState =
    GameState
      .initial(currentTimeMillis)
      .copy(
        version = version,
        seed = seed,
      )

}

object GameStateVersionOnly {

  implicit val decoder: JsonDecoder[GameStateVersionOnly] =
    DeriveJsonDecoder.gen[GameStateVersionOnly]

}

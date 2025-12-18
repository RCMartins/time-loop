package pt.rcmartins.loop.model.migrations

import pt.rcmartins.loop.model.GameState

trait GameSatedSavedVersion {

  def toGameState: GameState

}

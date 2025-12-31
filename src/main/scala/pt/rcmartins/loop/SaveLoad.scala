package pt.rcmartins.loop

import pt.rcmartins.loop.model.GameState

trait SaveLoad {

  def saveToLocalStorage(gameState: GameState): Either[Throwable, Unit]

  def saveString(gameState: GameState): String

  def loadFromLocalStorage(): Option[GameState]

  def loadString(str: String): Option[GameState]

}

package pt.rcmartins.loop

import pt.rcmartins.loop.model.GameState

class SaveLoadMock extends SaveLoad {

  override def saveToLocalStorage(gameState: GameState): Either[Throwable, Unit] =
    Right(())

  override def saveString(gameState: GameState): String =
    ""

  override def loadFromLocalStorage(): Option[GameState] =
    None

  override def loadString(str: String): Option[GameState] =
    None

}

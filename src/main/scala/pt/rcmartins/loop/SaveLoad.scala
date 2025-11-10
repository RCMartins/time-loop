package pt.rcmartins.loop

import org.scalajs.dom.window.localStorage
import pt.rcmartins.loop.model.{GameState, GameStateSaved}
import zio.json.{DecoderOps, EncoderOps}

import scala.util.Try

object SaveLoad {

  private val SaveGameName = "save"

  def saveToLocalStorage(gameState: GameState): Either[Throwable, Unit] =
    Try {
      localStorage.setItem(SaveGameName, saveString(gameState))
    }.toEither

  def saveString(gameState: GameState): String =
    jsonToBase64(saveToJson(gameState))

  private def saveToJson(gameState: GameState): String =
    GameStateSaved.fromGameState(gameState).toJson

  private def jsonToBase64(str: String): String =
    java.util.Base64.getEncoder.encodeToString(str.getBytes)

  def loadFromLocalStorage(): Option[GameState] =
    Option(localStorage.getItem(SaveGameName))
      .flatMap(loadString)
      // TODO how to deal with version updates?
      .filter(_.version == GameState.CurrentVersion)

  def loadString(str: String): Option[GameState] = {
    val strTrimmed = str.trim
    if (strTrimmed.startsWith("{"))
      loadFromJson(strTrimmed)
    else
      loadFromJson(base64ToJson(strTrimmed))
  }

  private def loadFromJson(str: String): Option[GameState] =
    str.fromJson[GameStateSaved].toOption.map(_.toGameState)

  private def base64ToJson(str: String): String =
    new String(java.util.Base64.getDecoder.decode(str.getBytes))

  def reloadDataFromLoadedSave(
      loadedAllData: GameState,
//      messageAlert: Option[String] = Some("Game Loaded!"),
  ): Unit = {
    GameData.loadGameState(loadedAllData)
  }

}

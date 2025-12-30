package pt.rcmartins.loop

import org.scalajs.dom.window.localStorage
import pt.rcmartins.loop.model.GameState
import pt.rcmartins.loop.model.migrations._
import pt.rcmartins.loop.model.saved.GameStateSaved
import zio.json.{DecoderOps, EncoderOps}

import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

class SaveLoadImpl extends SaveLoad {

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

  private val versionsToTry: LazyList[String => Option[GameState]] =
    LazyList[String => Option[GameState]](
      _.fromJson[GameStateSaved].tap(printlnErrors).toOption.map(_.toGameState),
      _.fromJson[GameStateMinimal].tap(printlnErrors).toOption.map(_.toGameState),
      _.fromJson[GameStateSkillsOnly].tap(printlnErrors).toOption.map(_.toGameState),
      _.fromJson[GameStateVersionOnly].tap(printlnErrors).toOption.map(_.toGameState),
    )

  private def printlnErrors[T](either: Either[String, T]): Unit =
    either.left.foreach(err => println(s"Error loading save: $err"))

  private def loadFromJson(str: String): Option[GameState] =
    versionsToTry.map(_(str)).collectFirst { case Some(gs) => gs }

  private def base64ToJson(str: String): String =
    new String(java.util.Base64.getDecoder.decode(str.getBytes))

}

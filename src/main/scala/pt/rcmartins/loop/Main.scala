package pt.rcmartins.loop

import com.raquo.airstream.core.AirstreamError.CombinedError
import com.raquo.laminar.api.L._
import org.scalajs.dom
import pt.rcmartins.loop.data.StoryActions
import pt.rcmartins.loop.model.{GameState, ItemType}

object Main {

  def main(args: Array[String]): Unit = {
    AirstreamError.registerUnhandledErrorCallback { e =>
      println("=== UNHANDLED AIRSTREAM ERROR ===")

      def dump(t: Throwable, indent: String = ""): Unit = t match {
        case ce: CombinedError =>
          println(indent + s"CombinedError with ${ce.causes.size} inner errors")
          ce.causes.zipWithIndex.foreach { case (inner, i) =>
            println(indent + s"  child #$i:")
            inner.foreach(dump(_, indent + "    "))
          }
        case other =>
          println(indent + s"${other.getClass.getName}: ${other.getMessage}")
          other.getStackTrace.take(15).foreach { f =>
            println(indent + s"  at $f")
          }
      }

      dump(e)
    }

    // Hack to force object load before the json deserialization happens
    val _ = ItemType.Coins.id
    val _ = StoryActions.Data.InitialActions.map(_.actionDataType.id)

    val saveLoad: SaveLoad = new SaveLoadImpl()

    val currentGameState: GameState =
      saveLoad.loadFromLocalStorage() match {
        case None       => GameState.initial
        case Some(save) => save
      }

    val gameData: GameData = {
      val gameUtils = new GameUtils()
      new GameData(
        constructorGameState = currentGameState,
        // TODO add some kind of offline progress ?
        gameLogic = new GameLogic(System.currentTimeMillis(), gameUtils, saveLoad),
        utils = gameUtils,
      )
    }

    MobileUtils.setupMobileLogic()

    render(
      dom.document.getElementById("main-div"),
      new UI(gameData, saveLoad).run()
    )
  }

}

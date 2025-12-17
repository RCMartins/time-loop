package pt.rcmartins.loop

import com.raquo.airstream.core.AirstreamError.CombinedError
import com.raquo.laminar.api.L._
import org.scalajs.dom
import pt.rcmartins.loop.model.GameState

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

    val currentGameState: GameState =
      SaveLoad.loadFromLocalStorage() match {
        case None       => GameState.initial
        case Some(save) => save
      }

    val gameData: GameData = {
      val gameUtils = new GameUtils()
      new GameData(
        constructorGameState = currentGameState,
        // TODO add some kind of offline progress ?
        gameLogic = new GameLogic(System.currentTimeMillis(), gameUtils),
        utils = gameUtils,
      )
    }

    render(
      dom.document.getElementById("main-div"),
      new UI(gameData).run()
    )
  }

}

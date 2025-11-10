package pt.rcmartins.loop

import com.raquo.airstream.state.Var
import com.raquo.laminar.api.L.Signal
import com.raquo.laminar.keys.HtmlProp
import com.softwaremill.quicklens.ModifyPimp
import pt.rcmartins.loop.model._

object GameData {

  private val gameStateVar: Var[GameState] = Var(GameState.initial)

  // TODO should this be private?
  val gameState: Signal[GameState] = gameStateVar.signal

  val timeElapsedMicro: Signal[Long] = gameState.map(_.timeElapsedMicro).distinct
  val timeElapsedLong: Signal[Long] = timeElapsedMicro.map(_ / 1_000_000L).distinct

  val energyMicro: Signal[Long] = gameState.map(_.energyMicro).distinct
  val energyLong: Signal[Long] = energyMicro.map(_ / 1_000_000L.toInt).distinct

  val maxEnergyInt: Signal[Int] = gameState.map(_.maxEnergyInt).distinct
  val tiredBaseSecond: Signal[Double] = gameState.map(_.tiredBaseSecond).distinct
  val tiredMultSecond: Signal[Double] = gameState.map(_.tiredMultSecond).distinct

  val skills: Signal[SkillsState] = gameState.map(_.skills).distinct
  val inventory: Signal[InventoryState] = gameState.map(_.inventory).distinct
  val currentAction: Signal[Option[ActiveActionData]] = gameState.map(_.currentAction).distinct
  val nextActions: Signal[Seq[ActiveActionData]] = gameState.map(_.visibleNextActions).distinct
  val deckActions: Signal[Seq[ActiveActionData]] = gameState.map(_.deckActions).distinct
  val selectedNextAction: Signal[Option[Long]] = gameState.map(_.selectedNextAction).distinct

  def runUpdateGameState(): Unit = {
    val initialGameState = gameStateVar.now()
    val newState = GameLogic.update(initialGameState)
    gameStateVar.set(newState)
  }

  def loadGameState(newGameState: GameState): Unit =
    gameStateVar.set(newGameState)

  def selectNextAction(id: Long): Unit =
    gameStateVar.update(
      _.modify(_.selectedNextAction).setTo(Some(id))
    )

}

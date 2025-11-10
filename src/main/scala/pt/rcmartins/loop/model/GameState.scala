package pt.rcmartins.loop.model

import pt.rcmartins.loop.data.Area1

import scala.util.Random

case class GameState(
    version: Int,
    seed: Long,
    timeElapsedMicro: Long,
    energyMicro: Long,
    maxEnergyInt: Int,
    tiredBaseSecond: Double,
    tiredMultSecond: Double,
    stats: Stats,
    skills: SkillsState,
    inventory: InventoryState,
    currentAction: Option[ActiveActionData],
    visibleNextActions: Seq[ActiveActionData],
    selectedNextAction: Option[Long],
    deckActions: Seq[ActiveActionData],
    actionsHistory: Seq[ActionData],
)

object GameState {

  val CurrentVersion: Int = 1
  private val StartingMaxEnergy: Int = 100

  val initial: GameState = GameState(
    version = CurrentVersion,
    seed = Random.nextLong(),
    timeElapsedMicro = 0L,
    energyMicro = StartingMaxEnergy * 1000000L,
    maxEnergyInt = StartingMaxEnergy,
    tiredBaseSecond = 0.1,
    tiredMultSecond = 1.01,
    stats = Stats.initial,
    skills = SkillsState.initial,
    inventory = InventoryState.initial,
    currentAction = None,
    visibleNextActions = Area1.Data.InitialActionData.map(_.toActiveAction),
    selectedNextAction = None,
    deckActions = Seq(),
    actionsHistory = Seq(),
  )

  def save(): Unit = {}

  def load(dataStr: String): GameState = {
    initial
  }

}

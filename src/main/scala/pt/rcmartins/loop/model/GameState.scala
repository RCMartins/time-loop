package pt.rcmartins.loop.model

import pt.rcmartins.loop.data.Area1

case class GameState(
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
)

object GameState {

  val StartingMaxEnergy: Int = 100
  val initial: GameState = GameState(
    timeElapsedMicro = 0L,
    energyMicro = StartingMaxEnergy * 1000000L,
    maxEnergyInt = StartingMaxEnergy,
    tiredBaseSecond = 0.1,
    tiredMultSecond = 1.01,
    stats = Stats.initial,
    skills = SkillsState.initial,
    inventory = InventoryState.initial,
    currentAction = None, // Some(Area1.Data.WakeUp.toActiveAction), // None
    visibleNextActions = Seq(Area1.Data.WakeUp.toActiveAction),
    selectedNextAction = None,
    deckActions = Seq(),
  )

  def save(): Unit = {}

  def load(dataStr: String): GameState = {
    initial
  }

}

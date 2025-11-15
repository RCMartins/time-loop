package pt.rcmartins.loop.model

import pt.rcmartins.loop.data.Level1

import scala.util.Random

case class GameState(
    version: Int,
    seed: Long,
    timeElapsedMicro: Long,
    energyMicro: Long,
    maxEnergyInt: Int,
    initialTiredSecond: Double,
    initialTiredMultSecond: Double,
    currentTiredSecond: Double,
    currentTiredMultSecond: Double,
    nextTiredIncreaseMicro: Long,
    characterArea: CharacterArea,
    stats: Stats,
    skills: SkillsState,
    inventory: InventoryState,
    currentAction: Option[ActiveActionData],
    visibleNextActions: Seq[ActiveActionData],
    selectedNextAction: Option[Long],
    deckActions: Seq[ActiveActionData],
    actionsHistory: Seq[ActionData],
) {

  def currentTiredSecondMicro: Long = (currentTiredSecond * 1_000_000L).toLong
  def maxEnergyMicro: Long = maxEnergyInt * 1_000_000L

  def resetForNewLoop: GameState =
    this.copy(
      timeElapsedMicro = 0L,
      energyMicro = maxEnergyInt * 1_000_000L,
      currentTiredSecond = initialTiredSecond,
      currentTiredMultSecond = initialTiredMultSecond,
      nextTiredIncreaseMicro = 1_000_000L,
      stats = stats.resetForNewLoop,
      skills = skills.resetLoopProgress,
      inventory = InventoryState.initial,
      currentAction = None,
      visibleNextActions = Level1.Data.InitialActionData.map(_.toActiveAction),
      selectedNextAction = None,
      deckActions = Seq(),
      actionsHistory = Seq(),
    )

}

object GameState {

  val CurrentVersion: Int = 1

  private val StartingMaxEnergy: Int = 100
  private val initialTiredSecond: Double = 0.1
  private val InitialTiredMultSecond: Double = 1.00372699 // x^60=1.25 per minute

  val MaximumAmountOfVisibleActions = 4
  val FoodConsumptionIntervalMicro: Long = 5 * 1_000_000L

  val initial: GameState = GameState(
    version = CurrentVersion,
    seed = Random.nextLong(),
    timeElapsedMicro = 0L,
    energyMicro = StartingMaxEnergy * 1_000_000L,
    maxEnergyInt = StartingMaxEnergy,
    initialTiredSecond = initialTiredSecond,
    initialTiredMultSecond = InitialTiredMultSecond,
    currentTiredSecond = initialTiredSecond,
    currentTiredMultSecond = InitialTiredMultSecond,
    nextTiredIncreaseMicro = 1_000_000L,
    characterArea = Level1.Data.InitialCharacterArea,
    stats = Stats.initial,
    skills = SkillsState.initial,
    inventory = InventoryState.initial,
    currentAction = None,
    visibleNextActions = Level1.Data.InitialActionData.map(_.toActiveAction),
    selectedNextAction = None,
    deckActions = Seq(),
    actionsHistory = Seq(),
  )

}

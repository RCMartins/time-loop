package pt.rcmartins.loop.model

import com.softwaremill.quicklens.ModifyPimp
import pt.rcmartins.loop.data.StoryActions

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
    visibleMoveActions: Seq[ActiveActionData],
    selectedNextAction: Option[(ActionId, Option[Int])],
    deckActions: Seq[ActiveActionData],
    actionsHistory: Seq[ActionDataType],
    storyActionsHistory: Seq[StoryLineHistory],
    inProgressStoryActions: Seq[RunTimeStoryAction],
) {

  def currentTiredSecondMicro: Long = (currentTiredSecond * 1_000_000L).toLong
  def maxEnergyMicro: Long = maxEnergyInt * 1_000_000L

  def resetForNewLoop: GameState =
    GameState(
      version = version,
      seed = seed,
      timeElapsedMicro = 0L,
      energyMicro = maxEnergyInt * 1_000_000L,
      maxEnergyInt = maxEnergyInt,
      initialTiredSecond = initialTiredSecond,
      initialTiredMultSecond = initialTiredMultSecond,
      currentTiredSecond = initialTiredSecond,
      currentTiredMultSecond = initialTiredMultSecond,
      nextTiredIncreaseMicro = 1_000_000L,
      characterArea = StoryActions.Data.InitialCharacterArea,
      stats = stats.resetForNewLoop,
      skills = skills.resetLoopProgress,
      inventory = InventoryState.initial,
      currentAction = None,
      visibleNextActions = StoryActions.Data.InitialActions.map(_.toActiveAction),
      visibleMoveActions = StoryActions.Data.InitialMoveActions.map(_.toActiveAction),
      selectedNextAction = None,
      deckActions = Seq(),
      actionsHistory = Seq(),
      storyActionsHistory = storyActionsHistory,
      inProgressStoryActions = Seq(),
    )

  def addElapedTimeMicro(actualElapsedMicro: Long): GameState =
    this.modifyAll(_.timeElapsedMicro, _.stats.totalElapedTimeMicro).using(_ + actualElapsedMicro)

}

object GameState {

  val CurrentVersion: Int = 1

  private val StartingMaxEnergy: Int = 100
  private val initialTiredSecond: Double = 0.2
  private val InitialTiredMultSecond: Double = 1.00372699 // x^60=1.25 per minute

  val MaximumAmountOfVisibleActions = 4
  val FoodConsumptionIntervalMicro: Long = 5 * 1_000_000L

  val StoryLineDelayMicro = 3_000_000

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
    characterArea = StoryActions.Data.InitialCharacterArea,
    stats = Stats.initial,
    skills = SkillsState.initial,
    inventory = InventoryState.initial,
    currentAction = None,
    visibleNextActions = StoryActions.Data.InitialActions.map(_.toActiveAction),
    visibleMoveActions = StoryActions.Data.InitialMoveActions.map(_.toActiveAction),
    selectedNextAction = None,
    deckActions = Seq(),
    actionsHistory = Seq(),
    storyActionsHistory = Seq(),
    inProgressStoryActions = Seq(),
  )

  object LoopCount {

    def unapply(gameState: GameState): Option[Int] =
      Some(gameState.stats.loopNumber)

  }

}

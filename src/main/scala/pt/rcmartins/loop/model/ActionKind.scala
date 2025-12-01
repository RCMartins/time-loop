package pt.rcmartins.loop.model

import com.raquo.laminar.api.L.Signal
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import scala.util.Random

sealed trait ActionKind {
  def name: String
}

object ActionKind {
  case object Agility extends ActionKind { val name: String = "Agility" }
  case object Exploring extends ActionKind { val name: String = "Exploring" }
  case object Foraging extends ActionKind { val name: String = "Foraging" }
  case object Social extends ActionKind { val name: String = "Social" }
  case object Crafting extends ActionKind { val name: String = "Crafting" }
  case object Gardening extends ActionKind { val name: String = "Gardening" }
  case object Cooking extends ActionKind { val name: String = "Cooking" }
  case object Magic extends ActionKind { val name: String = "Magic" }

  implicit val decoder: JsonDecoder[ActionKind] = DeriveJsonDecoder.gen[ActionKind]
  implicit val encoder: JsonEncoder[ActionKind] = DeriveJsonEncoder.gen[ActionKind]

}

final case class ActionData(
    actionDataType: ActionDataType,
    area: Seq[CharacterArea],
    title: String,
    effectLabel: EffectLabel,
    kind: ActionKind,
    actionTime: ActionTime,
    actionSuccessType: ActionSuccessType = ActionSuccessType.Always,
    initialAmountOfActions: AmountOfActions = AmountOfActions.Standard(1),
    forceMaxAmountOfActions: Option[Int] = None,
    firstTimeUnlocksActions: GameState => Seq[ActionData] = _ => Seq.empty,
    everyTimeUnlocksActions: (GameState, Int) => Seq[ActionData] = (_, _) => Seq.empty,
    addStory: GameState => Option[StoryLine] = _ => None,
    invalidReason: GameState => Option[ReasonLabel] = _ => None,
    showWhenInvalid: Boolean = true,
    changeInventory: InventoryState => InventoryState = identity,
    moveToArea: Option[CharacterArea] = None,
    difficultyModifier: ActionDifficultyModifier = ActionDifficultyModifier.empty,
) {

  val baseTimeMicro: Long = actionTime.baseTimeSec * 1_000_000L

  def toActiveAction: ActiveActionData =
    new ActiveActionData(
      id = ActionId(Random.nextLong()),
      data = this,
      microSoFar = 0L,
      xpMultiplier = 1.0,
      amountOfActionsLeft = initialAmountOfActions,
      currentActionSuccessChance = actionSuccessType match {
        case ActionSuccessType.Always                     => 1.0
        case ActionSuccessType.WithFailure(baseChance, _) => baseChance
      },
      actionSuccessChanceIncrease = actionSuccessType match {
        case ActionSuccessType.Always                   => 0.0
        case ActionSuccessType.WithFailure(_, increase) => increase
      },
    )

}

case class ActiveActionData(
    id: ActionId,
    data: ActionData,
    microSoFar: Long,
    xpMultiplier: Double,
    amountOfActionsLeft: AmountOfActions,
    currentActionSuccessChance: Double,
    actionSuccessChanceIncrease: Double,
    limitOfActions: Option[Int] = None,
    numberOfCompletions: Int = 0,
) {

  override def toString: String =
    s"ActiveActionData(data=${data.title}, microSoFar=$microSoFar, amountOfActionsLeft=$amountOfActionsLeft)"

  def areaIsValid(state: GameState): Boolean =
    data.area.contains(state.characterArea)

  def isInvalid(state: GameState): Boolean =
    data.invalidReason(state).nonEmpty || !areaIsValid(state)

}

object ActiveActionData {

  def longSoFar(action: Signal[ActiveActionData]): Signal[Long] =
    action.map(_.microSoFar / 1_000_000L)

  def microLeft(action: Signal[ActiveActionData]): Signal[Long] =
    action.map(action => action.data.baseTimeMicro - action.microSoFar)

  def progressRatio(action: Signal[ActiveActionData]): Signal[Double] =
    action.map(action => action.microSoFar.toDouble / action.data.baseTimeMicro.toDouble)

}

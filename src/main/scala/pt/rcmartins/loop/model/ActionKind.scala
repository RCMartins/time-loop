package pt.rcmartins.loop.model

import com.raquo.laminar.api.L.{Signal, Var}
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
    title: String,
    effectLabel: EffectLabel,
    kind: ActionKind,
    baseTimeSec: Long,
    initialAmountOfActions: Int = 1,
    unlocksActions: Seq[ActionData] = Seq.empty,
    invalidReason: GameState => Option[ReasonLabel] = _ => None,
    showWhenInvalid: Boolean = true,
    changeInventory: InventoryState => InventoryState = identity,
) {

  def baseTimeMicro: Long = baseTimeSec * 1_000_000L

  def toActiveAction: ActiveActionData =
    new ActiveActionData(
      id = Random.nextLong(),
      data = this,
      microSoFar = 0L,
      amountOfActionsLeft = initialAmountOfActions,
    )

}

case class ActiveActionData(
    id: Long,
    data: ActionData,
    microSoFar: Long,
    amountOfActionsLeft: Int,
) {

  override def toString: String =
    s"ActiveActionData(data=${data.title}, microSoFar=$microSoFar, amountOfActionsLeft=$amountOfActionsLeft)"

}

object ActiveActionData {

  def longSoFar(action: Signal[ActiveActionData]): Signal[Long] =
    action.map(_.microSoFar / 1_000_000L)

  def microLeft(action: Signal[ActiveActionData]): Signal[Long] =
    action.map(action => action.data.baseTimeMicro - action.microSoFar)

  def progressRatio(action: Signal[ActiveActionData]): Signal[Double] =
    action.map(action => action.microSoFar.toDouble / action.data.baseTimeMicro.toDouble)

}

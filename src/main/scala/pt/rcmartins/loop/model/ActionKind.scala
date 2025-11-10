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
  case object Cooking extends ActionKind { val name: String = "Cooking" }
  case object Crafting extends ActionKind { val name: String = "Crafting" }
  case object Gardening extends ActionKind { val name: String = "Gardening" }
  case object Foraging extends ActionKind { val name: String = "Foraging" }
  case object Social extends ActionKind { val name: String = "Social" }
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
    unlocksActions: Seq[ActionData] = Seq.empty,
    invalidReason: GameState => Option[ReasonLabel] = _ => None,
    changeInventory: InventoryState => InventoryState = identity,
) {

  def baseTimeMicro: Long = baseTimeSec * 1_000_000L

  def toActiveAction: ActiveActionData =
    new ActiveActionData(
      id = Random.nextLong(),
      data = this,
      microSoFar = Var(0L),
    )

}

class ActiveActionData(
    val id: Long,
    val data: ActionData,
    val microSoFar: Var[Long],
) {

  val longSoFar: Signal[Long] =
    microSoFar.signal.map(_ / 1_000_000L)

  val progressRatio: Signal[Double] =
    microSoFar.signal.map(_.toDouble / data.baseTimeMicro.toDouble)

  override def toString: String =
    s"ActiveActionData(id=$id, data=${data.title}, microSoFar=${microSoFar.now()})"

}

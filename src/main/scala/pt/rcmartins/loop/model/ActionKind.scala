package pt.rcmartins.loop.model

import com.raquo.laminar.api.L.{Signal, Var}

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
}

final case class ActionData(
    actionDataType: ActionDataType,
    title: String,
    subtitle: String,
    kind: ActionKind,
    baseTimeSec: Long,
    unlocksActions: Seq[ActionData] = Seq.empty,
    //    enable: Signal[Boolean] = Val(true)
    isValid: GameState => Boolean = _ => true,
) {

  def baseTimeMicro: Long = baseTimeSec * 1_000_000L

  def toActiveAction: ActiveActionData =
    new ActiveActionData(
      id = Random.nextLong(),
      data = this,
      microSoFar = Var(0L),
    )

}

//class InGameActionData(
//    id: String,
//    data: ActionData,
//    microSoFar: Var[Long],
//) {
//
//  def progressRatio: Double =
//    microSoFar.toDouble / (data.baseTimeSec * 1000000).toDouble
//
//}

class ActiveActionData(
    val id: Long,
    val data: ActionData,
    val microSoFar: Var[Long],
) {

  val longSoFar: Signal[Long] =
    microSoFar.signal.map(_ / 1_000_000L)

  val progressRatio: Signal[Double] =
    microSoFar.signal.map(_.toDouble / data.baseTimeMicro.toDouble)

}

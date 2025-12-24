package pt.rcmartins.loop.model

import com.raquo.laminar.api.L.Signal
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ActiveActionData(
    data: ActionData,
    microSoFar: Long,
    targetTimeMicro: Long,
    xpMultiplier: Double,
    amountOfActionsLeft: AmountOfActions,
    currentActionSuccessChance: Double,
    actionSuccessChanceIncrease: Double,
    limitOfActions: Option[Int] = None,
    numberOfCompletions: Int = 0,
) {

  val id: ActionId = data.actionDataType.id

  override def toString: String =
    s"ActiveActionData(data=${data.title}, microSoFar=$microSoFar, amountOfActionsLeft=$amountOfActionsLeft)"

  @inline
  def areaIsValid(state: GameState): Boolean =
    ActiveActionData.areaIsValid(state, data)

  @inline
  def isInvalid(state: GameState): Boolean =
    ActiveActionData.isInvalid(state, data)

}

object ActiveActionData {

  implicit val decoder: JsonDecoder[ActiveActionData] = DeriveJsonDecoder.gen[ActiveActionData]
  implicit val encoder: JsonEncoder[ActiveActionData] = DeriveJsonEncoder.gen[ActiveActionData]

  def longSoFar(action: Signal[ActiveActionData]): Signal[Long] =
    action.map(_.microSoFar / 1_000_000L)

  def microLeft(action: Signal[ActiveActionData]): Signal[Long] =
    action.map(action => action.data.baseTimeMicro - action.microSoFar)

  def progressRatio(action: Signal[ActiveActionData]): Signal[Double] =
    action.map(action => action.microSoFar.toDouble / action.data.baseTimeMicro.toDouble)

  def areaIsValid(state: GameState, data: ActionData): Boolean =
    data
      .area(state)
      .contains(
        state.currentAction.flatMap(_.data.moveToArea).getOrElse(state.characterArea)
      )

  def isInvalid(state: GameState, data: ActionData): Boolean =
    data.invalidReason(state).nonEmpty || !areaIsValid(state, data)

}

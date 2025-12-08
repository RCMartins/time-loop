package pt.rcmartins.loop.model

import com.raquo.laminar.api.L.Signal

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

  @inline
  def areaIsValid(state: GameState): Boolean =
    ActiveActionData.areaIsValid(state, data)

  @inline
  def isInvalid(state: GameState): Boolean =
    ActiveActionData.isInvalid(state, data)

}

object ActiveActionData {

  def longSoFar(action: Signal[ActiveActionData]): Signal[Long] =
    action.map(_.microSoFar / 1_000_000L)

  def microLeft(action: Signal[ActiveActionData]): Signal[Long] =
    action.map(action => action.data.baseTimeMicro - action.microSoFar)

  def progressRatio(action: Signal[ActiveActionData]): Signal[Double] =
    action.map(action => action.microSoFar.toDouble / action.data.baseTimeMicro.toDouble)

  def areaIsValid(state: GameState, data: ActionData): Boolean =
    data.area.contains(
      state.currentAction.flatMap(_.data.moveToArea).getOrElse(state.characterArea)
    )

  def isInvalid(state: GameState, data: ActionData): Boolean =
    data.invalidReason(state).nonEmpty || !areaIsValid(state, data)

}

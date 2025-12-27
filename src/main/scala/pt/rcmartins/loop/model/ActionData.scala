package pt.rcmartins.loop.model

import pt.rcmartins.loop.data.StoryActions
import zio.json.{JsonDecoder, JsonEncoder}

final case class ActionData(
    actionDataType: ActionDataType,
    area: GameState => Seq[CharacterArea],
    title: String,
    effectLabel: EffectLabel,
    kind: ActionKind,
    actionTime: ActionTime,
    actionSuccessType: ActionSuccessType = ActionSuccessType.Always,
    initialAmountOfActions: AmountOfActions = AmountOfActions.Standard(1),
    forceMaxAmountOfActionsIs1: Boolean = false,
    firstTimeUnlocksActions: PartialFunction[GameState, Seq[ActionData]] = PartialFunction.empty,
    everyTimeUnlocksActions: PartialFunction[(GameState, Int), Seq[ActionData]] =
      PartialFunction.empty,
    addStory: PartialFunction[(GameState, Int), Option[StoryLine]] = PartialFunction.empty,
    invalidReason: GameState => Option[ReasonLabel] = _ => None,
    showWhenInvalid: Boolean = true,
    changeInventory: InventoryState => InventoryState = identity,
    permanentBonusUnlocks: Seq[PermanentBonusUnlockType] = Seq.empty,
    moveToArea: Option[CharacterArea] = None,
    difficultyModifier: ActionDifficultyModifier = ActionDifficultyModifier.empty,
) {

  def toActiveAction: ActiveActionData =
    new ActiveActionData(
      data = this,
      microSoFar = 0L,
      targetTimeMicro = actionTime match {
        case ActionTime.Standard(baseTimeSec)      => baseTimeSec * 1_000_000L
        case ActionTime.LinearTime(baseTimeSec, _) => baseTimeSec * 1_000_000L
        case ActionTime.ReduzedXP(baseTimeSec, _)  => baseTimeSec * 1_000_000L
      },
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

object ActionData {

  implicit val decoder: JsonDecoder[ActionData] =
    ActionDataType.decoder.mapOrFail { actionDataType =>
      StoryActions.allActions(actionDataType.id) match {
        case Some(data) => Right(data)
        case None       => Left(s"Unknown DataAction: ${actionDataType.id}")
      }
    }

  implicit val encoder: JsonEncoder[ActionData] =
    JsonEncoder.long.contramap[ActionData](_.actionDataType.id.id)

}

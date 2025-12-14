package pt.rcmartins.loop.model

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
    firstTimeUnlocksActions: GameState => Seq[ActionData] = _ => Seq.empty,
    everyTimeUnlocksActions: (GameState, Int) => Seq[ActionData] = (_, _) => Seq.empty,
    addStory: GameState => Option[StoryLine] = _ => None,
    invalidReason: GameState => Option[ReasonLabel] = _ => None,
    showWhenInvalid: Boolean = true,
    changeInventory: InventoryState => InventoryState = identity,
    permanentBonusUnlocks: Seq[PermanentBonusUnlockType] = Seq.empty,
    moveToArea: Option[CharacterArea] = None,
    difficultyModifier: ActionDifficultyModifier = ActionDifficultyModifier.empty,
) {

  val baseTimeMicro: Long = actionTime.baseTimeSec * 1_000_000L

  def toActiveAction: ActiveActionData =
    new ActiveActionData(
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

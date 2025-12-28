package pt.rcmartins.loop.data

import pt.rcmartins.loop.model._

object ActionsUtils {

  def pickupToItem(
      actionDataType: ActionDataType,
      area: GameState => Seq[CharacterArea],
      itemType: ItemType,
      amount: Int,
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions = AmountOfActions.Standard(1),
      firstTimeUnlocksActions: PartialFunction[GameState, Seq[ActionData]] = PartialFunction.empty,
      everyTimeUnlocksActions: PartialFunction[(GameState, Int), Seq[ActionData]] =
        PartialFunction.empty,
      addStory: PartialFunction[(GameState, Int), Option[StoryLine]] = PartialFunction.empty,
  ): ActionData = ActionData(
    actionDataType = actionDataType,
    area = area,
    title = s"Pick up ${itemType.amountFormat(amount)} ${itemType.name}",
    effectLabel = EffectLabel.GetItem(itemType, amount),
    kind = ActionKind.Foraging,
    actionTime = actionTime,
    initialAmountOfActions = initialAmountOfActions,
    changeInventory = _.addItem(itemType, amount),
    invalidReason = state =>
      Option.unless(state.inventory.canAddItem(itemType, amount))(ReasonLabel.InventoryFull),
    firstTimeUnlocksActions = firstTimeUnlocksActions,
    everyTimeUnlocksActions = everyTimeUnlocksActions,
    addStory = addStory,
  )

  def gardeningAction(
      actionDataType: ActionDataType,
      area: GameState => Seq[CharacterArea],
      itemType: ItemType,
      amount: Int,
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions = AmountOfActions.Standard(1),
  ): ActionData = ActionData(
    actionDataType = actionDataType,
    area = area,
    title = s"Pick up ${amount} ${itemType.name}",
    effectLabel = EffectLabel.GetItem(itemType, amount),
    kind = ActionKind.Gardening,
    actionTime = actionTime,
    initialAmountOfActions = initialAmountOfActions,
    changeInventory = _.addItem(itemType, amount),
    invalidReason = state =>
      Option.unless(state.inventory.canAddItem(itemType, amount))(ReasonLabel.InventoryFull),
  )

  def buyItemAction(
      actionDataType: ActionDataType,
      area: GameState => Seq[CharacterArea],
      itemType: ItemType,
      amount: Int,
      cost: Int,
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions,
      firstTimeUnlocksActions: PartialFunction[GameState, Seq[ActionData]] = PartialFunction.empty,
      addStory: PartialFunction[(GameState, Int), Option[StoryLine]] = PartialFunction.empty,
      changeInventoryExtra: InventoryState => InventoryState = identity,
  ): ActionData =
    ActionData(
      actionDataType = actionDataType,
      area = area,
      title = s"Buy ${amount} ${itemType.name}",
      effectLabel = EffectLabel.BuyItem(itemType, amount, cost),
      kind = ActionKind.Social,
      actionTime = actionTime,
      initialAmountOfActions = initialAmountOfActions,
      changeInventory = inventory =>
        changeInventoryExtra(inventory.addItem(itemType, amount).removeItem(ItemType.Coins, cost)),
      invalidReason = state =>
        Option.unless(
          state.inventory.canRemoveItem(ItemType.Coins, cost) &&
            state.inventory.canAddItem(itemType, amount)
        )(
          ReasonLabel.NotEnoughCoins
        ),
      firstTimeUnlocksActions = firstTimeUnlocksActions,
      addStory = addStory,
    )

  def sellItemAction(
      actionDataType: ActionDataType,
      area: GameState => Seq[CharacterArea],
      itemType: ItemType,
      amount: Int,
      coinsGain: Int,
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions,
      firstTimeUnlocksActions: PartialFunction[GameState, Seq[ActionData]] = PartialFunction.empty,
      changeInventoryExtra: InventoryState => InventoryState = identity,
  ): ActionData =
    ActionData(
      actionDataType = actionDataType,
      area = area,
      title = s"Sell ${amount} ${itemType.name}",
      effectLabel = EffectLabel.SellItem(itemType, amount, coinsGain),
      kind = ActionKind.Social,
      actionTime = actionTime,
      initialAmountOfActions = initialAmountOfActions,
      changeInventory = inventory =>
        changeInventoryExtra(
          inventory.removeItem(itemType, amount).addItem(ItemType.Coins, coinsGain)
        ),
      invalidReason = state =>
        Option.unless(state.inventory.canRemoveItem(itemType, amount))(
          ReasonLabel.NotEnoughResources
        ),
      firstTimeUnlocksActions = firstTimeUnlocksActions,
    )

  def buyInventoryIncrease(
      actionDataType: ActionDataType,
      area: GameState => Seq[CharacterArea],
      name: String,
      cost: Int,
      inventoryMaxSize: Int,
      actionTime: ActionTime,
      firstTimeUnlocksActions: PartialFunction[GameState, Seq[ActionData]] = PartialFunction.empty,
  ): ActionData =
    ActionData(
      actionDataType = actionDataType,
      area = area,
      title = s"Buy $name",
      effectLabel = EffectLabel.BuyInventoryIncrease(name, cost, inventoryMaxSize),
      kind = ActionKind.Social,
      actionTime = actionTime,
      initialAmountOfActions = AmountOfActions.Standard(1),
      changeInventory =
        _.removeItem(ItemType.Coins, cost).increaseInventorySizeTo(inventoryMaxSize),
      invalidReason = state =>
        Option.unless(
          state.inventory.canRemoveItem(ItemType.Coins, cost)
        )(ReasonLabel.NotEnoughCoins),
      firstTimeUnlocksActions = firstTimeUnlocksActions,
    )

  def cookingAction(
      actionDataType: ActionDataType,
      area: GameState => Seq[CharacterArea],
      itemType: ItemType,
      amount: Int,
      cost: Seq[(ItemType, Int)],
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions,
      actionSuccessType: ActionSuccessType = ActionSuccessType.Always,
      firstTimeUnlocksActions: PartialFunction[GameState, Seq[ActionData]] = PartialFunction.empty,
      showWhenInvalid: Boolean = true,
  ): ActionData =
    ActionData(
      actionDataType = actionDataType,
      area = area,
      title = s"Cook $amount ${itemType.name}",
      effectLabel = EffectLabel.CraftItem(itemType, amount, cost),
      kind = ActionKind.Cooking,
      actionTime = actionTime,
      initialAmountOfActions = initialAmountOfActions,
      actionSuccessType = actionSuccessType,
      showWhenInvalid = showWhenInvalid,
      changeInventory = _.removeMultipleItems(cost).addItem(itemType, amount),
      invalidReason = state =>
        Option
          .unless(
            cost.forall { case (itemType, amount) =>
              state.inventory.canRemoveItem(itemType, amount)
            }
          )(ReasonLabel.NotEnoughResources)
          .orElse(
            Option.unless(
              state.inventory.canAddItem(itemType, amount)
            )(ReasonLabel.InventoryFull),
          ),
      firstTimeUnlocksActions = firstTimeUnlocksActions,
    )

  def craftItem(
      actionDataType: ActionDataType,
      area: GameState => Seq[CharacterArea],
      itemType: ItemType,
      amount: Int,
      cost: Seq[(ItemType, Int)],
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions,
      actionSuccessType: ActionSuccessType = ActionSuccessType.Always,
      firstTimeUnlocksActions: PartialFunction[GameState, Seq[ActionData]] = PartialFunction.empty,
      permanentBonusUnlocks: Seq[PermanentBonusUnlockType] = Seq.empty,
      addStory: PartialFunction[(GameState, Int), Option[StoryLine]] = PartialFunction.empty,
      showWhenInvalid: Boolean = true,
  ): ActionData =
    ActionData(
      actionDataType = actionDataType,
      area = area,
      title = s"Craft $amount ${itemType.name}",
      effectLabel = EffectLabel.CraftItem(itemType, amount, cost),
      kind = ActionKind.Crafting,
      actionTime = actionTime,
      initialAmountOfActions = initialAmountOfActions,
      actionSuccessType = actionSuccessType,
      showWhenInvalid = showWhenInvalid,
      changeInventory = _.removeMultipleItems(cost).addItem(itemType, amount),
      invalidReason = state =>
        Option
          .unless(
            cost.forall { case (itemType, amount) =>
              state.inventory.canRemoveItem(itemType, amount)
            }
          )(ReasonLabel.NotEnoughResources)
          .orElse(
            Option.unless(
              state.inventory.canAddItem(itemType, amount)
            )(ReasonLabel.InventoryFull),
          ),
      firstTimeUnlocksActions = firstTimeUnlocksActions,
      permanentBonusUnlocks = permanentBonusUnlocks,
      addStory = addStory,
    )

  def tradeItemForBag(
      title: String,
      effectLabel: EffectLabel,
      actionDataType: ActionDataType,
      area: GameState => Seq[CharacterArea],
      cost: Seq[(ItemType, Int)],
      bonus: Seq[(ItemType, Int)],
      inventoryMaxSize: Int,
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions,
      actionSuccessType: ActionSuccessType = ActionSuccessType.Always,
      firstTimeUnlocksActions: PartialFunction[GameState, Seq[ActionData]] = PartialFunction.empty,
      permanentBonusUnlocks: Seq[PermanentBonusUnlockType] = Seq.empty,
      addStory: PartialFunction[(GameState, Int), Option[StoryLine]] = PartialFunction.empty,
      showWhenInvalid: Boolean = true,
  ): ActionData =
    ActionData(
      actionDataType = actionDataType,
      area = area,
      title = title,
      effectLabel = effectLabel,
      kind = ActionKind.Social,
      actionTime = actionTime,
      initialAmountOfActions = initialAmountOfActions,
      actionSuccessType = actionSuccessType,
      showWhenInvalid = showWhenInvalid,
      changeInventory = _.increaseInventorySizeTo(inventoryMaxSize)
        .removeMultipleItems(cost)
        .addMultipleItems(bonus),
      invalidReason = state =>
        Option
          .unless(
            bonus.forall { case (itemType, amount) =>
              // Bad action design if the increase is not enough to hold the new 'bonus' items...
              state.inventory.increaseInventorySizeTo(inventoryMaxSize).canAddItem(itemType, amount)
            }
          )(ReasonLabel.InventoryFull)
          .orElse(
            Option
              .unless(
                cost.forall { case (itemType, amount) =>
                  state.inventory.canRemoveItem(itemType, amount)
                }
              )(ReasonLabel.NotEnoughResources)
          ),
      firstTimeUnlocksActions = firstTimeUnlocksActions,
      permanentBonusUnlocks = permanentBonusUnlocks,
      addStory = addStory,
    )

  implicit class IntMoneyOps(private val amount: Int) extends AnyVal {

    def euro: Int = amount * 100
    def euros: Int = amount * 100
    def cents: Int = amount

  }

}

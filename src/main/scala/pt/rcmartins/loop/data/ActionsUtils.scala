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
      firstTimeUnlocksActions: GameState => Seq[ActionData] = _ => Seq.empty,
      everyTimeUnlocksActions: (GameState, Int) => Seq[ActionData] = (_, _) => Seq.empty,
      addStory: GameState => Option[StoryLine] = _ => None,
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
      firstTimeUnlocksActions: GameState => Seq[ActionData] = _ => Seq.empty,
      addStory: GameState => Option[StoryLine] = _ => None,
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
      firstTimeUnlocksActions: GameState => Seq[ActionData] = _ => Seq.empty,
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
      firstTimeUnlocksActions: GameState => Seq[ActionData] = _ => Seq(),
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
      firstTimeUnlocksActions: GameState => Seq[ActionData] = _ => Seq.empty,
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
      changeInventory = inventory =>
        cost
          .foldLeft(inventory) { case (inv, (it, ct)) => inv.removeItem(it, ct) }
          .addItem(itemType, amount),
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
      firstTimeUnlocksActions: GameState => Seq[ActionData] = _ => Seq.empty,
      permanentBonusUnlocks: Seq[PermanentBonusUnlockType] = Seq.empty,
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
      changeInventory = inventory =>
        cost
          .foldLeft(inventory) { case (inv, (it, ct)) => inv.removeItem(it, ct) }
          .addItem(itemType, amount),
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
    )

}

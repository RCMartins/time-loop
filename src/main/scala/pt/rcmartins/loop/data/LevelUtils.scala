package pt.rcmartins.loop.data

import pt.rcmartins.loop.model._

object LevelUtils {

  def pickupToItem(
      actionDataType: ActionDataType,
      area: Option[CharacterArea],
      itemType: ItemType,
      amount: Int,
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions = AmountOfActions.Standard(1),
  ): ActionData = ActionData(
    actionDataType = actionDataType,
    area = area,
    title = s"Pick up ${amount} ${itemType.name}",
    effectLabel = EffectLabel.GetItem(itemType, amount),
    kind = ActionKind.Foraging,
    actionTime = actionTime,
    initialAmountOfActions = initialAmountOfActions,
    changeInventory = _.addItem(itemType, amount),
    invalidReason = state =>
      Option.unless(state.inventory.canAddItem(itemType, amount))(ReasonLabel.InventoryFull)
  )

  def buyItemAction(
      actionDataType: ActionDataType,
      area: Option[CharacterArea],
      itemType: ItemType,
      amount: Int,
      cost: Int,
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions,
      firstTimeUnlocksActions: Unit => Seq[ActionData] = _ => Seq.empty,
  ): ActionData =
    ActionData(
      actionDataType = actionDataType,
      area = area,
      title = s"Buy ${amount} ${itemType.name}",
      effectLabel = EffectLabel.BuyItem(itemType, amount, cost),
      kind = ActionKind.Social,
      actionTime = actionTime,
      initialAmountOfActions = initialAmountOfActions,
      changeInventory = _.addItem(itemType, amount).removeItem(ItemType.Coins, cost),
      invalidReason = state =>
        Option.unless(
          state.inventory.canRemoveItem(ItemType.Coins, cost) &&
            state.inventory.canAddItem(itemType, amount)
        )(
          ReasonLabel.NotEnoughCoins
        ),
      firstTimeUnlocksActions = firstTimeUnlocksActions,
    )

  def craftItem(
      actionDataType: ActionDataType,
      area: Option[CharacterArea],
      itemType: ItemType,
      amount: Int,
      cost: Seq[(ItemType, Int)],
      actionTime: ActionTime,
      initialAmountOfActions: AmountOfActions,
      actionSuccessType: ActionSuccessType = ActionSuccessType.Always,
      firstTimeUnlocksActions: Unit => Seq[ActionData] = _ => Seq.empty,
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
        Option.unless(
          cost.forall { case (itemType, amount) =>
            state.inventory.canRemoveItem(itemType, amount)
          } &&
            state.inventory.canAddItem(itemType, amount)
        )(ReasonLabel.NotEnoughResources),
      firstTimeUnlocksActions = firstTimeUnlocksActions,
    )

}

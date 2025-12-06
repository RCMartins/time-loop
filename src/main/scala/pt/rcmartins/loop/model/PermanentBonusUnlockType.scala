package pt.rcmartins.loop.model

sealed trait PermanentBonusUnlockType

object PermanentBonusUnlockType {

  case class ProgressiveActionCount(bonus: PermanentBonus, baseValue: Int, multiplier: Int)
      extends PermanentBonusUnlockType

}

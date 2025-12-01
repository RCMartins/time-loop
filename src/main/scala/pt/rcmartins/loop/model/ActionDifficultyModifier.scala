package pt.rcmartins.loop.model

case class ActionDifficultyModifier(
    increaseTirednessAbsoluteMicro: Long,
)

object ActionDifficultyModifier {

  val empty: ActionDifficultyModifier =
    ActionDifficultyModifier(
      increaseTirednessAbsoluteMicro = 0
    )

}

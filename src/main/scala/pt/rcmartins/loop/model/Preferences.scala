package pt.rcmartins.loop.model

case class Preferences(
    speedMultiplier: Int,
    lastUIMultSelected: Map[ActionDataType, Int],
)

object Preferences {

  val initial: Preferences = Preferences(
    speedMultiplier = 1,
    lastUIMultSelected = Map.empty,
  )

}

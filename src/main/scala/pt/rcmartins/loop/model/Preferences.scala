package pt.rcmartins.loop.model

case class Preferences(
    lastUIMultSelected: Map[ActionDataType, Int],
)

object Preferences {

  val initial: Preferences = Preferences(
    lastUIMultSelected = Map.empty,
  )

}

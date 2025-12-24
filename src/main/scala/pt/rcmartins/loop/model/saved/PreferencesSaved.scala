package pt.rcmartins.loop.model.saved

import pt.rcmartins.loop.model.{ActionDataType, Preferences}
import zio.json._

case class PreferencesSaved(
    lastUIMultSelected: Seq[(ActionDataType, Int)],
) {

  def toPreferences: Preferences =
    Preferences(
      lastUIMultSelected = lastUIMultSelected.toMap,
    )

}

object PreferencesSaved {

  implicit val decoder: JsonDecoder[PreferencesSaved] =
    DeriveJsonDecoder.gen[PreferencesSaved]

  implicit val encoder: JsonEncoder[PreferencesSaved] =
    DeriveJsonEncoder.gen[PreferencesSaved]

  def fromPreferences(preferences: Preferences): PreferencesSaved =
    PreferencesSaved(
      lastUIMultSelected = preferences.lastUIMultSelected.toSeq,
    )

}

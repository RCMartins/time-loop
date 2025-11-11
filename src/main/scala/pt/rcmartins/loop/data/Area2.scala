package pt.rcmartins.loop.data

import pt.rcmartins.loop.model.ActionDataType.Area2DataType
import pt.rcmartins.loop.model.{ActionData, ActionKind, EffectLabel}

object Area2 {

  object Data {

    def ExploreTown: ActionData = ActionData(
      actionDataType = Area2DataType.ExploreTown,
      title = "Explore the Town",
      effectLabel = EffectLabel.Movement,
      kind = ActionKind.Agility,
      baseTimeSec = 1000,
    )

  }

}

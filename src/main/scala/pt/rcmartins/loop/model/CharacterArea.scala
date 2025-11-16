package pt.rcmartins.loop.model

sealed trait CharacterArea

object CharacterArea {

  case object Area1_House extends CharacterArea
  case object Area2_Town extends CharacterArea
  case object Area3_Store extends CharacterArea
  case object Area4_EquipmentStore extends CharacterArea
  case object Area5_Forest extends CharacterArea

}

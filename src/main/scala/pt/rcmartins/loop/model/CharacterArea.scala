package pt.rcmartins.loop.model

sealed trait CharacterArea {

  def name: String

}

object CharacterArea {

  case object Area1_Home extends CharacterArea { val name: String = "Home" }
  case object Area2_Town extends CharacterArea { val name: String = "Town" }
  case object Area3_Store extends CharacterArea { val name: String = "General Store" }
  case object Area4_EquipmentStore extends CharacterArea { val name: String = "Equipment Store" }
  case object Area5_Forest extends CharacterArea { val name: String = "Forest" }
  case object Area6_My_Soap_Shop extends CharacterArea { val name: String = "My Soap Store" }

}

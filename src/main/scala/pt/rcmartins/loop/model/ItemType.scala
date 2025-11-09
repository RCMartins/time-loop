package pt.rcmartins.loop.model

sealed trait ItemType {
  val name: String
}

object ItemType {

  case object Rice extends ItemType { val name: String = "Rice" } // 5
  case object Momo extends ItemType { val name: String = "Momo" } // 8
  case object Curry extends ItemType { val name: String = "Curry" } // 12
  case object Chatpate extends ItemType { val name: String = "Chatpate" } // 15
  case object Panipuri extends ItemType { val name: String = "Panipuri" } // 20

}

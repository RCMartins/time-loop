package pt.rcmartins.loop.model

sealed trait Dir8 {

  val dir: Int

  def oppositeDir: Int = (dir + 4) % 8

}

object Dir8 {

  case object Top extends Dir8 { val dir = 0 }
  case object TopRight extends Dir8 { val dir = 1 }
  case object Right extends Dir8 { val dir = 2 }
  case object BottomRight extends Dir8 { val dir = 3 }
  case object Bottom extends Dir8 { val dir = 4 }
  case object BottomLeft extends Dir8 { val dir = 5 }
  case object Left extends Dir8 { val dir = 6 }
  case object TopLeft extends Dir8 { val dir = 7 }

}

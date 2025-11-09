package pt.rcmartins.loop.model

trait ActionDataType {}

object ActionDataType {

  case object Bug extends ActionDataType

  trait Area1DataType extends ActionDataType

  trait Area2DataType extends ActionDataType

}

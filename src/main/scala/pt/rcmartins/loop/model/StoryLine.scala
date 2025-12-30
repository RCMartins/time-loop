package pt.rcmartins.loop.model

import pt.rcmartins.loop.model.StoryLine.StoryPart
import pt.rcmartins.loop.model.StoryLine.StoryPart._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class StoryLine(seq: Seq[StoryPart]) {

  def join(otherStoryPart: StoryPart): StoryLine =
    copy(seq = seq :+ otherStoryPart)

}

object StoryLine {

  sealed trait StoryPart

  object StoryPart {

    case class StoryTextLine(text: String) extends StoryPart

    case class ForceAction(actionData: ActionData) extends StoryPart

    implicit val decoder: JsonDecoder[StoryPart] = DeriveJsonDecoder.gen[StoryPart]
    implicit val encoder: JsonEncoder[StoryPart] = DeriveJsonEncoder.gen[StoryPart]

  }

  def join(storyPart1: StoryPart, storyPart2: StoryPart): StoryLine =
    StoryLine(Seq(storyPart1, storyPart2))

  def simple(text: String): StoryLine = StoryLine(Seq(StoryTextLine(text)))

  implicit val decoder: JsonDecoder[StoryLine] = DeriveJsonDecoder.gen[StoryLine]
  implicit val encoder: JsonEncoder[StoryLine] = DeriveJsonEncoder.gen[StoryLine]

}

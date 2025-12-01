package pt.rcmartins.loop.model

import pt.rcmartins.loop.model.StoryLine.StoryPart
import pt.rcmartins.loop.model.StoryLine.StoryPart._

case class StoryLine(seq: Seq[StoryPart]) {

  def join(otherStoryPart: StoryPart): StoryLine =
    copy(seq = seq :+ otherStoryPart)

}

object StoryLine {

  sealed trait StoryPart

  object StoryPart {

    case class StoryTextLine(text: String) extends StoryPart

    case class ForceAction(actionData: ActionData) extends StoryPart

  }

  def join(storyPart1: StoryPart, storyPart2: StoryPart): StoryLine =
    StoryLine(Seq(storyPart1, storyPart2))

  def simple(text: String): StoryLine = StoryLine(Seq(StoryTextLine(text)))

}

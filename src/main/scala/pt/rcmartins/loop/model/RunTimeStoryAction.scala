package pt.rcmartins.loop.model

import pt.rcmartins.loop.model.StoryLine.StoryPart
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class RunTimeStoryAction(
    storyPart: StoryPart,
    requiredElapsedTimeMicro: Long,
)

object RunTimeStoryAction {

  implicit val decoder: JsonDecoder[RunTimeStoryAction] = DeriveJsonDecoder.gen[RunTimeStoryAction]
  implicit val encoder: JsonEncoder[RunTimeStoryAction] = DeriveJsonEncoder.gen[RunTimeStoryAction]

}

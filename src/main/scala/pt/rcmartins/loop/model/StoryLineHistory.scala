package pt.rcmartins.loop.model

import scala.util.Random

case class StoryLineHistory(
    id: Long,
    line: String,
)

object StoryLineHistory {

  def apply(line: String): StoryLineHistory =
    StoryLineHistory(Random.nextLong(), line)

}

package pt.rcmartins.loop.model

import pt.rcmartins.loop.model.StoryLine.StoryPart

case class RunTimeStoryAction(
    storyPart: StoryPart,
    requiredElapsedTimeMicro: Long,
)

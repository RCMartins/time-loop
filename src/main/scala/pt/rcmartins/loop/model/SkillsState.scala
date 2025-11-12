package pt.rcmartins.loop.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class SkillsState(
    agility: SkillState, // movement, etc.
    explore: SkillState, // exploration, etc.
    foraging: SkillState, // gathering resources, etc.
    social: SkillState, // communication, persuasion, etc.
    crafting: SkillState, // building, repairing, etc.
    gardening: SkillState, // plant care,  etc.
    cooking: SkillState, // food preparation, etc.
    magic: SkillState, // supernatural abilities, etc.
) {

  def allSkillsSeq: Seq[SkillState] = Seq(
    agility,
    explore,
    foraging,
    social,
    crafting,
    gardening,
    cooking,
    magic,
  )

  def allHigherThan0: Seq[SkillState] =
    allSkillsSeq.filter(_.permXPMicro > 0)

  def get(kind: ActionKind): SkillState =
    kind match {
      case ActionKind.Agility   => agility
      case ActionKind.Exploring => explore
      case ActionKind.Foraging  => foraging
      case ActionKind.Social    => social
      case ActionKind.Crafting  => crafting
      case ActionKind.Gardening => gardening
      case ActionKind.Cooking   => cooking
      case ActionKind.Magic     => magic
    }

  def update(kind: ActionKind, function: SkillState => SkillState): SkillsState =
    kind match {
      case ActionKind.Agility   => copy(agility = function(agility))
      case ActionKind.Exploring => copy(explore = function(explore))
      case ActionKind.Foraging  => copy(foraging = function(foraging))
      case ActionKind.Social    => copy(social = function(social))
      case ActionKind.Crafting  => copy(crafting = function(crafting))
      case ActionKind.Gardening => copy(gardening = function(gardening))
      case ActionKind.Cooking   => copy(cooking = function(cooking))
      case ActionKind.Magic     => copy(magic = function(magic))
    }

  def resetLoopProgress: SkillsState =
    SkillsState(
      agility = agility.resetLoopProgress,
      explore = explore.resetLoopProgress,
      foraging = foraging.resetLoopProgress,
      social = social.resetLoopProgress,
      crafting = crafting.resetLoopProgress,
      gardening = gardening.resetLoopProgress,
      cooking = cooking.resetLoopProgress,
      magic = magic.resetLoopProgress,
    )

}

object SkillsState {

  val initial: SkillsState = SkillsState(
    agility = SkillState.initial(ActionKind.Agility),
    explore = SkillState.initial(ActionKind.Exploring),
    cooking = SkillState.initial(ActionKind.Cooking),
    crafting = SkillState.initial(ActionKind.Crafting),
    gardening = SkillState.initial(ActionKind.Gardening),
    foraging = SkillState.initial(ActionKind.Foraging),
    social = SkillState.initial(ActionKind.Social),
    magic = SkillState.initial(ActionKind.Magic),
  )

  implicit val decoder: JsonDecoder[SkillsState] = DeriveJsonDecoder.gen[SkillsState]
  implicit val encoder: JsonEncoder[SkillsState] = DeriveJsonEncoder.gen[SkillsState]

}

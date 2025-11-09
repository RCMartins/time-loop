package pt.rcmartins.loop.model

case class SkillsState(
    agility: SkillState, // movement, etc.
    explore: SkillState, // exploration, etc.
    cooking: SkillState, // food preparation, etc.
    crafting: SkillState, // building, repairing, etc.
    gardening: SkillState, // plant care,  etc.
    foraging: SkillState, // gathering resources, etc.
    social: SkillState, // communication, persuasion, etc.
    magic: SkillState, // supernatural abilities, etc.
) {

  val allSeq: Seq[SkillState] = Seq(
    agility,
    explore,
    cooking,
    crafting,
    gardening,
    foraging,
    social,
    magic,
  )

  val allHigherThan0: Seq[SkillState] =
    allSeq.filter(_.permXPMicro > 0)

  def get(kind: ActionKind): SkillState =
    kind match {
      case ActionKind.Agility   => agility
      case ActionKind.Exploring => explore
      case ActionKind.Cooking   => cooking
      case ActionKind.Crafting  => crafting
      case ActionKind.Gardening => gardening
      case ActionKind.Foraging  => foraging
      case ActionKind.Social    => social
      case ActionKind.Magic     => magic
    }

  def update(kind: ActionKind, function: SkillState => SkillState): SkillsState =
    kind match {
      case ActionKind.Agility   => copy(agility = function(agility))
      case ActionKind.Exploring => copy(explore = function(explore))
      case ActionKind.Cooking   => copy(cooking = function(cooking))
      case ActionKind.Crafting  => copy(crafting = function(crafting))
      case ActionKind.Gardening => copy(gardening = function(gardening))
      case ActionKind.Foraging  => copy(foraging = function(foraging))
      case ActionKind.Social    => copy(social = function(social))
      case ActionKind.Magic     => copy(magic = function(magic))
    }

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

}

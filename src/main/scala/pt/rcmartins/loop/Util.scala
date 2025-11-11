package pt.rcmartins.loop

import com.raquo.laminar.api.L._
import pt.rcmartins.loop.GameData.selectedNextAction
import pt.rcmartins.loop.model.{ActionKind, ActiveActionData, SkillState}

object Util {

  private val owner = new Owner {}

  def activeActionCard(vm: ActiveActionData): HtmlElement = {
    val base =
      "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow transition " +
        "hover:ring-emerald-400/60 hover:shadow-md focus:outline-none " +
        "focus:ring-2 focus:ring-emerald-400 pointer-events-none shadow-lg"

    div(
      role := "button",
      cls := base,
      tabIndex := 0,

      // Content
      div(
        cls := "flex items-start gap-3",
        // Icon + kind accent
        div(
          cls := "mt-0.5 shrink-0 rounded-xl bg-slate-700/60 ring-1 ring-slate-600 p-2",
          actionIcon(vm.data.kind)
        ),

        // Title + subtitle + badges
        div(
          cls := "min-w-0 flex-1",
          div(
            cls := "flex items-start justify-between gap-3",
            div(
              h3(cls := "text-base font-semibold tracking-tight", vm.data.title),
              p(cls := "text-xs text-slate-300/90", vm.data.effectLabel.label)
            ),
          ),

          // Badges row
          div(
            cls := "mt-2 flex flex-wrap items-center gap-2",
            span(
              cls := "px-2 py-0.5 text-xs rounded-full bg-slate-700/70 ring-1 ring-slate-600",
              child.text <-- vm.longSoFar.map { timeSoFar =>
                s"\u00A0$timeSoFar\u00A0 / \u00A0${vm.data.baseTimeSec}\u00A0"
              },
            ),
            span(
              cls := "px-2 py-0.5 text-xs rounded-full bg-slate-900/70 ring-1 ring-slate-600",
              child.text <-- vm.microLeft.withCurrentValueOf(GameData.skills).map {
                case (timeSoFar, skills) =>
                  val currentTime: Double =
                    calcWithSkillDouble(timeSoFar, skills.get(vm.data.kind)) / 1_000_000L
                  f"$currentTime%.1f s"
              },
            ),
          ),
          div(
            cls := "mt-3",
            div(
              cls := "h-1.5 rounded-full bg-slate-700/60 overflow-hidden",
              div(
                // origin-left ensures scaleX grows from the left edge
                cls := "h-1.5 rounded-full bg-emerald-500 origin-left will-change-transform",

                // reactive transform: scaleX(progressRatio)
                transform <-- vm.progressRatio.map { ratio =>
                  val clamped = ratio.max(0.0).min(1.0)
                  s"scaleX($clamped)"
                }
              )
            )
          )
        )
      )
    )
  }

  private def calcWithSkillBonus(baseTime: Long, state: SkillState): Long = {
    Math.ceil(baseTime.toDouble / state.finalSpeedMulti).toLong
  }

  private def calcWithSkillDouble(baseTime: Long, state: SkillState): Double =
    baseTime.toDouble / state.finalSpeedMulti

  def actionCard(vm: Signal[ActiveActionData]): HtmlElement = {
    val isSelected = Var(false)

    val base =
      "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow transition " +
        "hover:ring-emerald-400/60 hover:shadow-md focus:outline-none " +
        "focus:ring-2 focus:ring-emerald-400 m-1 mt-4"

    val selectedCls =
      " ring-2 ring-emerald-500 shadow-lg"

    val disabledCls =
      " opacity-50 grayscale pointer-events-none"

    val isDisabled =
      vm.map(_.data.invalidReason).combineWith(GameData.gameState).map {
        case (invalidReasonF, gameState) =>
          invalidReasonF(gameState).isDefined
      }

    isSelected.signal
      .withCurrentValueOf(vm, isDisabled)
      .foreach {
        case (true, action, false) =>
          println(s"Action selected: ${action.data.title}")
          GameData.selectNextAction(action.id)
        case _ =>
      }(owner)

    val tooltipText =
      vm.map(_.data.invalidReason).combineWith(GameData.gameState).map {
        case (invalidReasonF, gameState) =>
          invalidReasonF(gameState).map(_.label).getOrElse("")
      }

    div(
      role := "button",
      tabIndex := 0,
      cls := base,
      cls(selectedCls) <-- selectedNextAction.combineWith(vm.map(_.id)).map {
        case (optId, actionId) => optId.contains(actionId)
      },
      cls(disabledCls) <-- isDisabled,
//      disabled <-- isDisabled,

//      // Interactions: click / Enter / Space toggle select and fire onSelect
      onClick --> { _ =>
        isSelected.set(true)
      },

      // Content
      div(
        cls := "relative inline-block",
        cls := "flex items-start gap-3",
        // Icon + kind accent
        div(
          cls := "mt-0.5 shrink-0 rounded-xl bg-slate-700/60 ring-1 ring-slate-600 p-2",
          child <-- vm.map(action => actionIcon(action.data.kind))
        ),

        // Title + subtitle + badges
        div(
          cls := "min-w-0 flex-1",
          div(
            cls := "flex items-start justify-between gap-3",
            div(
              h3(
                cls := "text-base font-semibold tracking-tight",
                child.text <-- vm.map(_.data.title)
              ),
              p(cls := "text-xs text-slate-300/90", child.text <-- vm.map(_.data.effectLabel.label))
            ),
          ),

          // Badges row
          div(
            cls := "mt-2 flex flex-wrap items-center gap-2",
            span(
              cls := "px-2 py-0.5 text-xs rounded-full bg-slate-700/70 ring-1 ring-slate-600",
              "\u00A0",
              child.text <-- vm.map(_.data.baseTimeSec.toString),
              "\u00A0",
            ),
          ),
        ),

        // Tooltip — visibility controlled by Signal
        div(
          cls := "absolute left-1/2 -translate-x-1/2 bottom-full mb-2 px-2 py-1 " +
            "text-xs text-slate-100 bg-slate-700 rounded-md whitespace-nowrap shadow-lg " +
            "transition-opacity duration-150 opacity-0 pointer-events-none",
          child.text <-- tooltipText,
          // Toggle visibility via opacity
          cls("opacity-100") <-- isDisabled
        )
      )
    )
  }

  private def skillAccent(kind: ActionKind, darker: Boolean): String =
    (kind match {
      case ActionKind.Agility   => "bg-emerald-"
      case ActionKind.Exploring => "bg-indigo-"
      case ActionKind.Foraging  => "bg-lime-"
      case ActionKind.Social    => "bg-pink-"
      case ActionKind.Crafting  => "bg-sky-"
      case ActionKind.Gardening => "bg-green-"
      case ActionKind.Cooking   => "bg-amber-"
      case ActionKind.Magic     => "bg-purple-"
    }) + (if (darker) "700" else "400")

  def skillRow(skillSig: Signal[SkillState]): HtmlElement =
    div(
      cls := "rounded-xl p-3 bg-slate-800/60 ring-1 ring-slate-700 flex items-center gap-3",
      // Dot accent
      div(
        cls := "h-2.5 w-2.5 rounded-full",
        cls <-- skillSig.map { sk => skillAccent(sk.kind, darker = false) },
      ),
      // Title + level
      div(
        cls := "min-w-0 flex-1",
        div(
          cls := "flex items-center justify-between gap-3",
          span(
            cls := "text-sm font-semibold",
            child <-- skillSig.map(s => actionIcon(s.kind)),
            " ",
            child.text <-- skillSig.map(_.kind.name),
          ),
          span(cls := "text-xs text-slate-300/80", "Lv ", child.text <-- skillSig.map(_.loopLevel))
        ),
        div(
          cls := "mt-1",
          div(
            cls := "h-1.5 rounded-full bg-slate-700/60 overflow-hidden",
            div(
              // grow from the left, GPU-friendly
              cls := "h-1.5 rounded-full origin-left will-change-transform",
              cls <-- skillSig.map(sk => skillAccent(sk.kind, darker = false)),

              // scale instead of width; clamp for safety and keep a tiny visible bar
              transform <-- skillSig.map { sk =>
                val r = sk.loopRatio.max(0.0).min(1.0)
                val visible = math.max(0.02, r) // ensures it never fully disappears
                s"scaleX($visible)"
              }
            )
          ),
          div(
            cls := "mt-1 text-[11px] text-slate-300/80",
            child.text <-- skillSig.map(sk => s"${sk.loopXPLong} / ${sk.nextLoopXP} XP")
          )
        ),
        div(
          cls := "mt-1",
          div(
            cls := "h-1.5 rounded-full bg-slate-700/60 overflow-hidden",
            div(
              // grow from the left, GPU-friendly
              cls := "h-1.5 rounded-full origin-left will-change-transform",
              cls <-- skillSig.map(sk => skillAccent(sk.kind, darker = true)),

              // scale instead of width; clamp for safety and keep a tiny visible bar
              transform <-- skillSig.map { sk =>
                val r = sk.permRatio.max(0.0).min(1.0)
                val visible = math.max(0.02, r) // ensures it never fully disappears
                s"scaleX($visible)"
              }
            )
          ),
          div(
            cls := "mt-1 text-[11px] text-slate-300/80",
            child.text <-- skillSig.map(sk => s"${sk.permXPLong} / ${sk.nextPermXP} XP")
          )
        )
      )
    )

  private def actionIcon(kind: ActionKind): Element =
    kind match {
      case ActionKind.Agility =>
        i(
          cls := "fa-solid fa-person-running h-5 w-5 opacity-90"
        )
      case ActionKind.Exploring =>
        i(
          cls := "fa-solid fa-magnifying-glass h-5 w-5 opacity-90"
        )
      case ActionKind.Foraging =>
        i(
          cls := "fa-solid fa-apple-whole h-5 w-5 opacity-90"
        )
      case ActionKind.Social =>
        i(
          cls := "fa-solid fa-people-arrows h-5 w-5 opacity-90"
        )
      case ActionKind.Crafting =>
        i(
          cls := "fa-solid fa-scissors h-5 w-5 opacity-90"
        )
      case ActionKind.Gardening =>
        i(
          cls := "fa-solid fa-seedling h-5 w-5 opacity-90"
        )
      case ActionKind.Cooking =>
        i(
          cls := "fa-solid fa-utensils h-5 w-5 opacity-90"
        )
      case ActionKind.Magic =>
        i(
          cls := "fa-solid fa-magic-wand-sparkles h-5 w-5 opacity-90"
        )
      case _ =>
        i(
          cls := "fa-solid fa-question h-5 w-5 opacity-90"
        )
    }

}

package pt.rcmartins.loop

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLDivElement
import pt.rcmartins.loop.model._

import scala.annotation.tailrec

object Util {

  private val owner = new Owner {}

  private val baseCardClasses: String =
    "rounded-2xl p-4 pb-5 bg-slate-800/60 ring-1 ring-slate-700 shadow transition "

  private val baseCardHoverClasses: String =
    "hover:ring-emerald-400/60 hover:shadow-md focus:outline-none " +
      "focus:ring-2 focus:ring-emerald-400"

  def activeActionCard(
      vm: Signal[ActiveActionData],
      skills: Signal[SkillsState],
  ): HtmlElement = {
    val data: Signal[ActionData] = vm.distinctBy(_.id).map(_.data)
    val longSoFar: Signal[Long] = ActiveActionData.longSoFar(vm).distinct
    val microLeft: Signal[Long] = ActiveActionData.microLeft(vm).distinct
    val progressRatio: Signal[Double] = ActiveActionData.progressRatio(vm).distinct
    val numberOfActionsLeftSignal: Signal[AmountOfActions] = vm.map(_.amountOfActionsLeft)

    div(
      cls := baseCardClasses,
      cls := "shadow-lg",
      tabIndex := 0,

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
              child.text <-- longSoFar.combineWith(data).map { case (timeSoFar, data) =>
                s"\u00A0$timeSoFar\u00A0 / \u00A0${data.actionTime.baseTimeSec}\u00A0"
              },
            ),
            span(
              cls := "px-2 py-0.5 text-xs rounded-full bg-slate-900/70 ring-1 ring-slate-600",
              child.text <-- microLeft.combineWith(data, skills).map {
                case (timeSoFar, data, skills) =>
                  val currentTime: Double =
                    calcWithSkillDouble(timeSoFar, skills.get(data.kind)) / 1_000_000L
                  f"$currentTime%.1f s"
              },
            ),
            child.maybe <--
              data.map(_.actionSuccessType).distinct.map {
                case ActionSuccessType.WithFailure(baseChance, increase) =>
                  Some(
                    div(
                      cls := "relative group",
                      span(
                        cls := "px-2 py-0.5 text-xs rounded-full bg-slate-700/70 ring-1 ring-slate-600",
                        "\u00A0",
                        child.text <--
                          vm.map(_.currentActionSuccessChance).map { currentActionSuccessChance =>
                            f"${(currentActionSuccessChance * 100).toInt}%02d%%"
                          },
                        "\u00A0",
                      ),
                      div(
                        cls := "absolute left-1/2 -translate-x-1/2 top-full mt-1 px-2 py-1 z-20 " +
                          "text-xs bg-slate-900 text-white rounded shadow-lg whitespace-nowrap " +
                          "opacity-0 group-hover:opacity-100 transition-opacity duration-150 pointer-events-none",
                        f"This action has a base ${(baseChance * 100).toInt}%d%% of success + ${(increase * 100).toInt}%d%% for every failure"
                      )
                    )
                  )
                case _ =>
                  None
              }
          ),
          div(
            cls := "mt-3 mb-1",
            div(
              cls := "h-1.5 rounded-full bg-slate-700/60 overflow-hidden",
              div(
                // origin-left ensures scaleX grows from the left edge
                cls := "h-1.5 rounded-full bg-emerald-500 origin-left will-change-transform",

                // reactive transform: scaleX(progressRatio)
                transform <-- progressRatio.map { ratio =>
                  val clamped = ratio.max(0.0).min(1.0)
                  s"scaleX($clamped)"
                }
              )
            )
          ),
        ),
        amountOfActionsTooltip(numberOfActionsLeftSignal),
      )
    )
  }

  private def calcWithSkillDouble(baseTime: Long, state: SkillState): Double =
    baseTime.toDouble / state.finalSpeedMulti

  def actionCard(
      actionSignal: Signal[ActiveActionData],
      gameData: GameData,
  ): HtmlElement = {
    val data: Signal[ActionData] = actionSignal.map(_.data).distinct
    val isSelected: Var[Boolean] = Var(false)
    val selectedLimit: Var[Int] = Var(-1)

    val selectedCls =
      " ring-2 ring-emerald-500 shadow-lg"

    val disabledCls =
      " opacity-50 grayscale pointer-events-none"

    val isDisabled: Signal[Boolean] =
      data.combineWith(gameData.gameState).map { case (actionData, gameState) =>
        ActiveActionData.isInvalid(gameState, actionData)
      }

    isSelected.signal
      .sample(isSelected, selectedLimit, actionSignal, isDisabled)
      .foreach {
        case (true, limit, action, false) =>
          gameData.selectNextAction(
            action.id,
            Option
              .when(action.data.forceMaxAmountOfActionsIs1)(1)
              .orElse(Some(limit).filter(_ != -1))
          )
        case _ =>
      }(owner)

    val invalidTooltipText: Signal[String] =
      data
        .map(_.invalidReason)
        .combineWith(gameData.gameState)
        .map { case (invalidReasonF, gameState) =>
          invalidReasonF(gameState).map(_.label).getOrElse("Different Area")
        }
        .distinct

    val numberOfActionsLeftSignal: Signal[AmountOfActions] =
      actionSignal.map(_.amountOfActionsLeft)

    val bonusIcon: Signal[Seq[(HtmlElement, String)]] = {
      @tailrec
      def calcNextMultBonus(completitions: Int, baseValue: Int, multiplier: Int): Int =
        if (completitions >= baseValue)
          calcNextMultBonus(completitions, baseValue * multiplier, multiplier)
        else
          baseValue

      data.combineWith(gameData.stats).map { case (data, stats) =>
        data.permanentBonusUnlocks.map {
          case PermanentBonusUnlockType.ProgressiveActionCount(bonus, baseValue, multiplier) =>
            val count = stats.getLoopCount(data.actionDataType)
            (
              span(
                p(
                  s"Completing this action $count/${calcNextMultBonus(count, baseValue, multiplier)}x will:"
                ),
                bonus.description.split("\n").toSeq.map(p(_))
              ),
              permanentBonusToSVGPath(bonus)
            )
        }
      }
    }

    div(
      cls := "relative",
      cls := "m-2 mt-8 me-3",
      div(
        role := "button",
        tabIndex := 0,
        cls := baseCardClasses,
        cls := "relative",
        cls(selectedCls) <-- gameData.selectedNextAction.combineWith(actionSignal.map(_.id)).map {
          case (optId, actionId) => optId.map(_._1).contains(actionId)
        },
        cls(baseCardHoverClasses) <-- isDisabled.map(!_),
        onClick --> { _ =>
          isSelected.set(true)
        },

        // Content
        div(
          cls(disabledCls) <-- isDisabled,
          cls := "relative inline-block",
          cls := "flex items-start gap-3",
          // Icon + kind accent
          div(
            cls := "mt-0.5 shrink-0 rounded-xl bg-slate-700/60 ring-1 ring-slate-600 p-2",
            child <-- actionSignal.map(action => actionIcon(action.data.kind)),
          ),

          // Title + subtitle + badges
          div(
            cls := "min-w-0 flex-1",
            div(
              cls := "flex items-start justify-between gap-3",
              div(
                h3(
                  cls := "text-base font-semibold tracking-tight",
                  child.text <-- actionSignal.map(_.data.title),
                ),
                p(
                  cls := "text-xs text-slate-300/90",
                  child.text <-- actionSignal.map(_.data.effectLabel.label),
                )
              ),
            ),

            // Badges row
            div(
              cls := "mt-2 flex flex-wrap items-center gap-2",
              span(
                cls := "px-2 py-0.5 text-xs rounded-full bg-slate-700/70 ring-1 ring-slate-600",
                "\u00A0",
                child.text <-- actionSignal.map(_.data.actionTime.baseTimeSec.toString),
                "\u00A0",
              ),
              child.maybe <--
                actionSignal.map(_.data.actionSuccessType match {
                  case ActionSuccessType.WithFailure(baseChance, increase) =>
                    Some(
                      div(
                        cls := "relative group",
                        span(
                          cls := "px-2 py-0.5 text-xs rounded-full bg-slate-700/70 ring-1 ring-slate-600",
                          "\u00A0",
                          f"${(baseChance * 100).toInt}%02d%%",
                          "\u00A0",
                        ),
                        div(
                          cls := "absolute left-1/2 -translate-x-1/2 top-full mt-1 px-2 py-1 z-20 " +
                            "text-xs bg-slate-900 text-white rounded shadow-lg whitespace-nowrap " +
                            "opacity-0 group-hover:opacity-100 transition-opacity duration-150 pointer-events-none",
                          f"This action has a base ${(baseChance * 100).toInt}%d%% of success + ${(increase * 100).toInt}%d%% for every failure"
                        )
                      )
                    )
                  case _ =>
                    None
                })
            ),
          ),

          // Invalid reason tooltip (top center)
          div(
            cls := "absolute left-1/2 -translate-x-1/2 bottom-full mb-2 px-2 py-1 " +
              "text-xs text-slate-100 bg-slate-700 rounded-md whitespace-nowrap shadow-lg " +
              "transition-opacity duration-150 opacity-0 pointer-events-none",
            child.text <-- invalidTooltipText,
            cls("opacity-100") <-- isDisabled
          ),
          amountOfActionsTooltip(numberOfActionsLeftSignal),
          selectAmountOfActionsOverlay(actionSignal, selectedLimit),
        ),
      ),
      children <--
        bonusIcon.map {
          _.map { case (tooltipDescription, iconPath) =>
            div(
              cls := "absolute -top-8 right-2",
              div(
                // inline-block so the wrapper is just the icon's size
                cls := "relative inline-block group",
                div(
                  cls := "inline-block bg-blue-500 rounded w-7 h-7",
                  Constants.Icons.CreateIconElement(Val(iconPath), Val(""))
                ),
                // Tooltip
                div(
                  cls := "absolute right-0 top-full mt-1 px-2 py-1 z-20 " +
                    "text-xs bg-slate-900 text-white rounded shadow-lg whitespace-nowrap " +
                    "hidden group-hover:block pointer-events-none z-10",
                  tooltipDescription
                )
              )
            )
          }
        }
    )
  }

  private def amountOfActionsTooltip(amountOfActionsSignal: Signal[AmountOfActions]): HtmlElement =
    div(
      cls := "absolute right-0 top-full translate-x-3/4 -translate-y-1/4 mt-2 px-2 py-1 " +
        "text-xs text-slate-100 bg-slate-700 rounded-md whitespace-nowrap shadow-lg " +
        "transition-opacity duration-150 opacity-0 pointer-events-none",
      child.text <-- amountOfActionsSignal.map {
        case AmountOfActions.Standard(amount) => s"x$amount"
        case AmountOfActions.Unlimited        => "∞"
      },
      cls("opacity-100") <-- amountOfActionsSignal.map(_.moreThanOne)
    )

  private def selectAmountOfActionsOverlay(
      actionSignal: Signal[ActiveActionData],
      selectedLimit: Var[Int],
  ): ReactiveHtmlElement[HTMLDivElement] = {
    val allowed = actionSignal.map(action =>
      action.amountOfActionsLeft match {
        case _
            if action.data.moveToArea.nonEmpty ||
              action.data.initialAmountOfActions.singleAction ||
              action.data.forceMaxAmountOfActionsIs1 =>
          Nil
        case AmountOfActions.Unlimited =>
          List(1, 5, 10, -1)
        case AmountOfActions.Standard(1) =>
          List(1)
        case AmountOfActions.Standard(amount) if amount < 5 =>
          List(1, 2, 3, -1).filter(_ <= amount)
        case AmountOfActions.Standard(amount) if amount < 10 =>
          List(1, 2, 5, -1).filter(_ <= amount)
        case AmountOfActions.Standard(amount) =>
          List(1, 5, 10, -1).filter(_ <= amount)
      }
    )
    val hideDiv = actionSignal.map(action =>
      action.data.moveToArea.nonEmpty || !action.amountOfActionsLeft.moreThanOne
    )
    div(
      cls := "absolute top-2 right-2",
      div(
        cls := "flex items-center gap-1",
        children <-- allowed.map(_.map { amount =>
          val label =
            if (amount == -1) "Max"
            else s"x$amount"

          button(
            cls := "px-2 py-0.5 text-[10px] rounded-md " +
              "bg-slate-700/60 ring-1 ring-slate-600 " +
              "hover:bg-slate-600/60 transition cursor-pointer " +
              "select-none text-base",

            // highlight if selected
            cls("ring-emerald-400 bg-emerald-600/60 text-white") <--
              selectedLimit.signal.map(_ == amount),
            label,
            onClick --> { e =>
              e.stopPropagation()
              selectedLimit.set(amount)
            }
          )
        }),
        cls("opacity-100") <-- hideDiv
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
          span(
            cls := "text-xs text-slate-300/80",
            child.text <-- skillSig.map(skill => f"x${skill.finalSpeedMulti}%.2f"),
          )
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

  private def permanentBonusToSVGPath(permanentBonus: PermanentBonus): String =
    permanentBonus match {
      case PermanentBonus.HalfTiredness => Constants.Icons.Strong
    }

}

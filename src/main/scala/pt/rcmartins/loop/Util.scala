package pt.rcmartins.loop

import com.raquo.laminar.api.L._
import pt.rcmartins.loop.GameData.selectedNextAction
import pt.rcmartins.loop.model.{ActionKind, ActiveActionData, SkillState}

object Util {

  private val owner = new Owner {}

//  def activeActionCard(vm: ActiveActionData): HtmlElement = {
//    val base =
//      "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow transition " +
//        "hover:ring-emerald-400/60 hover:shadow-md focus:outline-none " +
//        "focus:ring-2 focus:ring-emerald-400 pointer-events-none shadow-lg"
//
//    div(
//      role := "button",
//      cls := base,
//      tabIndex := 0,
////      cls <-- vm.enable.map {
////        case true  => base
////        case false => base + disabledCls
////      },
////      cls(selectedCls) <-- isSelected.signal,
//
//      //      // Interactions: click / Enter / Space toggle select and fire onSelect
//      //      onClick.filter(vm.disabled.map(!_)) --> { _ =>
//      //        isSelected.update(!_); onSelect()
//      //      },
//      //      onKeyDown.filter(vm.disabled.map(!_)).collect {
//      //        case ev if ev.key == "Enter" || ev.key == " " => ev.preventDefault(); ()
//      //      } --> { _ => isSelected.update(!_); onSelect() },
//
//      // Content
//      div(
//        cls := "flex items-start gap-3",
//        // Icon + kind accent
//        div(
//          cls := "mt-0.5 shrink-0 rounded-xl bg-slate-700/60 ring-1 ring-slate-600 p-2",
//          actionIcon(vm.data.kind)
//        ),
//
//        // Title + subtitle + badges
//        div(
//          cls := "min-w-0 flex-1",
//          div(
//            cls := "flex items-start justify-between gap-3",
//            div(
//              h3(cls := "text-base font-semibold tracking-tight", vm.data.title),
//              p(cls := "text-xs text-slate-300/90", vm.data.subtitle)
//            ),
//            //            vm.hotkey.map(k =>
//            //              span(
//            //                cls := "px-2 py-0.5 text-xs rounded-md bg-slate-700/70 ring-1 ring-slate-600 font-mono",
//            //                k
//            //              )
//            //            )
//          ),
//
//          // Badges row
//          div(
//            cls := "mt-2 flex flex-wrap items-center gap-2",
//            span(
//              cls := "px-2 py-0.5 text-xs rounded-full bg-slate-700/70 ring-1 ring-slate-600",
//              s"${vm.microSoFar / 1000000} / ${vm.data.baseTimeSec}",
//            ),
//            //            span(
//            //              cls := "px-2 py-0.5 text-xs rounded-full bg-amber-700/40 ring-1 ring-amber-600/50",
//            //              child.text <-- Val(s"${vm.energy}⚡")
//            //            )
//          ),
//
//          // Optional progress/cooldown
//
//          div(
//            cls := "mt-3",
//            div(
//              cls := "h-1.5 rounded-full bg-slate-700/60 overflow-hidden",
//              div(
//                cls := "h-1.5 rounded-full bg-emerald-500 transition-all",
//                width.percent := (vm.progressRatio * 100).toInt,
//              )
//            )
//          )
//        )
//      )
//    )
//  }

  def activeActionCard(vm: ActiveActionData): HtmlElement = {
    val base =
      "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow transition " +
        "hover:ring-emerald-400/60 hover:shadow-md focus:outline-none " +
        "focus:ring-2 focus:ring-emerald-400 pointer-events-none shadow-lg"

    div(
      role := "button",
      cls := base,
      tabIndex := 0,
      //      cls <-- vm.enable.map {
      //        case true  => base
      //        case false => base + disabledCls
      //      },
      //      cls(selectedCls) <-- isSelected.signal,

      //      // Interactions: click / Enter / Space toggle select and fire onSelect
      //      onClick.filter(vm.disabled.map(!_)) --> { _ =>
      //        isSelected.update(!_); onSelect()
      //      },

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
              p(cls := "text-xs text-slate-300/90", vm.data.subtitle)
            ),
          ),

          // Badges row
          div(
            cls := "mt-2 flex flex-wrap items-center gap-2",
            span(
              cls := "px-2 py-0.5 text-xs rounded-full bg-slate-700/70 ring-1 ring-slate-600",
              child.text <-- vm.longSoFar.map { timeSoFar =>
                s"$timeSoFar / ${vm.data.baseTimeSec}"
              },
            ),
            //            span(
            //              cls := "px-2 py-0.5 text-xs rounded-full bg-amber-700/40 ring-1 ring-amber-600/50",
            //              child.text <-- Val(s"${vm.energy}⚡")
            //            )
          ),

          // Optional progress/cooldown

          div(
            cls := "mt-3",
            div(
              cls := "h-1.5 rounded-full bg-slate-700/60 overflow-hidden",
              div(
                cls := "h-1.5 rounded-full bg-emerald-500 transition-all",
                width.percent <-- vm.progressRatio.map(ratio => (ratio * 100).toInt),
              )
            )
          )
        )
      )
    )
  }

  def actionCard(vm: Signal[ActiveActionData]): HtmlElement = {
    val isSelected = Var(false)

    val base =
      "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow transition " +
        "hover:ring-emerald-400/60 hover:shadow-md focus:outline-none " +
        "focus:ring-2 focus:ring-emerald-400 m-1"

    val selectedCls =
      " ring-2 ring-emerald-500 shadow-lg"

    val disabledCls =
      " opacity-50 grayscale pointer-events-none"

    isSelected.signal
      .withCurrentValueOf(vm)
      .foreach {
        case (true, action) =>
          println(s"Action selected: ${action.data.title}")
          GameData.selectNextAction(action.id)
        case _ =>
      }(owner)

    div(
      role := "button",
      tabIndex := 0,
      cls := base,
      cls(selectedCls) <-- selectedNextAction.combineWith(vm.map(_.id)).map {
        case (optId, actionId) => optId.contains(actionId)
      },

//      // Interactions: click / Enter / Space toggle select and fire onSelect
      onClick --> { _ =>
        isSelected.set(true)
      },

      // Content
      div(
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
              p(cls := "text-xs text-slate-300/90", child.text <-- vm.map(_.data.subtitle))
            ),
          ),

          // Badges row
          div(
            cls := "mt-2 flex flex-wrap items-center gap-2",
            span(
              cls := "px-2 py-0.5 text-xs rounded-full bg-slate-700/70 ring-1 ring-slate-600",
              child.text <-- vm.map(_.data.baseTimeSec.toString)
            ),
//            span(
//              cls := "px-2 py-0.5 text-xs rounded-full bg-amber-700/40 ring-1 ring-amber-600/50",
//              child.text <-- Val(s"${vm.energy}⚡")
//            )
          ),
        )
      )
    )
  }

  def skillAccent(kind: ActionKind): String =
    kind match {
      case ActionKind.Agility   => "bg-emerald-500"
      case ActionKind.Gardening => "bg-lime-500"
      case ActionKind.Cooking   => "bg-amber-500"
      case ActionKind.Crafting  => "bg-sky-500"
      case _                    => "bg-emerald-500"
    }

  def skillRow(skillSig: Signal[SkillState]): HtmlElement =
    div(
      cls := "rounded-xl p-3 bg-slate-800/60 ring-1 ring-slate-700 flex items-center gap-3",
      // Dot accent
      div(
        cls := "h-2.5 w-2.5 rounded-full",
        cls <-- skillSig.map { sk => skillAccent(sk.kind) },
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
        // Progress bar
        div(
          cls := "mt-1",
          div(
            cls := "h-1.5 rounded-full bg-slate-700/60 overflow-hidden",
            div(
              cls := "h-1.5 rounded-full transition-all",
              cls <-- skillSig.map { sk => skillAccent(sk.kind) },
              width.percent <-- skillSig.map { sk => (sk.loopRatio * 100).toInt }
            )
          ),
          div(
            cls := "mt-1 text-[11px] text-slate-300/80",
            child.text <-- skillSig.map { sk => s"${sk.loopXPLong}/${sk.nextLoopXP} XP" }
          )
        )
      )
    )

//  def skillsPanelRows(skillsSignal: Signal[List[SkillState]]): HtmlElement =
//    div(
//      cls := "space-y-2",
//      children <-- skillsSignal.split(_.kind) { case (_, _, s) => skillRow(s) }
//    )

  def actionIcon(kind: ActionKind): Element = kind match {
    case ActionKind.Gardening =>
      i(
        cls := "fa-solid fa-seedling h-5 w-5 opacity-90"
      )
    case ActionKind.Exploring =>
      i(
        cls := "fa-solid fa-magnifying-glass h-5 w-5 opacity-90"
      )
    case ActionKind.Cooking =>
      i(
        cls := "fa-solid fa-utensils h-5 w-5 opacity-90"
      )
    case ActionKind.Crafting =>
      i(
        cls := "fa-solid fa-scissors h-5 w-5 opacity-90"
      )
    case ActionKind.Agility =>
      i(
        cls := "fa-solid fa-person-running h-5 w-5 opacity-90"
      )
    case _ =>
      i(
        cls := "fa-solid fa-question h-5 w-5 opacity-90"
      )
  }

}

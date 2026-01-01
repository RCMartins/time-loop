package pt.rcmartins.loop

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLDivElement
import pt.rcmartins.loop.MobileUI.MobileTab
import pt.rcmartins.loop.Util.{actionCard, secondsToPrettyStr, skillRow}
import pt.rcmartins.loop.model.StoryLineHistory

import scala.scalajs.js.timers

class MobileUI(
    gameData: GameData,
    saveLoad: SaveLoad,
) {

  import gameData._

  private val tabVar: Var[MobileTab] = Var(MobileTab.Actions)

  def run(): ReactiveHtmlElement[HTMLDivElement] = {
    div(
      cls := "h-[100dvh] flex flex-col bg-slate-900 text-slate-100",
      topFixed(),
      div(
        cls := "flex-1 overflow-auto p-3",
        child <-- tabVar.signal.map {
          case MobileTab.Actions => actionsView()
//          case MobileTab.Map       => mapView()
          case MobileTab.Inventory => inventoryView()
          case MobileTab.Skills    => skillsView()
          case MobileTab.Story     => storyView()
          case MobileTab.Settings  => settingsView()
        }
      ),
      bottomTabs(tabVar)
    )
  }

  private val currentActionIsDefined: Signal[Boolean] =
    currentAction.map(_.isDefined).distinct

  private def actionsView(): ReactiveHtmlElement[HTMLDivElement] = {
    div(
      cls := "h-full flex flex-col",
      div(
        cls := "flex-1 overflow-y-auto",
        div(
          cls := "grid gap-1 sm:grid-cols-2 p-2",
          children <-- nextActions.split(_.id) { case (_, _, action) =>
            actionCard(action, gameData)
          }
        )
      ),
      div(
        cls := "shrink-0",
        div(
          cls := "flex items-center justify-center p-2",
          Util.worldMiniMap(
            characterArea,
            nextMoveActions,
            area => gameData.selectNextMoveAction(area),
          )
        )
      )
    )
  }

//  private def mapView(): ReactiveHtmlElement[HTMLDivElement] = {
//    div(
//      cls := "flex items-center justify-center",
//      Util.worldMiniMap(
//        characterArea,
//        nextMoveActions,
//        area => gameData.selectNextMoveAction(area),
//      )
//    )
//  }

  private def skillsView(): ReactiveHtmlElement[HTMLDivElement] = {
    div(
      cls := "space-y-2",
      children <-- skills.map(_.allHigherThan0).split(_.kind) { case (_, _, s) => skillRow(s) }
    )
  }

  private def storyView(): ReactiveHtmlElement[HTMLDivElement] = {
    div(
      cls := "overflow-y-auto",
      children <--
        storyActionsHistory.split(_.id) { case (_, StoryLineHistory(_, line), _) =>
          p(
            cls("animate-fade-in"),
            span(line),
          )
        },
      onMountCallback { ctx =>
        implicit val owner: Owner = ctx.owner
        val box = ctx.thisNode.ref

        def moveToBottom(): Unit =
          timers.setTimeout(0) {
            box.scrollTop = box.scrollHeight
          }
        storyActionsHistory.changes.foreach { _ => moveToBottom() }
        moveToBottom()
      },
    )
  }

  private def settingsView(): ReactiveHtmlElement[HTMLDivElement] = {
    Util.debugView(gameData, saveLoad)
  }

  private def inventoryView(): ReactiveHtmlElement[HTMLDivElement] = {
    div(
      Util.inventoryView(
        inventory,
        timeElapsedMicro,
      )
    )
  }

  private def topFixed(): ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "space-y-1 min-h-32 max-h-32 p-0.5",
      div(
        cls := "relative", // allow absolute positioning inside
        // progress background
        div(
          cls := "h-4 rounded-full bg-slate-700/60 overflow-hidden",
          div(
            cls := "h-4 rounded-full bg-green-600 origin-left will-change-transform",
            transform <-- energyRatio.map { ratio =>
              val clamped = ratio.max(0.0).min(1.0)
              s"scaleX($clamped)"
            }
          )
        ),
        // centered label
        div(
          cls := "absolute inset-0 flex items-center justify-center text-[11px] font-semibold text-slate-100",
          child.text <-- energyLong.combineWith(maxEnergyInt).map { case (energy, maxEnergy) =>
            f"$energy%d / $maxEnergy%d"
          }
        )
      ),
      div(
        cls := "flex justify-between px-2",
        span("Time Elapsed"),
        span(
          child.text <-- timeElapsedLong.map(secondsToPrettyStr)
        ),

        // Global elapsed time tooltip
        cls := "relative inline-block group",
        div(
          cls := "absolute left-0 top-full mt-1 px-2 py-1 z-20 " +
            "text-xs bg-slate-900 text-white rounded shadow-lg whitespace-nowrap " +
            "hidden group-hover:block pointer-events-none z-10",
          "Total Elapsed Time: ",
          child.text <-- globalTimeElapsedLong.map(secondsToPrettyStr)
        )
      ),
      div(
        cls := "flex justify-between px-2",
        span("Time Saved"),
        span(
          child.text <-- extraTimeLong.map(secondsToPrettyStr)
        ),
      ),
      div(
        cls := "flex w-full items-start gap-3",
        child.maybe <--
          currentActionIsDefined.map {
            case false =>
              None
            case true =>
              Some(
                Util.mobileCurrentAction(
                  currentAction,
                  skills,
                )
              )
          }
      )
    )

  private def bottomTabs(tabVar: Var[MobileTab]): HtmlElement = {
    def tabButton(tab: MobileTab, label: String, icon: String): HtmlElement =
      button(
        tpe := "button",
        cls := "flex-1 h-14 flex flex-col items-center justify-center gap-0.5 " +
          "text-xs transition select-none",
        cls("text-emerald-300") <-- tabVar.signal.map(_ == tab),
        cls("text-slate-300") <-- tabVar.signal.map(_ != tab),
        onClick --> (_ => tabVar.set(tab)),

        // You can replace icon with your SVG icon function
        span(cls := "text-base leading-none", icon),
        span(label)
      )

    div(
      cls := "h-16 bg-slate-900/80 ring-1 ring-slate-800 " +
        "backdrop-blur flex items-stretch",
      tabButton(MobileTab.Actions, "Actions", "⚡"),
//      tabButton(MobileTab.Map, "Map", "🧭"),
      tabButton(MobileTab.Inventory, "Inventory", "🎒"),
      tabButton(MobileTab.Story, "Story", "📖"),
      tabButton(MobileTab.Skills, "Skills", "📈"),
      tabButton(MobileTab.Settings, "Settings", "⚙️"),
    )
  }

}

object MobileUI {

  sealed trait MobileTab

  object MobileTab {
    case object Actions extends MobileTab
//    case object Map extends MobileTab
    case object Inventory extends MobileTab
    case object Skills extends MobileTab
    case object Story extends MobileTab
    case object Settings extends MobileTab
  }

}

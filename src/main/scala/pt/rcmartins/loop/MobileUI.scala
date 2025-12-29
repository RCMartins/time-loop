package pt.rcmartins.loop

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLDivElement
import pt.rcmartins.loop.MobileUI.MobileTab
import pt.rcmartins.loop.Util.{actionCard, activeActionCard}
import pt.rcmartins.loop.data.StoryActions

class MobileUI(
    gameData: GameData
) {

  import gameData._

  private val tabVar: Var[MobileTab] = Var(MobileTab.Actions)

  def run(): ReactiveHtmlElement[HTMLDivElement] = {
    mobileShell(
      actionsView = actionsView(),
      mapView = div(),
      skillsView = div(),
      inventoryView = div(),
    )
  }

  private def mobileShell(
      actionsView: ReactiveHtmlElement[HTMLDivElement],
      mapView: ReactiveHtmlElement[HTMLDivElement],
      skillsView: ReactiveHtmlElement[HTMLDivElement],
      inventoryView: ReactiveHtmlElement[HTMLDivElement],
  ): ReactiveHtmlElement[HTMLDivElement] = {

    div(
      cls := "h-[100dvh] flex flex-col bg-slate-950 text-slate-100",

      // Main content (no scrolling)
      div(
        cls := "flex-1 overflow-hidden p-3",
        child <-- tabVar.signal.map {
          case MobileTab.Actions   => actionsView
          case MobileTab.Map       => mapView
          case MobileTab.Skills    => skillsView
          case MobileTab.Inventory => inventoryView
        }
      ),

      // Bottom tabs
      bottomTabs(tabVar)
    )
  }

  private val currentActionIsDefined: Signal[Boolean] =
    currentAction.map(_.isDefined).distinct

  private def actionsView(): ReactiveHtmlElement[HTMLDivElement] = {
    div(
      div(
        cls := "space-y-3 min-h-32 max-h-32",
        div(
          cls := "flex items-start gap-3",
          child.maybe <--
            currentActionIsDefined.map {
              case false =>
                None
              case true =>
                Some(
                  mobileCurrentAction(
                    currentAction,
                    skills,
                  )
                )
            }
        )
      ),
      div(
        cls := "grid gap-5 sm:grid-cols-2",
        children <-- nextActions.split(_.id) { case (_, _, action) =>
          actionCard(action, gameData)
        }
      )
    )
  }

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
      tabButton(MobileTab.Map, "Map", "🧭"),
      tabButton(MobileTab.Skills, "Skills", "📈"),
      tabButton(MobileTab.Inventory, "Inventory", "🎒")
    )
  }

}

object MobileUI {

  sealed trait MobileTab

  object MobileTab {
    case object Actions extends MobileTab
    case object Map extends MobileTab
    case object Inventory extends MobileTab
    case object Skills extends MobileTab
  }

}

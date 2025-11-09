package pt.rcmartins.loop

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.{html, window, HTMLDivElement}
import pt.rcmartins.loop.GameData._
import pt.rcmartins.loop.Util._
import pt.rcmartins.loop.model._

import scala.scalajs.js.timers.{setInterval, setTimeout}

object Main {

  // Example skills list (use your real skillsPanelRows or skillsPanelGrid)
  val skillsView =
    div(
      cls := "space-y-2",
//      div(
//        cls := "rounded-xl p-3 bg-slate-800/60 ring-1 ring-slate-700",
//        "Gardening — Lv 2 (15/40)"
//      ),
//      div(cls := "rounded-xl p-3 bg-slate-800/60 ring-1 ring-slate-700", "Cooking — Lv 1 (8/20)"),
//      div(cls := "rounded-xl p-3 bg-slate-800/60 ring-1 ring-slate-700", "Writing — Lv 3 (22/60)")
//      children <--
//        skills.map(_.allSeq.map { skill =>
//          skillRow(skill)
//        })

      children <-- skills.map(_.allSeq).split(_.kind) { case (_, _, s) => skillRow(s) }
    )

  // Current action sample
  val currentActionView: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "flex items-start gap-3",
      child.maybe <--
        currentAction.map {
          case None =>
            None
          case Some(activeAction) =>
            Some(activeActionCard(activeAction))
        }
    )

  // Next actions grid (your actionCard components here)
  val nextActionsView: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "grid gap-3 sm:grid-cols-2",
      children <--
        nextActions.split(_.id) { case (_, _, action) =>
          actionCard(action)
        }
    )

  // Inventory list
  val inventoryView =
    ul(
      cls := "space-y-2",
      children <-- inventory.map(_.allItems).map {
        _.map { case (item, qty) =>
          li(
            cls := "rounded-lg p-2 bg-slate-800/60 ring-1 ring-slate-700 flex justify-between items-center",
            span(cls := "font-mono", s"$qty "),
            span(item.name),
          )
        }
      }
    )

  // Misc info (stats / timers)
  val miscView =
    div(
      cls := "space-y-2 text-sm",
      div(
        cls := "flex justify-between",
        span("Time"),
        // span("07:42")
        span(
          child.text <-- timeElapsedLong.map(s => "%02d:%02d".format(s / 60, s % 60))
        )
      ),
      div(
        cls := "flex justify-between",
        span("Energy"),
        span(
          child.text <-- energyLong.combineWith(maxEnergyInt).map { case (energy, maxEnergy) =>
            f"$energy%d / $maxEnergy%d"
          }
        ),
      ),
//      div(cls := "flex justify-between", span("Loop"), span("Day 6"))
    )

  def main(args: Array[String]): Unit = {
//    render(dom.document.getElementById("main-div"), mainDiv())

    setInterval(25) {
      runUpdateGameState()
    }

    val ui = appLayout(skillsView, currentActionView, nextActionsView, inventoryView, miscView)
    render(dom.document.getElementById("main-div"), ui)
  }

//  private def mainDiv(): ReactiveHtmlElement[html.Div] = {
////    div("Hello, Time Loop Scala!")
//    div(
//      skillsPanelRows(
//        Val(
//          List(
//            SkillState("cooking", "Cooking", 2, 30, 100),
//            SkillState("gardening", "Gardening", 1, 10, 50),
//            SkillState("crafting", "Crafting", 3, 80, 150),
//          )
//        )
//      ),
////      actionCard(
////        ActionData(
////          title = "Gardening",
////          subtitle = "Grow some plants",
////          baseTimeSec = 300,
////          energy = 20,
////          kind = ActionKind.Gardening,
////          progress01 = Some(0.5),
////        )
////      )(() => println("Gardening action selected")),
////      actionCard(
////        ActionData(
////          title = "Crafting",
////          subtitle = "Make some soap",
////          baseTimeSec = 300,
////          energy = 20,
////          kind = ActionKind.Crafting,
////          progress01 = Some(0.5),
////        )
////      )(() => println("Crafting action selected"))
//    )
//  }

  /** Reusable section shells */
  def panelCard(title: String, mods: Mod[HtmlElement]*): HtmlElement =
    div(
      cls := "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow",
      h3(cls := "text-sm font-semibold tracking-tight mb-2", title),
      mods
    )

  /** Skills sidebar (left) */
  def skillsSidebar(skillsView: HtmlElement): HtmlElement =
    div(
      cls := "space-y-3 sticky top-4 self-start",
      panelCard("Skills", skillsView)
    )

  /** Center column: top (current) + bottom (next) */
  def centerColumn(currentActionView: HtmlElement, nextActionsView: HtmlElement): HtmlElement =
    div(
      // two rows: auto (top) + 1fr (bottom)
      cls := "grid grid-rows-[auto,1fr] gap-4 min-h-[60dvh]",
      // top: current action
      panelCard(
        "Current Action",
        // keep this compact; grows only as needed
        div(cls := "space-y-3", currentActionView)
      ),
      // bottom: next actions list with own scroll
      div(
        cls := "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow flex flex-col min-h-0",
        h3(cls := "text-sm font-semibold tracking-tight mb-2", "Next Actions"),
        div(cls := "overflow-auto min-h-0 grow", nextActionsView)
      )
    )

  /** Right sidebar: inventory + other info stacked */
  def rightSidebar(inventoryView: HtmlElement, miscView: HtmlElement): HtmlElement =
    div(
      cls := "space-y-3 sticky top-4 self-start",
      panelCard("Inventory", div(cls := "space-y-2 max-h-[40dvh] overflow-auto", inventoryView)),
      panelCard("Notes / Info", div(cls := "space-y-2 max-h-[30dvh] overflow-auto", miscView))
    )

  /** Page scaffold */
  def appLayout(
      skillsView: HtmlElement,
      currentActionView: HtmlElement,
      nextActionsView: HtmlElement,
      inventoryView: HtmlElement,
      miscView: HtmlElement
  ): HtmlElement =
    div(
      // page padding & responsive grid
      cls := "min-h-dvh p-4 md:p-6 bg-slate-900 text-slate-100",
      div(
        // mobile: single column; desktop: 3 columns
        cls := "grid gap-4 md:grid-cols-[260px,1fr,300px]",
        // Order for mobile stacking
        div(cls := "order-2 md:order-1", skillsSidebar(skillsView)),
        div(cls := "order-1 md:order-2", centerColumn(currentActionView, nextActionsView)),
        div(cls := "order-3 md:order-3", rightSidebar(inventoryView, miscView))
      )
    )

}

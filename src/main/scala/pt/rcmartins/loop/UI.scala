package pt.rcmartins.loop

import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.softwaremill.quicklens.ModifyPimp
import org.scalajs.dom
import org.scalajs.dom.{HTMLDivElement, HTMLUListElement}
import pt.rcmartins.loop.GameUtils.Toast
import pt.rcmartins.loop.Util._
import pt.rcmartins.loop.data.StoryActions
import pt.rcmartins.loop.model._

import scala.scalajs.js.timers

class UI(
    gameData: GameData,
    saveLoad: SaveLoad,
) {

  private val DEBUG_MODE: Boolean = true

  import gameData._

  def run(): ReactiveHtmlElement[HTMLDivElement] = {
    div(
      // page padding & responsive grid
      cls := "min-h-dvh p-4 md:py-6 md:px-20 bg-slate-900 text-slate-100",
      styleAttr <-- MobileUtils.isMobileSignal.map {
        case true  => "zoom: 0.75;"
        case false => "zoom: 1;"
      },
      div(
        // mobile: single column; desktop: 3 columns
        cls := "grid gap-4 md:grid-cols-[260px,1fr,300px]",
        // Order for mobile stacking
        div(cls := "order-1 md:order-1", skillsSidebar(skillsView)),
        div(
          cls := "order-2 md:order-2",
          centerColumn()
        ),
        div(cls := "order-3 md:order-3", rightSidebar())
      ),
      toastContainer
    )
  }

  private val skillsView =
    div(
      cls := "space-y-2",
      children <-- skills.map(_.allHigherThan0).split(_.kind) { case (_, _, s) => skillRow(s) }
    )

  private val currentActionIsDefined: Signal[Boolean] =
    currentAction.map(_.isDefined).distinct

  private val currentActionView: HtmlElement =
    panelCard(
      span("Current Action"),
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
                  activeActionCard(
                    // TODO ugly
                    currentAction.map(_.getOrElse(StoryActions.Data.WakeUp.toActiveAction)),
                    skills,
                  )
                )
            }
        )
      )
    )

  private val nextActionsView: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "grid gap-5 sm:grid-cols-2",
      children <-- nextActions.split(_.id) { case (_, _, action) =>
        actionCard(action, gameData)
      }
    )

  private val nextMoveActionsView: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "flex items-center justify-center",
      worldMiniMap(
        characterArea,
        nextMoveActions,
        area => gameData.selectNextMoveAction(area),
      )
    )

  // Misc info (stats / timers)
  private val miscView: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "space-y-2 text-sm",
      div(
        cls := "flex justify-between",
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
        cls := "flex justify-between",
        span("Time Saved"),
        span(
          child.text <-- extraTimeLong.map(secondsToPrettyStr)
        ),
      ),
      child.maybe <--
        loopNumber.map(_ > 1).distinct.map {
          case false =>
            None
          case true =>
            Some(
              div(
                cls := "flex justify-between",
                span("Loop Count"),
                span(child.text <-- loopNumber.map(_.toString)),
              )
            )
        },
      div(
        cls := "flex justify-between",
        span("Energy"),
        span(
          child.text <-- energyLong.combineWith(maxEnergyInt).map { case (energy, maxEnergy) =>
            f"$energy%d / $maxEnergy%d"
          }
        ),
      ),
      div(
        cls := "flex justify-between",
        span("Tiredness"),
        span(
          child.text <-- currentTiredSecond.map { tiredSecond =>
            f"$tiredSecond%.2f / sec"
          }
        ),
      ),
    )

  /** Reusable section shells */
  private def panelCard(title: ReactiveHtmlElement[_], mods: Mod[HtmlElement]*): HtmlElement =
    div(
      cls := "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow",
      h3(cls := "text-sm font-semibold tracking-tight mb-2", title),
      mods
    )

  /** Skills sidebar (left) */
  private def skillsSidebar(skillsView: HtmlElement): HtmlElement =
    div(
      cls := "space-y-3 sticky top-4 self-start",
      panelCard(span("Skills"), skillsView)
    )

  /** Center column: top (current) + bottom (next) */
  private def centerColumn(): HtmlElement = {
    div(
      // two rows: auto (top) + 1fr (bottom)
      cls := "grid grid-rows-[auto,1fr] gap-4 min-h-[60dvh]",
      // top: current action
      child <--
        MobileUtils.isMobileSignal.map {
          case false =>
            div(
              cls := "grid grid-cols-2 gap-3",
              currentActionView,
              storyDiv,
            )
          case true =>
            div(
              storyDiv,
              div(cls := "mt-4", currentActionView),
            )
        },
      // bottom: next actions list with own scroll
      div(
        cls := "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow flex flex-col min-h-0 ",
        h3(
          cls := "text-sm font-semibold tracking-tight mb-2 relative",
          "Area Actions",
          div(
            cls := "absolute left-1/2 -translate-x-1/2 bottom-full mb-1 px-2 py-1 " +
              "text-xs text-slate-100 bg-slate-700 rounded-md whitespace-nowrap shadow-lg " +
              "transition-opacity duration-150 opacity-100 pointer-events-none",
            child.text <--
              characterArea.combineWith(currentActionMovingArea).map {
                case (characterArea, None) =>
                  characterArea.name
                case (characterArea, Some(actionMovingArea)) =>
                  s"${characterArea.name} -> ${actionMovingArea.name}"
              }
          ),
        ),
        div(cls := "overflow-auto min-h-0 grow min-h-32", nextActionsView),
      ),
      // bottom: next actions list with own scroll
      child.maybe <--
        showMapUI.map {
          case false =>
            None
          case true =>
            Some(
              div(
                cls := "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow flex flex-col min-h-0",
                h3(cls := "text-sm font-semibold tracking-tight mb-2", "Move Actions"),
                div(cls := "overflow-auto min-h-0 grow min-h-32", nextMoveActionsView)
              )
            )
        },
      div(
        cls := "mt-3 relative", // allow absolute positioning inside
        // progress background
        div(
          cls := "h-5 rounded-full bg-slate-700/60 overflow-hidden",
          div(
            cls := "h-5 rounded-full bg-green-600 origin-left will-change-transform",
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
    )
  }

  private def storyDiv: HtmlElement =
    panelCard(
      span("Story"),
      div(
        cls := "overflow-y-auto min-h-32 max-h-32",
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
    )

  /** Right sidebar: inventory + other info stacked */
  private def rightSidebar(): HtmlElement =
    div(
      cls := "space-y-3 sticky top-4 self-start",
      panelCard(
        span(
          "Inventory",
          child.text <-- inventory.map(inv => s" (max size: ${inv.maximumSize})"),
        ),
        div(
          cls := "space-y-2 pb-1 max-h-[40dvh] overflow-auto",
          inventoryView(inventory, timeElapsedMicro)
        )
      ),
      panelCard(
        span("Notes / Info"),
        div(cls := "space-y-2 max-h-[30dvh] overflow-auto", miscView)
      ),
      if (DEBUG_MODE)
        panelCard(
          span("Debug Actions"),
          div(
            cls := "space-y-2 max-h-[30dvh] overflow-auto",
            debugView(gameData, saveLoad),
          )
        )
      else
        emptyNode
    )

  private def toastContainer: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "fixed top-4 right-4 space-y-2 z-50",
      children <-- gameData.utils.toastsVar.signal.map(_.map(renderToast))
    )

  private def renderToast(t: Toast): HtmlElement =
    div(
      idAttr := t.id.toString,
      cls := "bg-slate-900 text-white text-sm px-4 py-2 rounded shadow-lg " +
        "opacity-0 transition-opacity duration-300",
      onMountCallback { ctx =>
        // fade in
        ctx.thisNode.ref.classList.add("opacity-100")

        // schedule auto-remove
        dom.window.setTimeout(
          () => {
            ctx.thisNode.ref.classList.remove("opacity-100")
            dom.window.setTimeout(
              () => {
                gameData.utils.toastsVar.update(_.filterNot(_.id == t.id))
              },
              300
            ) // wait for fade-out transition
          },
          2000
        ) // toast visible for 2s
      },
      t.message
    )

}

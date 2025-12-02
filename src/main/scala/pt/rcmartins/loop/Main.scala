package pt.rcmartins.loop

import com.raquo.airstream.core.AirstreamError.CombinedError
import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.softwaremill.quicklens.ModifyPimp
import org.scalajs.dom
import org.scalajs.dom.{HTMLDivElement, HTMLUListElement}
import pt.rcmartins.loop.GameData._
import pt.rcmartins.loop.Util._
import pt.rcmartins.loop.data.Level1
import pt.rcmartins.loop.model.{GameState, StoryLineHistory}

import scala.scalajs.js.timers
import scala.scalajs.js.timers.setInterval

object Main {

  private val DEBUG_MODE: Boolean = true

  def main(args: Array[String]): Unit = {
    AirstreamError.registerUnhandledErrorCallback { e =>
      println("=== UNHANDLED AIRSTREAM ERROR ===")

      def dump(t: Throwable, indent: String = ""): Unit = t match {
        case ce: CombinedError =>
          println(indent + s"CombinedError with ${ce.causes.size} inner errors")
          ce.causes.zipWithIndex.foreach { case (inner, i) =>
            println(indent + s"  child #$i:")
            inner.foreach(dump(_, indent + "    "))
          }
        case other =>
          println(indent + s"${other.getClass.getName}: ${other.getMessage}")
          other.getStackTrace.take(15).foreach { f =>
            println(indent + s"  at $f")
          }
      }

      dump(e)
    }

    SaveLoad.loadFromLocalStorage().foreach(SaveLoad.reloadDataFromLoadedSave)

    setInterval(25) {
      runUpdateGameState()
    }

    val ui: ReactiveHtmlElement[HTMLDivElement] =
      div(
        // page padding & responsive grid
        cls := "min-h-dvh p-4 md:py-6 md:px-20 bg-slate-900 text-slate-100",
        div(
          // mobile: single column; desktop: 3 columns
          cls := "grid gap-4 md:grid-cols-[260px,1fr,300px]",
          // Order for mobile stacking
          div(cls := "order-1 md:order-1", skillsSidebar(skillsView)),
          div(
            cls := "order-2 md:order-2",
            centerColumn()
          ),
          div(cls := "order-3 md:order-3", rightSidebar(inventoryView, miscView))
        ),
      )

    render(dom.document.getElementById("main-div"), ui)
  }

  private val skillsView =
    div(
      cls := "space-y-2",
      children <-- skills.map(_.allHigherThan0).split(_.kind) { case (_, _, s) => skillRow(s) }
    )

  private val currentActionIsDefined: Signal[Boolean] =
    currentAction.map(_.isDefined).distinct

  private val currentActionView: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "flex items-start gap-3",
      child.maybe <--
        currentActionIsDefined.map {
          case false => None
          case true  =>
            Some(
              activeActionCard(currentAction.map(_.getOrElse(Level1.Data.WakeUp.toActiveAction)))
            )
        }
    )

  private val nextActionsView: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "grid gap-5 sm:grid-cols-2",
      children <-- nextActions.split(_.id) { case (_, _, action) => actionCard(action) }
    )

  private val nextMoveActionsView: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "grid gap-5 sm:grid-cols-2",
      children <-- nextMoveActions.split(_.id) { case (_, _, action) => actionCard(action) }
    )

  private val inventoryView: ReactiveHtmlElement[HTMLUListElement] =
    ul(
      cls := "space-y-2",
      children <-- inventory.map(_.items).map {
        _.filter(_._2 > 0).sortBy(_._1.inventoryOrder).map { case (item, qty, cooldownTimeMicro) =>
          li(
            cls := "rounded-lg p-2 bg-slate-800/60 ring-1 ring-slate-700 flex justify-between items-center m-1",
            span(cls := "font-mono", s"$qty "),
            span(item.name),
            span(
              child.text <-- timeElapsedMicro.map(elapsed =>
                if (elapsed > cooldownTimeMicro)
                  ""
                else {
                  val remainingMicro = cooldownTimeMicro - elapsed
                  val remainingSec = remainingMicro.toDouble / 1e6
                  f"$remainingSec%.1f"
                }
              )
            ),
          )
        }
      }
    )

  // Misc info (stats / timers)
  private val miscView: ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "space-y-2 text-sm",
      div(
        cls := "flex justify-between",
        span("Time Elapsed"),
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
      div(
        cls := "grid grid-cols-2 gap-3",
        panelCard(
          span("Current Action"),
          div(cls := "space-y-3 min-h-32 max-h-32", currentActionView)
        ),
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

              // Observe changes to the story list
              storyActionsHistory.changes.foreach { _ =>
                // 2) Defer scroll to the end of the JS tick so children are already updated
                timers.setTimeout(0) {
                  box.scrollTop = box.scrollHeight
                }
              }
            },
          )
        )
      ),
      // bottom: next actions list with own scroll
      div(
        cls := "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow flex flex-col min-h-0",
        h3(cls := "text-sm font-semibold tracking-tight mb-2", "Available Actions"),
        div(cls := "overflow-auto min-h-0 grow min-h-32", nextActionsView)
      ),
      // bottom: next actions list with own scroll
      div(
        cls := "rounded-2xl p-4 bg-slate-800/60 ring-1 ring-slate-700 shadow flex flex-col min-h-0",
        h3(cls := "text-sm font-semibold tracking-tight mb-2", "Move Actions"),
        div(cls := "overflow-auto min-h-0 grow min-h-32", nextMoveActionsView)
      ),
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

  /** Right sidebar: inventory + other info stacked */
  private def rightSidebar(inventoryView: HtmlElement, miscView: HtmlElement): HtmlElement =
    div(
      cls := "space-y-3 sticky top-4 self-start",
      panelCard(
        span(
          "Inventory",
          child.text <-- inventory.map(inv => s" (max size: ${inv.maximumSize})"),
        ),
        div(cls := "space-y-2 max-h-[40dvh] overflow-auto", inventoryView)
      ),
      panelCard(
        span("Notes / Info"),
        div(cls := "space-y-2 max-h-[30dvh] overflow-auto", miscView)
      ),
      if (DEBUG_MODE)
        panelCard(
          span("Debug Actions"),
          div(cls := "space-y-2 max-h-[30dvh] overflow-auto", debugView)
        )
      else
        emptyNode
    )

  private def debugView: HtmlElement = {
    val owner: Owner = new OneTimeOwner(() => println("Debug view owner disposed"))
    val addEnergyBus: EventBus[Int] = new EventBus[Int]()
    val multiplyAllSkillsBus: EventBus[Double] = new EventBus[Double]()
    val multiplySpeedBus: EventBus[Double] = new EventBus[Double]()

    addEnergyBus.events
      .withCurrentValueOf(GameData.gameState)
      .foreach { case (amount, state) =>
        val newEnergy = state.energyMicro + amount.toLong * 1_000_000L
        val cappedEnergy = Math.min(newEnergy, state.maxEnergyMicro)
        val newState: GameState =
          state
            .modify(_.energyMicro)
            .setTo(cappedEnergy)
            .modify(_.stats.usedCheats)
            .setTo(true)
        GameData.loadGameState(newState)
      }(owner)

    multiplyAllSkillsBus.events
      .withCurrentValueOf(GameData.gameState)
      .foreach { case (multiplier, state) =>
        val newState: GameState =
          state
            .modify(_.skills)
            .using(
              _.mapSkills {
                _.modifyAll(_.initialBonusMultiplier, _.currentBonusMultiplier)
                  .setTo(multiplier)
              }
            )
            .modify(_.stats.usedCheats)
            .setTo(true)
        GameData.loadGameState(newState)
      }(owner)

    multiplySpeedBus.events
      .withCurrentValueOf(GameData.gameState)
      .foreach { case (multiplier, state) =>
        val newState: GameState =
          state
            .modify(_.skills.globalGameSpeed)
            .setTo(multiplier)
            .modify(_.stats.usedCheats)
            .setTo(true)
        GameData.loadGameState(newState)
      }(owner)

    div(
      cls := "flex flex-col gap-2",
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "Loop Now!",
        onClick --> { _ =>
          GameData.DebugLoopNow()
        }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "Add 100 Energy",
        onClick --> { _ => addEnergyBus.writer.onNext(100) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "x1 Speed",
        onClick --> { _ => multiplySpeedBus.writer.onNext(1.0) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "x2 Speed",
        onClick --> { _ => multiplySpeedBus.writer.onNext(2.0) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "x5 Speed",
        onClick --> { _ => multiplySpeedBus.writer.onNext(5.0) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "x10 Speed",
        onClick --> { _ => multiplySpeedBus.writer.onNext(10.0) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "x1 Skills",
        onClick --> { _ => multiplyAllSkillsBus.writer.onNext(1.0) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "x2 Skills",
        onClick --> { _ => multiplyAllSkillsBus.writer.onNext(2.0) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "x5 Skills",
        onClick --> { _ => multiplyAllSkillsBus.writer.onNext(5.0) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "x10 Skills",
        onClick --> { _ => multiplyAllSkillsBus.writer.onNext(10.0) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "Hard Reset!",
        onClick --> { _ =>
          GameData.DebugHardReset()
        }
      )
    )
  }

}

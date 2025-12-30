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
import scala.scalajs.js.timers.setInterval

class UI(
    gameData: GameData,
    saveLoad: SaveLoad,
) {

  // TODO Hide map until first move action is available

  private val owner = new Owner {}
  private val DEBUG_MODE: Boolean = true

  import gameData._

  def run(): ReactiveHtmlElement[HTMLDivElement] = {
    setInterval(25) {
      gameData.runUpdateGameState()
    }

    gameData.utils.toastBus.events.foreach { toast =>
      gameData.utils.toastsVar.update(_ :+ toast)
    }(owner)

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
        area => gameData.selectNextMoveAction(area)
      )
    )

  private def worldMiniMap(
      currentArea: Signal[CharacterArea],
      onMove: CharacterArea => Unit
  ): ReactiveHtmlElement[HTMLDivElement] = {

    // Center (current location)
    def cellCenter(): HtmlElement =
      div(
        cls := "flex items-center justify-center w-32 h-10",
        div(
          cls := "px-3 py-1 rounded-full text-[11px] font-semibold " +
            "bg-emerald-600 text-white shadow whitespace-nowrap",
          div(
            cls := "w-5 h-5 inline-block mr-1 align-middle",
            Constants.Icons.CreateIconElement(currentArea.map(_.iconPath), Val("")),
          ),
          child.text <-- currentArea.map(_.name),
        )
      )

    // Neighbor cell in a given direction
    def cellDir(dir: CharacterArea => Option[CharacterArea]): HtmlElement =
      div(
        cls := "flex items-center justify-center w-32 h-10",
        child.maybe <-- currentArea.combineWith(nextMoveActions).map { case (loc, moveActions) =>
          val targetAreaOpt: Option[CharacterArea] = dir(loc)
          moveActions
            .find(_.data.moveToArea == targetAreaOpt)
            .flatMap(action => action.data.moveToArea.map(_ -> action))
            .map { case (targetArea, action) =>
              button(
                tpe := "button",
                cls := "px-3 py-1 rounded-full text-[11px] " +
                  "border border-slate-600 bg-slate-800/80 text-slate-200 " +
                  "hover:bg-slate-700/80 hover:text-white transition " +
                  "whitespace-nowrap",
                onClick --> { _ => onMove(targetArea) },
                div(
                  cls := "w-5 h-5 inline-block mr-1 align-middle",
                  Constants.Icons.CreateIconElement(Val(targetArea.iconPath), Val("")),
                ),
                div(
                  span(targetArea.name),
                  " (",
                  s"${action.targetTimeSec}",
                  ")",
                )
              )
            }
        }
      )

    div(
      cls := "relative inline-block p-2 rounded-2xl bg-slate-900/80 " +
        "ring-1 ring-slate-700 shadow",

      // The 3x3 grid
      div(
        cls := "grid grid-cols-3 grid-rows-3 gap-1",
        // top row
        cellDir(_.connection(Dir8.TopLeft)),
        cellDir(_.connection(Dir8.Top)),
        cellDir(_.connection(Dir8.TopRight)),
        // middle row
        cellDir(_.connection(Dir8.Left)),
        cellCenter(),
        cellDir(_.connection(Dir8.Right)),
        // bottom row
        cellDir(_.connection(Dir8.BottomLeft)),
        cellDir(_.connection(Dir8.Bottom)),
        cellDir(_.connection(Dir8.BottomRight)),
      ),
    )
  }

  private val inventoryView: ReactiveHtmlElement[HTMLUListElement] =
    ul(
      cls := "space-y-2",
      children <-- inventory.map(_.items.filter(_._2 > 0)).split(_._1) {
        case (_, (item, _, _), itemSignal) =>
          li(
            cls := "rounded-lg p-2 bg-slate-800/60 ring-1 ring-slate-700 flex items-center m-1",

            // ICON (10%)
            div(
              cls := "w-[10%] flex justify-center",
              Constants.Icons.CreateIconElement(
                itemSignal.map(_._1.iconPath),
                itemSignal.map(_._1.iconColor),
              )
            ),

            // QTY (20%)
            div(
              cls := "w-[20%] text-xs font-mono text-right",
              child.text <-- itemSignal.map(_._2).distinct.map(qty => item.amountFormatInv(qty)),
            ),

            // NAME (50%)
            div(
              cls := "w-[50%] text-xs pl-2 truncate",
              item.name
            ),

            // COOLDOWN (20%)
            div(
              cls := "w-[20%] text-xs text-right",
              child.text <-- itemSignal.map(_._3).distinct.combineWith(timeElapsedMicro).map {
                case (cooldownTimeMicro, elapsed) =>
                  if (elapsed > cooldownTimeMicro)
                    ""
                  else {
                    val remainingSec = (cooldownTimeMicro - elapsed).toDouble / 1e6
                    f"$remainingSec%.1f"
                  }
              }
            )
          )

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
        div(cls := "space-y-2 pb-1 max-h-[40dvh] overflow-auto", inventoryView)
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
    val exportSaveStringToClipboard: EventBus[Unit] = new EventBus[Unit]()
    val importStringFromClipboard: EventBus[Unit] = new EventBus[Unit]()

    addEnergyBus.events
      .withCurrentValueOf(gameData.gameState)
      .foreach { case (amount, state) =>
        val newEnergy = state.energyMicro + amount.toLong * 1_000_000L
        val cappedEnergy = Math.min(newEnergy, state.maxEnergyMicro)
        val newState: GameState =
          state
            .modify(_.energyMicro)
            .setTo(cappedEnergy)
            .modify(_.stats.usedCheats)
            .setTo(true)
        gameData.loadGameState(newState)
      }(owner)

    multiplyAllSkillsBus.events
      .withCurrentValueOf(gameData.gameState)
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
        gameData.loadGameState(newState)
      }(owner)

    multiplySpeedBus.events
      .withCurrentValueOf(gameData.gameState)
      .foreach { case (multiplier, state) =>
        val newState: GameState =
          state
            .modify(_.skills.globalGameSpeed)
            .setTo(multiplier)
            .modify(_.stats.usedCheats)
            .setTo(true)
        gameData.loadGameState(newState)
      }(owner)

    exportSaveStringToClipboard.events
      .withCurrentValueOf(gameData.gameState)
      .foreach { state =>
        dom.window.navigator.clipboard.writeText(saveLoad.saveString(state))
        gameData.utils.showToast("Copied to clipboard!")
      }(owner)

    importStringFromClipboard.events.foreach { _ =>
      dom.window.navigator.clipboard
        .readText()
        .toFuture
        .foreach { str =>
          saveLoad.loadString(str).foreach { loadedState =>
            gameData.loadGameState(loadedState)
            saveLoad.saveToLocalStorage(loadedState)
            gameData.utils.showToast("Game loaded!")
          }
        }(scala.scalajs.concurrent.JSExecutionContext.queue)
    }(owner)

    div(
      cls := "flex flex-col gap-2",
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "Loop Now!",
        onClick --> { _ =>
          gameData.DebugLoopNow(saveLoad)
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
        "x10 Speed",
        onClick --> { _ => multiplySpeedBus.writer.onNext(10.0) }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "x100 Speed",
        onClick --> { _ => multiplySpeedBus.writer.onNext(100.0) }
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
        "Export",
        onClick --> { _ =>
          exportSaveStringToClipboard.writer.onNext(())
        }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "Import",
        onClick --> { _ =>
          importStringFromClipboard.writer.onNext(())
        }
      ),
      button(
        cls := "px-3 py-1 bg-slate-700 rounded hover:bg-slate-600",
        "Hard Reset!",
        onClick --> { _ =>
          gameData.DebugHardReset(saveLoad)
        }
      )
    )
  }

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

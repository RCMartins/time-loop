package pt.rcmartins.loop

import com.raquo.laminar.api.L._
import org.scalajs.dom

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

// ---------- Confirm modal state + API ----------
final case class ConfirmConfig(
    title: String,
    message: String,
    confirmText: String = "Delete",
    cancelText: String = "Cancel",
    // Optional: require typing a phrase like "DELETE" to enable confirm
    requirePhrase: Option[String] = None,
    // If true, clicking the backdrop closes
    closeOnBackdrop: Boolean = true,
    // Called when user confirms (can be async). If it fails, modal stays open and shows error.
    onConfirm: () => Future[Unit]
)

final class ConfirmState {
  val cfgVar = Var(Option.empty[ConfirmConfig])
  val busyVar = Var(false)
  val errorVar = Var(Option.empty[String])
  val typedVar = Var("")
  val lastFocusVar = Var(Option.empty[dom.Element])

  val isOpen: Signal[Boolean] = cfgVar.signal.map(_.nonEmpty)

  def open(cfg: ConfirmConfig): Unit = {
    // remember focus so we can restore it
    lastFocusVar.set(Option(dom.document.activeElement).collect { case e: dom.HTMLElement => e })
    typedVar.set("")
    errorVar.set(None)
    busyVar.set(false)
    cfgVar.set(Some(cfg))
    dom.document.body.classList.add("overflow-hidden") // prevent scroll
  }

  def close(): Unit = {
    cfgVar.set(None)
    busyVar.set(false)
    errorVar.set(None)
    typedVar.set("")
    dom.document.body.classList.remove("overflow-hidden")
    // restore focus
    lastFocusVar.now().collect { case e: dom.HTMLElement => e }.foreach(_.focus())
    lastFocusVar.set(None)
  }

  def render: HtmlElement = ConfirmModal(this)

  // internal getters
  def cfgSignal = cfgVar.signal
  def busySignal = busyVar.signal
  def errorSignal = errorVar.signal
  def typedVarRef = typedVar

  def setBusy(b: Boolean) = busyVar.set(b)
  def setError(msg: Option[String]) = errorVar.set(msg)

  def confirm(): Unit = {
    cfgVar.now() match {
      case None      => ()
      case Some(cfg) =>
        setError(None)
        setBusy(true)
        cfg.onConfirm().onComplete {
          case Success(_) =>
            setBusy(false)
            close()
          case Failure(ex) =>
            setBusy(false)
            setError(Some(Option(ex.getMessage).getOrElse("Something went wrong")))
        }
    }
  }

}

// ---------- Modal UI ----------
object ConfirmModal {

  def apply(state: ConfirmState): HtmlElement = {
    // Helpers to find focusable elements inside the modal
    def focusables(root: dom.Element): List[dom.HTMLElement] = {
      val sel =
        "button, [href], input, select, textarea, [tabindex]:not([tabindex='-1'])"
      root
        .querySelectorAll(sel)
        .toArray
        .toList
        .collect { case e: dom.Element => e }
        .filter(e => !e.hasAttribute("disabled") && e.getAttribute("aria-hidden") != "true")
        .map(_.asInstanceOf[dom.HTMLElement])
    }

    // very small focus trap: TAB cycles inside modal content
    def trapTab(root: dom.Element, ev: dom.KeyboardEvent): Unit = {
      if (ev.key != "Tab") return
      val fs = focusables(root)
      if (fs.isEmpty) return
      val first = fs.head
      val last = fs.last
      val active = dom.document.activeElement match {
        case e: dom.Element => e
        case _              => first
      }

      if (ev.shiftKey && active == first) {
        ev.preventDefault()
        last.focus()
      } else if (!ev.shiftKey && active == last) {
        ev.preventDefault()
        first.focus()
      }
    }

    val modalRef = Var(Option.empty[dom.Element])

    // Enable confirm only if:
    // - not busy
    // - and phrase requirement (if any) satisfied
    val canConfirm: Signal[Boolean] =
      state.cfgSignal.combineWithFn(state.busySignal, state.typedVarRef.signal) {
        case (cfgOpt, busy, typed) =>
          cfgOpt.exists { cfg =>
            val phraseOk = cfg.requirePhrase.forall(p => typed.trim == p)
            !busy && phraseOk
          }
      }

    // Outer wrapper: only present when open
    div(
      child <-- state.cfgSignal.map {
        case None => emptyNode

        case Some(cfg) =>
          div(
            // Overlay
            cls := "fixed inset-0 z-50 flex items-center justify-center p-4",

            // Backdrop
            div(
              cls := "absolute inset-0 bg-black/50 backdrop-blur-sm",
              onClick --> { _ =>
                if (cfg.closeOnBackdrop && !state.busySignal.now()) state.close()
              }
            ),

            // Panel
            div(
              cls := "relative w-full max-w-md rounded-2xl bg-white shadow-xl ring-1 ring-black/10 " +
                "dark:bg-slate-900 dark:ring-white/10",
              // store ref for focus trapping
              onMountCallback { ctx =>
                modalRef.set(Some(ctx.thisNode.ref))
                // focus first meaningful control
                dom.window.setTimeout(
                  () => {
                    val root = ctx.thisNode.ref
                    // try to focus cancel first (safer), else first focusable
                    val cancelBtn = root.querySelector("[data-cancel='true']") match {
                      case e: dom.Element => e
                      case _              => null
                    }
                    if (cancelBtn != null) cancelBtn.asInstanceOf[dom.HTMLElement].focus()
                    else focusables(root).headOption.foreach(_.focus())
                  },
                  0
                )
              },
              // Close on ESC, trap TAB
              onKeyDown --> { ev =>
                if (ev.key == "Escape" && !state.busySignal.now()) state.close()
                modalRef.now().foreach(root => trapTab(root, ev))
              },

              // Content
              div(
                cls := "p-6",
                div(
                  cls := "flex items-start gap-3",

                  // Icon (warning)
                  div(
                    cls := "mt-0.5 flex h-10 w-10 items-center justify-center rounded-full " +
                      "bg-red-50 text-red-600 ring-1 ring-red-100 " +
                      "dark:bg-red-500/10 dark:text-red-400 dark:ring-red-500/20",
                    // exclamation mark
                    span(cls := "text-xl font-bold", "!")
                  ),
                  div(
                    cls := "min-w-0 flex-1",
                    h3(
                      cls := "text-lg font-semibold text-slate-900 dark:text-slate-100",
                      cfg.title
                    ),
                    p(cls := "mt-1 text-sm text-slate-600 dark:text-slate-300", cfg.message),

                    // Optional: type-to-confirm
                    child <-- state.cfgSignal.map {
                      case Some(c) if c.requirePhrase.nonEmpty =>
                        val phrase = c.requirePhrase.get
                        div(
                          cls := "mt-4",
                          label(
                            cls := "block text-xs font-medium text-slate-700 dark:text-slate-300",
                            s"Type “$phrase” to confirm"
                          ),
                          input(
                            cls := "mt-1 w-full rounded-lg border border-slate-200 bg-white px-3 py-2 " +
                              "text-sm text-slate-900 shadow-sm outline-none " +
                              "focus:ring-2 focus:ring-red-500/40 focus:border-red-300 " +
                              "dark:bg-slate-950 dark:text-slate-100 dark:border-slate-700",
                            controlled(
                              value <-- state.typedVarRef.signal,
                              onInput.mapToValue --> state.typedVarRef.writer
                            ),
                            placeholder := phrase,
                            disabled <-- state.busySignal
                          )
                        )
                      case _ => emptyNode
                    },

                    // Error message
                    child <-- state.errorSignal.map {
                      case None      => emptyNode
                      case Some(err) =>
                        div(
                          cls := "mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-100 " +
                            "dark:bg-red-500/10 dark:text-red-200 dark:ring-red-500/20",
                          err
                        )
                    }
                  )
                )
              ),

              // Footer buttons
              div(
                cls := "flex items-center justify-end gap-2 border-t border-slate-100 px-6 py-4 " +
                  "dark:border-slate-800",
                button(
                  cls := "rounded-lg px-3 py-2 text-sm font-medium text-slate-700 " +
                    "hover:bg-slate-100 active:bg-slate-200 " +
                    "dark:text-slate-200 dark:hover:bg-slate-800 dark:active:bg-slate-700",
                  "Cancel",
                  dataAttr("cancel") := "true",
                  disabled <-- state.busySignal,
                  onClick --> { _ => state.close() }
                ),
                button(
                  cls := "rounded-lg px-3 py-2 text-sm font-semibold text-white " +
                    "bg-red-600 hover:bg-red-700 active:bg-red-800 " +
                    "disabled:opacity-50 disabled:cursor-not-allowed " +
                    "dark:bg-red-500 dark:hover:bg-red-600",
                  child.text <-- state.busySignal.map(b => if (b) "Working…" else cfg.confirmText),
                  disabled <-- canConfirm.map(!_),
                  onClick --> { _ => state.confirm() }
                )
              )
            )
          )
      }
    )
  }

}

// ---------- Usage example ----------
object ExamplePage {

  private val confirm = new ConfirmState

  val view: HtmlElement = div(
    cls := "p-6 space-y-4",
    h2(cls := "text-xl font-semibold", "Danger zone"),
    button(
      cls := "rounded-lg bg-red-600 px-4 py-2 text-white hover:bg-red-700",
      "Delete account",
      onClick --> { _ =>
        confirm.open(
          ConfirmConfig(
            title = "Delete account",
            message =
              "This permanently deletes your account and all associated data. This cannot be undone.",
            confirmText = "Delete",
            cancelText = "Cancel",
            requirePhrase = Some("DELETE"),
            onConfirm = () =>
              // Replace with your actual async call
              Future {
                dom.console.log("Deleting…")
              }
          )
        )
      }
    ),

    // Render once near the root of your page/app
    confirm.render
  )

}

package pt.rcmartins.loop

import com.raquo.laminar.api.L._
import org.scalajs.dom
import pt.rcmartins.loop.facade.MediaQueryListEvent

object MobileUtils {

  private val isMobileCountVar: Var[Seq[Boolean]] = Var(Seq(false, false, false))
  val isMobileSignal: Signal[Boolean] =
    isMobileCountVar.signal.map(_.exists(identity)).distinct

  def setupMobileLogic(): Unit = {
    Seq(
      dom.window.matchMedia("(pointer: coarse)"),
      dom.window.matchMedia("(hover: none)"),
      dom.window.matchMedia("(max-width: 768px)")
    ).zipWithIndex.foreach { case (mq, index) =>
      // Check initial state
      isMobileCountVar.update(_.updated(index, mq.matches))

      mq.addEventListener(
        "change",
        (e: dom.Event) => {
          val mqlEvent = e.asInstanceOf[MediaQueryListEvent]
          isMobileCountVar.update(_.updated(index, mqlEvent.matches))
        }
      )
    }
  }

}

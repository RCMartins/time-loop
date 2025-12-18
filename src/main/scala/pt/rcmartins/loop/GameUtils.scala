package pt.rcmartins.loop

import com.raquo.airstream.state.Var
import com.raquo.laminar.api.L.EventBus
import pt.rcmartins.loop.GameUtils.Toast

import scala.util.Random

class GameUtils {

  val toastsVar = Var(List.empty[Toast])
  val toastBus = new EventBus[Toast]

  def showToast(msg: String): Unit = {
    val toast = Toast(
      id = Random.nextLong(),
      message = msg
    )
    toastBus.emit(toast)
  }

}

object GameUtils {

  case class Toast(id: Long, message: String)

}

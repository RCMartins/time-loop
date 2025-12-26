package pt.rcmartins.loop.facade

import scala.scalajs.js

@js.native
trait MediaQueryListEvent extends js.Object {
  val matches: Boolean
  val media: String
}

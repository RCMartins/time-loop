package pt.rcmartins.loop.model

import zio.json._

case class Buffs(
    temporary: Seq[(Long, Buff)],
    permanentLoop: Seq[Buff],
    permanentGame: Seq[Buff],
) {

  def insertTemporaryIfPossible(
      buff: Buff,
      timeElapsedMicro: Long,
      durationMicro: Long
  ): Option[Buffs] =
    if (!temporary.exists(_._2.id == buff.id))
      Some(insertTemporary(buff, timeElapsedMicro, durationMicro))
    else
      None

  private def insertTemporary(buff: Buff, timeElapsedMicro: Long, durationMicro: Long): Buffs = {
    copy(
      temporary = (temporary :+ (timeElapsedMicro + durationMicro, buff)).sortBy(_._1),
    )
  }

  def resetForNewLoop: Buffs =
    copy(
      temporary = Seq.empty,
      permanentLoop = Seq.empty,
    )

}

object Buffs {

  val initial: Buffs = Buffs(
    temporary = Seq.empty,
    permanentLoop = Seq.empty,
    permanentGame = Seq.empty,
  )

  implicit val encoder: JsonEncoder[Buffs] = DeriveJsonEncoder.gen[Buffs]
  implicit val decoder: JsonDecoder[Buffs] = DeriveJsonDecoder.gen[Buffs]

}

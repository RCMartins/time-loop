package pt.rcmartins.loop.model

import pt.rcmartins.loop.model.SkillState._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class SkillState(
    kind: ActionKind,
    loopLevel: Int, // 0..N
    loopXPMicro: Long, // current XP in loop level
    permLevel: Int, // 0..N
    permXPMicro: Long, // current XP in permanent level
    initialBonusMultiplier: Double,
    currentBonusMultiplier: Double,
) {

  def nextLoopXP: Long = LoopNextLevelXpCache(loopLevel)
  def nextLoopXPMicro: Long = nextLoopXP * 1_000_000L
  def loopRatio: Double = Math.min(1.0, loopXPMicro.toDouble / nextLoopXPMicro.toDouble)
  private def loopMulti: Double = SkillState.LoopMultiCache(loopLevel)
  def loopXPLong: Long = loopXPMicro / 1_000_000L

  def nextPermXP: Long = PermanentNextLevelXpCache(permLevel)
  def nextPermXPMicro: Long = nextPermXP * 1_000_000L
  def permRatio: Double = Math.min(1.0, permXPMicro.toDouble / nextPermXPMicro.toDouble)
  private def permMulti: Double = SkillState.PermanentMultiCache(permLevel)
  def permXPLong: Long = permXPMicro / 1_000_000L

  def finalSpeedMulti: Double =
    loopMulti * permMulti * currentBonusMultiplier

  def resetLoopProgress: SkillState =
    copy(
      loopLevel = 0,
      loopXPMicro = 0L,
      currentBonusMultiplier = initialBonusMultiplier,
    )

}

object SkillState {

  def initial(kind: ActionKind): SkillState =
    SkillState(
      kind = kind,
      loopLevel = 0,
      loopXPMicro = 0,
      permLevel = 0,
      permXPMicro = 0,
      initialBonusMultiplier = 1.0,
      currentBonusMultiplier = 1.0,
    )

  private val NextLevelXpFactor: Double = 1.02
  private val LoopMultiplier: Double = 1.05
  private val PermanentMultiplier: Double = 1.01

  private val LoopNextLevelXpCache: IndexedSeq[Long] =
    exponentialCalcLong(10, NextLevelXpFactor)

  private val PermanentNextLevelXpCache: IndexedSeq[Long] =
    exponentialCalcLong(25, NextLevelXpFactor)

  private val LoopMultiCache: IndexedSeq[Double] =
    exponentialCalcDouble(1.0, LoopMultiplier)

  private val PermanentMultiCache: IndexedSeq[Double] =
    exponentialCalcDouble(1.0, PermanentMultiplier)

  private def exponentialCalcLong(base: Long, mult: Double): IndexedSeq[Long] = {
    var cache: IndexedSeq[Long] = Vector(base)
    var last: Double = base.toDouble
    for (_ <- 1 to 1000) {
      last *= mult
      cache = cache :+ Math.ceil(last).toLong
    }
    cache
  }

  private def exponentialCalcDouble(base: Double, mult: Double): IndexedSeq[Double] = {
    var cache: IndexedSeq[Double] = Vector(base)
    var last: Double = base
    for (_ <- 1 to 1000) {
      last = last * mult
      cache = cache :+ last
    }
    cache
  }

  implicit val decoder: JsonDecoder[SkillState] = DeriveJsonDecoder.gen[SkillState]
  implicit val encoder: JsonEncoder[SkillState] = DeriveJsonEncoder.gen[SkillState]

}

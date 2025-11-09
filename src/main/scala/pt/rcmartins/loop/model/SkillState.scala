package pt.rcmartins.loop.model

import pt.rcmartins.loop.model.SkillState.NextLevelXpCache

final case class SkillState(
    kind: ActionKind,
    loopLevel: Int, // 0..N
    loopXPMicro: Long, // current XP in this loop level
    permLevel: Int, // 0..N
    permXPMicro: Long, // current XP in this level
) {

  def nextLoopXP: Long = NextLevelXpCache(loopLevel)
  def nextLoopXPMicro: Long = nextLoopXP * 1_000_000L
  def loopRatio: Double = Math.min(1.0, loopXPMicro.toDouble / nextLoopXPMicro.toDouble)
  def loopMulti: Double = SkillState.GenerationMultiCache(loopLevel)
  def loopXPLong: Long = loopXPMicro / 1_000_000L

  def nextPermXP: Long = NextLevelXpCache(permLevel)
  def nextPermXPMicro: Long = nextPermXP * 1_000_000L
  def permRatio: Double = Math.min(1.0, permXPMicro.toDouble / nextPermXPMicro.toDouble)
  def permMulti: Double = SkillState.PermanentMultiCache(permLevel)
  def permXPLong: Long = permXPMicro / 1_000_000L

  def finalSpeedMulti: Double =
    loopMulti * permMulti

}

object SkillState {

  def initial(kind: ActionKind): SkillState =
    SkillState(
      kind = kind,
      loopLevel = 0,
      loopXPMicro = NextLevelXpCache(0),
      permLevel = 0,
      permXPMicro = NextLevelXpCache(0),
    )

  private val NextLevelXpFactor: Double = 1.02
  private val GenerationMulti: Double = 1.05
  private val PermanentMult: Double = 1.01

  val NextLevelXpCache: IndexedSeq[Long] = exponentialCalcLong(25, NextLevelXpFactor)
  val GenerationMultiCache: IndexedSeq[Double] = exponentialCalcDouble(1.0, GenerationMulti)
  val PermanentMultiCache: IndexedSeq[Double] = exponentialCalcDouble(1.0, PermanentMult)

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

}

package stryker4s.model

import stryker4s.api.testprocess.Fingerprint

import scala.concurrent.duration.FiniteDuration

sealed trait InitialTestRunResult {
  def isSuccessful: Boolean

  /** Initial testruns can report their own taken duration, or a timed one will be taken as a backup if this is `
    */
  def reportedDuration: Option[FiniteDuration] = None
}

final case class InitialTestRunCoverageReport(
    isSuccessful: Boolean,
    firstRun: Map[Int, Seq[Fingerprint]],
    secondRun: Map[Int, Seq[Fingerprint]],
    duration: FiniteDuration
) extends InitialTestRunResult {
  override def reportedDuration: Option[FiniteDuration] = Some(duration)
}

final case class NoCoverageInitialTestRun(isSuccessful: Boolean) extends InitialTestRunResult

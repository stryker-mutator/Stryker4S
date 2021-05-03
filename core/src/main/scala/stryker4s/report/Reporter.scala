package stryker4s.report

import better.files.File
import cats.effect.IO
import cats.syntax.applicative._
import fs2.{INothing, Pipe}
import mutationtesting._
import stryker4s.config.Config

import scala.concurrent.duration.FiniteDuration

abstract class Reporter {
  def mutantTested: Pipe[IO, MutantTestedEvent, INothing] = in => in.drain
  def onRunFinished(runReport: FinishedRunEvent): IO[Unit] = runReport.pure[IO].void
}

final case class MutantTestedEvent(totalMutants: Int)

final case class FinishedRunEvent(
    report: MutationTestResult[Config],
    metrics: MetricsResult,
    duration: FiniteDuration,
    reportsLocation: File
)

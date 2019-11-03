package stryker4s.report

import grizzled.slf4j.Logging
import mutationtesting.{MetricsResult, MutationTestReport}
import stryker4s.config._
import stryker4s.files.DiskFileIO
import stryker4s.model.{Mutant, MutantRunResult}

import scala.util.{Failure, Try}

class Reporter(implicit config: Config) extends FinishedRunReporter with ProgressReporter with Logging {
  lazy val reporters: Seq[MutationRunReporter] = config.reporters collect {
    case Console             => new ConsoleReporter()
    case Html                => new HtmlReporter(DiskFileIO)
    case Json                => new JsonReporter(DiskFileIO)
    case Dashboard(reporter) => reporter
  }

  private[this] val progressReporters = reporters collect { case r: ProgressReporter       => r }
  private[this] val finishedRunReporters = reporters collect { case r: FinishedRunReporter => r }

  override def reportMutationStart(mutant: Mutant): Unit =
    progressReporters.foreach(_.reportMutationStart(mutant))

  override def reportMutationComplete(result: MutantRunResult, totalMutants: Int): Unit =
    progressReporters.foreach(_.reportMutationComplete(result, totalMutants))

  override def reportRunFinished(report: MutationTestReport, metrics: MetricsResult): Unit = {
    val reported = finishedRunReporters.map(reporter => Try(reporter.reportRunFinished(report, metrics)))
    val failed = reported.collect({ case f: Failure[Unit] => f })
    if (failed.nonEmpty) {
      warn(s"${failed.length} reporter(s) failed to report:")
      failed.map(_.exception).foreach(warn(_))
    }
  }
}

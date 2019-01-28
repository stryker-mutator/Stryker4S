package stryker4s.run.process

import java.nio.file.Path

import better.files.File
import stryker4s.config.Config
import stryker4s.model._
import stryker4s.mutants.findmutants.SourceCollector
import stryker4s.run.MutantRunner

import scala.concurrent.TimeoutException
import scala.util.{Failure, Success}

class ProcessMutantRunner(command: Command, processRunner: ProcessRunner, sourceCollector: SourceCollector)(
    implicit config: Config)
    extends MutantRunner(processRunner, sourceCollector) {

  def runMutant(mutant: Mutant, workingDir: File, subPath: Path): MutantRunResult = {
    val id = mutant.id
    info(s"Starting test-run ${id + 1}...")
    processRunner(command, workingDir, ("ACTIVE_MUTATION", id.toString)) match {
      case Success(0)                         => Survived(mutant, subPath)
      case Success(exitCode) if exitCode != 0 => Killed(mutant, subPath)
      case Failure(exc: TimeoutException)     => TimedOut(exc, mutant, subPath)
    }
  }

  override def runInitialTest(workingDir: File): Boolean = {
    processRunner(command, workingDir, ("ACTIVE_MUTATION", "None")) match {
      case Success(0)                         => true
      case Success(exitCode) if exitCode != 0 => false
      case Failure(exc: TimeoutException)     => false
    }
  }
}

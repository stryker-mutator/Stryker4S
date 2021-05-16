package stryker4s.command.runner

import better.files.File
import cats.effect.IO
import stryker4s.model._
import stryker4s.run.TestRunner
import stryker4s.run.process.{Command, ProcessRunner}

import scala.concurrent.TimeoutException
import scala.util.{Failure, Success}

class ProcessTestRunner(command: Command, processRunner: ProcessRunner, tmpDir: File) extends TestRunner {
  def initialTestRun(): IO[InitialTestRunResult] = {
    processRunner(command, tmpDir, List.empty[(String, String)]: _*).map {
      case Success(0) => NoCoverageInitialTestRun(true)
      case _          => NoCoverageInitialTestRun(false)
    }
  }

  def runMutant(mutant: Mutant): IO[MutantRunResult] = {
    val id = mutant.id
    processRunner(command, tmpDir, ("ACTIVE_MUTATION", id.toString)).map {
      case Success(0)                   => Survived(mutant)
      case Success(_)                   => Killed(mutant)
      case Failure(_: TimeoutException) => TimedOut(mutant)
      case _                            => Error(mutant)
    }
  }

}

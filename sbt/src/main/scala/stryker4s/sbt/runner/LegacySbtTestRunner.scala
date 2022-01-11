package stryker4s.sbt.runner

import cats.effect.IO
import sbt.Keys.*
import sbt.Tests.Output
import sbt.*
import stryker4s.extension.exception.InitialTestRunFailedException
import stryker4s.log.Logger
import stryker4s.model.*
import stryker4s.run.TestRunner

class LegacySbtTestRunner(initialState: State, settings: Seq[Def.Setting[?]], extracted: Extracted)(implicit
    log: Logger
) extends TestRunner {
  def initialTestRun(): IO[InitialTestRunResult] = runTests(
    initialState,
    throw InitialTestRunFailedException(
      s"Unable to execute initial test run. Sbt is unable to find the task 'test'."
    ),
    onSuccess = NoCoverageInitialTestRun(true),
    onFailed = NoCoverageInitialTestRun(false)
  )

  def runMutant(mutant: Mutant, testNames: Seq[String]): IO[MutantRunResult] = {
    val mutationState =
      extracted.appendWithSession(settings :+ mutationSetting(mutant.id.globalId), initialState)
    runTests(
      mutationState,
      onError = {
        log.error(s"An unexpected error occurred while running mutation ${mutant.id}")
        Error(mutant)
      },
      onSuccess = Survived(mutant),
      onFailed = Killed(mutant)
    )
  }

  private def runTests[T](state: State, onError: => T, onSuccess: => T, onFailed: => T): IO[T] =
    IO(Project.runTask(Test / executeTests, state)) map {
      case Some((_, Value(Output(TestResult.Passed, _, _)))) => onSuccess
      case Some((_, Value(Output(TestResult.Failed, _, _)))) => onFailed
      case Some((_, Value(Output(TestResult.Error, _, _))))  => onFailed
      case _                                                 => onError
    }

  private def mutationSetting(mutation: Int): Def.Setting[?] =
    Test / javaOptions += s"-DACTIVE_MUTATION=${String.valueOf(mutation)}"
}

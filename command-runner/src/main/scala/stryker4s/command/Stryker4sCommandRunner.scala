package stryker4s.command

import cats.data.NonEmptyList
import cats.effect.{Deferred, IO, Resource}
import stryker4s.command.config.ProcessRunnerConfig
import stryker4s.command.runner.ProcessTestRunner
import stryker4s.config.Config
import stryker4s.log.Logger
import stryker4s.mutants.applymutants.ActiveMutationContext
import stryker4s.mutants.applymutants.ActiveMutationContext.ActiveMutationContext
import stryker4s.run.process.ProcessRunner
import stryker4s.run.{Stryker4sRunner, TestRunner}

import java.nio.file.Path
import scala.concurrent.duration.FiniteDuration

class Stryker4sCommandRunner(processRunnerConfig: ProcessRunnerConfig, timeout: Deferred[IO, FiniteDuration])(implicit
    log: Logger
) extends Stryker4sRunner {
  override def mutationActivation(implicit config: Config): ActiveMutationContext = ActiveMutationContext.envVar

  override def resolveTestRunners(
      tmpDir: Path
  )(implicit config: Config): Resource[IO, NonEmptyList[TestRunner]] = {
    val innerTestRunner =
      Resource.pure[IO, TestRunner](new ProcessTestRunner(processRunnerConfig.testRunner, ProcessRunner(), tmpDir))

    val withTimeout = TestRunner.timeoutRunner(timeout, innerTestRunner)

    withTimeout.map(NonEmptyList.of(_))
  }
}

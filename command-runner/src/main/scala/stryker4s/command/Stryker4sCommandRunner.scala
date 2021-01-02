package stryker4s.command

import java.nio.file.Path

import cats.effect.{ContextShift, IO, Resource, Timer}
import stryker4s.command.config.ProcessRunnerConfig
import stryker4s.command.runner.ProcessTestRunner
import stryker4s.config.Config
import stryker4s.log.Logger
import stryker4s.mutants.applymutants.ActiveMutationContext
import stryker4s.mutants.applymutants.ActiveMutationContext.ActiveMutationContext
import stryker4s.run.process.ProcessRunner
import stryker4s.run.{Stryker4sRunner, TestRunner}

class Stryker4sCommandRunner(processRunnerConfig: ProcessRunnerConfig)(implicit
    log: Logger,
    timer: Timer[IO],
    cs: ContextShift[IO]
) extends Stryker4sRunner {
  override def mutationActivation(implicit config: Config): ActiveMutationContext = ActiveMutationContext.envVar

  override def resolveTestRunner(tmpDir: Path)(implicit config: Config): Resource[IO, TestRunner] =
    Resource.pure[IO, TestRunner](new ProcessTestRunner(processRunnerConfig.testRunner, ProcessRunner(), tmpDir))
}

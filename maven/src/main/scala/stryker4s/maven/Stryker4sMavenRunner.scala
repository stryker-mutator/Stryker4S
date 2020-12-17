package stryker4s.maven

import cats.effect.{ContextShift, IO, Timer}
import org.apache.maven.project.MavenProject
import org.apache.maven.shared.invoker.{DefaultInvoker, Invoker}
import stryker4s.config.Config
import stryker4s.maven.runner.MavenMutantRunner
import stryker4s.mutants.applymutants.ActiveMutationContext.{envVar, ActiveMutationContext}
import stryker4s.mutants.findmutants.SourceCollector
import stryker4s.report.Reporter
import stryker4s.run.{MutantRunner, Stryker4sRunner}
import stryker4s.log.Logger

class Stryker4sMavenRunner(project: MavenProject)(implicit log: Logger, timer: Timer[IO], cs: ContextShift[IO])
    extends Stryker4sRunner {
  override def resolveRunner(collector: SourceCollector, reporter: Reporter)(implicit config: Config): MutantRunner =
    new MavenMutantRunner(project, resolveInvoker(), collector, reporter)

  override def mutationActivation(implicit config: Config): ActiveMutationContext = envVar

  private def resolveInvoker(): Invoker = new DefaultInvoker
}

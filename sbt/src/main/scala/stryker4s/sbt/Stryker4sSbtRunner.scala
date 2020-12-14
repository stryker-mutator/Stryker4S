package stryker4s.sbt

import cats.effect.{ContextShift, IO, Timer}
import sbt._
import stryker4s.config.Config
import stryker4s.log.Logger
import stryker4s.mutants.applymutants.ActiveMutationContext
import stryker4s.mutants.applymutants.ActiveMutationContext.ActiveMutationContext
import stryker4s.mutants.findmutants.SourceCollector
import stryker4s.report.Reporter
import stryker4s.run.{MutantRunner, Stryker4sRunner}
import stryker4s.sbt.runner.SbtMutantRunner
import stryker4s.mutants.applymutants.MatchBuilder
import stryker4s.mutants.applymutants.CoverageMatchBuilder

/** This Runner run Stryker mutations in a single SBT session
  *
  * @param state SBT project state (contains all the settings about the project)
  */
class Stryker4sSbtRunner(state: State)(implicit log: Logger, timer: Timer[IO], cs: ContextShift[IO])
    extends Stryker4sRunner {
  override def resolveRunner(collector: SourceCollector, reporter: Reporter)(implicit config: Config): MutantRunner =
    new SbtMutantRunner(state, collector, reporter)

  override def resolveMatchBuilder(implicit config: Config): MatchBuilder =
    if (config.legacyTestRunner) new MatchBuilder(mutationActivation) else new CoverageMatchBuilder(mutationActivation)

  override val mutationActivation: ActiveMutationContext = ActiveMutationContext.sysProps
}

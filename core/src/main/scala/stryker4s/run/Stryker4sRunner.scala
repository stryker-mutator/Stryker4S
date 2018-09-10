package stryker4s.run

import grizzled.slf4j.Logging
import stryker4s.Stryker4s
import stryker4s.config.{CommandRunner, Config, ConfigReader}
import stryker4s.mutants.Mutator
import stryker4s.mutants.applymutants.{MatchBuilder, StatementTransformer}
import stryker4s.mutants.findmutants.{FileCollector, MutantFinder, MutantMatcher}
import stryker4s.run.process.{Command, ProcessRunner}
import stryker4s.run.report.Reporter

object Stryker4sRunner extends App with Logging {

  implicit val config: Config = ConfigReader.readConfig()

  val stryker4s = new Stryker4s(
    new FileCollector,
    new Mutator(new MutantFinder(new MutantMatcher),
                new StatementTransformer,
                new MatchBuilder),
    resolveRunner(),
    new Reporter()
  )

  stryker4s.run()

  private def resolveRunner()(implicit config: Config): MutantRunner = {
    config.testRunner match {
      case CommandRunner(command, args) =>
        new ProcessMutantRunner(Command(command, args), ProcessRunner.resolveRunner())
    }
  }
}

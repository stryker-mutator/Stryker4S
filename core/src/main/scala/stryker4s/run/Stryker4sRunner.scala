package stryker4s.run

import stryker4s.Stryker4s
import stryker4s.config.{CommandRunner, Config, ConfigReader}
import stryker4s.mutants.Mutator
import stryker4s.mutants.applymutants.{MatchBuilder, StatementTransformer}
import stryker4s.mutants.findmutants.{FileCollector, MutantFinder, MutantMatcher}
import stryker4s.run.process.{Command, ProcessMutantRunner, ProcessRunner}
import stryker4s.run.report.Reporter
import stryker4s.run.threshold.ScoreStatus

import scala.meta.internal.tokenizers.PlatformTokenizerCache

class Stryker4sRunner {

  implicit val config: Config = ConfigReader.readConfig()

  def run(): ScoreStatus = {

    // Scalameta uses a cache file->tokens that exists at a process level
     // if one file changes between runs (in the same process, eg a single SBT session) could lead to an error, so
     // it is cleaned before it starts.
    PlatformTokenizerCache.megaCache.clear()

    val stryker4s = new Stryker4s(
      new FileCollector,
      new Mutator(new MutantFinder(new MutantMatcher), new StatementTransformer, new MatchBuilder),
      resolveRunner(),
      new Reporter()
    )
    stryker4s.run()
  }

  def resolveRunner()(implicit config: Config): MutantRunner = config.testRunner match {
    case CommandRunner(command, args) => new ProcessMutantRunner(Command(command, args), ProcessRunner.resolveRunner())
  }

}

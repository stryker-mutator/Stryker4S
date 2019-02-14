package stryker4s.config

import better.files._
import org.apache.logging.log4j.Level
import pureconfig.ConfigWriter

case class Config(mutate: Seq[String] = Seq("**/main/scala/**/*.scala"),
                  baseDir: File = File.currentWorkingDirectory,
                  testRunner: TestRunner = CommandRunner("sbt", "test"),
                  reporters: List[Reporter] = List(ConsoleReporter()),
                  logLevel: Level = Level.INFO,
                  files: Option[Seq[String]] = None,
                  excludedMutations: ExcludedMutations = ExcludedMutations(),
                  thresholds: Thresholds = Thresholds()) {

  def toHoconString: String = {
    import stryker4s.config.implicits.ConfigWriterImplicits._

    ConfigWriter[Config].to(this).render(options)
  }
}

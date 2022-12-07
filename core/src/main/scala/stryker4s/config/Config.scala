package stryker4s.config

import fs2.io.file.Path

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.meta.{dialects, Dialect}

final case class Config(
    mutate: Seq[String] = Seq.empty,
    testFilter: Seq[String] = Seq.empty,
    baseDir: Path = Path("").absolute,
    reporters: Set[ReporterType] = Set(Console, Html),
    files: Seq[String] = Seq.empty,
    excludedMutations: Config.ExcludedMutations = Set.empty,
    thresholds: Thresholds = Thresholds(),
    dashboard: DashboardOptions = DashboardOptions(),
    timeout: FiniteDuration = FiniteDuration(5000, TimeUnit.MILLISECONDS),
    timeoutFactor: Double = 1.5,
    maxTestRunnerReuse: Option[Int] = None,
    legacyTestRunner: Boolean = false,
    scalaDialect: Dialect = dialects.Scala3,
    concurrency: Int = Config.defaultConcurrency,
    debug: DebugOptions = DebugOptions(),
    staticTmpDir: Boolean = false,
    cleanTmpDir: Boolean = true
)

object Config extends pure.ConfigConfigReader with circe.ConfigEncoder {

  private def defaultConcurrency: Int = concurrencyFor(Runtime.getRuntime().availableProcessors())

  def concurrencyFor(cpuCoreCount: Int) = {
    // Use (n / 4 concurrency, rounded) + 1
    if (cpuCoreCount > 4) cpuCoreCount / 2
    else cpuCoreCount
    (cpuCoreCount.toDouble / 4).round.toInt + 1
  }

  /** Type alias for `Set[String]` so extra validation can be done
    */
  type ExcludedMutations = Set[String]

  lazy val default: Config = Config()
}

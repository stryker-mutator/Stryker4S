package stryker4s.config

import cats.effect.Async
import cats.syntax.all.*
import ciris.*
import fs2.io.file.Files
import stryker4s.config.codec.CirisConfigDecoders
import stryker4s.config.source.ConfigSource
import stryker4s.log.Logger

private class ConfigLoader[F[_]](source: ConfigSource[F]) extends CirisConfigDecoders {

  def thresholds: ConfigValue[F, Thresholds] = (
    source.thresholdsHigh,
    source.thresholdsLow,
    source.thresholdsBreak
  ).parMapN(Thresholds.apply).as(validateThresholds)

  def dashboard: ConfigValue[F, DashboardOptions] = (
    source.dashboardBaseUrl,
    source.dashboardReportType,
    source.dashboardProject,
    source.dashboardVersion,
    source.dashboardModule
  ).parMapN(DashboardOptions.apply)

  def debug: ConfigValue[F, DebugOptions] = (
    source.debugLogTestRunnerStdout,
    source.debugDebugTestRunner
  ).parMapN(DebugOptions.apply)

  def config: ConfigValue[F, Config] = (
    source.mutate,
    source.testFilter,
    source.baseDir,
    source.reporters,
    source.files,
    source.excludedMutations,
    thresholds,
    dashboard,
    source.timeout,
    source.timeoutFactor,
    source.maxTestRunnerReuse,
    source.legacyTestRunner,
    source.scalaDialect,
    source.concurrency,
    debug,
    source.staticTmpDir,
    source.cleanTmpDir
  ).parMapN(Config.apply)

}

object ConfigLoader {

  def load[F[_]: Async](source: ConfigSource[F])(implicit log: Logger) =
    Async[F].delay(log.info(s"Loading config...")) *>
      new ConfigLoader(source).config.load[F]

  def loadAll[F[_]: Async: Files](extraConfigSources: List[ConfigSource[F]])(implicit log: Logger): F[Config] = for {
    aggregated <- ConfigSource.aggregate[F](extraConfigSources)
    withDefaults = aggregated.withDefaults
    config <- ConfigLoader.load[F](withDefaults)
  } yield config

}

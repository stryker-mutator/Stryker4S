package stryker4s.config.implicits

import java.nio.file.Path

import better.files.File
import pureconfig.ConfigReader
import stryker4s.config.{ConsoleReporterType, ExcludedMutations, ReporterType}

trait ConfigReaderImplicits {

  /** Converts a [[java.nio.file.Path]] to a [[better.files.File]] so PureConfig can read it
    *
    */
  private[config] implicit val toFileReader: ConfigReader[File] =
    ConfigReader[Path] map (p => File(p))

  private[config] implicit val toReporterList: ConfigReader[ReporterType] =
    ConfigReader[String] map {
      case ConsoleReporterType.name => ConsoleReporterType
    }

  private[config] implicit val exclusions: ConfigReader[ExcludedMutations] =
    ConfigReader[List[String]] map (exclusions => ExcludedMutations(exclusions.toSet))
}

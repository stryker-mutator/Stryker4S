package stryker4s.config.implicits

import java.nio.file.Path

import better.files.File
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.semiauto._
import stryker4s.config._
import stryker4s.extension.mutationtype.Mutation

/** Conversions of custom case classes or enums so PureConfig can read it.
  * @example `toFileReader` makes PureConfig able to read `better.files.File` from a `java.nio.file.Path`
  */
trait ConfigReaderImplicits {

  implicit private[config] val fileReader: ConfigReader[File] =
    ConfigReader[Path] map (p => File(p))

  implicit private[config] val reporterReader: ConfigReader[ReporterType] =
    deriveEnumerationReader[ReporterType]

  implicit private[config] val dashboardReportTypeReader: ConfigReader[DashboardReportType] =
    deriveEnumerationReader[DashboardReportType]

  implicit private[config] val exclusionsReader: ConfigReader[Config.ExcludedMutations] =
    ConfigReader[List[String]] emap { exclusions =>
      val (valid, invalid) = exclusions.partition(Mutation.mutations.contains)
      if (invalid.nonEmpty)
        Left(
          CannotConvert(
            exclusions.mkString(", "),
            s"excluded-mutations",
            s"invalid option(s) '${invalid.mkString(", ")}'. Valid exclusions are '${Mutation.mutations.mkString(", ")}'"
          )
        )
      else
        Right(valid.toSet)
    }

  implicit private[config] val uriReader = pureconfig.module.sttp.reader

  implicit private[config] val thresholdsReader: ConfigReader[Thresholds] = deriveReader[Thresholds] emap {
    case Thresholds(high, _, _) if isNotPercentage(high)   => notPercentageError(high, "high")
    case Thresholds(_, low, _) if isNotPercentage(low)     => notPercentageError(low, "low")
    case Thresholds(_, _, break) if isNotPercentage(break) => notPercentageError(break, "break")
    case Thresholds(high, low, _) if high < low =>
      Left(
        CannotConvert(
          high.toString(),
          "thresholds.high",
          s"'high' ($high) must be greater than or equal to 'low' ($low)"
        )
      )
    case Thresholds(_, low, break) if low <= break =>
      Left(
        CannotConvert(
          low.toString(),
          "thresholds.low",
          s"'low' ($low) must be greater than 'break' ($break)"
        )
      )
    case valid => Right(valid)
  }

  private def isNotPercentage(n: Int) = n < 0 || n > 100

  /** Helper function to create a CannotConvert when a value is not a percentage
    */
  private def notPercentageError(value: Int, name: String): Left[CannotConvert, Thresholds] =
    Left(CannotConvert(value.toString(), s"thresholds.$name", "must be a percentage 0-100"))

}

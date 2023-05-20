package stryker4s.config.pure

import cats.syntax.either.*
import fs2.io.file.Path
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.semiauto.*
import stryker4s.config.*
import stryker4s.extension.mutationtype.Mutation

import java.nio.file.Path as JPath
import scala.meta.{dialects, Dialect}

/** Conversions of custom case classes or enums so PureConfig can read it.
  *
  * @example
  *   `pathReader` makes PureConfig able to read `fs2.io.file.Path` from a `java.nio.file.Path`
  */
trait ConfigConfigReader {

  implicit def pathReader: ConfigReader[Path] =
    ConfigReader[JPath].map(Path.fromNioPath).map(_.absolute)

  implicit def reporterReader: ConfigReader[ReporterType] =
    deriveEnumerationReader[ReporterType]

  implicit def dashboardReportTypeReader: ConfigReader[DashboardReportType] =
    deriveEnumerationReader[DashboardReportType]

  implicit def exclusionsReader: ConfigReader[Config.ExcludedMutations] =
    ConfigReader[List[String]] emap { exclusions =>
      val (valid, invalid) = exclusions.partition(Mutation.mutations.contains)
      if (invalid.nonEmpty)
        CannotConvert(
          exclusions.mkString(", "),
          s"excluded-mutations",
          s"invalid option(s) '${invalid.mkString(", ")}'. Valid exclusions are '${Mutation.mutations.mkString(", ")}'"
        ).asLeft
      else
        valid.toSet.asRight
    }

  implicit def uriReader = _root_.pureconfig.module.sttp.reader

  implicit def thresholdsReader: ConfigReader[Thresholds] = {
    def isNotPercentage(n: Int) = n < 0 || n > 100

    def notPercentageError(value: Int, name: String): Either[CannotConvert, Thresholds] =
      CannotConvert(value.toString(), s"thresholds.$name", "must be a percentage 0-100").asLeft

    deriveReader[Thresholds] emap {
      case Thresholds(high, _, _) if isNotPercentage(high)   => notPercentageError(high, "high")
      case Thresholds(_, low, _) if isNotPercentage(low)     => notPercentageError(low, "low")
      case Thresholds(_, _, break) if isNotPercentage(break) => notPercentageError(break, "break")
      case Thresholds(high, low, _) if high < low =>
        CannotConvert(
          high.toString(),
          "thresholds.high",
          s"'high' ($high) must be greater than or equal to 'low' ($low)"
        ).asLeft
      case Thresholds(_, low, break) if low <= break =>
        CannotConvert(
          low.toString(),
          "thresholds.low",
          s"'low' ($low) must be greater than 'break' ($break)"
        ).asLeft
      case valid => valid.asRight
    }
  }

  implicit def dialectReader: ConfigReader[Dialect] = {
    val deprecatedVersions = List("scala211", "scala2.11", "2.11", "211")

    val scalaVersions = Map(
      List("scala212", "scala2.12", "2.12", "212") -> dialects.Scala212,
      List("scala212source3") -> dialects.Scala212Source3,
      List("scala213", "scala2.13", "2.13", "213", "2") -> dialects.Scala213,
      List("scala213source3", "source3") -> dialects.Scala213Source3,
      List("scala3future", "future") -> dialects.Scala3Future,
      List("scala30", "scala3.0", "3.0", "30", "dotty") -> dialects.Scala30,
      List("scala31", "scala3.1", "3.1", "31") -> dialects.Scala31,
      List("scala32", "scala3.2", "3.2", "32") -> dialects.Scala32,
      List("scala33", "scala3.3", "3.3", "33") -> dialects.Scala33,
      List("scala3", "3") -> dialects.Scala3
    )

    ConfigReader[String].emap { input =>
      def toCannotConvert(msg: String) = {
        val invalidDialectString =
          s"Leaving this configuration empty defaults to scala213source3 which might also work for you. Valid scalaDialects are: ${scalaVersions.keys.flatten
              .map(d => s"'$d'")
              .mkString(", ")}"
        CannotConvert(input, "scala-dialect", s"$msg. $invalidDialectString")
      }

      if (deprecatedVersions.contains(input))
        toCannotConvert("Deprecated dialect").asLeft
      else
        scalaVersions
          .collectFirst { case (strings, dialect) if strings.contains(input.toLowerCase()) => dialect }
          .toRight(toCannotConvert("Unsupported dialect"))
    }
  }

}

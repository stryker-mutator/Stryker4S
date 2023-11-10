package stryker4s.mutants.findmutants

import cats.effect.IO
import cats.syntax.either.*
import fs2.io.file.Path
import stryker4s.config.Config
import stryker4s.extension.FileExtensions.*
import stryker4s.log.Logger

import scala.meta.parsers.XtensionParseInputLike
import scala.meta.{Dialect, Parsed, Source}

class MutantFinder()(implicit config: Config, log: Logger) {

  def parseFile(file: Path): IO[Source] = {
    implicit val dialect: Dialect = config.scalaDialect

    IO(file.toNioPath.parse[Source]).flatMap {
      case e: Parsed.Error =>
        log.error(s"Error while parsing file '${file.relativePath}', ${e.message}")
        IO.raiseError(e.details)
      case s => IO.fromEither(s.toEither.leftMap(_.details))
    }
  }
}

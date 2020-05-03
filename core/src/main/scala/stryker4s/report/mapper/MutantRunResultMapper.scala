package stryker4s.report.mapper
import java.nio.file.Path

import mutationtesting.MutantStatus.MutantStatus
import mutationtesting._
import stryker4s.config.{Config, Thresholds => ConfigThresholds}
import stryker4s.model._

trait MutantRunResultMapper {
  protected[report] def toReport(results: Iterable[MutantRunResult])(implicit config: Config): MutationTestReport =
    MutationTestReport(
      thresholds = toThresholds(config.thresholds),
      files = toMutationTestResultMap(results.toSeq)
    )

  private def toThresholds(thresholds: ConfigThresholds): Thresholds =
    Thresholds(high = thresholds.high, low = thresholds.low)

  private def toMutationTestResultMap(
      results: Seq[MutantRunResult]
  )(implicit config: Config): Map[String, MutationTestResult] =
    results groupBy (_.fileSubPath) map {
      case (path, runResults) => path.toString.replace('\\', '/') -> toMutationTestResult(runResults)
    }

  private def toMutationTestResult(runResults: Seq[MutantRunResult])(implicit config: Config): MutationTestResult =
    MutationTestResult(
      fileContentAsString(runResults.head.fileSubPath),
      runResults.map(toMutantResult)
    )

  private def toMutantResult(runResult: MutantRunResult): MutantResult = {
    val mutant = runResult.mutant
    MutantResult(
      mutant.id.toString,
      mutant.mutationType.mutationName,
      mutant.mutated.syntax,
      toLocation(mutant.original.pos),
      toMutantStatus(runResult)
    )
  }

  private def toLocation(pos: scala.meta.inputs.Position): Location =
    Location(
      start = Position(line = pos.startLine + 1, column = pos.startColumn + 1),
      end = Position(line = pos.endLine + 1, column = pos.endColumn + 1)
    )

  private def toMutantStatus(mutant: MutantRunResult): MutantStatus =
    mutant match {
      case _: Survived   => MutantStatus.Survived
      case _: Killed     => MutantStatus.Killed
      case _: NoCoverage => MutantStatus.NoCoverage
      case _: TimedOut   => MutantStatus.Timeout
      case _: Error      => MutantStatus.CompileError
    }

  private def fileContentAsString(path: Path)(implicit config: Config): String =
    (config.baseDir / path.toString).contentAsString
}

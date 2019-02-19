package stryker4s.run.report
import stryker4s.config.Config
import stryker4s.model.MutantRunResults
import stryker4s.run.report.mapper.MutantRunResultMapper

class HtmlReporter extends MutantRunReporter with MutantRunResultMapper {

  override def report(runResults: MutantRunResults)(implicit config: Config): Unit = {
    val mapped = toJsonReport(runResults)



      println(mapped)
  }
}

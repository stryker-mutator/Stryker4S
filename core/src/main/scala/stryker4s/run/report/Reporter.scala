package stryker4s.run.report
import grizzled.slf4j.Logging
import stryker4s.config.Config
import stryker4s.model.MutantRunResults

class Reporter extends Logging{

  /**
    * Generate a report for each reporter that is available.
    */
  def report(runResult: MutantRunResults)(implicit config: Config): Unit = {
    config.reporters.foreach { _.report(runResult) }
  }

  def determineExitCode(runResults: MutantRunResults)(implicit config: Config): Int = {
    config.thresholds.break match {
      case Some(threshold) if runResults.mutationScore < threshold => {
        error(s"Mutation score below threshold! Score: ${runResults.mutationScore}. Threshold: $threshold")
        1
      }
      case _ => {
        debug("No breaking threshold configured. Won\'t fail the build no matter how low your mutation score is. Set `thresholds.break` to change this behavior.")
        0
      }
    }
  }
}

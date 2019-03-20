package stryker4s.report
import better.files.Resource
import stryker4s.config.Config
import stryker4s.model.MutantRunResults
import stryker4s.report.mapper.MutantRunResultMapper

class HtmlReporter(implicit config: Config) extends FinishedRunReporter with MutantRunResultMapper {

  private val reportVersion = "1.0.1"

  def indexHtml(json: String): String = {
    val mutationTestElementsScript = Resource.getAsString(
      s"META-INF/resources/webjars/mutation-testing-elements/$reportVersion/dist/mutation-test-elements.js")

    s"""<!DOCTYPE html>
       |<html>
       |<body>
       |  <mutation-test-report-app title-postfix="Stryker4s report"></mutation-test-report-app>
       |  <script>
       |    document.querySelector('mutation-test-report-app').report = $json
       |  </script>
       |  <script>
       |    $mutationTestElementsScript
       |  </script>
       |</body>
       |</html>""".stripMargin
  }

  override def reportRunFinished(runResults: MutantRunResults): Unit = {
    val mapped = toReport(runResults).toJson

    println(mapped)
  }
}

package stryker4s.report
import grizzled.slf4j.Logging
import stryker4s.config.Config
import stryker4s.files.FileIO
import stryker4s.model.MutantRunResults
import stryker4s.report.mapper.MutantRunResultMapper

class HtmlReporter(fileIO: FileIO)(implicit config: Config)
    extends FinishedRunReporter
    with MutantRunResultMapper
    with Logging {

  private val reportVersion = "1.0.2"
  private val htmlReportResource =
    s"META-INF/resources/webjars/mutation-testing-elements/$reportVersion/dist/mutation-test-elements.js"
  private val title = "Stryker4s report"

  def indexHtml(json: String): Iterator[Char] = {
    val mutationTestElementsScript = fileIO.readResource(htmlReportResource)

    val startHtml = s"""<!DOCTYPE html>
       |<html>
       |<body>
       |  <mutation-test-report-app title-postfix="$title"></mutation-test-report-app>
       |  <script>
       |    document.querySelector('mutation-test-report-app').report = """.stripMargin
    val middleHtml = s"""
       |  </script>
       |  <script>
       |    """.stripMargin
    val endHtml = s"""
       |  </script>
       |</body>
       |</html>""".stripMargin

    startHtml.iterator ++
      json ++
      middleHtml ++
      mutationTestElementsScript ++
      endHtml
  }

  override def reportRunFinished(runResults: MutantRunResults): Unit = {
    val mapped = toReport(runResults).toJson

    val targetLocation = config.baseDir / s"target/stryker4s-report-${System.currentTimeMillis()}" / "index.html"
    val reportContent = indexHtml(mapped)
    fileIO.createAndWrite(targetLocation, reportContent)

    debug(s"Written HTML report to $targetLocation")
  }
}

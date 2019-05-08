package stryker4s.report

import grizzled.slf4j.Logging
import stryker4s.config.Config
import stryker4s.files.FileIO
import stryker4s.model.MutantRunResults
import stryker4s.report.mapper.MutantRunResultMapper

import scala.io.Source

class HtmlReporter(fileIO: FileIO)(implicit config: Config)
    extends FinishedRunReporter
    with MutantRunResultMapper
    with Logging {

  private val title = "Stryker4s report"
  private val mutationTestElementsName = "mutation-test-elements.js"
  private val htmlReportResource = s"mutation-testing-elements/$mutationTestElementsName"
  private val reportFilename = "report.js"

  val indexHtml: String =
    s"""<!DOCTYPE html>
       |<html>
       |<head>
       |  <script src="mutation-test-elements.js"></script>
       |</head>
       |<body>
       |  <mutation-test-report-app title-postfix="$title">
       |    Your browser doesn't support <a href="https://caniuse.com/#search=custom%20elements">custom elements</a>.
       |    Please use a latest version of an evergreen browser (Firefox, Chrome, Safari, Opera, etc).
       |  </mutation-test-report-app>
       |  <script src="$reportFilename"></script>
       |</body>
       |</html>""".stripMargin

  def reportJs(json: String): String = s"document.querySelector('mutation-test-report-app').report = $json"

  def testElementsJs(): Source = fileIO.readResource(htmlReportResource)

  override def reportRunFinished(runResults: MutantRunResults): Unit = {
    val mapped = toReport(runResults).toJson

    val targetLocation = config.baseDir / s"target/stryker4s-report-${System.currentTimeMillis()}"
    val mutationTestElementsLocation = targetLocation / mutationTestElementsName
    val indexLocation = targetLocation / "index.html"
    val reportLocation = targetLocation / reportFilename

    val reportContent = reportJs(mapped)
    val mutationTestElementsContent = testElementsJs()

    fileIO.createAndWrite(indexLocation, indexHtml)
    fileIO.createAndWrite(reportLocation, reportContent)
    fileIO.createAndWrite(mutationTestElementsLocation, mutationTestElementsContent)

    info(s"Written HTML report to $indexLocation")
  }
}

package stryker4s.report
import better.files.File
import org.mockito.captor.ArgCaptor
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import stryker4s.config.Config
import stryker4s.files.{DiskFileIO, FileIO}
import stryker4s.model.MutantRunResults
import stryker4s.testutil.Stryker4sSuite

import scala.concurrent.duration._

class HtmlReporterTest extends Stryker4sSuite with MockitoSugar with ArgumentMatchersSugar {

  describe("indexHtml") {
    it("should contain title") {
      implicit val config: Config = Config()
      val mockFileIO = mock[FileIO]
      val resourceLocation = "META-INF/resources/webjars/mutation-testing-elements/1.0.2/dist/mutation-test-elements.js"
      when(mockFileIO.readResource(resourceLocation)) thenReturn "console.log('hello');"
      val sut = new HtmlReporter(mockFileIO)

      val result = sut.indexHtml("""{ 'foo': 'bar' }""")

      val expected = s"""<!DOCTYPE html>
                        |<html>
                        |<body>
                        |  <mutation-test-report-app title-postfix="Stryker4s report"></mutation-test-report-app>
                        |  <script>
                        |    document.querySelector('mutation-test-report-app').report = { 'foo': 'bar' }
                        |  </script>
                        |  <script>
                        |    console.log('hello');
                        |  </script>
                        |</body>
                        |</html>""".stripMargin
      result should equal(expected)
    }
  }

  describe("mutation-test-elements") {
    it("should find the resource") {
      implicit val config: Config = Config()
      val fileIO = DiskFileIO

      val sut = new HtmlReporter(fileIO)

      val result = sut.indexHtml("""{ 'foo': 'bar' }""")
      result.linesIterator.length should be > 50
    }
  }

  describe("reportRunFinished") {
    it("should write a file to the report directory") {
      implicit val config: Config = Config()
      val mockFileIO = mock[FileIO]
      val sut = new HtmlReporter(mockFileIO)
      val runResults = MutantRunResults(Nil, 50.0, 30.seconds)

      sut.reportRunFinished(runResults)

      val fileCaptor = ArgCaptor[File]
      verify(mockFileIO).createAndWrite(fileCaptor, any[String])
      fileCaptor.value.pathAsString should fullyMatch regex ".*target(/|\\\\)stryker4s-report-(\\d*)(/|\\\\)index.html$"
    }
  }
}

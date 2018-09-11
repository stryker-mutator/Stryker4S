package stryker4s.run.report

import org.mockito.IdiomaticMockito
import org.mockito.MockitoSugar._
import org.scalatest.BeforeAndAfterEach
import stryker4s.config.Config
import stryker4s.model.MutantRunResults
import stryker4s.{Stryker4sSuite, TestAppender}

import scala.concurrent.duration._
import scala.language.postfixOps

class ReporterTest extends Stryker4sSuite with BeforeAndAfterEach with IdiomaticMockito {

  describe("reporter") {
    it("should log that the console reporter is used when a non existing reporter is configured") {
      val reporterMock = mock[MutantRunReporter]
      implicit val conf: Config = Config(reporters = List(reporterMock))

      val sut: Reporter = new Reporter()
      val mutantRunResults = MutantRunResults(List.empty, 100.0, 10 seconds)

      sut.report(mutantRunResults)

      verify(reporterMock).report(mutantRunResults)
    }
  }
}

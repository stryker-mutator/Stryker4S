package stryker4s.report

import org.mockito.integrations.scalatest.MockitoFixture
import stryker4s.config.Config
import stryker4s.model.MutantRunResults
import stryker4s.testutil.Stryker4sSuite

import scala.concurrent.duration._
import scala.language.postfixOps

class ReporterTest extends Stryker4sSuite with MockitoFixture {

  describe("reporter") {
    it("should log that the console reporter is used when a non existing reporter is configured") {
      val reporterMock = mock[FinishedRunReporter]
      implicit val conf: Config = Config()

      val sut: CombinedReporter = new CombinedReporter(Seq(reporterMock))
      val mutantRunResults = MutantRunResults(List.empty, 100.0, 10 seconds)

      sut.reportRunFinished(mutantRunResults)

      verify(reporterMock).reportRunFinished(mutantRunResults)
    }
  }
}

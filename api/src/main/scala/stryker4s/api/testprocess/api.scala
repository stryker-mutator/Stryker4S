package stryker4s.api.testprocess

// Messages serialized between the main Stryker4s process and its subprocess where tests are run.
// Each case class has a `@SerialVersionUID` so serializing in-between Scala versions work

sealed trait Message

sealed trait Request extends Message

@SerialVersionUID(3008503622726292148L)
final case class SetupTestContext(context: TestProcessContext) extends Request

@SerialVersionUID(4929497926875736311L)
final case class StartTestRun(mutation: Int) extends Request

@SerialVersionUID(6539766406312948278L)
final case class StartInitialTestRun() extends Request

sealed trait Response extends Message

@SerialVersionUID(549618399043999164L)
final case class SetupTestContextSuccessful() extends Response

sealed trait TestResultResponse extends Response

@SerialVersionUID(7287069995681357334L)
final case class TestsSuccessful() extends TestResultResponse

@SerialVersionUID(2877149475182945995L)
final case class TestsUnsuccessful() extends TestResultResponse

@SerialVersionUID(3670742971252993246L)
final case class CoverageTestRunResult(isSuccessful: Boolean, coverageReport: CoverageReport) extends TestResultResponse

@SerialVersionUID(1058983162546605150L)
final case class ErrorDuringTestRun(msg: String) extends TestResultResponse

/** Keys for system properties passed to the testprocess
  */
object TestProcessProperties {
  val port = "stryker4s.testprocess.port"
}

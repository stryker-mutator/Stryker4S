package stryker4s.report.html
import org.everit.json.schema.ValidationException
import org.json.JSONObject
import stryker4s.report.model
import stryker4s.report.model._
import stryker4s.testutil.{MutationTestingElementsJsonSchema, Stryker4sSuite}

class MutationTestResultTest extends Stryker4sSuite {

  describe("created json") {
    it("should be valid according to mutation-report-schema") {
      val schema = MutationTestingElementsJsonSchema.mutationTestingElementsJsonSchema
      val sut = model.MutationTestReport(
        schemaVersion = "1",
        thresholds = Thresholds(high = 80, low = 10),
        files = Map(
          "src/stryker4s/Stryker4s.scala" -> model.MutationTestResult(
            source = "case class Stryker4s(foo: String)",
            mutants = Seq(
              MutantResult("1", "BinaryOperator", "-", Location(
                Position(1, 2),
                Position(2, 3)
              ), status = MutantStatus.Killed)
            )
          )
        )
      )
      val result = new JSONObject(sut.toJson.toString())

      try {
        schema.validate(result)
      } catch {
        case exc: ValidationException =>
          // For testing purposes, to log failing validations
          fail(s"ValidationException occurred: ${exc.getAllMessages}", exc)
      }
    }
  }

}

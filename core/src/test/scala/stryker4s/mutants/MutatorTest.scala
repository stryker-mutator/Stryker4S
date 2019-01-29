package stryker4s.mutants
import stryker4s.config.Config
import stryker4s.mutants.applymutants.{MatchBuilder, StatementTransformer}
import stryker4s.mutants.findmutants.{MutantFinder, MutantMatcher}
import stryker4s.scalatest.{FileUtil, LogMatchers, TreeEquality}
import stryker4s.testutil.stubs.TestSourceCollector
import stryker4s.testutil.Stryker4sSuite

import scala.meta._

class MutatorTest extends Stryker4sSuite with TreeEquality with LogMatchers {

  describe("run") {
    it("should return a single Tree with changed pattern match") {
      implicit val conf: Config = Config()
      val files = new TestSourceCollector(Seq(FileUtil.getResource("scalaFiles/simpleFile.scala")))
        .collectFilesToMutate()

      val sut = new Mutator(
        new MutantFinder(new MutantMatcher),
        new StatementTransformer,
        new MatchBuilder
      )

      val result = sut.mutate(files)

      val expected = """object Foo {
                       |  def bar = sys.props.get("ACTIVE_MUTATION").orElse(sys.env.get("ACTIVE_MUTATION")) match {
                       |    case Some("0") =>
                       |      15 >= 14
                       |    case Some("1") =>
                       |      15 < 14
                       |    case Some("2") =>
                       |      15 == 14
                       |    case _ =>
                       |      15 > 14
                       |  }
                       |  def foobar = sys.props.get("ACTIVE_MUTATION").orElse(sys.env.get("ACTIVE_MUTATION")) match {
                       |    case Some("3") =>
                       |      s""
                       |    case _ =>
                       |      s"${{
                       |        bar
                       |      }}foo"
                       |  }
                       |}""".stripMargin.parse[Source].get
      result.loneElement.tree should equal(expected)
    }
  }
  describe("logs") {
    it("should log the amount of mutants found") {
      implicit val conf: Config = Config()
      val files = new TestSourceCollector(Seq(FileUtil.getResource("scalaFiles/simpleFile.scala")))
        .collectFilesToMutate()

      val sut = new Mutator(
        new MutantFinder(new MutantMatcher),
        new StatementTransformer,
        new MatchBuilder
      )

      sut.mutate(files)

      "Found 1 of 1 file(s) to be mutated." shouldBe loggedAsInfo
      "4 Mutant(s) generated" shouldBe loggedAsInfo
    }

    it("should log the amount of excluded mutants") {
      implicit val conf: Config = Config(excludedMutations = Set("EqualityOperator"))
      val files = new TestSourceCollector(Seq(FileUtil.getResource("scalaFiles/simpleFile.scala")))
        .collectFilesToMutate()

      val sut = new Mutator(
        new MutantFinder(new MutantMatcher),
        new StatementTransformer,
        new MatchBuilder
      )

      sut.mutate(files)

      "Found 1 of 1 file(s) to be mutated." shouldBe loggedAsInfo
      "4 Mutant(s) generated" shouldBe loggedAsInfo
      s"Of which 3 Mutant(s) are excluded." shouldBe loggedAsInfo
    }
  }

  describe("string interpolation") {

    it("checks if the Scalameta workaround is still needed") {
      // If this test fails, the bug mentioned above is fixed, and the workaround can be removed
      val interpolated =
        Term.Interpolate(q"s", List(Lit.String("interpolate this"), Lit.String("bar")), List(q"foo"))

      // We expect that after the fix the interpolate string will look as followed.
      // interpolated.syntax should equal("""s"interpolate this${foo}bar"""")

      interpolated.syntax should equal("""s"interpolate this$foobar"""")
    }
  }
}

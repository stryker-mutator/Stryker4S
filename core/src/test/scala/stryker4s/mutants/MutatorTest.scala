package stryker4s.mutants

import stryker4s.Stryker4sSuite
import stryker4s.config.Config
import stryker4s.mutants.applymutants.{MatchBuilder, StatementTransformer}
import stryker4s.mutants.findmutants.{MutantFinder, MutantMatcher}
import stryker4s.scalatest.{FileUtil, LogMatchers, TreeEquality}
import stryker4s.stubs.TestSourceCollector

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
                       |  def bar = sys.env.get("ACTIVE_MUTATION") match {
                       |    case Some("0") =>
                       |      15 >= 14
                       |    case Some("1") =>
                       |      15 < 14
                       |    case Some("2") =>
                       |      15 == 14
                       |    case _ =>
                       |      15 > 14
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
      "3 Mutant(s) generated" shouldBe loggedAsInfo
    }
  }
}

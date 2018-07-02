package stryker4s

import stryker4s.config.Config
import stryker4s.mutants.Mutator
import stryker4s.mutants.applymutants.{MatchBuilder, StatementTransformer}
import stryker4s.mutants.findmutants.{MutantFinder, MutantMatcher}
import stryker4s.run.MutantRegistry
import stryker4s.scalatest.{FileUtil, TreeEquality}
import stryker4s.stubs.TestSourceCollector

import scala.meta._

class MutatorTest extends Stryker4sSuite with TreeEquality {

  describe("run") {
    it("should return a single Tree with changed pattern match") {
      implicit val conf: Config = Config()
      val files = new TestSourceCollector(Seq(FileUtil.getResource("scalaFiles/simpleFile.scala")))
        .collectFiles()

      val sut = new Mutator(
        new MutantFinder(new MutantMatcher, new MutantRegistry),
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
}

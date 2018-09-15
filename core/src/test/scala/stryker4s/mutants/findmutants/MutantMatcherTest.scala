package stryker4s.mutants.findmutants

import stryker4s.Stryker4sSuite
import stryker4s.extensions.ImplicitMutationConversion.mutationToTree
import stryker4s.extensions.mutationtypes._
import stryker4s.model.Mutant
import stryker4s.scalatest.TreeEquality

import scala.meta._
import scala.meta.contrib._

class MutantMatcherTest extends Stryker4sSuite with TreeEquality {
  val sut = new MutantMatcher

  def expectMutations(matchFun: PartialFunction[Tree, Seq[Mutant]],
                      tree: Tree,
                      original: Term,
                      expectedTerms: Term*): Unit = {
    val found: Seq[Mutant] = tree.collect(matchFun).flatten

    expectedTerms.foreach(expectedTerm => expectMutations(found, original, expectedTerm))
  }

  def expectMutations(actualMutants: Seq[Mutant], original: Term, expectedMutations: Term*): Unit = {
    expectedMutations.foreach(expectedMutation => {
      val actualMutant = actualMutants
        .find(mutant => mutant.mutated.isEqual(expectedMutation) &&
          mutant.original.isEqual(original))
        .getOrElse(fail("mutant not found"))

      actualMutant.original should equal(original)
      actualMutant.mutated should equal(expectedMutation)
    })
  }

  describe("All Matchers") {
    it("should match a conditional statement") {
      val tree = q"def foo = 15 > 20 && 20 < 15"

      val found: Seq[Mutant] = tree.collect(sut.allMatchers()).flatten

      found should have length 7
      expectMutations(found, q">", q">=", q"<", q"==")
      expectMutations(found, q"&&", q"||")
      expectMutations(found, q"<", q"<=", q">", q"==")
    }

    it("should match a method") {
      val tree = q"def foo = List(1, 2).filterNot(filterNotFunc).filter(filterFunc)"

      val found = tree.collect(sut.allMatchers()).flatten

      found should have length 2
      expectMutations(found, q"filterNot", q"filter")
      expectMutations(found, q"filter", q"filterNot")
    }

    it("should match a boolean and a conditional") {
      val tree = q"def foo = false && 15 > 4"

      val found = tree.collect(sut.allMatchers()).flatten

      found should have length 5
      expectMutations(found, q"false", q"true")
      expectMutations(found, q"&&", q"||")
      expectMutations(found, q">", q"<", q"==")
    }

    it("should match the default case of a constructor argument") {
      val tree = q"class Person(isOld: Boolean = 18 > 15) { }"

      val found = tree.collect(sut.allMatchers()).flatten

      found should have length 3
      expectMutations(found, q">", q">=", q"<", q"==")
    }

    it("should match on the default case of a function argument") {
      val tree = q"def hasGoodBack(isOld: Boolean = age > 60): Boolean = isOld"

      val found = tree.collect(sut.allMatchers()).flatten

      found should have length 3
      expectMutations(found, q">", q">=", q"<", q"==")
    }
  }

  describe("matchBinaryOperators matcher") {
    it("should match >= sign with >, <, and ==") {
      expectMutations(
        sut.matchBinaryOperators(),
        q"def foo = 18 >= 20",
        GreaterThanEqualTo,
        GreaterThan,
        LesserThan,
        EqualTo
      )
    }

    it("should match > with >=, < and ==") {
      expectMutations(
        sut.matchBinaryOperators(),
        q"def foo = 18 > 20",
        GreaterThan,
        GreaterThanEqualTo,
        LesserThan,
        EqualTo
      )
    }

    it("should match <= to <, >= and ==") {
      expectMutations(
        sut.matchBinaryOperators(),
        q"def foo = 18 <= 20",
        LesserThanEqualTo,
        LesserThan,
        GreaterThanEqualTo,
        EqualTo
      )
    }

    it("should match < to <=, > and ==") {
      expectMutations(
        sut.matchBinaryOperators(),
        q"def foo = 18 < 20",
        LesserThan,
        LesserThanEqualTo,
        GreaterThan,
        EqualTo
      )
    }

    it("should match == to !=") {
      expectMutations(
        sut.matchBinaryOperators(),
        q"def foo = 18 == 20",
        EqualTo,
        NotEqualTo
      )
    }

    it("should match != to ==") {
      expectMutations(
        sut.matchBinaryOperators(),
        q"def foo = 18 != 20",
        NotEqualTo,
        EqualTo
      )
    }
  }
  describe("logicalOperators matcher") {
    it("should match && to ||") {
      expectMutations(
        sut.matchLogicalOperators(),
        q"def foo = a && b",
        And,
        Or
      )
    }

    it("should match || to &&") {
      expectMutations(
        sut.matchLogicalOperators(),
        q"def foo = a || b",
        Or,
        And
      )
    }
  }

  describe("matchMethodMutators matcher") {
    it("should match filter to filterNot") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).filter(_ % 2 == 0)",
        Filter,
        FilterNot
      )
    }

    it("should match filterNot to filter") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).filterNot(_ % 2 == 0)",
        FilterNot,
        Filter
      )
    }

    it("should match exists to forAll") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).exists(_ % 2 == 0)",
        Exists,
        ForAll
      )
    }

    it("should match forAll to exists") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).forAll(_ % 2 == 0)",
        ForAll,
        Exists
      )
    }

    it("should match isEmpty to nonEmpty") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).isEmpty",
        IsEmpty,
        NonEmpty
      )
    }

    it("should match nonEmpty to isEmpty") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).nonEmpty",
        NonEmpty,
        IsEmpty
      )
    }

    it("should match indexOf to lastIndexOf") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).indexOf(2)",
        IndexOf,
        LastIndexOf
      )
    }

    it("should match lastIndexOf to indexOf") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).lastIndexOf(2)",
        LastIndexOf,
        IndexOf
      )
    }

    it("should match max to min") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).max",
        Max,
        Min
      )
    }

    it("should match min to max") {
      expectMutations(
        sut.matchMethodMutators(),
        q"def foo = List(1, 2, 3).min",
        Min,
        Max
      )
    }
  }

  describe("matchBooleanSubstitutions matcher") {
    it("should match false to true") {
      expectMutations(
        sut.matchBooleanSubstitutions(),
        q"def foo = false",
        False,
        True
      )
    }

    it("should match true to false") {
      expectMutations(
        sut.matchBooleanSubstitutions(),
        q"def foo = true",
        True,
        False
      )
    }
  }
  describe("stringMutators matcher") {
    it("should match foo to NonEmptyString") {
      expectMutations(
        sut.matchStringMutators(),
        q"""def foo: String = "bar"""",
        Lit.String("bar"),
        EmptyString
      )
    }

    it("should match empty string to StrykerWasHere") {
      expectMutations(
        sut.matchStringMutators(),
        q"""def foo = "" """,
        EmptyString,
        StrykerWasHereString
      )
    }

    it("should match on interpolated strings") {
      val interpolated =
        Term.Interpolate(q"s", List(Lit.String("interpolate "), Lit.String("")), List(q"foo"))
      val tree = q"def foo = $interpolated"
      val emptyStringInterpolate = Term.Interpolate(q"s", List(Lit.String("")), Nil)

      interpolated.syntax should equal("s\"interpolate $foo\"")
      expectMutations(
        sut.matchStringMutators(),
        tree,
        interpolated,
        emptyStringInterpolate
      )
    }

    it("should match once on interpolated strings with multiple parts") {
      val interpolated =
        Term.Interpolate(q"s",
                         List(Lit.String("interpolate "), Lit.String(" foo "), Lit.String(" bar")),
                         List(q"fooVar", q"barVar + 1"))
      val tree = q"def foo = $interpolated"
      val emptyStringInterpolate = Term.Interpolate(q"s", List(Lit.String("")), Nil)

      interpolated.syntax should equal("s\"interpolate $fooVar foo ${barVar + 1} bar\"")
      expectMutations(
        sut.matchStringMutators(),
        tree,
        interpolated,
        emptyStringInterpolate
      )
    }

    it("should not match non-string interpolation") {
      val interpolated =
        Term.Interpolate(q"q", List(Lit.String("interpolate "), Lit.String("")), List(q"foo"))
      val tree = q"def foo = $interpolated "

      val result = tree collect sut.allMatchers()

      interpolated.syntax should equal("q\"interpolate $foo\"")
      result should be(empty)
    }
  }

  describe("Create mutant id's") {
    it("should register multiple mutants from a found mutant with multiple mutations") {
      val sut = new MutantMatcher
      val mutants = sut.TermExtensions(GreaterThan) ~~> (LesserThan, GreaterThanEqualTo, EqualTo)

      mutants.map(mutant => mutant.id) should contain theSameElementsAs List(0, 1, 2)
    }
  }

}

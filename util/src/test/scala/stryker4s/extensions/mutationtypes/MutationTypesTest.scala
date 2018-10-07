package stryker4s.extensions.mutationtypes

import stryker4s.Stryker4sSuite
import stryker4s.extensions.ImplicitMutationConversion.mutationToTree
import stryker4s.scalatest.TreeEquality

import scala.meta._

class MutationTypesTest extends Stryker4sSuite with TreeEquality {
  describe("BinaryOperators") {
    it("> to GreaterThan") {
      q">" should matchPattern { case GreaterThan(_) => }
    }

    it(">= to GreaterThanEqualTo") {
      q">=" should matchPattern { case GreaterThanEqualTo(_) => }
    }

    it("<= to LesserThanEqualTo") {
      q"<=" should matchPattern { case LesserThanEqualTo(_) => }
    }

    it("< to LesserThan") {
      q"<" should matchPattern { case LesserThan(_) => }
    }

    it("== to EqualTo") {
      q"==" should matchPattern { case EqualTo(_) => }
    }

    it("!= to NotEqualTo") {
      q"!=" should matchPattern { case NotEqualTo(_) => }
    }
  }

  describe("BooleanSubstitutions") {
    it("false to False") {
      q"false" should matchPattern { case False(_) => }
    }

    it("true to True") {
      q"true" should matchPattern { case True(_) => }
    }
  }

  describe("LogicalOperators") {
    it("&& to And") {
      q"&&" should matchPattern { case And(_) => }
    }

    it("|| to Or") {
      q"||" should matchPattern { case Or(_) => }
    }
  }

  describe("StringMutators") {
    it("foo string to NonEmptyString") {
      q""""foo"""" should matchPattern { case NonEmptyString(_) => }
    }

    it("empty string to EmptyString") {
      Lit.String("") should matchPattern { case EmptyString(_) => }
    }

    it("string interpolation to StringInterpolation") {
      Term.Interpolate(q"s", List(Lit.String("foo "), Lit.String("")), List(q"foo")) should matchPattern {
        case StringInterpolation(_) =>
      }
    }

    it("q interpolation should not match StringInterpolation") {
      Term.Interpolate(q"q", List(Lit.String("foo "), Lit.String("")), List(q"foo")) should not matchPattern {
        case StringInterpolation(_) =>
      }
    }
  }

  describe("other cases") {
    it("should return original tree on match") {
      val tree = q">="

      val result = tree match {
        case GreaterThanEqualTo(t) => t
      }

      result should be theSameInstanceAs tree
    }

    it("should convert GreaterThan to >") {
      val wrapped = WrappedTree(GreaterThan)

      wrapped.term should equal(q">")
    }

    it("should convert to the proper type") {
      val falseTree: Tree = False
      val greaterThan: Tree = GreaterThan

      falseTree shouldBe a[Lit.Boolean]
      greaterThan shouldBe a[Term.Name]
    }

    case class WrappedTree(term: Tree)
  }
}
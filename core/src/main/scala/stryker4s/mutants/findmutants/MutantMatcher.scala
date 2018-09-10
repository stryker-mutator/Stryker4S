package stryker4s.mutants.findmutants

import stryker4s.extensions.ImplicitMutationConversion.mutationToTree
import stryker4s.extensions.mutationtypes._
import stryker4s.model.FoundMutant

import scala.meta.Tree

class MutantMatcher {

  def allMatchers(): PartialFunction[Tree, FoundMutant] =
    matchBinaryOperators() orElse
      matchBooleanSubstitutions() orElse
      matchLogicalOperators() orElse
      matchStringMutators() orElse
      matchMethodMutators()

  def matchBinaryOperators(): PartialFunction[Tree, FoundMutant] = {
    case GreaterThanEqualTo(orig) => FoundMutant(orig, GreaterThan, LesserThan, EqualTo)
    case GreaterThan(orig)        => FoundMutant(orig, GreaterThanEqualTo, LesserThan, EqualTo)
    case LesserThanEqualTo(orig)  => FoundMutant(orig, LesserThan, GreaterThanEqualTo, EqualTo)
    case LesserThan(orig)         => FoundMutant(orig, LesserThanEqualTo, GreaterThan, EqualTo)
    case EqualTo(orig)            => FoundMutant(orig, NotEqualTo)
    case NotEqualTo(orig)         => FoundMutant(orig, EqualTo)
  }

  def matchBooleanSubstitutions(): PartialFunction[Tree, FoundMutant] = {
    case True(orig)  => FoundMutant(orig, False)
    case False(orig) => FoundMutant(orig, True)
  }

  def matchLogicalOperators(): PartialFunction[Tree, FoundMutant] = {
    case And(orig) => FoundMutant(orig, Or)
    case Or(orig)  => FoundMutant(orig, And)
  }

  def matchStringMutators(): PartialFunction[Tree, FoundMutant] = {
    case EmptyString(orig)         => FoundMutant(orig, StrykerWasHereString)
    case NonEmptyString(orig)      => FoundMutant(orig, EmptyString)
    case StringInterpolation(orig) => FoundMutant(orig, EmptyStringInterpolation)
  }

  def matchMethodMutators(): PartialFunction[Tree, FoundMutant] = {
    case Filter(orig)      => FoundMutant(orig, FilterNot)
    case FilterNot(orig)   => FoundMutant(orig, Filter)
    case Exists(orig)      => FoundMutant(orig, ForAll)
    case ForAll(orig)      => FoundMutant(orig, Exists)
    case IsEmpty(orig)     => FoundMutant(orig, NonEmpty)
    case NonEmpty(orig)    => FoundMutant(orig, IsEmpty)
    case IndexOf(orig)     => FoundMutant(orig, LastIndexOf)
    case LastIndexOf(orig) => FoundMutant(orig, IndexOf)
    case Max(orig)         => FoundMutant(orig, Min)
    case Min(orig)         => FoundMutant(orig, Max)
  }
}

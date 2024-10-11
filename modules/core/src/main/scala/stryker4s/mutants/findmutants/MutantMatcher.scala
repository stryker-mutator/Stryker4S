package stryker4s.mutants.findmutants

import cats.data.NonEmptyVector
import cats.syntax.all.*
import stryker4s.config.{Config, ExcludedMutation}
import stryker4s.extension.PartialFunctionOps.*
import stryker4s.extension.TreeExtensions.{IsEqualExtension, PositionExtension, TransformOnceExtension}
import stryker4s.model.*
import stryker4s.mutants.tree.{IgnoredMutation, IgnoredMutations, Mutations}
import stryker4s.mutation.*

import scala.annotation.tailrec
import scala.meta.*

import MutantMatcher.MutationMatcher

trait MutantMatcher {

  /** Matches on all types of mutations and returns a list of all the mutations that were found.
    */
  def allMatchers: MutationMatcher
}

object MutantMatcher {

  /** A PartialFunction that can match on a ScalaMeta tree and return a `Either[IgnoredMutations, Mutations]`.
    *
    * If the result is a `Left`, it means a mutant was found, but ignored. The ADT
    * [[stryker4s.model.IgnoredMutationReason]] shows the possible reasons.
    */
  type MutationMatcher = PartialFunction[Tree, PlaceableTree => Either[IgnoredMutations, Mutations]]

}

class MutantMatcherImpl()(implicit config: Config) extends MutantMatcher {

  override def allMatchers: MutationMatcher =
    matchBooleanLiteral orElse
      matchEqualityOperator orElse
      matchLogicalOperator orElse
      matchConditionalExpression orElse
      matchMethodExpression orElse
      matchStringsAndRegex

  def matchBooleanLiteral: MutationMatcher = {
    case True(orig)  => createMutations(orig)(False)
    case False(orig) => createMutations(orig)(True)
  }

  def matchEqualityOperator: MutationMatcher = {
    case GreaterThanEqualTo(orig) => createMutations(orig)(GreaterThan, LesserThan, EqualTo)
    case GreaterThan(orig)        => createMutations(orig)(GreaterThanEqualTo, LesserThan, EqualTo)
    case LesserThanEqualTo(orig)  => createMutations(orig)(LesserThan, GreaterThanEqualTo, EqualTo)
    case LesserThan(orig)         => createMutations(orig)(LesserThanEqualTo, GreaterThan, EqualTo)
    case EqualTo(orig)            => createMutations(orig)(NotEqualTo)
    case NotEqualTo(orig)         => createMutations(orig)(EqualTo)
    case TypedEqualTo(orig)       => createMutations(orig)(TypedNotEqualTo)
    case TypedNotEqualTo(orig)    => createMutations(orig)(TypedEqualTo)
  }

  def matchLogicalOperator: MutationMatcher = {
    case And(orig) => createMutations(orig)(Or)
    case Or(orig)  => createMutations(orig)(And)
  }

  def matchConditionalExpression: MutationMatcher = {
    case If(orig)      => createMutations(orig)(ConditionalTrue, ConditionalFalse)
    case While(orig)   => createMutations(orig)(ConditionalFalse)
    case DoWhile(orig) => createMutations(orig)(ConditionalFalse)
  }

  def matchMethodExpression: MutationMatcher = {
    case Filter(orig, f)      => createMutations(orig, f, FilterNot)
    case FilterNot(orig, f)   => createMutations(orig, f, Filter)
    case Exists(orig, f)      => createMutations(orig, f, Forall)
    case Forall(orig, f)      => createMutations(orig, f, Exists)
    case Take(orig, f)        => createMutations(orig, f, Drop)
    case Drop(orig, f)        => createMutations(orig, f, Take)
    case TakeRight(orig, f)   => createMutations(orig, f, DropRight)
    case DropRight(orig, f)   => createMutations(orig, f, TakeRight)
    case TakeWhile(orig, f)   => createMutations(orig, f, DropWhile)
    case DropWhile(orig, f)   => createMutations(orig, f, TakeWhile)
    case IsEmpty(orig, f)     => createMutations(orig, f, NonEmpty)
    case NonEmpty(orig, f)    => createMutations(orig, f, IsEmpty)
    case IndexOf(orig, f)     => createMutations(orig, f, LastIndexOf)
    case LastIndexOf(orig, f) => createMutations(orig, f, IndexOf)
    case Max(orig, f)         => createMutations(orig, f, Min)
    case Min(orig, f)         => createMutations(orig, f, Max)
    case MaxBy(orig, f)       => createMutations(orig, f, MinBy)
    case MinBy(orig, f)       => createMutations(orig, f, MaxBy)
  }

  /** Match both strings and regexes instead of stopping when one of them gives a match
    */
  def matchStringsAndRegex: MutationMatcher = matchStringLiteral combine matchRegex

  def matchStringLiteral: MutationMatcher = {
    case EmptyString(orig)         => createMutations(orig)(StrykerWasHereString)
    case NonEmptyString(orig)      => createMutations(orig)(EmptyString)
    case StringInterpolation(orig) => createMutations(orig)(EmptyString)
  }

  def matchRegex: MutationMatcher = {
    case RegexConstructor(orig)   => createMutations(orig, RegexMutations(orig))
    case RegexStringOps(orig)     => createMutations(orig, RegexMutations(orig))
    case PatternConstructor(orig) => createMutations(orig, RegexMutations(orig))
  }

  private def createMutations[T <: Tree](
      original: Term,
      f: String => Term,
      mutated: MethodExpression
  ): PlaceableTree => Either[IgnoredMutations, Mutations] = {
    val replacements: NonEmptyVector[MethodExpression] = NonEmptyVector.one(mutated)
    buildMutations[MethodExpression](original, replacements, _(f))
  }

  private def createMutations[T <: Term](
      original: Term,
      mutated: Either[IgnoredMutation, NonEmptyVector[RegularExpression]]
  ): PlaceableTree => Either[IgnoredMutations, Mutations] = { placeableTree =>
    mutated
      .leftMap(NonEmptyVector.one(_))
      .flatMap(muts => buildMutations[RegularExpression](original, muts, _.tree)(placeableTree))

  }

  private def createMutations[T <: Term](
      original: Term
  )(
      firstReplacement: SubstitutionMutation[T],
      restReplacements: SubstitutionMutation[T]*
  ): PlaceableTree => Either[IgnoredMutations, Mutations] = {
    val replacements: NonEmptyVector[SubstitutionMutation[T]] =
      NonEmptyVector(firstReplacement, restReplacements.toVector)
    buildMutations[SubstitutionMutation[T]](original, replacements, _.tree)
  }

  private def buildMutations[T <: Mutation[? <: Tree]](
      original: Term,
      replacements: NonEmptyVector[T],
      mutationToTerm: T => Term
  ): PlaceableTree => Either[IgnoredMutations, Mutations] = placeableTree => {
    val mutations = replacements.map { mutations =>
      val tree = mutationToTerm(mutations)

      val (location, description, replacement) = mutations match {
        case r: RegularExpression => (r.location, r.description.some, r.replacement)
        case _                    => (original.pos.toLocation, none, tree.printSyntaxFor(config.scalaDialect))
      }

      val metadata = MutantMetadata(
        original.printSyntaxFor(config.scalaDialect),
        replacement,
        mutations.mutationName,
        location,
        description
      )
      val mutatedTopStatement = placeableTree.tree
        .transformExactlyOnce {
          case t if t.isEqual(original) && t.pos == original.pos =>
            tree
        }
        .getOrElse(
          throw new RuntimeException(
            s"Could not transform '$original' in ${placeableTree.tree} (${metadata.showLocation})"
          )
        )

      mutatedTopStatement match {
        case t: Term => MutatedCode(t, metadata)
        case t =>
          throw new RuntimeException(
            s"Could not transform '$original' in ${placeableTree.tree} (${metadata.showLocation}). Expected a Term, but was a ${t.getClass().getSimpleName}"
          )
      }

    }
    filterExclusions(mutations, replacements.head, original)

  }

  private def filterExclusions(
      mutations: NonEmptyVector[MutatedCode],
      mutationType: Mutation[?],
      original: Tree
  ): Either[IgnoredMutations, Mutations] = {
    if (excludedByConfig(mutationType.mutationName) || excludedByAnnotation(original, mutationType.fullName))
      mutations.tupleRight(MutationExcluded).asLeft
    else
      mutations.asRight
  }

  private def excludedByConfig(mutation: String): Boolean =
    config.excludedMutations.contains(ExcludedMutation(mutation))

  @tailrec
  private def excludedByAnnotation(original: Tree, mutationName: String): Boolean = {
    import stryker4s.extension.TreeExtensions.*
    original.parent match {
      case Some(value) =>
        value.getMods.exists(isSupressWarningsAnnotation(_, mutationName)) || excludedByAnnotation(
          value,
          mutationName
        )
      case None => false
    }
  }

  private def isSupressWarningsAnnotation(mod: Mod, mutationName: String): Boolean = {
    mod match {
      case Mod.Annot(
            Init.After_4_6_0(
              Type.Name("SuppressWarnings"),
              _,
              List(Term.ArgClause(List(Term.Apply.After_4_6_0(Name("Array"), Term.ArgClause(params, _))), _))
            )
          ) =>
        params.exists {
          case Lit.String(`mutationName`) => true
          case _                          => false
        }
      case _ => false
    }
  }

}

package stryker4s.mutants.applymutants

import stryker4s.extensions.TreeExtensions._
import stryker4s.model._

import scala.meta.contrib._
import scala.meta.{Source, Term, Tree}

class StatementTransformer {

  def transformSource(source: Source, foundMutants: Seq[Mutant]): SourceTransformations = {

    val transformedMutants: Seq[TransformedMutants] = foundMutants
      .groupBy(mutant => mutant.original)
      .map { case (original, mutants) => transformMutant(original, mutants) }
      .toSeq

    SourceTransformations(source, transformedMutants)
  }

  /** Transforms the statement in the original tree of the FoundMutant to the given mutations
    */
  def transformMutant(original: Term, registered: Seq[Mutant]): TransformedMutants = {
    val topStatement = original.topStatement()

    val transformedMutants = registered.map { mutant =>
      val newMutated = transformStatement(topStatement, mutant.original, mutant.mutated)
      Mutant(mutant.id, topStatement, newMutated)
    }.toList

    TransformedMutants(topStatement, transformedMutants)
  }

  /** Transforms the statement to the given mutation
    */
  def transformStatement(topStatement: Term, toMutate: Term, mutation: Term): Term =
    topStatement
      .transform {
        case foundTree: Tree if foundTree.isEqual(toMutate) && foundTree.pos == toMutate.pos =>
          mutation
      }
      .asInstanceOf[Term]
}

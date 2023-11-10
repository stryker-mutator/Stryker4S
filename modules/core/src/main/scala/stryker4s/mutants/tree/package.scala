package stryker4s.mutants

import cats.data.{NonEmptyList, NonEmptyVector}
import stryker4s.model.{IgnoredMutationReason, MutantWithId, MutatedCode, PlaceableTree}

import scala.meta.{Term, Tree}

package object tree {

  type Mutations = NonEmptyVector[MutatedCode]

  type IgnoredMutation = (MutatedCode, IgnoredMutationReason)
  type IgnoredMutations = NonEmptyVector[(MutatedCode, IgnoredMutationReason)]

  type MutantsWithId = NonEmptyVector[MutantWithId]

  type MutationMatcher = PartialFunction[Tree, PlaceableTree => Mutations]

  type DefaultMutationCondition = (NonEmptyList[Int]) => Term

}

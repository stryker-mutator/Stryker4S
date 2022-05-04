package stryker4s.model

import fs2.io.file.Path
import stryker4s.mutants.tree.MutationsWithId

import scala.meta.Tree

final case class MutatedFile(fileOrigin: Path, mutatedSource: Tree, mutants: MutationsWithId)

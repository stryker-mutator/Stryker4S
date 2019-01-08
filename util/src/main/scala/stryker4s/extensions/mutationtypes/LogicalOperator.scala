package stryker4s.extensions.mutationtypes

import scala.meta.Term

case object And extends LogicalOperator {
  override val tree: Term.Name = Term.Name("&&")
}

case object Or extends LogicalOperator {
  override val tree: Term.Name = Term.Name("||")
}

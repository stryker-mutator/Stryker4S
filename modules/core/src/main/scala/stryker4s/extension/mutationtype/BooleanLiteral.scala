package stryker4s.extension.mutationtype

import scala.meta.Lit

case object True extends BooleanLiteral {
  override val tree: Lit.Boolean = Lit.Boolean(true)
}

case object False extends BooleanLiteral {
  override val tree: Lit.Boolean = Lit.Boolean(false)
}

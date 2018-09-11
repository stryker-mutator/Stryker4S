package stryker4s.extensions.mutationtypes
import scala.meta.Lit

case object True extends BooleanSubstitution {
  override val tree: Lit.Boolean = Lit.Boolean(true)
}

case object False extends BooleanSubstitution {
  override val tree: Lit.Boolean = Lit.Boolean(false)
}

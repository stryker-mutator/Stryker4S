package stryker4s.scalatest

import org.scalactic.Equality

import scala.meta.Tree
import scala.meta.contrib.XtensionTreeEquality

/** Provides equality checking for ScalaTest on the structure of Trees.
  * Checks if two trees have the same structure and syntax
  * Can be used as follows: <code>firstTree should equal(secondTree)</code>.
  * If this trait is in scope but you still want to check for reference equality,
  * the <code>be</code> matcher can be used instead of <code>equal</code>
  */
trait TreeEquality {
  // Needs to be T <: Tree to work with subtypes of Tree instead of just Tree
  implicit def structureEquality[T <: Tree]: Equality[T] = new Equality[T] {
    override def areEqual(first: T, secondAny: Any): Boolean = secondAny match {
      case second: Tree => second.isEqual(first)
      case _            => false
    }
  }
}

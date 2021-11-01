package stryker4s.model

/** The base result of a mutant run.
  */
sealed trait MutantRunResult {
  def mutant: Mutant
  def description: Option[String]
  def testsCompleted: Option[Int]
}

sealed trait Detected extends MutantRunResult

sealed trait Undetected extends MutantRunResult

final case class Killed(mutant: Mutant, description: Option[String] = None, testsCompleted: Option[Int] = None)
    extends Detected

final case class TimedOut(mutant: Mutant, description: Option[String] = None, testsCompleted: Option[Int] = None)
    extends Detected

final case class Survived(mutant: Mutant, description: Option[String] = None, testsCompleted: Option[Int] = None)
    extends Undetected

final case class NoCoverage(mutant: Mutant, description: Option[String] = None, testsCompleted: Option[Int] = None)
    extends Undetected

final case class Error(mutant: Mutant, description: Option[String] = None, testsCompleted: Option[Int] = None)
    extends MutantRunResult

final case class Ignored(mutant: Mutant, description: Option[String] = None, testsCompleted: Option[Int] = None)
    extends MutantRunResult

final case class CompileError(mutant: Mutant, description: Option[String] = None, testsCompleted: Option[Int] = None)
    extends MutantRunResult

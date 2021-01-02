package stryker4s.testutil.stubs

import scala.util.{Success, Try}

import better.files.File
import cats.effect.{Blocker, ContextShift, IO}
import stryker4s.log.Logger
import stryker4s.run.process.{Command, ProcessRunner}

object TestProcessRunner {
  def apply(testRunExitCode: Try[Int]*)(implicit log: Logger, cs: ContextShift[IO]): TestProcessRunner =
    new TestProcessRunner(true, testRunExitCode: _*)
  def failInitialTestRun()(implicit log: Logger, cs: ContextShift[IO]): TestProcessRunner = new TestProcessRunner(false)
}

class TestProcessRunner(initialTestRunSuccess: Boolean, testRunExitCode: Try[Int]*)(implicit
    log: Logger,
    cs: ContextShift[IO]
) extends ProcessRunner {
  val timesCalled: Iterator[Int] = Iterator.from(0)

  /** Keep track on the amount of times the function is called.
    * Also return an exit code which the test runner would do as well.
    */
  override def apply(command: Command, workingDir: File, envVar: (String, String), blocker: Blocker): IO[Try[Int]] = {
    if (envVar._2.equals("None")) {
      IO.pure(Success(if (initialTestRunSuccess) 0 else 1))
    } else {
      timesCalled.next()
      IO.pure(testRunExitCode(envVar._2.toInt))
    }
  }
}

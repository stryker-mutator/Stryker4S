package stryker4s.sbt
import java.nio.file.Path

import better.files.File
import sbt.Keys._
import sbt._
import stryker4s.config.Config
import stryker4s.extension.exception.InitialTestRunFailedException
import stryker4s.model._
import stryker4s.run.MutantRunner
import stryker4s.run.process.ProcessRunner

class SbtMutantRunner(state: State, processRunner: ProcessRunner)(implicit config: Config)
    extends MutantRunner(processRunner) {

  val extracted: Extracted = Project.extract(state)

  override def runInitialTest(workingDir: File): Boolean = {
    val newState = extracted.appendWithoutSession(settings(workingDir), state)
    Project.runTask(test in Test, newState) match {
      case None =>
        throw InitialTestRunFailedException(
          s"Unable to execute initial test run. Sbt is unable to find the task 'test'.")
      case Some((_, Value(_))) => true
      case Some((_, Inc(_)))   => false
    }
  }

  override def runMutant(mutant: Mutant, workingDir: File, subPath: Path): MutantRunResult = {
    val newState = extracted.appendWithoutSession(settings(workingDir) ++ mutationSetting(mutant.id), state)

    Project.runTask(test in Test, newState) match {
      case None =>
        throw new RuntimeException(s"An unexpected error occurred while running mutation ${mutant.id}")
      case Some((_, Value(_))) => Survived(mutant, subPath)
      case Some((_, Inc(_)))   => Killed(mutant, subPath)
    }
  }

  private lazy val filteredSystemProperties = {
    // Matches strings that start with one of the options between brackets
    val regex = "^(java|sun|file|user|jna|os|sbt|jline|awt|user).*"

    val filteredProps =
      sys.props.toList
        .filterNot { case (key, _) => key.matches(regex) }
        .map { case (key, value) => s"-D$key=$value" }

    if (filteredProps.nonEmpty) {
      debug("System properties added to the forked JVM")
      filteredProps.foreach(debug(_))
    }

    filteredProps
  }

  private[this] def settings(tmpDir: File): Seq[Def.Setting[_]] = {
    val mainPath = {
      extracted
        .get(Compile / scalaSource)
        .absolutePath
        .diff(
          extracted.get(Compile / baseDirectory).absolutePath
        )
    }

    val testPath = {
      extracted
        .get(Test / scalaSource)
        .absolutePath
        .diff(
          extracted.get(Test / baseDirectory).absolutePath
        )
    }

    Seq(
      fork in Test := true,
      javaOptions in Test ++= {
        filteredSystemProperties
      },
      scalaSource in Compile := tmpDir.toJava / mainPath,
      scalaSource in Test := tmpDir.toJava / testPath
    )

  }

  private[this] def mutationSetting(mutation: Int): Seq[Def.Setting[_]] = {
    Seq(
      // Set active mutation
      javaOptions in Test += s"-DACTIVE_MUTATION=${String.valueOf(mutation)}"
    )
  }
}

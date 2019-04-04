import sbt.Keys._
import sbt._

import scala.sys.process

object Release {

  // Main release commands
  private val stryker4sPublish = "stryker4sPublish"
  private val stryker4sPublishSigned = "stryker4sPublishSigned"
  // Helper command names
  private val stryker4sMvnDeploy = "stryker4sMvnDeploy"
  private val publishM2 = "stryker4s-core/publishM2"
  private val crossPublish = "+publish"
  private val crossPublishSigned = "+publishSigned"
  private def setVersion(version: String) = s"""set version in ThisBuild := "$version""""

  lazy val releaseCommands: Setting[Seq[Command]] = commands ++= {
    val originalVersion = version.value
    Seq(
      // Called by sbt-ci-release
      Command.command(stryker4sPublish)(publishM2 :: stryker4sMvnDeploy :: crossPublish :: _),
      Command.command(stryker4sPublishSigned)(publishM2 :: stryker4sMvnDeploy :: crossPublishSigned :: _),
      // Called by stryker4sPublish(signed)
      // Set version again after deploy (causes local changes, which changes the version)
      Command.command(stryker4sMvnDeploy)(
        mvnDeploy(baseDirectory.value, version.value) andThen (setVersion(originalVersion) :: _)
      )
    )
  }

  /** Sets version of mvn project, calls `mvn deploy` and fails state if the command fails
    */
  private def mvnDeploy(baseDir: File, version: String): State => State =
    state =>
      mvnGoal(s"versions:set -DnewVersion=$version", baseDir) #&&
        mvnGoal(s"deploy --settings settings.xml -DskipTests", baseDir) ! match {
        case 0 => state
        case _ => state.fail
    }

  /** Returns a `ProcessBuilder` that runs the given maven command in the maven subdirectory
    */
  private def mvnGoal(command: String, baseDir: File): process.ProcessBuilder =
    process.Process(s"mvn --batch-mode $command -P release", baseDir / "runners" / "maven")

}

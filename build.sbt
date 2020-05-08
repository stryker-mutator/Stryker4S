lazy val root = (project withId "stryker4s" in file("."))
  .settings(
    Settings.buildLevelSettings,
    skip in publish := true,
    onLoad in Global ~= (_ andThen ("writeHooks" :: _))
  )
  .aggregate(
    stryker4sCore.jvm(Dependencies.versions.scala213),
    stryker4sCommandRunner.jvm(Dependencies.versions.scala213),
    sbtStryker4s
  )

lazy val stryker4sCore = newProject("stryker4s-core", "core")
  .settings(Settings.coreSettings)
  .jvmPlatform(scalaVersions = Dependencies.versions.crossScalaVersions)

lazy val stryker4sCommandRunner = newProject("stryker4s-command-runner", "command-runner")
  .settings(
    Settings.commandRunnerSettings,
    mainClass in (Compile, run) := Some("stryker4s.run.Stryker4sCommandRunner")
  )
  .dependsOn(stryker4sCore, stryker4sCore % "test->test")
  .jvmPlatform(scalaVersions = Dependencies.versions.crossScalaVersions)

// sbt project is a 'normal' project without projectMatrix because there is only 1 scala version
// sbt plugins have to use Scala 2.12
lazy val sbtStryker4s = (project withId "sbt-stryker4s" in file("sbt"))
  .enablePlugins(SbtPlugin)
  .settings(Settings.commonSettings, Settings.sbtPluginSettings)
  .dependsOn(stryker4sCore.jvm(Dependencies.versions.scala212))

def newProject(projectName: String, dir: String) =
  sbt.internal
    .ProjectMatrix(projectName, file(dir))
    .settings(Settings.commonSettings)

lazy val writeHooks = taskKey[Unit]("Write git hooks")
Global / writeHooks := GitHooks(file("git-hooks"), file(".git/hooks"), streams.value.log)

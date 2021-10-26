import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  object versions {
    val scala212 = "2.12.15"
    val scala213 = "2.13.6"
    val scala3 = "3.1.0"

    /** Cross-versions for main projects
      */
    val crossScalaVersions = Seq(scala213, scala212)

    /** Fuller cross-versions (used for injected packages like stryker4s-api and sbt-stryker4s-testrunner)
      */
    val fullCrossScalaVersions = crossScalaVersions ++ Seq(scala3)

    // Test dependencies
    val catsEffectScalaTest = "1.3.0"
    val mockitoScala = "1.16.46"
    val scalatest = "3.2.10"

    // Direct dependencies
    val catsCore = "2.6.1"
    val catsEffect = "3.2.9"
    val circe = "0.14.1"
    val fs2 = "3.2.1"
    val log4j = "2.14.1"
    val mutationTestingElements = "1.7.5"
    val mutationTestingMetrics = "1.7.5"
    val pureconfig = "0.17.0"
    val scalameta = "4.4.29"
    val sttp = "3.3.16"
    val testInterface = "1.0"
    val weaponRegeX = "0.6.0"
  }

  object test {
    val catsEffectScalaTest =
      Def.setting("org.typelevel" %%% "cats-effect-testing-scalatest" % versions.catsEffectScalaTest % Test)
    val mockitoScala = "org.mockito" %% "mockito-scala-scalatest" % versions.mockitoScala % Test
    val mockitoScalaCats = "org.mockito" %% "mockito-scala-cats" % versions.mockitoScala % Test
    val scalatest = Def.setting("org.scalatest" %%% "scalatest" % versions.scalatest % Test)
  }

  val catsCore = Def.setting("org.typelevel" %%% "cats-core" % versions.catsCore)
  val catsEffect = Def.setting("org.typelevel" %%% "cats-effect" % versions.catsEffect)
  val circeCore = Def.setting("io.circe" %%% "circe-core" % versions.circe)
  val fs2Core = Def.setting("co.fs2" %%% "fs2-core" % versions.fs2)
  val fs2IO = Def.setting("co.fs2" %%% "fs2-io" % versions.fs2)
  val log4j = "org.apache.logging.log4j" % "log4j-slf4j-impl" % versions.log4j
  val mutationTestingElements = "io.stryker-mutator" % "mutation-testing-elements" % versions.mutationTestingElements
  val mutationTestingMetrics =
    Def.setting("io.stryker-mutator" %%% "mutation-testing-metrics-circe" % versions.mutationTestingMetrics)
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % versions.pureconfig
  val pureconfigSttp = "com.github.pureconfig" %% "pureconfig-sttp" % versions.pureconfig
  val scalameta = Def.setting("org.scalameta" %%% "scalameta" % versions.scalameta)
  val scalapbRuntime =
    Def.setting("com.thesamet.scalapb" %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf")
  val sttpCirce = Def.setting("com.softwaremill.sttp.client3" %%% "circe" % versions.sttp)
  val sttpFs2Backend = "com.softwaremill.sttp.client3" %% "httpclient-backend-fs2" % versions.sttp
  val sttpCatsBackend = Def.setting("com.softwaremill.sttp.client3" %%% "cats" % versions.sttp)
  val testInterface = "org.scala-sbt" % "test-interface" % versions.testInterface
  val weaponRegeX = Def.setting("io.stryker-mutator" %%% "weapon-regex" % versions.weaponRegeX)

}

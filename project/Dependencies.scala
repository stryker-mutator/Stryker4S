import sbt.*

object Dependencies {
  object versions {
    val scala212 = "2.12.17"
    val scala213 = "2.13.10"
    val scala3 = "3.2.1"

    /** Cross-versions for main projects
      */
    val crossScalaVersions = Seq(scala213, scala212)

    /** Fuller cross-versions (used for injected packages like stryker4s-api and sbt-stryker4s-testrunner)
      */
    val fullCrossScalaVersions = crossScalaVersions ++ Seq(scala3)

    // Test dependencies
    val catsEffectScalaTest = "1.5.0"
    val mockitoScala = "1.17.12"
    val scalatest = "3.2.15"

    // Direct dependencies
    val catsCore = "2.9.0"
    val catsEffect = "3.4.5"
    val circe = "0.14.3"
    val fansi = "0.4.0"
    val fs2 = "3.5.0"
    val mutationTestingElements = "1.7.11"
    val mutationTestingMetrics = "1.7.11"
    val pureconfig = "0.17.2"
    val scalameta = "4.7.3"
    val slf4j = "2.0.6"
    val sttp = "3.8.8"
    val testInterface = "1.0"
    val weaponRegeX = "1.0.3"
  }

  object test {
    val catsEffectScalaTest = "org.typelevel" %% "cats-effect-testing-scalatest" % versions.catsEffectScalaTest % Test
    val mockitoScala = "org.mockito" %% "mockito-scala-scalatest" % versions.mockitoScala % Test
    val mockitoScalaCats = "org.mockito" %% "mockito-scala-cats" % versions.mockitoScala % Test
    val scalatest = "org.scalatest" %% "scalatest" % versions.scalatest % Test
  }

  val catsCore = "org.typelevel" %% "cats-core" % versions.catsCore
  val catsEffect = "org.typelevel" %% "cats-effect" % versions.catsEffect
  val circeCore = "io.circe" %% "circe-core" % versions.circe
  val fansi = "com.lihaoyi" %% "fansi" % versions.fansi
  val fs2Core = "co.fs2" %% "fs2-core" % versions.fs2
  val fs2IO = "co.fs2" %% "fs2-io" % versions.fs2
  val mutationTestingElements = "io.stryker-mutator" % "mutation-testing-elements" % versions.mutationTestingElements
  val mutationTestingMetrics =
    "io.stryker-mutator" %% "mutation-testing-metrics-circe" % versions.mutationTestingMetrics
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % versions.pureconfig
  val pureconfigSttp = "com.github.pureconfig" %% "pureconfig-sttp" % versions.pureconfig
  val scalameta = "org.scalameta" %% "scalameta" % versions.scalameta
  val scalapbRuntime =
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
  val slf4j = "org.slf4j" % "slf4j-simple" % versions.slf4j
  val sttpCirce = "com.softwaremill.sttp.client3" %% "circe" % versions.sttp
  val sttpFs2Backend = "com.softwaremill.sttp.client3" %% "fs2" % versions.sttp
  val testInterface = "org.scala-sbt" % "test-interface" % versions.testInterface
  val weaponRegeX = "io.stryker-mutator" %% "weapon-regex" % versions.weaponRegeX

}

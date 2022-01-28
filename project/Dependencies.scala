import sbt.*

object Dependencies {
  object versions {
    val scala212 = "2.12.15"
    val scala213 = "2.13.8"
    val scala3 = "3.1.1"

    /** Cross-versions for main projects
      */
    val crossScalaVersions = Seq(scala213, scala212)

    /** Fuller cross-versions (used for injected packages like stryker4s-api and sbt-stryker4s-testrunner)
      */
    val fullCrossScalaVersions = crossScalaVersions ++ Seq(scala3)

    // Test dependencies
    val catsEffectScalaTest = "1.4.0"
    val mockitoScala = "1.17.0"
    val scalatest = "3.2.11"

    // Direct dependencies
    val catsCore = "2.7.0"
    val catsEffect = "3.3.4"
    val circe = "0.14.1"
    val fansi = "0.3.0"
    val fs2 = "3.2.4"
    val mutationTestingElements = "1.7.8"
    val mutationTestingMetrics = "1.7.8"
    val pureconfig = "0.17.1"
    val scalameta = "4.4.33"
    val slf4j = "1.7.35"
    val sttp = "3.4.1"
    val testInterface = "1.0"
    val weaponRegeX = "0.6.0"
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
  val sttpFs2Backend = "com.softwaremill.sttp.client3" %% "httpclient-backend-fs2" % versions.sttp
  val testInterface = "org.scala-sbt" % "test-interface" % versions.testInterface
  val weaponRegeX = "io.stryker-mutator" %% "weapon-regex" % versions.weaponRegeX

}

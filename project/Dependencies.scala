import sbt._

object Dependencies {

  object versions {
    val scala212 = "2.12.8"

    val scalameta = "4.1.5"
    val pureconfig = "0.10.2"
    val scalatest = "3.0.7"
    val mockitoScala = "1.2.4"
    val betterFiles = "3.7.1"
    val log4j = "2.11.2"
    val grizzledSlf4j = "1.3.3"
    val everitJsonSchema = "1.11.1"
    val circe = "0.11.1"
    val mutationTestingElements = "1.0.2" // When updating, change the version both here and in `core/src/main/scala/stryker4s/report/HtmlReporter.scala`
  }

  object test {
    val scalatest = "org.scalatest" %% "scalatest" % versions.scalatest % Test
    val mockitoScala = "org.mockito" %% "mockito-scala" % versions.mockitoScala % Test
    val everitJsonSchema = "com.github.everit-org.json-schema" % "org.everit.json.schema" % versions.everitJsonSchema % Test
  }

  val pureconfig = "com.github.pureconfig" %% "pureconfig" % versions.pureconfig
  val scalameta = "org.scalameta" %% "scalameta" % versions.scalameta
  val scalametaContrib = "org.scalameta" %% "contrib" % versions.scalameta
  val betterFiles = "com.github.pathikrit" %% "better-files" % versions.betterFiles
  val log4jApi = "org.apache.logging.log4j" % "log4j-api" % versions.log4j
  val log4jCore = "org.apache.logging.log4j" % "log4j-core" % versions.log4j
  val log4jslf4jImpl = "org.apache.logging.log4j" % "log4j-slf4j-impl" % versions.log4j
  val grizzledSlf4j = "org.clapper" %% "grizzled-slf4j" % versions.grizzledSlf4j
  val circeCore = "io.circe" %% "circe-core" % versions.circe
  val circeGeneric = "io.circe" %% "circe-generic" % versions.circe
  val mutationTestingElements = "org.webjars.npm" % "mutation-testing-elements" % versions.mutationTestingElements

}

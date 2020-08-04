package stryker4s.config

import better.files.File
import pureconfig.error.{CannotConvert, ConfigReaderException, ConfigReaderFailures, ConvertFailure}
import stryker4s.config.implicits.ConfigReaderImplicits
import stryker4s.scalatest.LogMatchers
import stryker4s.testutil.{ExampleConfigs, Stryker4sSuite}
import sttp.client.UriContext
import pureconfig.ConfigSource
import pureconfig.generic.auto._

class ConfigReaderTest extends Stryker4sSuite with LogMatchers with ConfigReaderImplicits {
  describe("loadConfig") {
    it("should load stryker4s by type") {
      val configSource = ExampleConfigs.filled

      ConfigReader.readConfigOfType[Config](configSource) match {
        case Left(errors) => fail(errors.toList.mkString(","))
        case Right(config) =>
          config.baseDir shouldBe File("/tmp/project")
          config.mutate shouldBe Seq("bar/src/main/**/*.scala", "foo/src/main/**/*.scala", "!excluded/file.scala")
          config.reporters.loneElement shouldBe Html
          config.excludedMutations shouldBe Set("BooleanLiteral")
          config.thresholds shouldBe Thresholds(high = 85, low = 65, break = 10)
      }
    }

    it("should load config by type and give config errors when sometimes wrong") {
      val configSource = ExampleConfigs.empty

      ConfigReader.readConfigOfType[Config](configSource) match {
        case Left(error) => error.toList.map(a => a.description) shouldBe List("Key not found: 'stryker4s'.")
        case Right(_)    => fail("Config was read successfully which should not be the case.")
      }
    }

    it("should load default config with a nonexistent conf file") {
      val configSource = ExampleConfigs.nonExistentFile

      val result = ConfigReader.readConfig(configSource)

      result.baseDir shouldBe File.currentWorkingDirectory
      result.mutate shouldBe Seq("**/main/scala/**.scala")
      result.reporters should (contain.only(Html, Console))
      result.thresholds shouldBe Thresholds()
      result.dashboard shouldBe DashboardOptions(
        baseUrl = uri"https://dashboard.stryker-mutator.io",
        reportType = Full,
        project = None,
        version = None,
        module = None
      )
    }

    it("should fail on an empty config file") {
      val configSource = ExampleConfigs.empty

      lazy val result = ConfigReader.readConfig(configSource)
      val exc = the[ConfigReaderException[_]] thrownBy result

      "Failures in reading config: " shouldBe loggedAsError
      exc.getMessage() should include("Key not found: 'stryker4s'.")
    }

    it("should fail on an unknown reporter") {
      val configSource = ExampleConfigs.wrongReporter

      lazy val result = ConfigReader.readConfig(configSource)
      val exc = the[ConfigReaderException[_]] thrownBy result

      exc.getMessage() should include("Cannot convert configuration")
    }

    it("should load a config with unknown keys") {
      val configSource = ExampleConfigs.overfilled

      lazy val config = ConfigReader.readConfig(configSource)

      config.baseDir shouldBe File("/tmp/project")
      config.mutate shouldBe Seq("bar/src/main/**/*.scala", "foo/src/main/**/*.scala", "!excluded/file.scala")
      config.reporters.loneElement shouldBe Html
      config.excludedMutations shouldBe Set("BooleanLiteral")
    }

    it("should load a config with customized properties") {
      val configSource = ExampleConfigs.filled

      val result = ConfigReader.readConfig(configSource)

      result.baseDir shouldBe File("/tmp/project")
      result.mutate shouldBe Seq("bar/src/main/**/*.scala", "foo/src/main/**/*.scala", "!excluded/file.scala")
      result.reporters.loneElement shouldBe Html
      result.excludedMutations shouldBe Set("BooleanLiteral")
      result.dashboard shouldBe DashboardOptions(
        baseUrl = uri"https://fakeurl.com",
        reportType = MutationScoreOnly,
        project = Some("someProject"),
        version = Some("someVersion"),
        module = Some("someModule")
      )
    }

    it("should filter out duplicate keys") {
      val configSource = ExampleConfigs.duplicateKeys

      val result = ConfigReader.readConfig(configSource)

      result.reporters.loneElement shouldBe Html
    }

    it("should return a failure on a misshapen excluded-mutations") {
      val configSource = ExampleConfigs.invalidExcludedMutation

      lazy val result = ConfigReader.readConfig(configSource)
      val exc = the[ConfigReaderException[_]] thrownBy result

      val head = exc.failures.head
      head shouldBe a[ConvertFailure]
      val errorMessage =
        s"Cannot convert 'Invalid, StillInvalid, BooleanLiteral' to excluded-mutations: invalid option(s) 'Invalid, StillInvalid'. Valid exclusions are 'EqualityOperator, BooleanLiteral, ConditionalExpression, LogicalOperator, StringLiteral, MethodExpression'."
      errorMessage shouldBe loggedAsError
    }
  }

  describe("logs") {
    it("should log where the config is read from") {
      val configSource = ExampleConfigs.filled

      ConfigReader.readConfig(configSource)

      s"Attempting to read config from stryker4s.conf" shouldBe loggedAsInfo
    }

    it("should log warnings when no config file is found") {
      val configSource = ExampleConfigs.nonExistentFile

      ConfigReader.readConfig(configSource)

      s"Could not find config file ${File.currentWorkingDirectory / "nonExistentFile.conf"}" shouldBe loggedAsWarning
      "Using default config instead..." shouldBe loggedAsWarning
      s"Config used: ${Config.default}" shouldBe loggedAsDebug
    }

    it("should log warnings when unknown keys are used") {
      val configSource = ExampleConfigs.overfilled

      ConfigReader.readConfig(configSource)

      s"""|The following configuration key(s) are not used, they could stem from an older stryker4s version: 'other-unknown-key, unknown-key'.
          |Please check the documentation at https://github.com/stryker-mutator/stryker4s/blob/master/docs/CONFIGURATION.md for available options.""".stripMargin shouldBe loggedAsWarning
    }
  }

  describe("ConfigReaderImplicits") {
    describe("Thresholds") {
      val testValues = List(
        "empty=true" -> Thresholds(),
        "high=85, low=65, break=10" -> Thresholds(high = 85, low = 65, break = 10),
        "high=30, low=30" -> Thresholds(high = 30, low = 30),
        "low=30, break=29" -> Thresholds(low = 30, break = 29),
        "high=100" -> Thresholds(high = 100),
        "high=-1" -> CannotConvert("-1", "thresholds.high", "must be a percentage 0-100"),
        "low=-1" -> CannotConvert("-1", "thresholds.low", "must be a percentage 0-100"),
        "break=-1" -> CannotConvert("-1", "thresholds.break", "must be a percentage 0-100"),
        "high=101" -> CannotConvert("101", "thresholds.high", "must be a percentage 0-100"),
        "low=101" -> CannotConvert("101", "thresholds.low", "must be a percentage 0-100"),
        "break=101" -> CannotConvert("101", "thresholds.break", "must be a percentage 0-100"),
        "high=50,low=51" -> CannotConvert(
          "50",
          "thresholds.high",
          "'high' (50) must be greater than or equal to 'low' (51)"
        ),
        "low=50,break=51" -> CannotConvert("50", "thresholds.low", "'low' (50) must be greater than 'break' (51)"),
        "low=50,break=50" -> CannotConvert("50", "thresholds.low", "'low' (50) must be greater than 'break' (50)")
      )

      testValues.foreach {
        case (config, expected) =>
          it(s"should load $config to expected result") {
            val result = ConfigSource.string(config).load[Thresholds]

            result match {
              case Right(value) => value shouldBe expected
              case Left(ConfigReaderFailures(ConvertFailure(reason, _, _), _*)) =>
                reason shouldBe expected
              case other => fail(s"unexpected value $other")
            }
          }
      }
    }
  }
}

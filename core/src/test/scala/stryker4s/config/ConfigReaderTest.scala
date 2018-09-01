package stryker4s.config

import better.files.File
import ch.qos.logback.classic.Level
import org.scalatest.BeforeAndAfterEach
import pureconfig.error.{ConfigReaderException, ConvertFailure}
import stryker4s.scalatest.FileUtil
import stryker4s.{Stryker4sSuite, TestAppender}

class ConfigReaderTest extends Stryker4sSuite with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    TestAppender.reset()
  }

  describe("loadConfig") {
    it("should load default config with a nonexistent conf file") {
      val confPath = File("nonExistentFile.conf")

      val result = ConfigReader.readConfig(confPath)

      result should equal(Config())
    }

    it("should fail on an empty config file") {
      val confPath = FileUtil.getResource("stryker4sconfs/empty.conf")

      lazy val result = ConfigReader.readConfig(confPath)
      val exc = the[ConfigReaderException[_]] thrownBy result

      exc.getMessage() should include("Key not found: 'stryker4s'.")
    }

    it("should load a config with customized properties") {
      val confPath = FileUtil.getResource("stryker4sconfs/filled.conf")

      val result = ConfigReader.readConfig(confPath)

      val expected = Config(
        files = Seq("bar/src/main/**/*.scala", "foo/src/main/**/*.scala", "!excluded/file.scala"),
        baseDir = File("/tmp/project"),
        logLevel = Level.INFO,
        testRunner = CommandRunner("mvn", "clean test")
      )
      result should equal(expected)
    }

    it("should return a failure on a misshapen test runner") {
      val confPath = FileUtil.getResource("stryker4sconfs/wrongTestRunner.conf")

      lazy val result = ConfigReader.readConfig(confPath)
      val exc = the[ConfigReaderException[_]] thrownBy result

      val head = exc.failures.head
      head shouldBe a[ConvertFailure]
      head.description should equal(
        s"""No valid coproduct choice found for '{"args":"foo","command":"bar","type":"someOtherTestRunner"}'.""")
    }
  }

  describe("logs") {
    it("should log when config file in directory is used") {
      val confPath = FileUtil.getResource("stryker4sconfs/filled.conf")

      ConfigReader.readConfig(confPath)

      "Using stryker4s.conf in the current working directory" shouldBe loggedAsDebug
    }

    it("should log warnings when no config file is found") {
      val confPath = File("nonExistentFile.conf")

      ConfigReader.readConfig(confPath)

      s"Could not find config file ${File.currentWorkingDirectory / "nonExistentFile.conf"}" shouldBe loggedAsWarning
      "Using default config instead..." shouldBe loggedAsWarning
      val defaultConf = Config()
      s"Config used: ${defaultConf.toHoconString}".stripMargin shouldBe loggedAsDebug
    }
  }

  override def afterEach(): Unit = {
    TestAppender.reset()
  }
}

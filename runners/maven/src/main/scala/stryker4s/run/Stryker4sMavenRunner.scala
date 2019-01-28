package stryker4s.run
import org.apache.maven.shared.invoker.{DefaultInvoker, Invoker}
import stryker4s.config.Config
import stryker4s.mutants.findmutants.SourceCollector
import stryker4s.run.process.ProcessRunner

class Stryker4sMavenRunner extends Stryker4sRunner {
  override def resolveRunner(collector: SourceCollector)(implicit config: Config): MutantRunner =
    new MavenMutantRunner(resolveInvoker, ProcessRunner.resolveRunner(), collector)

  private def resolveInvoker: Invoker = new DefaultInvoker

}

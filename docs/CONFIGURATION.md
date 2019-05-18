# Configuration  

All configuration options can be set from the stryker4s.conf file in the root of the project. This file is read in the HOCON-format. All configuration should be in the "stryker4s" namespace.

```conf
stryker4s {
  # Your configuration here
}
```

- [General config](#general-config)
- [Process runner config](#process-runner-config)
- [Other configuration options](#other-configuration-options)

## General config

#### mutate

**Config file:** `mutate: [ "**/main/scala/**/*.scala" ]`  
**Default value:** `[ "**/main/scala/**/*.scala" ]`  
**Mandatory:** No  
**Description:**  
With `mutate` you configure the subset of files to use for mutation testing.
Generally speaking, these should be your own source files.  
The default for this will find files in the common Scala project format.

You can *ignore* files by adding an exclamation mark (`!`) at the start of an expression.

#### files

**Config file:** `files: [ "**/main/scala/**/*.scala" ]`  
**Default value:** result of `git ls-files --others --exclude-standard --cached`  
**Mandatory:** No  
**Description:**  
With `files` you can choose which files should be included in your mutation run sandbox.
This is normally not needed as it defaults to all files not ignored by git. If you do need to override `files` (for example, when your project isn't in a git repository), you can override the `files` config.

You can *ignore* files by adding an exclamation mark (`!`) at the start of an expression.

#### base-dir

**Config file:** `base-dir: '/usr/your/project/folder/here'`  
**Default value:** The directory from which the process is started  
**Mandatory:** No  
**Description:**  
With `base-dir` you specify the directory from which stryker4s starts and searches for mutations. The default for this is the directory from which the project is being run, which should be fine in most cases. This value can also be relative to the current working directory, E.G.: `base-dir: submodule1` to set the base-dir to a submodule of your project.

#### reporters

**Config file:** `reporters: ["console", "html"]`  
**Default value:** The `console` reporter  
**Mandatory:** No  
**Description:**  
With `reporters` you can specify reporters for stryker4s to use. The following reporters are supported:

- `console` will output progress and the final result to the console.
- `html` outputs a nice HTML report to `target/stryker4s-report-$timestamp/index.html`. See the [mutation-testing-elements repo](https://github.com/stryker-mutator/mutation-testing-elements/tree/master/packages/mutation-testing-elements#mutation-testing-elements) for more information.

#### excluded-mutations

**Config file:** `excluded-mutations: ["BooleanLiteral"]`  
**Default value:** `[]`  
**Mandatory:** No  
**Description:**  
With `excluded-mutations`, you can turn off certain mutations in the project. Allowed values are the following:

- `EqualityOperator`
- `BooleanLiteral`
- `ConditionalExpression`
- `LogicalOperator`
- `StringLiteral`
- `MethodExpression`

#### thresholds

**Config file:** `thresholds{ high=80, low=60, break=0 }`  
**Default values:** high=80, low=60, break=0  
**Mandatory:** No  
**Description:**  
Specify the thresholds for mutation scores.

- `mutation score >= high`: Success! Mutation score will be logged at the INFO level.
- `high > mutation score >= low`: Warning! Mutation score will be logged at the WARN level.
- `mutation score < low`: Dangerously low! Mutation score will be logged at the ERROR level with an extra warning.
- `mutation score < break`: Error! Stryker will exit with exit code 1, indicating a build failure.

Setting `break=0` (default value) ensures that the build will never fail.

## Process runner config

#### test-runner

**Config file:** `test-runner: { command: "sbt", args: "test" }`  
**Mandatory:** Yes  
**Description:**  
With `test-runner` you specify how stryker4s can invoke the test runner.  
Examples would be `sbt test`, `mvn test` or any other command to run your tests, including any parameters your tests might need.

*warning* The process runner should only be used when your specific test framework is not supported. Due to performance and predictability reasons.

## Other configuration options

#### log-level

**Default value:** `INFO`  
**Mandatory:** No  
**Description:**  
How to adjust the loglevel depends on how you run stryker4s:

- sbt plugin
  - Add `logLevel in stryker := Level.Debug` to your the in your build.sbt
  - Options: `Debug`, `Info`, `Warn`, `Error`
- Commandrunner
  - Pass the loglevel as a parameter when running, like so: `--debug`
  - Options: `--off`, `--error`, `--warn`, `--info`, `--debug`, `--trace`, `--all` (not case sensitive)
- Maven plugin
  - As a command-line property, like so: `mvn -Dorg.slf4j.simpleLogger.defaultLogLevel=warn stryker4s:run`
  - Options: `trace`, `debug`, `info`, `warn`, or `error`

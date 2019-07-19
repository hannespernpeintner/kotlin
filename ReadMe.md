[![official project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![TeamCity (simple build status)](https://img.shields.io/teamcity/http/teamcity.jetbrains.com/s/Kotlin_dev_Compiler.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=Kotlin_dev_Compiler&branch_Kotlin_dev=%3Cdefault%3E&tab=buildTypeStatusDiv)
[![Maven Central](https://img.shields.io/maven-central/v/org.jetbrains.kotlin/kotlin-maven-plugin.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.jetbrains.kotlin%22)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

# Prototype implementation of companion vals

This repository is a fork of the official Kotlin project where I added a feature called companion vals to the language.
The proposal was not created by me and can be found on https://github.com/Kotlin/KEEP/pull/106

I therefore changed some internals of the Kotlin compiler and mostly added some scopes and resolutions here and there.
Some changes are heavily inspired by or even copy-pasted from code from the typeclasses proposal in KEEP-87.
Without said contributions from people like truizlop and JorgeCastilloPrz, I wouldn't have made it that far, so I want to say THANKS a lot.

Furthermore, I want to stress that I don't want to compete in any way with the typeclasses proposal, of which I am really convinced of as now.
Still, the implicit value passing mechanism in typeclasses proposal seems to be very controversial, and I see companion vals as a possible
alternative that solves at least the "problem" of the need for with(xxx) {} for local extension functions and the compound extension
use case from another proposal that can be found on https://github.com/Kotlin/KEEP/pull/176.

# Companion vals

It's not too clear for me, what the original proposal contains, so I implemented my own vision of what it could look like.
In short, at different places (function signatures, lambda headers, top levels, class bodies etc.), one should be able to
define a val or a parameter as a companion (similar to companion objects that already exist in Kotlin) and use it as a receiver
in the corresponding scope.

## Examples

Examples of what this feature is all about can be found in the tests folder "compiler/testData/codegen/box/companionval".

Here are two nice and short examples what companion vals could enable:

```kotlin
class Printer<T> {
    fun T.customToString() = "printed:" + this.toString()
}

class Foo(val someString: String)


fun main() {
    companion val printer: Printer<String> = Printer()

    val result = Foo("bar").someString.customToString()

    assert(result == "printed:bar")
}
```

```kotlin
class ServerBackend {
    fun <T> executeInTransaction(action: ActionContext.() -> T) {}
}

class Printer {
    fun Any.customToString(): String = "custom: $this"
}

class ActionContext(companion val coroutineScope: CoroutineScope,
                    companion val printer: Printer)

fun main() {

    ServerBackend().executeInTransaction {
        async { println("I am async") }
        println("foo".customToString())
    }
}
```

# Missing things

* I wasn't able to implement nested lambdas correctly. In the example above for instance, one should be able to nest async and
customToString calls but I encountered problems with the stack to access the fields.
* The proposal mentions that members of accessible companion properties of a class should be accessible without qualifying the property,
in the like of `ActionContext().launch {}` in the example above. Haven't had time to implement this yet, but does work in extension
functions, as shown.

# Test it

If you already used the Kotlin repository, you should be able to use my fork in the same way. Just open the project,
set all environment variables (see official instructions below) and use the IDEA gradle task to open an IDE.

Alternatively, you can tell me what files from the dist folder I have to include in a zip archive and then I will upload a snapshot :)

# Kotlin Programming Language

Welcome to [Kotlin](https://kotlinlang.org/)! Some handy links:

 * [Kotlin Site](https://kotlinlang.org/)
 * [Getting Started Guide](https://kotlinlang.org/docs/tutorials/getting-started.html)
 * [Try Kotlin](https://play.kotlinlang.org/)
 * [Kotlin Standard Library](https://kotlinlang.org/api/latest/jvm/stdlib/index.html)
 * [Issue Tracker](https://youtrack.jetbrains.com/issues/KT)
 * [Forum](https://discuss.kotlinlang.org/)
 * [Kotlin Blog](https://blog.jetbrains.com/kotlin/)
 * [Follow Kotlin on Twitter](https://twitter.com/kotlin)
 * [Public Slack channel](https://slack.kotlinlang.org/)
 * [TeamCity CI build](https://teamcity.jetbrains.com/project.html?tab=projectOverview&projectId=Kotlin)

## Editing Kotlin

 * [Kotlin IntelliJ IDEA Plugin](https://kotlinlang.org/docs/tutorials/getting-started.html)
 * [Kotlin Eclipse Plugin](https://kotlinlang.org/docs/tutorials/getting-started-eclipse.html)
 * [Kotlin Sublime Text Package](https://github.com/vkostyukov/kotlin-sublime-package)

## Build environment requirements

In order to build Kotlin distribution you need to have:

- JDK 1.6, 1.7, 1.8 and 9
- Setup environment variables as following:

        JAVA_HOME="path to JDK 1.8"
        JDK_16="path to JDK 1.6"
        JDK_17="path to JDK 1.7"
        JDK_18="path to JDK 1.8"
        JDK_9="path to JDK 9"

For local development, if you're not working on bytecode generation or the standard library, it's OK to have only JDK 1.8 and JDK 9 installed, and to point JDK_16 and JDK_17 environment variables to your JDK 1.8 installation.

You also can use [Gradle properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties) to setup JDK_* variables.

> Note: The JDK 6 for MacOS is not available on Oracle's site. You can [download it here](https://support.apple.com/kb/DL1572).

On Windows you might need to add long paths setting to the repo:

    git config core.longpaths true 

## Building

The project is built with Gradle. Run Gradle to build the project and to run the tests 
using the following command on Unix/macOS:

    ./gradlew <tasks-and-options>
    
or the following command on Windows:

    gradlew <tasks-and-options>

On the first project configuration gradle will download and setup the dependencies on

* `intellij-core` is a part of command line compiler and contains only necessary APIs.
* `idea-full` is a full blown IntelliJ IDEA Community Edition to be used in the plugin module.

These dependencies are quite large, so depending on the quality of your internet connection 
you might face timeouts getting them. In this case you can increase timeout by specifying the following 
command line parameters on the first run: 
    
    ./gradlew -Dhttp.socketTimeout=60000 -Dhttp.connectionTimeout=60000

## Important gradle tasks

- `clean` - clean build results
- `dist` - assembles the compiler distribution into `dist/kotlinc/` folder
- `ideaPlugin` - assembles the Kotlin IDEA plugin distribution into `dist/artifacts/Kotlin` folder
- `install` - build and install all public artifacts into local maven repository
- `runIde` - build IDEA plugin and run IDEA with it
- `coreLibsTest` - build and run stdlib, reflect and kotlin-test tests
- `gradlePluginTest` - build and run gradle plugin tests
- `compilerTest` - build and run all compiler tests
- `ideaPluginTest` - build and run all IDEA plugin tests

**OPTIONAL:** Some artifacts, mainly Maven plugin ones, are built separately with Maven.
Refer to [libraries/ReadMe.md](libraries/ReadMe.md) for details.


### Building for different versions of IntelliJ IDEA and Android Studio

Kotlin plugin is intended to work with several recent versions of IntelliJ IDEA and Android Studio. Each platform is allowed to have a different set of features and might provide a slightly different API. Instead of using several parallel Git branches, project stores everything in a single branch, but files may have counterparts with version extensions (\*.as32, \*.172, \*.181). The primary file is expected to be replaced with its counterpart when targeting non-default platform.

More detailed description of this scheme can be found at https://github.com/JetBrains/bunches/blob/master/ReadMe.md.

Usually, there's no need to care about multiple platforms as all features are enabled everywhere by default. Additional counterparts should be created if there's an expected difference in behavior or an incompatible API usage is required **and** there's no reasonable workaround to save source compatibility. Kotlin plugin contains a pre-commit check that shows a warning if a file has been updated without its counterparts.

Development for some particular platform is possible after 'switching' that can be done with [Bunch Tool](https://github.com/JetBrains/bunches/releases) from the command line.

```sh
cd kotlin-project-dir

# switching to IntelliJ Idea 2018.2
bunch switch . 182
```

## <a name="working-in-idea"></a> Working with the project in IntelliJ IDEA

Working with the Kotlin project requires at least IntelliJ IDEA 2017.3. You can download IntelliJ IDEA 2017.3 [here](https://www.jetbrains.com/idea/download).

After cloning the project, to import the project in Intellij choose the project directory in the Open project dialog. Then, after project opened, Select 
`File` -> `New...` -> `Module from Existing Sources` in the menu, and select `build.gradle.kts` file in the project's root folder.

In the import dialog, select `use default gradle wrapper`.

To be able to run tests from IntelliJ easily, check `Delegate IDE build/run actions to Gradle` and choose `Gradle Test Runner` in the Gradle runner settings after importing the project.

At this time, you can use the latest released 1.2.x version of the Kotlin plugin for working with the code. To make sure you have the latest version installed, use Tools | Kotlin | Configure Kotlin Plugin Updates and press "Check for updates now".

### Compiling and running

From this root project there are Run/Debug Configurations for running IDEA or the Compiler Tests for example; so if you want to try out the latest and greatest IDEA plugin

* VCS -> Git -> Pull
* Run the "IDEA" run configuration in the project
* a child IntelliJ IDEA with the Kotlin plugin will then startup

### Including into composite build

To include kotlin compiler into [composite build](https://docs.gradle.org/current/userguide/composite_builds.html) you need to define `dependencySubstitution` for `kotlin-compiler` module in `settings.gradle`

```
includeBuild('/path/to/kotlin') {
    dependencySubstitution {
        substitute module('org.jetbrains.kotlin:kotlin-compiler') with project(':include:kotlin-compiler')
    }
}
```

# Contributing

Please be sure to review Kotlin's [contributing guidelines](docs/contributing.md) to learn how to help the project.

# aotc-gradle-plugin
Plugin for simplifying use of [Ahead-of-Time Compilation] in [Gradle] projects.

> For those not able (or brave enough) to go fully native with Graal on SubstrateVM.

The plugin facilitates generation of a compiled library for __the code which is executed during unit testing__. The resulting library is passed (together with the jars) to the JVM on startup. The JVM then loads precompiled classes from the library before falling back to the jars whenever no precompiled class is found.

> Ahead-of-time compilation technically corresponds to doing the JVM Hotspot compilation (also known as __warmup__) during the build process instead of when starting the application. In other words, the application will __hit the ground running__ when starting up.

While it is relatively simple to apply AOT compilation to all your code and dependencies (including the Java base module), this usually produces rather large compiled libraries (in the hundreds of MB range, 1GB for the JDK) which consume unnecessary resources. By using a more targeted approach, this plugin generates smaller compiled libraries. 

The plugin requires Java 11 or higher and only works on Linux. Works with all libraries of out the box. 

## License
[Apache 2.0]

## Usage
Enable plugin using

```
plugins {
    id "com.github.skjolber.aotc" version "1.0.1"
}
```

Run the command

```
./gradlew aotcLibrary --info 
```

for a compiled library at

```
./build/aotc/aotLibrary.so
```

Then add the following parameters when starting your application:

```
-XX:AOTLibrary=./build/aotc/aotLibrary.so
```

To print the actual classes loaded from the library, also add

```
-XX:+UnlockDiagnosticVMOptions -XX:+PrintAOT
```

As `aotcLibrary` can take a few minutes to complete, it does not run during normal commands like `compile` or `build`. Ideally this task only runs on a build server. 

## Details
Configuration options:

```
aotc {
    garbageCollector = 'g1' // alternatively 'default' or 'parallel'
    memory = '1024m' // extra XMX memory for the compilation command
    tiered = true // tiered compilation
    additionalCommands = files('exclude.txt') // one or more files for additional compile commands
    ignoreErrors = true // ignore errors during compilation
    captureMode = 'jcmd' // alternatively 'console' or 'mbean' 
}
```

The garbage collector must be the same during compilation and runtime. To guard against misconfiguration, also add

```
-XX:+UnlockDiagnosticVMOptions -XX:+UseAOTStrictLoading 
```
for a fail fast result.

Note that the new ZGC garbage collector cannot be used with AOT compilation for now.

#### Capture mode
It turns out that capturing the touched methods via console for now means it is all dumped in the console, which is not very user-friendly if you're looking to scroll up in the console for your test outputs. 

Dumping to console does however produce a bit smaller output for the smaller applications, since then there is no JMX and instrumentation agent involved (which touches methods during execution). 

For larger applications however, for example for Spring, all capture methods produce approximately the same output, since those additional classes are in use anyhow.

## Startup time
Depending on the application, somewhere between 5% and 10% improved startup time is to be expected. Extensive use of reflection will tend to reduce this number.

## Using JVM experimental features, is it safe?
AOT compilation was a previously commercially available feature (from Java 9) now available for free in OpenJDK. Experimental features have traditionally been quite stable in the JVM. 

The AOT compiler relies on the new [Graal JIT compiler](https://www.baeldung.com/graal-java-jit-compiler), which can also be enabled without AOT compilation (as a regular Hotspot compiler) using

```
-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler
```

Some people are running the new Graal JIT compiler in production (see links). The natural step after AOT compilation is going to [GraalVM] for a fully native runtime with its Substrate VM. GraalVM is however not yet production ready and in addition many libraries will need some adjustments to work 100% as well.

### Verifying compiled library
For re-running the unit test with the compiled library enabled, for example after first running

```
./gradlew build aotcLibrary
```

add

```
test {
    if (project.hasProperty('aotLibrary') && Boolean.parseBoolean(aotLibrary)) {
        jvmArgs '-XX:AOTLibrary=./build/aotc/aotLibrary.so'
    }
}
```

to `build.gradle` and run

```
./gradlew cleanTest test -PaotLibrary=true --info
```

# Get involved
If you have any questions, comments or improvement suggestions, please file an issue or submit a pull-request.

# Links
Links related to AOT compilation.

 * Compile ahead of time. It's fine? by Dmitry Chuyko: [Slides](https://assets.contentful.com/oxjq45e8ilak/3VZgJf2jLWaQQGKaeSsecc/a015330e94f964d96df0b366321ec068/Dmitry_Chuyko_AOT.pdf)
 * [JEP 295: Ahead-of-Time Compilation](http://openjdk.java.net/jeps/295)
 * https://github.com/oracle/graal
 * [Fast JVM startup with JDK 9](http://blog.gilliard.lol/2017/10/02/JVM-startup.html)
 * [Twitter’s Quest for a Wholly Graal Runtime](https://www.youtube.com/watch?v=G-vlQaPMAxg)
 * (JVMLS 2017): Ahead Of Time (AOT) Internals by Vladimir Kozlov and Igor Veresov: [Video](https://www.youtube.com/watch?v=yyDD_KRdQQU&list=PLX8CzqL3ArzXJ2EGftrmz4SzS6NRr6p2n&index=13)
 * [Programmatic jcmd Access](https://www.javacodegeeks.com/2016/05/programmatic-jcmd-access.html)

 - 1.0.0: Initial version

[Apache 2.0]:                      http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:                   https://github.com/skjolber/gradle-foss-library-template/issues
[Gradle]:                          https://gradle.org/
[Ahead-of-Time Compilation]:       https://openjdk.java.net/jeps/295
[GraalVM]:                         https://www.graalvm.org/


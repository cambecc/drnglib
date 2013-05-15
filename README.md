##Overview

**drnglib** (Digital Random Number Generator Library) is a Java 7 library that provides access to _Intel Secure Key_,
the hardware random number generator introduced in the Ivy Bridge microarchitecture. This library retrieves
cryptographically secure random values directly from the CPU using the `rdrand` instruction. Access to `rdrand` is
implemented by a small native library embedded as a resource in the .jar file.

The **DigitalRandom** class is provided as a drop-in replacement for
[SecureRandom](http://docs.oracle.com/javase/7/docs/api/java/security/SecureRandom.html). It is thread-safe and
stateless. All thread synchronization occurs in the hardware implementation of `rdrand`.

For more information see:
* [Wikipedia: RdRand](http://en.wikipedia.org/wiki/RdRand)
* [Intel DRNG Software Implementation Guide](http://software.intel.com/en-us/articles/intel-digital-random-number-generator-drng-software-implementation-guide)
* [Analysis of Intel's Ivy Bridge Digital Random Number Generator](http://www.cryptography.com/public/pdf/Intel_TRNG_Report_20120312.pdf) `PDF`

##Usage

Follow these two steps to start generating random numbers:

1. Add the following dependency to your project ([published on Maven Central](http://search.maven.org/#search|ga|1|a%3A%22drnglib%22%20g%3A%22net.nullschool%22)):
```xml
    <dependency>
        <groupId>net.nullschool</groupId>
        <artifactId>drnglib</artifactId>
        <version>0.9.1</version>
    </dependency>
```

2. Create an instance of `DigitalRandom` located in the `net.nullschool.util` package:
```java

    DigitalRandom random = new DigitalRandom();
    System.out.println(random.nextInt());
```

Your project will need [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and an
[Intel Ivy Bridge CPU](http://en.wikipedia.org/wiki/Ivy_Bridge_%28microarchitecture%29). If the CPU does not contain
a hardware random number generator, instantiation of `DigitalRandom` will throw `UnsupportedOperationException`.

**drnglib** runs on Windows, Mac OSX, and Linux. (Adding support for other operation systems simply requires building
the native library code on the desired platform.)

##Performance

The following micro-benchmarks show the relative performance of the JRE's implementations of `Random`
compared with `DigitalRandom`. These numbers were gathered using the benchmark harness contained in the test
class `PerformanceTest` under the following conditions:

* Intel Core i7 3770S (quad core with hyper-threading enabled)
* Windows 7
* Java 1.7.0_11 64-bit
* -Xms1G -Xmx2G -XX:+PrintCompilation -XX:+PrintGCDetails
* Each thread provided its own RNG instance; no sharing.

### Benchmark 1: Sustained generation rate of 64-bit longs (`Random.nextLong`)
_MiB/sec Mean (Standard Deviation)_ over 20 trials after JVM warm up

    +---------++-----------+-------------------++--------------+---------------+--------+
    | Threads ||  Random   | ThreadLocalRandom || SecureRandom | DigitalRandom | Speed* |
    +---------++-----------+-------------------++--------------+---------------+--------+
    |    1    || 379   (0) |     2965   (0)    ||    40 (0)    |    192 (2)    |  4.8x  |
    |    2    || 528 (263) |     5929   (0)    ||    75 (3)    |    382 (2)    |  5.0x  |
    |    3    || 340  (69) |     8836  (32)    ||   100 (6)    |    569 (4)    |  5.7x  |
    |    4    || 414 (118) |    11654  (78)    ||   129 (6)    |    747 (4)    |  5.8x  |
    |    5    || 422  (29) |    14193 (103)    ||   141 (5)    |    760 (0)    |  5.4x  |
    |    6    || 478  (37) |    16812 (462)    ||   147 (3)    |    760 (0)    |  5.2x  |
    |    7    || 515  (29) |    19482 (812)    ||   158 (3)    |    760 (0)    |  4.8x  |
    |    8    || 561  (43) |    21528 (911)    ||   167 (2)    |    758 (0)    |  4.5x  |
    +---------++-----------+-------------------++--------------+---------------+--------+
*speed improvement of DigitalRandom over SecureRandom

### Benchmark 2: Sustained fill rate of 2MiB arrays (`Random.nextBytes`)
_MiB/sec Mean (Standard Deviation)_ over 20 trials after JVM warm up

    +---------++-----------+-------------------++--------------+---------------+--------+
    | Threads ||  Random   | ThreadLocalRandom || SecureRandom | DigitalRandom | Speed* |
    +---------++-----------+-------------------++--------------+---------------+--------+
    |    1    || 370   (0) |      644   (0)    ||    59  (0)   |    205 (2)    |  3.5x  |
    |    2    || 652 (203) |     1287   (0)    ||   114  (6)   |    400 (3)    |  3.5x  |
    |    3    || 841 (342) |     1791  (59)    ||   155 (11)   |    576 (8)    |  3.7x  |
    |    4    || 910 (435) |     2092 (114)    ||   174 (11)   |    669 (6)    |  3.8x  |
    |    5    || 935 (470) |     2224 (129)    ||   185 (11)   |    673 (7)    |  3.6x  |
    |    6    || 750 (375) |     2324  (64)    ||   200  (8)   |    686 (8)    |  3.4x  |
    |    7    || 854 (378) |     2460  (41)    ||   213  (7)   |    702 (6)    |  3.3x  |
    |    8    || 694 (107) |     2508  (37)    ||   229  (6)   |    743 (8)    |  3.2x  |
    +---------++-----------+-------------------++--------------+---------------+--------+
*speed improvement of DigitalRandom over SecureRandom

### Benchmark 3: Sustained `UUID` generation rate
_Millions UUID/sec Mean (Standard Deviation)_ over 20 trials after JVM warm up

    +---------+-----------------+------------------------+---------+
    | Threads | UUID.randomUUID | DigitalRandom.nextUUID |  Speed* |
    +---------+-----------------+------------------------+---------+
    |    1    |    3.4 (0.0)    |       12.4 (0.2)       |   3.6x  |
    |    2    |    2.5 (0.1)    |       18.5 (3.4)       |   7.4x  |
    |    3    |    2.4 (0.1)    |       26.8 (3.8)       |  11.2x  |
    |    4    |    2.3 (0.0)    |       34.7 (3.7)       |  15.1x  |
    |    5    |    2.3 (0.0)    |       41.8 (2.7)       |  18.2x  |
    |    6    |    2.3 (0.0)    |       44.5 (1.3)       |  19.3x  |
    |    7    |    2.3 (0.0)    |       45.7 (0.7)       |  19.9x  |
    |    8    |    2.3 (0.0)    |       46.7 (0.7)       |  20.3x  |
    +---------+-----------------+------------------------+---------+
*speed improvement of DigitalRandom over SecureRandom

### Interpreting the results

`DigitalRandom` significantly outperforms `SecureRandom`, and even `Random` in some scenarios. `SecureRandom` on
Windows defaults to the SHA1PRNG algorithm, but its implementation suffers from fairly trivial issues, such as the
need to generate _all_ values into `byte[]` buffers, even when a simple `int` or `long` is requested. Linux machines
suffer further because `SecureRandom`'s default algorithm gathers entropy from `/dev/random`, which can be very slow.

Java 7 clearly benefits from the introduction of `ThreadLocalRandom`, but it is important to note it does not produce
randomness of cryptographic quality. It is an excellent replacement for `Random`, not `SecureRandom`.

The performance improvement `DigitalRandom` provides when generating `UUID`s is particularly striking. This is because
`UUID.randomUUID()`, which is the JRE garden path method for generating UUIDs, uses exactly one static instance of
`SecureRandom`. Sharing one instance of `SecureRandom` causes severe thread contention, whereas `DigitalRandom`
needs no thread synchronization.

##Build Requirements

###To build drnglib jar:

* [Java 7 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven 3](http://maven.apache.org/)
* [git](http://git-scm.com/)

###To build drnglib's native libraries:

* [gcc 4.2](http://gcc.gnu.org/) or greater (Unix/Mac OSX)
* [Visual Studio 2012](http://www.microsoft.com/visualstudio/eng/downloads#d-2012-express) any flavor (Windows)

##Build Instructions

To build the Java jar:

1. get the source: `git clone git@github.com:cambecc/drnglib.git`
2. from the `drnglib/` directory, do a maven build: `mvn clean install`

To build the native libraries:

NOTE: It is not necessary to build the native libraries unless you have made changes to the C code in the
`drnglib/src/main/c/` directory. Building the native libraries is somewhat tedious because each library flavor
must be built from a toolset for the associated operating system. This usually means a Windows install for .dll,
Mac OSX for .dylib, and Linux for .so. To build one particular flavor:

1. make sure the `JAVA_HOME` environment variable is defined to point to your Java 7 JDK
2. from the `drnglib/src/main/c/` directory, invoke the appropriate build script, e.g., `./build-macosx.sh`
3. rebuild the java jar to incorporate your native library. From the `drnglib/` directory, do a maven build:
`mvn clean install`

On Ubuntu, needed to get the following packages in order to build:

    sudo apt-get install libc6-dev-i386
    sudo apt-get install g++-multilib

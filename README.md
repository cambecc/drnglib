##Overview

**drnglib** (Digital Random Number Generator Library) is a Java library that provides access to _Intel Secure Key_, the
hardware random number generator introduced in the Ivy Bridge microarchitecture. This library retrieves cryptographically
secure random values directly from the CPU using the `RDRAND` instruction. Access to `RDRAND` is implemented by a small
native library embedded as a resource in the .jar file.

The **DigitalRandom** class is provided as a drop-in replacement for
[SecureRandom](http://docs.oracle.com/javase/7/docs/api/java/security/SecureRandom.html). It is thread-safe and stateless.
All thread synchronization occurs in the hardware implementation of `RDRAND`.

For more information see:
* [Wikipedia: RDRAND](http://en.wikipedia.org/wiki/RDRAND)
* [Intel DRNG Software Implementation Guide](http://software.intel.com/en-us/articles/intel-digital-random-number-generator-drng-software-implementation-guide)
* [Analysis of Intel's Ivy Bridge Digital Random Number Generator](http://www.cryptography.com/public/pdf/Intel_TRNG_Report_20120312.pdf) `PDF`

##Usage

1. Add the following maven dependency to your project:
```xml
    <dependency>
        <groupId>net.nullschool</groupId>
        <artifactId>drnglib</artifactId>
        <version>0.9.0</version>
    </dependency>
```

2. Create an instance of `DigitalRandom` located in the `net.nullschool.util` package:
```java

    DigitalRandom random = new DigitalRandom();
    System.out.println(random.nextInt());
```
    
##Requirements

###To build drnglib jar:

* [Intel Ivy Bridge CPU](http://en.wikipedia.org/wiki/Ivy_Bridge_%28microarchitecture%29)
* [Java 7 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven 3](http://maven.apache.org/)
* [git](http://git-scm.com/)

###To build drnglib's native libraries:

* [g++ 4.2](http://gcc.gnu.org/) or greater (Unix/Mac OSX)
* [Visual Studio 2012](http://www.microsoft.com/visualstudio/eng/downloads#d-2012-express) any flavor (Windows)

##Build Instructions

To build the Java jar:

1. get the source: `git clone git@github.com:cambecc/drnglib.git`
2. from the `drnglib/` directory, do a maven build: `mvn clean install`

To build the native libraries:

NOTE: It is not necessary to build the native libraries unless you have made changes to the C++ code in the
`drnglib/src/main/c++/` directory. Building the native libraries is somewhat tedious because each library flavor
must be built from a toolset for the associated operating system. This usually means a Windows install for .dll,
Mac OSX for .dylib, and Linux for .so. To build one particular flavor:

1. make sure the `JAVA_HOME` environment variable is defined to point to your Java 7 JDK
2. from the `drnglib/src/main/c++/` directory, invoke the appropriate build script, e.g., `./build-osx.sh`
3. rebuild the java jar to incorporate your native library. From the `drnglib/` directory, do a maven build:
`mvn clean install`

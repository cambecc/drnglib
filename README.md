##Overview

drnglib (Digital Random Number Generator Library) is a Java library that provides access to Intel Secure Key,
a hardware random number generator introduced in Ivy Bridge processors.

###See:

* http://software.intel.com/en-us/articles/intel-digital-random-number-generator-drng-software-implementation-guide
* http://www.cryptography.com/public/pdf/Intel_TRNG_Report_20120312.pdf

##Requirements

###To build drnglib jar:

* [Intel IvyBridge CPU](http://en.wikipedia.org/wiki/Ivy_Bridge_%28microarchitecture%29)
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

NOTE: It is not necessary to build the native libraries unless you have made changes to the C++ code in the `src/main/c++/` directory. Building the native libraries is somewhat tedious because each library flavor must be built from a toolset for the associated operating system. This usually means a Windows install for .dll, Mac OSX for .dylib, and Linux for .so. To build one particular flavor:

1. make sure the `JAVA_HOME` environment variable is defined to point to your Java 7 JDK
2. from the `drnglib/src/main/c++/` directory, invoke the appropriate build script, e.g., `./build-osx.sh`
3. rebuild the java jar to incorporate your native library. From the `drnglib/` directory, do a maven build: `mvn clean install`

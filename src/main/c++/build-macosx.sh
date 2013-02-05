# TODO: convert to make
set -e
set -x
gcc -Wall -std=c99 -m64 -dynamiclib -fast -fPIC -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin *.c -o drnglib-x64.dylib
mv drnglib-x64.dylib ../resources/net/nullschool/util/macosx

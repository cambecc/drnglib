# TODO: convert to make
set -e
set -x
g++ -m64 -dynamiclib -fast -fPIC -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin *.cpp -o drnglib-x64.dylib
mv drnglib-x64.dylib ../resources/net/nullschool/util/macosx

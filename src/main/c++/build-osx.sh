set -e
set -x
g++ -m64 -dynamiclib -fast -fPIC -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin *.cpp -o drnglib_x64.dylib
mv drnglib_x64.dylib ../resources/net/nullschool/util/

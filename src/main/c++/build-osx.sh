set -e
set -x
g++ -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -shared *.cpp -o ../../../target/drnglib_x64.dylib
cp ../../../target/drnglib_x64.dylib ../resources/net/nullschool/util/

set -e
set -x
#JAVA_HOME=`/usr/libexec/java_home`
g++ -v -fPIC -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -shared *.cpp -o ../../../target/drnglib_x64.so
cp ../../../target/drnglib_x64.so ../resources/net/nullschool/util/

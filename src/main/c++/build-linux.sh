set -e
set -x
FLAGS="-shared -static-libgcc -static-libstdc++ -O3"
g++ -m64 $FLAGS -I$JAVA_HOME/include -I$JAVA_HOME/include/linux *.cpp -o ../../../target/drnglib_x64.so -fPIC
cp ../../../target/drnglib_x64.so ../resources/net/nullschool/util/
g++ -m32 $FLAGS -I$JAVA_HOME/include -I$JAVA_HOME/include/linux *.cpp -o ../../../target/drnglib_x86.so
cp ../../../target/drnglib_x86.so ../resources/net/nullschool/util/

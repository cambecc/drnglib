# TODO: convert to make
set -e
set -x
FLAGS="-shared -static-libgcc -static-libstdc++ -O3"
OUTDIR=../resources/net/nullschool/util/linux
g++ -m64 $FLAGS -I$JAVA_HOME/include -I$JAVA_HOME/include/linux *.cpp -o $OUTDIR/drnglib-x64.so -fPIC
g++ -m32 $FLAGS -I$JAVA_HOME/include -I$JAVA_HOME/include/linux *.cpp -o $OUTDIR/drnglib-x86.so

# TODO: convert to make
set -e
set -x
FLAGS="-Wall -std=c99 -shared -O3"
OUTDIR=../resources/net/nullschool/util/linux
gcc -m64 $FLAGS -I$JAVA_HOME/include -I$JAVA_HOME/include/linux *.c -o $OUTDIR/drnglib-x64.so -fPIC
gcc -m32 $FLAGS -I$JAVA_HOME/include -I$JAVA_HOME/include/linux *.c -o $OUTDIR/drnglib-x86.so

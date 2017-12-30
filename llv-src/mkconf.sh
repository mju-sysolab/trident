#! /bin/sh

help="no"
ARG_CNT=$#

while [ $# != 0 ]
do  case "$1" in
  -h)   help="yes" ;;
  --src=*) SRC_PATH=`echo $1 | cut -f 2- -d =` ;;
  esac
  shift
done

# Check if no arguments
if [ $ARG_CNT -eq 0 -o $help = "yes" ]; then
    base_name=$(basename $0)
    echo "Usage: $base_name [-h] [--src=ABSOLUTE_PATH_TO_SRC]"
    echo ""
    exit 0
fi

SEDSTR='s|@LLVM_SRC_TOP@|'$SRC_PATH'|g'

( cd autoconf ; cat configure.ac.in | sed -e $SEDSTR > configure.ac )
( cd autoconf ; autoconf -o ../configure )

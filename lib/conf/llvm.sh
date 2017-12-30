export LLVM_VER=llvm-1.5
export LLVM_TOP=/home/rasr/apps/llvm/$LLVM_VER
export LLVMGCCDIR=$LLVM_TOP/cfrontend/x86/llvm-gcc

alias llvmgcc=$LLVMGCCDIR/bin/gcc
alias llvmg++=$LLVMGCCDIR/bin/g++

export LLVM_LIB_SEARCH_PATH=$LLVMGCCDIR/bytecode-libs
export PATH=$PATH:$LLVM_TOP/obj/Debug/bin


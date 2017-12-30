setenv LLVM_VER llvm-1.5
setenv LLVM_TOP /home/rasr/apps/llvm/$LLVM_VER
setenv LLVMGCCDIR $LLVM_TOP/cfrontend/x86/llvm-gcc

alias llvmgcc $LLVMGCCDIR/bin/gcc
alias llvmg++ $LLVMGCCDIR/bin/g++

setenv LLVM_LIB_SEARCH_PATH $LLVMGCCDIR/bytecode-libs
set path= ( $path $LLVM_TOP/obj/Debug/bin )


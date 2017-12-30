#LA-CC 05-135 Trident 0.7.1
#
#Copyright Notice
#Copyright 2006 (c) the Regents of the University of California.
#
#This Software was produced under a U.S. Government contract 
#(W-7405-ENG-36) by Los Alamos National Laboratory, which is operated by 
#the University of California for the U.S. Department of Energy. The U.S. 
#Government is licensed to use, reproduce, and distribute this Software. 
#Permission is granted to the public to copy and use this Software without 
#charge, provided that this Notice and any statement of authorship are 
#reproduced on all copies. Neither the Government nor the University makes 
#any warranty, express or implied, or assumes any liability or 
#responsibility for the user of this Software.


##===- projects/sample/tools/sample/Makefile ---------------*- Makefile -*-===##

#
# Indicate where we are relative to the top of the source tree.
#
LEVEL=../..

#
# Give the name of the tool.
#
TOOLNAME=llv

#
# LLVMLIBS -- LLVM libraries that are required
#
#


LLVMLIBS = \
        LLVMSparcV9ModuloSched \
        LLVMCBackend \
        LLVMPowerPC \
        LLVMSparcV9 \
        LLVMX86 \
        LLVMSkeleton \
        LLVMSelectionDAG \
        LLVMSparcV9RegAlloc \
        LLVMSparcV9InstrSched \
        LLVMCodeGen \
        LLVMTarget.a \
        LLVMSparcV9LiveVar \
        LLVMipo.a \
        LLVMipa.a \
        LLVMTransforms.a \
        LLVMScalarOpts.a \
        LLVMAnalysis.a \
        LLVMTransformUtils.a \
        LLVMBCReader \
        LLVMBCWriter \
        LLVMCore \
        LLVMSupport.a \
        LLVMbzip2 \
	LLVMSystem.a


# required ?

TOOLLINKOPTS = $(PLATFORMLIBDL)

#
# 
# USEDLIBS - List all project local libraries here
#
USEDLIBS= vhdl floatpasses


#
# List libraries that we'll need
# We use LIBS because sample is a dynamic library.
#
# -- I am not sure what goes here...
LIBS+=# -lsample

#
# Include Makefile.common so we know what to do.
#
include $(LEVEL)/Makefile.common


/*
 *
 LA-CC 05-135 Trident 0.7.1

Copyright Notice
Copyright 2006 (c) the Regents of the University of California.

This Software was produced under a U.S. Government contract
(W-7405-ENG-36) by Los Alamos National Laboratory, which is operated
by the University of California for the U.S. Department of Energy. The
U.S. Government is licensed to use, reproduce, and distribute this
Software. Permission is granted to the public to copy and use this
Software without charge, provided that this Notice and any statement
of authorship are reproduced on all copies. Neither the Government nor
the University makes any warranty, express or implied, or assumes any
liability or responsibility for the user of this Software.


 */


#ifndef TARGET_VHDL_H
#define TARGET_VHDL_H

namespace llvm {
  /// Command line options shared between TargetMachine implementations - 
  /// these should go in their own header eventually.
  ///

  /*
    decleared elsewhere?

  extern bool PrintMachineCode;
  */
  class TargetMachine;
  class Module;
  class IntrinsicLowering;
  

  // allocateCTargetMachine - Allocate and return a subclass of TargetMachine
  // that implements emits C code.  This takes ownership of the
  // IntrinsicLowering pointer, deleting it when the target machine is
  // destroyed.
  //
  /*
  TargetMachine *allocateCTargetMachine(const Module &M,
                                        IntrinsicLowering *IL = 0);
  */

  // this feels like a hack?
  
  TargetMachine *allocateVTargetMachine(const Module &M,
                                        IntrinsicLowering *IL = 0);
} // End llvm namespace

#endif






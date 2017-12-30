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


//===-- CTargetMachine.h - TargetMachine for the C backend ------*- C++ -*-===//
// 
//                     The LLVM Compiler Infrastructure
//
// This file was developed by the LLVM research group and is distributed under
// the University of Illinois Open Source License. See LICENSE.TXT for details.
// 
//===----------------------------------------------------------------------===//
// 
// This file declares the TargetMachine that is used by the C backend.
//
//===----------------------------------------------------------------------===//

#ifndef VTARGETMACHINE_H
#define VTARGETMACHINE_H

#include "llvm/Target/TargetMachine.h"

namespace llvm {
class IntrinsicLowering;

struct VTargetMachine : public TargetMachine {
  VTargetMachine(const Module &M, IntrinsicLowering *IL) :
    TargetMachine("VBackend", IL, M) {}

  // This is the only thing that actually does anything here.
  virtual bool addPassesToEmitAssembly(PassManager &PM, std::ostream &Out);


  // This class always works, but shouldn't be the default in most cases.
  static unsigned getModuleMatchQuality(const Module &M) { return 1; }
};

} // End llvm namespace


#endif

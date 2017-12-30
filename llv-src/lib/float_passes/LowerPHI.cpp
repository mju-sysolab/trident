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


#include "llvm/Transforms/Scalar.h"
#include "llvm/Function.h"
#include "llvm/Instructions.h"
#include "llvm/Pass.h"
#include "llvm/Value.h"
#include "llvm/Type.h"
#include <iostream>

#include "lowerphi.h"

using namespace llvm;

namespace {
  class LowerPHI : public FunctionPass {
    
  public:
    // anything else ??

    bool runOnFunction(Function &F);
  };

  RegisterOpt<LowerPHI> X("lowerphi", "Lower phi instructions into copies");
}

FunctionPass *llvm::createLowerPHIPass() { return new LowerPHI(); }

bool LowerPHI::runOnFunction(Function &F) {
  bool Changed = false;
  
  for (Function::iterator BB = F.begin(), E = F.end(); BB != E; ++BB)
    for (BasicBlock::iterator I = BB->begin(), E = BB->end(); I != E; ++I) {
      if (Instruction *Inst = dyn_cast<Instruction>(I)) {
	std::cout << " Instruction ";
	Inst->print(std::cout);
	std::cout << "\n";
	std::cout << " Operands " << Inst->getNumOperands() << "\n";
	for(unsigned op = 0; op < Inst->getNumOperands(); op++ ) {
	  Value *operand = Inst->getOperand(op);
	  std::cout << " Op ";
	  operand->print(std::cout);
	}


      }

    }
  return Changed;
}

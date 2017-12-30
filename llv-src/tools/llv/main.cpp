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


//===-- llc.cpp - Implement the LLVM Native Code Generator ----------------===//
// 
//                     The LLVM Compiler Infrastructure
//
// This file was developed by the LLVM research group and is distributed under
// the University of Illinois Open Source License. See LICENSE.TXT for details.
// 
//===----------------------------------------------------------------------===//
//
// This is the llc code generator driver. It provides a convenient
// command-line interface for generating native assembly-language code 
// or C code, given LLVM bytecode.
//
//===----------------------------------------------------------------------===//

//#include "llvm/Target/TargetMachineImpls.h"

#include "llvm/Bytecode/Reader.h"
#include "llvm/Target/TargetMachine.h"
#include "llvm/Target/TargetMachineRegistry.h"
#include "llvm/Transforms/Scalar.h"
#include "llvm/Module.h"

#include "llvm/Analysis/LoadValueNumbering.h"
#include "llvm/Analysis/Verifier.h"

#include "llvm/PassManager.h"
#include "llvm/Pass.h"

#include "llvm/Transforms/IPO.h"
#include "llvm/Assembly/Parser.h"
#include "llvm/Bytecode/WriteBytecodePass.h"
#include "llvm/Target/TargetData.h"

#include "llvm/Support/CommandLine.h"
#include "llvm/Support/PluginLoader.h"
#include "llvm/System/Signals.h"

// local features
#include "vhdl.h"
#include "float_loopunroll.h"
#include "lowerphi.h"
#include "RenameDuplicateVars.h"

#include <fstream>
#include <iostream>
#include <memory>

using namespace llvm;

// General options for llc.  Other pass-specific options are specified
// within the corresponding llc passes, and target-specific options
// and back-end code generation options are specified with the target machine.
// 
namespace {
  cl::opt<std::string>
  InputFilename(cl::Positional, cl::desc("<input bytecode>"), cl::init("-"));

  cl::opt<std::string>
  OutputFilename("o", cl::desc("Output filename"), cl::value_desc("filename"));

  cl::opt<bool> Force("f", cl::desc("Overwrite output files"));

  cl::opt<const TargetMachineRegistry::Entry*, false, TargetNameParser>
  MArch("march", cl::desc("Architecture to generate assembly for:"));

  cl::opt<bool>   
  Verify("verify", cl::desc("Verify each pass result"));

  cl::opt<bool>
  DisableInline("disable-inlining", cl::desc("Do not run the inliner pass"));
  
  cl::opt<bool>
  DisableOptimizations("disable-opt",
                       cl::desc("Do not run any optimization passes"));

  cl::opt<bool>
  StripDebug("strip-debug",
             cl::desc("Strip debugger symbol info from translation unit"));

  cl::opt<bool>
  DisableTailDuplication("disable-tail-dup", 
			 cl::desc("Do not run the tail duplication pass"));
  
  cl::opt<bool> 
  NoCompress("disable-compression", cl::init(false),
             cl::desc("Don't compress the generated bytecode"));
}
   
// GetFileNameRoot - Helper function to get the basename of a filename...
static inline std::string
GetFileNameRoot(const std::string &InputFilename) {
  std::string IFN = InputFilename;
  std::string outputFilename;
  int Len = IFN.length();
  if ((Len > 2) &&
      IFN[Len-3] == '.' && IFN[Len-2] == 'b' && IFN[Len-1] == 'c') {
    outputFilename = std::string(IFN.begin(), IFN.end()-3); // s/.bc/.s/
  } else {
    outputFilename = IFN;
  }
  return outputFilename;
}


static inline void addPass(PassManager &PM, Pass *P) {
  // Add the pass to the pass manager...
  PM.add(P);
  
  // If we are verifying all of the intermediate steps, add the verifier...
  if (Verify) PM.add(createVerifierPass());
}


void AddConfiguredTransformationPasses(PassManager &PM) {
  PM.add(createVerifierPass());                  // Verify that input is correct
  //addPass(PM, createLowerSetJmpPass());          // Lower llvm.setjmp/.longjmp
  //addPass(PM, createLowerSwitchPass()); // we don't support switches ...

  //??
  addPass(PM, createFunctionResolvingPass());    // Resolve (...) functions

  
  // If the -strip-debug command line option was specified, do it.
  if (StripDebug)
    addPass(PM, createStripSymbolsPass(true));

  if (DisableOptimizations) return;

  addPass(PM, createRaiseAllocationsPass());     // call %malloc -> malloc inst
  addPass(PM, createCFGSimplificationPass());    // Clean up disgusting code
  addPass(PM, createPromoteMemoryToRegisterPass());  // Kill useless allocas
  addPass(PM, createGlobalOptimizerPass());      // Optimize out global vars
  addPass(PM, createGlobalDCEPass());            // Remove unused fns and globs
  addPass(PM, createIPConstantPropagationPass());// IP Constant Propagation
  addPass(PM, createDeadArgEliminationPass());   // Dead argument elimination
  addPass(PM, createInstructionCombiningPass()); // Clean up after IPCP & DAE
  addPass(PM, createCFGSimplificationPass());    // Clean up after IPCP & DAE

  addPass(PM, createPruneEHPass());              // Remove dead EH info

  if (!DisableInline)
    addPass(PM, createFunctionInliningPass());   // Inline small functions
  // ?? this ?
  addPass(PM, createSimplifyLibCallsPass());     // Library Call Optimizations
  addPass(PM, createArgumentPromotionPass());    // Scalarize uninlined fn args

  addPass(PM, createRaisePointerReferencesPass());// Recover type information
  // ?? I don't think I want tail duplication ...
  if (!DisableTailDuplication)
    addPass(PM, createTailDuplicationPass());      // Simplify cfg by copying code
  // Can I create a Tail -- unduplicate pass ???
  addPass(PM, createCFGSimplificationPass());    // Merge & remove BBs
  addPass(PM, createScalarReplAggregatesPass()); // Break up aggregate allocas
  addPass(PM, createInstructionCombiningPass()); // Combine silly seq's
  addPass(PM, createCondPropagationPass());      // Propagate conditionals

  addPass(PM, createTailCallEliminationPass());  // Eliminate tail calls
  addPass(PM, createCFGSimplificationPass());    // Merge & remove BBs
  addPass(PM, createLICMPass());                 // Hoist loop invariants
  addPass(PM, createInstructionCombiningPass()); // Clean up after the unroller
  addPass(PM, createIndVarSimplifyPass());       // Canonicalize indvars
  addPass(PM, createFloatLoopUnrollPass());           // Unroll small loops
  addPass(PM, createInstructionCombiningPass()); // Clean up after the unroller
  addPass(PM, createLoadValueNumberingPass());   // GVN for load instructions
  addPass(PM, createGCSEPass());                 // Remove common subexprs
  addPass(PM, createSCCPPass());                 // Constant prop with SCCP

  // Run instcombine after redundancy elimination to exploit opportunities
  // opened up by them.
  addPass(PM, createInstructionCombiningPass());
  addPass(PM, createCondPropagationPass());      // Propagate conditionals

  addPass(PM, createDeadStoreEliminationPass()); // Delete dead stores
  addPass(PM, createAggressiveDCEPass());        // SSA based 'Aggressive DCE'
  addPass(PM, createCFGSimplificationPass());    // Merge & remove BBs
  addPass(PM, createDeadTypeEliminationPass());  // Eliminate dead types
  addPass(PM, createConstantMergePass());        // Merge dup global constants

  //addPass(PM, createLowerPHIPass());        // Merge dup global constants

  addPass(PM, createCollectVariableNamesPass()); // prepare for RenameDuplicateVars
  addPass(PM, createRenameDuplicateVarsPass());  // rename duplicate variables

}





// main - Entry point for the llc compiler.
//
int main(int argc, char **argv) {
  cl::ParseCommandLineOptions(argc, argv, " llvm system compiler\n");
  sys::PrintStackTraceOnErrorSignal();
  
  //std::cout << "hello\n";

  // Load the module to be compiled...
  std::auto_ptr<Module> M(ParseBytecodeFile(InputFilename));
  if (M.get() == 0) {
    std::cerr << argv[0] << ": bytecode didn't read correctly.\n";
    return 1;
  }
  Module &mod = *M.get();

  // Allocate target machine.  First, check whether the user has
  // explicitly specified an architecture to compile for.

  //std::cout << "MArch" << MArch->Name[0] << " mod " << mod  << "\n";

  // % function pointer ??? -- yes, but it is not used any more.
  TargetMachine* (*TargetMachineAllocator)(const Module&,
                                           IntrinsicLowering *) = 0;
  //std::cout << " Machine is " << TargetMachineAllocator << "\n";
  if (MArch == 0) {
    std::string Err;
    MArch = TargetMachineRegistry::getClosestStaticTargetForModule(mod, Err);
    if (MArch == 0) {
      std::cerr << argv[0] << ": error auto-selecting target for module '"
                << Err << "'.  Please use the -march option to explicitly "
                << "pick a target.\n";
      return 1;
    }
  }


  //std::cout << "MArch " << MArch->Name[0] << " allocated machine?\n";

  std::auto_ptr<TargetMachine> target(MArch->CtorFn(mod,0));
  assert(target.get() && "Could not allocate target machine!");
  TargetMachine &Target = *target.get();
  const TargetData &TD = Target.getTargetData();

  //std::cout << "machine allocated!\n";

  // Build up all of the passes that we want to do to the module...
  PassManager Passes;

  Passes.add(new TargetData("llv", TD.isLittleEndian(), TD.getPointerSize(),
                            TD.getPointerAlignment(), TD.getDoubleAlignment()));

  //std::cout << "added pass ?\n";

  // Figure out where we are going to send the output...
  std::ostream *Out = 0;
  if (OutputFilename != "") {
    if (OutputFilename != "-") {
      // Specified an output filename?
      if (!Force && std::ifstream(OutputFilename.c_str())) {
	// If force is not specified, make sure not to overwrite a file!
	std::cerr << argv[0] << ": error opening '" << OutputFilename
		  << "': file exists!\n"
		  << "Use -f command line argument to force output\n";
	return 1;
      }
      Out = new std::ofstream(OutputFilename.c_str());

      // Make sure that the Out file gets unlinked from the disk if we get a
      // SIGINT
      sys::RemoveFileOnSignal(sys::Path(OutputFilename));
    } else {
      Out = &std::cout;
    }
  } else {
    if (InputFilename == "-") {
      OutputFilename = "-";
      Out = &std::cout;
    } else {
      OutputFilename = GetFileNameRoot(InputFilename); 

      if (MArch->Name[0] == 'v') {
	int location = OutputFilename.rfind(".o",OutputFilename.length());
	if (location > 0) OutputFilename.erase(location, 2);
	OutputFilename += ".llv";
      } else if (MArch->Name[0] != 'c' || MArch->Name[1] != 0)  // not CBE
        OutputFilename += ".s";
      else
        OutputFilename += ".cbe.c";
      
      if (!Force && std::ifstream(OutputFilename.c_str())) {
        // If force is not specified, make sure not to overwrite a file!
        std::cerr << argv[0] << ": error opening '" << OutputFilename
                  << "': file exists!\n"
                  << "Use -f command line argument to force output\n";
        return 1;
      }
      
      Out = new std::ofstream(OutputFilename.c_str());
      if (!Out->good()) {
        std::cerr << argv[0] << ": error opening " << OutputFilename << "!\n";
        delete Out;
        return 1;
      }
      
      // Make sure that the Out file gets unlinked from the disk if we get a
      // SIGINT
      sys::RemoveFileOnSignal(sys::Path(OutputFilename));
    }
  }

  // add other passes here ....
  AddConfiguredTransformationPasses(Passes);


  // Ask the target to add backend passes as necessary
  if (Target.addPassesToEmitAssembly(Passes, *Out)) {
    std::cerr << argv[0] << ": target '" << Target.getName()
              << "' does not support static compilation!\n";
    if (Out != &std::cout) delete Out;
    // And the Out file is empty and useless, so remove it now.
    std::remove(OutputFilename.c_str());
    return 1;
  } else {
    // Run our queue of passes all at once now, efficiently.
    //std::cout << "Run our passes\n";
    Passes.run(*M.get());
  }

  // Delete the ostream if it's not a stdout stream
  if (Out != &std::cout) delete Out;

  return 0;
}

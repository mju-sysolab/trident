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



// Trident (C) 2005 The Regents of the University of California.
// Written by Neil Steiner


// The RenameDuplicateVars pass looks for any variable that is assigned to more than once 
// in the compilation unit, and uniquely renames such variables.  Note that there is 
// nothing wrong with using the same name in different scopes, but Trident likes for names  
// to be unique across the compilation unit.
// This "pass" actually consists of two chained passes masquerading as one.  The 
// CollectVariableNames pass builds a map of all the variable names in the compilation unit
// and the RenameDuplicateVars uses that map to identify unique names.


// pass -debug-only=rename-duplicate-vars to llv in order to enable debugging output 
// for this pass; defining this before #including llvm/Support/Debug.h simplifies things
#define DEBUG_TYPE "rename-duplicate-vars"


#include "RenameDuplicateVars.h"
#include "llvm/Support/InstIterator.h"
#include "llvm/ADT/Statistic.h"
#include "llvm/Support/Debug.h"
#include <iostream>
#include <sstream>
#include <map>


// use the llvm and std namespaces
using namespace llvm;
using namespace std;


// define this pass in the null namespace
namespace {

  // this map template will be used across both of our passes
  typedef map<string,int> variable_map_type;

  // register the CollectVariableNames pass as an optimizer pass
  RegisterOpt<class CollectVariableNames> registerCollectVariableNames("collect-variable-names", 
    "Collect all of the variable names in the code.");

  // the CollectVariableNames pass collects all of the variable names in the compilation unit, 
  // in order to make them available to the RenameDuplicateVars pass
  class CollectVariableNames : public FunctionPass {

  public:

    // main functionality for a FunctionPass
    virtual bool runOnFunction(Function& F);

    // accessor function used by RenameDuplicateVars
    virtual variable_map_type& getVariableMap(void) { 
      return variables;
    }

    // inform the optimizer that we preserve the results of all previous passes
    void getAnalysisUsage(AnalysisUsage &AU) const {
      AU.setPreservesAll();
    }

  protected:

    // map of all variable names
    variable_map_type variables;

  };

  // register the RenameDuplicateVars pass as an optimizer pass
  RegisterOpt<class RenameDuplicateVars> registerRenameDuplicateVars("rename-duplicate-vars", 
    "Rename duplicate variable names to ensure unique names across the compilation unit.");

  // the RenameDuplicateVars pass examines each instruction assignment to identify and 
  // resolve any duplicate names; these duplicates only occur in separate basic blocks, so 
  // there is no ambiguity or incorrectness in the llv output, but Trident is unhappy when 
  // it sees the same name more than once, even across blocks;
  // note that this pass relies the CollectVariableNames pass in order to identify all of 
  // the variable names in the compilation unit, and it informs the optimizer that it 
  // requires CollectVariableNames as a transitive pass; that means that CollectVariableNames
  // will be valid and accessible when RenameDuplicateVars runs, so that we can ask it 
  // for its variable map
  class RenameDuplicateVars : public FunctionPass {

  public:

    // main functionality for a FunctionPass
    virtual bool runOnFunction(Function& F);

    // inform the optimizer that we preserve the results of all previous passes, and that 
    // that we require the presence of CollectVariableNames as a transitive pass
    void getAnalysisUsage(AnalysisUsage &AU) const {
      AU.setPreservesAll();
      AU.addRequiredTransitive<CollectVariableNames>();
    }

  };

  // declare a statistic that we can update, that can be reported to the user if llv 
  // is invoked with the -stats flag;
  Statistic<> NumRenamed("rename-duplicate-vars", "Number of duplicate variables renamed");

}


// provide a way for PassManager clients to instantiate these passes, and do so from inside 
// the llvm namespace so that the compiler doesn't give us nasty looks
namespace llvm {

  FunctionPass* createRenameDuplicateVarsPass() { return new ::RenameDuplicateVars(); }
  FunctionPass* createCollectVariableNamesPass() { return new ::CollectVariableNames(); }

}


// gather the variables names used in each function that we visit
bool CollectVariableNames::runOnFunction(Function& F) {

  // make our presence known to the world
  DEBUG(cerr << "CollectVariableNames::runOnFunction() has been invoked." << endl);

  // iterate through each basic block in the function
  for(Function::iterator block = F.begin(), func_end = F.end(); block != func_end;block++) {

    // iterate through each instruction in the basic block
    for(BasicBlock::iterator inst = block->begin(), block_end = block->end(); inst != block_end;inst++) {

      // skip instructions that have no name (i.e. instructions that do not assign 
      // their results to a variable)
      if(!inst->hasName()) continue;

      // get the variable name
      const string& name = inst->getName();
      // and put it in the map
      variables[name] = 0;

    }

  }

  // tell the PassManager that we haven't changed anything
  return false;
}


// look for any variables that are defined more than once across the compilation unit, 
// and rename them uniquely
bool RenameDuplicateVars::runOnFunction(Function& F) {

  // make our presence known to the world
  DEBUG(cerr << "RenameDuplicateVars::runOnFunction() has been invoked." << endl);

  // look up the CollectVariableNames pass, that we requires as a transitive pass
  CollectVariableNames& cvn = getAnalysis<CollectVariableNames>();
  // look up the variable name map
  variable_map_type& variables = cvn.getVariableMap();

  // define a string stream that we'll use to build and test replacement names
  stringstream new_name_stream;

  // iterate through each basic block in the function
  for(Function::iterator block = F.begin(), func_end = F.end(); block != func_end;block++) {

    // iterate through each instruction in the basic block
    for(BasicBlock::iterator inst = block->begin(), block_end = block->end(); inst != block_end;inst++) {

      // skip instructions that have no name (i.e. instructions that do not assign 
      // their results to a variable)
      if(!inst->hasName()) continue;

      // get the variable name
      const string& name = inst->getName();
      // look for the variable name in the map
      variable_map_type::iterator variable = variables.find(name);

      // if the name has already been used, we'll have to uniquely rename it
      if(variable != variables.end() && variables[name] > 0) {
        DEBUG(cerr << "variable " << name << " has already been used" << endl);

        // loop until we find a unique replacement name
        for(int i = 0;i < INT_MAX;i++) {

          // begin with an empty name
          new_name_stream.str("");
          // assemble the name along with a period and the next integer
          new_name_stream << name << '.' << i;
          // convert the stream to a string
          const string& new_var_name = new_name_stream.str();
          // determine whether the new name really is unique
          variable_map_type::iterator new_variable = variables.find(new_var_name);
          // if the name is unique, perform the replacement
          if(new_variable == variables.end()) {

            // inform the user of the replacement
            DEBUG(cerr << "  " << name << " will be renamed to " << new_var_name << endl);
            // increment our statistic
            ++NumRenamed;
            // change the instruction's name (i.e. change the variable name that is 
            // assigns its results to)
            inst->setName(new_var_name);
            // be sure to add this name to the map of variable names
            variables[new_var_name] = 1;
            // and drop out of the loop since the replacement was successful
            break;

          }

        }

      // if the name has not already been used, flag it now to ensure that subsequent 
      // uses are addressed
      } else {
        // tell the map that we've seen this name one time
        variables[name] = 1;
      }

    }

  }

  // tell the PassManager that we haven't changed anything, even though we just finished 
  // replacing variable names; the fact is that the name changes have no effect whatsoever 
  // on the syntax or semantics of the code or the validity of previous optimizations
  return false;
}



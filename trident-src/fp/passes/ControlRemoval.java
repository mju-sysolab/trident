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


package fp.passes;

import java.util.*;

import fp.flowgraph.*;
import fp.util.BooleanEquation;
import fp.util.UseHash;




public class ControlRemoval extends Pass implements BlockPass {
  
  /*
    This is a very simple pass.  It removes branches, gotos, and return.
    Dropping return may have some implications, where gotos and branches
    are safe.

    This must run after addPredicates ...

  */


  public ControlRemoval(PassManager pm) {
    super(pm);
  }

  public String name() { return "ControlRemoval"; }

  public boolean optimize(BlockNode node) {
    ArrayList list = node.getInstructions();
    HashSet dead = new HashSet();
    for(Iterator iter = list.iterator(); iter.hasNext(); ) {
      Instruction instruction = (Instruction) iter.next(); 
      if (Branch.conforms(instruction) 
	  || Goto.conforms(instruction)
	  || Return.conforms(instruction))
	dead.add(instruction);
      if (Switch.conforms(instruction)) 
	System.err.println("Illegal switch in node "+node.getName());
    }
    for(Iterator iter = dead.iterator(); iter.hasNext(); ) {
      Instruction instruction = (Instruction)iter.next();
      node.removeInstruction(instruction);
    }
    return true;
  }
  
}


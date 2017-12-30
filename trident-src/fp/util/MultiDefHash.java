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


package fp.util;

import java.util.*;

import fp.flowgraph.Operand;
import fp.flowgraph.Instruction;


public class MultiDefHash extends HashMap {
  // this can just be a hashMap


  /*

  add(Instruction ...)
    if Instruction.result != null
    put(result, instruction)

  */


  //I need some more power...
  public void addinstructions(Collection instructions) {
    for (Iterator instIt = instructions.iterator();
    	 instIt.hasNext();){
      Instruction inst = (Instruction) instIt.next();
      add(inst);
    }
  }

  // where is the get ???

  public void add(Instruction inst) {
    int def_count = inst.getNumberOfDefs(); 
    for(int i = 0; i < def_count; i++) {
      Operand o = inst.getOperand(i);
      if (o == null) continue;
      // definitions are unique.
      ArrayList list = (ArrayList)get(o);
      if (list == null) {
	list = new ArrayList();
	put(o, list); 
      }
      if (!list.contains(inst)) {
	list.add(inst);
      }
    } // end operands
  }

  public void remove(Instruction inst) {
    int def_count = inst.getNumberOfDefs(); 
    for(int i = 0; i < def_count; i++) {
      Operand o = inst.getOperand(i);
      if (o == null) continue;
      // definitions are unique.
      ArrayList list = (ArrayList)get(o);
      if (list != null) {
	list.remove(inst);
	if (list.size() == 0)
	  super.remove(o);
      }
    } // end operands
  }


}

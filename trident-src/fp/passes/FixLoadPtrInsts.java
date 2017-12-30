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

/**
 * This class changes all loads that use a pointer to atomic types.  The type 
 * is changed from the pointer to atomic to the atomic type.
 */
public class FixLoadPtrInsts extends Pass implements BlockPass {
  public FixLoadPtrInsts(PassManager pm) {
    super(pm);
  }

  public String name() { return "FixLoadPtrInsts"; }


  /**
   * This method fixes all load pointer insts that point to non-arrays and 
   * non-pointers.  The type is changed to the type being pointed to.
   */
  public boolean optimize(BlockNode bn) {
    // For each instruction in this block...
    for(Iterator iIt = bn.getInstructions().iterator(); iIt.hasNext(); ) {
      Instruction inst = (Instruction) iIt.next();
      // If this instruction is a pointer load
      if(inst.isLoad() && inst.type().isPointer()) {
	Type type = ((PointerType)inst.type()).getType();
	// If the pointed-to type isn't an array or pointer, then change 
	// the type to the pointed-to type.
	if((!type.isArray()) && (!type.isPointer())) {
	  //System.out.println("changing instruction:");
	  //System.out.println(inst);
	  inst.setType(type);
	  //System.out.println(inst);
	}
      }
    }
 
    return true;
  }

}

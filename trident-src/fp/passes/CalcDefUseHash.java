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

import fp.util.BooleanEquation;
import fp.flowgraph.*;

// java imports
import java.util.*;

/**
 * A pass for calculating the defhash and the usehash.  This pass
 * should not be necessary if all other passes are careful to use the
 * proper approach to add and delete instructions and when updating an
 * instruction.
 *
 * @author Justin Tripp
 */


public class CalcDefUseHash extends Pass implements BlockPass {

  public CalcDefUseHash(PassManager pm) {
    super(pm);
  }

  public String name() { return "CalcDefUseHash"; }

  public boolean optimize(BlockNode node) {
    
    // Der Plan!
    // clone ArrayList.
    // delete all instructions
    // add the back again.

    ArrayList orig_instructions = node.getInstructions();
    ArrayList instructions = (ArrayList)orig_instructions.clone();
    // wipe instructions 
    orig_instructions.clear();
    // wipe hashes
    node.getDefHash().clear();
    node.getUseHash().clear();

    for(Iterator iter = instructions.iterator(); iter.hasNext(); ) 
      node.addInstruction((Instruction)iter.next());
    
    return true;
  }
  
}

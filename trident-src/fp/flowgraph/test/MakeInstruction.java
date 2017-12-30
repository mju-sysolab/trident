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


package fp.flowgraph.test;

import fp.flowgraph.*;

public class MakeInstruction implements Operators {

  public static void main(String args[]) {
    
    // that is an ugly constructor.
    Instruction inst = new Instruction(ADD_opcode, Type.Float);
    int def_count = inst.getNumberOfDefs();
    for(int i = 0; i < def_count; i++) {
      // probably not the best way, but I did avoid hard coding.
      // this is not a good way to create Operands.
      inst.putOperand(i, new BlockOperand("a", i));
    }
    for(int i = def_count; i< inst.getNumberOfOperands(); i++) {
      inst.putOperand(i, new BlockOperand("c", i));
    }
    
    inst.setType(Type.Int);

    System.out.println("Basic instruction ");
    System.out.println(inst);

  }
}
    

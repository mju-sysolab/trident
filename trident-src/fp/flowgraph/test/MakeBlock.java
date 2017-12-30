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

public class MakeBlock implements Operators {

  BlockNode block;

  MakeBlock() {
    this(null);
  }

  MakeBlock(BlockNode external) {
    if (external == null)
      block = new BlockNode();
    else
      block = external;

    // should there be a fancy label with its own unique name space ...
    block.setName("then",0);
    
    // who is keeping the operand name space unique and how do I get
    // the next free operand ???

    Instruction inst = null;

    Operand tmp0 = Operand.nextBlock("tmp");
    PrimalOperand a = Operand.newPrimal("a");
    inst = Load.create(LOAD, Type.Int, tmp0, a);
    block.addInstruction(inst);

    Operand tmp1 = Operand.nextBlock("tmp");
    PrimalOperand b = Operand.newPrimal("b");
    inst = Load.create(LOAD, Type.Int, tmp1, b);
    block.addInstruction(inst);

    Operand tmp2 = Operand.nextBlock("tmp");
    inst = Binary.create(ADD, Type.Int, tmp2, tmp0, tmp1);
    block.addInstruction(inst);

    BooleanOperand tmp3 = Operand.nextBoolean("tmp");
    inst = Test.create(SETEQ, Type.Int, tmp3, tmp2, 
		       Operand.newIntConstant(0));
    block.addInstruction(inst);

    inst = Branch.create(BR, tmp3, 
			 Operand.newLabel("then",0),
			 Operand.newLabel("exit"));
    block.addInstruction(inst);

  }

  public static void main(String args[]) {
    MakeBlock mb = new MakeBlock();
    System.out.println(mb.block);

  }

}

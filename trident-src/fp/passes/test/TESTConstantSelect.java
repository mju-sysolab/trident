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


package fp.passes.test;

import fp.flowgraph.*;
import fp.passes.*;
import fp.util.BooleanEquation;
import java.util.*;
import java.math.BigInteger;

public class TESTConstantSelect {

  // 1. Create math instructions, print inputs
  // 2. Run ConstantMath.optimize()
  // 3. Print generated instructions and pass/fail string
  // Mainly, you just run this test and inspect the output, it
  // doesn't check its output in an automated way.
  

  private static int retval = 0;
  private static PassManager pm = new PassManager();
  private static ConstantSelect constantSelect = new ConstantSelect(pm);

  private static void run(ArrayList iList, boolean result) {

    BlockNode bn = new BlockNode();
   
    System.out.println("Test ConstantSelect:");
    System.out.println("INPUT:");
    
    for (Iterator iter = iList.iterator(); iter.hasNext();) {
      Instruction i = (Instruction) iter.next();
      bn.addInstruction(i);
      System.out.println("    " + i.toString());
    }

    // do the math
    constantSelect.optimize(bn);

    // print the generated instructions
    System.out.println("OUTPUT:");
    Iterator it = bn.getInstructions().iterator();
    while (it.hasNext()) {
      Instruction inst = (Instruction)it.next();
      System.out.println("    " + inst.toString());
    }
    System.out.println();

  }

  public static void main(String args[]) {
    BlockOperand res_blockOp = Operand.newBlock("res_blockOp");
    BlockOperand res_blockOp2 = Operand.newBlock("res_blockOp2");
    BooleanOperand res_booleanOp = Operand.newBoolean("res_booleanOp");
    BooleanEquation bool = new BooleanEquation();
    BlockOperand dummy_res = Operand.newBlock("dummyResult");
    BlockOperand dummy_op = Operand.newBlock("dummyOperand");
    BlockOperand dummy_op2 = Operand.newBlock("dummyOperand2");
    BlockOperand dummy_op3 = Operand.newBlock("dummyOperand3");
    BlockOperand dummy_op4 = Operand.newBlock("dummyOperand4");
    BooleanOperand dummyBoolean_op = Operand.newBoolean("dummyBooleanOperand");
    BooleanOperand dummyBoolean_res = Operand.newBoolean("dummyBooleanResult");
    bool.setTrue();
    Instruction i1, i2;
    ArrayList iList = new ArrayList();

    iList.clear();
    iList.add(Select.create(Operators.SELECT, Type.Int, res_blockOp, BooleanOperand.TRUE, dummy_op, dummy_op2, bool));
    iList.add(Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op3, bool));
    run(iList, true);

    iList.clear();
    iList.add(Select.create(Operators.SELECT, Type.Int, res_blockOp, BooleanOperand.TRUE, dummy_op, dummy_op2, bool));
    iList.add(Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op3, bool));
    iList.add(Select.create(Operators.SELECT, Type.Int, res_blockOp2, BooleanOperand.TRUE, res_blockOp, dummy_op3, bool));
    iList.add(Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp2, dummy_op4, bool));
    run(iList, true);

    if (retval > 0) {
      System.exit(retval);
    }
  }
}

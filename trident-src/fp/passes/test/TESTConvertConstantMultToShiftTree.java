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

public class TESTConvertConstantMultToShiftTree {

  // 1. Create mult instructions, print inputs
  // 2. Run ConvertConstantMultToShiftTree.calculateConstMult()
  // 3. Print generated instructions and pass/fail string
  // Mainly, you just run this test and inspect the output, it
  // doesn't check its output in an automated way.
  

  private static int retval = 0;
  private static PassManager pm = new PassManager();
  private static ConvertConstantMultToShiftTree convertTree = new ConvertConstantMultToShiftTree(pm);

  private static void run(Instruction i1, Instruction i2, boolean result) {

    BlockNode bn = new BlockNode();
    bn.addInstruction(i1);
    bn.addInstruction(i2);

    // print the original parameters to the multiplication
    System.out.println("Test ConvertConstantMultToShiftTree on CONSTANT=" + Integer.toBinaryString(((IntConstantOperand)Binary.getVal2(i1)).getValue()) + " (" + ((IntConstantOperand)Binary.getVal2(i1)).getValue() + ") N=" + Binary.getVal1(i1).toString() + " resOp=" + Binary.getResult(i1).toString());

    System.out.println("INPUT:");
    System.out.println("    " + i1.toString());
    System.out.println("    " + i2.toString());

    // do the convert
    convertTree.optimize(bn);

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
    BlockOperand operand_blockOp = Operand.newBlock("operand_blockOp");
    BooleanEquation bool = new BooleanEquation();
    BlockOperand dummy_res = Operand.newBlock("dummyResult");
    BlockOperand dummy_op = Operand.newBlock("dummyOperand");
    bool.setTrue();
    Instruction i1, i2;

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("11",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("10001",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("10",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("100",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("1111",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("-1111",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("11011101",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("1110111",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("1011101",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("1",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("0",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.MUL, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("-1",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    i1 = Binary.create(Operators.DIV, Type.Int, res_blockOp, operand_blockOp, Operand.newIntConstant(Integer.valueOf("100",2).intValue()), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    if (retval > 0) {
      System.exit(retval);
    }
  }
}


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

public class TESTConstantMath {

  // 1. Create math instructions, print inputs
  // 2. Run ConstantMath.optimize()
  // 3. Print generated instructions and pass/fail string
  // Mainly, you just run this test and inspect the output, it
  // doesn't check its output in an automated way.
  

  private static int retval = 0;
  private static PassManager pm = new PassManager();
  private static ConstantMath constantMath = new ConstantMath(pm);

  private static void run(Instruction i1, Instruction i2, boolean result) {

    BlockNode bn = new BlockNode();
    
    bn.addInstruction(i1);
    bn.addInstruction(i2);

    // print the original parameters to the multiplication
    System.out.println("Test ConstantMath:");

    System.out.println("INPUT:");
    System.out.println("    " + i1.toString());
    System.out.println("    " + i2.toString());

    

    //BigInteger bi = new BigInteger("1011", 2);
    //System.out.println("1011 BigInteger:  " + bi.toString());
    //bi = new BigInteger("-1011", 2);
    //System.out.println("-1011 BigInteger:  " + bi.toString());
    //System.out.println("-1011 BigInteger:  " + bi.toString());
    //BigInteger bi = new BigInteger(Integer.toBinaryString(-3), 2);
   // BigInteger bi = new BigInteger("-" + "11", 2);
    //System.out.println("-3 " +  " BigInteger:  " + bi.toString(2));
    //bi = bi.abs();
    //System.out.println("absolute value " + " BigInteger:  " + bi.toString(2));

    // do the math
    constantMath.optimize(bn);

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
    BooleanOperand res_booleanOp = Operand.newBoolean("res_booleanOp");
    BooleanEquation bool = new BooleanEquation();
    BlockOperand dummy_res = Operand.newBlock("dummyResult");
    BlockOperand dummy_op = Operand.newBlock("dummyOperand");
    BooleanOperand dummyBoolean_op = Operand.newBoolean("dummyBooleanOperand");
    BooleanOperand dummyBoolean_res = Operand.newBoolean("dummyBooleanResult");
    bool.setTrue();
    Instruction i1, i2;

    // INTEGRAL TYPE

    // ADD
    i1 = Binary.create(Operators.ADD, Type.Int, res_blockOp, Operand.newIntConstant(3), Operand.newIntConstant(5), bool);
    i2 = Binary.create(Operators.ADD, Type.Int, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // ADD
    i1 = Binary.create(Operators.ADD, Type.Ubyte, res_blockOp, Operand.newIntConstant(129), Operand.newIntConstant(1), bool);
    i2 = Binary.create(Operators.ADD, Type.Ubyte, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // ABS
    i1 = Unary.create(Operators.ABS, Type.Sbyte, res_blockOp, Operand.newIntConstant(-35), bool);
    i2 = Binary.create(Operators.ADD, Type.Sbyte, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // SQRT
    i1 = Unary.create(Operators.SQRT, Type.Ubyte, res_blockOp, Operand.newIntConstant(49), bool);
    i2 = Binary.create(Operators.ADD, Type.Ubyte, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // SQRT
    i1 = Unary.create(Operators.SQRT, Type.Ubyte, res_blockOp, Operand.newIntConstant(48), bool);
    i2 = Binary.create(Operators.ADD, Type.Ubyte, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // INV
    i1 = Unary.create(Operators.INV, Type.Ubyte, res_blockOp, Operand.newIntConstant(49), bool);
    i2 = Binary.create(Operators.ADD, Type.Ubyte, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // NOT
    i1 = Unary.create(Operators.NOT, Type.Ubyte, res_blockOp, Operand.newIntConstant(4), bool);
    i2 = Binary.create(Operators.OR, Type.Ubyte, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // AND
    i1 = Binary.create(Operators.AND, Type.Bool, res_booleanOp, BooleanOperand.TRUE, BooleanOperand.FALSE, bool);
    i2 = Binary.create(Operators.OR, Type.Bool, dummyBoolean_res, res_booleanOp, dummyBoolean_op, bool);
    run(i1, i2, true);

    // OR 
    i1 = Binary.create(Operators.OR, Type.Bool, res_booleanOp, BooleanOperand.TRUE, BooleanOperand.FALSE, bool);
    i2 = Binary.create(Operators.OR, Type.Bool, dummyBoolean_res, res_booleanOp, dummyBoolean_op, bool);
    run(i1, i2, true);

    // XOR 
    i1 = Binary.create(Operators.XOR, Type.Bool, res_booleanOp, BooleanOperand.TRUE, BooleanOperand.FALSE, bool);
    i2 = Binary.create(Operators.OR, Type.Bool, dummyBoolean_res, res_booleanOp, dummyBoolean_op, bool);
    run(i1, i2, true);


    // FLOATING POINT TYPE 

    // ADD
    i1 = Binary.create(Operators.ADD, Type.Float, res_blockOp, Operand.newFloatConstant(3.3f), Operand.newFloatConstant(7.04f), bool);
    i2 = Binary.create(Operators.ADD, Type.Float, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // ABS
    i1 = Unary.create(Operators.ABS, Type.Double, res_blockOp, Operand.newDoubleConstant(-3.0007), bool);
    i2 = Binary.create(Operators.ADD, Type.Double, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // SQRT
    i1 = Unary.create(Operators.SQRT, Type.Float, res_blockOp, Operand.newFloatConstant(49.034f), bool);
    i2 = Binary.create(Operators.ADD, Type.Float, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    // INV
    i1 = Unary.create(Operators.INV, Type.Double, res_blockOp, Operand.newDoubleConstant(839.2347234), bool);
    i2 = Binary.create(Operators.ADD, Type.Double, dummy_res, res_blockOp, dummy_op, bool);
    run(i1, i2, true);

    if (retval > 0) {
      System.exit(retval);
    }
  }
}

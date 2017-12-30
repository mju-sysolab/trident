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


public class TESTVerifyOperands {

  // 1. Create instructions to store in a block node (bn)
  // 2. Run VerifyOperands.optimize(bn)
  // 3. Print block and pass/fail string
  

  private static int retval = 0;
  private static PassManager pm = new PassManager();
  private static VerifyOperands verifyOperands = new VerifyOperands(pm);
  private static BlockNode bn = new BlockNode();

  private static void run(Instruction i, boolean result) {
    bn.addInstruction(i);
    System.out.print("VerifyOperands on Block: [" + bn.toString() + "] ");
    try {
      verifyOperands.optimize(bn);
      if (result == false) { 
	System.out.println(" => FAIL");
	retval++;
      } else {
	System.out.println(" => PASS");
      } 
    } catch (IllegalOperand e) {
      if (result == false) {
	System.out.println(" => PASS");
      } else {
	System.out.print(e.getMessage());
	System.out.println(" => FAIL");
	retval++;
      }
    }
    bn.removeInstruction(i);
  }

  public static void main(String args[]) {
    PrimalOperand primalOp1 = Operand.newPrimal("primalOp1");
    PrimalOperand primalOp2 = Operand.newPrimal("primalOp2");
    BooleanOperand booleanOp1 = Operand.newBoolean("booleanOp1");
    BooleanOperand booleanOp2 = Operand.newBoolean("booleanOp2");
    BooleanOperand booleanOp3 = Operand.newBoolean("booleanOp3");
    LabelOperand labelOp1 = Operand.newLabel("labelOp1");
    LabelOperand labelOp2 = Operand.newLabel("labelOp2");
    LabelOperand labelOp3 = Operand.newLabel("labelOp3");
    LabelOperand labelOp4 = Operand.newLabel("labelOp4");
    BlockOperand blockOp1 = Operand.newBlock("blockOp1");
    BlockOperand blockOp2 = Operand.newBlock("blockOp2");
    BlockOperand blockOp3 = Operand.newBlock("blockOp3");
    IntConstantOperand intOp1 = Operand.newIntConstant(3);
    IntConstantOperand intOp2 = Operand.newIntConstant(4);
    IntConstantOperand intOp3 = Operand.newIntConstant(3);
    FloatConstantOperand floatOp1 = Operand.newFloatConstant(Float.parseFloat("3.07"));
    FloatConstantOperand floatOp2 = Operand.newFloatConstant(Float.parseFloat("4.07"));
    FloatConstantOperand floatOp3 = Operand.newFloatConstant(Float.parseFloat("5.07"));
    Instruction i;

    //load
    run(Load.create(Operators.LOAD, Type.Int, blockOp1, primalOp1), true);
    run(Load.create(Operators.LOAD, Type.Int, booleanOp1, primalOp1), true);
    run(Load.create(Operators.LOAD, Type.Int, booleanOp1, primalOp1), true);
    run(Load.create(Operators.LOAD, Type.Int, primalOp1, primalOp2), false);
    run(Load.create(Operators.LOAD, Type.Int, null, primalOp2), false);
    run(Load.create(Operators.LOAD, Type.Int, blockOp1, null), false);
    i = Load.create(Operators.LOAD, Type.Int, blockOp1, primalOp1);
    i.putOperand(1, blockOp1); 
    run(i, false);

    //store
    run(Store.create(Operators.STORE, Type.Int, primalOp1, blockOp1), true);
    run(Store.create(Operators.STORE, Type.Int, primalOp1, intOp1), true);
    run(Store.create(Operators.STORE, Type.Int, primalOp1, floatOp1), true);
    run(Store.create(Operators.STORE, Type.Int, primalOp1, booleanOp1), true);
    run(Store.create(Operators.STORE, Type.Int, null, blockOp1), false);
    run(Store.create(Operators.STORE, Type.Int, primalOp1, null), false);
    run(Store.create(Operators.STORE, Type.Int, primalOp1, labelOp1), false);
    i = Store.create(Operators.STORE, Type.Int, primalOp1, blockOp1);
    i.putOperand(0, blockOp1); 
    run(i, false);

    //branch
    run(Branch.create(Operators.BR, booleanOp1, labelOp1, labelOp2), true);
    run(Branch.create(Operators.BR, null, labelOp1, labelOp2), false);
    run(Branch.create(Operators.BR, booleanOp1, null, labelOp2), false);
    run(Branch.create(Operators.BR, booleanOp1, labelOp1, null), false);
    i = Branch.create(Operators.BR, booleanOp1, labelOp1, labelOp2);
    i.putOperand(0, labelOp1); 
    run(i, false);
    i = Branch.create(Operators.BR, booleanOp1, labelOp1, labelOp2);
    i.putOperand(1, blockOp1); 
    run(i, false);
    i = Branch.create(Operators.BR, booleanOp1, labelOp1, labelOp2);
    i.putOperand(2, blockOp1); 
    run(i, false);

    //goto
    run(Goto.create(Operators.GOTO, labelOp1), true);
    i = Goto.create(Operators.GOTO, labelOp1);
    i.putOperand(0, blockOp1);
    run(i, false);

    //return
    run(Return.create(Operators.RET, Type.Int, blockOp1), true);
    run(Return.create(Operators.RET, Type.Int, intOp1), true);
    run(Return.create(Operators.RET, Type.Int, floatOp1), true);
    run(Return.create(Operators.RET, Type.Int, booleanOp1), true);
    run(Return.create(Operators.RET, Type.Int, primalOp1), false);
    run(Return.create(Operators.RET, Type.Int, labelOp1), false);

    //binary, arith
    run(Binary.create(Operators.ADD, Type.Int, blockOp1, intOp2, intOp3), true);
    run(Binary.create(Operators.ADD, Type.Int, blockOp1, blockOp2, blockOp3), true);
    run(Binary.create(Operators.ADD, Type.Int, blockOp1, floatOp2, floatOp3), true);
    run(Binary.create(Operators.ADD, Type.Int, booleanOp1, intOp2, intOp3), false);
    run(Binary.create(Operators.ADD, Type.Int, blockOp1, labelOp1, intOp3), false);
    run(Binary.create(Operators.ADD, Type.Int, blockOp1, intOp2, primalOp1), false);
    run(Binary.create(Operators.ADD, Type.Int, null, intOp2, intOp3), false);
    run(Binary.create(Operators.ADD, Type.Int, blockOp1, null, intOp3), false);
    run(Binary.create(Operators.ADD, Type.Int, blockOp1, intOp2, null), false);

    //binary, bitwise
    run(Binary.create(Operators.AND, Type.Int, blockOp1, intOp2, intOp3), true);
    run(Binary.create(Operators.AND, Type.Int, booleanOp1, booleanOp2, booleanOp3), true);
    run(Binary.create(Operators.AND, Type.Int, blockOp1, blockOp2, blockOp3), true);
    run(Binary.create(Operators.AND, Type.Int, blockOp1, floatOp2, intOp3), false);
    run(Binary.create(Operators.AND, Type.Int, blockOp1, intOp2, floatOp3), false);

    //binary, shift
    run(Binary.create(Operators.SHL, Type.Int, blockOp1, intOp2, intOp3), true);
    run(Binary.create(Operators.SHL, Type.Int, blockOp1, blockOp2, blockOp3), true);
    run(Binary.create(Operators.SHL, Type.Int, booleanOp1, intOp2, intOp3), false);
    run(Binary.create(Operators.SHL, Type.Int, blockOp1, floatOp2, intOp3), false);
    run(Binary.create(Operators.SHL, Type.Int, blockOp1, intOp2, floatOp3), false);

    //test
    run(Test.create(Operators.SETEQ, Type.Int, booleanOp1, intOp2, intOp3), true);
    run(Test.create(Operators.SETEQ, Type.Int, booleanOp1, booleanOp2, booleanOp3), true);
    run(Test.create(Operators.SETEQ, Type.Int, booleanOp1, floatOp2, floatOp3), true);
    // this one should complain about the result operand not being boolean, but 
    // since it inherits from Binary, it uses that create().
    run(Test.create(Operators.SETEQ, Type.Int, blockOp1, intOp2, intOp3), false);
    run(Test.create(Operators.SETEQ, Type.Int, null, intOp2, intOp3), false);
    run(Test.create(Operators.SETEQ, Type.Int, booleanOp1, null, intOp3), false);
    run(Test.create(Operators.SETEQ, Type.Int, booleanOp1, intOp2, null), false);
    
    //phi
    //cast
    //select
    //switch

    if (retval > 0) {
      System.exit(retval);
    }
  }
}


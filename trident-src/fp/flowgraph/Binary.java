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


package fp.flowgraph;

import fp.util.BooleanEquation;

public class Binary extends InstructionFormat {
  
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

  public static boolean conforms(Operator op) {
    return op.format == Binary_format;
  }
  
  public static Operand getResult(Instruction i) {
    return i.getOperand(0);
  }

  public static void setResult(Instruction i, Operand result) {
    i.putOperand(0, result);
  }

  public static int indexOfResult(Instruction i) { return 0; }

  public static boolean hasResult(Instruction i) {
    return i.getOperand(0) != null;
  }


  public static Operand getVal1(Instruction i) {
    return i.getOperand(1);
  }

  public static void setVal1(Instruction i, Operand result) {
    i.putOperand(1, result);
  }

  public static boolean hasVal1(Instruction i) {
    return i.getOperand(1) != null;
  }

  
  public static Operand getVal2(Instruction i) {
    return i.getOperand(2);
  }

  public static void setVal2(Instruction i, Operand result) {
    i.putOperand(2, result);
  }

  public static boolean hasVal2(Instruction i) {
    return i.getOperand(2) != null;
  }

  public static Instruction create(Operator o, Type type, Operand result,
				   Operand val1, Operand val2) {
    return Binary.create(o, type, result, val1, val2, null);
  }

  public static Instruction create(Operator o, Type type, Operand result,
				   Operand val1, Operand val2, BooleanEquation eq) {
    Instruction i = new Instruction(o, type, eq);
    i.putOperand(0, result);
    i.putOperand(1, val1);
    i.putOperand(2, val2);
    return i;
  }
  
  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasResult(i)) {
      throw new IllegalOperand("Binary instruction does not have result operand.");
    }
    if (!hasVal1(i)) {
      throw new IllegalOperand("Binary instruction does not have first value operand.");
    }
    if (!hasVal2(i)) {
      throw new IllegalOperand("Binary instruction does not have second value operand.");
    }
  }

  public static void verifyArith(Instruction i) throws IllegalOperand {
    verify(i);
    if (!(getVal1(i).isIntegerOperand() 
      || getVal1(i).isFloatingPointOperand())) {
      throw new IllegalOperand("Binary instruction first value must be integer or float constant operand or block operand.");
    }
    if (!(getVal2(i).isIntegerOperand() 
      || getVal1(i).isFloatingPointOperand())) {
      throw new IllegalOperand("Binary instruction second value must be integer or float constant operand or block operand.");
    }
    if ((!getResult(i).isBlock()) && (!getResult(i).isPrimal())) {
      throw new IllegalOperand("Binary arith instruction result must be block or primal operand.");
    }
  }
  
  public static void verifyBitwise(Instruction i) throws IllegalOperand {
    verify(i);
    if (!getVal1(i).isIntegralOperand()) {
      throw new IllegalOperand("Bitwise binary instruction first value must be integral or block operand.");
    }
    if (!getVal2(i).isIntegralOperand()) {
      throw new IllegalOperand("Bitwise binary instruction second value must be integral or block operand.");
    }
    if ((!getResult(i).isBlock()) && (!getResult(i).isPrimal()) 
         && (!getResult(i).isBoolean())) {
      throw new IllegalOperand("Binary bitwise instruction result must be block, boolean or primal operand.");
    }
  }

  public static void verifyShift(Instruction i) throws IllegalOperand {
    verify(i);
    if (!getVal1(i).isIntegerOperand()) {
      throw new IllegalOperand("Shift binary instruction first value must be integer or block operand.");
    }
    if (!getVal2(i).isIntegerOperand()) {
      throw new IllegalOperand("Shift binary instruction second value must be integer or block operand.");
    }
    if ((!getResult(i).isBlock()) && (!getResult(i).isPrimal())) {
      throw new IllegalOperand("Binary shift instruction result must be block or primal operand.");
    }
  }

}

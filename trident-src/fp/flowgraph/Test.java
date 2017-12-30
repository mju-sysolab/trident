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

public class Test extends Binary {

  // this should not need to be here.
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

  public static boolean conforms(Operator op) {
    return op.format == Test_format;
  }
  
  public static Operand getResult(Instruction i) {
    return i.getOperand(0);
  }

  public static void setResult(Instruction i, BooleanOperand result) {
    i.putOperand(0, result);
  }

  public static int indexOfResult(Instruction i) { return 0; }

  public static boolean hasResult(Instruction i) {
    return i.getOperand(0) != null;
  }

  public static Instruction create(Operator o, Type type, 
				   BooleanOperand result,
				   Operand val1, Operand val2) {
    return Test.create(o, type, result, val1, val2, null);
  }

  public static Instruction create(Operator o, Type type, 
				   BooleanOperand result,
				   Operand val1, Operand val2,
				   BooleanEquation eq) {
    Instruction i = new Instruction(o, type, eq);
    i.putOperand(0, result);
    i.putOperand(1, val1);
    i.putOperand(2, val2);
    return i;
  }
 
  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasResult(i)) {
      throw new IllegalOperand("Setcc instruction does not have result operand.");
    }
    if (!hasVal1(i)) {
      throw new IllegalOperand("Setcc instruction does not have first value operand.");
    }
    if (!hasVal2(i)) {
      throw new IllegalOperand("Setcc instruction does not have second value operand.");
    }
    if (!getResult(i).isBoolean()) {
      throw new IllegalOperand("Setcc binary instruction result must be boolean operand.");
    }
    if (!getVal1(i).isFirstClassOperand()) {
      throw new IllegalOperand("Setcc binary instruction first value must be first class or block operand.");
    }
    if (!getVal2(i).isFirstClassOperand()) {
      throw new IllegalOperand("Setcc binary instruction second value must be first class or block operand.");
    }
  }
}

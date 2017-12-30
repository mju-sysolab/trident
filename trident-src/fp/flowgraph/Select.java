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

public class Select extends InstructionFormat {

  /*
    Select according to llvm:
    result = (condition) ? val1 : val2;

  */

  
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

  public static boolean conforms(Operator op) {
    return op.format == Select_format;
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

  public static BooleanOperand getCondition(Instruction i) {
    return (BooleanOperand)i.getOperand(1);
  }

  public static void setCondition(Instruction i, BooleanOperand result) {
    i.putOperand(1, result);
  }

  public static boolean hasCondition(Instruction i) {
    return i.getOperand(1) != null; 
  }

  public static Operand getVal1(Instruction i) {
    return i.getOperand(2);
  }

  public static void setVal1(Instruction i, Operand result) {
    i.putOperand(2, result);
  }

  public static boolean hasVal1(Instruction i) {
    return i.getOperand(2) != null;
  }

  
  public static Operand getVal2(Instruction i) {
    return i.getOperand(3);
  }

  public static void setVal2(Instruction i, Operand result) {
    i.putOperand(3, result);
  }

  public static boolean hasVal2(Instruction i) {
    return i.getOperand(3) != null;
  }


  public static Instruction create(Operator o, Type type, Operand result,
				   BooleanOperand condition, Operand val1, Operand val2) {
    return Select.create(o, type, result, condition, val1, val2, null);
  }

  public static Instruction create(Operator o, Type type, Operand result,
				   BooleanOperand condition, Operand val1, Operand val2,
				   BooleanEquation predicate) {
    Instruction i = new Instruction(o, type, predicate);
    i.putOperand(0, result);
    i.putOperand(1, condition);
    i.putOperand(2, val1);
    i.putOperand(3, val2);
    return i;
  }

  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasResult(i)) {
      throw new IllegalOperand("Select instruction does not have a result operand.");
    }
    if (!hasCondition(i)) {
      throw new IllegalOperand("Select instruction does not have a condition operand.");
    }
    if (!hasVal1(i)) {
      throw new IllegalOperand("Select instruction does not have a first value operand.");
    }
    if (!hasVal2(i)) {
      throw new IllegalOperand("Select instruction does not have a second value operand.");
    }
    if (!getVal1(i).isFirstClassOperand()) {
      throw new IllegalOperand("Select instruction first value operand is not a first class operand.");
    }
    if (!getVal2(i).isFirstClassOperand()) {
      throw new IllegalOperand("Select instruction second value operand is not a first class operand.");
    }
    if (!getCondition(i).isBoolean()) {
      throw new IllegalOperand("Select instruction condition operand " + getCondition(i).getName() + " is not boolean.");
    }
    if (! (getResult(i).isBoolean() || getResult(i).isBlock())) {
	throw new IllegalOperand("Select instruction result operand must be a boolean or block operand.");
    }
  }
}

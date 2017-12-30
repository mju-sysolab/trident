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

public class Branch extends InstructionFormat {
  
  public static boolean conforms(Instruction i) {
    return conforms(i.operator());
  }

  public static boolean conforms(Operator op) {
    return op.format == Branch_format;
  }
  
  public static BooleanOperand getBoolean(Instruction i) {
    return (BooleanOperand)i.getOperand(0);
  }

  public static void setBoolean(Instruction i, BooleanOperand result) {
    i.putOperand(0, result);
  }

  public static boolean hasBoolean(Instruction i) {
    return i.getOperand(0) != null;
  }


  public static LabelOperand getTarget1(Instruction i) {
    return (LabelOperand)i.getOperand(1);
  }

  public static void setTarget1(Instruction i, LabelOperand result) {
    i.putOperand(1, result);
  }

  public static boolean hasTarget1(Instruction i) {
    return i.getOperand(1) != null;
  }

  public static LabelOperand getTarget2(Instruction i) {
    return (LabelOperand)i.getOperand(2);
  }

  public static void setTarget2(Instruction i, LabelOperand result) {
    i.putOperand(2, result);
  }

  public static boolean hasTarget2(Instruction i) {
    return i.getOperand(2) != null;
  }

  
  // what does Type mean here ??

  public static Instruction create(Operator o, BooleanOperand test,
				   LabelOperand t1, LabelOperand t2) {
    return Branch.create(o, test, t1, t2, null);
  }

  public static Instruction create(Operator o, BooleanOperand test,
				   LabelOperand t1, LabelOperand t2, BooleanEquation eq) {
    Instruction i = new Instruction(o, Type.Bool, eq);
    i.putOperand(0, test);
    i.putOperand(1, t1);
    i.putOperand(2, t2);
    return i;
  }

  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasBoolean(i)) {
      throw new IllegalOperand("Branch instruction does not have conditional operand.");
    }
    if (!hasTarget1(i)) {
      throw new IllegalOperand("Branch instruction does not have first target operand.");
    }
    if (!hasTarget2(i)) {
      throw new IllegalOperand("Branch instruction does not have second target operand.");
    }
    if (!i.getOperand(0).isBoolean()) {
      throw new IllegalOperand("Branch instruction conditional operand is not boolean.");
    }
    if (!i.getOperand(1).isLabel()) {
      throw new IllegalOperand("Branch instruction first target operand is not a label.");
    }
    if (!i.getOperand(2).isLabel()) {
      throw new IllegalOperand("Branch instruction second target operand is not a label.");
    }
  }

}

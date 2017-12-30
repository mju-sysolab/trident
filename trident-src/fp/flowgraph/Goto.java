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

public class Goto extends InstructionFormat {
  
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

  public static boolean conforms(Operator op) {
    return op.format == Goto_format;
  }
  
  public static LabelOperand getTarget(Instruction i) {
    return (LabelOperand)i.getOperand(0);
  }

  public static void setTarget(Instruction i, LabelOperand result) {
    i.putOperand(0, result);
  }

  public static boolean hasTarget(Instruction i) {
    return i.getOperand(0) != null;
  }

  public static Instruction create(Operator o, LabelOperand val) {
    return Goto.create(o, val, null);
  }

  public static Instruction create(Operator o, LabelOperand val, BooleanEquation eq) {
    Instruction i = new Instruction(o, Type.Label, eq);
    i.putOperand(0, val);
    return i;
  }

  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasTarget(i)) {
      throw new IllegalOperand("Goto instruction does not have target operand.");
    }
    if (!i.getOperand(0).isLabel()) {
      throw new IllegalOperand("Goto instruction target operand is not a label.");
    }
  }

}

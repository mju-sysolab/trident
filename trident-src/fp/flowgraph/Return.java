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

public class Return extends InstructionFormat {

  public static boolean conforms(Instruction i) {
    return conforms(i.operator());
  }
  
  public static boolean conforms(Operator op) {
    return op.format == Return_format;
  }
  
  public static Operand getVal(Instruction i) {
    return i.getOperand(0);
  }

  public static void setVal(Instruction i, Operand result) {
    i.putOperand(0, result);
  }

  public static boolean hasVal(Instruction i) {
    return i.getOperand(0) != null;
  }

  public static Instruction create(Operator o, Type type, Operand val) {
    return Return.create(o, type, val, null);
  }
  
  public static Instruction create(Operator o, Type type, Operand val,
				   BooleanEquation eq) {
    Instruction i = new Instruction(o, type, eq);
    i.putOperand(0, val);
    return i;
  }

  // Include this as well??
  public static Instruction create(Operator o) {
    return Return.create(o, Type.Void, null, null);
  }

  // this is possibly more strange return nothing, conditionally?
  public static Instruction create(Operator o, BooleanEquation eq) {
    return Return.create(o, Type.Void, null, eq);
  }

  public static void verify(Instruction i) throws IllegalOperand {
    if (hasVal(i)) {
      if (!getVal(i).isFirstClassOperand()) {
	throw new IllegalOperand("Return instruction value operand is not a first class operand.");
      }
    }
  }

}

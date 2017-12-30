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

public class Unary extends InstructionFormat {
  
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

  public static boolean conforms(Operator op) {
    return op.format == Unary_format;
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


  public static Operand getVal(Instruction i) {
    return i.getOperand(1);
  }

  public static void setVal(Instruction i, Operand result) {
    i.putOperand(1, result);
  }

  public static boolean hasVal(Instruction i) {
    return i.getOperand(1) != null;
  }

  
  public static Instruction create(Operator o, Type type, 
				   Operand result, Operand val) {
    return Unary.create(o, type, result, val, null);
  }

  public static Instruction create(Operator o, Type type, Operand result,
				   Operand val, BooleanEquation eq) {
    Instruction i = new Instruction(o, type, eq);
    i.putOperand(0, result);
    i.putOperand(1, val);
    return i;
  }
  


}

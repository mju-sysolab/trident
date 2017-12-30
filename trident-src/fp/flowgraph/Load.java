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

public class Load extends InstructionFormat {

  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

   public static boolean conforms(Operator op) {
    return op.format == Load_format;
  }

    
  public static Operand getResult(Instruction i) {
    return i.getOperand(0);
  }

  public static void setResult(Instruction i, Operand result) {
    i.putOperand(0, result);
  }

  public static boolean hasResult(Instruction i) {
    return i.getOperand(0) != null;
  }

  public static Operand getSource(Instruction i) {
    return i.getOperand(1);
  }

  // primal only ???
  public static void setSource(Instruction i, Operand source) {
    i.putOperand(1, source);
  }

  public static boolean hasSource(Instruction i) {
    return i.getOperand(1) != null;
  }

  public static Instruction create(Operator o, Type type,
				   Operand result, Operand source) {
    return Load.create(o, type, result, source, null);
  }


  public static Instruction create(Operator o, Type type,
				   Operand result, Operand source, 
				   BooleanEquation eq) {
    Instruction i = new Instruction(o, type, eq);
    i.putOperand(0, result);
    i.putOperand(1, source);
    return i;
  }

  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasResult(i)) {
      throw new IllegalOperand("Load instruction does not have a result operand.");
    }
    if (!hasSource(i)) {
      throw new IllegalOperand("Load instruction does not have a source operand.");
    }
    if (!(getSource(i).isPrimal() || getSource(i).isAddr())) {
      throw new IllegalOperand("Load instruction source operand " + getSource(i).getName() + " is not primal or address operand.");
    }
    if (!(getResult(i).isBlock() || getResult(i).isBoolean())) {
      throw new IllegalOperand("Load instruction result operand " + getResult(i).getName() + " must be block or boolean operand.");
    }
  }
}

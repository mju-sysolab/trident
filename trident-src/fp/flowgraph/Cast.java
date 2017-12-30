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

public class Cast extends Binary {

  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

  public static boolean conforms(Operator op) {
    return op.format == Cast_format;
  }

  public static TypeOperand getType(Instruction i) {
    return (TypeOperand)i.getOperand(2);
  }

  public static void setType(Instruction i, TypeOperand result) {
    i.putOperand(2, result);
  }

  public static boolean hasType(Instruction i) {
    return i.getOperand(2) != null;
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

  public static Operand getValue(Instruction i) {
    return i.getOperand(1);
  }

  public static void setValue(Instruction i, Operand value) {
    i.putOperand(1, value);
  }

  public static boolean hasValue(Instruction i) {
    return i.getOperand(1) != null;
  }

  public static Instruction create(Operator o, Operand result, 
      Type type, Operand value, Type cast) {
    return Cast.create(o, result, type, value, cast, null);
  }

  public static Instruction create(Operator o, Operand result,
      Type type, Operand value, Type cast, BooleanEquation eq) {
    Instruction i = new Instruction(o, type, eq);
    i.putOperand(0, result);
    i.putOperand(1, value);
    i.putOperand(2, new TypeOperand(cast));
    return i;
  }

  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasResult(i)) {
      throw new IllegalOperand("Cast instruction does not have a result operand.");
    }
    if (!(getResult(i).isBlock() || getResult(i).isBoolean())) {
      throw new IllegalOperand("Cast instruction result operand is not block or boolean.");
    }
    if (!hasValue(i)) {
      throw new IllegalOperand("Cast instruction does not have a value operand.");
    }
    if (!(getValue(i).isFirstClassOperand())) {
      throw new IllegalOperand("Cast instruction result operand is not first class operand.");
    }
    if (!hasType(i)) {
      throw new IllegalOperand("Cast instruction does not have cast type operand.");
    }
    if ( !getType(i).isType()) {
      throw new IllegalOperand("Cast instruction type operand is not a type operand.");
    }
  }

}

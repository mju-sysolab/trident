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

/**
 * Aload is used for Array loads or loads by pointer reference into
 * arrays or structs.
 */
public class ALoad extends InstructionFormat {

  /**
   * Check to see if a given instruction is an ALoad
   * 
   * @param i Instruction to check
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

   /**
    * @param op 
    */
   public static boolean conforms(Operator op) {
    return op.format == ALoad_format;
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

  public static Operand getAddrSource(Instruction i) {
    return i.getOperand(1);
  }

  public static Operand getPrimalSource(Instruction i) {
    return i.getOperand(2);
  }

  public static void setAddrSource(Instruction i, Operand source) {
    i.putOperand(1, source);
  }

  public static void setPrimalSource(Instruction i, Operand source) {
    i.putOperand(2, source);
  }

  public static boolean hasAddrSource(Instruction i) {
    return i.getOperand(1) != null;
  }

  public static boolean hasPrimalSource(Instruction i) {
    return i.getOperand(2) != null;
  }

  public static Instruction create(Operator o, Type type,
				   Operand result, Operand addrSource, Operand primalSource) {
    return ALoad.create(o, type, result, addrSource, primalSource, null);
  }

  public static Instruction create(Operator o, Type type,
				   Operand result, Operand addrSource, Operand primalSource,
				   BooleanEquation eq) {
    Instruction i = new Instruction(o, type, eq);
    i.putOperand(0, result);
    i.putOperand(1, addrSource);
    i.putOperand(2, primalSource);
    return i;
  }

  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasResult(i)) {
      throw new IllegalOperand("ALoad instruction does not have a result operand.");
    }
    if (!hasAddrSource(i)) {
      throw new IllegalOperand("ALoad instruction does not have addr source operand.");
    }
    if (!hasPrimalSource(i)) {
      throw new IllegalOperand("ALoad instruction does not have primal source operand.");
    }
    if (!getAddrSource(i).isAddr()) {
      throw new IllegalOperand("ALoad instruction source operand " + getAddrSource(i).getName() + " is not address operand.");
    }
    if (!getPrimalSource(i).isPrimal()) {
      throw new IllegalOperand("ALoad instruction source operand " + getPrimalSource(i).getName() + " is not primal operand.");
    }
    if (!(getResult(i).isBlock() || getResult(i).isBoolean())) {
      throw new IllegalOperand("Load instruction result operand " + getResult(i).getName() + " must be block or boolean operand.");
    }
  }
}

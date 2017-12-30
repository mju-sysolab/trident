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

public class AStore extends InstructionFormat {

  // old astore format: primal = astore value, addr
  // new astore format: astore value, addr, primal

  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

   public static boolean conforms(Operator op) {
    return op.format == AStore_format;
  }

    
  public static Operand getAddrDestination(Instruction i) {
    return i.getOperand(1);
  }
  
  public static Operand getPrimalDestination(Instruction i) {
    return i.getOperand(2);
  }

  public static void setAddrDestination(Instruction i, Operand result) {
    i.putOperand(1, result);
  }

  public static void setPrimalDestination(Instruction i, Operand result) {
    i.putOperand(2, result);
  }

  public static boolean hasAddrDestination(Instruction i) {
    return i.getOperand(1) != null;
  }
  
  public static boolean hasPrimalDestination(Instruction i) {
    return i.getOperand(2) != null;
  }

  public static Operand getValue(Instruction i) {
    return i.getOperand(0);
  }

  public static void setValue(Instruction i, Operand source) {
    i.putOperand(0, source);
  }

  public static boolean hasValue(Instruction i) {
    return i.getOperand(0) != null;
  }

  public static Instruction create(Operator o, Type type,
    Operand addrDestination,
    Operand primalDestination,
    Operand value) {
    return AStore.create(o, type, addrDestination, primalDestination, value, null);
  }

  public static Instruction create(Operator o, Type type,
    Operand addrDestination,
    Operand primalDestination,
    Operand value, BooleanEquation eq) {
    Instruction i = new Instruction(o, type, eq);
    // in order to prevent EnsureSingleDefs from considering unrolled array stores 
    // as multiple defs, the astore format is being changed to: astore value, addr, primal
    i.putOperand(2, primalDestination);
    i.putOperand(0, value);
    i.putOperand(1, addrDestination);
    return i;
  }
 
  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasAddrDestination(i)) {
      throw new IllegalOperand("AStore instruction does not have an addr destination operand.");
    }
    if (!hasPrimalDestination(i)) {
      throw new IllegalOperand("AStore instruction does not have a primal destination operand.");
    }
    if (!hasValue(i)) {
      throw new IllegalOperand("AStore instruction does not have a source operand.");
    }
    if (!getAddrDestination(i).isAddr()) {
      throw new IllegalOperand("AStore instruction addr destination operand " + getAddrDestination(i).getName() + " is not address operand.");
    }
    if (!getPrimalDestination(i).isPrimal()) {
      throw new IllegalOperand("AStore instruction primal destination operand " + getPrimalDestination(i).getName() + " is not primal operand.");
    }
    if (!getValue(i).isFirstClassOperand()) {
      throw new IllegalOperand("AStore instruction source operand " + getValue(i).getName() + " must be first class operand.");
    }
  }

}

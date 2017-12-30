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

import java.util.*;

import fp.util.BooleanEquation;

public class Call extends InstructionFormat {

  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }

  public static boolean conforms(Operator op) {
    return op.format == Call_format;
  }
  
  // handle results
  public static Operand getResult(Instruction i) {
    return i.getOperand(0);
  }

  public static void setResult(Instruction i, Operand result) {
    i.putOperand(0, result);
  }

  public static boolean hasResult(Instruction i) {
    return i.getOperand(0) != null;
  }

  // handle functions
  public static LabelOperand getFunction(Instruction i) {
    return (LabelOperand)i.getOperand(1);
  }

  public static void setFunction(Instruction i, LabelOperand function) {
    i.putOperand(1, function);
  }

  public static boolean hasFunction(Instruction i) {
    return i.getOperand(1) != null;
  }

  // handle arguments
  public static TypeOperand getArgType(Instruction i, int index) {
    return (TypeOperand)i.getOperand(2*(index+1));
  }

  public static Operand getArgVal(Instruction i, int index) {
    return i.getOperand(2*(index+1) + 1);
  }

  public static void setArg(Instruction i, int index, Operand type, 
			    Operand value) {
    i.putOperand(2*(index+1), type);
    i.putOperand(2*(index+1) + 1, value);
  }

  public static boolean hasArg(Instruction i, int index) {
    return i.getOperand(2*(index+1)) != null;
  }

  public static Instruction create(Operator o, Type type, Operand result, 
  				   LabelOperand func, LinkedList args) {
    return Call.create(o, type, result, func, args, null);
  }

  public static Instruction create(Operator o, Type insType, Operand result,
  				   LabelOperand func, LinkedList args, BooleanEquation eq) {
    Instruction i = new Instruction(o, insType, eq);
    i.putOperand(0, result);
    i.putOperand(1, func);
    if (args != null) {
      i.resizeNumberOfOperands( args.size() + 5 );
      
      int index = 0;
      
      for(ListIterator iter = args.listIterator(); iter.hasNext(); ) {
	TypeOperand type = (TypeOperand)iter.next();
	Operand value = (Operand)iter.next();
	i.putOperand(2*index + 2, type);
	i.putOperand(2*(index + 1) + 1, value);
	index++;
      }
    }
    return i;
  }

  public static void verify(Instruction i) throws IllegalOperand {
    if (!hasResult(i)) {
      throw new IllegalOperand("Call instruction does not have a result operand.");
    }

    if (!hasFunction(i)) {
      throw new IllegalOperand("Call instruction does not have a function operand.");
    }

    int index = 0;
    while (hasArg(i, index)) {
      Operand type = getArgType(i, index);
      Operand value = getArgVal(i, index);
      if ((type==null) || (value==null)) {
	throw new IllegalOperand("Call instruction missing operand in its type/value pairs");
      }
      if (!type.isType()) {
	throw new IllegalOperand("Call instruction type operand " + type.getName() + " is not type operand.");
      }
      index++;
    }
  }
											  
}

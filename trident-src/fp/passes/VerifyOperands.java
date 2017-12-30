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



package fp.passes;

import fp.flowgraph.*;
import java.util.*;

public class VerifyOperands extends Pass implements BlockPass {

  /*
    The purpose of this pass is to:
    1. Verify that instructions have all the operands they claim to
     	have.

    2. Ensure that the correct operand is in the correct location 
    	for a given instruction.
    
    3. Ensure that there are not multiple primal writes to a given
    	primal in a block.

  */
  

  private String _name = null;
  static private int count = 0;


  public VerifyOperands(PassManager pm) {
    super(pm);
  }

  public VerifyOperands(PassManager pm, String s) {
    this(pm);
    _name = s;
  }

  public boolean optimize(BlockNode bn) {
    String debug = "";
    String count_string = "";

    Instruction i = null;
    Set primalsWritten = new HashSet();
    PrimalOperand primalOperand = null;
    Operand anOperand = null;

    ArrayList iList = bn.getInstructions();

    // clear the hashset for each block
    primalsWritten.clear();

    // check for criteria 1 and 6 
    try {
      for (Iterator it2 = bn.getInstructions().iterator(); it2.hasNext(); ) {
	i = (Instruction) it2.next();
	switch (i.operator().opcode) {
	  case Operators.LOAD_opcode:
	    Load.verify(i);
	    break;
	  case Operators.ALOAD_opcode:
	    ALoad.verify(i);
	    break;
	  case Operators.STORE_opcode:
	    Store.verify(i);
	    anOperand = Store.getDestination(i);
	    if (anOperand.isPrimal()) {
	      primalOperand = (PrimalOperand)anOperand;
	      if (primalsWritten.contains(primalOperand)) {
		throw new IllegalOperand("Primal Operand in Store instruction has already been written to in this block");
	      } else {
		primalsWritten.add(primalOperand);
	      }
	    }
	    break;
	  case Operators.ASTORE_opcode:
	    AStore.verify(i);
	    // for now, don't check if primal in store has already been written to in this block
	    break;
	  case Operators.BR_opcode:
	    Branch.verify(i);
	    break;
	  case Operators.GOTO_opcode:
	    Goto.verify(i);
	    break;
	  case Operators.RET_opcode:
	    Return.verify(i);
	    break;
	  case Operators.ADD_opcode:
	  case Operators.SUB_opcode:
	  case Operators.MUL_opcode:
	  case Operators.DIV_opcode:
	    Binary.verifyArith(i);
	    break;
	  case Operators.AND_opcode:
	  case Operators.OR_opcode:
	  case Operators.XOR_opcode:
	    Binary.verifyBitwise(i);
	    break;
	  case Operators.SHL_opcode:
	  case Operators.SHR_opcode:
	    Binary.verifyShift(i);
	    break;
	  case Operators.SETGT_opcode:
	  case Operators.SETGE_opcode:
	  case Operators.SETLT_opcode:
	  case Operators.SETLE_opcode:
	  case Operators.SETNE_opcode:
	  case Operators.SETEQ_opcode:
	    Test.verify(i);
	    break;
	  case Operators.PHI_opcode:
	    Phi.verify(i);
	    break;
	  case Operators.CAST_opcode:
	    Cast.verify(i);
	    break;
	  case Operators.SELECT_opcode:
	    Select.verify(i);
	    break;
	  case Operators.SWITCH_opcode:
	    Switch.verify(i);
	    break;
	  case Operators.GETELEMENTPTR_opcode:
	    Getelementptr.verify(i);
	    break;
	  default:
	    System.err.println("Verify: not a valid operator opcode: " + i.operator().opcode);
	    break;
	}
      } 
    } catch (IllegalOperand e) {
      String errmsg = e.getMessage();
      throw new IllegalOperand(errmsg + "\nError in block: [" + bn.getName() + "] instruction: [" + i.toString() + "]"); 
    }
    return(true);
  }

  public String name() { 
    return "VerifyOperands";
  }

  // ??
  public static int incrementCount() { return count++; }


}


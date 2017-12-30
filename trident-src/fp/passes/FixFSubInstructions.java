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

import java.util.*;

import fp.*;
import fp.flowgraph.*;
import fp.util.*;
import fp.hardware.*;


/** With this pass, kann man Subs mit Invs umtauschen.
 * 
 * @author Kris Peterson
 */
public class FixFSubInstructions extends Pass implements GraphPass {
   

  public FixFSubInstructions (PassManager pm) {
    super(pm);
  }
  
  /** do yo' thang, little buddy!
   * 
   * @param graph_BlockGraph 
   * @return success of being able to return a true
   */
  public boolean optimize(BlockGraph graph) {
    boolean scheduledOk = true;
     
    for (Iterator vIt = graph.getAllNodes().iterator(); 
             vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      for (Iterator vIti = ((ArrayList)node.getInstructions().clone()).iterator(); 
             vIti.hasNext();) {
        Instruction inst = (Instruction) vIti.next();
      
        if(inst.operator.opcode == Operators.SUB_opcode) {
	 //ooooh, nooo, I actually have to do something, maybe!
	  float num=1;
	  Operand val1 = Binary.getVal1(inst);
	  if(val1 instanceof DoubleConstantOperand) {
	    DoubleConstantOperand dval1 = (DoubleConstantOperand)val1;
	    num=(float)dval1.getValue();
	  }
	  else if(val1 instanceof FloatConstantOperand) {
	    FloatConstantOperand fpval1 = (FloatConstantOperand)val1;
	    num=(float)fpval1.getValue();
	  }
	  else if(val1 instanceof IntConstantOperand) {
	    IntConstantOperand ival1 = (IntConstantOperand)val1;
	    num=(float)ival1.getValue();
	  } 
	  if(num==0) {
	  ///nein, jetzt muss ich was unternehmen!!!
	    BooleanEquation eq = inst.getPredicate();
	    Type type = inst.type();
	    Instruction replacementInv = new Instruction(Operators.INV, type, 2, eq);
	    
	    Operand out = Binary.getResult(inst);
	    Operand in = Binary.getVal2(inst);
	    replacementInv.putOperand(0, out);
	    replacementInv.putOperand(1, in);
	    
	    node.removeInstruction(inst);
	    node.addInstruction(replacementInv);
	    
	    //inst.operator = Operators.INV;
	  }
	}
      
      }
      
    }
     
    return true;
  }
  
   
  public String name() { 
    return "FixFSubInstructions";
  }
   
}


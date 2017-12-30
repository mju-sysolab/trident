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

import fp.flowgraph.*;
import fp.util.BooleanEquation;
import fp.util.UseHash;


public class CallReplace extends Pass implements GraphPass {
  

  public CallReplace(PassManager pm) {
    super(pm);
  }

  public String name() { return "CallReplace"; }



  public boolean optimize(BlockGraph graph) {
    
    
    
    for (Iterator vIt = graph.getAllNodes().iterator(); 
           vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      ArrayList instructsToAdd = new ArrayList();
      
      //replace all "calls" with the function definition associated with it
      ArrayList list = node.getInstructions();
      for (Iterator vIt2 = ((ArrayList)list.clone()).iterator(); 
             vIt2.hasNext();) {
	Instruction inst = (Instruction) vIt2.next();
	if(Call.conforms(inst)) {
	  LabelOperand functionName = Call.getFunction(inst);
	  if(functionName.toString().indexOf("sqrt") > 0) {
	    TypeOperand type = Call.getArgType(inst, 0);
	    Operand input = Call.getArgVal(inst, 0);
	    Operand result = Call.getResult(inst);
	    BooleanEquation eq = inst.getPredicate();
	    
	    Instruction newsqrt = Unary.create(Operators.SQRT, type.getType(), 
	                                       result, input, eq);
	    node.removeInstruction(inst);
	    instructsToAdd.add(newsqrt);
	    //node.addInstruction(newsqrt);
	  }
	  else {
	  
	    BlockNode funNode = findNode(graph, functionName);
	    if(funNode == null) {
	      System.out.println("Error!  Function not defined!");
	      System.exit(-1);
	    }
	    ArrayList funList = funNode.getInstructions();
	    instructsToAdd.addAll(funList);
	    graph.removeNode(funNode);
	  }
	}
      }
      node.addInstructions(instructsToAdd);
    
    }
    return true;
  }
	
  BlockNode findNode(BlockGraph graph, LabelOperand operand) {
    for (Iterator vIt = graph.getAllNodes().iterator(); 
           vIt.hasNext();) {
      BlockNode source_Operand = (BlockNode) vIt.next();
      LabelOperand source_label = source_Operand.getLabel();
      if (source_label == operand) 
        return source_Operand;
    }
    
    return (BlockNode)null;
  }

  

	
}

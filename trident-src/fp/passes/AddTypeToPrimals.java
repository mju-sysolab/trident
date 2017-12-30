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
import fp.hardware.*;


/** this was created to add type to primals.  I'm not sure if this is still 
 *  necessary, because I think Christine changed the parser to add types to all 
 *  operands, but anyway, I need this so that I can get the width and size of 
 *  arrays which is only reachable through the type.
 * 
 * @author Kris Peterson
 */
public class AddTypeToPrimals extends Pass implements GraphPass {
   
   
  /** "@param pm
   * 
   * @param pm 
   */
  public AddTypeToPrimals(PassManager pm) {
    super(pm);
  }
  
  /** find primal operands on store or load instructions and set their type 
   *  equal to the type attached to the instruction.
   * 
   * @param graph_BlockGraph 
   */
  public boolean optimize(BlockGraph graph_BlockGraph) {
    for (Iterator vIt = graph_BlockGraph.getAllNodes().iterator(); 
                vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
       
      ArrayList list_ArrayList = node.getInstructions();
      for (Iterator it = ((ArrayList)list_ArrayList).iterator(); 
             it.hasNext(); ) {
        Instruction instr = (Instruction)it.next();
        if(instr.isStore() || instr.isLoad()) {
          int total = instr.getNumberOfOperands();
          for(int i = 0; i < total; i++) {
            Operand op_Operand = instr.getOperand(i);
            if(op_Operand == null) continue;
            if(op_Operand.isPrimal()) {
              op_Operand.setType(instr.type());
            }
          }
           
           
        }
      }
    }
     
     
    return true;
  }
  
   
  public String name() { 
    return "AddTypeToPrimals";
  }
   
}


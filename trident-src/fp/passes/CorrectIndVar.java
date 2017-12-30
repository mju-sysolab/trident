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

import fp.util.DefHash;

import fp.flowgraph.*;



// java imports
import java.util.*;

/**
 * This pass attempts to make index variables more palatable to what
 * we would expect to see.  In general, loops seem to make a cast to
 * and from an "SSA" variable.  This is very unnatural and likely
 * partially unnecessary.  This pass renames the result of that cast
 * and propagates those changes to the rest of the operations.  Also,
 * it modifies indvar.next to be primal because it needs to go outside
 * of the block and used as an input.
 *
 * @author Justin Tripp
 */


public class CorrectIndVar extends Pass implements GraphPass {

  public CorrectIndVar(PassManager pm) {
    super(pm);
  }

  public String name() { return "CorrectIndVar"; }

  public boolean optimize(BlockGraph graph) {
    int node_count = graph.getAllNodes().size();

    // general plan.  Look for instances of indvar and indvar.next
    // fix so that indvar.next is primal and indvar is only assigned once.
    HashMap op_map = new HashMap();
    
    BlockNode order[] = new BlockNode[node_count];
    for (Iterator vIt = graph.getAllNodes().iterator(); vIt.hasNext();){
      BlockNode node = (BlockNode) vIt.next();
      order[node.getPostOrder()] = node;
      makeMap(node, op_map);
    }
    
    // if there are not loops, lets not fix them.
    if (op_map.size() <= 0) return true;
    
    for(int i = 0; i < node_count; i++) {
      fixNode(order[i], op_map);
    }

    return true;
  }
    

  void makeMap(BlockNode node, HashMap op_map) {
    DefHash def_hash = node.getDefHash();
    Operand indvar = null;
    Operand indnext = null;
    for(Iterator kIt = def_hash.keySet().iterator(); kIt.hasNext(); ) {
      Operand op = (Operand) kIt.next();
      if (isOpIndVar(op)) {
	indvar = op;
      } else if (isOpIndNext(op)) {
	indnext = op;
      }
      if (indnext != null && indvar != null) break;
    }

    //this was commented out, because after phi lowering, it is no longer 
    //necessary for indvar.next to be primal
    /*if (indnext != null) {
      Operand new_indnext = Operand.ChangeTmp2Primal(indnext.getName());
      op_map.put(indnext, new_indnext);
    }*/
    
    if (indvar != null) {
      Operand new_indvar = Operand.nextBlock(indvar.getName());
      op_map.put(Type.Int+" "+indvar, new_indvar);
    }
  }


  void fixNode(BlockNode node, HashMap op_map) {
    //System.out.println(" Block "+node.getName());
    //System.out.println("Map "+op_map);

    ArrayList list = (ArrayList)node.getInstructions().clone();
    for(Iterator iIt = list.iterator(); iIt.hasNext();){
      Instruction inst = (Instruction)iIt.next();

      node.removeInstruction(inst);

      // this is ugly because we special case this cast.
      if (Cast.conforms(inst)) {
	Operand result = Cast.getResult(inst);
	Operand indvar = (Operand)op_map.get(Type.Int+" "+result);

	if (indvar != null) {
	  Cast.setResult(inst, indvar);
	  node.addInstruction(inst);
	  continue;
	}
      }

      Type type = inst.type();
      int def_count = inst.getNumberOfDefs();
      int total_count = inst.getNumberOfOperands();
      for(int i = 0; i < def_count; i++) {
	Operand op = inst.getOperand(i);
	if (op_map.containsKey(op)) {
	  inst.putOperand(i, (Operand)op_map.get(op));
	}
      } 
      for(int i = def_count; i < total_count; i++) {
	Operand op = inst.getOperand(i);
	if (op_map.containsKey(op)) {
	  inst.putOperand(i, (Operand)op_map.get(op));
	} else if (op_map.containsKey(type+" "+op)) {
	  inst.putOperand(i, (Operand)op_map.get(type+" "+op));
	}
      }
      node.addInstruction(inst);
    }
  }
  
  final static boolean isOpIndVar(Operand op) {
    String name = op.getName();
    return name.startsWith("indvar") && (name.indexOf("next") < 0);
  }

  final static boolean isOpIndNext(Operand op) {
    String name = op.getName();
    return name.startsWith("indvar") && (name.indexOf("next") >= 0);
  }

					 
    

}

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


public class FindBlockVarCollisions extends Pass implements GraphPass {
  

  public FindBlockVarCollisions(PassManager pm) {
    super(pm);
  }

  public String name() { return "FindBlockVarCollisions"; }

  

  public boolean optimize(BlockGraph graph) {
    HashMap vars = new HashMap();
    for (Iterator blockIt = (new HashSet(graph.getAllNodes())).iterator(); 
    	 blockIt.hasNext(); ) {
      BlockNode node = (BlockNode)blockIt.next();
      for (Iterator iIt = node.getInstructions().iterator(); 
    	   iIt.hasNext(); ) {
        Instruction inst = (Instruction)iIt.next();
	for (Iterator dIt = inst.getDefs().iterator(); 
    	     dIt.hasNext(); ) {
          Operand def = (Operand)dIt.next();
	  if(def.isPrimal()) continue;
	  if (def != null) {
	    if(vars.containsKey(def)) {
	      System.err.println("collision between " + def + " and ");
	      HashSet collisions = (HashSet)vars.get(def);
	      for (Iterator cIt = collisions.iterator(); 
    	           cIt.hasNext(); ) {
	       VarSaver coll = (VarSaver)cIt.next();
	       System.err.println("node " + coll.n + " inst " + coll.i);
	      }
	      VarSaver newColl = new VarSaver(node, inst);
	      collisions.add(newColl);
	      vars.put(def, collisions);
	    }
	    else {
	      VarSaver newColl = new VarSaver(node, inst);
	      HashSet setOfCollisions = new HashSet();
	      setOfCollisions.add(newColl);
	      vars.put(def, setOfCollisions);
	    }
	  }
	}
      }
    
	/*for (Iterator opIt = inst.getOperators().iterator(); 
    	 opIt.hasNext(); ) {
          Operator op = (Operator)opIt.next();
	  //if(vars.
	}*/
      
    }    
    return true;
  }
	
  private class VarSaver {
    public BlockNode n;
    public Instruction i;
    public VarSaver(BlockNode node, Instruction inst){
    
      n = node;
      i = inst;
    
    }
    /*public String name() {
    
    }*/
  }
}

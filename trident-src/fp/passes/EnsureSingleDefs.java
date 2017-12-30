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
import fp.util.BooleanOp;
import fp.util.DefHash;
import fp.util.MultiDefHash;
import fp.util.UseHash;


public class EnsureSingleDefs extends Pass implements GraphPass {
  /*
   * The purpose of this class is to ensure that all definitions for
   * a given block are singular.  This helps make the def-use hashes
   * useful.
   * 
   * This pass assumes several things:
   * 1. The operations are in execution order
   * 2. Blocks are still basic blocks (not merged).
   * 3. Collisions will usually be two operands. 
   * 
   * @author Justin L. Tripp
   * @version $Id: EnsureSingleDefs.java 2259 2006-02-13 16:29:34Z kpeter $
   */

  public EnsureSingleDefs(PassManager pm) {
    super(pm);
  }

  public String name() { return "EnsureSingleDefs"; }


  public boolean optimize(BlockGraph graph) {

    boolean change = true;
    // Make a hash of Multiple Definition Hashes.
    // Then we will see if we need to create new blocks.

    while(change) {
      change = false;

    HashMap check_defs = new HashMap();

    for (Iterator bIt = graph.getAllNodes().iterator(); bIt.hasNext();){
      BlockNode node = (BlockNode) bIt.next();


      if (node == graph.ENTRY || node == graph.EXIT) {
	continue;
      } else {
	MultiDefHash def_hash = new MultiDefHash();

	for(Iterator iIt = node.getInstructions().iterator(); iIt.hasNext();){
	  Instruction inst = (Instruction)iIt.next();
	  def_hash.add(inst);
	}

	// find a reason to keep the multi-def hash
	// filter to only multiples.
	boolean keep = false;
	for(Iterator vIt = def_hash.values().iterator(); vIt.hasNext(); ) {
	  ArrayList list = (ArrayList) vIt.next();
	  if (list.size() > 1) {
	    keep = true;
	  } else {
	    // filter simple lists.
	    vIt.remove();
	  }
	} // end def_hash value iterator
	if (keep)
	  check_defs.put(node, def_hash);
      }
    }

    // Why iterate twice ... Well, mostly to avoid concurrent modification.
    // If I want to split the block and modify the blockgraph, I cannot be
    // in the middle of iterating on the graph.

    for (Iterator esIt = check_defs.entrySet().iterator(); esIt.hasNext();){
      Map.Entry entry = (Map.Entry)esIt.next();
      BlockNode node = (BlockNode)entry.getKey();
      ArrayList list = node.getInstructions();
      MultiDefHash hash = (MultiDefHash)entry.getValue();
      
      //System.out.println(" Node "+node.getName());
      //System.out.println(" occurences "+hash);

      // find the first "second" occurence in the lists and
      // split on that.
      Instruction split = null;
      int split_index = list.size();

      // check each group
      for(Iterator hIt = hash.values().iterator(); hIt.hasNext();){ 
        ArrayList multi_list = (ArrayList)hIt.next();
	Instruction first = null;
	int first_index = list.size();
	Instruction second = null;
	int second_index = list.size();
	
	for(Iterator mlIt = multi_list.iterator(); mlIt.hasNext();) {
	  Instruction check = (Instruction)mlIt.next();
	  int check_index = list.indexOf(check);
	  if (first == null) {
	    first = check;
	    first_index = check_index;
	  } else if (first_index > check_index) {
	    second = first;
	    second_index = first_index;
	    first = check;
	    first_index = check_index;
	  } else if (second_index > check_index) {
	    second = check;
	    second_index = check_index;
	  }
	}
	
	if (second_index < split_index) {
	  split = second;
	  split_index = second_index;
	}
      }

      // karate chop
      if (split != null) {
	graph.splitBlock(node, split);
	change = true;
      }
    }
    } // end while(!change)

    return true;
  }



}

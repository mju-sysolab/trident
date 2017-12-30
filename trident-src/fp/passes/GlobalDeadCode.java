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
import fp.util.UseHash;


public class GlobalDeadCode extends Pass implements GraphPass {
  /*
   * The purpose of this class is to verify that code is actually used.
   * It does this by examining the lifetimes of the the variables in the 
   * code and if a value is produced in a given block and there is no way
   * to use it, then it is killed.
   * 
   * @author Justin L. Tripp
   * @version $Id: GlobalDeadCode.java 2259 2006-02-13 16:29:34Z kpeter $
   */

  public GlobalDeadCode(PassManager pm) {
    super(pm);
  }

  public String name() { return "GlobalDeadCode"; }

  // This is not taking into account global variables -- did we record
  // that info anywhere ????

  public boolean optimize(BlockGraph graph) {
    int node_count = graph.getAllNodes().size();

    HashMap live_in = new HashMap();
    HashMap live_out = new HashMap();

    //System.out.println(" graph size "+node_count);

    BlockNode post_order[] = new BlockNode[node_count];
    BlockNode entry_node = (BlockNode)graph.ENTRY;

    Set exit_nodes = graph.getExitNodes();
    boolean empty_exits = exit_nodes.isEmpty();
    //Set root_set = empty_exits ? infinite_loop : exit_nodes;

    //System.out.println("Build post_order");

    for (Iterator vIt = graph.getAllNodes().iterator(); vIt.hasNext();){
      BlockNode node = (BlockNode) vIt.next();
      post_order[node.getPostOrder()] = node;

      // this approach does not differentiate between inputs and outputs
      if (node == graph.ENTRY || node == graph.EXIT) {
	// All operands in ...
	live_in.put(node, new HashSet(graph.getVariableHash().keySet()));
	live_out.put(node, new HashSet(graph.getVariableHash().keySet()));
      } else {
	live_in.put(node, new HashSet());
	live_out.put(node, new HashSet());
      }
    }

    // This repeats until it reaches steady state.
    // while (blah)
    boolean change = true;
    while (change == true) {
      change = false;
      for (int i=0; i < post_order.length; i++) {
        BlockNode node = post_order[i];
	
	if (node == graph.ENTRY || node == graph.EXIT) continue;

	if (calculateLiveness(node, live_in, live_out))
	  change = true;
      }
    }
    
    for (int i=0; i < post_order.length; i++) {
      BlockNode node = post_order[i];

      /*
      System.out.println("Node "+node.getName());
      System.out.println(" live in "+live_in.get(node));
      System.out.println(" live out "+live_out.get(node));
      System.out.println(" instructions\n"+node);
      */

      HashSet node_out = (HashSet)live_out.get(node);
      HashSet good_operand = new HashSet();
      
      good_operand.addAll(node_out);
      // need to add boolean operand on exit edges ???

      for (Iterator edge_iter = node.getOutEdges().iterator(); 
	   edge_iter.hasNext(); ) {
	BlockEdge edge = (BlockEdge)edge_iter.next();
	addEquationOperands(good_operand, edge.getPredicate());
      }


      ArrayList copy = (ArrayList)node.getInstructions().clone();
      
      int last_size = copy.size() + 1;
      int attempt = 0;

      while (last_size > copy.size() || attempt == 0) {
	if (last_size == copy.size()) {
	  attempt++;
	} else { 
	  attempt = 0;
	  last_size = copy.size();
	}
	
	for(ListIterator iter = copy.listIterator(last_size); 
	    iter.hasPrevious(); ) {
	  Instruction instruction = (Instruction)iter.previous();
	  
	  int def_count = instruction.getNumberOfDefs(); 

	  if (def_count == 0) {
	    // nop, ret void, goto, br, switch, store

	    if (Store.conforms(instruction)) {
	      Operand destination = Store.getDestination(instruction);
	      if (good_operand.contains(destination)) {
		addOperandsToHash(good_operand, instruction);
		iter.remove();
	      }
	    } else {
	      // ??? nop, ret void, goto, br, switch
	      addOperandsToHash(good_operand, instruction);
	      iter.remove();
	    }

	  } else {
	    for(int op_count = 0; op_count < def_count; op_count++) {
	      Operand o = instruction.getOperand(op_count);
	      if (o != null) {
		if (good_operand.contains(o)) {
		  addOperandsToHash(good_operand, instruction);
		  iter.remove();
		  break;
		}
	      }
	    }
	  }
	} // end copy.iterator
      } // end while(last_size)
    
      // get rid of what is left.
      for(ListIterator iter = copy.listIterator(); iter.hasNext(); ) {
	Instruction instruction = (Instruction)iter.next();
	node.removeInstruction(instruction);
      }
      
    } // end node iterator

    // I also need to record the "live-ness" info somewhere in each BlockNode.
    // Examine Where the primals are defined.  If it is no longer live-out from
    // that location then remove that definition!!
    //
    // What about removing variables from the Def hash?
    // I could possibly do an analysis to determine what directly affects 
    // values into a channel, but that is probably too much at this point.


    return true;
  }


  private void addOperandsToHash(HashSet set, Instruction instruction) {
    addEquationOperands(set, instruction.getPredicate());
    
    int def_count = instruction.getNumberOfDefs();
    for (int i = def_count; i < instruction.getNumberOfOperands(); i++) {
      Operand o = instruction.getOperand(i);
      if (o != null) set.add(o);
    }
  }

  private void addEquationOperands(HashSet set, BooleanEquation eq) {
    // nothing to do if there are no operands.
    if (eq.isTrue() || eq.isFalse()) return;
    
    //System.out.println("Adding Equation "+eq);

    for(ListIterator iter = eq.getTwoLevel().listIterator(); 
	iter.hasNext(); ) {
      LinkedList list = (LinkedList) iter.next();
      for(ListIterator list_iter = list.listIterator(); 
	  list_iter.hasNext(); ) {
	BooleanOp bool_op = (BooleanOp)list_iter.next();
	BooleanOperand o = (BooleanOperand)bool_op.getOp();
	set.add(o);
      }
    }
  }
  


  boolean calculateLiveness(BlockNode node, HashMap live_in, 
			    HashMap live_out) {

    ArrayList list = node.getInstructions();
    
    HashSet my_live_in = (HashSet)live_in.get(node);
    HashSet my_live_out = (HashSet)live_out.get(node);

    DefHash def_hash = node.getDefHash();
    UseHash use_hash = node.getUseHash();
      
    HashSet new_in = new HashSet();
      
    //System.out.println("use_hash : "+use_hash);

    // Make a set of the uses
    for (Iterator iter = use_hash.keySet().iterator(); iter.hasNext();) {
      Operand op = (Operand)iter.next();
      //System.out.println(" op "+op);
      if (op == null) continue;
      if (op.isPrimal()) {
	new_in.add(op);
      } 
    }

    // Make a set of the live-out minus defs
    for (Iterator iter = ((HashSet)live_out.get(node)).iterator(); 
	 iter.hasNext();) {
      Operand op = (Operand)iter.next();
      if (!def_hash.containsKey(op)) {
	new_in.add(op);
      }
    }

    // the "real" in is the union of the above sets
    live_in.put(node, new_in);
    
    HashSet new_out = new HashSet();

    // the "real" out is the union of all sucessive nodes "ins"
    for (Iterator it = new ArrayList(node.getOutEdges()).iterator();
	 it.hasNext();){
      BlockEdge edge = (BlockEdge) it.next();
      BlockNode child_v = (BlockNode) edge.getSink();

      //System.out.println(" Node sucessor "+child_v.getName());
      //System.out.println(" live in "+live_in.get(child_v));
      // This is because this info is poor quality -- it needs to 
      // be updated after the merge of blocks
      
      //if (live_in.get(child_v) != null)
      new_out.addAll((HashSet)live_in.get(child_v));
    }
      
    live_out.put(node,new_out);

    return (!my_live_in.equals(new_in)
	     || !my_live_out.equals(new_out));
  }


}

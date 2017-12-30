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
package fp.circuit;

import java.util.*;

import fp.util.BooleanEquation;
import fp.util.BooleanOp;
import fp.flowgraph.*;

public class GenericStateMachine extends StateMachine {

  public GenericStateMachine(int states, BlockNode bn) {
    super(states);
    System.out.println("Generating a GenericStateMachine");
    HashMap cycle_info = findExitConditions(states, bn);
    BooleanEquation backedgePredicate = getBackEdge(bn);
    addExitConditionsToTable(cycle_info, backedgePredicate);
    collectInputs();    // this must be done after the edges are setup
    System.out.println("Exit conditions: "+cycle_info);
    System.out.println("STATE MACHINE: \n"+this);
  }


  /**
   * This method returns a hashmap containing cycle information. The info 
   * allows the statemachine to end early if the instructions in the cycle 
   * have predicates that evaluate to false.
   */
  static HashMap findExitConditions(int states, BlockNode bn) {
    HashMap cycle_info = new HashMap();
    
    // Get block start and end times.
    int blk_start = 0;
    int blk_end = states-1;

    // For each cycle in this block node...
    for(int i = blk_start; i < blk_end; i++) {
      BooleanEquation bool = new BooleanEquation();
      
      // For each instruction in the block node...
      Iterator it = bn.getInstructions().iterator();
      while(it.hasNext()) {
	Instruction inst = (Instruction) it.next();
	int i_start = inst.getExecClkCnt();
	int i_end = i_start + (int) inst.getRunLength(); // truncate runlength
	
	// If this instruction belongs to the current cycle, then OR the 
	// predicate with all other same-cycle inst's predicates.
	if((i_start <= i) && (i_end >= i)) {
	  // If the value of the predicate isn't known yet, then this cycle 
	  // must be executed; if the value is known, then OR it.
	  BooleanEquation pred = inst.getPredicate();
	  if(predHasBeenDefined(pred, i, bn)) {
	    //System.out.println("PRED: defined:     "+pred+", cycle = "+i);
	    bool.or(pred);
	  } else {
	    //System.out.println("PRED: NOT defined: "+pred+", cycle = "+i);
	    bool.or(new BooleanEquation(true));
	  }
	}
      }
      // Add the OR'ed predicates to the current cycle's map entry.
      cycle_info.put(new Integer(i), bool);
    }
    //System.out.println(cycle_info);
    return cycle_info;
  }


  /**
   * This method takes as input a hashmap containing the exit conditions 
   * for the statemachine, and it adds those conditions to the SM.
   */
  void addExitConditionsToTable(HashMap cond, BooleanEquation backedge) {
    // Find the first non-false state...
    int first = 0;
    for(int i = 0; i < cond.size(); i++) {
      BooleanEquation cycBool = (BooleanEquation) cond.get(new Integer(i));
      if(!cycBool.isFalse()) {
	first = i;
	break;
      }
    }

    // For each cycle in the hashmap...
    Iterator it = cond.keySet().iterator();
    while(it.hasNext()) {
      int cycle = ((Integer) it.next()).intValue();
      BooleanEquation bool = (BooleanEquation) cond.get(new Integer(cycle));
      //System.out.println("bool: "+bool);
      BooleanEquation b = (BooleanEquation)bool.clone();
      // CHANGE: is this correct???
      //if(!b.isTrue()) {  // If false or not yet known...
      if((!b.isTrue()) && (!b.isFalse())) {  // If not yet known...
	if(cycle == 0) {
	  // Deal with the last (or default) state.
	} else if (cycle == (_states-1)) {
	  // Do nothing??
	} else if (cycle > (_states-1)) {
	  // ERROR??
	} else {
	  boolean change = false;

	  _table[cycle-1][cycle] = b;
	  BooleanEquation b2 = (BooleanEquation)b.clone();

	  // For each of the possible next states(except the default state)
	  for(int i = cycle+1; i < (_states-1); i++) {
	    BooleanEquation b_tmp = (BooleanEquation)cond.get(new Integer(i));
	    // If this is not a repeated predicate...
	    if(!b2.isEqual(b_tmp)) {
	      //System.out.println("IS NOT EQUAL BOOL: "+ i);
	      _table[cycle-1][i] = b2.not();
	      change = true;
	      break;
	    }
	  }
	  if(!change) {
	    BooleanEquation b2_not = b2.not();
	    if((backedge != null) && b2_not.isEqual(backedge)) {

	      //System.out.println("ADDING ELEMENT! "+b2_not+","+addToCyc);
	      // Add the pred to the first non-false state.
	      _table[cycle-1][first] = b2_not; 
	    } else {
	      //System.out.println("CHANGING LAST ELEMENT IN TABLE! "+b2_not);
	      _table[cycle-1][_states-1] = b2_not;
	    }
	  }
	}
      }
    }

    // Fix the last state, if this has a backedge.
    if(backedge != null) {
      int last = cond.size();
      BooleanEquation backedge_not = ((BooleanEquation)backedge.clone()).not();
      _table[last-1][first] = backedge;
      _table[last-1][last] = backedge_not;
    }
  }


  /**
   * This method checks whether an operand has been defined previous to the 
   * given cycle.  It returns true if it has been defined and false otherwise.
   */
  static protected boolean hasBeenDefined(String operand, int cycle, 
					  BlockNode bn) {
    // Find the last definition of the operand before it's use in the 
    // current cycle.
    Iterator it = bn.getInstructions().iterator();
    while(it.hasNext()) {
      Instruction i = (Instruction) it.next();
      // If this inst finishes before the cycle when the use occurs...
      int end_cycle = i.getExecClkCnt() + (int) i.getRunLength();
      if(end_cycle < cycle)
	// If this inst defines the operand...
	if(i.getOperand(0).toString().trim().equals(operand)) {
	  return true;	
	}
    }
    return false;
  }


  /**
   * This method checks whether a boolean equation has been defined previous 
   * to the given cycle. It returns true if all parts of the boolean equation 
   * have been defined and false otherwise.
   */
  static protected boolean predHasBeenDefined(BooleanEquation be, int cycle, 
				       BlockNode bn) {
    // For each BooleanOp in the BooleanEquation, check if it has been 
    // defined.  If any BooleanOp has not been defined, then return false.
    for(ListIterator iter = be.getTwoLevel().listIterator();iter.hasNext(); ) {
      Iterator list_iter = ((LinkedList)iter.next()).listIterator();
      while(list_iter.hasNext()) {
	String operand = ((BooleanOp)list_iter.next()).getOp().getBoolName();
	if(!hasBeenDefined(operand, cycle, bn))
	  return false;
      }
    }
    return true;
  }
}

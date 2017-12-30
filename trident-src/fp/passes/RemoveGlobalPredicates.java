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

import fp.util.BooleanEquation;
import fp.flowgraph.*;

// java imports
import java.util.*;


public class RemoveGlobalPredicates extends Pass implements BlockPass {

  public RemoveGlobalPredicates(PassManager pm) {
    super(pm);
  }

  public String name() { return "RemoveGlobalPredicates"; }

  public boolean optimize(BlockNode node) {
    // get a list of valid predicates
    HashSet local_booleans = new HashSet();
    HashSet all_booleans = new HashSet();
    HashSet sensitive_ops = new HashSet();
    
    HashMap non_local_map = new HashMap();

    //System.out.println(" start non "+non_local_map);

    for(Iterator iter = node.getInstructions().iterator(); iter.hasNext(); ) {
      Instruction inst = (Instruction)iter.next();
      BooleanEquation eq = inst.getPredicate();

      for(Iterator bool_iter = eq.listBooleanOperands().listIterator();
	  bool_iter.hasNext(); ) {
	BooleanOperand op = (BooleanOperand)bool_iter.next();
        all_booleans.add(op);
      }

      // Select seems to be the only one.
      if (Select.conforms(inst)) {
	sensitive_ops.add(inst);
      }

      /* // No instructions have equations
      Instruction inst = statement.getInstruction();
      eq = inst.getEquation();
      if (eq != null) {
        iter = eq.listPredicates().listIterator();
        while(iter.hasNext()) {
          Predicate next = (Predicate)iter.next();
          all_preds.put(next,next);
        }
      }
      */
      
      // add booleans in decisions.
      if (!Test.conforms(inst)) continue;
      
      Operand result = Test.getResult(inst);
      // If you do not have a result, it cannot be a predicate.
      //if (result == null) continue;
      local_booleans.add( result );
    } // end instruction iter

    //System.out.println(" list of preds "+all_booleans);
    //System.out.println(" local hash "+local_booleans);
    
    for(Iterator all_iter = all_booleans.iterator(); 
	all_iter.hasNext(); ) {
      BooleanOperand p = (BooleanOperand) all_iter.next();
      
      if (!local_booleans.contains(p)) {
	// if not local ...
        // these predicates are a potential source of waste
        // BooleanEquation knows all op -> Preds.
        BooleanEquation non_local_eq = new BooleanEquation();
        non_local_eq.or(p);
        non_local_map.put(p, non_local_eq);
        //non_local.andTerm(p);
      }
    }
    
    //System.out.println(" after non map "+non_local_map);
    if (non_local_map.isEmpty()) {
      // If there is nothing to do -- don't do it.

      //System.out.println(" after "+vertex.getStatementList());
      return true;
    }
    // get a list of equations to filter
    LinkedList equations = new LinkedList();
    // get the predicates from the statements
    for(Iterator iter = node.getInstructions().iterator();
	iter.hasNext(); ) {
      Instruction stat = (Instruction)iter.next();
      equations.add( stat.getPredicate() );
      /* // does this need to add the one boolean op?
      if (stat.getInstruction().isInstruction(PSSAOperation.MUX)) {
        equations.add(stat.getInstruction().getEquation());
      }
      */
    }
    
    // get the predicates from the out edges
    for (Iterator it = node.getOutEdges().iterator(); it.hasNext();){
      BooleanEquation bool = ((BlockEdge)it.next()).getPredicate();
      // we cannot add nulls.
      if (bool != null )
        equations.add( bool);
    }

    // filter the predicate, remove non-local predicates
    for (ListIterator eq_iter = equations.listIterator(); eq_iter.hasNext();) {
      BooleanEquation bool_eq = (BooleanEquation)eq_iter.next();
      
      // no sense is wasting time.
      if (bool_eq.isFalse() || bool_eq.isTrue()) continue;
      
      BooleanEquation result  = new BooleanEquation(false);
      BooleanEquation new1_eq = (BooleanEquation)bool_eq.clone();
      BooleanEquation new2_eq = (BooleanEquation)bool_eq.clone();

      //System.out.println("\n original eq - "+bool_eq);
      for(Iterator non_local_iter = non_local_map.values().iterator(); 
	  non_local_iter.hasNext(); ) {

        BooleanEquation this_eq = (BooleanEquation)non_local_iter.next();
        BooleanEquation not_eq  = (BooleanEquation)this_eq.clone();
        not_eq.xor(new BooleanEquation().setTrue());

        /*
        System.out.println(" start     eq "+new1_eq);
        System.out.println(" start     eq "+new2_eq);
        System.out.println(" restrict  eq "+this_eq);
        System.out.println(" restrict  eq "+not_eq);
        */
        new1_eq.restrict(this_eq);
        new2_eq.restrict(not_eq);

        new1_eq.or(new2_eq);

        //System.out.println(" after     eq "+new1_eq);
        //System.out.println(" after     eq "+new2_eq);
        result.or(new1_eq);
        //System.out.println(" result    eq "+result);

        new1_eq = (BooleanEquation)result.clone();
        new2_eq = (BooleanEquation)result.clone();
      }
      // Make a copy.
      bool_eq.setFalse().or(result);
      //System.out.println(" end      eq - "+bool_eq);
    }
    
    for(Iterator it = sensitive_ops.iterator(); it.hasNext(); ) {
      Instruction inst = (Instruction)it.next();
      if (Select.conforms(inst)) {
	BooleanOperand bool = Select.getCondition(inst);
	if (!local_booleans.contains(bool)) {
	  // if it is not local then it must be true?
	  Select.setCondition(inst, BooleanOperand.TRUE);
	}
      }
    }

    
    return true;
  }
  
}

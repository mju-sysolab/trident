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

package fp.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import fp.flowgraph.*;

public class BEqToList extends LinkedList {

  /*
    The purpose of this class is to take a boolean equation
    and convert it into a set instructions to compute the 
    same result.  This approach is okay, but not super optimal.
    
    There is a slight refinement by keeping track of inversions,
    but this only knows about the current equation and does not
    know about similar equations in some larger scope.
  */



  // this tells you what the whole thing "equals"
  BooleanOperand result = null;
  HashMap neg_hash;

  public BEqToList(BooleanEquation eq, BooleanEquation pred) {
    super();

    if (eq.isTrue()) {
      result = BooleanOperand.TRUE;
    } else if (eq.isFalse()) {
      result = BooleanOperand.FALSE;
    } else {
      neg_hash = new HashMap();
      createList(eq, pred);
    }
    /*
    System.out.println(" Equation "+eq);
    System.out.println(" Instructions "+this);
    System.out.println(" Result "+result);
    */

  }

  public BooleanOperand getResult() { return result; }

  private void createList(BooleanEquation eq, BooleanEquation pred) {
    TwoLevelBooleanEquation two_level = eq.getTwoLevel();
    
    LinkedList term_list = new LinkedList();
    for(ListIterator iter = two_level.listIterator(); iter.hasNext(); ) {
      LinkedList list = (LinkedList) iter.next();
      LinkedList op_list = new LinkedList();

      for(ListIterator list_iter = list.listIterator(); 
          list_iter.hasNext(); ) {
	BooleanOp bool_op = (BooleanOp)list_iter.next();
	BooleanOperand o = (BooleanOperand)bool_op.getOp();
	boolean sense = bool_op.getSense();
	
	if (sense) 
	  op_list.add(o);
	else {
	  BooleanOperand neg_op = (BooleanOperand)neg_hash.get(o);
	  if (neg_op == null) {
	    neg_op = (BooleanOperand)o.getNext();
	    add(Unary.create(Operators.NOT, Type.Bool, neg_op, o, pred));
	    neg_hash.put(o, neg_op);
	  }
	  op_list.add(neg_op);
	}
      }
      
      term_list.add(doList(op_list, Operators.AND, pred));
      
    }
    // set the result
    result = doList(term_list, Operators.OR, pred);
  }
  
  
  private BooleanOperand doList(LinkedList list, Operator o, 
				BooleanEquation eq) {
    while(list.size() > 1) {
      LinkedList work_list = (LinkedList)list.clone();
      //System.out.println(" Work List "+work_list);
      for(ListIterator iter = work_list.listIterator(); iter.hasNext(); ) {
	BooleanOperand op1 = (BooleanOperand) iter.next();
	list.remove(op1);
	BooleanOperand op2 = null;
	if (iter.hasNext()) {
	  op2 = (BooleanOperand) iter.next();
	  list.remove(op2);
	} else {
	  list.add(op1);
	  continue;
	}
	
	BooleanOperand op = Operand.nextBoolean("btmp");
	add(Binary.create(o, Type.Bool, op, op1, op2, eq));
	list.add(op);
      }
    }
    
    return (BooleanOperand)list.iterator().next();
  }

 public static void main( String[] args ) {
    BooleanEquation bool = new BooleanEquation();
    BooleanEquation bool2 = new BooleanEquation();
    
    BooleanOperand A = Operand.newBoolean("A");
    BooleanOperand B = Operand.newBoolean("B");
    BooleanOperand C = Operand.newBoolean("C");
    BooleanOperand D = Operand.newBoolean("D");

    bool.or(A);
    bool.or(B);
    bool.or(C);

    bool2.or(A);
    bool2.and(B);
    bool2.and(C);

    bool.or(bool2);

    System.out.println(" "+bool);
    System.out.println(" "+new BEqToList(bool, new BooleanEquation(true)));
 }


}

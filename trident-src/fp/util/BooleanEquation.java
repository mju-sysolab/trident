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

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;

import java.util.HashMap;

import fp.util.bdd.BDD;
import fp.util.bdd.BDDNode;



public class BooleanEquation implements Truth {

  private LinkedList two_level;
  /*
    this hash map keeps track of the order in which
    variables were added to the BDD.  BDDs build their graphs
    based on how the variables are ordered and we need to keep
    track since we like names.
  */
    
  private static HashMap name_order = new HashMap(100);
  private static int name_count = 0;

  private static BDD bdd = new BDD(500,10000);
  private BDDNode _eq;

  static final int UNDEFINED = FALSE;
  static final int DONTCARE = TRUE;

  //private Hashtable name_location;
  //private Hashtable predicate_hash;
  
  // CONSTRUCTORS

  public BooleanEquation() {
    //two_level = new LinkedList();
    // set zero
    _eq = BDDNode.ZERO;

  }

  public BooleanEquation(boolean b) {
    this();
    if (b) this.setTrue();
  }

  public BooleanEquation(Bool n, boolean sense) {
    this();
    if (!sense) 
      nor(n);
    else 
      or(n);
  }

  public BooleanEquation(Bool n) {
    this(n, true);
  }

  public BooleanEquation(BooleanEquation eq) {
    this();
    or(eq);
    
  }

  
  BooleanEquation(TwoLevelBooleanEquation two) {
    this();

    if (two != null) {
      if (two.size() > 0) {
	for(ListIterator eq_iter = two.listIterator(); eq_iter.hasNext(); ) {
	  LinkedList term = (LinkedList)eq_iter.next();
	  // nothing fancy if this is empty because we just made it true.
	  BooleanEquation term_eq = new BooleanEquation(true);
	  for(ListIterator term_iter = term.listIterator(); 
	      term_iter.hasNext(); ) {
	    BooleanOp op = (BooleanOp)term_iter.next();
	    term_eq.and(new BooleanEquation(op.getOp(), op.getSense()));
	  }
	  or(term_eq);
	}
      }
    }
  }

  private BooleanEquation(BDDNode eq) {
    _eq = eq;
  }

  


  BDDNode getEq() { return _eq; }
  void setEq(BDDNode eq) { _eq = eq; }

  public static BooleanEquation NOT(Bool o) {
    return new BooleanEquation(o, false);
  }

  
  /*
    This is the new interface for getting the two level format.
    Since this will be backed by a BDD instead of the doubly 
    linked-list, this will be the place to get non BDD kinds of
    information. :)

   */

  public TwoLevelBooleanEquation getTwoLevel() {
    TwoLevelBooleanEquation result = null;
    if (_eq != null) {
      
      result = new TwoLevelBooleanEquation();
      Stack stack = new Stack();
      
      //new Exception().printStackTrace();
      
      //System.out.println(" _eq "+_eq);
      
      if (_eq.isOne() || _eq.isZero()) {
	if (_eq.isOne()) {
	  result.add(new LinkedList());
	}
      } else {
	recurseBDD(_eq,result,stack);
      }
      
    }
    return result;
  }

  /* this could be potentially cheaper if it was supported 
     directly in the BDD lib, but I am not going to think much about
     it right now.
  */
  public BooleanEquation replaceBool(Bool b, Bool c) {
    if (isTrue()) return new BooleanEquation(true);
    if (isFalse()) return new BooleanEquation(false);
    TwoLevelBooleanEquation two = getTwoLevel();
    BooleanEquation result = new BooleanEquation(false);
    
    for(ListIterator eq_iter = two.listIterator(); eq_iter.hasNext();) {
      LinkedList term = (LinkedList)eq_iter.next();
      BooleanEquation term_eq = new BooleanEquation(true);
      for(ListIterator term_iter = term.listIterator(); term_iter.hasNext();) {
	BooleanOp op = (BooleanOp)term_iter.next();
	Bool bool_op = op.getOp();
	if (bool_op == b) 
	  term_eq.and(new BooleanEquation(c, op.getSense()));
	else
	  term_eq.and(new BooleanEquation(bool_op, op.getSense()));
      }
      result.or(term_eq);
    }
    return result;
  }
  

  void recurseBDD(BDDNode node, LinkedList r, Stack s) {

    BDDNode high = node.getHigh();
    BDDNode low = node.getLow();

    //System.out.println(" 2lvl node "+node);

    HashEntry entry = (HashEntry)name_order.get(new Integer(bdd.getVar(node)));

    //System.out.println(" 2lvl high "+high+" zero ? "+high.isZero());
    if (high.isOne()) {
      LinkedList new_list = new LinkedList();
      new_list.add(new BooleanOp(entry.getOperand(),true));
      new_list.addAll(s);
      r.add(new_list);
      //return;
    } else if (!high.isZero()) {
      s.push(new BooleanOp(entry.getOperand(),true));
      recurseBDD(high, r, s);
      s.pop();
    }
    
    //System.out.println(" 2lvl low  "+low);
    if (low.isOne()) {
      LinkedList new_list = new LinkedList();
      new_list.add(new BooleanOp(entry.getOperand(),false));
      new_list.addAll(s);
      r.add(new_list);
      //return;
    } else if (!low.isZero()) {
      s.push(new BooleanOp(entry.getOperand(), false));
      recurseBDD(low, r, s);
      s.pop();
    }
  }

  private HashEntry getEntry(Bool n) {
    HashEntry entry = (HashEntry)name_order.get(n);
    
    if (entry == null) {
      name_count++;
      // This would be better if we set it less frequently.
      bdd.setVarNum(name_count+5);
      BDDNode node = bdd.bdd_ithvar(name_count);
      entry = new HashEntry(node, name_count, n);
      // put in hash.
      name_order.put(n, entry);
      // or should this guy be a linkedList?  It may be faster...
      name_order.put(new Integer(name_count),entry); // should we do this?
    }

    return entry;
  }  


  // LOGIC MANIPULATION METHODS
  
  public BooleanEquation or( Bool op ) {

    if (!isTrue()) {
      // is predicate in hash yes -- do the appropriate thing.
      // if no add to hash, give location.  Give BDDNode ???  
      // Add, hash by Operand.
      // there will be only one for each sense.

      HashEntry entry = getEntry(op);
      
      BDDNode new_node = entry.node;
      new_node.incRefCount();

      BDDNode new_eq = bdd.bdd_apply(_eq,new_node,BDD.OR);
      new_node.decRefCount();
      _eq.decRefCount();
      _eq = new_eq.incRefCount();
    }
    return this;
  }

  
  void nor( Bool op ) {
    if (!isTrue()) {
      // is predicate in hash yes -- do the appropriate thing.
      // if no add to hash, give location.  Give BDDNode ???  Add, hash by Operand.
      // there will be only one for each sense.

      HashEntry entry = getEntry(op);
      
      BDDNode new_node = entry.node;
      new_node = bdd.bdd_not(entry.node);
      new_node.incRefCount();

      BDDNode new_eq = bdd.bdd_apply(_eq,new_node,BDD.OR);
      new_node.decRefCount();
      _eq.decRefCount();
      _eq = new_eq.incRefCount();

    }
  }

  
  public BooleanEquation or( BooleanEquation eq ) {
    if( eq.isTrue() ) {
      setTrue();
    }
    else if( isTrue() ) {
      return this;
    }  else {
      BDDNode new_eq = bdd.bdd_apply(_eq,eq.getEq(),BDD.OR);
      _eq.decRefCount();
      _eq = new_eq.incRefCount();
    }

    return this;
  }

  public BooleanEquation xor( BooleanEquation eq ) {

    BDDNode new_eq = bdd.bdd_apply(_eq,eq.getEq(),BDD.XOR);
    _eq.decRefCount();
    _eq = new_eq.incRefCount();

    return this;
  }
  

  public BooleanEquation not() {
    if (this.isTrue()) {
      setFalse();
      return this;
    }

    if (this.isFalse()) {
      setTrue();
      return this;
    }

    BDDNode new_eq = bdd.bdd_not(_eq);
    _eq.decRefCount();
    _eq = new_eq.incRefCount();

    return this;
  }

  public BooleanEquation and( Bool op) {
    if ( isTrue() ) {
      // If the equation is true, the the result is the Predicate.
      setFalse();
      or(op);
    } else if (isFalse()) {
      return this;
    } else {
      HashEntry entry = getEntry(op);

      BDDNode new_node = entry.node;
      new_node.incRefCount();

      BDDNode new_eq = bdd.bdd_apply(_eq,new_node,BDD.AND);
      new_node.decRefCount();
      _eq.decRefCount();
      _eq = new_eq.incRefCount();
      
    }
    
    return this;
  }

  public BooleanEquation and( BooleanEquation eq) {
    if (eq.isTrue()) return this;

    // is eq or this false -- then we are false.
    if (eq.isFalse() || isFalse()) {
      setFalse();
      return this;
    }

    BDDNode new_eq = bdd.bdd_apply(_eq,eq.getEq(),BDD.AND);
    _eq.decRefCount();
    _eq = new_eq.incRefCount();

    return this;
  }

  /*
    Most code is poorly, poorly documented  -- ugh!

    restrict -- this is really for setting a series
    of boolean variables to some known value.  If you have
    an operand that you know is true, create an equation
    with the positive sense of that variable and restrict
    all other variables to that value.  For example

    eq = a | b
    new_eq = a # e.g. a is "true"
    ya_new_eq = eq.restrict(new_eq) => b

   */
  public void restrict(BooleanEquation eq) {
    //System.out.println("\n Restrict "+eq+" on "+this);
    BDDNode new_eq = bdd.bdd_restrict(_eq,eq.getEq());
    //System.out.println(" Result   "+new_eq+"\n");
    _eq.decRefCount();
    _eq = new_eq.incRefCount();
    //System.out.println(" Result   "+this+"\n");
  }


  BooleanEquation simplify(BooleanEquation d) {
    BDDNode new_eq = bdd.bdd_simplify(_eq,d.getEq());

    System.out.println(" input\n "+_eq);
    System.out.println(" domain\n "+d.getEq());
    System.out.println(" result\n "+new_eq);

    System.out.println(" input\n "+bdd.toDot(_eq));
    System.out.println(" domain\n "+bdd.toDot(d.getEq()));
    System.out.println(" result\n "+bdd.toDot(new_eq));

    new_eq.incRefCount();

    return new BooleanEquation(new_eq);
  }

//I (Kris) made this public.  If you disagree, you know where to find me:
 public LinkedList listBooleanOperands() {
    LinkedList result = new LinkedList();
    
    if (isTrue() || isFalse()) 
      return result;
    
    recurseMakeList(_eq, result);
    
    return result;
  }
    
  void recurseMakeList(BDDNode node, LinkedList list) {
    BDDNode high = node.getHigh();
    BDDNode low = node.getLow();

    HashEntry entry = (HashEntry)name_order.get(new Integer(bdd.getVar(node)));
    Bool o = entry.getOperand();

    if (!list.contains(o))
      list.add(o);

    if (!high.isConst()) 
      recurseMakeList(high,list);
    
    if (!low.isConst()) 
      recurseMakeList(low,list);
  }

  
  // UTILITY METHODS
  
  // this is the proper way to clone.
  public Object clone() {
    //System.out.println("CLONE!!!");
    BooleanEquation ret = new BooleanEquation();
    
    ret.setEq(_eq.incRefCount());
    
    return ret;
  }

  
  public static BooleanEquation combine( BooleanEquation eq1, 
					 BooleanEquation eq2 ) {
  
    BooleanEquation ret = new BooleanEquation();
    ret.or( (BooleanEquation)eq1.clone() );
    ret.or( (BooleanEquation)eq2.clone() );
    
    return ret;
  }

  
  public BooleanEquation setTrue() {
    _eq.decRefCount();
    _eq = BDDNode.ONE;
    return this;
  }

  public BooleanEquation setFalse() {
    _eq.decRefCount();
    _eq = BDDNode.ZERO;
    return this;
  }

  public boolean isEqual(BooleanEquation eq) {
    return _eq == eq.getEq();
  }

  public boolean isFalse() { 
    return (_eq == BDDNode.ZERO);
  }
  public boolean isTrue() { 
    return (_eq == BDDNode.ONE);
  }
  
  public String toString() {
    StringBuffer sbuf = new StringBuffer();

    if (isFalse()) 
      sbuf.append("false ");
    else if (isTrue()) 
      sbuf.append("true ");
    else 
      sbuf.append(getTwoLevel().toString());
    
    return sbuf.toString();
  }

  private int containsOperand(Bool o) {
    int result = -1;
    /*
    //System.out.println(" looking for "+o);
    
    ListIterator eq_iter = getTwoLevel().listIterator();
    while (eq_iter.hasNext()) {
      LinkedList term = (LinkedList)eq_iter.next();
      ListIterator term_iter = term.listIterator();
      
      int term_result = FALSE;
      
      while (term_iter.hasNext()) {
	Predicate p = (Predicate)term_iter.next();

	//System.out.print(" looking at p "+p);

	if (p.getPredicate() == o)  {
	  if (p.getTruth()) {
	    term_result |= POSITIVE;
	  } else
	    term_result |= NEGATIVE;
	  
	  //System.out.println("  result "+TRUTH[term_result]);

	  if (term_result == TRUE) return TRUE;
	} // else {
	  //System.out.println(" Operands not equal ... ");
	  // }
      }

      if (result == -1) 
	// first time through
	result = term_result;
      else if (term_result == FALSE) 
	// if we ever see a FALSE -- the whole thing is FALSE
	return FALSE;
      else 
	result |= term_result;

      if (result == TRUE) return TRUE;
   }
    */
    return result;
  }

  public String toDot() {
    return toString();
  }

  public String toDotBDD() {
    return bdd.toDot(_eq);
  }

  class HashEntry {
    BDDNode node;
    int number;
    Bool _op;

    HashEntry(BDDNode n, int num, Bool o) {
      node = n;
      number = num;
      _op = o;
    }

    Bool getOperand() { return _op; }
  }
    


  public static void main( String[] args ) {
    BooleanEquation bool = new BooleanEquation();
    BooleanEquation term1 = new BooleanEquation();
    BooleanEquation term2 = new BooleanEquation();
    BooleanEquation term3 = new BooleanEquation();
    
    Bool A = new BoolTest("A");
    Bool B = new BoolTest("B");
    Bool C = new BoolTest("C");
    Bool D = new BoolTest("D");
    
    System.out.println(" "+bool);

    term1.or(A);
    bool.or(NOT(B));
    bool.or(term1);
    bool.or(C);
    bool.or(D);
    System.out.println(" "+bool);
    term2.or(NOT(D)).and(A);
    //bool.or(NOT(D)).and(A);
    System.out.println(" "+term2);
    bool.or(term2);
    System.out.println(" "+bool);
    //System.out.println(" "+bool.toDotBDD());
    
    bool.setFalse();
    term1.setFalse().or(A).and(B);
    bool.or(term1);
    System.out.println(" "+term1+" : "+bool);
    term1.setFalse().or(B).and(C);
    bool.or(term1);
    System.out.println(" "+term1+" : "+bool);
    term1.setFalse().or(A).and(C);
    bool.or(term1);
    System.out.println(" "+term1+" : "+bool);
    System.out.println(" "+bool.toDotBDD());

  }
}





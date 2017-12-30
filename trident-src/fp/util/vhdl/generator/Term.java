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

package fp.util.vhdl.generator;

import java.util.*;

public class Term extends CheckObject implements VHDLout, Operation {

  // Terms should be fancier -- but not right now...
  LinkedList _factors = new LinkedList();

  static final Class[] _classes = { Factor.class, Primary.class };
  static final String[] _ops = { MULT, DIV, MOD, REM};
  
  Term(Object left, String op, Object right) {
    super(_classes, _ops, left, op, right);
  }
  
  Term(Object o) {
    this(o, null, null);
  }


  class MultTerm {
    // should this be an interface ??
    String _operator;
    Object _factor;

    MultTerm(String o, Object f) {
      _operator = o;
      _factor = f;
    }
  }


  protected void addTerm(Object o) {
    if(objectAllowed(o)) 
      _operands.add(new MultTerm(NOP, o));
  }

  protected void addTerm(String op, Object o) {
    if (objectAllowed(o) && operatorAllowed(op)) 
      _operands.add(new MultTerm(op, o));
    }


  public Term mult(Object term) { 
    addTerm(MULT, term); 
    return this;
  }

  public Term div(Object term) { 
    addTerm(DIV, term); 
    return this;
  }

  public Term mod(Object term) { 
    addTerm(MOD, term); 
    return this;
  }

  public Term rem(Object term) { 
    addTerm(REM, term); 
    return this;
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    for(ListIterator list_iter=_operands.listIterator(); list_iter.hasNext();) {
      MultTerm term = (MultTerm)list_iter.next();
      if (!NOP.equals(term._operator)) {
	s.append(" ").append(term._operator).append(" ");
      }
      ((VHDLout)term._factor).toVHDL(s, "");
    }
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }

  
  public static void main(String args[]) {
    // mult div mod rem

    Primary a = new SimpleName("a");
    Primary a1 = new SimpleName("a_1");
    Primary b = new SimpleName("b");
    Primary c = new SimpleName("c");

    System.out.println("Primaries "+a+" "+b+" "+c+"\n");

    Term t0 = new Term(a);
    print(t0);

    Term t1 = new Term(a, MULT, b);
    print(t1);
    t1.div(b);
    print(t1);

    Term t2 = new Term(new Paren(new Term(a, MULT, b)), DIV, 
		       new Paren(new Term(b, MULT, c)));
    print(t2);
    Term t3 = new Div(new Paren(new Mult(a,b)), 
		      new Paren(new Mult(c,a1)));
    print(t3);
    
    Term t4 = new Term(a, MOD, b).rem(c).mod(a).mod(b).rem(a).rem(c);
    print(t4);
    
  }

  public static void print(Term f) {
    System.out.println("Term: "+f);
  }



}

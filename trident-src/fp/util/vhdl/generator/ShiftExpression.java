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

public class ShiftExpression extends CheckObject implements VHDLout, Operation {


  Object _left;
  Object _right;
  String _operator;


  static final Class[]  _classes = { SimpleExpression.class, 
				     Term.class, Factor.class, 
				     Primary.class };

  static final String[] _ops = { SLL, SRL, SLA, SRA, ROL, ROR };


  public ShiftExpression(Object left, String op, Object right) {
    super(_classes, _ops, left, op, right);
  }

  public ShiftExpression(Object left) {
    this(left, null, null);
  }


  protected void addTerm(String op, Object term) { 
    if (objectAllowed(term) && operatorAllowed(op)) {
      _right = term;
      _operator = op;
    }
  }

  protected void addTerm(Object term) {
    if (objectAllowed(term)) 
      _left = term;
  }

  public ShiftExpression sll(Object term) { 
    addTerm(SLL, term); 
    return this;
  }

  public ShiftExpression srl(Object term) { 
    addTerm(SRL, term); 
    return this;
  }

  public ShiftExpression sla(Object term) { 
    addTerm(SLA, term);
    return this;
  }


  public ShiftExpression sra(Object term) { 
    addTerm(SRA, term); 
    return this;
  }

  public ShiftExpression rol(Object term) { 
    addTerm(ROL, term); 
    return this;
  }

  public ShiftExpression ror(Object term) { 
    addTerm(ROR, term);
    return this;
  }


  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    ((VHDLout)_left).toVHDL(s,"");
    if (_operator != null) {
      s.append(" ").append(_operator).append(" ");
    }
    if (_right != null) {
      ((VHDLout)_right).toVHDL(s, "");
    }

    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }


   public static void main(String args[]) {
    // mult div mod rem

    SimpleName a = new SimpleName("a");
    SimpleName a1 = new SimpleName("a_1");
    SimpleName b = new SimpleName("b");
    SimpleName c = new SimpleName("c");

    System.out.println("Primaries "+a+" "+b+" "+c+"\n");

    ShiftExpression se = new ShiftExpression(a, ADD, new Object());
    print(se);

    se = new ShiftExpression(a, ADD, new Paren(new Mult(a,b)));
    print(se);

    se = new ShiftExpression(a, ROR, new Paren(new Mult(a,b)));
    print(se);

    se = new ShiftExpression(new Paren(new Add(a,b)), SUB, c);
    print(se);

    se = new ShiftExpression(new Mult(a,b), SRL, new Paren(new Add(a,b,c)));
    print(se);

    se = new ShiftExpression(new Mult(a,b));
    print(se);

    //Term t3 = new Div(new Paren(new Mult(a,b)), 
    //new Paren(new Mult(c,a1)));

    
  }

  public static void print(ShiftExpression f) {
    System.out.println("ShiftExpression: "+f);
  }


  

}

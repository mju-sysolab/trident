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

public class Expression extends CheckObject implements VHDLout, Operation {
  
  String _operator = NOP;

  static final Class[]  _classes = { 
    /*Relation.class,
    ShiftExpression.class,
    SimpleExpression.class, 
    Term.class, Factor.class, 
    Primary.class,
    Name.class,
    Expression.class,
    Aggregate.class, */
    // the above array was not working out due to a loop 
    // in the static initializer ...
        VHDLout.class // is that dangerous?
  };

  public static final Expression ONE = new Expression(Char.ONE);
  public static final Expression ZERO = new Expression(Char.ZERO);


  static final String[] _ops = { AND, OR, XOR, NAND, NOR, XNOR };



  public Expression(Object left, String operator,
		    Object right) {
    super(_classes, _ops, left, operator, right);
    setOp(operator);
  }

  public Expression(Object left) {
    this(left, null, null);
  }

  public Expression(LinkedList objs, String operator) {
    super(_classes, _ops);
    setOp(operator);

    // what does this do for an empty list??
    // or what does AND(SINGLE_OP_MEAN?)
    if (objs.size() != 0) {
      for(ListIterator iter = objs.listIterator(); iter.hasNext(); ) {
	Object o = iter.next();
	addTerm(o);
      }
    } else {
      _operator = NOP;
      addTerm(new SimpleName("null"));
    }
  }
   
  // public ??
  public Expression(String operator) {
    super(_classes, _ops);
    setOp(operator);
  }
  
  

  void setOp(String op) {
    if (operatorAllowed(op)) 
      _operator = op;
  }

  protected void addTerm(String op, Object term) { 
    // throw exception -- we cannot change the op along the way.
    if (objectAllowed(term)) 
      if (_operands.size() == 1 ||
	  _operator.equals(op)) {
	setOp(op);
	_operands.add(term);
      }
  }
  
  protected void addTerm(Object term) {
    if (objectAllowed(term)) {
      if (_operator == NOR || _operator == NAND) {
	if (_operands.size() == 1)
	  _operands.add(term);
      } else {
	_operands.add(term);
      }
    }
  }
  

  public Expression and(Object o) {
    addTerm(AND, o);
    return this;
  }

  public Expression or(Object o) {
    addTerm(OR, o);
    return this;
  }

  public Expression xor(Object o) {
    addTerm(XOR, o);
    return this;
  }

  public Expression nand(Object o) {
    addTerm(NAND, o);
    return this; // ??
  }

  public Expression nor(Object o) {
    addTerm(NOR, o);
    return this;
  }

  public Expression xnor(Object o) {
    addTerm(XNOR, o);
    return this;
  }
  
  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    for(ListIterator iter = _operands.listIterator(); iter.hasNext(); ) {
      VHDLout r = (VHDLout)iter.next();
      r.toVHDL(s, "");
      if (iter.hasNext()) {
	s.append(" ").append(_operator).append(" ");
      }
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

    Expression se = new Expression(a, XOR, b);
    print(se);

    se = new Expression(a, AND, new Paren(new Mult(a,b)));
    print(se);
    se.and(c);
    print(se);

    se = new Expression(a, XNOR, new Paren(new Mult(a,b)));
    print(se);

    se = new And(new Paren(new Add(a,b)), c);
    print(se);

    se = new Or(new Paren(new ShiftExpression(new Mult(a,b))), 
		new Paren(new Add(a,b,c)));
    print(se);

    se = new Xor(a, b ,c);
    print(se);
    se = new Or(new Paren(new And(a,b)), 
		new Paren(new And(b,c)), 
		new Paren(new And(a,c)));
    print(se);

    //Term t3 = new Div(new Paren(new Mult(a,b)), 
    //new Paren(new Mult(c,a1)));

    
  }

  public static void print(Expression f) {
    System.out.println("Expression: "+f);
  }


 
}

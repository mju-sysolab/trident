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

public class Factor extends CheckObject implements VHDLout, Operation {

  Object _right;
  Object _left;
  
  String _operator = NOP;

  static final Class[]  _classes = { Primary.class };
  static final String[] _ops = { ABS, NOT, POW };
  
  public Factor(Object left, String op, Object right) {
    super(_classes, _ops, left, op, right);
    _operator = op;
  }

  public Factor(Object left, Object right) {
    this(right, POW, left);
  }

  public Factor(String op, Object right) {
    this(null, op, right);
    if (operatorAllowed(op)) {
      addTerm(op, right);
      // else an exeption ....
    } // else if you set op and it did not work exception ?
  }

  public Factor(Object left) {
    this(left, NOP , null);
  }

  protected void addTerm(String op, Object term) { 
    if (objectAllowed (term) && operatorAllowed(op)) {
      _operator = op;
      _right = term;
    }
  }

  protected void addTerm(Object term) {
    if (objectAllowed(term)) {
      _left = term;
    }
  }



  public StringBuffer toVHDL(StringBuffer s, String pre) {
    boolean nop = NOP.equals(_operator);
    s.append(pre);
    if (_left != null) {
      ((VHDLout)_left).toVHDL(s,"");
      if (!nop) s.append(" ");
    }
    if (!nop) {
      s.append(_operator).append(" ");
      ((VHDLout)_right).toVHDL(s, "");
    }
    return s;
  }

   
  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }
  
  public static void main(String args[]) {
    Primary a = new SimpleName("a");
    Primary a1 = new SimpleName("a_1");
    Primary b = new SimpleName("b");
    Primary c = new SimpleName("c");

    System.out.println("Primaries "+a+" "+b+" "+c+"\n");

    print(new Factor(a, POW, b));
    print(new Factor(ABS, b));
    print(new Factor(NOT, b));
    print(new Factor(c));
  
    print(new Not(c));
    print(new Abs(c));
  }

  public static void print(Factor f) {
    System.out.println("Factor: "+f);
  }

}

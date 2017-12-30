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

class BooleanEq implements VHDLout {
  
  // this only applies to std_ulogic and std_ulogic_vector
  // from the std_logic_1164 library.

  public static final String AND = "and";
  public static final String OR = "or";
  public static final String NOT = "not";
  public static final String XOR = "xor"; //?
  public static final String EQ = "=";

  
  LinkedList _bools;

  BooleanEq(Bus name) {
    _bools = new LinkedList();
    if (name != null) {
      _bools.add(new Bool(name));
    }

  }



  class Bool {
    Bus _s;
    String _op;
    boolean _not;
    
    Bool(Bus s, String op, boolean ed) {
      _s = s;
      _op = op;
      _not = ed;
    }

    Bool(Bus s) {
      this(s, null, false);
    }

    public String toString() {
      StringBuffer sbuf = new StringBuffer();
      if (_op != null) {
	sbuf.append(_op).append(" ");
      }
      if (_not) {
	sbuf.append(NOT).append(" ");
      }
      sbuf.append(_s.toString());
      return sbuf.toString();
    }

  }


  void eq(Bus name) {
    // should check size = 1
    _bools.add(new Bool(name, EQ, false));
  }

  void and(Bus name) {
    _bools.add(new Bool(name, AND, false));
  }

  void and_not(Bus name) {
    _bools.add(new Bool(name, AND, true));
  }
   
  void or(Bus name) {
    _bools.add(new Bool(name, OR, false));
  }
 
  void or_not(Bus name) {
    _bools.add(new Bool(name, OR, true));
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    for(ListIterator iter = _bools.listIterator(); iter.hasNext(); ) {
      s.append(iter.next().toString());
    }
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }
}

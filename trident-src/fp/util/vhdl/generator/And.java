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

public class And extends Expression {

  public And(Object a, Object b) {
    super(a, AND, b);
  }

  public And(Object a, Object b, Object c) {
    this(new Object[] {a, b, c} );
  }

  public And(Object a, Object b, Object c, Object d) {
    this(new Object[] {a, b, c, d} );
  }

  public And(LinkedList l) {
    super(AND);
    for(ListIterator iter = l.listIterator(); iter.hasNext(); ) {
      addTerm(iter.next());
    }
  }

  public And(Object[] o) {
    super(AND);
    for (int i=0; i < o.length; i++) {
      addTerm(o[i]);
    }
  }

}

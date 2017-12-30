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

public abstract class CheckObject {

  protected LinkedList _operands;

  Class[]  _classes;
  String[] _ops;

  CheckObject(Class[] classes, String[] ops,
	      Object left, String op, Object right) {
    _classes = classes;
    _ops = ops;
    _operands = new LinkedList();

    if (objectAllowed(left)) {
      addTerm(left);
      if (objectAllowed(right)) {
	// op should be tested too.
	if (operatorAllowed(op)) {
	  addTerm(op, right);
	  // else an exeption ....
	} else {
	  if (op != null)
	    System.out.println("CheckObject: Rejecting operator "+op);
	  
	}
	// else if you set op and it did not work exception ?
      } else {
	if (right != null)
	  System.out.println("CheckObject: Rejecting right object "+right);
      }
      
    } else {
      System.out.println("CheckObject: Rejecting left object "+left);
    }
  }

  CheckObject(Class[] classes, String[] ops) {
    _classes = classes;
    _ops = ops;
    _operands = new LinkedList();
  }

  abstract protected void addTerm(Object o);
  abstract protected void addTerm(String op, Object o);
  

  boolean operatorAllowed(String s) {
    if (s == null) return false;
    for (int i = 0; i < _ops.length; i++) {
      if (_ops[i].equals(s)) 
	return true;
    }
    return false;
  }

  boolean objectAllowed(Object o) {
    if (o == null) return false;
    for (int i = 0; i < _classes.length; i++) {
      if (_classes[i].isInstance(o)) {
	return true;
      }
    }
    return false;
  }


}

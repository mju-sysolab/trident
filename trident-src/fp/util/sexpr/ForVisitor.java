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


package fp.util.sexpr;

import java.util.*;

public class ForVisitor extends SExprVisitor {
  HashMap vars = new HashMap();


  public void forVector(Vector that) {
    //System.out.println("Have Vector "+that);
    
    //    for(int i=0; i < that.size(); i++) {
    if (that.size() > 0) {
      Object obj = that.elementAt(0);
      if (obj instanceof Symbol) {
	if (((Symbol)obj).toString().equals("for")) {
	  Symbol var = (Symbol)that.elementAt(1);
	  // check for "in" ?
	  // if this is a vector, then we should have a range option ...
	  Symbol in = (Symbol)that.elementAt(2);
	  Object o = that.elementAt(3);
	  if (!(o instanceof Vector)) {
	    // this should be a run-time exception.
	    System.err.println("Illegal for .. in argument. " + o);
	    System.exit(-1);
	  }
	  Vector span = (Vector)o;
	  int start = 4;

	  Vector new_vector = new Vector();
	  for(Iterator iter = span.iterator(); iter.hasNext();) {
	    // there could be a check here and an exception if
	    // I cannot make this into a string -- what about symbols?
	    Object thing = iter.next();
	    vars.put(var.toString(), thing.toString());
	    for(int i=start; i<that.size(); i++) {
	      Vector v = (Vector) that.elementAt(i);
	      CloneVisitor cv = new CloneVisitor(vars);
	      cv.visit(v);
	      new_vector.add(cv.getClone());
	    }
	  }
	  that.clear();
	  that.addAll(new_vector);
	  //System.out.println("new vector "+new_vector);
	}
      }
    }
  }

}

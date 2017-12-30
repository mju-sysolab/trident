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

public class IndexedName implements VHDLout, Name {

  private Name _prefix;
  private LinkedList _expressions;

  public IndexedName(Name name, Expression exp) {
    _prefix = name;
    _expressions = new LinkedList();
    _expressions.add(exp);
  }

  public void add(Expression exp) {
    _expressions.add(exp);
  }

  public String getName() { return _prefix.getName(); }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre).append(_prefix.getName()).append("(");
    int count = 0;
    for(ListIterator iter=_expressions.listIterator(); iter.hasNext();) {
      Expression exp = (Expression)iter.next();
      if (count !=0) s.append(", ");
      exp.toVHDL(s,"");
      count++;
    }
    s.append(")");
    return s;
  }

   
  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }
  
}

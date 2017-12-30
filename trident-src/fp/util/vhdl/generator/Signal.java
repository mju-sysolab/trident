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

public class Signal implements BlockItem, EntityItem, VHDLout {

  private LinkedList _identifiers;
  private SubType _type;
  private Identifier _kind;
  private Expression _expression;

  public Signal(LinkedList ids, SubType type, Identifier kind,
			   Expression exp) {
    if (ids != null) 
      _identifiers = ids;
    else 
      _identifiers = new LinkedList();
    _type = type;
    _kind = kind;
    _expression = exp;
  }

  public Signal(Identifier id, SubType type, Expression exp) {
    this(null, type, null, exp);
    _identifiers.add(id);
  }
			      

  public Signal(Identifier id, SubType type) {
    this(id, type, null);
  }

  public LinkedList getNames() {
    LinkedList names = new LinkedList();
    for(ListIterator list_iter = _identifiers.listIterator(); 
	list_iter.hasNext();) {
      Identifier id = (Identifier)list_iter.next();
      names.add(id.getId());
    }
    return names;
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre).append("signal ");
    int count = 0;
    for(ListIterator list_iter = _identifiers.listIterator(); list_iter.hasNext();) {
      Identifier id = (Identifier)list_iter.next();
      if (count != 0) 
	s.append(", ");
      id.toVHDL(s,"");
    }
    s.append(" : ");
    _type.toVHDL(s,"");
    if (_kind != null) {
      s.append(" ");
      _kind.toVHDL(s,"");
      s.append(" ");
    }
    if (_expression != null) {
      s.append(" := ");
      _expression.toVHDL(s,"");
    }
    s.append(";\n");
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }


}


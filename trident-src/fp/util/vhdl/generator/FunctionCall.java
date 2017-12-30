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

public class FunctionCall implements Primary, VHDLout {

  private Name _name;
  private AssociationList _list;
  private HashMap _hash;

  public FunctionCall(Name name, AssociationList list) {
    _name = name;
    if (list == null) {
      _list = new AssociationList(false);
    } else {
      _list = list;
      _list._wrap = false;
    }
  }

  public FunctionCall(Name name) {
    this(name, null);
  }

  public void add(Association a) {
    _list.add(a);
  }

  public void add(Name name) {
    _list.add(new ActualPart(name));
  }

  public void add(Expression exp) {
    _list.add(new ActualPart(exp));
  }

  public void add(ActualPart ap) {
    _list.add(ap);
  }
  
  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    ((VHDLout)_name).toVHDL(s,"");

    if (_list.size() > 0) {
      s.append("( ");
      _list.toVHDL(s,pre);
      s.append(" )");
    }
    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }



  

}

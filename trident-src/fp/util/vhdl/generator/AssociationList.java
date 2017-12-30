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

public class AssociationList extends LinkedList implements VHDLout {

  boolean _wrap = true;

  public AssociationList(boolean wrap) {
    super();
    _wrap = wrap;
  }

  public AssociationList() {
    this(true);
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    int count = 0;
    s.append(pre);
    for(ListIterator list_iter = listIterator(); list_iter.hasNext(); ) {
      VHDLout out = (VHDLout)list_iter.next();
      if (count != 0) {
	if (_wrap) 
	  s.append(",\n").append(pre);
	else
	  s.append(", ");
      }
      out.toVHDL(s,"");

      count++;
    }
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(),"").toString();
  }
  

}  





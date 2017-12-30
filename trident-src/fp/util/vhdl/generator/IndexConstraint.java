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

public class IndexConstraint implements Constraint, VHDLout {
  
  LinkedList _ranges;
  boolean is_ranges = true;

  public IndexConstraint(LinkedList ranges) {
    _ranges = ranges;
  }

  public IndexConstraint(SubType sub) {
    this(new LinkedList());
    _ranges.add(sub);
    is_ranges = false;
  }
    
  public IndexConstraint(Range sub) {
    this(new LinkedList());
    is_ranges = true;
    _ranges.add(sub);
  }


  public void add(SubType sub) {
    if (!is_ranges) 
      _ranges.add(sub);
  }

  public void add(Range range) {
    if (is_ranges)
      _ranges.add(range);
  }


  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);

    int count = 0;
    for(ListIterator list_iter = _ranges.listIterator(); list_iter.hasNext(); ) {
      VHDLout out = (VHDLout) list_iter.next();
      if (count == 0) {
	s.append("(");
      } else {
	s.append(", ");
      }
      out.toVHDL(s,"");
      count++;
    }
    s.append(")");
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(),"").toString();
  }



}  

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

public class ChoiceList extends LinkedList implements VHDLout {
  
  // This is not super safe, but more so

  public ChoiceList(Choice c) {
    super();
    if (c != null)
      add(c);
  }

  public ChoiceList() {
    this(null);
  }


  public boolean add(Object o) {
    if (o instanceof Choice) 
      return super.add(o);
    else {
      System.out.println("Attempt was made to add Non-choice to choice list");
      return false;
    }
  }

  
  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    int count = 0;
    for(ListIterator list_iter = listIterator(); list_iter.hasNext();) {
      if (count != 0) s.append(" | ");
	
      VHDLout vo = (VHDLout)list_iter.next();
      vo.toVHDL(s,"");
      count++;
    }
    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }




}  

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


package fp.synthesis;

import java.util.*;

public class MemInfoList extends ArrayList implements Text {
  
  private int  _next_index;

  public MemInfoList() {
    super();
    _next_index = 0;
  }

  public boolean add(MemInfo info) {
    info.setIndex(_next_index);
    _next_index++;
    return super.add(info);
  }

  public String toText(String prefix) {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(prefix);
    sbuf.append(START);
    sbuf.append("memory");
    sbuf.append(LINE);
      
    for(Iterator iter = iterator(); iter.hasNext(); ) {
      MemInfo reg = (MemInfo)iter.next();
      sbuf.append(reg.toText(prefix+TAB));
    }
    sbuf.append(prefix).append(END).append(LINE);
    return sbuf.toString();
  }

  public static void sort(List list) {
    class MemInfoCompare implements Comparator {
      public int compare(Object o1, Object o2) {
	if((o1 instanceof MemInfo) &&
	   (o2 instanceof MemInfo)) 
	  return (((MemInfo) o1).getName()
		  .compareToIgnoreCase(((MemInfo)o2).getName()));
	else
	  throw new ClassCastException("Not a MemInfo!");
      }
    }
    Collections.sort(list, new MemInfoCompare());
  }

}

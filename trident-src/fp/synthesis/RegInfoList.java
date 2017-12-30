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

public class RegInfoList extends ArrayList implements Text {
  
  
  private long _address_offset;
  private int  _address_start;
  private int  _address_next;
  private int  _next_index;

  public RegInfoList(long address_offset, int address_start, 
		     int address_next) {
    super();
    _address_offset = address_offset;
    _address_start = address_start;
    _address_next = address_next;
    _next_index = 0;
  }

  long getAddressOffset() { return _address_offset; }
  int getAddressStart() { return _address_start; }

  public boolean add(RegInfo info) {
    long simple = _address_start + _next_index;
    long addr = _address_offset + (_address_next * (simple));
    info.setIndex(_next_index);
    info.setAddr(addr);
    info.setAddrSimple(simple);
    _next_index++;
    return super.add(info);
  }

  public String toText(String prefix) {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(prefix);
    sbuf.append(START);
    sbuf.append("registers");
    sbuf.append(LINE);
      
    for(Iterator iter = iterator(); iter.hasNext(); ) {
      RegInfo reg = (RegInfo)iter.next();
      sbuf.append(reg.toText(prefix+TAB));
    }
    sbuf.append(prefix).append(END).append(LINE);
    return sbuf.toString();
  }

  public static void sort(List list) {
    class RegInfoCompare implements Comparator {
      public int compare(Object o1, Object o2) {
	if((o1 instanceof RegInfo) &&
	   (o2 instanceof RegInfo)) 
	  return (((RegInfo) o1).getName()
		  .compareToIgnoreCase(((RegInfo)o2).getName()));
	else
	  throw new ClassCastException("Not a RegInfo!");
      }
    }
    Collections.sort(list, new RegInfoCompare());
  }

}

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

public class MemInfo {
  
  private String _name;
  private int    _width;
  private int    _depth;
  private long   _addr;
  private long   _addr_simple;
  private long   _index;
  private int    _bank;

  public static final String START = "(";
  public static final String END = ")";
  public static final String LINE = "\n";
  public static final String TAB = "\t";

  MemInfo(String name, int width, int depth) {
    _name = name; 
    _width = width;
    _depth = depth;
    _addr = -1;
    _index = -1;
    _addr_simple = -1;
    _bank = -1;
  }

  MemInfo(String name, int width) {
    this(name, width, 1);
  }

  public String getName() {  return _name; }
  public void setName(String name) { _name = name; }

  public int getWidth() { return _width; }
  public void setWidth(int width) { _width = width; }

  public int getDepth() { return _depth; }
  public void setDepth(int depth) { _depth = depth; }
  
  public long getAddr() {  return _addr; }
  public String getAddrHexStr() {  return "0x"+Long.toHexString(_addr); }
  public void setAddr(long addr) {  _addr = addr; }

  // this is the address without the external offset.
  public void setAddrSimple(long addr) { _addr_simple = addr; }
  public long getAddrSimple() { return _addr_simple; }

  public long getIndex() { return _index; }
  public void setIndex(long l) { _index = l; }
  
  public String toString() {
    return "[ "+_name+", "+_index+", "+_width+" ]";
  }
  
  public String toText(String prefix) {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(prefix);
    sbuf.append(START);
    sbuf.append("array");
    sbuf.append(" ").append(_name).append(LINE);

    addTextInt(sbuf, prefix, "width", _width);
    addTextInt(sbuf, prefix, "depth", _depth);
    addTextStr(sbuf, prefix, "address", getAddrHexStr());
    addTextLong(sbuf, prefix, "index", _index);

    sbuf.append(prefix).append(END);
    sbuf.append(LINE);

    return sbuf.toString();
  }

  void addTextLong(StringBuffer sbuf, String prefix, String name, 
		     long value) {
    sbuf.append(prefix).append(TAB);
    sbuf.append(START).append(name);
    sbuf.append(" ").append(value).append(END);
    sbuf.append(LINE);
  }

  void addTextInt(StringBuffer sbuf, String prefix, String name, 
		     int value) {
    sbuf.append(prefix).append(TAB);
    sbuf.append(START).append(name);
    sbuf.append(" ").append(value).append(END);
    sbuf.append(LINE);
  }

  void addTextStr(StringBuffer sbuf, String prefix, String name, 
		     String value) {
    sbuf.append(prefix).append(TAB);
    sbuf.append(START).append(name);
    sbuf.append(" ").append(value).append(END);
    sbuf.append(LINE);
  }
      
}

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

abstract class VHDLBase implements VHDLout {

  protected String _name;
  protected LinkedList _header;
  protected LinkedList _footer;

  VHDLBase(String name) {
    _name = name;
    _header = new LinkedList();
    _footer = new LinkedList();
  }

  VHDLBase() {
    this(null);
  }

  void setName(String n) { _name = n; }
  public String getName() { return _name; }

  LinkedList getHeader() { return _header; }
  void addHeader(VHDLout v) { _header.add(v); }

  public void addHeaderComment(String c) {
    addHeader(new Comment(c));
  }

  LinkedList getFooter() { return _footer; }
  void addFooter(VHDLout v) { _footer.add(v); }

  public void addFooterComment(String c) {
    addFooter(new Comment(c));
  }


  protected void appendHeader(StringBuffer sbuf, String pre) {
    for(ListIterator iter=_header.listIterator();iter.hasNext();) {
      ((VHDLout)iter.next()).toVHDL(sbuf, pre);
    }
    sbuf.append("\n");
  } 

  protected void appendFooter(StringBuffer sbuf, String pre) {
    sbuf.append("\n");
    for(ListIterator iter=_footer.listIterator();iter.hasNext();) {
      ((VHDLout)iter.next()).toVHDL(sbuf, pre);
    }
  }

  abstract protected void appendBody(StringBuffer sbuf, String tabs);

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    appendHeader(s, pre);
    appendBody(s, pre);
    appendFooter(s, pre);
    return s;
  }


  public String toString() {
    StringBuffer sbuf = new StringBuffer();

    return toVHDL(sbuf,"").toString();
  }


  public static void main(String args[]) {

  }
}




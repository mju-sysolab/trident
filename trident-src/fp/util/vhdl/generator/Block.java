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

abstract public class Block extends VHDLBase implements VHDLout {

  private String _type_name;

  Block(String name, String type_name) {
    super(name);
    _type_name = type_name;
  }

  void setType(String type) { _type_name = type; }
  public String getType() { return _type_name; }

  protected void appendName(StringBuffer sbuf, String pre) { 
    sbuf.append(pre).append(getType()).append(" ");
    sbuf.append(getName()).append("\n");
  }

  protected void appendHeader(StringBuffer sbuf, String pre) {
    super.appendHeader(sbuf, pre);
    appendName(sbuf, pre);
  }

  protected void appendFooter(StringBuffer sbuf, String pre) {
    appendEnd(sbuf, pre);
    super.appendFooter(sbuf, pre);
  }

  /*
  abstract protected void appendBody(StringBuffer sbuf, String tabs);
    sbuf.append(tabs).append("\n");
  }
  */

  protected void appendEnd(StringBuffer sbuf, String pre) {
    sbuf.append(pre).append("end ").append(getName()).append(";\n");
  }




}

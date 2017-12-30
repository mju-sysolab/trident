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


package fp.flowgraph;

public class Variable {

  private Operand _operand;
  private Type _type;
  private boolean _external;
  

  int read_count;
  int write_count;

  public Variable(Operand op, Type ty, boolean external) {
    _operand = op;
    _type = ty;
    _external = external;
  }
  
  
  public Operand getOperand() { return _operand; }

  public Type getType() { return _type; }

  public boolean isExternal() { return _external; }


  public int getReadCount() { return read_count; }
  public void setReadCount(int i) { read_count = i; }

  public int getWriteCount() { return write_count; } 
  public void setWriteCount(int i) { write_count = i; }


  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append(_type.toString());
    buf.append(" ");
    buf.append(_operand.toString());
    return buf.toString();
  }

}
  

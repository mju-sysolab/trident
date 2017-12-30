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

public final class LongConstantOperand extends ConstantOperand {
  
  private long _value;

  public LongConstantOperand(long value) {
    super("const"+value);
    _value = value;
  }

  public Operand copy() { 
    return new LongConstantOperand(_value);
  }

  public Operand getNext() {
    return copy();
  }

  public long getValue() { return _value; }

  public boolean isIntegerOperand() {
    return true;
  }

}

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

public final class MemoryOperand extends Operand {
  
  int size;

  MemoryOperand(String name, int size) {
    _name = name;
    this.size = size;
    _assignment = NOT_ASSIGNED;
  }

  public Operand copy() { 
    return new MemoryOperand(_name, size);
  }

  public Operand getNext() {
    return Operand.nextMemory(_name);
  }
}

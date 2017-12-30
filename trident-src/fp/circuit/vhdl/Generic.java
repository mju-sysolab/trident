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


package fp.circuit.vhdl;

import fp.util.vhdl.generator.Expression;
import fp.util.vhdl.generator.InterfaceConstant;
import fp.util.vhdl.generator.Primary;
import fp.util.vhdl.generator.SimpleName;
import fp.util.vhdl.generator.SubType;

public class Generic {
  public Generic(String s) {
    name = new SimpleName(s);
  }

  SubType type;
  Primary value;
  SimpleName name;
  
  InterfaceConstant getInterfaceConstant() {
    return new InterfaceConstant(name, type, new Expression(value));
  }

}

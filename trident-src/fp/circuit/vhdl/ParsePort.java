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

import fp.circuit.PortTag;

import fp.util.vhdl.generator.IndexConstraint;
import fp.util.vhdl.generator.InterfaceSignal;
import fp.util.vhdl.generator.Mode;
import fp.util.vhdl.generator.Primary;
import fp.util.vhdl.generator.SimpleName;
import fp.util.vhdl.generator.SubType;

public class ParsePort {
  public ParsePort(String s) {
    if ("in".equals(s)) {
      mode = Mode.IN;
    } else if ("out".equals(s)) {
      mode = Mode.OUT;
    } else if ("inout".equals(s)) {
      mode = Mode.INOUT;
    } else {
      System.err.println("Unparsed Mode "+s);
    }
  }

  Mode mode;
  // just the basic type...
  SubType type;
  
  Primary value;

  private SimpleName name;
  private String _name;

  String tag;
  String open;
  IndexConstraint size;
  // this is for me.
  int width;
  
  InterfaceSignal getInterfaceSignal() {
    // this is more complicated ...
    if (size != null) {
      if (type == SubType.STD_LOGIC) {
	type = new SubType(new SimpleName("std_logic_vector"),
			   size);
      } else {
	System.err.println("Ach -- not the right type errror.");
	System.exit(-1);
      }
    }
    return new InterfaceSignal(name, mode, type);
  }

  void setName(String new_name) {
    _name = new_name;
    name = new SimpleName(new_name);
  }

  public String getName() { return _name; }
  public SimpleName getSimpleName() { return name; }

  public int getDirection() { 
    if (mode == Mode.IN) {
      return PortTag.IN;
    } else if (mode == Mode.OUT) {
      return PortTag.OUT;
    } else if (mode == Mode.INOUT) {
      return PortTag.INOUT;
    }
    return 0;
  }

  public int getWidth() { return width; }

}

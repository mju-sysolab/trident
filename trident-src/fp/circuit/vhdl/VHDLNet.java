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

import java.util.Iterator;

import fp.circuit.Net;
import fp.circuit.Circuit;


import fp.util.vhdl.generator.*;

public class VHDLNet extends Net {
  
  Bus _bus;
  SimpleName _name;

  VHDLNet(Circuit parent, String name) {
    super(parent, name);
  }


  public void build(String name, Object[] arg_o) {
    //System.out.println("Building VHDLNet "+getName());

    DesignUnit du = ((VHDLCircuit)getParent()).getVHDLParent();

    LibraryUnit lu = du.getLibraryUnit();
    //Entity e = lu.getEntity();

    Architecture a = lu.getArchitecture();
    // this is assuming the widths are correct in the wires.
    // the width = 0 thing is a kludge...
    if (getWidth() == 1
	|| getWidth() == 0) {
      a.addItem(new Signal(getVHDLName(), SubType.STD_LOGIC));
    } else {
      a.addItem(new Signal(getVHDLName(), SubType.STD_LOGIC_VECTOR(getWidth() - 1, 0)));
    }
  }

  public Bus getBus() {
    if (_bus == null) {
      if (getWidth() == 1
	  || getWidth() == 0) {
	_bus = new Bus(getName());
      } else {
	_bus = new Bus(getName(), getWidth() - 1, 0);
      }
    }
    return _bus;
  }
    
  public SimpleName  getVHDLName() {
    if (_name == null) {
      // this is probably expensive
      String name = getName();
      if (name.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
	if (name.indexOf("__") < 0) {
	  _name = new SimpleName(name);
	} else {
	  _name = new SimpleName('\\'+name+'\\');
	}
      } else {
	_name = new SimpleName('\\'+name+'\\');
      }
    }
    return _name;
  }

}

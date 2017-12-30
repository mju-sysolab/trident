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

import java.util.HashSet;
import java.util.Iterator;

import fp.circuit.Circuit;
import fp.circuit.PortTag;

import fp.util.vhdl.generator.*;



public class VHDLPort extends fp.circuit.Port {

  InterfaceSignal _vhdl_port;
  Identifier _identifier;

  VHDLPort(Circuit parent, String name, int width, int direction) {
    super(parent, name, width, direction);
  }


  InterfaceDeclaration getVHDLPort() {
    if (_vhdl_port == null) {
      Mode m;
      int dir = getDirection();
      if (dir == PortTag.IN) 
	m = Mode.IN;
      else if (dir == PortTag.OUT) 
	m = Mode.OUT;
      else if (dir == PortTag.INOUT) 
	m = Mode.INOUT;
      else 
	m = Mode.UNDEF;
      
      SubType t;
      int width = getWidth();
      if (width == 1) 
	t = SubType.STD_LOGIC;
      else
	t = SubType.STD_LOGIC_VECTOR(width - 1, 0);
      
      //_vhdl_port = new Port(getName(), m, t);
      _vhdl_port = new InterfaceSignal(getIdentifier(), m, t);
    }
    return _vhdl_port;
  }


  Identifier getIdentifier() {
    if (_identifier == null) {
      String name = getRefName();
      if (name.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
	if (name.indexOf("__") < 0) {
	  _identifier = new Identifier(name);
	} else {
	  _identifier = new Identifier('\\'+name+'\\');
	}
      } else {
	_identifier = new Identifier('\\'+name+'\\');
      }
    } 
      
    return _identifier;
  }


  public void checkInternalNamePortName(Architecture a) {
    int dir = getDirection();

    String port_name = getRefName();

    if (dir == PortTag.IN) {
      HashSet ports = getOutPorts();
      for(Iterator iter = ports.iterator(); iter.hasNext(); ) {
	PortTag pi = (PortTag) iter.next();
	VHDLNet net = (VHDLNet)pi.getNet();
	// if the net is not equal to the port...
	if (!port_name.equals(net.getName())) {
	  ConditionalSignalAssignment csa = 
	    new ConditionalSignalAssignment(net.getVHDLName());
	  a.addStatement(csa);
	  csa.addCondition(new Waveform(new SimpleName(getIdentifier())), 
			   null);
	}
      }
      
    } else if (dir == PortTag.OUT) {
      HashSet ports = getInPorts();
      for(Iterator iter = ports.iterator(); iter.hasNext(); ) {
	PortTag pi = (PortTag) iter.next();
	VHDLNet net = (VHDLNet)pi.getNet();
	// if the net is not equal to the port...
	if (!port_name.equals(net.getName())) {
	  ConditionalSignalAssignment csa = 
	    new ConditionalSignalAssignment(new SimpleName(getIdentifier()));

	  a.addStatement(csa);
	  csa.addCondition(new Waveform(net.getVHDLName()),null);
	}
      }
    } else {
      System.out.println("VHDLPort : cannot handle port direction.");
      System.exit(-1);
    }
  }    

  public void build(String name, Object[] arg_o) {
    //System.out.println("Building VHDLPort "+getRefName());

    /*
      VHDL is interesting.  I may need to collect some information for
      repetive use or something.
    */

    DesignUnit du = ((VHDLCircuit)getParent()).getVHDLParent();

    LibraryUnit lu = du.getLibraryUnit();
    Entity e = lu.getEntity();

    Architecture a = lu.getArchitecture();
    
    e.addPort(getVHDLPort());
    
    // Okay, if my output wires is not the same as my name.
    // then I need to buffer the result.
    // or alias the result, but in FPGAs the result is the same.

    checkInternalNamePortName(a);
    


    /*
      The portinfos on this port imply connections or mappings that will need
      to occur in the architecture section of probably my parent ... 
    */


  }
 
}

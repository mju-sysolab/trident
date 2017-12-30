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


public class Component implements BlockItem, VHDLout {
  private Identifier _id;
  private GenericList _generics;
  private PortList _ports;

  public Component(Identifier id, GenericList generics, PortList ports) {
    _id = id;
    if (generics != null) 
      _generics = generics;
    else
      _generics = new GenericList();
    
    if (ports != null) 
      _ports = ports;
    else
      _ports = new PortList();

  }

  
  public Component(Identifier id, PortList ports) {
    this(id, null, ports);
  }

  public Component(Identifier id) {
    this(id, null, null);
  }


  public Identifier getId() { return _id; }

  public void addPort(InterfaceDeclaration p) { _ports.add(p); }
  PortList getPorts() { return _ports; }

  public void addGeneric(InterfaceDeclaration g) { _generics.add(g); }
  GenericList getGenerics() { return _generics; }

  public LinkedList getNames() {
    LinkedList names = new LinkedList();
    names.add(_id.getId());
    return names;
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    // these extra carriage returns may not bee the best idea...
    s.append("\n");
    s.append(pre).append("component ");
    _id.toVHDL(s,"");
    s.append(" is\n");
    if (_generics.size() > 0) {
      _generics.toVHDL(s,pre+TAB);
    }
    if (_ports.size() > 0) {
      _ports.toVHDL(s,pre+TAB);
    }
    s.append(pre).append("end component ");
    _id.toVHDL(s,"");
    s.append(";\n");
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(),"").toString();
  }

}

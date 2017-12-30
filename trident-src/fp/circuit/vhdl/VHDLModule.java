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

import java.util.*;
import fp.circuit.Net;
import fp.circuit.Circuit;
import fp.circuit.Operator;
import fp.circuit.PortTag;

import fp.util.vhdl.generator.*;

public class VHDLModule {
  
  /* data that needs to be known:
   what library
   what ports map to what ports
   outs -> VHDL outs -- I don't think direction is important
   ins -> VHDL ins
   what generics map to what -- ??
   needs clock ?
   what is the library name
   what is the VHDL name

   I need one more thing -- the pre-declaration of the VHDL ...
   I could :
     1) put in a big chunk of text that is "correct"
     2) record all the necessary bits of data and build it (ugh).
       vhdl name,
       port
         direction
	 name
	 size
       generic
         name
	 type
	 value
   */
      

  private String _library;

  private HashMap _port_map;
  private HashMap _gen_map;
  private HashMap _open_map;
  private HashMap _tag_map;

  private String _lib_name;
  private String _vhdl_name;
  private String _include;
  private Component _component;

  VHDLModule(String libName, String vhdlName, String library, 
	     String vhdlinclude, 
	     HashMap map, HashMap gmap, HashMap omap, HashMap tmap,
	     Component comp) {
    _library = library;
    
    _port_map = map;
    _gen_map = gmap;
    _open_map = omap;
    _tag_map = tmap;

    _lib_name = libName;
    _vhdl_name = vhdlName;
    _include = vhdlinclude;
    _component = comp;
  }
  
  VHDLModule(String libName, String vhdlName, String library,
	     String vhdlinclude, Component comp) {
    this(libName, vhdlName, library, vhdlinclude, 
	 new HashMap(), new HashMap(), new HashMap(), new HashMap(),
	 comp);
  }

  void setInclude(String inc) { _include = inc; }
  
  Component getComponent() { return _component; }

  HashMap getPortMap() { return _port_map; }
  void setPortMap(HashMap h) { _port_map = h; }  
  HashMap getGenericMap() { return _gen_map; }
  void setGenericMap(HashMap h) { _gen_map = h; }  
  HashMap getOpenMap() { return _open_map; }
  HashMap getTagMap() { return _tag_map; }

  public void build(String name, DesignUnit du, VHDLOperator op) {
    
    //System.out.println("Building VHDLModule "+op.getRefName());

    // we should be protected in the lower code...
    du.addLibrary(_library);
    du.addUse(new Use(_include));

    LibraryUnit lu = du.getLibraryUnit();
    // don't need it.
    //Entity e = lu.getEntity();

    // need to get in library
    Architecture a = lu.getArchitecture();
    // this assumes it was built already.
    // what makes this so it does not add itself as a component
    // more than once? -- need a hash?
    // I could search the component list
    // a.getItems() , find components and see if there is a match ...
    a.addItem(_component);
    
    Instance inst = new Instance(new SimpleName(op.getRefName()), _component);

    //System.out.println(" gmap "+_gen_map);
    for (Iterator iter=_gen_map.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();

      // this can be Number or String, but we don't care
      String value = entry.getValue().toString();
      String generic = entry.getKey().toString();
      
      // this is cheating
      inst.addGenericMap(new SimpleName(generic), new SimpleName(value));
    }

    PortTag tag = null;
    //System.out.println(" map "+_port_map);

    for (Iterator iter=_port_map.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();

      // this can be Number or String, but we don't care
      String libport = entry.getValue().toString();
      String modport = entry.getKey().toString();
      
      //System.out.println(" in_ports "+op.getInPorts());
      
      tag = op.findInPort(libport);
      if (tag != null) {
	VHDLNet net = (VHDLNet)tag.getNet();
	inst.addPortMap(new SimpleName(modport), net.getVHDLName());
	continue;
      } 
      
      //System.out.println(" out_ports "+op.getOutPorts());

      tag = op.findOutPort(libport);
      if (tag != null) {
	VHDLNet net = (VHDLNet)tag.getNet();
	inst.addPortMap(new SimpleName(modport), net.getVHDLName());
	continue;
      } 

      //System.out.println(" neither in nor out :"+modport+": :"+libport+":");

      if ("clk".equals(modport)) {
	System.out.println("JEFF I NEED A CLOCK!");
	inst.addPortMap(new SimpleName(modport), new SimpleName(libport));
      }
      
      // 0 and 1 should be low and high or something ...
      //if (libport.startsWith("%")) {
      // % does not happen anymore
      if ("open".equals(libport)) {
	inst.addPortMap(new SimpleName(modport), ActualPart.OPEN); 
      } else if ("0".equals(libport)) {
	//(others => '0') or '0'
	// Need to know size!!! -- cheating!
	inst.addPortMap(new SimpleName(modport), Char.ZERO);
      } else if ("1".equals(libport)) {
	inst.addPortMap(new SimpleName(modport), Char.ONE);
      }
      
    }

    a.addStatement(inst);
    

    
  }

  /*

  private String _library;
  private HashMap _port_map;
  private HashMap _gen_map;
  private String _lib_name;
  private String _vhdl_name;
  private String _include;
  private Component _component;

  */


  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("Library ").append(_library);
    sbuf.append("\n");
    sbuf.append(" Name (in library) ").append(_lib_name).append("\n");
    sbuf.append(" Include ").append(_include).append("\n");
    sbuf.append(" our name ").append(_vhdl_name).append("\n");
    sbuf.append(" port_map ").append(_port_map).append("\n");
    sbuf.append(" gen_map ").append(_gen_map).append("\n");
    // could use vhdlOut ...
    sbuf.append("Component ").append(_component).append("\n");
    return sbuf.toString();
  }

}

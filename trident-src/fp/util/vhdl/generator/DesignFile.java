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

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;

import java.text.DateFormat;
                                                                                      

public class DesignFile extends VHDLBase {
  private LinkedList _design_units;

  // I need a print writer thing and such to produce the file

  private String _file_name;
  private boolean _top_down = false;

  public DesignFile(String name, String file_name) {
    super(name);
    _design_units = new LinkedList();
    _header.add(new Comment(" This file was generated automatically."));
    _header.add(new Comment(" VHDL built by VHDLGen version 0.12"));
    _header.add(new Comment(" written by Justin L. Tripp"));
    _header.add(new Comment(" Copyright (c) 2005"));
    _header.add(new Comment(""));
    _header.add(new Comment(" ---------------------------------------- "));
    _header.add(new Comment(" FileName           :"+file_name));
    _header.add(new Comment(" ModelName          :"+name));
    _header.add(new Comment(" Title              :"));
    _header.add(new Comment(" Purpose            :"));
    _header.add(new Comment(" Author(s)          :"));
    _header.add(new Comment(" Comment            :"));
    _header.add(new Comment(" ---------------------------------------- "));
    _header.add(new Comment(" Version  | Author  | Date     | Changes  "));
    _header.add(new Comment(" ---------------------------------------- "));
    _header.add(new Comment(" 1.0      | VHDLgen |          | initial  "));
    _header.add(new Comment(" ---------------------------------------- "));

    _header.add(new Comment(""));
		

  }

  public DesignFile(String name) {
    this(name, "");
  }

  public DesignFile() {
    this("", "");
  }

  
  public void setTopDown(boolean b) { _top_down = b; }
  public boolean getTopDown() { return _top_down; }

  LinkedList getDesignUnits() { return _design_units; }
  public void addDesignUnit(DesignUnit du) { _design_units.add(du); }


  public String toVHDL() {
    StringBuffer s = new StringBuffer();

    return toVHDL(s).toString();
  }


  StringBuffer toVHDL(StringBuffer sbuf) {
    // iterate on design units
    String date = (new Date()).toString();
    _header.add(new Comment(" Generated on "+(date)));

    appendHeader(sbuf,"");
    appendBody(sbuf,"");

    return sbuf;
  }
  

  protected void appendBody(StringBuffer s, String pre) {
    // we have to go backwards, since we describe top-down
    // and tools like too see bottom-up.
    // okay -- eithway now.

    if (_top_down) {
      for(ListIterator iter=_design_units.listIterator(); iter.hasNext();) {
	VHDLout du = (VHDLout)iter.next();
	du.toVHDL(s,pre);
      }
    } else {
      int size = _design_units.size();
      for(ListIterator iter=_design_units.listIterator(size); 
	  iter.hasPrevious(); ) {
	DesignUnit du = (DesignUnit)iter.previous();
	du.toVHDL(s, pre);
      }
    }
  }


  public static void write(DesignFile df, String file_name) {
    PrintWriter out = getPrintWriter(file_name);
    System.out.println("Writing DesignFile to "+file_name);
    out.println(df.toVHDL());
    out.close();
  }
  
  
  public static PrintWriter getPrintWriter(String fileName) {
    Writer ostream = null ;
    try {
      ostream = new java.io.FileWriter(new File(fileName) ) ;
    }
    catch (java.io.IOException e) {
      e.printStackTrace();
      return null ;
    }
    return new PrintWriter(ostream);
  }


  public static void main(String args[]) {
    DesignFile df = new DesignFile();
    DesignUnit du = new DesignUnit("Sam");
    
    df.addDesignUnit(du);
    
    du.addLibrary("ieee");
    du.addUse(new Use("ieee.std_logic_1164.all"));
    du.addUse(new Use("ieee.std_logic_unsigned.all"));
    du.addUse(new Use("ieee.std_logic_arith.all"));

    LibraryUnit lu = du.getLibraryUnit();
    Entity e = lu.getEntity();
    
    e.addPort(new InterfaceSignal(new SimpleName("stop"), 
				  Mode.OUT, SubType.STD_LOGIC));
    e.addPort(new InterfaceSignal(new SimpleName("AddressStop"),
				  Mode.IN, SubType.STD_LOGIC));
    e.addPort(new InterfaceSignal(new SimpleName("AddressSelect"),
				  Mode.IN, SubType.STD_LOGIC_VECTOR(3,0)));

    Architecture arch = lu.getArchitecture();

    SimpleName a = new SimpleName("a");
    SimpleName b = new SimpleName("b");
    SimpleName c = new SimpleName("c");
    SimpleName d = new SimpleName("d");
    SimpleName clk = new SimpleName("clk");

    SimpleName cell_1 = new SimpleName("cell_1");
    SimpleName cell_2 = new SimpleName("cell_2");

    arch.addItem(new Signal(a, SubType.STD_LOGIC));
    arch.addItem(new Signal(b, SubType.STD_LOGIC));
    arch.addItem(new Signal(c, SubType.STD_LOGIC_VECTOR(3,0)));

    Component c1 = new Component(cell_1);
    c1.addPort(new InterfaceSignal(clk, Mode.IN, SubType.STD_LOGIC));
    c1.addPort(new InterfaceSignal(a, Mode.IN, SubType.STD_LOGIC));
    c1.addPort(new InterfaceSignal(b, Mode.IN, SubType.STD_LOGIC));
    c1.addPort(new InterfaceSignal(c, Mode.OUT, SubType.STD_LOGIC_VECTOR(3,0)));
 
    Component c2 = new Component(cell_2);
    c2.addPort(new InterfaceSignal(clk, Mode.IN, SubType.STD_LOGIC));
    c2.addPort(new InterfaceSignal(c, Mode.IN, SubType.STD_LOGIC));
    c2.addPort(new InterfaceSignal(b, Mode.IN, SubType.STD_LOGIC));
    c2.addPort(new InterfaceSignal(d, Mode.OUT, SubType.STD_LOGIC_VECTOR(3,0)));

    arch.addItem(c1);
    arch.addItem(c2);

    Instance i1 = new Instance(new SimpleName("bob_1"), c1);
    
    i1.addPortMap(new Association(clk,clk));
    i1.addPortMap(new Association(new SimpleName("t1"), a));
    i1.addPortMap(new Association(new SimpleName("t2"), b));
    i1.addPortMap(new SimpleName("l"), new SliceName(c, 3, 0));
    

    arch.addStatement(i1);

    DesignFile.write(df,"test.vhd");
  }




}

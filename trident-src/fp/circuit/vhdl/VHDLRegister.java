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
import java.util.LinkedList;
import java.math.BigInteger;

import fp.circuit.Circuit;
import fp.circuit.Operator;
import fp.circuit.PortTag;
import fp.circuit.Register;

import fp.util.vhdl.generator.*;

public class VHDLRegister extends Register {

  VHDLRegister(Circuit parent, String name, int width, String value) {
    super(parent, name, width, value);
    //System.out.println("Register : init "+value+"("+width+")");
  }


  
  public void build(String name, Object[] arg_o) {
    //System.out.println("Building VHDLRegister");
    //System.out.println("Register : build "+getValue()+"("+getWidth()+")");
    
    VHDLCircuit parent = (VHDLCircuit)getParent();

    DesignUnit du = parent.getVHDLParent();
    LibraryUnit lu = du.getLibraryUnit();
    //Entity e = lu.getEntity();
    Architecture a = lu.getArchitecture();
    
    // need to get the name of the out edge.
    PortTag out = (PortTag)getOutPorts().iterator().next();
    VHDLNet net = (VHDLNet)out.getNet();

    // may not need name.
    //ProcessStatement p = new ProcessStatement(new SimpleName(getName()));
    // name dangerous -- register is reserved word.
    ProcessStatement p = new ProcessStatement();
    // add it early, does not matter much.
    a.addStatement(p);

    // must handle preset value at some point.
    
    // can we guarantee this?
    //SimpleName clk = new SimpleName(clk);
    VHDLNet clk = (VHDLNet)parent.findNet("clk");
    if (clk != null) {
      p.addSignal(clk.getVHDLName());
    } else {
      System.out.println("Flip-Flop with NO CLOCK!!!");
    }

    VHDLNet reset = (VHDLNet)parent.findNet("reset");
    if (reset != null) {
      p.addSignal(reset.getVHDLName());
    } else {
      System.out.println("Flip-Flop with no reset.");
    }


    Iterator iter = getInPorts().iterator();
    VHDLNet we = null;
    VHDLNet in = null;
    PortTag in_port = (PortTag)iter.next();

    // not good.... fix me -- not too safe either.
    if ("we".equals(in_port.getName())) {
      we  = (VHDLNet)in_port.getNet();
    } else {
      in = (VHDLNet)in_port.getNet();
    }

    if (iter.hasNext()) {
      in_port = (PortTag)iter.next();
      if (we == null) {
	we = (VHDLNet)in_port.getNet();
      } else {
	in = (VHDLNet)in_port.getNet();
      }
    }
    
    Name vhdl_clk = clk.getVHDLName();
    Name vhdl_in = in.getVHDLName();
    Name vhdl_out = net.getVHDLName();
    // can we be reset tolerant ?
    Name vhdl_reset = reset.getVHDLName();
    Name vhdl_we = null;
    if (we != null) 
      vhdl_we = we.getVHDLName();

    IfStatement if1 = (IfStatement)p.addStatement(new IfStatement());
    
    Primary init = VHDLConstant.genConstant(new BigInteger(getValue(),16), 
					    getWidth());
    // here I can get away with LinkedLists because they do not need to do VHDLout
    LinkedList if1_state_0 = 
      if1.addElseIf( new Paren(new Eq(vhdl_reset, Char.ONE)));
    if1_state_0.add(new SignalAssignment(vhdl_out, new Waveform(init)));

    And and = new And(new AttributeName(vhdl_clk, AttributeName.EVENT), 
		      new Eq(vhdl_clk, Char.ONE));

    LinkedList if1_state_1 = if1.addElseIf( new Paren(and));

    SequentialStatement sa = new SignalAssignment(vhdl_out, vhdl_in);
    if (we != null) {
      IfStatement if2 = new IfStatement();
      if1_state_1.add(if2);
      LinkedList if2_state = 
	if2.addElseIf(new Paren(new Eq(vhdl_we, Char.ONE)));
      if2_state.add(sa);
    } else {
      if1_state_1.add(sa);
    }

  }


}

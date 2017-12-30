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


package fp.synthesis;

import fp.circuit.Circuit;
import fp.circuit.Operation;
import fp.circuit.dot.DotCircuit;
import fp.circuit.vhdl.VHDLCircuit;

import java.util.*;

public final class RegfileGenerator extends GenericCircuitGenerator {
  /**
   * This class implements an abstract register-file circuit.
   */

  private HashMap _registerLog;
  private ArrayList _external_names;
  private RegInfoList _reg_list;

  RegfileGenerator(Circuit parent, ArrayList externs, RegInfoList regs) {
    super(_target, "regfile", parent);
    
    _external_names = externs;
    
    _registerLog = new HashMap();
    _reg_list = regs;
    
    _circuit.insertInPort("clk", "i_clk", "clk", 1);
    _circuit.insertInPort("reset", "i_reset", "reset", 1);
  }

  protected HashMap getRegisterLog() {
    return _registerLog;
  }

  protected ArrayList getRegInfos() {
    return _reg_list;
  }

  /*
  protected ArrayList getExternals() {
    return _externals;
  }
  */

  class InsideWires {
    InsideWires(String iw, String iwe, String ir) {
      w = iw;
      we = iwe;
      r = ir;
    };
    String w;
    String we;
    String r;
  }

  /**
   * Creates a register in the register file.  Returns true if this is 
   * the first time trying to add a register with this name.  Otherwise, 
   * if this register has already been added, it doesn't add another and 
   * returns false.
   */
  public boolean addRegister(String name, String in_name, String we_name, 
			     String out_name, int width, int contents) {
    if (!_registerLog.containsKey(name)) {
      InsideWires inside = addPorts(name, in_name, we_name, out_name, width);
      _circuit.insertRegister("reg_"+name, inside.w, 
			      inside.we, inside.r, width, contents);
      RegInfo reg = new RegInfo(name, width);
      _registerLog.put(name, reg);
      // check to see if this register can be seen externally
      if(_external_names.contains(name))  
	_reg_list.add(reg);
      return true;
    } 
    return false;
  }

  public boolean addRegister(String name, String in_name, String we_name, 
			     String out_name, int width, float contents) {
    if (!_registerLog.containsKey(name)) {
      InsideWires inside = addPorts(name, in_name, we_name, out_name, width);
      _circuit.insertRegister("reg_"+name, inside.w, 
			      inside.we, inside.r, width, contents);
      RegInfo reg = new RegInfo(name, width);
      _registerLog.put(name, new RegInfo(name, width));
      //check to see if this register can be seen externally
      if(_external_names.contains(name))  
	_reg_list.add(reg);
      return true;
    } 
    return false;
  }

  public boolean addRegister(String name, String in_name, String we_name, 
			     String out_name, int width, double contents) {
    if (!_registerLog.containsKey(name)) {
      InsideWires inside = addPorts(name, in_name, we_name, out_name, width);
      _circuit.insertRegister("reg_"+name, inside.w, 
			      inside.we, inside.r, width, contents);

      RegInfo reg = new RegInfo(name, width);
      _registerLog.put(name, reg);
      //check to see if this register can be seen externally
      if(_external_names.contains(name))  
	_reg_list.add(reg);	
      return true;
    } 
    return false;
  }

  /**
   * This method adds the following ports to the regfile for a single 
   * register: data write, data read, and write enable.
   */
  private InsideWires addPorts(String name, String in_name, String we_name, 
			String out_name, int width) {
    String w  = name+_circuit.getUniqueName();
    String we = name+_circuit.getUniqueName();
    String r  = name+_circuit.getUniqueName();

    // Create the data write port.
    _circuit.insertInPort(in_name, "i_"+name, w, width);
    // Create the write enable port.
    _circuit.insertInPort(we_name, "i_"+name+"_we", we, 1);
    // Create the data read port.
    _circuit.insertOutPort(r, "o_"+name, out_name, width);

    if(_external_names.contains(name)) {
      if(_buildTop)
	_circuit.getParent().insertOutPort("r_"+name, "o_"+name, name+"_r", 
					   width);
      else
	_circuit.getParent().insertOutPort("r_"+name, "o_"+name, width);
    }

    return new InsideWires(w, we, r);
  }

}

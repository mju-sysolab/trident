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

import fp.circuit.*;
import fp.circuit.dot.DotCircuit;
import fp.circuit.vhdl.VHDLCircuit;

import java.util.*;
import java.math.BigInteger;

public final class RegisterInterfaceGenerator extends InterfaceGenerator {
  
  // THIS IS THE REAL ONE ...

  private RegInfoList _registers;

  private static final int ADDR_BUS_START  = 0;
  private static final int STAT_BUS_REG_WE = 0;

  RegisterInterfaceGenerator(Circuit circuit, RegInfoList regInfos) { 
    _circuit = circuit;
    _registers = regInfos;
    
    RegInfoList.sort(_registers);  // Sort these so that order is maintained!
        
  }

  public void generate() {
    // Add logic for creating the in port, getting the write enable, and 
    // getting the read enable from the status bus.
    makeStatBusLogic();

    // Add logic for creating the in/out ports.
    makeDataBusLogic();

    // Add logic for creating the in/out ports and accessing the address 
    // portion of the bus. Also, the start signal is at addr(0).
    String startWire = makeAddrBusLogic();

    // Add logic for decoding the addr bus, and look for read and write 
    // enables.
    makeAccessLogic(startWire);
  }


  void makeStatBusLogic() {
    // Make the status bus inport...
    String status = I_STAT_BUS;
    //_circuit.insertInPort(status, status, STAT_BUS_WIDTH);

    // Get the register write enable.
    String regWe = REG_WE;

    insertExpand(_circuit, "expand_"+regWe, status, STAT_BUS_REG_WE, 
		STAT_BUS_REG_WE, regWe, STAT_BUS_WIDTH);
  }

  void makeDataBusLogic() {
    // Add an inport for the data...
    String memIn = I_DATA_BUS;
    //_circuit.insertInPort(memIn, REG_IN, DATA_BUS_WIDTH);
    //Port p = _circuit.getPortByName(memIn);
    // this is evil ins regards to width, I tried to find the port, but it is a 
    // heirarchical name.... :(
    _circuit.insertOperator(Operation.BUF, REG_IN, memIn, REG_IN, DATA_BUS_WIDTH);

    // Make a data outport...
    String memOut = O_DATA_BUS;
    //_circuit.insertOutPort(memOut, memOut, DATA_BUS_WIDTH);    
  }

  String makeAddrBusLogic() {
    // add start signal
    RegInfo start_ri = new RegInfo("start", 1);
    _registers.add(start_ri);
    
    // Make the inports for the addr bus...
    String addrBusName = I_ADDR_BUS;
    int nAddrPorts = 
      AddressDecodeGenerator.getDecoderWidth(_registers.size() + 
					     _registers.getAddressStart());

    int addrStartBit = 
      AddressDecodeGenerator.getStartBit(_registers.getAddressStart());

    int addrEndBit = addrStartBit + nAddrPorts;

    //_circuit.insertInPort(addrBusName, addrBusName, ADDR_BUS_WIDTH);

    String addr = "addr";
    insertExpand(_circuit, "expand_"+addr+_circuit.getUniqueName(), 
		 addrBusName, addrStartBit, addrEndBit-1, addr, 
		 ADDR_BUS_WIDTH, true);

    // Get the global start signal.
    String start = "start";
    int address = (int)(start_ri.getAddrSimple());

    DecOut out = makeAddressDecoder(start, address, address, start);
    return out.w2;  // Return the registered start decode...
  }

  void makeAccessLogic(String startWire) {
    String done = "done";

    // Get the negated mem_en bit from i_addr.
    /*
    int nMemories = MemoryInterfaceGenerator.N_MEMORIES;
    int bitNum = ADDR_BUS_WIDTH-
      AddressDecodeGenerator.getDecoderWidth(nMemories)-1;
    String mem_en = "addr_"+bitNum;
    */
    String mem_en = "mem_en";
    String mem_en_n = mem_en+"_n";
    _circuit.insertOperator(Operation.NOT, "inv_mem_en", mem_en, mem_en_n, 1);

    // For each register, add an address decoder, and read and write logic...
    int i = 0;
    HashMap orMap = new HashMap();
    for(Iterator it = _registers.iterator(); it.hasNext(); i++) {
      RegInfo reg = (RegInfo)it.next();
      String name = reg.getName();
      // skip start ??
      // we build start differently.
      if (name.equals("start")) continue;
      //long addr = reg.getAddr();
      //long addr = i;
      long addr = reg.getAddrSimple();
      int width = reg.getWidth();

      DecOut decOut = makeAddressDecoder(name, addr, _registers.size());
      String decRd = decOut.w2;
      String decWt = decOut.w1;
      String orOut = makeReadLogic(name, decRd, name+"_r", width);
      orMap.put(orOut, new PortTag(null, "in"+i, PortTag.IN, DATA_BUS_WIDTH));
      makeWriteLogic(name, decWt, name+"_w", REG_WE, mem_en_n, I_DATA_BUS, 
		     done, width);
    }

    // Output done when the start signal is high.
    String expDone = "exp_"+done;
    String muxOut = done+"_mout";
    String zeroOut = _circuit.getUniqueWireName();
    _circuit.insertConstant("c_0", BigInteger.ZERO, zeroOut, DATA_BUS_WIDTH);
    insertZeroExtend(_circuit, "extend_"+done, done, expDone, 1, 
		     DATA_BUS_WIDTH);
    insertMux(_circuit, "mux_"+done, zeroOut, expDone, startWire, muxOut, 
	      DATA_BUS_WIDTH);
    orMap.put(muxOut, new PortTag(null, "in"+i, PortTag.IN, DATA_BUS_WIDTH));  

    String orOut = O_DATA_BUS;
    orMap.put(orOut, new PortTag(null, "out", PortTag.OUT, DATA_BUS_WIDTH));
    _circuit.insertOperator("or_"+orOut, Operation.OR, orMap);    
  }


  /**
   * Read from the register file.
   *
   * AND the address decoder output with the read_reg signal. The output of 
   * the AND gate controls a mux which guards the output of the register's 
   * port outside the design level.
   */
  String makeReadLogic(String regName, String decoderOut, String portOut, 
		       int width) {
    // Make the guarding mux...
    String zeroOut = _circuit.getUniqueWireName();
    String muxOut = regName+"_mout";
    _circuit.insertConstant("c_0", BigInteger.ZERO, zeroOut, width);
    insertMux(_circuit, "mux_"+regName, zeroOut, portOut, decoderOut, muxOut, 
	      width);

    String expOut = "exp_"+regName;
    insertZeroExtend(_circuit, "extend_"+regName, muxOut, expOut, width, 
		 DATA_BUS_WIDTH);

    return expOut;
  }

  /**
   * Write to the register file.
   */
  void makeWriteLogic(String regName, String decoderOut, String portIn, 
		      String regWe, String memEnN, String memIn, String done, 
		      int width) {
    // Make an AND gate that goes to the write enable for the HW design...
    HashMap andMap = new HashMap();

    String portInEn = regName + "_we";

    andMap.put(regWe, new PortTag(null, "in0", PortTag.IN, 1));
    andMap.put(decoderOut, new PortTag(null, "in1", PortTag.IN, 1));
    andMap.put(done, new PortTag(null, "in2", PortTag.IN, 1));
    andMap.put(memEnN, new PortTag(null, "in3", PortTag.IN, 1));

    andMap.put(portInEn, new PortTag(null, "out", PortTag.OUT, 1));
    _circuit.insertOperator("and_bus"+_circuit.getUniqueName(), 
			    Operation.AND, andMap);
  }

  
  public static void main(String args[]) {
    Circuit parent = new DotCircuit(null, null, "top");
    RegInfoList regInfos = new RegInfoList(0x8000,0,1);

    for(int i = 0; i < 10; i++) {
      RegInfo reg = new RegInfo("r"+i, i, 32);
      regInfos.add(reg);
    }

    RegisterInterfaceGenerator regbus = new RegisterInterfaceGenerator(parent, regInfos);
    regbus.generate();

    parent.build("regbus_test.dot", null);
  }

}

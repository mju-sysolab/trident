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

public final class RegisterBusGenerator extends GenericCircuitGenerator {
  
  private ArrayList _registers;

  private static final String I_ADDR_BUS = "i_addr";
  private static final String I_STAT_BUS = "i_status";
  private static final String I_DATA_BUS = "i_data";
  private static final String O_DATA_BUS = "o_reg";
  
  public  static final int DATA_BUS_WIDTH = 64;
  private static final int ADDR_BUS_WIDTH = 22;
  private static final int STAT_BUS_WIDTH = 3;

  private static final int ADDR_BUS_START  = 0;
  private static final int STAT_BUS_REG_WE = 0;
  public  static final int OFFSET = 1;

  public  static final String REG_IN = "reg_in";
  private static final String REG_WE = "reg_we";

  
  RegisterBusGenerator(Circuit circuit, ArrayList regInfos) {
    _circuit = circuit;
    _registers = regInfos;
    RegInfo.sort(_registers);  // Sort these so that order is maintained!
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
    _circuit.insertInPort(status, status, STAT_BUS_WIDTH);

    // Get the register write enable.
    String regWe = REG_WE;

    insertExpand(_circuit, "slice_"+regWe, status, STAT_BUS_REG_WE, 
		STAT_BUS_REG_WE, regWe, STAT_BUS_WIDTH);
  }

  void makeDataBusLogic() {
    // Add an inport for the data...
    String memIn = I_DATA_BUS;
    _circuit.insertInPort(memIn, REG_IN, DATA_BUS_WIDTH);

    // Make a data outport...
    String memOut = O_DATA_BUS;
    _circuit.insertOutPort(memOut, memOut, DATA_BUS_WIDTH);    
  }

  String makeAddrBusLogic() {
    // Make the inports for the addr bus...
    String addrBusName = I_ADDR_BUS;
    int nAddrPorts = 
      AddressDecodeGenerator.getDecoderWidth(_registers.size()+1 + OFFSET);

    //int addrStartBit = OFFSET;
    int addrStartBit = AddressDecodeGenerator.getStartBit(OFFSET);
    int addrEndBit = addrStartBit + nAddrPorts;
    _circuit.insertInPort(addrBusName, addrBusName, ADDR_BUS_WIDTH);
    String addr = "addr";
    insertExpand(_circuit, "expand_"+addr, addrBusName, addrStartBit, 
		 addrEndBit-1, addr, ADDR_BUS_WIDTH);

    // Get the global start signal.
    String start = "start";
    DecOut out = makeAddressDecoder(start, _registers.size(), 
				    _registers.size(), start);
    return out.w2;  // Return the registered start decode...
  }

  void makeAccessLogic(String startWire) {
    String done = "done";

    // For each register, add an address decoder, and read and write logic...
    int i = 0;
    HashMap orMap = new HashMap();
    for(Iterator it = _registers.iterator(); it.hasNext(); i++) {
      RegInfo reg = (RegInfo)it.next();
      String name = reg.getName();
      //long addr = reg.getAddr();
      long addr = i;
      int width = reg.getWidth();

      DecOut decOut = makeAddressDecoder(name, addr, _registers.size());
      String decRd = decOut.w2;
      String decWt = decOut.w1;
      String orOut = makeReadLogic(name, decRd, name+"_r", width);
      orMap.put(orOut, new PortTag(null, "in"+i, PortTag.IN, DATA_BUS_WIDTH));
      makeWriteLogic(name, decWt, name+"_w", REG_WE, I_DATA_BUS, done, width);
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
    _circuit.insertOperator(orOut, Operation.OR, orMap);    
  }


  DecOut makeAddressDecoder(String regName, long regAddr, int numRegs, 
			    String output) {
    String decOut = "addr_dec_"+regName;
    // Get addr dec width; add 1 because of start signal
    int decWidth = AddressDecodeGenerator.getDecoderWidth(numRegs+1 + OFFSET);

    // Create an address decoder...
    AddressDecodeGenerator decoder =         
      new AddressDecodeGenerator(_circuit, "addr_"+regName, decOut, regAddr, 
				 OFFSET, decWidth);
    decoder.generate();

    String regIn = (output == null) ? decOut : "or_we_"+regName;

    // Register the output of the decoder for the XD1 reg read delay;
    String regOut = (output == null) ? decOut+"_r" : output+"_r";
    String regN = (output == null) ? "reg_"+decOut : "reg_"+output;
    String in = (output == null) ? decOut : regName;
    _circuit.insertRegister(regN, regIn, null, regOut, 1, 0);
    DecOut out = new DecOut(in, regOut);

    // Buffer for getting the correct name for the design-level start signal.
    if(output != null) {
      _circuit.insertOperator(Operation.AND, regIn, REG_WE, decOut, regIn, 1);
      insertBuf(_circuit, "buf_"+regName, regIn, regName, 1);
    }

    return out;
  }
  DecOut makeAddressDecoder(String regName, long regAddr, int numRegs) {
    return makeAddressDecoder(regName, regAddr, numRegs, null);
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
		      String regWe, String memIn, String notDone, 
		      int width) {
    // Make an AND gate that goes to the write enable for the HW design...
    HashMap andMap = new HashMap();

    String portInEn = regName + "_we";

    andMap.put(regWe, new PortTag(null, "in0", PortTag.IN, 1));
    andMap.put(decoderOut, new PortTag(null, "in1", PortTag.IN, 1));
    // CHANGE: is the notDone signal necessary?
    andMap.put(notDone, new PortTag(null, "in2", PortTag.IN, 1));
    andMap.put(portInEn, new PortTag(null, "out", PortTag.OUT, 1));
    _circuit.insertOperator("and_bus"+_circuit.getUniqueName(), 
			    Operation.AND, andMap);
  }

  class DecOut {
    DecOut(String aw1, String aw2) {
      w1 = aw1;
      w2 = aw2;
    }
    public String w1;
    public String w2;
  }



  
  public static void main(String args[]) {
    Circuit parent = new DotCircuit(null, null, "top");
    ArrayList regInfos = new ArrayList();

    for(int i = 0; i < 10; i++) {
      RegInfo reg = new RegInfo("r"+i, i, 32);
      regInfos.add(reg);
    }

    RegisterBusGenerator regbus = new RegisterBusGenerator(parent, regInfos);
    regbus.generate();

    parent.build("regbus_test.dot", null);
  }

}

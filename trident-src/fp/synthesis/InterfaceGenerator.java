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

public abstract class InterfaceGenerator extends GenericCircuitGenerator {

  protected static final String I_ADDR_BUS = "i_addr";
  protected static final String I_STAT_BUS = "i_status";
  protected static final String I_DATA_BUS = "i_data";
  protected static final String O_DATA_BUS = "o_reg";
  
  // this is really bogus
  public    static int DATA_BUS_WIDTH = 64;
  protected static int ADDR_BUS_WIDTH = 22;
  protected static int STAT_BUS_WIDTH = 4;

  public    static final String REG_IN = "reg_in";
  protected static final String REG_WE = "reg_we";

  // this should not be used.
  //public    static final int OFFSET = 1;



  /**
   * This method creates an address decoder of the given size and then 
   * decides whether a registered output is appropriate.
   * 
   * what is the difference between num and addr ?
   * num is only used to determine how wide the address is.  I guess
   * this allows you to have big addresses, but only decode a small part???
   */
  DecOut makeAddressDecoder(String name, long addr, int num, String output) {
    String decOut = "addr_dec_"+name;
    // Get addr dec width; add 1 because of start signal
    int decWidth = AddressDecodeGenerator.getDecoderWidth(num+ 1);

    System.out.println("name="+name+", addr="+(addr)+", num="+num);

    // Create an address decoder...
    AddressDecodeGenerator decoder =         
      new AddressDecodeGenerator(_circuit, "addr_"+name, decOut, addr, 
				 0, decWidth);
    decoder.generate();

    String regIn = (output == null) ? decOut : "and_we_"+name;

    // Register the output of the decoder for the XD1 reg read delay;
    String regOut = (output == null) ? decOut+"_r" : output+"_r";
    String regN = (output == null) ? "reg_"+decOut : "reg_"+output;
    String in = (output == null) ? decOut : name;
    _circuit.insertRegister(regN, regIn, null, regOut, 1, 0);
    DecOut out = new DecOut(in, regOut);

    // Buffer for getting the correct name for the design-level start signal.
    if(output != null) {
      _circuit.insertOperator(Operation.AND, regIn, REG_WE, decOut, regIn, 1);
      insertBuf(_circuit, "buf_"+name, regIn, name, 1);
    }

    return out;
  }
  DecOut makeAddressDecoder(String name, long addr, int num) {
    return makeAddressDecoder(name, addr, num, null);
  }

  /**
   * This method makes a memory decoder (actually an address decoder) in 
   * order to determine which memory is being referenced.  It uses the 
   * top "decWidth" bits for the memory selection and the next bit as a 
   * memory enable bit.
   */
  static String makeMemoryDecoder(Circuit circuit, String name, long addr, 
				  int num) {
    String decOut = "mem_dec_"+addr;
    int decWidth = AddressDecodeGenerator.getDecoderWidth(num);
    // this should be better automated ...
    int high = ADDR_BUS_WIDTH - 1    -1; // Sub 2 to line up mem sel bits
    int low = high + 1 - decWidth;
    long offset = (long) Math.pow(2, low) - 1;

    /*
    // Create a memory decoder.
    AddressDecodeGenerator decoder = 
      new AddressDecodeGenerator(circuit, "addr_"+name, decOut, addr, low, 
				 decWidth, ADDR_BUS_WIDTH);
    */
    // Create a memory decoder.
    AddressDecodeGenerator decoder = 
      new AddressDecodeGenerator(circuit, "addr_"+name, decOut, addr, low, 
				 decWidth, high);
    decoder.generate();

    /*
    // Make a register.
    String regOut = decOut+"_r";
    circuit.insertRegister("reg_"+decOut, decOut, null, regOut, 1, 0);
    return regOut;
    */

    return decOut;
  }




  class DecOut {
    DecOut(String aw1, String aw2) {
      w1 = aw1;
      w2 = aw2;
    }
    public String w1;
    public String w2;
  }

}

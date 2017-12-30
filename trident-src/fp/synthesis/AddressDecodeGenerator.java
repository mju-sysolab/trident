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

public final class AddressDecodeGenerator extends GenericCircuitGenerator {

  private String  _outName;
  private int     _width;
  private String  _inName;
  private long    _address;
  private long    _offset;
  private int _highBit;

  AddressDecodeGenerator(Circuit circuit, String inName, String outName, 
			 long address, long offset, int width, 
			 int highBit) {
    _circuit = circuit;
    _inName = inName;
    _address = (highBit != -1) ? address : address+offset;
    _offset = offset;
    _width = width;
    _outName = outName;
    _highBit = highBit;
  }
  AddressDecodeGenerator(Circuit circuit, String inName, String outName, 
			 long address, long offset, int width) {
    this(circuit, inName, outName, address, offset, width, -1);
  }

  protected void generate() {
    String addrBits = getAddrBitString();
    HashMap andMap = new HashMap(addrBits.length());

    // For each bit of the address...
    int start = (_highBit != -1) ? 0 : getStartBit(_offset);
    for(int i = start; i < addrBits.length(); i++) {
      String input = _inName+"_"+i;
      char bit = addrBits.charAt((addrBits.length()-1)-i);
      String addr = (_highBit != -1) ? "addr_"+(i+_offset) : "addr_"+i;
      if(bit == '0') {
	// If this is a zero bit, then invert the input...
	input = "inv_"+input;
	_circuit.insertOperator(Operation.NOT, input, addr, input,1);
      } else {
	_circuit.insertOperator(Operation.BUF, "buf_"+input, addr, 
				input, 1);
      }
      
      // Add this input to the operator input hashmap.
      andMap.put(input, new PortTag(null, "in"+i, PortTag.IN, 1));
    }
    
    // Add the output to the map and insert the operator into the circuit.
    andMap.put(_outName, new PortTag(null, "out", PortTag.OUT, 1));
    _circuit.insertOperator("and_addr_dec"+_circuit.getUniqueName(), 
			    Operation.AND, andMap);
  }


  private String getAddrBitString() {
    String addrBits = Long.toBinaryString(_address);

    int leng = addrBits.length();
    
    if(leng > _width) {
      // If the address is too large for this decoder...
      int first1 = addrBits.indexOf('1');
      if(first1 < (leng - _width))
	throw new SynthesisException("Address is too large for width!!!" + 
				     "\n addr(b)="+addrBits+", width="+_width);
      // Remove excess leading 0's.
      addrBits = addrBits.substring(leng - _width);

    } else if(leng < _width) {  
      // If addr is too small...

      // Pack leading 0's so addrBits is _width in length.
      int pleng = _width - leng;
      String packBits = "";
      for(int i = 0; i < pleng; i++)
	packBits += "0";
      addrBits = packBits + addrBits;
    }
    return addrBits;
  }

  static int getStartBit(long offset) {
    return (offset == 0) ? 0 : (int)Math.floor(Math.log(offset)/Math.log(2));
  }

  static int getDecoderWidth(int numRegs) {
    return (numRegs==1) ? 1 : ((int)Math.ceil(Math.log(numRegs)/Math.log(2)));
  }

  public static void main(String args[]) {
    Circuit parent = new DotCircuit(null, null, "top");
    AddressDecodeGenerator ad = 
      new AddressDecodeGenerator(parent, "reg1", "out", 1234, 1, 12);
    parent.build("ad_test.dot", null);
  }


}

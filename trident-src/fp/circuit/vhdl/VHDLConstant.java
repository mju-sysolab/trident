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
import fp.circuit.Constant;
import fp.circuit.PortTag;

import fp.util.vhdl.generator.*;


public class VHDLConstant extends Constant {

  VHDLConstant(Circuit parent, String name, 
	       String value, int width, int type) {
    super(parent, name, value, width, type);
  }


  public void build(String name, Object[] arg_o) {
    //System.out.println("Building VHDLConstant");

    
    //System.out.println("Setting width to 8 for grins");
    //setWidth(8);


    VHDLCircuit parent = (VHDLCircuit)getParent();

    DesignUnit du = parent.getVHDLParent();
    LibraryUnit lu = du.getLibraryUnit();
    //Entity e = lu.getEntity();
    Architecture a = lu.getArchitecture();
    
    // need to get the name of the out edge.
    PortTag out = (PortTag)getOutPorts().iterator().next();
    VHDLNet net = (VHDLNet)out.getNet();

    // not legal yet...
    //a.addStatement(new SignalAssignment(net.getBus(), bitString(getValue(), getWidth())));

    ConditionalSignalAssignment csa = 
      new ConditionalSignalAssignment(net.getVHDLName());

    // Get the value; if its an int then must do bit manipulations.
  
    Primary p = null;
    if (getType() == INT) {
      //System.out.println(" Value "+getValue());
      BigInteger bi = new BigInteger(getValue(), 16);
      
      p = genConstant(bi, getWidth());

    } else if (getType() == FLOAT) {
      p = new BitStringLiteral(BitStringLiteral.X, getValue());
    } else if (getType() == DOUBLE) {
      p = new BitStringLiteral(BitStringLiteral.X, getValue());
    } else {
      // punting ...
      String const_str = getValue();
      if (const_str.length() == 1) 
	p = new Char(const_str.charAt(0));
      else
	p = new StringLiteral(const_str);
    }
    
    //System.out.println("String "+p+" "+getWidth()+" "+getName());

    csa.addCondition(new Waveform(p),null);
    a.addStatement(csa);
    
  }


  static Primary genConstant(BigInteger bi, int width) {
    return genConstant(bi, width, true);
  }

  static Primary genConstant(BigInteger bi, int width, boolean anySizeZeros) {
    Primary p = null;
    
    
    /* this will need to be better ---
       If all constants always have wires, then we don't need to 
       work as hard as we have been for zero.  (others => '0') 
       should be okay for all zeros except single bit.
       
       We need to work harder for positive and especially for 
       negative values.
    */
    
    
    // check against zero ...
    int result = bi.compareTo(BigInteger.ZERO);
    
    if (result == 0) {
      // zero (others => '0') unless width = 1 or anySizeZeros is false
      if (width == 1) {
	return (Primary)Char.ZERO;
      } 
      else if(anySizeZeros == true) {
	// others
	return (Primary)Aggregate.OTHERS_IS_ZERO;
      }
      else {
	//Write the necessary number of 0s
	return genHexandBits(bi, width);
      }
    } 
    else if (result > 0) {
      p = genHexandBits(bi, width);  
    } 
    else {
      if (width > 1) {
      // negative
      /* negative is lame -- I am going to punt and not worry about
	 it.  The only trick is what to do when it is one bit or less.
      */

	LinkedList params = new LinkedList();
	
	FunctionCall fc = 
	  new FunctionCall(new SimpleName("conv_std_logic_vector"));
    
	fc.add(new Expression(new NumericLiteral(bi.toString())));
	fc.add(new Expression(new NumericLiteral(width)));
	p = fc;
      } 
      else if (width == 1) {
	// if we have a negative number and the width 1, then
	p = Char.ONE;
      } 
      else {
	// the width is negative -- hmmm.
	System.err.println("Negative width of a negative constant -- blah.");
	System.exit(-1);
      }
    }
    return p;
  }

  static Primary genHexandBits(BigInteger bi, int width) {
    //Tries to write as much as possible in hex, but will also create bits when the width is not a multiple of 4
    Primary p = null;

    int bi_width = bi.bitLength();
    int digits = width / 4;
    int bi_digits = bi_width / 4;
    int bits = width % 4;
    int bi_bits = bi_width % 4;
      
    String hex = bi.toString(16);
    int length = hex.length();

    //System.out.println(" hex "+hex);

    String head = "";
      
    if (width <= bi_width) {
	
      String sub = hex.substring(length - digits);
      if (length - digits != 0) {
	head = 
	  bitString(Integer.parseInt(hex.substring(0, length - digits),
				     16), bits);
      }
      p = concat(head,sub);
    } else {
      if (digits > bi_digits) {
	// how many digits are we missing
	
	int diff = digits - bi_digits - ((bi_bits > 0) ? 1 : 0);
	//System.out.println(" digits "+digits+" bi_digits "+bi_digits);
	//System.out.println(" digits required > bi_digits "+(diff));
	  
	for (int i=0; i<diff; i++) {
	  hex = "0"+hex;
	}
	String sub = hex;
	  
	if (bits != 0) {
	  head = bitString(0, bits);
	}
	p = concat(head,sub);
	  
      } else {
	// this case is when there is a slight different in bit sizes
	String sub = hex.substring(length - digits);
	  
	if (length - digits != 0)
	  head = 
	    bitString(Integer.parseInt(hex.substring(0, length - digits), 
				       16), bits);
	p = concat(head, sub);
      }
    }
    return p;
  }

  static Primary concat(String bits, String hex) {
  Primary p = null;

    BitStringLiteral bsl = null;

    if (hex.length() > 0) {
      bsl = new BitStringLiteral(BitStringLiteral.X, hex);

      if (bits.length() > 0) {
	p = new Concat(new BitStringLiteral(BitStringLiteral.B, bits), 
		       bsl);
      } else {
	p = bsl;
      }
    } else {
      if (bits.length() > 1) 
	p = new BitStringLiteral(BitStringLiteral.B, bits);
      else if (bits.startsWith("1")) 
	p = Char.ONE;
      else 
	p = Char.ZERO;
	
    }
      
    return p;
  }
    
  static String bitString(int value, int width) {
    StringBuffer bob = new StringBuffer();
    
    boolean neg = (value < 0);
    int min_width = minWidth(value);

    String binary = Integer.toBinaryString(value);
    int bin_size = binary.length();

    if (width <= 32) {
      if (width <= min_width) {
	bob.append(binary.substring(bin_size - width));
      } else {
	int difference = width - min_width;
	for (int i=0; i<difference; i++) {
	  bob.append(neg ? '1' : '0');
	}
	bob.append(binary.substring(bin_size-min_width));
      }
    } else {
      System.out.println(" Constants bigger than 32 bits? ");
      System.exit(-1);
    }
    
    return bob.toString();
  }

  // this does not work for negative numbers
  static int minWidth(int value) {
    int width = 0;
    boolean neg = (value < 0);
    if (neg) {
      value *= -1;
      width++;
    }
    
    while (value > 0) {
      value >>>=1;
      width++;
    }
    return width;
  }



  public static void main(String[] args) {
    int test [][] = {
      {-5, 10}, {-5, 1}, {5, 10}, {5, 2}, {5, 1},
      {2, 3}, {2, 20}, {2,6}, {2,1}, {12345678,30},
      {123456789,30}, {123456789,18},
    };

    for(int i = 0; i<test.length; i++) {
      Primary p = VHDLConstant.genConstant(BigInteger.valueOf(test[i][0]), 
					   test[i][1]);
      System.out.println("Val "+test[i][0]+" width "+test[i][1]+" - VHDL: "+p);
    }


  }

}

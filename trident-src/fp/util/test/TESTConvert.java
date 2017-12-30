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


package fp.util.test;
import java.io.*;
import fp.util.*;

public class TESTConvert
{
  private static int compareFloatsViaDoubleHexString(float in, String hexstring) {
    float out = Convert.ieeeDoubleHexStringToFloat(hexstring);
    System.out.print("infloat = " + in + " hexstring=" + hexstring + " outfloat=" + out );
    if ((in == out) || (Float.isNaN(in) && Float.isNaN(out))) {
      System.out.println(" => PASS");
      return 0;
    } else {
      System.out.println(" => FAIL");
      return 1;
    }
  }
  
  private static int compareFloatsViaFloatHexString(float in, String hexstring) {
    float out = Convert.ieeeFloatHexStringToFloat(hexstring);
    System.out.print("infloat = " + in + " hexstring=" + hexstring + " outfloat=" + out);
    if ((in == out) || (Float.isNaN(in) && Float.isNaN(out))) {
      System.out.println(" => PASS");
      return 0;
    } else {
      System.out.println(" => FAIL");
      return 1;
    }
  }
  
  private static int compareDoubles(double in, String hexstring) {
    double out = Convert.ieeeDoubleHexStringToDouble(hexstring);
    System.out.print("indouble = " + in + " hexstring=" + hexstring + " outdouble=" + out);
    if ((in == out) || (Double.isNaN(in) && Double.isNaN(out))) {
      System.out.println(" => PASS");
      return 0;
    } else {
      System.out.println(" => FAIL");
      return 1;
    }
  }

  public static void main(String args[]) {
    float infloat = 0;
    float outfloat = 0;
    String hexstring, decstring;
    byte[] hexarray = new byte[16];
    StringBuffer sb;
    int ch;
    double indouble = 0;
    double outdouble = 0;
    boolean EOF = false;
    int retval = 0;
    try
    {
      FileInputStream myStream = new FileInputStream("floats.dat");
      while(!EOF) 
      {
	sb = new StringBuffer(16);
	while ( ((char)(ch = myStream.read()) != ' ') && (ch != -1)) {
	  sb.append( (char) ch );
	}
	if (ch == -1) {
	  EOF = true;
	  break;
	}
	myStream.read(hexarray);
	ch = myStream.read(); // new line
	if (ch == -1) {
	  EOF = true;
	}
	decstring = sb.toString();
	infloat = Float.parseFloat(decstring);	
	hexstring = new String(hexarray);
	if (compareFloatsViaDoubleHexString(infloat, hexstring) != 0) retval = 1;
      }
    }
    catch(IOException e)
    {
      System.err.println ("Error reading input file" + e);
      System.exit(1);
      return;
    }

    // Special cases
    if (compareFloatsViaFloatHexString(Float.POSITIVE_INFINITY, "7f800000") != 0) retval = 1;;
    if (compareFloatsViaFloatHexString(Float.NEGATIVE_INFINITY, "ff800000") != 0) retval = 1;;
    if (compareFloatsViaFloatHexString(Float.NaN, "7fc00000") != 0) retval = 1;;
    if (compareFloatsViaFloatHexString(Float.MAX_VALUE, "7f7fffff") != 0) retval = 1;;
    if (compareFloatsViaFloatHexString(Float.MIN_VALUE, "00000001") != 0) retval = 1;;

    if (compareDoubles(Double.POSITIVE_INFINITY, "7ff0000000000000") != 0) retval = 1;;
    if (compareDoubles(Double.NEGATIVE_INFINITY, "fff0000000000000") != 0) retval = 1;;
    if (compareDoubles(Double.NaN, "7ff8000000000000") != 0) retval = 1;;
    if (compareDoubles(Double.MAX_VALUE, "7fefffffffffffff") != 0) retval = 1;;
    if (compareDoubles(Double.MIN_VALUE, "0000000000000001") != 0) retval = 1;;

    if (retval != 0) System.exit(retval);
  }


}

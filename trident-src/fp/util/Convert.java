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


package fp.util;

public class Convert {

  public static final int ieeeFloatHexStringLength = 8;
  public static final int ieeeDoubleHexStringLength = 16;

  public static boolean isNegative(String hexString) {
    char firstHex = hexString.charAt(0);
    int firstDigit = Character.digit(firstHex, 16);
    if ((firstDigit & 0x8) == 0x8) {
      //System.out.print("--negative--");
      return true;
    }
    return false;
  }

  public static String toPositiveIeeeFPHexString(String hexString) {
    //System.out.println("toUnsignedHexString input: " + hexString);
    int strLen = hexString.length();

    if (strLen == 0) {
      System.err.println("IEEE hex string length must be greater than 0");
      return hexString;
    }

    char firstHex = hexString.charAt(0);
    int firstDigit = Character.digit(firstHex, 16);
    firstDigit = firstDigit & 0x7;
    hexString = Integer.toHexString(firstDigit) + hexString.substring(1, strLen);
    return hexString;
  }

  public static float ieeeDoubleHexStringToFloat(String hexString) {
    float f = 0;
    int strLen = hexString.length();
    boolean isNeg = Convert.isNegative(hexString);
    if (isNeg) { 
      hexString = Convert.toPositiveIeeeFPHexString(hexString);
    }
    if (strLen <= ieeeDoubleHexStringLength) {
      // convert to long then double then float
      long l = Long.parseLong(hexString, 16);
      //System.out.print(" long=" + Long.toHexString(l));
      double d = Double.longBitsToDouble(l);
      //System.out.print(" double=" + d);
      Double D = new Double(Double.longBitsToDouble(Long.parseLong(hexString, 16)));
      f = D.floatValue();
      //System.out.println(" float=" + f);
    } else {
      System.err.println("IEEE double hex string must be <= length " + ieeeDoubleHexStringLength );
      return 0;
    }

    if (isNeg) {
      f = -f;
    }
    return f;
  }

  public static float ieeeFloatHexStringToFloat(String hexString) {
    float f = 0;
    int strLen = hexString.length();
    boolean isNeg = Convert.isNegative(hexString);
    if (isNeg) { 
      hexString = Convert.toPositiveIeeeFPHexString(hexString);
    }
    if (strLen <= ieeeFloatHexStringLength) {
      // convert to int then float
      f = Float.intBitsToFloat(Integer.parseInt(hexString, 16));
    } else {
      System.err.println("IEEE float hex string must be <= length " + ieeeFloatHexStringLength);
      return 0;
    }
    
    if (isNeg) {
      f = -f;
    }
    return f;
  }

  public static double ieeeDoubleHexStringToDouble(String hexString) {
    double d = 0;
    int strLen = hexString.length();
    boolean isNeg = Convert.isNegative(hexString);
    if (isNeg) { 
      hexString = Convert.toPositiveIeeeFPHexString(hexString);
    }
    
    if (strLen <= ieeeDoubleHexStringLength) {
      // convert to long then double
      d = Double.longBitsToDouble(Long.parseLong(hexString, 16));
    } else {
      System.err.println("IEEE double hex string must be <= length " + ieeeDoubleHexStringLength);
      return 0;
    }

    if (isNeg) {
      d = -d;
    }
    return d;
  }

  public static double ieeeFloatHexStringToDouble(String hexString) {
    double d = 0;
    int strLen = hexString.length();
    boolean isNeg = Convert.isNegative(hexString);

    if (isNeg) { 
      hexString = Convert.toPositiveIeeeFPHexString(hexString);
    }

    if (strLen == ieeeFloatHexStringLength) {
      // convert to int then float then double 
      Float f = new Float(Float.intBitsToFloat(Integer.parseInt(hexString,16)));
      d = f.doubleValue();
    } else {
      System.err.println("IEEE float hex string must be <= length " + ieeeFloatHexStringLength);
      return 0;
    }

    if (isNeg) {
      d = -d;
    }
    return d;
  }

  public static String toIeeeFloatHexString(float inFloat) {
    return Integer.toHexString(Float.floatToIntBits(inFloat));
  }
  
  public static String toIeeeDoubleHexString(float inFloat) {
    double d = (double)inFloat;
    return Long.toHexString(Double.doubleToLongBits(d));
  }

  public static String toIeeeDoubleHexString(double inDouble) {
    return Long.toHexString(Double.doubleToLongBits(inDouble));
  }

}

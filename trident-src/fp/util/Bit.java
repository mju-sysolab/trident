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

/**
 * Utility Class for static methods that can be used be different classes in 
 * sc.compiler.
 * Written by Justin Tripp for Sea Cucumber.
 */
public class Bit {

  public static int[] bit_count = {
    0,1,1,2,1,2,2,3,1,2,2,3,2,3,3,4,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,
    1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,
    1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,
    2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
    1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,
    2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
    2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
    3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,4,5,5,6,5,6,6,7,5,6,6,7,6,7,7,8
  };

  /**
   * Count the number of ones in an integer.  This is used to determine 
   * whether an number is a power of two.  If there is only one bit, then 
   * it is guaranteed that it is a power of two.
   * @param c Input value.
   * @return Returns the count of set bits in the integer.
   */
  public static int countOnes(int c) {
    return (bit_count[c & 255] + bit_count[(c >>> 8) & 255]
            + bit_count[(c >>> 16) & 255] + bit_count[(c >>> 24) & 255]);
  }


  /**
   * This method returns the wire width necessary to represent the given value.
   * There has got to be a better way to do this.
   */
  public static int calculateConstantWidth(int val) {
    if (val == 0) {
      return 0;
    } else if (val == 1) {
      return 1;
    } else {
      if (val < 0) {
	val = -val;
	return minWidth(val) + 1;
      } else {
	return minWidth(val);
      }
    }
  }

  static int minWidth(int val) {
    int width = 0;
    while(val > 0) {
      val >>>= 1;
      width++;
    }
    return width;
  }

}

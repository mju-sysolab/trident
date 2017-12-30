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

import java.lang.reflect.Array;

/**
* Convenience method for producing a simple textual
* representation of an array.
*
* <P>The format of the returned <code>String</code> is the same as
* <code>AbstractCollection.toString</code>:
* <ul>
* <li>non-empty array: [blah, blah]
* <li>empty array: []
* <li>null array: null
* </ul>
*
* @author Jerome Lacoste
* @author www.javapractices.com
*/
public final class ArrayToString {

  /**
   * <code>aArray</code> is a possibly-null array whose elements are
   * primitives or objects; arrays of arrays are also valid, in which case
   * <code>aArray</code> is rendered in a nested, recursive fashion.
   */
  public static String get(Object aArray){
    if ( aArray == null ) return fNULL;
    checkObjectIsArray(aArray);

    StringBuffer result = new StringBuffer( fSTART_CHAR );
    int length = Array.getLength(aArray);
    for ( int idx = 0 ; idx < length ; ++idx ) {
      Object item = Array.get(aArray, idx);
      if ( isNonNullArray(item) ){
	//recursive call!
	result.append( get(item) );
      }
      else{
	result.append( item );
      }
      if ( ! isLastItem(idx, length) ) {
	result.append(fSEPARATOR);
      }
    }
    result.append(fEND_CHAR);
    return result.toString();
  }

  // PRIVATE //
  private static final String fSTART_CHAR = "[";
  private static final String fEND_CHAR = "]";
  private static final String fSEPARATOR = ", ";
  private static final String fNULL = "null";

  private static void checkObjectIsArray(Object aArray){
    if ( ! aArray.getClass().isArray() ) {
      throw new IllegalArgumentException("Object is not an array.");
    }
  }

  private static boolean isNonNullArray(Object aItem){
    return aItem != null && aItem.getClass().isArray();
  }

  private static boolean isLastItem(int aIdx, int aLength){
    return (aIdx == aLength - 1);
  }

  /**
   * Test harness.
   */
  public static void main(String[] args) {

    boolean[] booleans = { true, false, false };
    char[] chars = {'B', 'P', 'H'};
    byte[] bytes = {3};
    short[] shorts = {5,6};
    int[] ints = {7,8,9,10};
    long[] longs = {100,101,102};
    float[] floats = { 99.9f, 63.2f};
    double[] doubles = { 212.2, 16.236, 42.2};
    String[] strings = {"blah", "blah", "blah"};
    java.util.Date[] dates = { new java.util.Date(), new java.util.Date() };
    System.out.println("booleans: " + get(booleans));
    System.out.println("chars: " + get(chars));
    System.out.println("bytes: " + get(bytes));
    System.out.println("shorts: " + get(shorts));
    System.out.println("ints: " + get(ints));
    System.out.println("longs: " + get(longs));
    System.out.println("floats: " + get(floats));
    System.out.println("double: " + get(doubles));
    System.out.println("strings: " + get(strings));
    System.out.println("dates: " + get(dates));

    int[] nullInts = null;
    int[] emptyInts = {};
    String[] emptyStrings = {"", ""};
    String[] nullStrings = {null, null};
    System.out.println("null ints: " + get(nullInts));
    System.out.println("empty ints: " + get(emptyInts));
    System.out.println("empty Strings: " + get(emptyStrings));
    System.out.println("null Strings: " + get(nullStrings));

    String[] arrayA = {"A", "a"};
    String[] arrayB = {"B", "b"};
    String[][] arrayOfArrays = {arrayA, arrayB};
    System.out.println("array Of Arrays: " + get(arrayOfArrays));
  }
} 


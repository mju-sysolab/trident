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


//----------------------------------------------------------
// Compute square root of large numbers using Heron's method
//----------------------------------------------------------

package fp.util;
import java.math.*;

public class BigSquareRoot {

  private static BigDecimal ZERO = new BigDecimal ("0");
  private static BigDecimal ONE = new BigDecimal ("1");
  private static BigDecimal TWO = new BigDecimal ("2");
  public static final int DEFAULT_MAX_ITERATIONS = 50;
  public static final int DEFAULT_SCALE = 10;

  private BigDecimal error;
  private int iterations;
  private boolean traceFlag;
  private int scale = DEFAULT_SCALE;
  private int maxIterations = DEFAULT_MAX_ITERATIONS;

  //---------------------------------------
  // The error is the original number minus
  // (sqrt * sqrt). If the original number
  // was a perfect square, the error is 0.
  //---------------------------------------

  public BigDecimal getError () {
    return error;
  }

  //-------------------------------------------------------------
  // Number of iterations performed when square root was computed
  //-------------------------------------------------------------

  public int getIterations () {
    return iterations;
  }

  //-----------
  // Trace flag
  //-----------

  public boolean getTraceFlag () {
    return traceFlag;
  }

  public void setTraceFlag (boolean flag) {
    traceFlag = flag;
  }

  //------
  // Scale
  //------

  public int getScale () {
    return scale;
  }

  public void setScale (int scale) {
    this.scale = scale;
  }

  //-------------------
  // Maximum iterations
  //-------------------

  public int getMaxIterations () {
    return maxIterations;
  }

  public void setMaxIterations (int maxIterations) {
    this.maxIterations = maxIterations;
  }

  //--------------------------
  // Get initial approximation
  //--------------------------

  private static BigDecimal getInitialApproximation (BigDecimal n) {
    BigInteger integerPart = n.toBigInteger ();
    int length = integerPart.toString ().length ();
    if ((length % 2) == 0) {
      length--;
    }
    length /= 2;
    BigDecimal guess = ONE.movePointRight (length);
    return guess;
  }

  //----------------
  // Get square root
  //----------------

  public BigDecimal get (BigInteger n) {
    return get (new BigDecimal (n));
  }

  public BigDecimal get (BigDecimal n) {

    // Make sure n is a positive number

    if (n.compareTo (ZERO) <= 0) {
      throw new IllegalArgumentException ();
    }

    BigDecimal initialGuess = getInitialApproximation (n);
    trace ("Initial guess " + initialGuess.toString ());
    BigDecimal lastGuess = ZERO;
    BigDecimal guess = new BigDecimal (initialGuess.toString ());

    // Iterate

    iterations = 0;
    boolean more = true;
    while (more) {
      lastGuess = guess;
      guess = n.divide(guess, scale, BigDecimal.ROUND_HALF_UP);
      guess = guess.add(lastGuess);
      guess = guess.divide (TWO, scale, BigDecimal.ROUND_HALF_UP);
      trace ("Next guess " + guess.toString ());
      error = n.subtract (guess.multiply (guess));
      if (++iterations >= maxIterations) {
	more = false;
      }
      else if (lastGuess.equals (guess)) {
	more = error.abs ().compareTo (ONE) >= 0;
      }
    }
    return guess;

  }

  //------
  // Trace
  //------

  private void trace (String s) {
    if (traceFlag) {
      System.out.println (s);
    }
  }

  //----------------------
  // Get random BigInteger
  //----------------------

  public static BigInteger getRandomBigInteger (int nDigits) {
    StringBuffer sb = new StringBuffer ();
    java.util.Random r = new java.util.Random ();
    for (int i = 0; i < nDigits; i++) {
      sb.append (r.nextInt (10));
    }
    return new BigInteger (sb.toString ());
  }

  //-----
  // Test
  //-----

  public static void main (String[] args) {

    BigInteger n;
    BigDecimal sqrt;
    BigSquareRoot app = new BigSquareRoot ();
    app.setTraceFlag (true);

    // Generate a random big integer with a hundred digits

    n = BigSquareRoot.getRandomBigInteger (100);

    // Build an array of test numbers

    String testNums[] = {"9", "30", "720", "1024", n.toString ()};

    for (int i = 0; i < testNums.length; i++) {
      n = new BigInteger (testNums[i]);
      if (i > 0) {
	System.out.println ("----------------------------");
      }
      System.out.println ("Computing the square root of");
      System.out.println (n.toString ());
      int length = n.toString ().length ();
      if (length > 20) {
	app.setScale (length / 2);
      }
      sqrt = app.get (n);
      System.out.println ("Iterations " + app.getIterations ());
      System.out.println ("Sqrt " + sqrt.toString ());
      System.out.println (sqrt.multiply (sqrt).toString ());
      System.out.println (n.toString ());
      System.out.println ("Error " + app.getError ().toString ());
    }

  }

}

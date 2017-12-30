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
package fp.circuit;


public abstract class Constant extends Node {

  public static final int INT = 0;
  public static final int FLOAT = 1;
  public static final int DOUBLE = 2;

  private int _type;
  private String _value;
  private int _width;

  public Constant(Circuit parent, String name, String value, int width, 
		  int type) {
    super(parent, name);
    _value = value;
    _width = width;
    _type  = type;
  }

  public Constant(Circuit parent, String value, int width, int type) {
    this(parent, "constant", value, width, type);
  }

  /* CHANGE: are the following needed? */
  /*
  public Constant(Circuit parent, String name, int value, int width) {
    this(parent, name, Integer.toHexString(value), width);
  }
  public Constant(Circuit parent, String name, float value, int width) {
    this(parent, name, 
	 Integer.toHexString(Float.floatToRawIntBits(value)), width);
  }
  public Constant(Circuit parent, String name, double value, int width) {
    this(parent, name, 
	 Long.toHexString(Double.doubleToRawLongBits(value)), width);
  }
  */

  // Return the hex value...
  public String getValue() { return _value; }

  public int getType() { return _type; }

  public int getWidth() { return _width; }

  public void setWidth(int i) { _width = i; }

  /**
   * The following methods are for accessing the updated String-type "_value"
   * private variable.
   */

  public int getValueInt() { 
    return Integer.parseInt(_value, 16); 
  }

  public float getValueFloat() {
    return Float.intBitsToFloat(Integer.parseInt(_value,16));
  }
  public double getValueDouble() {
    return Double.longBitsToDouble(Long.parseLong(_value,16));
  }

  /** CHANGE: are these needed?

  public void setIntValue(int value) { 
    _value = Integer.toHexString(value); 
  }
  public void setFloatValue(float value) {
    _value = Integer.toHexString(Float.floatToRawIntBits(value));
  }
  public void setDoubleValue(double value) {
    _value = Long.toHexString(Double.doubleToRawLongBits(value));
  }
  */
}

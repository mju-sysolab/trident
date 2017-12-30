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

public abstract class Register extends Node {


  private String _value;
  private int    _width;

  

  public Register(Circuit parent, String name, int width, String value) {
    super(parent, name);
    
    // these are important
    _value = value;
    _width = width;

  }

  

  public Register(Circuit parent, int width, String value) {
    this(parent, "dff", width, value);
  }

  public int getWidth() { return _width; }

  protected String getValue() { return _value; }
  
  public int getValueInt() { return Integer.parseInt(_value, 16); }

  public float getValueFloat() { 
    return Float.intBitsToFloat(Integer.parseInt(_value, 16));
  }
  public double getValueDouble() {
    return Double.longBitsToDouble(Long.parseLong(_value, 16));
  }
  

}

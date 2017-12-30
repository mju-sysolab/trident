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

package fp.util.vhdl.generator;

import java.util.*;

public class Type {
  private String _name;
  private int _start;
  private int _stop;
  

  private static class Undef extends Type {
    Undef() { super("undef"); }
  }
  public static final Type UNDEF = new Undef(); 

  private static class Bit extends Type {
    Bit() { super("bit"); }
  }
  public static final Type BIT = new Bit();

  private static class StdLogic extends Type {
    StdLogic() { super("std_logic"); }
  }
  public static final Type STD_LOGIC = new StdLogic();

   private static class StdULogic extends Type {
    StdULogic() { super("std_ulogic"); }
  }
  public static final Type STD_ULOGIC = new StdULogic();

  private static class _Boolean extends Type {
    _Boolean() { super("boolean"); }
  }
  public static final Type BOOLEAN = new _Boolean();

  private static class _Integer extends Type {
    _Integer() { super("integer"); }
  }
  public static final Type INTEGER = new _Integer();

  private static class Real extends Type {
    Real() { super("real"); }
  }
  public static final Type REAL = new Real();

  // is this a vector?
  private static class Character extends Type {
    Character() { super("character"); }
  }
  public static final Type CHARACTER = new Character();

  private static class Time extends Type {
    Time() { super("time"); }
  }
  public static final Type TIME = new Time();

 
  public Type(String n, int start, int stop) {
    _name = n;
    _start = start;
    _stop = stop;
  }
  
  public Type(String n) {
    this(n,0,0);
  }

  public int getStart() { return _start; }
  public int getStop() { return _stop; }

  /*

  public static Type VECTOR(int start, int stop) {
    return new _Vector(start, stop);
  }

  public static Type BIT_VECTOR(int start, int stop) {
    return new BitVector(start, stop);
  }

  public static Type STD_LOGIC_VECTOR(int start, int stop) {
    return new StdLogicVector(start, stop);
  }

  public static Type STD_ULOGIC_VECTOR(int start, int stop) {
    return new StdULogicVector(start, stop);
  }

  */
  public String direction() {
    StringBuffer sbuf = new StringBuffer();

    if (_start == _stop) {
      return "";
    } else {
      String direction;
      if (_start > _stop) {
	direction = " downto ";
      } else {
	direction = " to ";
      }
      sbuf.append("(");
      sbuf.append(_start);
      sbuf.append(direction);
      sbuf.append(_stop);
      sbuf.append(")");
    }
    return sbuf.toString();
  }


  public String toString() {
    StringBuffer sbuf = new StringBuffer(_name);
    // is this right?
    if (_start == _stop) {
      return sbuf.toString();
    } else {
      sbuf.append(direction());
    }
    return sbuf.toString();
  }
}


/*

class _Vector extends Type {
  _Vector(int start, int stop) { 
    super("vector", start, stop); 
  }
}

class BitVector extends Type {
  BitVector(int start, int stop) { 
    super("bit_vector", start, stop); 
  }
}

class StdLogicVector extends Type {
  StdLogicVector(int start, int stop) { 
    super("std_logic_vector", start, stop); 
  }
}

class StdULogicVector extends Type {
  StdULogicVector(int start, int stop) { 
    super("std_ulogic_vector", start, stop); 
  }
}


*/

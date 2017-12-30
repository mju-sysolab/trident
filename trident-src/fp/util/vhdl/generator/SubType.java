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

public class SubType implements VHDLout {

  /*
    This is the SubType indication (not to be confused with the 
    subtype declaration.

    subtype_indication ::=
    [ resolution_function_name ] type_mark [ constraint ]


  */

  private Name _function;
  private Name _type;
  private Constraint _constraint;


  private static class Bit extends SubType {
    Bit() { super(new SimpleName("bit")); }
  }
  public static final SubType BIT = new Bit();
  
  private static class StdLogic extends SubType {
    StdLogic() { super(new SimpleName("std_logic")); }
  }
  public static final SubType STD_LOGIC = new StdLogic();

   private static class StdULogic extends SubType {
    StdULogic() { super(new SimpleName("std_ulogic")); }
  }
  public static final SubType STD_ULOGIC = new StdULogic();

  private static class _Boolean extends SubType {
    _Boolean() { super(new SimpleName("boolean")); }
  }
  public static final SubType BOOLEAN = new _Boolean();

  private static class _Integer extends SubType {
    _Integer() { super(new SimpleName("integer")); }
  }
  public static final SubType INTEGER = new _Integer();

  private static class Real extends SubType {
    Real() { super(new SimpleName("real")); }
  }
  public static final SubType REAL = new Real();

  // is this a vector?
  private static class Character extends SubType {
    Character() { super(new SimpleName("character")); }
  }
  public static final SubType CHARACTER = new Character();

  private static class Time extends SubType {
    Time() { super(new SimpleName("time")); }
  }
  public static final SubType TIME = new Time();


  public SubType(Name function, Name type, Constraint constraint) {
    _function = function;
    _type = type;
    _constraint = constraint;
  }

  public SubType(Name type, Constraint constraint) {
    this(null, type, constraint);
  }

  public SubType(Name type) {
    this(null, type, null);
  }


  
  public static SubType VECTOR(int start, int stop) {
    return new _Vector(start, stop);
  }

  public static SubType BIT_VECTOR(int start, int stop) {
    return new BitVector(start, stop);
  }

  public static SubType STD_LOGIC_VECTOR(int start, int stop) {
    return new StdLogicVector(start, stop);
  }

  public static SubType STD_LOGIC_VECTOR(Object start, 
					 String direction, 
					 Object stop) {
    return new StdLogicVector(start, direction, stop);
  }

  public static SubType STD_ULOGIC_VECTOR(int start, int stop) {
    return new StdULogicVector(start, stop);
  }

  public static SubType STD_ULOGIC_VECTOR(Object start, 
					  String direction,
					  Object stop) {
    return new StdULogicVector(start, direction, stop);
  }


  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    if (_function != null) {
      ((VHDLout)_function).toVHDL(s,"");
      s.append(" ");
    }
    ((VHDLout)_type).toVHDL(s,"");
    if (_constraint != null) {
      //s.append(" ");
      ((VHDLout)_constraint).toVHDL(s,"");
    }
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(),"").toString();
  }

  public static String direction(int start, int stop) {
    if (start > stop) {
      return Range.DOWNTO;
    } else {
      return Range.TO;
    }
  }
}

  
class _Vector extends SubType {
  _Vector(int start, int stop) { 
    super(new SimpleName("vector"), 
	  new IndexConstraint(new Range(new NumericLiteral(start),
					direction(start,stop),
					new NumericLiteral(stop))));
  }
  }

class BitVector extends SubType {
  BitVector(int start, int stop) { 
    super(new SimpleName("bit_vector"), 
	  new IndexConstraint(new Range(new NumericLiteral(start),
					direction(start,stop),
					new NumericLiteral(stop))));
    }
}

class StdLogicVector extends SubType {
  StdLogicVector(int start, int stop) { 
    this(new NumericLiteral(start), direction(start, stop),
	 new NumericLiteral(stop));
  }

  StdLogicVector(Object se_start, 
		 String direction,
		 Object se_stop) { 
    super(new SimpleName("std_logic_vector"), 
	  new IndexConstraint(new Range(se_start, direction,
					se_stop)));
    }
  
}

class StdULogicVector extends SubType {
  StdULogicVector(int start, int stop) { 
    this(new NumericLiteral(start), direction(start, stop),
	 new NumericLiteral(stop));
  }

  StdULogicVector(Object se_start, 
		 String direction,
		 Object se_stop) { 
    super(new SimpleName("std_ulogic_vector"), 
	  new IndexConstraint(new Range(se_start, direction,
					se_stop)));
  }
}


  



  









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


package fp.flowgraph;

import java.util.*;

/**
 * Type is used to describe different kinds of operands.  In LLVM,
 * operands are not typed directly, but are typed by their use in
 * conjunction with a particular operator.  In an Instruction, the
 * operator in combination with the Type field determine what the
 * operands should be.
 * 
 * However, this approach generates a few different questions:
 * 
 * 1. Is it forbidden to have the same operand be used as two
      different types?
 * 
 * In general, I would think yes.  If the operand is to be used
 * different it has to have a cast between the different "typed"
 * operand uses.  LLVM may have a different opinion about this.
 * 
 * 2. ...
 * 
 * @author Justin L. Tripp
 * @see Instruction
 */
public class Type {

  /* Should we make types??

  Well, I definitely think there should be a small set of fixed types.
  LLVM defines a tleast 13 primitive types.  Then there are derived 
  types arrays, structs, etc.  I think the types should be static
  and so we probably should have a hash to see if a given type 
  exists and return the instance.

  Other types are:

  Arrays
  Function types
  Structure types
  Pointer types


  What do we do for widths?  Are there going to be anywidths?  What
  that really means is that are we going to interact with the outside
  world?  The "outside" requires certain widths and behavior ...

  */

  // data members
  private final int _width;
  private final int _traits;
  private final String _name;

  private HashMap _types = new HashMap();
  
  public final static int NONE        = 0x00000000;
  public final static int UNSIGNED    = 0x00000001;
  public final static int SIGNED      = 0x00000002;
  public final static int INTEGER     = 0x00000004;
  public final static int INTEGRAL    = 0x00000008;
  public final static int FLOAT       = 0x00000010;
  public final static int DOUBLE      = 0x00000020;
  public final static int FIRSTCLASS  = 0x00000040;
  public final static int PRIMITIVE   = 0x00000080;
  public final static int ARRAY       = 0x00000200;
  public final static int LABEL       = 0x00000200;
  public final static int STRUCT      = 0x00000400;
  public final static int POINTER     = 0x00000800;
  

  /**
   * Constructor for making a dynamic or non-standard type.  Most used
   * types already exist, but the constructor allows for more types to
   * be created.  This should be used sparingly and only to create
   * width specific types not already provided.
   * 
   * @param name Name of the type
   * @param width width of the type
   * @param traits special type traits (see above)
   */
  protected Type(String name, int width, int traits) {
    _name = name;
    _width = width;
    _traits = traits;
    _types.put(name, this);
  }

  public String getHash() {
    return _name;
  }
  
  public int getWidth() {
    return _width; 
  }

  public boolean isUnsigned() { 
    return (_traits & UNSIGNED) != 0;
  }

  public boolean isSigned() { 
    return (_traits & SIGNED) != 0;
  }

  public boolean isInteger() { 
    return (_traits & INTEGER) != 0;
  }

  public boolean isIntegral() { 
    return (_traits & INTEGRAL) != 0;
  }

  public boolean isFloat() { 
    return (_traits & FLOAT) != 0;
  }

  public boolean isDouble() { 
    return (_traits & DOUBLE) != 0;
  }

  public boolean isPrimitive() { 
    return (_traits & PRIMITIVE) != 0;
  }

    public boolean isArray() { 
    return (_traits & ARRAY) != 0;
  }

  public boolean isLabel() { 
    return (_traits & LABEL) != 0;
  }

  public boolean isStruct() { 
    return (_traits & STRUCT) != 0;
  }

  public boolean isPointer() { 
    return (_traits & POINTER) != 0;
  }

  public boolean isFirstClass() { 
    return (_traits & FIRSTCLASS) != 0;
  }



  public final static Type Void = 
    new Type("void", 0, PRIMITIVE);

  public final static Type Bool = 
    new Type("bool", 1, PRIMITIVE | FIRSTCLASS | INTEGRAL);
  
  public final static Type Ubyte = 
    new Type("ubyte", 8, 
	     UNSIGNED | PRIMITIVE | FIRSTCLASS | INTEGER | INTEGRAL );
  
  public final static Type Sbyte = 
    new Type("sbyte", 8, 
	     SIGNED | PRIMITIVE | FIRSTCLASS | INTEGER | INTEGRAL );
  
  public final static Type Ushort = 
    new Type("ushort", 16,
	     UNSIGNED | PRIMITIVE | FIRSTCLASS | INTEGER | INTEGRAL );
  
  public final static Type Short = 
    new Type ("short", 16, 
	      SIGNED | PRIMITIVE | FIRSTCLASS | INTEGER | INTEGRAL );

  public final static Type Uint = 
    new Type ("uint", 32,
	      UNSIGNED | PRIMITIVE | FIRSTCLASS | INTEGER | INTEGRAL );
  
  public final static Type Int = 
    new Type("int", 32,
	     SIGNED | PRIMITIVE | FIRSTCLASS | INTEGER | INTEGRAL );

  public final static Type Ulong = 
    new Type("ulong", 64,
	     UNSIGNED | PRIMITIVE | FIRSTCLASS | INTEGER | INTEGRAL );

  public final static Type Long =
    new Type("long", 64,
	     SIGNED | PRIMITIVE | FIRSTCLASS | INTEGER | INTEGRAL);

  public final static Type Float = 
    new Type("float", 32,
	     SIGNED | PRIMITIVE | FIRSTCLASS | FLOAT);

  public final static Type Double = 
    new Type("double", 64,
	     SIGNED | PRIMITIVE | FIRSTCLASS | DOUBLE);

  // ??
  public final static Type Label =
    new Type("label", 0,
	     LABEL | PRIMITIVE);
  
  public final static Type None = null;


  public Type getType(String s) { 
    return (Type)_types.get(s);
  }


  public String toString() {
    return _name;
  }

}
 





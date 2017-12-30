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

public class ArrayType extends Type {

  private Type _type;
  private int _len;

  public ArrayType(Type type, int len) {
    super(getHash(type), type.getWidth(), ARRAY);
    _type = type;
    _len = len;
  }

  public Type getType() { return _type; }
  public int getLen() { return _len; }

  public int getWidth() { 
    return (_len * _type.getWidth()); 
  }

  public static String getHash(Type type) { 
    return "array_"+type.getHash() ;
  }

  // needs fancy code for toString()

}
    

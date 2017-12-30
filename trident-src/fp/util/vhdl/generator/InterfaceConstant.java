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

public class InterfaceConstant implements VHDLout, InterfaceDeclaration {

  private IdentifierList _identifiers;
  private SubType _type;
  private Expression _expression;

  public InterfaceConstant(IdentifierList ids, SubType type, Expression exp) {
    if (ids != null) 
      _identifiers = ids;
    else
      _identifiers = new IdentifierList();
    _type = type;
    _expression = exp;
  }

  public InterfaceConstant(IdentifierList ids, SubType type) {
    this(ids, type, null);
  }
  
  public InterfaceConstant(Identifier id, SubType type) {
    this(new IdentifierList(id), type, null);
  }

  
  public InterfaceConstant(Identifier id, SubType type, Expression exp) {
    this(new IdentifierList(id), type, exp);
  }

  public void addIds(Identifier id) {
    _identifiers.add(id);
  }
  
  public StringBuffer toVHDL(StringBuffer s, String pre) {
    // label top.
    s.append(pre);
    // s.append("signal "); // ?? is allowed
    s.append("constant ");
    _identifiers.toVHDL(s,"");
    s.append(" : in ");
    _type.toVHDL(s,"");
    if (_expression != null) {
      s.append(" := ");
      _expression.toVHDL(s,"");
    }

    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }



}  

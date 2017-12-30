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

public class InterfaceVariable implements VHDLout, InterfaceDeclaration {

  private IdentifierList _identifiers;
  private Mode _mode;
  private SubType _type;
  private Expression _expression;

  public InterfaceVariable(IdentifierList ids, Mode mode, SubType type,
			   Expression exp) {
    if (ids != null) 
      _identifiers = ids;
    else
      _identifiers = new IdentifierList();

    _mode = mode;
    _type = type;
    _expression = exp;
  }

  public InterfaceVariable(IdentifierList ids, Mode mode, SubType type) {
    this(ids, mode, type, null);
  }
  
  public InterfaceVariable(IdentifierList ids, SubType type) {
    this(ids, null, type, null);
  }

  public InterfaceVariable(Identifier id, SubType type) {
    this(new IdentifierList(id), null, type, null);
  }

  public void addIds(Identifier id) {
    _identifiers.add(id);
  }
  
  public StringBuffer toVHDL(StringBuffer s, String pre) {
    // label top.
    s.append(pre);
    // s.append("signal "); // ?? is allowed
    // s.append("variable ");
    // ModelSim does not like variable.
    _identifiers.toVHDL(s,"");
    s.append(" : ");
    if (_mode != null) {
      _mode.toVHDL(s,"");
      s.append(" ");
    }
    _type.toVHDL(s,"");
    if (_expression != null) {
      _expression.toVHDL(s,"");
    }

    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }



}  

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

public class TypeDeclaration implements VHDLout, BlockItem, EntityItem {

  /*
    Two kinds full type declaration and incomplete.  Not super different.
    Incomplete just has less information.  I combine them here.  If it
    really matters I can provide a wrapper class for both.
  */

  private Identifier _name;
  private TypeDefinition _def;
  
  private SubType _type;


  public TypeDeclaration(Identifier type, TypeDefinition def) {
    _name = type;
    _def = def;
  }

  // this may not be fancy enough ...
  public SubType getType() {
    if (_type == null) {
      _type = new SubType(new SimpleName(_name));
    }
    return _type;
  }

  public LinkedList getNames() {
    LinkedList names = new LinkedList();
    names.add(_name.getId());
    return names;
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    // label top.

    s.append(pre).append("type ");
    _name.toVHDL(s,"");
    if (_def != null) {
      s.append(" is ");
      ((VHDLout)_def).toVHDL(s, "");
    }
    s.append(";\n"); 
    
    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }



}  

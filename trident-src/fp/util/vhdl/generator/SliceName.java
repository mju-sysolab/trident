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

// Identifier DOES implement VHDLout, so SimpleName does as well, since
// it extends Identifier.  Thus SimpleNames can be used where VHDLout 
// is required.


public class SliceName implements Name, Primary, VHDLout {

  // prefix
  private Name _name;
  private FunctionCall _function;
  // discrete range
  private DiscreteRange _range;


  public SliceName(Name n, DiscreteRange r) {
    _name = n;
    _range = r;
  }

  public SliceName(Name n, int a, int b) {
    this(n, new DiscreteRange(a,b));
  }

  public SliceName(FunctionCall f, DiscreteRange r) {
    this((Name)null, r);
    _function = f;
  }

  public String getName() {
    return _name.getName();
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    if (_name != null)
      s.append(_name.getName());
    else
      _function.toVHDL(s,"");
    s.append("(");
    _range.toVHDL(s,"");
    s.append(")");

    return s;
  }
  
  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }
  
}

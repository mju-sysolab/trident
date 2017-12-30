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


public class Association implements VHDLout {
  private FormalPart _formal;
  private ActualPart _actual;

  public Association(FormalPart fp, ActualPart ap) {
    _formal = fp;
    _actual = ap;
  }

  public Association(ActualPart ap) {
    this((FormalPart)null, ap);
  }

  public Association(Name name) {
    this((FormalPart)null, new ActualPart(name));
  }

  public Association(Name formal, Object actual) {
    this(new FormalPart(formal), new ActualPart(new Expression(actual)));
  }

  public Association(Name formal, Name actual, Expression e) {
    this(new FormalPart(formal), new ActualPart(actual, e));
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    // these extra carriage returns may not bee the best idea...
    s.append(pre);
    if (_formal != null) {
      _formal.toVHDL(s,pre);
      s.append(" => ");
    }
    _actual.toVHDL(s,pre);
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(),"").toString();
  }

}

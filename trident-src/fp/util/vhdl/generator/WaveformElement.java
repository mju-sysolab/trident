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

public class WaveformElement implements VHDLout {

  // expression [ after time_expression ]
  // null [after time_expression ]

  private Expression _value;
  private Expression _time;

  public WaveformElement(Expression value, Expression time) {
    _value = value;
    _time = time;
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    int count = 0;
    if (_value != null) {
      ((VHDLout)_value).toVHDL(s, "");
    } else {
      s.append("null");
    }
    if (_time != null) {
      s.append(" after ");
      ((VHDLout)_time).toVHDL(s, "");
    }
    return s;
  }
  
  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }


   
}

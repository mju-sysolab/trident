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

public class Range implements VHDLout {

  // _attribute_name
  private SimpleExpression _a;
  private String _direction;
  private SimpleExpression _b;

  public static final String TO = "to";
  public static final String DOWNTO = "downto";
  public static final String INDEX = "index";


  public Range(Object a, String direction, Object b) {
    _a = new SimpleExpression(a);
    _direction = direction;
    _b = new SimpleExpression(b);
  }

  public Range(int a, int b) {
    this(new NumericLiteral(a), direction(a,b), new NumericLiteral(b));
  }
  
  public static String direction(int start, int stop) {
    if (start > stop) {
      return DOWNTO;
    } else if (start ==stop) {
      return INDEX;
    } else {
      return TO;
    }
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    _a.toVHDL(s,"");
    if (_direction != INDEX) {
      s.append(" ").append(_direction).append(" ");
      _b.toVHDL(s,"");
    }
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }
    


}


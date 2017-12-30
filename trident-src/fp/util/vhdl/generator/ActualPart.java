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


public class ActualPart implements VHDLout {

  public static final SimpleName OPEN = new SimpleName("open");

  Expression _expression;
  Name _designator;
  Name _function_type;

  ActualPart(Expression expression, Name functor, Name design) {
    _expression = expression;
    _designator = design;
    _function_type = functor;
  }

  public ActualPart(Name designator) {
    this(null, null, designator);
  }

  public ActualPart(Expression e) {
    this(e, null, null);
  }

  public ActualPart(Name a, Name b) {
    this(null, a, b);
  }

  public ActualPart(Name a, Expression e) {
    this(e, a, null);
  }

 public StringBuffer toVHDL(StringBuffer s, String pre) {
    // these extra carriage returns may not bee the best idea...
    s.append(pre);
    if (_expression != null) {
      if (_designator != null && _function_type == null) {
	s.append(_designator).append("( ");
	_expression.toVHDL(s,"");
	s.append(" )");
      } else {
	_expression.toVHDL(s,"");
      }
      
    } else {
      if (_function_type != null) 
	s.append(_function_type).append("(");
      s.append(_designator);
      if (_function_type != null) s.append(")");
    }
    return s;
  }

  public String toString() {
    return toVHDL(new StringBuffer(),"").toString();
  }



}

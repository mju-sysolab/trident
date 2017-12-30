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



public class AttributeName implements Name, Primary, VHDLout {
  // prefix (Name | Functional Call)
  private Name _prefix_name;
  private FunctionCall _prefix_func;
  // signature -- skipped for now
  private SimpleName _designator;
  private Expression _expression;

  public static final SimpleName EVENT  = new SimpleName("event");
  public static final SimpleName HIGH   = new SimpleName("high");
  public static final SimpleName LOW    = new SimpleName("low");
  public static final SimpleName ACTIVE = new SimpleName("active");



  AttributeName(Name name, FunctionCall func, Object obj,
		SimpleName designator, Expression exp) {
    if (name != null) {
      _prefix_name = name;
    } else {
      _prefix_func = func;
    }

    _designator = designator;
    _expression = exp;
  }

  // If I need expressions, I will add them.

  public AttributeName(Name name, SimpleName designator) {
    this(name, null, null, designator, null);
  }

  public AttributeName(FunctionCall func, SimpleName designator) {
    this(null, func, null, designator, null);
  }

  public String getName() {
    return toString();
  }

  
  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);
    if (_prefix_func == null) {
      ((VHDLout)_prefix_name).toVHDL(s,"");
    } else {
      ((VHDLout)_prefix_func).toVHDL(s,"");
    }
    // signature goes here 
    s.append("'");
    _designator.toVHDL(s,"");
    
    if (_expression != null) {
      s.append("( ");
      _expression.toVHDL(s,"");
      s.append(" )");
    }

    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }




  
  public static void main(String args[]) {
    checkId("Bob",HIGH);
    checkId("B_56ABab",EVENT);
    // illegal
    checkId("5_abcde",LOW);
    checkId("abcde%",ACTIVE);
    checkId("abcde.",LOW);
  }

  public static void checkId(String name, SimpleName event) {
    SimpleName a = new SimpleName(name);
    System.out.println("AttributeName "+(new AttributeName(a,event)));
  }


}

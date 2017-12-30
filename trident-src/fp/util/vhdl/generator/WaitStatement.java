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

public class WaitStatement extends Statement implements SequentialStatement, VHDLout {
  
  private SensitivityList _sensitivity;
  private Expression _conditions;
  private Expression _timeout;

  
  public WaitStatement(Identifier label, SensitivityList sensitivity, 
		       Expression conditions, Expression timeout ) {
    super("wait", label);

    if (sensitivity == null) 
      _sensitivity = new SensitivityList();
    else 
      _sensitivity = sensitivity;

    _conditions = conditions;
    _timeout = timeout;
  }

  public WaitStatement(SensitivityList sensitivity, 
		       Expression conditions, Expression timeout) {
    this(null, sensitivity, conditions, timeout);
  }
  
  public WaitStatement(SensitivityList sensitivity, 
		       Expression conditions) {
    this(null, sensitivity, conditions, null);
  }

  public WaitStatement(Expression conditions) {
    this(null, null, conditions, null);
  }

  public WaitStatement(Expression conditions, Expression timeout) {
    this(null, null, conditions, timeout);
  }

  public WaitStatement() {
    this(null, null, null, null);
  }
  
  public void addSignal(Name signal) {
    _sensitivity.add(signal);
  }

  public void setCondition(Expression condition) {
    _conditions = condition;
  }

  public void setTimeOut(Expression timeout) {
    _timeout = timeout;
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    // label top.
    s.append(pre);
    if (getLabel() != null) {
      getLabel().toVHDL(s,"");
      s.append(": ");
    }

    s.append("wait ");
    
    if (_sensitivity.size() > 0) {
      s.append("on ");
      _sensitivity.toVHDL(s,"");
      s.append(" ");
    }

    if (_conditions != null) {
      s.append("until ");
      _conditions.toVHDL(s, "");
      s.append(" ");
    }

    if (_timeout != null) {
      s.append("for ");
      _timeout.toVHDL(s, "");
      s.append(" ");
    }

    s.append(";\n");

    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }

  
  
  public static void main(String args[]) {
    // mult div mod rem

    SimpleName a = new SimpleName("a");
    SimpleName b = new SimpleName("b");
    SimpleName c = new SimpleName("c");
    SimpleName d = new SimpleName("d");
    SimpleName clk = new SimpleName("clk");

    Identifier my_wait = new SimpleName("my_wait");

    Expression ex = new Expression(new Eq(clk, Char.ONE));
    Expression ex1 = new Expression(new Gt(new Add(a,c), b));
    SensitivityList sl = new SensitivityList();
    sl.add(clk);


    WaitStatement wait = new WaitStatement(ex);
    print(wait);

    wait = new WaitStatement(sl,ex1);
    print(wait);

    wait = new WaitStatement(my_wait, sl, ex, null);
    print(wait);


  }

  public static void print(WaitStatement f) {
    System.out.println("WaitStatement: "+f);
  }


  



}

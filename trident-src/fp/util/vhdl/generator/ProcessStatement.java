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

public class ProcessStatement extends Statement implements VHDLout, ConcurrentStatement {
  
  private SensitivityList _sensitivity;
  boolean postponed = false;
  // process declarative part (skipping)
  private LinkedList _statements;
  
  public ProcessStatement(Identifier label) {
    super("process", label);

    _sensitivity = new SensitivityList();
    _statements = new LinkedList();
  }

  public ProcessStatement() {
    this(null);
  }


  public Name addSignal(Name name) {
    _sensitivity.add(name);
    return name;
  }

  public SequentialStatement addStatement(SequentialStatement state) {
    _statements.add(state);
    return state;
  }
  

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    // label top.
    Identifier label = getLabel();
    if (label != null) {
      s.append(pre);
      pre += TAB;
      label.toVHDL(s,"");
      s.append(":\n");
    }

    s.append(pre);
    if (postponed) {
      s.append("postponed ");
    }
    s.append("process ");
    _sensitivity.toVHDL(s,"");
    s.append(" is\n");
    // declarative part;
    s.append(pre).append("begin").append("\n");

    for(ListIterator list_iter = _statements.listIterator(); list_iter.hasNext(); ) {
      Statement st = (Statement)list_iter.next();
      ((VHDLout)st).toVHDL(s,pre+TAB);
    }

    s.append(pre).append("end ");
    if (postponed) s.append("postponed ");
    s.append("process ");
    if (label != null) {
      label.toVHDL(s,"");
    }
    s.append(";\n"); //?
    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }


    public static void main(String args[]) {
    // mult div mod rem

    SimpleName d = new SimpleName("d");
    SimpleName q = new SimpleName("q");
    SimpleName reset = new SimpleName("reset");
    SimpleName enable = new SimpleName("enable");
    SimpleName clk = new SimpleName("clk");

    // Flip-flop with enable.
    ProcessStatement p = new ProcessStatement(new SimpleName("dff"));
    p.addSignal(clk);
    //p.addSignal(reset);

    IfStatement if1 = new IfStatement();
    p.addStatement(if1);

    SequentialStatementList if1_statements = new SequentialStatementList();
    if1.addElseIf( new Paren(new And(new AttributeName(clk, AttributeName.EVENT),
				     new Eq(clk,Char.ONE))),
		   if1_statements);
    IfStatement if2 = new IfStatement();
    if1_statements.add(if2);

    LinkedList if2_statements = if2.addElseIf(new Paren(new Eq(enable,Char.ONE)));
    if2_statements.add(new SignalAssignment(q, new Waveform(d)));
    
    print(p);

    p = new ProcessStatement(new SimpleName("dff_reset"));
    p.addSignal(clk);
    p.addSignal(reset);

    IfStatement if3 = (IfStatement)p.addStatement(new IfStatement());
    LinkedList if3_state_1 = if3.addElseIf(new Expression(new Eq(reset,Char.ONE)));
    if3_state_1.add(new SignalAssignment(q,new Waveform(Char.ZERO)));
    LinkedList if3_state_2 = if3.addElseIf(new Paren(new And(new AttributeName(clk, AttributeName.EVENT),
							     new Eq(clk,Char.ONE))));
    if3_state_2.add(new SignalAssignment(q,new Waveform(d)));
    
    print(p);

  }

  public static void print(ProcessStatement f) {
    System.out.println("ProcessStatement:\n"+f);
  }






}

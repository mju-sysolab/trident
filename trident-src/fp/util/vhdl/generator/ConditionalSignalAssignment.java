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

public class ConditionalSignalAssignment extends Statement 
  implements VHDLout, ConcurrentStatement {
  
  /* Concurrent Signal Assignment
     
  concurrent_signal_assignment <= 
     [label:] [ postponed ] conditional signal assignment 
   | [label:] [ postponed ] selected signal assignment

  conditional signal assignment <=
    ( name | aggregate ) '<=' 
        [ guarded ] [ delay_mechanism ] 
          { waveform when boolean_expression else }
          waveform [ when boolean_expression ];

  selected signal assignment <=
    with expression select
       ( name | aggregate ) '<='
           [ guarded ] [ delay_mechanisim ]
                  { waveform when choices, }
                waveform when choices;
		
  */

  // Name could be many things...
  Name _target;

  // options (like guarded or delay mechanism
  // skip options for now.

  LinkedList _conditionals;

  public ConditionalSignalAssignment(Identifier label, Name target) {
    super("csa", label);
    _target = target;
    _conditionals = new LinkedList();
  }

  public ConditionalSignalAssignment(Name target) {
    super("csa", null);
    _target = target;
    _conditionals = new LinkedList();
  }

  class WaveCondition {
    Waveform _wave;
    Expression _condition;

    WaveCondition(Waveform wave, Expression condition) {
      _wave = wave;
      _condition = condition;
    }
  }


  public void addCondition(Waveform wave, Expression eq) {
    _conditionals.add(new WaveCondition(wave, eq));
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre);

    Identifier label = getLabel();
    if (label != null) {
      label.toVHDL(s,"");
      s.append(": ");
    }
      
    ((VHDLout)_target).toVHDL(s,"");
    s.append(" <= ");
    int size = _conditionals.size();
    int count = 0;
    for(ListIterator list_iter = _conditionals.listIterator(); list_iter.hasNext(); ) {
      WaveCondition wc = (WaveCondition)list_iter.next();
      wc._wave.toVHDL(s,"");
      if (wc._condition != null) {
	s.append(" when ");
	wc._condition.toVHDL(s,"");
	s.append(" ");
	count++;
	if (count < size) {
	  s.append("else ");
	}
      } else {
	s.append(" ");
      }
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

    SimpleName a = new SimpleName("a");
    SimpleName a1 = new SimpleName("a_1");
    SimpleName b = new SimpleName("b");
    SimpleName c = new SimpleName("c");

    System.out.println("Primaries "+a+" "+b+" "+c+"\n");

    ConditionalSignalAssignment csa = new ConditionalSignalAssignment(a);
    csa.addCondition(new Waveform(b), null);
    print(csa);

    csa = new ConditionalSignalAssignment(b);
    csa.addCondition(new Waveform(b), new Expression(new Relation(new Add(a,c),
								  Operation.EQ,
								  a1)));
    print(csa);
    // how do I write constants???
    
    SimpleName cell_sel = new SimpleName("cell_1_0_CellSelect");
    SimpleName addr_sel = new SimpleName("AddressSelect");
    SimpleName sel_cell = new SimpleName("SelectCell");
    Char one = Char.ONE;
    Char zero = Char.ZERO;
    AssociationList params = new AssociationList();
    params.add(new SimpleName("7"));
    params.add(new SimpleName("4"));
    FunctionCall fc = new FunctionCall(new SimpleName("conv_std_logic_vector"),
				       params);
    csa = new ConditionalSignalAssignment(cell_sel);
    csa.addCondition(new Waveform(one), new And(new Eq(addr_sel, fc), 
						new Eq(sel_cell, one)));
    csa.addCondition(new Waveform(zero), null);
    print(csa);
    


  }

  public static void print(ConditionalSignalAssignment f) {
    System.out.println("Conditional Assign: "+f);
  }


  

}

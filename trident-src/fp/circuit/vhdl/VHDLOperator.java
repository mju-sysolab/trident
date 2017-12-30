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
package fp.circuit.vhdl;

import java.util.*;
import java.math.BigInteger;

import fp.circuit.Circuit;
import fp.circuit.Constant;
import fp.circuit.Node;
import fp.circuit.Operator;
import fp.circuit.PortTag;

import fp.util.vhdl.generator.*;

import fp.synthesis.SynthesisException;


public class VHDLOperator extends Operator {

  VHDLOperator(Circuit parent, String name, fp.circuit.Operation type) {
    super(parent, name, type);
    
  }

  public void build(String name, Object[] arg_o) {

    fp.circuit.Operation type = getOperation();
    //System.out.println("Building VHDLOperator "+getName());

    VHDLCircuit parent = (VHDLCircuit)getParent();

    DesignUnit du = parent.getVHDLParent();
    LibraryUnit lu = du.getLibraryUnit();
    //Entity e = lu.getEntity();
    Architecture a = lu.getArchitecture();

    HashMap modules = parent.getModules();
    // here is where VHDLModules are built ...
    VHDLModule module = (VHDLModule)modules.get(type.getName());
    if (module != null) {
      module.build(name,du,this);

    } else if (type == fp.circuit.Operation.EXPAND) {
      

      // one input, many outputs
      Iterator iter = getInPorts().iterator();
      VHDLNet in0 = (VHDLNet)((PortTag)iter.next()).getNet();
      int width = in0.getWidth();

      for(int i = 0; i < width; i++) {
	PortTag pt = (PortTag)findOutPort("out"+(i));
	if(pt == null)
	  continue;
	
	VHDLNet net = (VHDLNet) pt.getNet();

	ConditionalSignalAssignment csa =
	  new ConditionalSignalAssignment(net.getVHDLName());
	// add it early, does not matter much.
	a.addStatement(csa);          
	csa.addCondition(new Waveform(new IndexedName(in0.getVHDLName(),
	    new Expression(new NumericLiteral(i)))), null);
      }
    } else {
    
      // need to get the name of the out edge.
      PortTag out = (PortTag)getOutPorts().iterator().next();
      VHDLNet net = (VHDLNet)out.getNet();
      
      ConditionalSignalAssignment csa = 
	new ConditionalSignalAssignment(net.getVHDLName());
      // add it early, does not matter much.
      a.addStatement(csa);
      
      // this should be the big switchObject -- but not yet.
      if (type == fp.circuit.Operation.ADD) {
	// lets see, add has two operands, maybe three, 
	// order does not matter unless there is a third.

	Iterator iter = getInPorts().iterator();
	VHDLNet in0 = (VHDLNet)((PortTag)iter.next()).getNet();
	VHDLNet in1 = (VHDLNet)((PortTag)iter.next()).getNet();
	
	csa.addCondition(new Waveform(new Add((VHDLout)in0.getVHDLName(), 
					   (VHDLout)in1.getVHDLName())),null);

      } else if (type == fp.circuit.Operation.SUB ) {
	
	VHDLNet in0 = (VHDLNet)((PortTag)findInPort("in0")).getNet();
	VHDLNet in1 = (VHDLNet)((PortTag)findInPort("in1")).getNet();
      
	csa.addCondition(new Waveform(new Sub((VHDLout)in0.getVHDLName(), 
					    (VHDLout)in1.getVHDLName())),null);
      } else if (type == fp.circuit.Operation.SLICE) {

	VHDLNet in0 = (VHDLNet)((PortTag)findInPort("in0")).getNet();
	
	int const_in1 = -1;
	VHDLNet in1 = (VHDLNet)((PortTag)findInPort("in1")).getNet();
	for (Iterator net_iter = in1.getSources().iterator(); 
	     net_iter.hasNext();) {
	  PortTag pt = (PortTag) net_iter.next();
	  Node pt_parent = pt.getParent();
	  if (pt_parent instanceof Constant) {
	    const_in1 = ((Constant)pt_parent).getValueInt();
	  }
	}

	int const_in2 = -1;
	VHDLNet in2 = (VHDLNet)((PortTag)findInPort("in2")).getNet();
	for (Iterator net_iter = in2.getSources().iterator(); 
	     net_iter.hasNext();) {
	  PortTag pt = (PortTag) net_iter.next();
	  Node pt_parent = pt.getParent();
	  if (pt_parent instanceof Constant) {
	    const_in2 = ((Constant)pt_parent).getValueInt();
	  }
	}

	if (Math.abs(const_in1 - const_in2) > 0) {
	  csa.addCondition(new Waveform(new SliceName(in0.getVHDLName(), 
	      const_in1, const_in2)), null); 
	} else if (const_in1 - const_in2 == 0) {
	  csa.addCondition(new Waveform(new IndexedName(in0.getVHDLName(), 
	  new Expression(new NumericLiteral(1)))), null); 
	}

      } else if (type == fp.circuit.Operation.CONCAT) {

	SimpleExpression se = null;
	int count = getInPorts().size();
	
	for(int i = count; i > 0; i--) {
	  VHDLNet in = (VHDLNet)((PortTag)findInPort("in"+(i-1))).getNet();
	  if (se == null) {
	    se = new Concat(in.getVHDLName());
	  } else 
	    se.concat(in.getVHDLName());
	}
	csa.addCondition(new Waveform(se),null);


      } else if (type == fp.circuit.Operation.AND) {

      // logics could be folded into one big thing.
      int size = getInPorts().size();
      Iterator iter = getInPorts().iterator(); 

      LinkedList list = new LinkedList();
      for(int i = 0; i < size; i++ ) {
	VHDLNet in0 = (VHDLNet)((PortTag)iter.next()).getNet();
	list.add(in0.getVHDLName());
      }

      csa.addCondition(new Waveform(new And(list)), null);
    } else if (type == fp.circuit.Operation.BUF) {
      Iterator iter = getInPorts().iterator();
      VHDLNet in0 = (VHDLNet)((PortTag)iter.next()).getNet();

      // simple.
      csa.addCondition(new Waveform(in0.getVHDLName()), null);
    } else if (type == fp.circuit.Operation.OR) {
      int size = getInPorts().size();
      Iterator iter = getInPorts().iterator(); 

      LinkedList list = new LinkedList();
      for(int i = 0; i < size; i++ ) {
	VHDLNet in0 = (VHDLNet)((PortTag)iter.next()).getNet();
	list.add(in0.getVHDLName());
      }
      csa.addCondition(new Waveform(new Or(list)), null);

    } else if (type == fp.circuit.Operation.XOR) {
      int size = getInPorts().size();
      Iterator iter = getInPorts().iterator(); 

      LinkedList list = new LinkedList();
      for(int i = 0; i < size; i++ ) {
	VHDLNet in0 = (VHDLNet)((PortTag)iter.next()).getNet();
	list.add(in0.getVHDLName());
      }
      csa.addCondition(new Waveform(new Xor(list)), null);

    } else if (type == fp.circuit.Operation.NOT) {
      //int size = getInPorts().size();
      Iterator iter = getInPorts().iterator(); 
      VHDLNet in0 = (VHDLNet)((PortTag)iter.next()).getNet();
      csa.addCondition(new Waveform(new Not(in0.getVHDLName())),null);

    } else if (type == fp.circuit.Operation.MULT) {
      /*
	Different than VHDL additions, a VHDL multiply is twice
	the size of its inputs.  This is due to the fact that if
	you multiply two N bit numbers, you end up with a 2N sized
	result.  However, this is not the behavior in C or on
	microprocessor in general.  So, we are adding a wire to
	capture the 2N result and pass only the bottom N bits.
	
	This may not work for single bit multiplies ...
      */
      Iterator iter = getInPorts().iterator();
      VHDLNet in0 = (VHDLNet)((PortTag)iter.next()).getNet();
      VHDLNet in1 = (VHDLNet)((PortTag)iter.next()).getNet();

      int width = in0.getWidth();

      // get name, relying on the lack of illegal unique names.
      SimpleName mult_wire = new SimpleName(parent.getUniqueWireName());

      
      // add wire to architecture ...
      a.addItem(new Signal(mult_wire, 
			   SubType.STD_LOGIC_VECTOR((2*width) - 1, 0)));

      // add index selection
      // is this robust ?
      csa.addCondition(new Waveform(new SliceName(mult_wire, width-1, 0)),
		       null);

      // add statement
      ConditionalSignalAssignment mult_csa = 
	new ConditionalSignalAssignment(new SimpleName(mult_wire));
      // add it early, does not matter much.
      a.addStatement(mult_csa); 

      mult_csa.addCondition(new Waveform(new Mult(in0.getVHDLName(), 
					     in1.getVHDLName())),null);
      


    } else if (type == fp.circuit.Operation.MUX) {
      int size = getInPorts().size();
      Iterator iter = getInPorts().iterator(); 
      
      VHDLNet in0, in1, s;
      PortTag tag = findInPort("in0");
      
      if (tag == null) {
	throw new SynthesisException("Operator "+getName()
				     +" Mux without in0 input - out "+
				     out+" out net "+net+"\n"+this.toString());
      }

      in0 = (VHDLNet)tag.getNet();
      tag = findInPort("in1");
      in1 = (VHDLNet)tag.getNet();
      tag = findInPort("s");
      s = (VHDLNet)tag.getNet();

      // in0 when s = 0
      csa.addCondition(new Waveform(in0.getVHDLName()), 
		       new Expression(new Eq(s.getVHDLName(), Char.ZERO)));
      // else in1 when s = 1;
      csa.addCondition(new Waveform(in1.getVHDLName()), 
		       new Expression(new Eq(s.getVHDLName(), Char.ONE)));
    } else if (type.getName().equals("aaa_setlt")) {

      VHDLNet in0, in1;
      PortTag tag = findInPort("in0");
      in0 = (VHDLNet)tag.getNet();
      tag = findInPort("in1");
      in1 = (VHDLNet)tag.getNet();

      FunctionCall fc0 = new FunctionCall(new SimpleName("signed"));
      fc0.add(in0.getVHDLName());

      FunctionCall fc1 = new FunctionCall(new SimpleName("signed"));
      fc1.add(in1.getVHDLName());

      csa.addCondition(new Waveform(Char.ONE),
		       new Expression(new Lt(fc0, fc1)));
      csa.addCondition(new Waveform(Char.ZERO), null);

    } else if (type.getName().equals("aaa_setle")) {

      VHDLNet in0, in1;
      PortTag tag = findInPort("in0");
      in0 = (VHDLNet)tag.getNet();
      tag = findInPort("in1");
      in1 = (VHDLNet)tag.getNet();

      FunctionCall fc0 = new FunctionCall(new SimpleName("signed"));
      fc0.add(in0.getVHDLName());

      FunctionCall fc1 = new FunctionCall(new SimpleName("signed"));
      fc1.add(in1.getVHDLName());

      csa.addCondition(new Waveform(Char.ONE),
		       new Expression(new Le(fc0, fc1)));
      csa.addCondition(new Waveform(Char.ZERO), null);

    } else if (type.getName().equals("aaa_setgt")) {

      VHDLNet in0, in1;
      PortTag tag = findInPort("in0");
      in0 = (VHDLNet)tag.getNet();
      tag = findInPort("in1");
      in1 = (VHDLNet)tag.getNet();

      FunctionCall fc0 = new FunctionCall(new SimpleName("signed"));
      fc0.add(in0.getVHDLName());

      FunctionCall fc1 = new FunctionCall(new SimpleName("signed"));
      fc1.add(in1.getVHDLName());

      csa.addCondition(new Waveform(Char.ONE),
		       new Expression(new Gt(fc0, fc1)));
      csa.addCondition(new Waveform(Char.ZERO), null);

    } else if (type.getName().equals("aaa_setge")) {

      VHDLNet in0, in1;
      PortTag tag = findInPort("in0");
      in0 = (VHDLNet)tag.getNet();
      tag = findInPort("in1");
      in1 = (VHDLNet)tag.getNet();

      FunctionCall fc0 = new FunctionCall(new SimpleName("signed"));
      fc0.add(in0.getVHDLName());

      FunctionCall fc1 = new FunctionCall(new SimpleName("signed"));
      fc1.add(in1.getVHDLName());

      csa.addCondition(new Waveform(Char.ONE),
		       new Expression(new Ge(fc0, fc1)));
      csa.addCondition(new Waveform(Char.ZERO), null);

    } else if (type.getName().equals("aaa_seteq")) {

      VHDLNet in0, in1;
      PortTag tag = findInPort("in0");
      in0 = (VHDLNet)tag.getNet();
      tag = findInPort("in1");
      in1 = (VHDLNet)tag.getNet();

      csa.addCondition(new Waveform(Char.ONE),
		       new Expression(new Eq(in0.getVHDLName(),
					     in1.getVHDLName())));
      csa.addCondition(new Waveform(Char.ZERO), null);

    } else if (type.getName().equals("aaa_setne")) {

      VHDLNet in0, in1;
      PortTag tag = findInPort("in0");
      in0 = (VHDLNet)tag.getNet();
      tag = findInPort("in1");
      in1 = (VHDLNet)tag.getNet();

      csa.addCondition(new Waveform(Char.ONE),
		       new Expression(new Ne(in0.getVHDLName(),
					     in1.getVHDLName())));
      csa.addCondition(new Waveform(Char.ZERO), null);

    } else if (type.getName().equals("aaa_cshl")) {
      VHDLNet in0 = (VHDLNet)((PortTag)findInPort("in0")).getNet();
	
      int const_in1 = -1;
      VHDLNet in1 = (VHDLNet)((PortTag)findInPort("in1")).getNet();
      for (Iterator net_iter = in1.getSources().iterator(); 
	   net_iter.hasNext();) {
	PortTag pt = (PortTag) net_iter.next();
	Node pt_parent = pt.getParent();
	if (pt_parent instanceof Constant) {
	  const_in1 = ((Constant)pt_parent).getValueInt();
	}
      }
      
      int width = in0.getWidth();
      /*
       we have width and const_in1
       what if const_in1 = 0  or is < 0?
       -- we can either barf or expect that constant op will have taken
       care of it.

       what if width - const_in1 <= 0 ?
       -- this is covered.
       
       this works, but may need to be more robust ...
      */

      Primary se = null;
      
      if (width > const_in1) {
	SliceName slice = new SliceName(in0.getVHDLName(), 
					width - 1 - const_in1, 0);
	se = new Concat(slice);
      
	String zero_s = "";
	for (int i = 0; i< const_in1; i++) {
	  zero_s += "0";
	}

	BitStringLiteral zero = new BitStringLiteral("B",zero_s);
	((Concat)se).concat(zero);
      } else {
	se = VHDLConstant.genConstant(BigInteger.ZERO, width);
      }
      
      
      csa.addCondition(new Waveform(se), null);

    } else if (type.getName().equals("aaa_cshr")) {
      VHDLNet in0 = (VHDLNet)((PortTag)findInPort("in0")).getNet();
	
      int const_in1 = -1;
      VHDLNet in1 = (VHDLNet)((PortTag)findInPort("in1")).getNet();
      for (Iterator net_iter = in1.getSources().iterator(); 
	   net_iter.hasNext();) {
	PortTag pt = (PortTag) net_iter.next();
	Node pt_parent = pt.getParent();
	if (pt_parent instanceof Constant) {
	  const_in1 = ((Constant)pt_parent).getValueInt();
	}
      }
      
      int width = in0.getWidth();
      /*
       we have width and const_in1
       what if const_in1 = 0  or is < 0?
       -- we can either barf or expect that constant op will have taken
       care of it.

       what if width - const_in1 <= 0 ?
       -- this is covered.
       
       this works, but may need to be more robust ...
      */

      Primary se = null;
      
      if (width > const_in1) {
	SliceName slice = new SliceName(in0.getVHDLName(), 
					width - 1, const_in1);
	String zero_s = "";
	for (int i = 0; i< const_in1; i++) {
	  zero_s += "0";
	}

	BitStringLiteral zero = new BitStringLiteral("B",zero_s);
	
	se = new Concat(zero);
	((Concat)se).concat(slice);
      } else {
	se = VHDLConstant.genConstant(BigInteger.ZERO, width);
      }
      
      
      csa.addCondition(new Waveform(se), null);
      
    } else if (type.getName().equals("aaa_some_libop")) {

      // what library
      // what ports map to what ports
      // outs -> VHDL outs
      // ins -> VHDL ins
      // needs clock ?
      // what is the library name
      // what is the VHDL name
      
    } else {

      /*
      System.out.println("VHDLOperator::build() I do not know how to handle type "+type.getName()+" "+type.getSymbol()+"  ");
      System.exit(-1);
      */

      throw new SynthesisException("VHDLOperator::build() I tried everything else and" + 
			     "I do not know how to handle type "
			     +type.getName()+" "+type.getSymbol()+"\n"
			     +this.toString());
    }
    }
  }
}

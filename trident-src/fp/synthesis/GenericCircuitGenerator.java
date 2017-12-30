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


package fp.synthesis;

import fp.circuit.*;
import fp.circuit.dot.*;
import fp.circuit.vhdl.*;
import fp.util.UniqueName;
import fp.util.BooleanEquation;
import fp.util.*;
import fp.GlobalOptions;

import java.util.*;
import java.math.BigInteger;



public abstract class GenericCircuitGenerator {

  protected Circuit _circuit;
  private boolean _isPipelined = false;
  protected static String _target;
  protected static Library library;
  protected Object[] _objs;

  // strange share variable.
  protected static boolean _buildTop;
  
  GenericCircuitGenerator() {
  } 
  
  /**
   * This is the main constructor...
   */
  public GenericCircuitGenerator(String target, String name, Circuit parent) {
    // this should happen once. 
    library = new Library("requires_file_name");
    _target = new String(target);
    _buildTop = GlobalOptions.buildTop;

    _objs = new Object[4];

    // this could be reflection, too.
    if ("vhdl".equals(_target)) {
      _circuit = new VHDLCircuit(parent, null, name);
    } else if ("dot".equals(_target)) {
      _circuit = new DotCircuit(parent, null, name);
      ((DotCircuit)_circuit).setClustering(true);
    } else {
      // this should be an exception
      throw new SynthesisException("Unknown circuit technology "+_target);
    }
  }
  public GenericCircuitGenerator(String target, String name) {
    this(target, name, null);
  }

  public String getTarget() {  return _target;  }

  public boolean isPipelined() {
    return _isPipelined;
  }


  /**
   * This method creates a new port while ensuring that duplicates 
   * don't get created.  If a duplicate is about to be created, it 
   * just returns the original port.
   */
  protected Port insertInPort(Circuit graph, String parent_net, 
				     String name, String child_net, 
				     int width) {
    Port port = graph.getPortByName(name);
    if(port == null) 
      return graph.insertInPort(parent_net, name, child_net, width);
    else
      return port;
  }

 /**
   * this is a simple method use to make sure that only one input of a
   * given name is introduced
   */
  protected Port insertOutPort(Circuit graph, String child_net,
                              String name, String parent_net, int width) {
    Port port = graph.getPortByName(getCircuitName(graph)+"_0."+name+".0");
    //    System.out.println(graph.getPortByName(name)+","+name);
    if (port == null) {
      return graph.insertOutPort(child_net, name, parent_net, width);
    } else {
      return port;
    }
  }

  /**
   * This method creates a logic tree from a list of wires that need
   * to be ORed, ANDed, or XORed together.
   */
  protected String makeLogicTree(Circuit graph, Operation type,
                                 LinkedList inputs, int width) {
    // combine all of the inputs
    String logic_out = null;

    if( inputs.size() == 1 ) {
      // if there is only one path, just return the input
      logic_out = (String)inputs.getFirst();
    } else if( inputs.size() > 1 ) {
      // otherwise, combine everyone on the list in a tree
      ListIterator in_iter = inputs.listIterator();
      logic_out = (String)in_iter.next();

      while( in_iter.hasNext() ) {
	String in = (String)in_iter.next();
	// CHANGE: is this wirename correct??
        String wire = graph.getUniqueWireName();
	//String wire = graph.getUniqueWireName("t_net");
        //String wire = graph.getParent().getUniqueWireName();
	graph.insertOperator(type, type.toString()+"_tree_"+wire, logic_out, 
			     in, wire, width);
	logic_out = wire;
      }
    } else {
      System.err.println( "ERROR: can't make logic tree with no inputs" );
      throw new SynthesisException();
    }

    return logic_out;
  }

  static protected String getCircuitName(Circuit circuit) {
    return circuit.getRefName();
  }

  protected Operator insertBuf(Circuit circuit, String name, String in, 
			       String out, int width) {
    return circuit.insertOperator(Operation.BUF, name, in, out, width);
  }

  /**
   * Builds a 2-to-1 mux...
   *    name    :  name of mux
   *    in0     :  data input to mux
   *    in1     :  data input to mux
   *    sel     :  control wire to mux
   *    out     :  out wire from mux
   *    width   :  width of data wires
   */
  protected static Operator insertMux(Circuit circuit, String name, String in0, 
			       String in1, String sel, String out, 
			       int width) {
    HashMap map = new HashMap();

    if((in0 == null) || (in1 == null) || (sel == null))
      throw new SynthesisException("null input to mux!");

    if(in0.equals(in1)) {
      new SynthesisException("ERROR: mux data inputs are equal (inefficient)"
			     +" and it does not work!!!");
    }

    // Add the inputs and outputs of the mux to the hashmap.
    map.put(in0, new PortTag( null, "in0", PortTag.IN, width));
    map.put(in1, new PortTag( null, "in1", PortTag.IN, width));
    map.put(sel, new PortTag( null, "s", PortTag.IN, 1));
    map.put(out, new PortTag( null, "out", PortTag.OUT, width));
    
    // Now, return the newly inserted MUX.
    return circuit.insertOperator(name, Operation.MUX, map);
  }


  protected static Operator insertSlice(Circuit circuit, String name, 
					String in, int r0, int r1, String out, 
					int widthIn, int widthOut) {
    // What if the range is an odd value???
    if((r0 > r1) || (r0 < 0) || (r1 < 0))
      throw new SynthesisException("Range of slice operator is wrong?!");
    else if((r1-r0+1) != widthOut)
      throw new SynthesisException("Range-widthOut mismatch!   "+
				   "r0="+r0+", r1="+r1+", widthOut="+widthOut);
    else if(widthIn <= widthOut)
      throw new SynthesisException("widthIn <= widthOut !!");

    int cWidth = 32;
    // jt -- I had to add the prefix so we did not have name collisions
    // somehow wer are not being unique like we should...
    String c0 = circuit.getUniqueWireName("sconst");
    String c1 = circuit.getUniqueWireName("sconst");
    circuit.insertConstant("c_"+r0, r0, c0, cWidth); //width?
    circuit.insertConstant("c_"+r1, r1, c1, cWidth); //width?

    HashMap map = new HashMap();
    map.put(in, new PortTag( null, "in0", PortTag.IN, widthIn));
    map.put(c1, new PortTag( null, "in1", PortTag.IN, cWidth)); // width?
    map.put(c0, new PortTag( null, "in2", PortTag.IN, cWidth));
    map.put(out, new PortTag( null, "out", PortTag.OUT, widthOut)); 

    return circuit.insertOperator(name, Operation.SLICE, map);
  }


  protected Operator insertExpand(Circuit circuit, String name, String in, 
				  int r0, int r1, String outPrefix, 
				  int width, boolean fullName) {
    // What if the range is an odd value???
    if((r0 > r1) || (r0 < 0) || (r1 < 0))
      throw new SynthesisException("Range of expand operator is wrong!");

    String portPrefix = "out";
    HashMap map = new HashMap();

    map.put(in, new PortTag(null, "in", PortTag.IN, width));

    for(int i = r0; i <= r1; i++) {
      String out = ((r0 == r1) && (!fullName)) ? outPrefix : outPrefix+"_"+i;
      map.put(out, new PortTag(null, portPrefix+i, PortTag.OUT,1));
    }

    return circuit.insertOperator(name, Operation.EXPAND, map);
  }
  protected Operator insertExpand(Circuit circuit, String name, String in, 
				  int r0, int r1, String outPrefix, 
				  int width) {
    return insertExpand(circuit, name, in, r0, r1, outPrefix, width, false);
  }


  
  protected static Operator insertZeroExtend(Circuit circuit, String name, 
					     String in, String out, 
					     int widthIn, int widthOut) {
    int diff = widthOut - widthIn;
    if(diff < 0)
      throw new SynthesisException("widthOut < widthIn!");

    if (diff == 0) {
      // this is just in case
      HashMap map = new HashMap();
      map.put(in, new PortTag(null, "in0", PortTag.IN, widthIn));
      map.put(out, new PortTag(null, "out", PortTag.OUT, widthOut));
      return circuit.insertOperator(name, Operation.BUF, map);

    } else {

      // jt -- this has a similar problem to that of above.  The
      // odd thing is that this code looks right, so the bad code
      // is probably somewhere else ...
      String c0 = circuit.getUniqueWireName("zconst");
      circuit.insertConstant("c_0", BigInteger.ZERO, c0, diff);
      
      HashMap map = new HashMap();
      map.put(in, new PortTag(null, "in0", PortTag.IN, widthIn));
      map.put(c0, new PortTag(null, "in1", PortTag.IN, diff));
      map.put(out, new PortTag(null, "out", PortTag.OUT, widthOut));
      
      return circuit.insertOperator(name, Operation.CONCAT, map);
    } 
  }
 

  void resolveWidths() {
    // Collect all nets at all levels of hierarchy.
    HashSet nlist = _circuit.collectNets(new HashSet());
    
    Iterator it = nlist.iterator();
    while(it.hasNext()) {
      Net net = (Net) it.next();
      boolean isErrorless = ((Net) net).resolveWidth();
      if(!isErrorless)
	System.out.println("   wire: "+net);
    }
  }
}

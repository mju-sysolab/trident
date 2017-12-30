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

//import fp.passes.*;

import fp.flowgraph.*;
import fp.util.UniqueName;
import fp.util.BooleanEquation;
import fp.util.*;
import fp.hardware.*;
import fp.hwdesc.Chip;
import fp.hwdesc.Hardware;
import fp.hwdesc.Resource;
import fp.GlobalOptions;

import java.util.*;


/**
 * This class generates an abstract circuit based on the CFG 
 * that is passed in.  
 */
public class DesignCircuitGenerator extends GenericCircuitGenerator implements CircuitSwitch {

  private   static HashMap _topLevelWires;
  private   static HashSet _idleWires;
  private   static HashMap _controlWires;
  protected static RegfileGenerator _regfile;

  private          RegInfoList _reg_list;
  private          MemInfoList _mem_list;

  protected static MemoryInterfaceGenerator _memory;
  private          DataPathCircuitGenerator _dpGen;

  public DesignCircuitGenerator() {
  }

  // this constructor is used when using no top to the design.
  public DesignCircuitGenerator(String target, String name) {
    this(target, name, null, new RegInfoList(0,0,1), new MemInfoList());
    // are these reasonable defaults ?

    Hardware hw = GlobalOptions.hardware;
    
    long reg_offset = 0L;
    long mem_offset = 0L;
    int memory_count = 0;
    
    // this code assumes one fpga and one block of memory ... it should be more flexible.
    // I should really put things into hash maps that can be recalled by name like
    // {'mem0' : addr 0x0000, 'mem1': addr 0x40000 ...
    for(Iterator iter = hw.chip.listIterator(); iter.hasNext(); ) {
      Chip chip = (Chip)iter.next();
      if (chip.typeCode == Chip.FPGA_TYPE) {
	for(Iterator r_iter = chip.resource.listIterator(); r_iter.hasNext(); ) {
	  Resource res = (Resource)r_iter.next();
	  if (res.typeCode == Resource.REGISTER_TYPE) {
	    reg_offset = (long)res.address;
	  }
	}
      }
      
      if (chip.typeCode == Chip.RAM_TYPE) {
	mem_offset = (long)chip.address;
	memory_count = memory_count + chip.count;
      }
    }
 
  }
 
  public DesignCircuitGenerator(String target, String name, Circuit parent,
				RegInfoList reg_list, MemInfoList mem_list) {
    super(target, name, parent);
    _topLevelWires = new HashMap();
    _idleWires = new HashSet();
    _controlWires = new HashMap();
    
    _reg_list = reg_list;
    _mem_list = mem_list;
  }

  /**
   * Creates the top level structure of the abstract circuit 
   * design.
   */
  public void generate(BlockGraph graph) {
    /**
     * 1) create global signals
     * 2) create the block structures
     *     -- create data path logic
     *     -- create state machine
     * 3) create global registers
     */
    BlockNode bn;
    Circuit block;
    int onetime, i = 0;
    HashSet already_seen = new HashSet();
    HashSet add_to_ors = new HashSet();

    // Create the register file and memory bus for the top level circuit.
    ArrayList extOpNames = graph.getExternalOperandNames();
    _regfile = new RegfileGenerator(_circuit, extOpNames, _reg_list);

    ChipDef chipInfo = GlobalOptions.chipDef;
    _memory = new MemoryInterfaceGenerator(_circuit, _mem_list);
    _memory.generate(chipInfo);

    System.out.println(" MEM LIST "+_mem_list);

    if (hasExternalMemories()) {
      // disable before building
      GlobalOptions.makeTestBench = false;
      System.err.println("Disabling TestBench due to External Memories");
    }

    // Create the global signals (clk, reset).
    if(_buildTop) {
      _circuit.insertInPort("start", "start", "start", 1);
      _circuit.insertInPort("clk", "clk", "clk", 1);
      _circuit.insertInPort("reset", "reset", "reset", 1);
    } else {
      _circuit.insertInPort("start", "start", 1);
      _circuit.insertInPort("clk", "clk", 1);
      _circuit.insertInPort("reset", "reset", 1);
    }

    onetime = graph.getAllNodes().size()-1;

    // Iterate over each block in the CFG.
    for(Iterator it1 = graph.getAllNodes().iterator(); it1.hasNext(); ) {
      bn = (BlockNode) it1.next();

      // Don't make circuits for the "hidden" blocks.
      if( (bn == graph.ENTRY) || (bn == graph.EXIT) ) {
	i++;
	continue;
      }
      
      System.out.println();
      System.out.println("BLOCK NAME -- " + bn.getLabel().getFullName()+", II="+ bn.getII()+", modulo:"+bn.getIsModuloScheduled());

      // Create a sub-circuit for this block.
      block = _circuit.insertCircuit(bn.getLabel().getFullName());
      block.insertInPort("clk", "i_clk", "clk", 1);
      block.insertInPort("reset", "i_reset", "reset", 1);
      
      // Create logic in the data path based on the instructions in this block.
      _dpGen = new DataPathCircuitGenerator();
      HashSet wires = _dpGen.generate(block, bn, chipInfo);

      // Add access to the register file for all registers.
      already_seen.addAll(wires);

      if(i == onetime) {  // Only do this code once at end of processing...
	// For each element in the register file...
	Iterator hit = _regfile.getRegisterLog().keySet().iterator();
	while(hit.hasNext()) {
	  String name = (String)hit.next();
	  int width = 
	    ((RegInfo)_regfile.getRegisterLog().get(name)).getWidth();
	  String entry = name+"/"+width;
	  if(!already_seen.contains(entry)) {
	    wires.add(name+"/"+width);
	    add_to_ors.add(name+"/"+width);
	  }
	}

      }
      //System.out.println("wires:  "+wires);
      _topLevelWires.put(getCircuitName(block), wires);

      makeControlLogic(block, bn, (BlockNode)graph.EXIT);

      i++;
    }

    // This loop must occur after the first because makeblockcontrolinput() 
    // uses _controlWires that was updated by the first loop.
    for(Iterator it2 = graph.getAllNodes().iterator(); it2.hasNext(); ) {
      BlockNode blk = (BlockNode) it2.next();
      if(blk.equals(graph.ENTRY) || blk.equals(graph.EXIT))
	continue;  // Skip the entry and exit blocks!
      makeBlockControlInput(blk, (BlockNode)graph.ENTRY);
    }

    // Make the design's done signal.
    makeGlobalDoneSignal(graph);

    // Connect the data and write enable wires from the blocks to the regfile.
    makeConnectionsToRegfile(add_to_ors, extOpNames);

    // Connect the memory access-related wires.
    _memory.makeConnections();

    if(!_buildTop) {
      // Give all the wires their widths, and check wire connectivity.
      resolveWidths();
      
      // Build the circuit in the underlying representation.
      _circuit.build(graph.getName(), _objs);
    }
  }

  public HashMap getMemoryInterface() {
    return MemoryInterfaceGenerator.getMemoryInterface();
  }


  public boolean hasExternalMemories() {
    if (_mem_list != null) {
      return _mem_list.size() > 0;
    } else
      return false;
  }


  private void makeGlobalDoneSignal(BlockGraph bg) {
    int i = 0;
    HashMap map = new HashMap();

    // Add each block's idle wire to the inputs of the AND gate.
    Iterator it = _idleWires.iterator();
    while(it.hasNext()) {
      String name = (String)it.next();
      map.put(name, new PortTag(null, "in"+(i++), PortTag.IN, 1));
    }

    // Add the output to the AND gate.
    map.put("and_done", new PortTag(null, "out", PortTag.OUT, 1));

    // Make the AND operator and the OUT port...
    _circuit.insertOperator("and_done", Operation.AND, map);

    // delay a copy of the done signal
    String reg_out = _circuit.getUniqueWireName("reg_done");
    _circuit.insertRegister("done_1", "and_done", (String)null, 
			    reg_out, 1, 1);
    _circuit.insertOperator(Operation.AND, "done", "and_done", reg_out,
			    "done_2", 1);
    // this is silly, I have to register the output to avoid glitching...
    _circuit.insertRegister("done_2", "done_2", (String)null, "done", 1, 1);

    if(_buildTop)
      _circuit.insertOutPort("done", "o_done", "done", 1);
    else
      _circuit.insertOutPort("done", "o_done", 1);      
  }


  /**
   * This method makes the control logic input to this block. 
   */
  private String makeBlockControlInput(BlockNode bn, BlockNode entry) {
    String block_name = bn.getLabel().getFullName();
    String or_out = "start_"+bn.getLabel().getFullName();
    int i = 0;
    boolean entryIsPointing = false;

    HashMap map = new HashMap();
    map.put(or_out, new PortTag(null, "out", PortTag.OUT, 1));

    // If the GlobalEntry node is pointing to this node...
    if(((BlockEdge)entry.getOutEdges().toArray()[0]).getSink().equals(bn)) {
      map.put("start", new PortTag(null, "in"+(i++), PortTag.IN, 1));
      entryIsPointing = true;
    }

    LinkedList wires = (LinkedList) _controlWires.get(block_name);

    if(wires == null) {
      // If there will be no entries to the OR operator...
      if(!entryIsPointing)
	throw new SynthesisException("control: no control input to block!");
    } else {
      // Add the wires to the OR operator...
      Iterator w_it = wires.listIterator();
      while(w_it.hasNext()) {
	String wire = (String) w_it.next();
	map.put(wire, new PortTag(null, "in"+(i++), PortTag.IN, 1));
      }
    }
    _circuit.insertOperator("or_"+or_out, Operation.OR, map);
    
    return or_out;
  }


  private HashSet makeBlockControlOutput(Circuit block, BlockNode bn, 
					 StateMachine sm, BlockNode exit) {
    String block_name = bn.getLabel().getFullName();
    Set outEdges = bn.getOutEdges();
    HashSet predicates = new HashSet();
    HashMap exitCond = sm.getExitConditions();
    System.out.println("exit conditions:  "+exitCond);

    // For each of the block's outedges...
    for(Iterator edge_it = outEdges.iterator(); edge_it.hasNext(); ) {
      BlockEdge edge = (BlockEdge) edge_it.next();

      // Make out ports for every block that could follow this one.
      BlockNode sink_node = (BlockNode) edge.getSink();
      String sink_name = sink_node.getLabel().getFullName();

      if(sink_node.equals(exit))
	continue;  // Skip if this is the exit block...

      if(sink_node.equals(bn)) {
	continue;  // Skip if this is a loop edge...
      }

      String out = "next_"+sink_name+_circuit.getUniqueName();

      // Make a hashmap with the entries containing(dst node, list of wires)
      if(!_controlWires.containsKey(sink_name)) {
	LinkedList l = new LinkedList();
	l.add(out);
	_controlWires.put(sink_name, l);
      } else {
	LinkedList l = (LinkedList)_controlWires.get(sink_name);
	if(l == null) 
	  throw new SynthesisException("makeblockcontroloutput() no linked list found!");
	l.add(out);
	_controlWires.put(sink_name, l);
      }

      block.insertOutPort(out, "o_"+out, out, 1); 

      // Check if predicate logic has already been made.
      HashMap existingPreds = _dpGen.getPredicateWires();
      BooleanEquation edgePred = edge.getPredicate();
      
      String pred_wire = null;
      if(edgePred.isTrue() || edgePred.isFalse()) {
	//System.out.println("PREDICATE IS TRUE OR FALSE");
	pred_wire = "c_pred"+block.getUniqueName();
	int val = (edgePred.isTrue()) ? 1 : 0;
	_dpGen._circuit.insertConstant("c_"+val, val, pred_wire, 1);
      } else if(!existingPreds.containsKey(edgePred.toString())) {
	// If the predicate doesn't exist, then create it in the datapath...
	pred_wire = _dpGen.makePredicateLogic(_dpGen._circuit, edgePred, 
					      getNumBlockCycles(bn), bn);
	if(pred_wire == null) 
	  throw new SynthesisException("makeBlockControlOutput(): "+
				       "predicate wasn't defined");
	//System.out.println("MAKING PREDICATE:          " + pred_wire);
      } else {
	pred_wire = (String)existingPreds.get(edgePred.toString());
	//System.out.println("PREDICATE ALREADY EXISTS:  " + pred_wire);
      }
      
      // Add an out port to the datapath if it doesn't already exist...
      if(!_dpGen.getDpOutWires().contains(pred_wire)) {
	_dpGen._circuit.insertOutPort(pred_wire, "o_pred_"+pred_wire, 
				      pred_wire, 1);
	_dpGen.getDpOutWires().add(pred_wire);
	//System.out.println("CREATING OUTPORT FOR PRED: "+ pred_wire);
      }

      // Get the state input to the next block's start signal.
      String sm_done = getStateInputForStartSignal(bn, edgePred, sm, block);

      System.out.println(" inputs "+sm.getInputs());

      predicates.addAll(makeFsmInputs(bn, edgePred, exitCond));

      // AND the predicate and the statemachine's done signal...
      block.insertOperator(Operation.AND, "and_"+out, pred_wire, sm_done,
			   out, 1);
    }

    return predicates;
  }

  /**
   * This method adds outports to give the FSM access to the predicates 
   * that were calculated inside the block circuit.  Both registered and 
   * non-registered versions of the wire can be output. The boolean operand 
   * names are returned in a HashSet.
   */
  HashSet makeFsmInputs(BlockNode bn, BooleanEquation edgePred, 
			HashMap exitCond) {
    HashSet predicates = new HashSet();
    System.out.println(" bn "+bn.getName()+" eq "+edgePred
		       +"\nexitCond hash "+exitCond);


    // For each boolean operand, create wires for the FSM inputs.
    Iterator it =((LinkedList)edgePred.listBooleanOperands()).listIterator();
    while(it.hasNext()) {
      boolean makeReg = false, makeNonReg = false;
      BooleanOperand op = (BooleanOperand) it.next();
      String op_name = op.getFullName();

      //predicates.add(op_name);  // This was adding too many predicates...

      int defCycle = getOpDefCycle(bn, op_name);
      
      /*
      // For each of the SM cycles...
      for(int i = 0; i < exitCond.size(); i++) {
	BooleanEquation cond = (BooleanEquation) exitCond.get(new Integer(i));
	if(cond == null)
	  continue;
	// If the exit condition is equal to the current operand...
	String condName = cond.toString().replace('~', ' ').trim();
	if(condName.equals(op.toString().trim())) {
	  predicates.add(op_name);
	  if(i > defCycle)
	    makeReg = true;
	  else if(i == defCycle)
	    makeNonReg = true;
	}
      }
      */
      // danger
      /* 
	 Okay, I don't seem agree with Jeff about whether these should be
	 registered or unregistered.  Unless the value used outside of the
	 block (e.g. for start signals), using the unregistered version 
	 should be fine.
      */
      makeNonReg = true;

      if(makeReg && makeNonReg) {
	// this is not working ...
	throw new SynthesisException("State machine isn't yet smart enough"+
				     " to have both registered and non-"+
				     "registed versions of the same predicate"+
				     " input!");
      }

      if(makeReg) {
	// Get the registered wire for the statemachine input.
	String input = op_name + "_s%" + (defCycle+1);	
	if(!_dpGen.getDpOutWires().contains(op_name)) {
	  _dpGen._circuit.insertOutPort(input, "o_"+op_name, 
					op_name, 1); 
	  _dpGen.getDpOutWires().add(op_name);
	}
	System.out.println("Making registered FSM input:  "+input);
      }

      if(makeNonReg) {
	// Make the non-registered wire for the statemachine input.
	String input = op_name;
	if(!_dpGen.getDpOutWires().contains(op_name)) {
	  _dpGen._circuit.insertOutPort(input, "o_"+op_name, 
					op_name, 1); 
	  _dpGen.getDpOutWires().add(op_name);
	}
	System.out.println("Making non-registered FSM input:  "+input);
      }

    }

    return predicates;
  }


  /**
   * Return the last cycle associated with the operand's defining inst.
   */
  private static int getOpDefCycle(BlockNode bn, String operand) {
    // For each instruction...
    for(Iterator it = bn.getInstructions().iterator(); it.hasNext(); ) {
      Instruction inst = (Instruction) it.next();
      // If this instruction defines the specified operand...
      if(inst.getOperand(0).getFullName().equals(operand)) {
	// Get the last cycle associated with this instruction.
	int start = inst.getExecClkCnt();
	return (start + (int)inst.getRunLength());	
      }
    }
    // If the definition of the operand was not found, throw excpetion!
    throw new SynthesisException("Operand ("+operand+
				 ") definition not found!");
  }


  /**
   * This method makes the control logic for managing the multiple blocks...
   */
  private void makeControlLogic(Circuit block, BlockNode bn, BlockNode exit) {
    // Add the StateMachine to the sub-circuit.
    StateMachine sm = newStateMachine(bn);

    // Create an in port for the start signal.
    String blkStart = "start_"+bn.getLabel().getFullName();
    block.insertInPort(blkStart, "i_"+blkStart, "start", 1);
    
    //makeBlockControlInput(bn, entry);
    HashSet predicates = makeBlockControlOutput(block, bn, sm, exit);

    LinkedList inputs = new LinkedList();
    inputs.add("clk");
    inputs.add("reset");

    int numCycles = getNumBlockCycles(bn); 

    Set smSet = sm.getInputs().keySet();
    predicates.addAll(smSet);
    inputs.addAll(predicates);
    block.insertFSM("fsm"+_circuit.getUniqueName(), inputs, sm);

    // Add outports from the dp to the block for inputs to the FSM...
    for(Iterator sIt = smSet.iterator(); sIt.hasNext(); ) {
      String smWire = (String) sIt.next();
      // Add an out port to the datapath if it doesn't already exist...
      if(!_dpGen.getDpOutWires().contains(smWire) && 
	 !smWire.equals("start")) {
	_dpGen._circuit.insertOutPort(smWire, "o_pred_"+smWire, 
				      smWire, 1);
	_dpGen.getDpOutWires().add(smWire);
      }
    }

    String idle = "idle_"+bn.getLabel().getFullName();
    block.insertOutPort("s%"+numCycles, "o_idle", idle, 1);
    _idleWires.add(idle);
  }


  private String getStateInputForStartSignal(BlockNode bn, BooleanEquation eq, 
					     StateMachine sm, Circuit block) {
    HashMap cond = sm.getExitConditions();
    int cycle = -1;
    //System.out.println("FSM cond: "+cond);
    //System.out.println("eq:       "+eq);
    for(int i = 0; i < cond.size(); i++) {
      BooleanEquation bool = (BooleanEquation)cond.get(new Integer(i));
      //System.out.println("          "+bool);
      if(bool.isEqual(eq)) {
	//cycle = i+1;
	cycle = i;
	break;
      }
    }
    String out = null;
    if(cycle == -1) {
      cycle = cond.size()-1;
      out = "s%"+cycle;
    } else {
      out = "s%"+cycle+"_1";
      block.insertRegister("reg_s%"+cycle, "s%"+cycle, null, out, 1, 0);
    }

    return out;
  }


  /**
   * This method computes the number of cycles that occur in the block node.  
   * It is used to find the size of the state machine.
   */
  static protected int getNumBlockCycles(BlockNode bn) {
    int max_cycles = 0;
    for(Iterator it = bn.getInstructions().iterator(); it.hasNext(); ) {
      Instruction inst = (Instruction) it.next();
      int tmp = inst.getExecClkCnt() + (int) inst.getRunLength();
      if(max_cycles < tmp)
	max_cycles = tmp;
    }
    return max_cycles+1;  // add 1 because "0th" state should be counted.
  }


  /**
   * Return the type of state machine based on the type of scheduling was 
   * performed on the given block node.
   */
  static private StateMachine newStateMachine(BlockNode bn) {
    if(bn.getIsModuloScheduled()) {
      int ii = bn.getII();
      return new ModuloStateMachine(ii+1, bn);
    } else {
      int numCycles = getNumBlockCycles(bn);
      return new GenericStateMachine(numCycles+1, bn);
    }
  }


  protected void makeConnectionsToRegfile(HashSet add, ArrayList extVars) {
    // For each block... 
    Iterator blk_it = _topLevelWires.keySet().iterator();
    while(blk_it.hasNext()) {
      String block1 = (String) blk_it.next();
      HashSet wires1 = (HashSet) _topLevelWires.get(block1);

      // For each wire in this block...
      Iterator wire_it = wires1.iterator();
      while(wire_it.hasNext()) {
	HashMap data_map = new HashMap();
	HashMap en_map = new HashMap();
	
	String wire_info = (String) wire_it.next();
	int index = wire_info.lastIndexOf('/');
	String wire_name = wire_info.substring(0, index);
	int width = Integer.parseInt(wire_info.substring(index+1));

	// If the other blocks contain this wire name, then add them to 
	// the input of the OR operator and delete those wires.
	int i = 1;
	Iterator it = _topLevelWires.keySet().iterator();
	while(it.hasNext()) {  // For each block...
	  String block2 = (String) it.next();
	  if(!block2.equals(block1)) {  // If not looking at same blocks...
	    HashSet wires2 = (HashSet) _topLevelWires.get(block2);
	    if(wires2.contains(wire_info)) {  
	      // If both blocks contain the same wire name...
	      data_map.put(block2+"_"+wire_name,
			   new PortTag(null, "in"+i, PortTag.IN, width));
	      en_map.put(block2+"_"+wire_name+"_we",
			 new PortTag(null, "in"+(i++), PortTag.IN, 1));
	      wires2.remove(wire_info);
	    }
	  }
	}

	// This adds access to the register file.
	String data_in = null;
	String en_in = null;
	if(add.contains(wire_name+"/"+width)) {
	  // Here, the or gate will only have 1 input because the variable 
	  // was never stored in the code.
	  data_in = "m_init_"+wire_name;
	  en_in = "init_"+wire_name+"_we";

	  if(!extVars.contains(wire_name))
	    throw new SynthesisException("shouldn't have added regfile access -- name: "+wire_name+", external variables: "+extVars);
	} else {
	  // Here, the or gate will have more than one input because it 
	  // was defined at least once.
	  data_in = block1+"_"+wire_name;
	  en_in = block1+"_"+wire_name+"_we";

	  // If this is a external variable, then make access to the regfile.
	  if(extVars.contains(wire_name)) {
	    data_map.put("m_init_"+wire_name, 
			 new PortTag(null, "in"+i, PortTag.IN, width));
	    en_map.put("init_"+wire_name+"_we", 
		       new PortTag(null, "in"+i, PortTag.IN, 1));
	  }
	}

	// If this operand is external, then give it external regfile access.
	if(extVars.contains(wire_name))
	  makeRegfileAccess(wire_name, width);

	// Add the data wires to an OR operator.
       	data_map.put(data_in, 
		     new PortTag(null,"in0",PortTag.IN,width));
	data_map.put("w_"+wire_name, 
		     new PortTag(null, "out", PortTag.OUT, width));
	
	// Add the enable wires to an OR operator.
       	en_map.put(en_in, 
		   new PortTag(null,"in0",PortTag.IN,1));
	en_map.put("we_"+wire_name,
		   new PortTag(null, "out", PortTag.OUT, 1));

	_circuit.insertOperator("or_"+wire_name, 
				Operation.OR, data_map);
	_circuit.insertOperator("or_"+wire_name+"_we", 
				Operation.OR, en_map);

	wire_it.remove();
      }
      // Remove the processed block.
      blk_it.remove();
    }
  }

  /** 
   * This method gives the user external register file access for the 
   * specified operand.
   */
  void makeRegfileAccess(String operand, int width) {
    // Create a mux for disabling the init inputs to the design.
    String zeroOut = _circuit.getUniqueWireName();
    _circuit.insertConstant("c_0", 0, zeroOut, width);
    insertMux(_circuit, "mux_"+operand+_circuit.getUniqueName(),
	      zeroOut, "init_"+operand, "init_"+operand+"_we", 
	      "m_init_"+operand, width);
    
    // Add inports to the topLevel circuit for initial reg values.
    if(_buildTop) {
      int busWidth = RegisterInterfaceGenerator.DATA_BUS_WIDTH;
      String portOutput = (width == busWidth) ? "init_"+operand : 
	"i_"+operand;
      _circuit.insertInPort(operand+"_we", "i_"+operand+"_we", 
			    "init_"+operand+"_we", 1);
      _circuit.insertInPort(RegisterInterfaceGenerator.REG_IN, "i_"+operand, 
			    portOutput, busWidth);

      // If the bus width is too wide, then slice it...      
      if(width < busWidth)
	insertSlice(_circuit, "slice_"+operand, portOutput, 
		    0, width-1, "init_"+operand, busWidth, width);
      else if(width > busWidth)  // If bus is too narrow, exception!
	throw new SynthesisException("Register is wider than reg bus!");

    } else {  // Otherwise, just build inports without external wires..
      _circuit.insertInPort("i_"+operand+"_we", 
			    "init_"+operand+"_we", 1);
      _circuit.insertInPort("i_"+operand, "init_"+operand, width);
    }
  }


  /*
  ArrayList getExternalRegs() {
    return _regfile.getExternals();
  }
  */

  /*
  ArrayList getExtRegInfos() {
    return _regfile.getExtRegInfos();
  }
  */

  HashSet getBusInPorts() {
    return _memory.getInPorts();
  }
  HashSet getBusOutPorts() {
    return _memory.getOutPorts();
  }

  class ConnectionPair {
    ConnectionPair(String wireNameP, int widthP) {
      wireName = wireNameP;
      width = widthP;
    }
    String wireName;
    int    width;

    boolean equals(ConnectionPair p) {
      return (p.wireName.equals(this.wireName) && (p.width == this.width));
    }
  }
  
}

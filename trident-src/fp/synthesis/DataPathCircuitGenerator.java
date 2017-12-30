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
import fp.flowgraph.*;
import fp.flowgraph.Operator;
import fp.util.BooleanEquation;
import fp.util.BooleanOp;
import fp.hardware.*;
import fp.hwdesc.Memory;

import java.util.*;
//import java.lang.Integer;
import java.math.BigInteger;


public final class DataPathCircuitGenerator extends DesignCircuitGenerator {
  /**
   * This class generates the datapath part of the abstract circuit.  The 
   * datapath exists inside of a block circuit along with the state machine. 
   */

  private static final int ADDR_WIDTH = 32;

  private HashMap _blockOutWires;
  private HashSet _dpOutWires;
  private HashSet _dpInWires;
  private HashSet _dpInvWires;
  private HashSet _blkWires;
  private HashMap _dpRegisters;
  private HashMap _predicateWires;

  DataPathCircuitGenerator() {
    super();
    _blockOutWires = new HashMap();
    _dpOutWires = new HashSet();
    _dpInWires = new HashSet();
    _dpInvWires = new HashSet();
    _blkWires = new HashSet();
    _dpRegisters = new HashMap();
    _predicateWires = new HashMap();
  }

  /**
   * This method has two purposes:
   * 1) create a data path sub-circuit in this block's circuit
   * 2) create the logic for each instruction in this block node 
   * It returns a HashSet containing the names of the wires that should 
   * be connected to the register file.
   */
  public HashSet generate(Circuit blockLevel, BlockNode cfgBlockNode, 
			  ChipDef chipInfo) {
    _circuit = blockLevel.insertCircuit("dp_"+blockLevel.getName());
  
    for(Iterator it=cfgBlockNode.getInstructions().iterator();it.hasNext(); ) {
      Instruction current = (Instruction) it.next();
      Operator operator = current.operator;

      //System.out.println("-- inst type: " + current.type()+"  "+operator.format+" "+current);

      // Create the logic based on what the current operator is...
      switch(operator.format) {
      case InstructionFormat.Store_format:
	caseStore(_circuit, current, cfgBlockNode);
	break;
      case InstructionFormat.Load_format:
	caseLoad(_circuit, current, cfgBlockNode);
	break;
      case InstructionFormat.Select_format:
	caseSelect(_circuit, current, cfgBlockNode);
	break;
      case InstructionFormat.Test_format:
	caseTest(_circuit, current, cfgBlockNode);
	break;
      case InstructionFormat.Unary_format:
	caseUnary(_circuit, current, cfgBlockNode);
	break;
      case InstructionFormat.Binary_format:
	caseBinary(_circuit, current, cfgBlockNode);
	break;
      case InstructionFormat.Cast_format:
	caseCast(_circuit, current, cfgBlockNode);
	break;
      case InstructionFormat.ALoad_format:
	caseALoad(_circuit, current, cfgBlockNode, chipInfo);
	break;
      case InstructionFormat.AStore_format:
	caseAStore(_circuit, current, cfgBlockNode, chipInfo);
	break;
      default:
	System.out.println("Operator("+current.operator+") not yet supported");
	continue;
      }
    }
   
    _circuit.insertInPort("reset", "i_reset", "reset", 1);
    _circuit.insertInPort("clk", "i_clk", "clk", 1);
    return makeBlockOutPorts(blockLevel);
  }

  /**
   * This method creates a mux, which is used to control the data flowing 
   * out of the datapath/block. It also creates an outport from the datapath
   * to the block level for the write enable logic.
   * 
   * This is only used in array loads and stores...
   */
  private String makeMuxLogic(Circuit dp, Instruction inst, 
			      String blkEn, BlockNode bn, 
			      boolean isAddress) {
    int startCycle = inst.getExecClkCnt();
    //int width = inst.type().getWidth();
    int width = (isAddress) ? ADDR_WIDTH : 
                              inst.type().getWidth();
    float runLength = inst.getRunLength();
    BooleanEquation pred = inst.getPredicate();
    Circuit block = dp.getParent();

    // Add an inport to get info from the state machine.
    String cycle = null; 
    if(isAddress)  // Use the starting cycle if aload/astore address write..
      cycle = insertStateInputFromSM(dp, startCycle, 0);
    else           // Otherwise use the ending cycle.
      cycle = insertStateInputFromSM(dp, startCycle, runLength);
    
    // Make the conditions for asserting the enable line.
    String en = null;
    if(pred.isTrue()) {
      // Add a buffer for the state machine cycle input.
      // makeLogicTree should deal with this.
      en = cycle;
    } else if(pred.isFalse()) {
      // Make a zero (invalid) output.
      String zeroOut = block.getUniqueWireName();
      Constant zeroConst = block.insertConstant("c_0", 0, zeroOut, width);
      en = zeroOut;
    } else {
      // Otherwise, predicate is not yet known; so make some logic...
      String predName = makePredicateLogic(dp, pred, startCycle, bn);
      LinkedList inputs = new LinkedList();
      if(predName != null) {
	// Only add the predicate input if the predicate has been
	// already defined in a previous cycle...
        inputs.add(predName);
	inputs.add(cycle);
	// Make a logic tree of the above predicate logic.
	en = makeLogicTree(dp, Operation.AND, inputs, 1);
      } else {
	en = cycle;
      }
    }

    // Make an "en" port from the DP to the block level.
    Operand operand = null;
    Operand address = null;
    if(AStore.conforms(inst)) {
      operand = AStore.getPrimalDestination(inst);
      address = AStore.getAddrDestination(inst);
      //S
      //operand = (isAddress) ? AStore.getValue(inst) : AStore.getPrimalDestination(inst);
      //address = AStore.getAddrDestination(inst);
    } else if(ALoad.conforms(inst)) {
      //S
      //operand = ALoad.getPrimalSource(inst);
      //address = ALoad.getAddrSource(inst);
      operand = (isAddress) ? inst.getOperand(1) : inst.getOperand(0);
      address = ALoad.getAddrSource(inst);
    } else {  // CHANGE:  is this correct?  should it always be operand(0)?
      
      // if it is not an ALOAD and not an ASTORE, where does the address 
      // come from and who cares ?
      operand = (isAddress) ? inst.getOperand(1) : inst.getOperand(0);
    }
    //System.out.println("mux operand:  "+operand);
    String name = operand.getFullName();

    //String blkEn = block.getUniqueWireName(name)+"_we";
    insertOutPort(dp, en, "o_"+blkEn, blkEn, 1);

    // Make a zero (invalid) output.
    String zeroOut2 = "z_"+block.getUniqueWireName();
    block.insertConstant("c_0", 0, zeroOut2, width);

    // mux the invalid and valid data_out signals
    String muxName = "mux_"+cycle+block.getUniqueName();
    String muxIn = null;
    String muxOut = null;
    if(isAddress) {
      muxOut = address.getFullName()+block.getUniqueName();
    } else {
      muxOut = name+"_"+cycle;
    }
    if(AStore.conforms(inst))
      muxIn = (isAddress) 
        ? getOpName(dp, inst, AStore.getAddrDestination(inst), true, bn, true) 
        : getOpName(dp, inst, AStore.getValue(inst), true, bn, false);
    else
      muxIn = getOpName(dp, inst, inst.getOperand(1), true, bn, isAddress);

    //System.out.println(inst+" result MuxIn "+muxIn);
    insertMux(block, muxName, zeroOut2, muxIn, blkEn, muxOut, width);

    return muxOut;
  }
  
  /*
    This is currently only used by caseStore ...

  */

  private String makeDataMuxLogic(Circuit dp, Instruction inst, 
				  String blkEn, BlockNode bn) {
    return makeMuxLogic(dp, inst, blkEn, bn, false);
  }


  /**
   * This method builds connection logic between the datapath and the memory 
   * interface.  This includes an address write and data read path of wires.
   */
  private void caseALoad(Circuit dpCircuit, Instruction inst, 
			 BlockNode bn, ChipDef chipInfo) {
    System.out.println("caseAload: "+inst);
    Circuit block = dpCircuit.getParent();
    Circuit top = block.getParent();
    Operand arrayOp = ALoad.getPrimalSource(inst);
    Operand addrOp = ALoad.getAddrSource(inst);

    // Create the guard logic for writing the address to the membus.
    String addrEn = block.getUniqueWireName(addrOp.getFullName()) + "_we";
    String addrOut = makeMuxLogic(dpCircuit, inst, addrEn, bn, true);

    // Connect the address block in the datapath to the memory bus' 
    // address read port (actually just make ports/wires up to the 
    // top level).

    // this mixing of wires and names is not good ...
    String topUnique = top.getUniqueName();
    String topAddrWire = arrayOp.getFullName()+topUnique;

    // Fix up the address width.
    String portIn = null;
    int addrWidth = MemoryInterfaceGenerator.ADDR_WIDTH;
    if(addrWidth < ADDR_WIDTH) {
      portIn = "slice"+block.getUniqueName();
      insertSlice(block, portIn, addrOut, 0, 
		  addrWidth-1, portIn, ADDR_WIDTH, 
		  addrWidth);
    } else {
      portIn = addrOut;
    }
    // Make outports for the address and write enable.
    block.insertOutPort(portIn, 
			"o_"+addrOp.getFullName()+block.getUniqueName(), 
			topAddrWire, addrWidth);
    //String addrWe = addrOp.getFullName()+"_we";
    String addrWe = addrEn; 
    Port p = block.getPortByName("o_"+addrWe);
    block.insertOutPort(addrWe,"o_"+addrWe+block.getUniqueName(), topAddrWire+"_we", 1);

    // Connect the data between the memory bus and datapath.
    Operand resultOp = ALoad.getResult(inst);
    String dpDataWire = arrayOp.getFullName()+"_"+resultOp.getFullName();
    String blockDataWire = arrayOp.getFullName()+block.getUniqueName();
    Memory mb = chipInfo.findMemoryBlock(arrayOp);
    String memName = mb.getName();
    String topDataWire = arrayOp.getFullName()+top.getUniqueName();
    block.insertInPort(topDataWire, "i_"+blockDataWire, 
		       blockDataWire, inst.type().getWidth());
    dpCircuit.insertInPort(blockDataWire,"i_"+dpDataWire, dpDataWire, 
			   inst.type().getWidth());

    // Add this memory access to the memory interface data structure.
    String blkName = bn.getLabel().getFullName();
    _memory.addMemoryAccess(memName, blkName, topAddrWire, topDataWire,
			    MemoryInterfaceGenerator.ADDR_WIDTH, 
			    inst.type().getWidth(),
			    inst.getExecClkCnt(), 
			    MemoryInterfaceGenerator.LOAD);
  }

  private void caseAStore(Circuit dpCircuit, Instruction inst, 
			  BlockNode bn, ChipDef chipInfo) {
    Circuit block = dpCircuit.getParent();
    Circuit top = block.getParent();
    Operand addrOp = AStore.getAddrDestination(inst);
    Operand arrayOp = AStore.getPrimalDestination(inst);

    String addrEn = block.getUniqueWireName(arrayOp.getFullName())+"_we";

    // Create the guard logic for writing the address to the memory interface.
    String addrOut = makeMuxLogic(dpCircuit, inst, addrEn, bn, true);


    // Create the guard logic for writing the data to the memory interface.
    String dataOut = makeMuxLogic(dpCircuit, inst, addrEn, bn, false);

    int dataWidth = inst.type().getWidth();
    String extendOut = dataOut+"_ext";
    insertZeroExtend(block, "zextend_"+dataOut, dataOut, extendOut, 
		     dataWidth, MemoryInterfaceGenerator.DATA_WIDTH);

    // Connect the address in the datapath to the memory interface.
    String topAddrWire = addrOp.getFullName()+top.getUniqueName();
    int memWidth = MemoryInterfaceGenerator.ADDR_WIDTH;
    String portIn = null;
    if(memWidth < ADDR_WIDTH) {
      portIn = addrOut + "_s";
      insertSlice(block, "slice_"+portIn, addrOut, 0, memWidth-1, portIn, 
		  ADDR_WIDTH, memWidth);
    } else {
      portIn = addrOut;
    }
    block.insertOutPort(portIn, 
			"o_"+addrOp.getFullName()+block.getUniqueName(), 
			topAddrWire, MemoryInterfaceGenerator.ADDR_WIDTH);

    // Connect the data in the datapath to the memory interface.
    String topDataWire = arrayOp.getFullName()+top.getUniqueName();

    block.insertOutPort(extendOut, 
			"o_"+arrayOp.getFullName()+block.getUniqueName(),
			topDataWire, MemoryInterfaceGenerator.DATA_WIDTH);
    String we = topDataWire+"_we";
    block.insertOutPort(addrEn, "o_"+we, we, 1);

    // Add this memory access to the memory interface data structures.
    String blkName = bn.getLabel().getFullName();
    String memName = chipInfo.findMemoryBlock(arrayOp).getName();
    _memory.addMemoryAccess(memName, blkName, topAddrWire, topDataWire, 
			    MemoryInterfaceGenerator.ADDR_WIDTH, 
			    MemoryInterfaceGenerator.DATA_WIDTH, 
			    inst.getExecClkCnt(), 
			    MemoryInterfaceGenerator.STORE);
  }


  /**
   * This method implements all of the binary operators that occur in the 
   * control flow graph.
   */
  private void caseBinary(Circuit dpCircuit, Instruction inst, 
			  BlockNode bn) {
    Operand source1 = Binary.getVal1(inst);
    Operand source2 = Binary.getVal2(inst);
    Operand result = Binary.getResult(inst);
    Operation op = library.select(inst);

    // If the operation is handled, then insert it.
    if(op != null) {
      String out_name = result.getFullName();
      String op_name = "op_"+inst.operator()
	               + dpCircuit.getUniqueName();
      // Case where a binary inst's result is a primal.
      if(result.isPrimal())
	throw new SynthesisException("caseBinary(): primal result found");

      dpCircuit.insertOperator(op, op_name, 
			       getOpName(dpCircuit, inst, source1, false, bn),
			       getOpName(dpCircuit, inst, source2, false, bn),
			       out_name, inst.type().getWidth());
    } else {
      throw new SynthesisException("Binary operator not yet supported: "+
				   inst.operator());
    }
  }


  private void caseCast(Circuit dpCircuit, Instruction inst, BlockNode bn) {
    //System.out.println("caseCast():  "+inst);

    Operand source = Cast.getValue(inst);
    Operand target = Cast.getResult(inst);
    TypeOperand type_op = Cast.getType(inst);

    int target_width = type_op.getType().getWidth();
    int source_width = inst.type().getWidth();

    String op_name = "op_"+inst.operator+dpCircuit.getParent().getUniqueName();
    Operation op = library.select(inst);
    String wire_name;

    if (op != null) {
      /*
      // I don't think I can use this one...
      dpCircuit.insertOperator(op, op_name,
			       getOpName(dpCircuit, inst, source, false, bn), 
			       target.getFullName(),
			       inst.type().getWidth());
      */
      
      // this feels like a kludge...
      if (op.getName().equals("tr_dcast")) {
	Operation my_op = Operation.SLICE;
	
	HashMap map = new HashMap();
	String source_name = getOpName(dpCircuit, inst, source, false, bn);
	map.put(source_name, new PortTag(null, "in0", PortTag.IN, source_width));

	String const1_name = dpCircuit.getUniqueWireName();
	dpCircuit.insertConstant("c_"+(target_width-1), (target_width-1), const1_name, source_width);
	map.put(const1_name, new PortTag(null, "in1", PortTag.IN, source_width));

	String const2_name = dpCircuit.getUniqueWireName();
	dpCircuit.insertConstant("c_"+(0), (0), const2_name, source_width);
	map.put(const2_name, new PortTag(null, "in2", PortTag.IN, source_width));

	String target_name = target.getFullName();
	map.put(target_name, new PortTag(null, "out", PortTag.OUT, target_width));
	
	dpCircuit.insertOperator(op_name, my_op, map);

      } else if (op.getName().equals("tr_ucast")) {
	// source only matters
	if (type_op.getType().isSigned()) {
	  // first we slice
	  Operation slice = Operation.SLICE;
	  
	  HashMap slice_map = new HashMap();
	  String source_name = getOpName(dpCircuit, inst, source, false, bn);
	  slice_map.put(source_name, new PortTag(null, "in0", PortTag.IN, source_width));
	  
	  String slice_const1 = dpCircuit.getUniqueWireName();
	  dpCircuit.insertConstant("c_"+(source_width-1), (source_width-1), slice_const1, source_width);
	  slice_map.put(slice_const1, new PortTag(null, "in1", PortTag.IN, source_width));

	  String slice_const2 = dpCircuit.getUniqueWireName();
	  dpCircuit.insertConstant("c_"+(source_width-2), (source_width-2), slice_const2, source_width);
	  slice_map.put(slice_const2, new PortTag(null, "in2", PortTag.IN, source_width));

	  String top_bit = dpCircuit.getUniqueWireName("topbit");
	  slice_map.put(top_bit, new PortTag(null, "out", PortTag.OUT, 1)); // 1 bit
	  
	  dpCircuit.insertOperator(op_name+"_slice", slice, slice_map);

	  // Then mux
	  Operation mux = Operation.MUX;
	  
	  HashMap mux_map = new HashMap();
	  mux_map.put(top_bit, new PortTag(null, "s", PortTag.IN, 1)); // top_bit is the selector

	  String mux_const1 = dpCircuit.getUniqueWireName();
	  int mux_const_width = target_width - source_width;
	  dpCircuit.insertConstant("c_0", BigInteger.ZERO, mux_const1, mux_const_width);
	  mux_map.put(mux_const1, new PortTag(null, "in0", PortTag.IN, mux_const_width));

	  String mux_const2 = dpCircuit.getUniqueWireName();
	  BigInteger neg_value = new BigInteger("2").pow(mux_const_width).subtract(BigInteger.ONE);
	  dpCircuit.insertConstant("c_neg", neg_value, mux_const2, mux_const_width);
	  mux_map.put(mux_const2, new PortTag(null, "in1", PortTag.IN, mux_const_width));
		  
	  String mux_out = dpCircuit.getUniqueWireName("consts");
	  mux_map.put(mux_out, new PortTag(null, "out", PortTag.OUT, mux_const_width));

	  dpCircuit.insertOperator(op_name+"_mux", mux, mux_map);

	  // now concat
	  Operation concat = Operation.CONCAT;

	  HashMap concat_map = new HashMap();
	  concat_map.put(mux_out, new PortTag(null, "in0", PortTag.IN, mux_const_width)); 
	  concat_map.put(source_name, new PortTag(null, "in1", PortTag.IN, source_width)); 

	  String target_name = target.getFullName();
	  concat_map.put(target_name, new PortTag(null, "out", PortTag.OUT, target_width));

	 dpCircuit.insertOperator(op_name, concat, concat_map); 

	} else {
	  Operation my_op = Operation.CONCAT;
	  
	  HashMap map = new HashMap();
	  String source_name = getOpName(dpCircuit, inst, source, false, bn);
	  map.put(source_name, new PortTag(null, "in0", PortTag.IN, source_width));
	  
	  String const1_name = dpCircuit.getUniqueWireName();
	  dpCircuit.insertConstant("c_"+0, 0, const1_name, target_width - source_width);
	  map.put(const1_name, new PortTag(null, "in1", PortTag.IN, source_width));

	  String target_name = target.getFullName();
	  map.put(target_name, new PortTag(null, "out", PortTag.OUT, target_width));

	  dpCircuit.insertOperator(op_name, my_op, map);

	}

      } else {
	HashMap map = new HashMap();
	String source_name = getOpName(dpCircuit, inst, source, false, bn);
	map.put(source_name, new PortTag(null, "in0", PortTag.IN, source_width));
	String target_name = target.getFullName();
	map.put(target_name, new PortTag(null, "out", PortTag.OUT, target_width));

	dpCircuit.insertOperator(op_name, op, map);
      }
    } else {
      // throw exception ...
      throw new SynthesisException("Unknown cast operator "+inst);
    }
  }


  /**
   * This method implements the load operator in the datapath logic. Its 
   * simple in that it only makes IN ports from the regfile to the datapath.
   * It then relies on other operators to build a register if the value is 
   * not immediately consumed by any instruction. If the value is only 
   * immediately consumed then no registers will be connected by the 
   * consuming instructions.
   */
  private void caseLoad(Circuit dpCircuit, Instruction inst, BlockNode bn) {
    Circuit block = dpCircuit.getParent();
    Operand source = Load.getSource(inst);
    Operand target = Load.getResult(inst);
    String primal = source.getFullName();
    String local = target.getFullName();
    int width = inst.type().getWidth();

    // Deal with all the types of operands!
    if(target.isPrimal())
      throw new SynthesisException("register copy not allowed...");
    if(source.isConstant() || source.isBlock()) {
      // Create a buffer...
      String in = getOpName(dpCircuit, inst, source, false, bn);
      String out = target.getFullName();
      insertBuf(dpCircuit, "buf_"+dpCircuit.getUniqueName(), 
		in, out, width);
      return;
    } else if(!source.isPrimal()) {
      // Error!
      throw new SynthesisException("Unhandled operand for load");
    }

    String blk_primal = primal+block.getUniqueName();

    if(!_dpInWires.contains(primal)) {
      dpCircuit.insertInPort(blk_primal, "i_"+primal, primal, width);
      _dpInWires.add(primal);
      // Has a primal already been loaded? If not, make a path of 
      // wires and ports to the regfile.
      _regfile.addRegister(primal, "w_"+primal, "we_"+primal, 
			   "r_"+primal, width, 0);
    }
    if(!_blkWires.contains(primal)) {
      block.insertInPort("r_"+primal, "i_"+primal, blk_primal, width);
      _blkWires.add(primal);
    }
  }

  /**
   * This method implements the select operator in the datapath logic.
   */
  private void caseSelect(Circuit dpCircuit, Instruction inst, BlockNode bn) {
    Operand source1 = Select.getVal1(inst);
    Operand source2 = Select.getVal2(inst);
    Operand condition = Select.getCondition(inst);
    Operand target = Select.getResult(inst);
    String out = target.getFullName();

    boolean isPrimalResult = target.isPrimal();
    if(isPrimalResult)
      throw new SynthesisException("caseSelect: primal result found.");
    
    // Build a mux or buffer depending on how the conditional evaluates.
    if(condition == BooleanOperand.TRUE) {
      // Create a buffer with the 1st input.
      String in0 = getOpName(dpCircuit, inst, source1, false, bn);
      insertBuf(dpCircuit, 
		"buf_"+inst.operator+dpCircuit.getUniqueName(), 
		in0, out, inst.type().getWidth());
    } else if(condition == BooleanOperand.FALSE) {
      // Create a buffer with the 2nd input.
      String in1 = getOpName(dpCircuit, inst, source2, false, bn);
      insertBuf(dpCircuit, 
		"buf_"+inst.operator+dpCircuit.getUniqueName(), 
		in1, out, inst.type().getWidth());

    } else if (source1 == source2) {
      System.out.println("WARNING: mux with identical inputs");
       // Create a buffer with the 1st input.
      String in0 = getOpName(dpCircuit, inst, source1, false, bn);
      insertBuf(dpCircuit, 
		"buf_"+inst.operator+dpCircuit.getUniqueName(), 
		in0, out, inst.type().getWidth());
    } else {
      // Create a mux because the conditional isn't known yet...
      String in0 = getOpName(dpCircuit, inst, source2, false, bn);
      String in1 = getOpName(dpCircuit, inst, source1, false, bn);
      String sel = getOpName(dpCircuit, inst, condition, false, bn);
      String name = "mux_"+inst.operator+dpCircuit.getUniqueName();
      insertMux(dpCircuit, name, in0, in1, sel, out, inst.type().getWidth());
   }
  }


  /**
   * This method implements a store operator in the datapath logic.
   */
  private void caseStore(Circuit dpCircuit, Instruction inst, BlockNode bn) {
    Circuit blockCircuit = dpCircuit.getParent();
    Operand source = Store.getValue(inst);
    Operand target = Store.getDestination(inst);
    boolean isPrimalCopy = source.isPrimal();

    if(isPrimalCopy)  // Primal copies are not allowed!
      throw new SynthesisException("register copy not allowed...");

    // If this is a store to a block, then just use a buffer...
    if(target.isBlock() || target.isAddr()) {  
      String in = getOpName(dpCircuit, inst, source, false, bn);
      String out = target.getFullName();
      insertBuf(dpCircuit, "buf"+dpCircuit.getUniqueName(), 
		in, out, inst.type().getWidth());
      return;
    }

    // jt -- ugh

    // In this case there should only be one.
    String blkEn = inst.getOperand(0).getFullName()+"_we"; 
    
    String dataOut = makeDataMuxLogic(dpCircuit, inst, blkEn, bn);

    // Save the mux output and write enable to connect to outports later.  
    // The write enable wire is just the mux_out string plus "_en" added 
    // to the end.  The mux output and write enable will be added at the 
    // same time, so only one element needs to be added to the hashmap.  

    ConnectionPair predAndWidth = new ConnectionPair(blkEn, 
						     inst.type().getWidth());
    _blockOutWires.put(dataOut, predAndWidth);
  }


  /** 
   * This method implements test operations (i.e. setgt, setlt, ...) in 
   * the datapath logic.
   */
  private void caseTest(Circuit dpCircuit, Instruction inst, BlockNode bn) {
    Operand source1 = Test.getVal1(inst);
    Operand source2 = Test.getVal2(inst);

    Operation op = library.select(inst);
    if (op != null) {
      String op_name = "op_" +
	inst.operator+dpCircuit.getUniqueName();
      HashMap map = new HashMap();
      map.put(getOpName(dpCircuit, inst, source1, false, bn),
	      new PortTag(null, "in0", PortTag.IN, inst.type().getWidth()));
      map.put(getOpName(dpCircuit, inst, source2, false, bn), 
	      new PortTag(null, "in1", PortTag.IN, inst.type().getWidth()));
      map.put(inst.getOperand(0).getFullName(), 
	      new PortTag(null, "out", PortTag.OUT, 1));
      dpCircuit.insertOperator(op_name, op, map);
    } else {
      // throw exception ...
      throw new SynthesisException("Unknown test operator "+inst);
    }
  }

  /** 
   * This method implements unary operations (not, ...) in the datapath. 
   */
  private void caseUnary(Circuit dpCircuit, Instruction inst, BlockNode bn) { 
    Operand source = Unary.getVal(inst);
    Operand target = Unary.getResult(inst);
    String op_name = "op_"+inst.operator+dpCircuit.getParent().getUniqueName();
    Operation op = library.select(inst);
    if (op != null) {
      dpCircuit.insertOperator(op, op_name,
			       getOpName(dpCircuit, inst, source, false, bn), 
			       target.getFullName(),
			       inst.type().getWidth());
    } else {
      // throw exception ...
      throw new SynthesisException("Unknown unary operator "+inst);
    }
  }


  /**
   * This method creates an in port from the block level to the dataflow 
   * level.  It gives the state associated with this instruction.
   */
  private String insertStateInputFromSM(Circuit dp, int startCycle, 
					float runLength) {
    
    String cycle = "s%"+(int)(startCycle+runLength);

    // CHANGE: the following won't work with multiple blocks??
    //         this had to be done because getPortByName() returns some 
    //         odd results...
    if ( dp.getPortByName(getCircuitName(dp)+"_0."+"i_"+cycle+".0") != null ) {
      return cycle;
    }

    insertInPort(dp, cycle, "i_"+cycle, cycle, 1);

    return cycle;
  }
  private String insertStateInputFromSM(Circuit dp, Instruction inst) {
    return insertStateInputFromSM(dp, inst.getExecClkCnt(), 
				  inst.getRunLength());
  }

  /**
   * This method connects the definition of an operand to its use in the 
   * current cycle. Pipeline registers (or a single register) are added 
   * if necessary.  A string is returned with the name of the wire.
   */
  public String makeOpConnection(Circuit dp, String operand, int cur_cycle, 
				 BlockNode bn, boolean isPredicate, 
				 boolean isAddress) {
    String wire_name = null;
    Instruction def_inst = null;
    int def_end_cycle = -1;
    Operand result = null;

    // this looks really bad -- it needs to be fixed.
    /*
      Why do we need to search through all the instructions?  What about
      the def-use hashes?  Why can't we have the instruction?
    */

    // Find the most recent def of the current operand.
    for(Iterator it = bn.getInstructions().iterator(); it.hasNext(); ) {
      Instruction i_inst = (Instruction) it.next();
      int i_start_cycle = i_inst.getExecClkCnt();
      int i_end_cycle = i_start_cycle + (int)i_inst.getRunLength();

      // Don't check any instructions that are scheduled later than 
      // the current instruction.
      if(i_start_cycle > cur_cycle) break;
      // but don't look at operands that aren't available yet
      if(i_end_cycle > cur_cycle) continue;

      // identify the instruction def
      Operand i_result = AStore.conforms(i_inst) 
        ? AStore.getAddrDestination(i_inst) : i_inst.getOperand(0);
      if(i_result.toString().equals(operand)) {
	def_inst = i_inst;
	def_end_cycle = i_end_cycle;
        result = i_result;
      }
    }
    
    //System.out.println(" Op "+operand);
    //System.out.println("def_end_cycle "+def_end_cycle+" cur_cycle "+cur_cycle);


    if(def_end_cycle == cur_cycle) {
      // This is where immediate consumption of the LHS operand occurs. 
      // So, no registers are needed.
      if(Load.conforms(def_inst)) 
      	wire_name = Load.getSource(def_inst).getFullName();
      else if(ALoad.conforms(def_inst))
	wire_name = ALoad.getPrimalSource(def_inst).getFullName()+"_"+
	  ALoad.getResult(def_inst).getFullName();
      /*
	wire_name = def_inst.getOperand(2).getFullName()+"_"+
	  def_inst.getOperand(0).getFullName();
      */
      else 
      	wire_name = result.getFullName();
    } else if((def_end_cycle < cur_cycle) && (def_end_cycle >= 0)) {
      // This is where delayed consumption of the LHS operand occurs.  
      // So, create as many pipeline registers as required.
      
      // Get the width of the operand.
      int width = 0;
      if(isPredicate)      // Predicate logic is boolean, so of width 1.
        width = 1;
      else if(isAddress)   // Address width should be separate from others.
        width = ADDR_WIDTH;
      else if(Cast.conforms(def_inst))  
	if (operand.equals(Cast.getResult(def_inst).toString())) 
	  width = Cast.getType(def_inst).getType().getWidth();
	else 
	  width = def_inst.type().getWidth();
      else // Otherwise, width is variable.
        width = def_inst.type().getWidth();

      if(isPipelined()) {
	// Add pipeline registers...
        // CHANGE: is this wire name correct???
        //wire_name = def_inst.getOperand(0).getFullName()+"_"+
        //(cur_cycle-def_end_cycle);
        wire_name = result.getFullName()+"_"+cur_cycle;
        String in_name = null;
        if(Load.conforms(def_inst))
          in_name = Load.getSource(def_inst).getFullName();
        else if(ALoad.conforms(def_inst))
          in_name = ALoad.getPrimalSource(def_inst).getFullName() + "_" + 
            result.getFullName();
        else
          in_name = result.getFullName();
        insertPipelineRegisters(dp,"reg_"+result.getFullName(), 
          in_name, width,	cur_cycle - def_end_cycle);
      } else {
	// Add a single register because this design isn't pipelined...
      	wire_name=result.getFullName()+"_s%"+(def_end_cycle+1);
      	String in_name = null;
      	if(Load.conforms(def_inst)) 
          in_name = Load.getSource(def_inst).getFullName();
        else if(ALoad.conforms(def_inst))
          in_name = ALoad.getPrimalSource(def_inst).getFullName() + "_" + 
            result.getFullName();
        else
          in_name = result.getFullName();
        if(!_dpRegisters.containsKey(result.getFullName())) {
	  dp.insertRegister("reg_"+result.getFullName(), in_name, 
            insertStateInputFromSM(dp, def_inst), wire_name, width, 0);
          _dpRegisters.put(result.getFullName(), null);
        }
      }
    } else {  
      // Error case where no previous definitions of the operand were found.
      throw new SynthesisException("ERROR in makeOpConnection() -- "+
        "no previous DEFINITION found:\n"+operand+
        ", cur:" + cur_cycle);
    }

    return wire_name;
  }
  public String makeOpConnection(Circuit dp, String operand, int cur_cycle, 
				 BlockNode bn, boolean isPredicate) {
    return makeOpConnection(dp, operand, cur_cycle, bn, isPredicate, false);
  }

  /**
   * This method determines how to set up the data (whether the data is 
   * in the form of a constant or block or primal) and then follows 
   * through by generating it.  This method determines whether a register 
   * is needed between whatever the connection is--this is much more 
   * simplified in that registers are only created here, and then, only 
   * if they are needed. They are only needed if the LHS operand isn't 
   * immediately consumed.
   */
  protected String getOpName(Circuit dp, Instruction cur_inst,  
			     Operand operand, boolean isStore, BlockNode bn, 
			     boolean isAddress) {
    String wire_name = null;
    int width;

    //System.out.println("getOpName inst "+cur_inst+" op "+operand);
    //System.out.println(" isStore "+isStore);


    if (isAddress) 
      width = ADDR_WIDTH;
    else if (Cast.conforms(cur_inst)) {
      // cast needs its own special case :(
      if (operand == Cast.getResult(cur_inst)) {
	width = Cast.getType(cur_inst).getType().getWidth();
      } else {
	width = cur_inst.type().getWidth();
      } 
    } else {
      // common case
      width = cur_inst.type().getWidth();
    }
      
    // jlt old method
    //int width = (isAddress) ? ADDR_WIDTH : cur_inst.type().getWidth();
    /*
      ?? Future method ?
    int width = (isAddress) ? MemoryInterfaceGenerator.ADDR_WIDTH :
                              cur_inst.type().getWidth();
    */
    if (operand.isBlock()) {
      wire_name = makeOpConnection(dp, operand.toString(), 
				   cur_inst.getExecClkCnt(), bn, false, 
				   isAddress);
    } else if(operand.isAddr()) {
      wire_name = makeOpConnection(dp, operand.toString(), 
				   cur_inst.getExecClkCnt(), bn, false, 
				   isAddress);
    } else if(operand.isBoolean()) {
      wire_name = makeOpConnection(dp, operand.toString(),
				   cur_inst.getExecClkCnt(), bn, true, 
				   isAddress);
    } else if( operand.isConstant() ) {
      // If the operand is a constant, then create a constant...
      /*
      if(ALoad.conforms(cur_inst))
	wire_name = dp.getParent().getUniqueWireName();
      else
      */
      wire_name = dp.getUniqueWireName();

      // Depending on the type of the operand, insert the specific constant
      if(operand.isFloatConstant()) 
	dp.insertConstant("c_"+((FloatConstantOperand)operand).getValue(), 
			  ((FloatConstantOperand)operand).getValue(),
			  wire_name, width);
      else if(operand.isDoubleConstant()) 
	dp.insertConstant("c_"+((DoubleConstantOperand)operand).getValue(), 
			  ((DoubleConstantOperand)operand).getValue(),
			  wire_name, width);
      else if(operand.isIntConstant()) 
	dp.insertConstant("c_"+((IntConstantOperand)operand).getValue(), 
			  ((IntConstantOperand)operand).getValue(),
			  wire_name, width);
      else 
	throw new SynthesisException("getOpName() -- type not yet supported");            
    } else if (operand.isPrimal()) {
      throw new SynthesisException("getOpName(): primal found...");
    } else {
      System.out.println("getOpName -- type of operand not yet supported");
      System.out.println("         "+cur_inst);
      throw new SynthesisException();
    }

    //System.out.println(" wire_name "+wire_name);
    // Create a port out, if this is a store instruction.
    if(isStore && !_dpOutWires.contains(wire_name)) {
      String portIn = wire_name;
      /*
	Here Jeff gets a unique wire name, but does not
	use it for the next request of that same wire.
	So, this is an incorrect implementation.  I have
	modified it so that it will use the same name always
	and we will leave the unique wire for another day.

	jlt -- does this really belong here?  If an instruction is a store,
	it can take care of this itself...  Why push the work to this
	function?

      wire_name = dp.getParent().getUniqueWireName();
      dp.insertOutPort(portIn, "o_"+portIn, wire_name, width);
      */
      dp.insertOutPort(portIn, "o_"+portIn, portIn, width);
      _dpOutWires.add(portIn);
    }

    return wire_name;
  }
  
  protected String getOpName(Circuit dp, Instruction cur_inst, 
			     Operand operand, 
			     boolean isStore, BlockNode bn) {
    return getOpName(dp, cur_inst, operand, isStore, bn, false);
  }

  /**
   * This method makes all of the current block's out ports.  It does this 
   * after creating the datapath to take care of the case where a STORE 
   * writes to the same variable during different cycles.
   */
  private HashSet makeBlockOutPorts(Circuit block) {
    Set entries = _blockOutWires.entrySet();
    String data_port_input = null;
    String en_port_input = null;
    HashSet processed = new HashSet();
    HashSet wiresToRegfile = new HashSet();

    for(Iterator i1 = entries.iterator(); i1.hasNext(); ) {
      int n_added = 0;

      Map.Entry e1 = (Map.Entry) i1.next();

      // Skip this entry if already processed.
      if(processed.contains(e1)) 
	continue;

      HashMap data_map = new HashMap();
      HashMap en_map = new HashMap();
      
      // Separate the state info from the key; save the operand info.
      String wirename = (String)e1.getKey();
      String operand1 = wirename.replaceFirst("_s%[0-9]+$", "");
      String state1 = wirename.substring(wirename.lastIndexOf("_s%"));

      // Get the info on the connecting wire.
      ConnectionPair pair = (ConnectionPair) e1.getValue();
      String pred = pair.wireName;
      int width = pair.width;
      
      // Find all matching map values (operands) and put them in another 
      // map to add to an OR operator.
      for(Iterator i2 = entries.iterator(); i2.hasNext(); ) {
	Map.Entry e2 = (Map.Entry) i2.next();
	String wirename2 = (String)e2.getKey();
	String operand2 = wirename2.replaceFirst("_s%[0-9]+$", "");
	String state2 = wirename2.substring(wirename2.lastIndexOf("_s%"));

	// If the operands are the same, but not necessarily the cycle...
	// then add the mux outputs to the inputs of the OR gate.
	if(operand1.equals(operand2)) {
	  data_map.put(e2.getKey(), 
		       new PortTag(null, (String)e2.getKey(), PortTag.IN, 
				   width));
 	  en_map.put(operand1+"_"+state2+"_we", 
 		     new PortTag(null, operand1+"_"+state2+"_we", 
				 PortTag.IN, 1));
	  processed.add(e2);
	  n_added++;
	}
      }

      // If only one input was added, then don't create an OR gate, just 
      // an OUT port.
      if(n_added == 1) {

	// Name the input wires to the OUT port.
	data_port_input = (String) e1.getKey();
	en_port_input   = pred;

	// Add the OUT ports.
	String block_name = getCircuitName(block);
	block.insertOutPort(data_port_input, "o_"+operand1, 
			    block_name+"_"+operand1, 
			    width);
	block.insertOutPort(en_port_input, "o_"+operand1+"_we", 
			    block_name+"_"+operand1+"_we", 1);
	String addWireName = operand1+"/"+width;
	wiresToRegfile.add(addWireName);
	//ConnectionPair cp = new ConnectionPair(operand1, width);
	//wiresToRegfile.add(cp);
	_regfile.addRegister(operand1, "w_"+operand1, "we_"+operand1,
                             "r_"+operand1, width, 0);
      } else if(n_added > 1) {

	// Shouldn't allow multiple stores to the same primal in the same 
	// hyperblock...
	throw new SynthesisException("2+ stores to primal in hyperblock!" + 
				     "\n    Primal = " + operand1);
      }
      
      // Remove the current map entry now that it has been processed.
      i1.remove();
    }
    return wiresToRegfile;
  }

  /**
   * This method takes a boolean equation (representing the predicate) and 
   * generates the logic for the circuit.  This is usually used for store 
   * operations...
   */
  protected String makePredicateLogic(Circuit graph, BooleanEquation eq,
				      int cycle, BlockNode bn) {
    if(!predHasBeenDefined(eq, cycle, bn)) {
      System.out.println("PREDICATE NOT DEFINED!  "+eq+" -- cycle: "+cycle);
      return (String) null;
    }

    LinkedList or_inputs = new LinkedList();
    for(ListIterator iter = eq.getTwoLevel().listIterator();iter.hasNext(); ) {
      LinkedList and_inputs = new LinkedList();
      LinkedList list = (LinkedList) iter.next();
      for(ListIterator list_iter = list.listIterator();
          list_iter.hasNext(); ) {
	String invOut = null;
	BooleanOp term = (BooleanOp)list_iter.next();
	String operand = term.getOp().toString();
	String toAdd = makeOpConnection(graph, operand, cycle, bn, true);
	String name = null;

	if(term.getSense() == false) {
	  // CHANGE: the following is a hack! there doesn't yet appear 
	  //         to be a way around this...How do i fix this????
	  if(term.getOp().getBoolName().trim().startsWith("(BOOLEAN) %")) {
	    int last = term.getOp().getBoolName().lastIndexOf('%');
	    name = term.getOp().getBoolName().substring(last+1);
	    invOut = "inv%"+name;
	    //	  invOut = "dp_inv_"+term.getOp().getBoolName();
	  } else {
	    throw new SynthesisException("ERROR boolean mismatch: " + invOut);
	  }
	  if(!_dpInvWires.contains(invOut)) {
	    graph.insertOperator(Operation.NOT, invOut, toAdd, invOut, 1);
	    _dpInvWires.add(invOut);
	  }
	}
	and_inputs.add((term.getSense()) ? toAdd : invOut);
      }
      String lower = makeLogicTree(graph, Operation.AND, and_inputs, 1);
      or_inputs.add(lower);
    }
    // Return the output from the logic tree made from the OR list.
    String out = makeLogicTree(graph, Operation.OR, or_inputs, 1);
    _predicateWires.put(eq.toString(), out); 
    return out;
  }

  /**
   * This method checks whether an operand has been defined previous to the 
   * given cycle.  It returns true if it has been defined and false otherwise.
   */
  static protected boolean hasBeenDefined(String operand, int cycle, 
					  BlockNode bn) {
    // Find the last definition of the operand before it's use in the 
    // current cycle.
    Iterator it = bn.getInstructions().iterator();
    while(it.hasNext()) {
      Instruction i = (Instruction) it.next();
      // If this inst finishes before the cycle when the use occurs...
      int end_cycle = i.getExecClkCnt() + (int) i.getRunLength();
      if(end_cycle < cycle)
	// If this inst defines the operand...
	if(i.getOperand(0).toString().trim().equals(operand)) 
	  return true;	
    }

    return false;
  }

  /**
   * This method checks whether a boolean equation has been defined previous 
   * to the given cycle. It returns true if all parts of the boolean equation 
   * have been defined and false otherwise.
   */
  static protected boolean predHasBeenDefined(BooleanEquation be, int cycle, 
					      BlockNode bn) {
    // For each BooleanOp in the BooleanEquation, check if it has been 
    // defined.  If any BooleanOp has not been defined, then return false.
    for(ListIterator iter = be.getTwoLevel().listIterator();iter.hasNext(); ) {
      Iterator list_iter = ((LinkedList)iter.next()).listIterator();
      while(list_iter.hasNext()) {
	String operand = ((BooleanOp)list_iter.next()).getOp().getBoolName();
	if(!hasBeenDefined(operand, cycle, bn))
	  return false;
      }
    }
    return true;
  }


  /**
   * This method creates 'num' pipeline registers after the input wire.  
   * The names of the wires out of each pipeline register begin with 
   * the specified 'name' and end with a number specifying the pipeline 
   * register stage.
   */
  public void insertPipelineRegisters(Circuit circuit, String name, 
				      String input, int width, int num) {
    // Create the connected pipeline registers.
    for(int i = 1; i <= num; i++) {
      if(!_dpRegisters.containsKey(name+"_"+i)) {
	String in = (i == 1) ? input : name+"_"+(i-1);
	String out = name+"_"+i;
	circuit.insertRegister(name+"_"+i, in, null, out, width, 0);
	_dpRegisters.put(name+"_"+i, null);
      }
    }
  }

  HashMap getPredicateWires() {
    return _predicateWires;
  }

  HashSet getDpOutWires() {
    return _dpOutWires;
  }

}

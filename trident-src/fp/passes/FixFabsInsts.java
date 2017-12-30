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


package fp.passes;

import java.util.*;

import fp.flowgraph.*;


public class FixFabsInsts extends Pass implements GraphPass, Operators {
  /** 
   * This pass looks for instructions that were created by LLVM (?) from 
   * converting the C fabs() instruction. An example fabs "triple" is the 
   * following:
   *
   * (BOOLEAN)%bool = setge  float (BLOCK)%blk1,     (FLOAT)%const0.0
   * (BLOCK)  %blk2 = sub    float (FLOAT)%const0.0, (BLOCK)%blk1
   * (BLOCK)  %abs  = select float (BOOLEAN)%bool,   (BLOCK)%blk1, (BLOCK)%blk2
   *
   * The reasoning behind fixing this is due to the simplicity of a floating 
   * point absolute value operator implemented in hardware. This only requires 
   * forcing the MSB to 0 (in most cases). By removing the fabs triples, 
   * 3 expensive floating point operations are removed from the datapath. The 
   * triples are replaced by a FABS instruction that will be handled by the 
   * CircuitGenerators.
   */


  public FixFabsInsts(PassManager pm) {
    super(pm);
  }

  public String name() { return "FixFabsInsts"; }

  /**
   * This method searches for fabs triples, removes them, and adds the 
   * corresponding FABS instructions.
   */
  public boolean optimize(BlockGraph graph) {
    // For each block node...
    for(Iterator bIt = graph.getAllNodes().iterator(); bIt.hasNext(); ) {
      BlockNode bn = (BlockNode) bIt.next();

      // Find all of the corresponding fabs instructions in this block.
      LinkedList blkTriples = findFabsTriples(bn);

      // For each fabs triple...
      for(Iterator lIt = blkTriples.iterator(); lIt.hasNext(); ) {
	Triple triple = (Triple) lIt.next();
	Instruction sub = triple.getSub();
	Instruction sge = triple.getSge();
	Instruction sel = triple.getSel();

	// Make/add the FABS call.
	// Is this OK to use sub's type and predicate?  It should be...
	Instruction fabs = new Instruction(Operators.ABS, 
					   sub.type(), 2, 
					   sub.getPredicate());
	Operand def = sel.getOperand(0);
	Operand use = sge.getOperand(1);
	fabs.putOperand(0, def);
	fabs.putOperand(1, use);
	bn.addInstruction(fabs);

	// Remove the fabs instructions.
	bn.removeInstruction(sub);
	bn.removeInstruction(sge);
	bn.removeInstruction(sel);

	//System.out.println("REMOVING TRIPLE");
	//System.out.println("\t"+sge);
	//System.out.println("\t"+sub);
	//System.out.println("\t"+sel+"\n");
      }

    }

    return true;
  }


  private LinkedList findFabsTriples(BlockNode bn) {
    LinkedList triples = new LinkedList();

    // For each instruction in this block node...
    for(Iterator iIt = bn.getInstructions().iterator(); iIt.hasNext(); ) {
      Instruction inst = (Instruction) iIt.next();

      // If this is a FP select instruction...
      if(Select.conforms(inst) && inst.type().isFloat()) {
	// Record this instruction's operands.
	Operand bool = Select.getCondition(inst);
	Operand blk1 = Select.getVal1(inst);
	Operand blk2 = Select.getVal2(inst);
	
	// Look for corresponding sub and setgt instructions.
	Instruction sub = findSub(bn, blk2, blk1);
	Instruction sge = findSge(bn, bool, blk1);

	// If these instructions were found, then a triple should be formed...
	if((sub != null) && (sge != null)) {
	  triples.add(new Triple(inst, sub, sge));
	}
      }
    }
    return triples;
  }

  private Instruction findSub(BlockNode bn, Operand defOp, Operand useOp) {
    // For each instruction in this block node...
    for(Iterator iIt = bn.getInstructions().iterator(); iIt.hasNext(); ) {
      Instruction inst = (Instruction) iIt.next();
      // If this is a FP subtraction instruction...
      if((inst.operator.opcode == SUB_opcode) && inst.type().isFloat()) {
	// If this fp sub has the correct def and uses...
	if(Binary.getResult(inst).equals(defOp) &&
	   hasZeroConst(inst, 1) &&
	   Binary.getVal2(inst).equals(useOp)) {
	  return inst;
	}
      }
    }
    return (Instruction) null;
  }

  private Instruction findSge(BlockNode bn, Operand defOp, Operand useOp) {
    // For each instruction in this block node...
    for(Iterator iIt = bn.getInstructions().iterator(); iIt.hasNext(); ) {
      Instruction inst = (Instruction) iIt.next();
      // If this is a FP setge instruction...
      if((inst.operator.opcode == SETGE_opcode) && inst.type().isFloat()) {
	// If this fp setge has the correct def and uses...
	if(Test.getResult(inst).equals(defOp) &&
	   Test.getVal1(inst).equals(useOp) &&
	   hasZeroConst(inst, 2)) {
	  return inst;
	}
      }
    }
    return (Instruction) null;
  }

  private boolean hasZeroConst(Instruction inst, int opNum) {
    Operand op = inst.getOperand(opNum);
    if(!op.isFloatConstant()) return false;
    return (((FloatConstantOperand)op).getValue() == 0.0);
  }


  private class Triple {
    private Instruction sel;
    private Instruction sub;
    private Instruction sge;

    Triple(Instruction sel, Instruction sub, Instruction sge) {
      this.sel = sel;
      this.sub = sub;
      this.sge = sge;
    }

    Instruction getSel() { return sel; }
    Instruction getSub() { return sub; }
    Instruction getSge() { return sge; }

    void setSel(Instruction sel) { this.sel = sel; }
    void setSub(Instruction sub) { this.sub = sub; }
    void setSge(Instruction sge) { this.sge = sge; }

    public String toString() { return "[ "+sel+"; "+sub+"; "+sge+" ]"; }
  }

}

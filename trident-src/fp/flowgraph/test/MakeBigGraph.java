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


package fp.flowgraph.test;

import fp.flowgraph.*;

import fp.passes.AddPredicates;
import fp.passes.GraphPass;

public class MakeBigGraph extends BlockGraph implements Operators {

  MakeBigGraph() {
    super();
    BlockNode a, b;

    a = (BlockNode)addNode();

    MakeBlock mb = new MakeBlock(a);
    
    // need to fix up last branch in mb
    int size = a.getInstructions().size();
    Instruction branch = (Instruction)a.getInstructions().get(size - 1);
    a.removeInstruction(branch);

    Branch.setTarget2(branch, Operand.newLabel("if",0));
    a.addInstruction(branch);

    b = (BlockNode)addNode();
    addBlock(b);

    ((ControlFlowEdge)addEdge(ENTRY, a)).setLabel(Boolean.TRUE);
    ((ControlFlowEdge)addEdge(a,a)).setLabel(Boolean.TRUE);
    ((ControlFlowEdge)addEdge(a,b)).setLabel(Boolean.FALSE);
    ((ControlFlowEdge)addEdge(b, EXIT)).setLabel(Boolean.TRUE);

  }

  void addBlock(BlockNode b) {
    Instruction inst;
    
    // set name of block
    b.setName("if",0);

    // make some operands
    Operand tmp0 = Operand.nextBlock("tmp");
    PrimalOperand a = Operand.newPrimal("a");

    // add an instruction via InstructionFormat
    inst = Load.create(LOAD, Type.Int, tmp0, a);
    b.addInstruction(inst);

    // make some more operands
    Operand tmp1 = Operand.nextBlock("tmp");
    Operand one = Operand.newIntConstant(1);
    
    // add a binary instruction
    inst = Binary.create(ADD, Type.Int, tmp1, tmp0, one);
    b.addInstruction(inst);

    // add a store instruction.
    inst = Store.create(STORE, Type.Int, a, tmp1);
    b.addInstruction(inst);

    inst = Return.create(RET);
    b.addInstruction(inst);
  }


  public static void main(String args[]) {
    MakeBigGraph mg = new MakeBigGraph();
    mg.writeDotFile("mbg_test.dot");

    //GraphPass pass = new AddPredicates();
    //pass.optimize(mg);
    
    //mg.writeDotFile("mbg2_test.dot");
  }

}

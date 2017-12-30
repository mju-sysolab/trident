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


package fp.hardware;

import java.util.*;

import fp.flowgraph.*;
import fp.*;
import fp.passes.OperationSelection;
import fp.hardware.AllocateArrays.ArrayInfo;
import fp.hwdesc.Memory;

  /**
   * This class locates all getelementptr instructions and converts them to 
   * instructions that calculate the address to be accessed.
   */
public class ConvertGepInstructions {
  private HashSet _gepsToRemove;
  private HashSet _instsToAdd;
  private OperationSelection _opSel;

  public ConvertGepInstructions(OperationSelection opSel) {
    _opSel = opSel;
  }

  // Find each GEP instruction and convert it...
  public boolean gepConversion(BlockGraph graph) {
    ChipDef chipInfo = GlobalOptions.chipDef;

    // For each block...
    for(Iterator bIt = graph.getAllNodes().iterator(); bIt.hasNext(); ) {
      BlockNode bn = (BlockNode) bIt.next();
      // Allocate the hashsets to record what to add/remove.
      _gepsToRemove = new HashSet();
      _instsToAdd = new HashSet();
      // For each Gep instruction...
      for(Iterator iIt = bn.getInstructions().iterator(); iIt.hasNext(); ) {
	Instruction inst = (Instruction) iIt.next();
	if(Getelementptr.conforms(inst)) {
	  // Create new instructions for the GEP inst.
	  convertGep(bn, inst, chipInfo);
	}
      }
      // Add all of the instructions needed for the GEP conversion.
      for(Iterator aIt = _instsToAdd.iterator(); aIt.hasNext(); ) {
	Instruction aInst = (Instruction) aIt.next();
	_opSel.select(aInst); // Set the correct version of aInst.
	System.out.println("adding  : "+aInst);
	bn.addInstruction(aInst);
      }
      // Remove all of the GEP instructions from this block node.
      for(Iterator rIt = _gepsToRemove.iterator(); rIt.hasNext(); ) {
	Instruction rInst = (Instruction) rIt.next();
	System.out.println("removing: "+rInst);
	bn.removeInstruction(rInst);
      }
    }
    return true;
  }


  /**
   * This method determines which instructions need to be added in order for 
   * the calculation of the address specified by the GEP instruction.
   */
  private void convertGep(BlockNode bn, Instruction inst, ChipDef chipInfo) {
    Memory mb = chipInfo.findMemoryBlock(Getelementptr.getArrayVar(inst));
    String opName = Getelementptr.getArrayVar(inst).getFullName();
    ArrayInfo arrayInfo = (ArrayInfo) mb.getArrayInfo(opName);
    LinkedList dInfo = arrayInfo.dimInfo;

    // Calculate index...
    Operand indexOp = calcIndex(inst, dInfo);

    // Calculate index shift...
    int wordSize = mb.getWidth();
    int atomicWidth = arrayInfo.type.getWidth();
    int test = atomicWidth/wordSize;
    double logBase2 = (test >= 1) ? (Math.log(test)/Math.log(2)) : 0.0;
    Operand logOp = new IntConstantOperand((int)logBase2);

    System.out.println(" indexOp "+indexOp+" wordSize "+wordSize+" atomicWidth "+atomicWidth+" test "+test+" logOp "+logOp);


    Operand shlOp = Operand.nextBlock("gep");
    Instruction shiftInst = Shift.create(Operators.SHL, Type.Int, 
					 shlOp, indexOp, logOp, 
					 inst.getPredicate());
    _instsToAdd.add(shiftInst);

    // Calculate address offset...
    int arrayAddr = arrayInfo.addr;
    Operand addrOp = Getelementptr.getResult(inst);
    Instruction addInst = Binary.create(Operators.ADD, Type.Int, addrOp, shlOp,
					new IntConstantOperand(arrayAddr), 
					inst.getPredicate());
    _instsToAdd.add(addInst);

    System.out.println(" arrayAddr "+arrayAddr+" op "+addrOp);
    
    // Remove the GEP instruction from this block node at a later time...
    _gepsToRemove.add(inst);
  }


  /**
   * Returns a linked list with the index operands of the specified GEP inst.
   */
  private LinkedList getIndices(Instruction inst) {
    if(!Getelementptr.conforms(inst))
      return (LinkedList) null;

    int i = 1;  // Skip the first element (its the type of element).
    LinkedList indices = new LinkedList();

    while(Getelementptr.hasVal(inst, i))
      indices.add(Getelementptr.getValOperand(inst, i++));

    return indices;
  }

  private Operand calcIndex(Instruction inst, LinkedList dimInfo) {
    // For each index operand in the Gep instruction (reverse order)...
    Operand prevTerm = null;
    LinkedList indices = getIndices(inst);
    System.out.println("calcIndex() -- dimInfo: "+dimInfo);
    System.out.println("            -- indices: "+indices);
    int i = indices.size()-1, nElements = 1;
    for(ListIterator it=indices.listIterator(indices.size());
	it.hasPrevious(); ) {
      Operand operand = (Operand) it.previous();
      System.out.println("index operand: "+operand);

      // Mul this index operand with the number of elements.
      Operand mulOp = Operand.nextBlock("gep");
      Instruction mulInst = Binary.create(Operators.MUL, Type.Int, mulOp, 
					  operand, 
					  new IntConstantOperand(nElements), 
					  inst.getPredicate());
      _instsToAdd.add(mulInst);
      System.out.println(mulInst);
      // If there was a previous term, then add that with this term...
      if(prevTerm != null) {
	Operand addOp = Operand.nextBlock("gep");
	Instruction addInst = Binary.create(Operators.ADD, Type.Int, 
					    addOp, mulOp, prevTerm, 
					    inst.getPredicate());
	_instsToAdd.add(addInst);
	System.out.println(addInst);
	prevTerm = addOp;
      } else {
	prevTerm = mulOp;
      }
      nElements *= ((Integer)dimInfo.get(i--)).intValue();
    }

    // Return the final index calculation.
    return prevTerm;
  }


  /** THIS IS AN OLD METHOD
   * This method determines which instructions need to be added in order for 
   * the calculation of the address specified by the GEP instruction.
   */
  private void convertGepOLD(BlockNode bn, Instruction inst, ChipDef chipInfo) {
    Memory mb = chipInfo.findMemoryBlock(Getelementptr.getArrayVar(inst));
    String opName = Getelementptr.getArrayVar(inst).getFullName();
    ArrayInfo arrayInfo = (ArrayInfo) mb.getArrayInfo(opName);

    int arrayAddr = arrayInfo.addr;
    int wordSize = mb.getWidth();
    LinkedList dInfo = arrayInfo.dimInfo;
    int nDims = dInfo.size();
    int atomicWidth = arrayInfo.type.getWidth();

    // Remove the GEP instruction from this block node at a later time...
    _gepsToRemove.add(inst);

    // Get array index...
    int test = atomicWidth/wordSize;
    double logBase2 = (test >= 1) ? (Math.log(test)/Math.log(2)) : 0.0;

    // Set up the operands...
    Operand shlResultOp = null;
    Operand addrOp = Getelementptr.getResult(inst);
    Operand indexOp = null;

    if(nDims == 1) {
      indexOp = Getelementptr.getValOperand(inst, 1);
    } else if(nDims == 2) {
      if((logBase2 == 0) && (arrayAddr == 0)) {
	//System.out.println("calcIndex2D() w/ addr = 0");
	calcIndex2D(inst, dInfo, addrOp);
	return;
      } else {
	//System.out.println("calcIndex2D() w/out addr");
	indexOp = calcIndex2D(inst, dInfo);      
      }
    } else if(nDims > 2)  // Don't allow >2-dim arrays yet...
      throw new ConvertGepInstException("Can't yet handle >2-dim arrays");

    if((logBase2 != 0)) {
      // Add a shift instruction to the block node.
      shlResultOp = (arrayAddr != 0) ? Operand.nextBlock("gep") : addrOp;
      Operand logOp = new IntConstantOperand((int)logBase2);
      Instruction shiftInst = Shift.create(Operators.SHL, Type.Int, 
					   shlResultOp, indexOp, logOp, 
					   inst.getPredicate());
      _instsToAdd.add(shiftInst);
    }

    if(arrayAddr != 0) {
      // Add an add instruction to the block node.
      // CHANGE: is this correct?
      Operand inOp1 = (shlResultOp != null) ? shlResultOp : indexOp;
      Operand inOp2 = new IntConstantOperand(arrayAddr);
      Instruction addInst = Binary.create(Operators.ADD, Type.Int, addrOp,
					  inOp1, inOp2, inst.getPredicate());
      _instsToAdd.add(addInst);
    }

    if((logBase2 == 0.0) && (arrayAddr == 0) && (nDims < 2)) {
      // Neither a shift nor add instruction was added...so must instead do 
      // a store from the (BLOCK) index to the address operand.
      Instruction storeInst = Store.create(Operators.STORE, Type.Int, addrOp, 
					   indexOp, inst.getPredicate());
      _instsToAdd.add(storeInst);
    }

  }

  /**THIS IS AN OLD METHOD
   * This method calculates (and/or creates instructions to find) the index 
   * to a 2-dimensional array. Calculates index = (i1*rowLeng)+i2 , where i1 
   * is the first index and i2 is the second index.
   */
  private Operand calcIndex2D(Instruction inst, LinkedList dimInfo, 
			      Operand addressOp) {
    Operand index1 = Getelementptr.getValOperand(inst, 1);
    Operand index2 = Getelementptr.getValOperand(inst, 2);
    String index1Name = index1.getFullName();
    String index2Name = index2.getFullName();
    
    int rowLen = ((Integer)dimInfo.getLast()).intValue();

    // If both indices are constant, then the index is fully known...
    if((index1 instanceof ConstantOperand) && 
       (index2 instanceof ConstantOperand)) {
      int i1Val = ((IntConstantOperand)index1).getValue();
      int i2Val = ((IntConstantOperand)index2).getValue();
      Operand op = new IntConstantOperand(i1Val*rowLen + i2Val);
      if(addressOp == null) 
	return op;
      Instruction storeInst = Store.create(Operators.STORE, Type.Int, 
					   addressOp, op, inst.getPredicate());
      _instsToAdd.add(storeInst);
      return addressOp;
    }

    // Check if only the first operand is known (because of first check)...
    Operand tmpOp = null;
    if(index1 instanceof ConstantOperand) {
      // If only the first index is known, then don't need a multiply.
      int i1Val = ((IntConstantOperand)index1).getValue();
      tmpOp = new IntConstantOperand(i1Val*rowLen);
    } else {
      boolean bExit = false;
      tmpOp = Operand.nextBlock("gep");

      // If the second index is a constant zero...
      if((index2 instanceof ConstantOperand) && 
	 (((IntConstantOperand)index2).getValue() == 0)) {
	if(addressOp != null) // If the address offset is already known...
	  tmpOp = addressOp;
	bExit = true; // Return without inserting an add operation.
      }

      // A multiply is needed...This will get changed to a series of 
      // shift operations in a later pass (so not too expensive).
      Operand constOp = new IntConstantOperand(rowLen);
      Instruction mulInst = Binary.create(Operators.MUL, Type.Int, tmpOp, 
					  index1, constOp, 
					  inst.getPredicate());
      _instsToAdd.add(mulInst);

      if(bExit)  
	return (Operand) null;
    }

    // Create an add instruction for the second index...    
    Operand indexOp = (addressOp == null) ? Operand.nextBlock("gep") : 
                                            addressOp;
    Instruction addInst = Binary.create(Operators.ADD, Type.Int, indexOp, 
					tmpOp, index2, inst.getPredicate());
    _instsToAdd.add(addInst);    
    return indexOp;
  }
  private Operand calcIndex2D(Instruction inst, LinkedList dimInfo) {
    return calcIndex2D(inst, dimInfo, null);
  }


  public String name() {
    return "ConvertGepInst"; 
  }

  private class ConvertGepInstException extends RuntimeException {
    public ConvertGepInstException() {
      super();
    }
    public ConvertGepInstException(String message) {
      super("\n"+message);
    }
  }

}

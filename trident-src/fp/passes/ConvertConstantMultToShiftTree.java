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

import fp.flowgraph.*;
import fp.util.Bit;
import fp.util.BooleanEquation;
import java.util.*;

public class ConvertConstantMultToShiftTree extends Pass implements BlockPass {

  /*
     The purpose of this pass is to convert a multiplication that has one 
     constant operand to a series of shifts, subtractions and additions.  
     This code creates a balanced tree of operations.
     Some code from optimize() and from some small routines at the end 
     was taken from Sea Cucumber.
     
   */

  public ConvertConstantMultToShiftTree(PassManager pm) {
    super(pm);
  }

  public boolean optimize(BlockNode node) {

    ArrayList list = node.getInstructions();
    HashMap subsMap = new HashMap();
    ArrayList instRemovalList = new ArrayList();
    ArrayList instAdditionList = new ArrayList();

    for(ListIterator iter = list.listIterator(); iter.hasNext(); ) {
      Instruction inst = (Instruction) iter.next(); 
      char op = inst.operator.opcode;

      // check for integer multiplication or division
      if (((op == Operators.MUL_opcode) || (op == Operators.DIV_opcode)) && inst.type().isInteger()){
	Operand operandA = Binary.getVal1(inst);
	Operand operandB = Binary.getVal2(inst);

	boolean constantA = operandA.isConstant();
	boolean constantB = operandB.isConstant();

	// check for a constant operand	
	if (constantA || constantB) {
	  
	  if (op == Operators.MUL_opcode) {

	    if (constantA && constantB)  {
	      // Both are constant ??? -- this will be handled somewhere else;
	      continue;

	    } else { 
	      // If A is constant or B is constant there is not much of
	      // a difference.  This only works with int anyway.
	      // Decide which is the constant and which is the operand.
	      Object o = null;
	      Operand operand = null;
	      int value = 0;
	      long long_value = 0;

	      if (constantA && operandA.isIntConstant()) {
		value = ((IntConstantOperand)operandA).getValue();
		operand = operandB;
	      } else if (constantB && operandB.isIntConstant()) {
		value = ((IntConstantOperand)operandB).getValue();
		operand = operandA;
	      } else continue;  

	      // special cases (multiplying by 1, 0 or -1)
	      if (value == 1) {
		instRemovalList.add(inst);
		// put val for result in substitutions
		subsMap.put(Binary.getResult(inst), operand);
		continue;
	      } else if (value == 0) {
		instRemovalList.add(inst);
		// put zero for result in substitutions
		subsMap.put(Binary.getResult(inst), Operand.newIntConstant(0));
		continue;
	      } else if (value == -1) {
		instRemovalList.add(inst);
		// add instruction for subtraction 
		instAdditionList.add(Binary.create(Operators.SUB, Type.Int, Binary.getResult(inst), Operand.newIntConstant(0), operand, inst.getPredicate()));
		continue;
	      }

	      LinkedList new_inst = 
		calculateConstMult(operand, value, Binary.getResult(inst),
		    inst.getPredicate());
	      // del old instruction.
	      // add new instructions.
	      instRemovalList.add(inst);
	      for(ListIterator new_itr = new_inst.listIterator(); 
		  new_itr.hasNext();) {
	      instAdditionList.add((Instruction)new_itr.next());
	      }
	    } 
	  } else {
	    // Divide.
	    if (constantA && constantB) {
	      // Easy. -- done somewhere else
	      continue;
	    } else if (constantA) {
	      // constantA only -- this is also not so simple it is Constant/Variable.
	      // In this case, what can we do?
	      continue;
	    } else {
	      // constantB only
	      if (operandB.isIntConstant()) {
		int value;
		value = ((IntConstantOperand)operandB).getValue();
		if (Bit.countOnes(value) == 1) {
		  value--;
		  //		  System.out.println(" OOOOhhh -- sign problems.  Unsigned or Signed.");
		  Operand constOperand = Operand.newIntConstant(Bit.countOnes(value));
	          instRemovalList.add(inst);
		  instAdditionList.add(Binary.create(Operators.SHR, Type.Int, Binary.getResult(inst), operandB,
			constOperand)); 
		}
	      }
	    }
	  } 
	}
      }
    }

    // remove instructions
    for(Iterator iter = instRemovalList.iterator(); iter.hasNext(); ) {
      Instruction inst = (Instruction) iter.next(); 
      node.removeInstruction(inst);
    }

    // add instructions
    node.addInstructions(instAdditionList);


    if (subsMap.size() > 0) {
      HashMap instMap = new HashMap();

      for(Iterator iter = list.iterator(); iter.hasNext(); ) {
	Instruction inst = (Instruction) iter.next(); 

	// Check uses for any substitutions.
	int numDefs = inst.getNumberOfDefs();
	int numUses = inst.getNumberOfUses();

	for(int i = numDefs; i < (numDefs + numUses); i++) {
	  Operand use = inst.getOperand(i);
	  Operand useSub = (Operand)(subsMap.get(use));

	  // Record the substitution we will make later
	  if (useSub != null) {
	    instMap.put(inst, use);
	  }
	}
      }

      Set instSet = instMap.keySet();

      // Remove all the instructions that need substitutions 
      for(Iterator iter = instSet.iterator(); iter.hasNext(); ) {
	Instruction inst = (Instruction) iter.next(); 
	node.removeInstruction(inst);
      }

      // make the substitution
      for(Iterator iter = instSet.iterator(); iter.hasNext(); ) {
	Instruction inst = (Instruction) iter.next(); 
	Operand use = (Operand)instMap.get(inst);
	Operand useSub = (Operand)(subsMap.get(use));
	inst.replaceOperand(use, useSub);
	node.addInstruction(inst);
      }
    }

    return true;
  }

  public class Tuple {
    public Operand result;
    public Operator operator;
    public int weight;

    public Tuple(Operand res, Operator op, int wt) {
      result = res;
      operator = op;
      weight = wt; 
    }
  }

  public class TupleComparator implements Comparator{

    public TupleComparator(){};

    public int compare (Object o1, Object o2) {
      // can't let it think they're equal or it won't add an object
      // with the same weight as one that is already in the tree
      if (((Tuple)o1).weight == ((Tuple)o2).weight) {
	return 1;
      } else
	return (((Tuple)o1).weight - ((Tuple)o2).weight);
    }

    public boolean equals (Object o1, Object o2) {
      return (((Tuple)o1).result == ((Tuple)o2).result);
    }
  }



  public LinkedList calculateConstMult(Operand Noperand, int  constant, Operand r, 
      BooleanEquation predicate) {

    // iterate on the constant
    // look at the first bit
    // find the next one
    //
    // if the next one is > 2 away
    // we need to do a shift then subtract
    // calculate the two shifts C1 and C2, 
    // add those instructions to a separate instruction list
    // add info for adding C1 and subtracting C2 to a sorted list
    //
    // if the bit width is 2 
    // calculate the two shifts C3 and C4,
    // add those instructions to the separate instruction list
    // add info for adding C3 and C4 to a sorted list
    // 
    // if the bit is one 
    // calculate the shift C5 
    // add that instruction to the separate instruction list
    // add info for adding C5 to a sorted list
    // 
    // loop back to look at bit.

    // Information about results to be added and subtracted is put in
    // a tuple and added to a SortedTree, with the weight being the
    // amount shifted (approximately)
    // Use the smallest weighted results, combine them by
    // creating either an add or subtract instruction and add
    // that to the instruction list, then add the
    // result to a new tree, and so on until only one node left.
    // When computing the result weight, use the max weight plus one.


    boolean neg_const = (constant < 0);
    if (neg_const) constant = -constant;

    int width = Bit.calculateConstantWidth(constant);

    LinkedList list = new LinkedList();
    SortedSet tupleSet = new TreeSet(new TupleComparator());

    int last_zero_count = 0;
    int index = width - 1;
    Operand sh_result = null;

    while(index >= 0) {

      int next_zero = findNextZero(index, constant);
      int next_one = findNextOne(next_zero, constant);

      int difference = index - next_zero;
      sh_result = null;

      if (difference > 2) {

	// C1 = N << index + 1
	sh_result = addShiftCurrent(list, Noperand, index+1, predicate);
	tupleSet.add(new Tuple(sh_result, Operators.ADD, index+1));

	// C2 = N << next_zero + 1
	if (next_zero+1 == 0) {
	  sh_result = Noperand;
	} else {
	  sh_result = addShiftCurrent(list, Noperand, next_zero+1, predicate);
	}
	tupleSet.add(new Tuple(sh_result, Operators.SUB, next_zero+1));

      } else if (difference == 2) {
	// C3 = N << index
	sh_result = addShiftCurrent(list, Noperand, index, predicate);
	tupleSet.add(new Tuple(sh_result, Operators.ADD, index));

	// C4 = N << index - 1 
	if (index-1 == 0) {
	  sh_result = Noperand;
	} else {
	  sh_result = addShiftCurrent(list, Noperand, index-1, predicate);
	}
	tupleSet.add(new Tuple(sh_result, Operators.ADD, index-1));

      } else if (difference == 1) {
	// C5 = N << index 
	if (index == 0) {
	  sh_result = Noperand;
	} else {
	  sh_result = addShiftCurrent(list, Noperand, index, predicate);
	}
	tupleSet.add(new Tuple(sh_result, Operators.ADD, index));
      }

      index = next_one;
    }

    // Now use the list of tuples to create the rest of the instructions.
    // Tuples set is sorted by lowest to highest weight
    // Take two lowest weight tuples and combine.
    // Put resulting tuple in new tree.
    // Iterate until resulting tree only has one node

    // make a list so we can remove stuff from it while iterating
    LinkedList tupleList = new LinkedList(tupleSet);

    while (tupleList.size() > 1) {
      ListIterator tli = tupleList.listIterator() ;
      Tuple tup = null;
      Tuple tup1 = null;
      Tuple tup2 = null;
      SortedSet newSet = new TreeSet(new TupleComparator());


      while (tli.hasNext()) {
	tup = (Tuple)tli.next();

	// if an add, assign to tup1, if need to
	// can't have first be a subtract
	if ((tup1 == null) && (tup.operator == Operators.ADD)) {
	  tup1 = tup;
	  tli.remove();
	} else if (tup2 == null)  {
	  tup2 = tup;
	  tli.remove();

	} else {
	  // we had to skip over a tuple, so need to restart iterator for next time
	  tli = tupleList.listIterator();
	}

	// If found two to combine
	// combine them and put new tuple in the newSet with a combined weight
	if ((tup1 != null) && (tup2 != null)) {
	  Operand binary_result = null;

	  if (tup2.operator == Operators.ADD) {
	    binary_result = addAddCurrent(list, tup1.result, tup2.result, predicate);
	  } else if (tup2.operator == Operators.SUB) {
	    binary_result = addSubCurrent(list, tup1.result, tup2.result, predicate);
	  }

	  newSet.add(new Tuple(binary_result, Operators.ADD, Math.max(tup1.weight, tup2.weight) + 1));
	  tup = null;
	  tup1 = null;
	  tup2 = null;
	}
      } 

      // this was a leftover, it had noone to pair with, so just put it in the new set
      if (tup != null) {
	newSet.add(tup);
      }

      // get ready to start iterating over the list again 
      tupleList = new LinkedList(newSet);
    } 

    // at the end if neg_const, invert the result.
    Instruction last_inst = (Instruction)list.getLast();
    Operand last_result = Binary.getResult(last_inst);

    if (neg_const) {
      addSubCurrent(list, Operand.newIntConstant(0), last_result, predicate);
      last_inst = (Instruction)list.getLast();
    }

    // make the result variable of the last instruction the correct name
    Binary.setResult(last_inst, r);

    return list;
  }


  Operand addShiftCurrent(LinkedList list,
			  Operand last_op,
			  int shift,
			  BooleanEquation predicate) {
    // C1 = Cp << difference + last_zero_count
    Operand result = Operand.nextBlock("shlTmp");
    Instruction current = Binary.create(Operators.SHL, Type.Int, result, last_op, 
	Operand.newIntConstant(shift), (BooleanEquation)predicate.clone());
    list.add(current);
    return result;
  }

  Operand addAddCurrent(LinkedList list,
			Operand op_a,
			Operand op_b,
			BooleanEquation predicate) {
    // C1 = Ca + Cb
    Operand result = Operand.nextBlock("addTmp");
    Instruction current = Binary.create(Operators.ADD, Type.Int, result, op_a, 
	op_b, (BooleanEquation)predicate.clone());
    list.add(current);
    return result;
  }

  Operand addSubCurrent(LinkedList list,
			Operand op_a,
			Operand op_b,
			BooleanEquation predicate) {
    // C1 = Ca - Cb
    Operand result = Operand.nextBlock("subTmp");
    Instruction current = Binary.create(Operators.SUB, Type.Int, result, op_a, 
	op_b, (BooleanEquation)predicate.clone());
    list.add(current);
    return result;
  }

  boolean isZero(int index, int constant) {
    return ((1 << (index)) & constant) == 0;
  }
  
  boolean isOne(int index, int constant) {
    return ((1 << (index)) & constant) != 0;
  }  

  int findNextZero(int start, int constant) {
    while (start >= 0) {
      if (isZero(start,constant)) {
	return start;
      } else {
	start--;
      }
    }
    return -1;
  }

  int findNextOne(int start, int constant) {
    while (start >= 0) {
      if (isOne(start,constant)) {
	return start;
      } else {
	start--;
      }
    }
    return -1;
  }

  public String name() {
    return "ConvertConstantMultToShiftTree";
  }

}

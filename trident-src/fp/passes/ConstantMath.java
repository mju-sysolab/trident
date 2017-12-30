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
import fp.util.BigSquareRoot;
import java.util.*;
import java.math.BigInteger;
 
public class ConstantMath extends Pass implements BlockPass {
 
  /*
    The purpose of this pass is to do constant math
  */

  public ConstantMath(PassManager pm) {
    super(pm);
  }
 
  public boolean optimize(BlockNode node) {
    // For example, in the following instruction:
    // %a5 = mult %const3 %const4 predicate
    // This instruction can be removed and then
    // substitute %const12 for %a5 hereafter.

    HashSet instsToAdd = new HashSet();
    HashSet instsToRem = new HashSet();

    boolean steadyState = false;

    while (!steadyState) {

      steadyState = true;

      // Store substitution pair in a hash map.
      HashMap subsMap = new HashMap();  

      ArrayList list = node.getInstructions();
      ArrayList instRemovalList = new ArrayList();

      for(ListIterator iter = list.listIterator(); iter.hasNext(); ) {
	Instruction inst = (Instruction) iter.next(); 
	Type type = inst.type();
	boolean instRemoved = false;

	if (type.isIntegral()) {
	  instRemoved = computeIntegralResult(inst, type, subsMap, instRemovalList);
	} else if (type.isFloat()) {
	  instRemoved = computeFloatResult(inst, type, subsMap, instRemovalList);
	} else if (type.isDouble()) {
	  instRemoved = computeDoubleResult(inst, type, subsMap, instRemovalList);
	}

	// if instruction was removed and substitution made, then
	// we need to make a note of it, because we will need to go
	// through this loop again
	if (instRemoved)
	  steadyState = false;

      }

      for(Iterator iter = instRemovalList.iterator(); iter.hasNext(); ) {
	Instruction inst = (Instruction) iter.next(); 
	node.removeInstruction(inst);
      }

      //System.out.println("\nSubsMap:\n"+subsMap);
      /*
	Really need to simplify the map here.  The problem is that the map
	can say: A -> B, B -> C, C -> D.  That does not work with approach
	used below.  The mappings are only examined once.  So, to support
	that we need to simplify the mappings so that we have:
	A -> D, B -> D, C-> D.  So that if you come across the operand, you
	will get the final result.

      */
      resolveMap(subsMap);
      //System.out.println("\n Resolved SubsMap:\n"+subsMap);



      if (subsMap.size() > 0) {
	HashSet subPairSet = new HashSet();

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
	      subPairSet.add(new SubPair(inst, use));
	    }
	  }
	}
	
	//System.out.println("\nSubPairSet: "+subPairSet);

	// Remove all the instructions that need substitutions 
	for(Iterator iter = subPairSet.iterator(); iter.hasNext(); ) {
	  SubPair sp = (SubPair) iter.next();
	  Instruction inst = (Instruction) sp.inst;
	  //node.removeInstruction(inst);
	  instsToRem.add(inst);
	}

	// make the substitution
	for(Iterator iter = subPairSet.iterator(); iter.hasNext(); ) {
	  SubPair sp = (SubPair) iter.next();
	  Operand use = sp.op;
	  Instruction inst = sp.inst;
	  Operand useSub = (Operand)(subsMap.get(use));
	  /*
	  // If the instruction is used to calculate an address, then fix it...
	  if(use.isAddr() && useSub.isBlock()) {
	    Operand addrUseSub = Operand.newAddr(useSub.getFullName());
	    inst.replaceOperand(use, addrUseSub);
	  } else
	    inst.replaceOperand(use, useSub);
	  */
	  //System.out.println("------------------");
	  //System.out.println("before: "+inst);

	  inst.replaceOperand(use, useSub);
	  instsToAdd.add(inst);

	  //System.out.println("after : "+inst);
	}
      }

    }

    // Add/remove the instructions...    
    // why do we need a new LinkedList is not ArrayList a List?
    List addList = new LinkedList(instsToAdd);
    List remList = new LinkedList(instsToRem);

    node.removeInstructions(remList);
    node.addInstructions(addList);
    


    return true;
  }

  private static HashSet getOpSet(HashSet subPairSet) {
    HashSet set = new HashSet();
    for(Iterator it = subPairSet.iterator(); it.hasNext(); ) {
      SubPair sp = (SubPair) it.next();
      set.add(sp.op);
    }
    return set;
  }

  class SubPair {
    SubPair(Instruction i, Operand o) {
      inst = i;
      op = o;
    }
    public Instruction inst;
    public Operand op;
    public String toString() {
      return inst + "=" + op;
    }
  }

  void resolveMap(HashMap map) {
    // map is defined as:  A becomes B (A -> B).
    Set keys = map.keySet();
    for(Iterator iter=keys.iterator();iter.hasNext();) {
      Operand op = (Operand)iter.next();
      if (map.containsValue(op)) {
	// get my target
	Operand target = (Operand)map.get(op);
	// fixing is expensive.
	for(Iterator map_iter=map.entrySet().iterator(); map_iter.hasNext();) {
	  Map.Entry entry = (Map.Entry)map_iter.next();
	  Operand key = (Operand)entry.getKey();
	  Operand value = (Operand)entry.getValue();
	  if (value == op) entry.setValue(target);
	}
      }
    }
    
  }



  // "integral" defined in the LLVM manual is Bool, Ubyte, Sbyte, Ushort, Short, Uint, Int, Ulong, Long
  private boolean computeIntegralResult(Instruction inst, Type type, HashMap subsMap,  
      ArrayList instRemovalList) {

    Operand computedResultOperand = null;
    Operand originalResultOperand = null; 
    BigInteger resultBigInteger = null;

    //Examine math instructions to look for constant math
    if (Binary.conforms(inst)) {
      Operand val1Operand = Binary.getVal1(inst);
      Operand val2Operand = Binary.getVal2(inst);

      if (((val1Operand.isConstant() && val2Operand.isConstant()) 
	  || (val1Operand.isBooleanConstant() && val2Operand.isBooleanConstant()))) {

	String val1String = getIntegralValString(type, val1Operand);
	String val2String = getIntegralValString(type, val2Operand);

	BigInteger val1BigInteger = new BigInteger(val1String, 2);
	BigInteger val2BigInteger = new BigInteger(val2String, 2);

	// Do the computation now instead of at runtime
	switch (inst.getOpcode()) {
	  case Operators.AAA_IADD_opcode:
	  case Operators.ADD_opcode:
	    resultBigInteger = val1BigInteger.add(val2BigInteger);
	    break;
	  case Operators.SUB_opcode:
	    resultBigInteger = val1BigInteger.subtract(val2BigInteger);
	    break;
     	  case Operators.AAA_IMUL_opcode:
	  case Operators.MUL_opcode:
	    resultBigInteger = val1BigInteger.multiply(val2BigInteger);
	    break;
	  case Operators.DIV_opcode:
	    resultBigInteger = val1BigInteger.divide(val2BigInteger);
	    break;
	  case Operators.SETEQ_opcode:
	    if (val1BigInteger.compareTo(val2BigInteger) == 0) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETNE_opcode:
	    if (val1BigInteger.compareTo(val2BigInteger) != 0) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETLT_opcode:
	    if (val1BigInteger.compareTo(val2BigInteger) == -1) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETGT_opcode:
	    if (val1BigInteger.compareTo(val2BigInteger) == 1) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETLE_opcode:
	    if (val1BigInteger.compareTo(val2BigInteger) <= 0) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETGE_opcode:
	    if (val1BigInteger.compareTo(val2BigInteger) >= 0) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.AND_opcode:
	    resultBigInteger = val1BigInteger.and(val2BigInteger);
	    break;
	  case Operators.OR_opcode:
	    resultBigInteger = val1BigInteger.or(val2BigInteger);
	    break;
	  case Operators.XOR_opcode:
	    resultBigInteger = val1BigInteger.xor(val2BigInteger);
	    break;
	  case Operators.SHL_opcode:
	  case Operators.AAA_CSHL_opcode:  // temporarily inserted; comments welcome
	    resultBigInteger = val1BigInteger.shiftLeft(val2BigInteger.intValue());
	    break;
	  case Operators.SHR_opcode:
	    resultBigInteger = val1BigInteger.shiftRight(val2BigInteger.intValue());
	    break;
	  default:
	    break;
	}

	originalResultOperand = Binary.getResult(inst);

      } // end all constant 
      else if(((val1Operand.isConstant() || val2Operand.isConstant())) &&
	      (!(val1Operand.isConstant() && val2Operand.isConstant()))) {
	// If only one of the operands is constant...
	// Take care of cases such as:
	// 1)   a = b + 0     =>  a = b
	// 2)   a = b * 0     =>  a = 0
	// 3)   a = b * 1     =>  a = b
	// 4)   a = b << 0    =>  a = b

	/*
	// We must optimize them.
	// Don't optimize instructions that calculate an address...
	if(Binary.getResult(inst).isAddr())
	  return false;
	*/
	
	// Get the constant and non-constant operands...
	Operand constOp = null, nonConstOp = null;
	if(val1Operand.isConstant()) {
	  constOp = val1Operand;
	  nonConstOp = val2Operand;
	} else {
	  constOp = val2Operand;
	  nonConstOp = val1Operand;	  
	}

	String valString = getIntegralValString(type, constOp);
	BigInteger valBigInteger = new BigInteger(valString, 2);

	switch (inst.getOpcode()) {
	  case Operators.AAA_IADD_opcode:
	    if(valBigInteger.equals(BigInteger.ZERO)) {        
	      // If adding a zero...
	      computedResultOperand = nonConstOp;
	      //System.out.println("\nrm add: "+inst);
	    }
	    break;
	  case Operators.AAA_IMUL_opcode:
	    if(valBigInteger.equals(BigInteger.ZERO)) {        
	      // If multiplying by zero... constant zero!
	      computedResultOperand = constOp;
	      //System.out.println("\nrm mul: "+inst);
	    } else if(valBigInteger.equals(BigInteger.ONE)) {  
	      // If multiplying by one...
	      computedResultOperand = nonConstOp;
	      //System.out.println("\nrm mul: "+inst);
	    }
	    break;
	  case Operators.AAA_CSHL_opcode:
	    if(valBigInteger.equals(BigInteger.ZERO)) {        // If shifting by 0.
	      computedResultOperand = nonConstOp;
	      //System.out.println("\nrm cshl: "+inst);
	    }
	    break;
  	  default:
	    break;
	}

	originalResultOperand = (computedResultOperand != null) ? Binary.getResult(inst) : null;

      } // end simple one constants
    } // end binary operations

    // ABS, INV, NOT, SQRT
    if(Unary.conforms(inst)) {
      Operand valOperand = Unary.getVal(inst);
      if (valOperand.isConstant() || valOperand.isBooleanConstant()) {

	String valString = getIntegralValString(type, valOperand);
	BigInteger valBigInteger = new BigInteger(valString, 2);

	// Do the computation now instead of at runtime
	switch (inst.getOpcode()) {
	  case Operators.ABS_opcode:
	    resultBigInteger = valBigInteger.abs();
	    //System.out.println("absolute value " + " BigInteger:  " + resultBigInteger.toString(2));
	    break;
	  case Operators.INV_opcode:
	    resultBigInteger = BigInteger.ONE.divide(valBigInteger);
	    break;
	  case Operators.NOT_opcode:
	    resultBigInteger = valBigInteger.not();
	    break;
	  case Operators.SQRT_opcode:
	    BigSquareRoot bsr = new BigSquareRoot();
	    resultBigInteger = bsr.get(valBigInteger).toBigInteger();
	    break;
	  default:
	    break;
	}

	originalResultOperand = Unary.getResult(inst);

      }
    }

    // We need to generate result operands
    if ((originalResultOperand != null) && (computedResultOperand == null)) {
      if ((type == Type.Sbyte) || (type == Type.Ubyte)) {
	computedResultOperand = new IntConstantOperand(resultBigInteger.byteValue());
      }
      if ((type == Type.Short) || (type == Type.Ushort)) {
	computedResultOperand = new IntConstantOperand(resultBigInteger.shortValue());
      }
      if ((type == Type.Int) || (type == Type.Uint)) {
	computedResultOperand = new IntConstantOperand(resultBigInteger.intValue());
      }
      if ((type == Type.Long) || (type == Type.Ulong)) {
	computedResultOperand = new LongConstantOperand(resultBigInteger.longValue());
      }
      if (type == Type.Bool) {
	boolean testBit = resultBigInteger.testBit(0);
	if (testBit == true) {
	  computedResultOperand = BooleanOperand.TRUE;
	} else {
	  computedResultOperand = BooleanOperand.FALSE;
	}
      }
    }

    if (computedResultOperand != null) {
      System.out.println("Found a math integer instruction with constant operand(s):\n "
			 + inst);
      subsMap.put(originalResultOperand, computedResultOperand);
      System.out.println(" op "+originalResultOperand+" -> "+computedResultOperand);
      instRemovalList.add(inst);
      return true;
    }
    return false;
  }

  private boolean computeFloatResult(Instruction inst, Type type, HashMap subsMap,  
      ArrayList instRemovalList) {

    Operand computedResultOperand = null;
    Operand originalResultOperand = null; 
    float result = 0;

    //Examine math instructions to look for constant math
    if (Binary.conforms(inst)) {
      Operand val1Operand = Binary.getVal1(inst);
      Operand val2Operand = Binary.getVal2(inst);

      if (val1Operand.isConstant() && val2Operand.isConstant()) {

        float val1 = ((FloatConstantOperand)val1Operand).getValue();
        float val2 = ((FloatConstantOperand)val2Operand).getValue();


	// Do the computation now instead of at runtime
	switch (inst.getOpcode()) {
	  case Operators.ADD_opcode:
	    result = val1 + val2;
	    break;
	  case Operators.SUB_opcode:
	    result = val1 - val2;
	    break;
	  case Operators.MUL_opcode:
	    result = val1 * val2;
	    break;
	  case Operators.DIV_opcode:
	    result = val1 / val2;
	    break;
	  case Operators.SETEQ_opcode:
	    if (val1 == val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETNE_opcode:
	    if (val1 != val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETLT_opcode:
	    if (val1 < val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETGT_opcode:
	    if (val1 > val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETLE_opcode:
	    if (val1 <= val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETGE_opcode:
	    if (val1 >= val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  default:
	    break;
	}

	originalResultOperand = Binary.getResult(inst);
      }
    }

    // ABS, INV, NOT, SQRT
    if(Unary.conforms(inst)) {
      Operand valOperand = Unary.getVal(inst);

      if (valOperand.isConstant()) {

        float val = ((FloatConstantOperand)valOperand).getValue();

	// Do the computation now instead of at runtime
	switch (inst.getOpcode()) {
	  case Operators.ABS_opcode:
	    result = Math.abs(val);
	    break;
	  case Operators.INV_opcode:
	    result = 1 /val;
	    break;
	  case Operators.SQRT_opcode:
	    //  I don't know if this is correct, will casting make a difference in the result
	    result = (float)(Math.sqrt((double)val));
	    break;
	  default:
	    break;
	}

	originalResultOperand = Unary.getResult(inst);

      }
    }


    // We need to generate result operands
    if ((originalResultOperand != null) && (computedResultOperand == null)) {
	computedResultOperand = new FloatConstantOperand(result);
    }

    if (computedResultOperand != null) {
      System.out.println("Found a math floatinstruction with constant operands:  " + inst.toString());
      subsMap.put(originalResultOperand, computedResultOperand);
      instRemovalList.add(inst);
      return true;
    }

    return false;
  }

  private boolean computeDoubleResult(Instruction inst, Type type, HashMap subsMap,  
      ArrayList instRemovalList) {

    Operand computedResultOperand = null;
    Operand originalResultOperand = null; 
    double result = 0;

    //Examine math instructions to look for constant math
    if (Binary.conforms(inst)) {
      Operand val1Operand = Binary.getVal1(inst);
      Operand val2Operand = Binary.getVal2(inst);

      if (val1Operand.isConstant() && val2Operand.isConstant()) {

        double val1 = ((DoubleConstantOperand)val1Operand).getValue();
        double val2 = ((DoubleConstantOperand)val2Operand).getValue();


	// Do the computation now instead of at runtime
	switch (inst.getOpcode()) {
	  case Operators.ADD_opcode:
	    result = val1 + val2;
	    break;
	  case Operators.SUB_opcode:
	    result = val1 - val2;
	    break;
	  case Operators.MUL_opcode:
	    result = val1 * val2;
	    break;
	  case Operators.DIV_opcode:
	    result = val1 / val2;
	    break;
	  case Operators.SETEQ_opcode:
	    if (val1 == val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETNE_opcode:
	    if (val1 != val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETLT_opcode:
	    if (val1 < val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETGT_opcode:
	    if (val1 > val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETLE_opcode:
	    if (val1 <= val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  case Operators.SETGE_opcode:
	    if (val1 >= val2) {
	      computedResultOperand = BooleanOperand.TRUE;
	    } else {
	      computedResultOperand = BooleanOperand.FALSE;
	    }
	    break;
	  default:
	    break;
	}

	originalResultOperand = Binary.getResult(inst);
      }
    }

    // ABS, INV, NOT, SQRT
    if(Unary.conforms(inst)) {
      Operand valOperand = Unary.getVal(inst);

      if (valOperand.isConstant()) {

        double val = ((DoubleConstantOperand)valOperand).getValue();

	// Do the computation now instead of at runtime
	switch (inst.getOpcode()) {
	  case Operators.ABS_opcode:
	    result = Math.abs(val);
	    break;
	  case Operators.INV_opcode:
	    result = 1 /val;
	    break;
	  case Operators.SQRT_opcode:
	    result = (Math.sqrt(val));
	    break;
	  default:
	    break;
	}

	originalResultOperand = Unary.getResult(inst);

      }
    }


    // We need to generate result operands
    if ((originalResultOperand != null) && (computedResultOperand == null)) {
	computedResultOperand = new DoubleConstantOperand(result);
    }

    if (computedResultOperand != null) {
      System.out.println("Found a math double instruction with constant operands:  " + inst.toString());
      subsMap.put(originalResultOperand, computedResultOperand);
      instRemovalList.add(inst);
      return true;
    }

    return false;
  }

  // for Integral types only
  private String getIntegralValString(Type type, Operand valOperand) {
    String valString = null;

    if ((type == Type.Long) || (type == Type.Ulong)) {
      long val = ((LongConstantOperand)valOperand).getValue();

      // make sure the string is correctly signed
      if (type.isSigned() && (val < 0)) {
	valString = "-" + Long.toBinaryString(-val);
      } else {
	valString = Long.toBinaryString(val); 
      }
    } else if (type.isInteger()) {
      int val = ((IntConstantOperand)valOperand).getValue();

      // make sure the string is correctly signed
      if (type.isSigned() && (val < 0)) {
	valString = "-" + Integer.toBinaryString(-val);
      } else {
	valString = Integer.toBinaryString(val); 
      }
    } else if (type == Type.Bool) { 

      if (valOperand == BooleanOperand.TRUE)  {
	valString = "1";
      } else {
	valString = "0";
      }
    }
    return valString;
  }

     
  public String name() {
    return "ConstantMath";
  }
 
 
}


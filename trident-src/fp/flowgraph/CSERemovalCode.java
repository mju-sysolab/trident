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


package fp.flowgraph;

import java.util.*;

import fp.util.BooleanEquation;
import fp.util.UseHash;
import fp.util.Bool;




public class CSERemovalCode  {
  
  //sorry, I needed to use this, so I stole it away and gave it its very own
  //class
  
  /*
    This is fairly limited CSE analysis -- it only looks for a pair of 
    binary operations that match.  It does not build anything more fancy and
    look for larger matches.

    For example:
    
    c = a + b;        -->   tmp = a + b;
    d = a + b;                c = tmp;
                              d = tmp;
    However,
    
    t1 = a + b;
    c = t1 + f;
    t2 = a + f;
    d = t2 + b;
    
    does not change at all.
			      
  */


  public CSERemovalCode() {
  }


  public boolean removeEmCSEs(BlockNode node) {
    
    ArrayList list = node.getInstructions();
    HashMap expression_hash = populateExpressionHash(list);

    // process the expression hash 
    Collection expressions = expression_hash.values();

    for(Iterator iter = expressions.iterator(); iter.hasNext(); ) {
      LinkedList expression_list = (LinkedList)iter.next();
      
      if (expression_list.size() > 1) {
      //System.out.println("expression_list " + expression_list);
        Instruction first_instruction = (Instruction)expression_list.getFirst();
	Operand first_result = null;
	if (Binary.conforms(first_instruction)) {
	  first_result = Binary.getResult(first_instruction);
	} if (Test.conforms(first_instruction)) {
	  first_result = Test.getResult(first_instruction);
	}
	
        BooleanEquation predicate = new BooleanEquation(false);
        
        Operand tmp = first_result.getNext();

	// debug.
        //System.out.println("first_result " + first_result + " New tmp var "+tmp);

        Instruction new_statement = first_instruction.copy();
	// this preps the predicate so we can OR values in.
	new_statement.setPredicate(predicate);
	
	if (Binary.conforms(new_statement)) {
	  Binary.setResult(new_statement, tmp);
	} else if (Test.conforms(new_statement)) {
	  // Danger -- Will?
	  Test.setResult(new_statement, (BooleanOperand)tmp);
	}
	    
	for (ListIterator cse_iter = expression_list.listIterator(); cse_iter.hasNext(); ) {
          Instruction cse_statement = (Instruction)cse_iter.next();
          //System.out.println("     inst "+cse_statement);

          predicate.or(cse_statement.getPredicate());
	  
	  updateCSEInstruction(cse_statement, tmp, node);
	  
          //System.out.println(" mod inst "+cse_statement);
        }

        //System.out.println(" New statement "+new_statement);

        node.addInstruction(new_statement);
      }
    }
    
    return true;
  }
  


  HashMap populateExpressionHash(ArrayList list) {
    HashMap expression_hash = new HashMap();

    String hash_input = null;

    for(ListIterator iter = list.listIterator(); iter.hasNext(); ) {
      Instruction current = (Instruction)iter.next();

      // ignore the Nops.
      // if (current.isNOP()) continue;

      Operator operator = current.operator;
      
      switch(operator.format) {
      case InstructionFormat.Binary_format:
      case InstructionFormat.Test_format:
	// shift is currently unused.
      case InstructionFormat.Shift_format:
	hash_input = handleBinary(current, operator);
	break;
	
      case InstructionFormat.Select_format:
	hash_input = handleSelect(current, operator);
	break;
	
	/*
      case InstructionFormat.Load_format:
      case InstructionFormat.Store_format:
      case InstructionFormat.Malloc_format:
	break;
	
      case InstructionFormat.Return_format:
      case InstructionFormat.Goto_format:
      case InstructionFormat.Branch_format:
      case InstructionFormat.Switch_format:
	break;
	
      case InstructionFormat.Phi_format:
      case InstructionFormat.Cast_format:
	break;
	*/
      default:
	continue;
      }

      if (hash_input == null) continue;

      Object o = expression_hash.get(hash_input);
      if (o == null) {
	o = new LinkedList();
      }
      ((LinkedList)o).add(current);
      expression_hash.put(hash_input,o);
    }

    return expression_hash;
  }
    
  String handleBinary(Instruction current, Operator operator) {
    Operand opA = null; Operand opB = null;
    switch(operator.format) {
    case InstructionFormat.Binary_format:
      opA = Binary.getVal1(current);
      opB = Binary.getVal2(current);
      break;
    case InstructionFormat.Test_format:
      opA = Test.getVal1(current);
      opB = Test.getVal2(current);
      break;
    }
    
    // If both are constant, we will fix this some where else.
    if ((opA != null && opA.isConstant() )
	&& (opB != null && opB.isConstant())) {
      return null;
    }

    String hash_input = null;
    String inst_string = operator+" ";
    String opA_string = opA.toString();

    // for CSE we must be doing a non ASSN thing.
    if (opB != null) {
      String opB_string = opB.toString();
      // swap it if necessary.
      if (current.isCommutative() && 
	  opA_string.compareTo(opB_string) > 0) {
	hash_input = inst_string.concat(opB_string).concat(" ").concat(opA_string);
      } else {
	hash_input = inst_string.concat(opA_string).concat(" ").concat(opB_string);
      }
    } else {
      hash_input = inst_string.concat(opA_string);
    }
    
    return hash_input;
  }

  // these cannot yet be built -- so, there is not a heavy reason to support them.
  String handleSelect(Instruction current, Operator operator) {
    /*
    Operand opA = null; Operand opB = null;
    switch(operator.format) {
    case InstructionFormat.Binary_format:
      opA = Binary.getVal1(current);
      opB = Binary.getVal2(current);
      break;
    case InstructionFormat.Test_format:
      opA = Test.getVal1(current);
      opB = Test.getVal2(current);
      break;
    }
    
    // If both are constant, we will fix this some where else.
    if ((opA != null && opA.isConstant() )
	&& (opB != null && opB.isConstant())) {
      return null;
    }

    String hash_input = null;
    String inst_string = operator+" ";
    String opA_string = opA.toString();

    // for CSE we must be doing a non ASSN thing.
    if (opB != null) {
      String opB_string = opB.toString();
      // swap it if necessary.
      if (instruction.isCommutativeInstruction() && 
	  opA_string.compareTo(opB_string) > 0) {
	hash_input = inst_string.concat(opB_string).concat(" ").concat(opA_string);
      } else {
	hash_input = inst_string.concat(opA_string).concat(" ").concat(opB_string);
      }
    } else {
      hash_input = inst_string.concat(opA_string);
    }
    */
    //return hash_input;
    return null;
  }


  Operand findRealOperand(HashMap hash, Operand op) {
    Operand result = null;
    boolean done = false;
    Object o;

    while(true) {
      o = hash.get(op);
      if (o instanceof Operand) {
        if (((Operand)o).isPrimal()) {
          return (Operand) o;
        } else {
          op = (Operand)o;
        }
      } else {
        return op;
      }    
    }
  }


  /*
    Due to the load/store construction of LLVM, we will need to avoid
    creating aliases.  There is not a way (currently) to represent
    aliases, so we will avoid creating them.  This issue is, when
    replacing the aliases sub-part,

  */

  void updateCSEInstruction(Instruction instruction, Operand new_op, 
			    BlockNode node) {
    Operand result = null;
    node.removeInstruction(instruction);
    if (Binary.conforms(instruction)) {
      result = Binary.getResult(instruction);
      Binary.setVal1(instruction, null);
      Binary.setVal2(instruction, null);
    } else if (Test.conforms(instruction)) {
      result = Test.getResult(instruction);
      Test.setVal1(instruction, null);
      Test.setVal2(instruction, null);
    }
    
    if (result.isPrimal()) {
      // is this okay for type ?
      instruction.operator = Operators.STORE;
      Store.setValue(instruction, (PrimalOperand)new_op);
      node.addInstruction(instruction);
    } else {
      // if not primal, we must replace uses.
      UseHash use_hash = node.getUseHash();
      ArrayList orig_list = (ArrayList)use_hash.get(result);
      // the must be uses for us to replace them.
      if (orig_list != null) {
	ArrayList list = (ArrayList)orig_list.clone();
	for(ListIterator iter = list.listIterator(); iter.hasNext(); ) {
	  Instruction use_inst = (Instruction)iter.next();
	  node.removeInstruction(use_inst);
	  use_inst.replaceOperand(result, new_op);
	  node.addInstruction(use_inst);
	}
      }
      //do same for out edge predicates!
      if(new_op.isBoolean()) {
	for (Iterator edge_iter = node.getOutEdges().iterator(); 
	     edge_iter.hasNext(); ) {
	  BlockEdge edge = (BlockEdge)edge_iter.next();
	  BooleanEquation eq = edge.getPredicate();
	  if((eq == null) || (eq.isTrue()) || (eq.isFalse())) continue;
	  BooleanEquation eqNew = eq.replaceBool((Bool)result, (Bool)new_op);
	  edge.setPredicate(eqNew);
	}
      }
    }
  }
      


}


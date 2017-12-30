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

import fp.circuit.Operation;
import fp.flowgraph.Cast;
import fp.flowgraph.IllegalOpcode;
import fp.flowgraph.Instruction;
import fp.flowgraph.Operators;
import fp.GlobalOptions;

public class QXOperationSelect extends OperationSelect implements Operators {
  
  public Operation operationSelect(Instruction instruction) throws IllegalOpcode {
    String op_name = instruction.operator.toString();
    char op_code = instruction.operator.opcode;
    Operation operation = null;

    // Need to assign specific op_codes to circuit operations
    switch (op_code) {
    case AAA_IADD_opcode:
      operation = Operation.ADD;
      break;
    case AAA_ISUB_opcode:
      operation = Operation.SUB;
      break;
    case AAA_IMUL_opcode:
      operation = Operation.MULT;
      break;
    case AAA_IINV_opcode:
      operation = Operation.NEG;
      break;
    case AAA_AND_opcode:
      operation = Operation.AND;
      break;
    case AAA_OR_opcode:
      operation = Operation.OR;
      break;
    case AAA_XOR_opcode:
      operation = Operation.XOR;
      break;
    case AAA_NOT_opcode:
      operation = Operation.NOT;
      break;
    case AAA_SHL_opcode:
      operation = Operation.SHL;
      break;
    case AAA_SHR_opcode:
      operation = Operation.SHR;
      break;
    case AAA_LOAD_opcode:
      System.err.println("No Load allowed here.");
      operation = null;
      break;
    case AAA_STORE_opcode:
      System.err.println("No Store allowed here.");
      operation = null;
      break;
    case AAA_SELECT_opcode:
      operation = Operation.MUX;
      break;
    case CAST_opcode:
      // this will work in limited circumstances...
      operation = null;
      if (Cast.conforms(instruction)) {
	//System.out.println("Inst "+instruction);
	int input_width = instruction.type().getWidth();
	int output_width = Cast.getType(instruction).getType().getWidth();
	if (input_width == output_width) 
	  operation = Operation.BUF;
      }
      /*if (operation == null) {
	throw new IllegalOpcode("This cast is unsupported " + op_name);
      }*/
      break;

    default:
      operation = Operation.get(op_name);
      // check for failure here.  Eventually make Operation.get throw an exception?
      if (operation == null) {
	throw new IllegalOpcode("There is no circuit operation for opcode name " + op_name);
      }
    }
    return operation;

  }

}


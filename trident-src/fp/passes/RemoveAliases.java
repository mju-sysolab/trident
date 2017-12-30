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

/**
  * Optimizer that iterates through the graph and removes primal/primal and 
  * non-primal/non-primal loads and stores.
  * @author Neil Steiner
  */
public class RemoveAliases extends Pass implements BlockPass {

	/**
	  * Constructor.
	  * @param pm the PassManager object.
	  */
	public RemoveAliases(PassManager pm) {
		super(pm);
	}

	/** Returns the name of this optimizer. */
	public String name() { return "RemoveAliases"; }


	/**
	  * Function to search for and remove aliases.
	  * @param block the BlockNode object to optimize
	  */
	public boolean optimize(BlockNode block) {

		// remember whether we've changed anything
		boolean block_changed = false;

		// display the initial state
		//System.out.println();
		//System.out.println(block);
		//System.out.println();

		// declare a hash for substitutions
		HashMap substitutions = new HashMap();

		// iterate through all the instructions in the block
		for(Iterator instruction_iterator = block.getInstructions().iterator(); 
			instruction_iterator.hasNext();) {

			// look up the next instruction
			Instruction instruction = (Instruction) instruction_iterator.next();

			// ignore instructions that are neither loads nor stores
			boolean is_load = instruction.isLoad();
			boolean is_store = instruction.isStore();
			if(!is_load && !is_store) continue;

      // identify the operands according to the instruction type
			Operand source_operand = null;
			Operand target_operand = null;
      if(Load.conforms(instruction)) {
        // load instruction
        source_operand = Load.getSource(instruction);
        target_operand = Load.getResult(instruction);
      } else if(Store.conforms(instruction)) {
        // store instruction
        source_operand = Store.getValue(instruction);
        target_operand = Store.getDestination(instruction);
      }

      //else if(ALoad.conforms(instruction)) {
      //  // aload instruction
      //  source_operand = ALoad.getPrimalSource(instruction);
      //  target_operand = ALoad.getResult(instruction);
      //} else if(AStore.conforms(instruction)) {
      //  // astore instruction
      //  source_operand = AStore.getValue(instruction);
      //  target_operand = AStore.getPrimalDestination(instruction);
      //}

			// if either of the operands is null, we'll ignore this instruction and let someone
			// else figure out what to do about it
			if(source_operand == null || target_operand == null) continue;

			// determine which operands are primal
			boolean source_operand_is_primal = source_operand.isPrimal();
			boolean target_operand_is_primal = target_operand.isPrimal();

			// if exactly one of the two operands is primal, we're good
			boolean primality_is_good = source_operand_is_primal ^ target_operand_is_primal;
			if(primality_is_good) continue;

			//System.err.println("problem child " +  instruction);
			//System.err.println("source_operand " +  source_operand);
			//System.err.println("target_operand " +  target_operand);
			
			// otherwise we're dealing with an alias; we begin by removing it from the hashes
			block.removeFromHashes(instruction);
			// and then we remove it from the block
			instruction_iterator.remove();

			// keep track of target and source operands for pending substitution
			substitutions.put(target_operand,source_operand);

			// inform the user of the primal/primal and non-primal/non-primal loads and stores
			// that we detected
			//System.out.println("\tWARNING: Found primal/primal or non-primal/non-primal load or store:");
			//System.out.println("\t\tInstruction: " + instruction);

		}

		// if we did not schedule substitutions, we simply skip to the next block
		if(substitutions.isEmpty()) return false;

		// iterate through all the instructions in the block
		for(Iterator instruction_iterator = block.getInstructions().iterator(); 
			instruction_iterator.hasNext();) {

			// look up the next instruction, and keep track of whether it changes
			Instruction instruction = (Instruction) instruction_iterator.next();
			boolean instruction_changed = false;

			// iterate through the operands in the instruction
			int num_operands = instruction.getNumberOfOperands();
			for(int operand_index = 0;operand_index < num_operands;operand_index++) {

				// look up the operand
				Operand operand = instruction.getOperand(operand_index);

				// perform the substitution if applicable
				if(substitutions.containsKey(operand)) {
					// remove the instruction from the hashes
					block.removeFromHashes(instruction);
					// replace the operand
					Operand substitution = (Operand) substitutions.get(operand);
					instruction.putOperand(operand_index,substitution);
					// and add the new instruction to the hashes
					block.updateHashes(instruction);
					// remember that we'll need to update the DefUse hash
					instruction_changed = true;
					// and remember to inform the PassManager that this pass made a change
					block_changed = true;
				}

			}

			// inform the user that we performed the replacement
			//if(instruction_changed) {
			//	System.out.println();
			//	System.out.println("\tWARNING: Replaced primal/primal or non-primal/non-primal load or store:");
			//	System.out.println("\t\tInstruction: " + instruction);
			//	System.out.println();
			//}

		}

		// let the PassManager know whether we've changed anything
		return block_changed;

	}

}

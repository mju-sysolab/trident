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
import fp.hardware.*;
import fp.hardware.AllocateArrays.ArrayInfo;
import fp.hwdesc.Memory;

import java.util.*;

public class CheckPrimalWrites extends Pass implements BlockPass {
  /**
   * This class checks all of the block to ensure that they
   * are not violating SSA rules as well as our style of 
   * single primal writes to arrays or registers.
   *
   * Arrays may get an exception if they have multiple ports.
   */

  public CheckPrimalWrites(PassManager pm) {
    super(pm);
  }

  public boolean optimize(BlockNode block) {

    //HashMap primal_map = new HashMap();
    //HashMap ssa_map = new HashMap();

    HashMap array_map = new HashMap();

    int cycle_count = block.getCycleCount();

    HashMap mem_read = new HashMap();
    HashMap mem_write = new HashMap();

    System.out.println(" Cycle count "+cycle_count);

    HashMap[] test_read = new HashMap[cycle_count];
    HashMap[] test_write = new HashMap[cycle_count];
    
    ChipDef chipInfo = fp.GlobalOptions.chipDef;
    
    ArrayList mList = chipInfo.getMemoryBlockList();

    

    int mi = 0;
    for(ListIterator it = mList.listIterator(); it.hasNext(); mi++) {
      Memory mb = (Memory)it.next();
      
      String memName = "mem" + mi;

      HashMap map = mb.getArrayInfos();
      
      mem_read.put(memName, new Integer(mb.getNumOfReadBus()));
      for(int i = 0; i < cycle_count; i++) {
	test_read[i] = new HashMap();
      }
      mem_write.put(memName, new Integer(mb.getNumOfWriteBus()));
      for(int i = 0; i < cycle_count; i++) {
	test_write[i] = new HashMap();
      }

      for(Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
	Map.Entry me = (Map.Entry)iter.next();
	String array_name = (String)me.getKey();

	// arrays -> mem 
	array_map.put(array_name, memName);
      }
    }
	
    for(Iterator instruction_iterator = block.getInstructions().iterator(); 
	instruction_iterator.hasNext();) {
      
      // look up the next instruction
      Instruction instruction = (Instruction) instruction_iterator.next();

      int cycle = instruction.getExecClkCnt();

      // ignore instructions that are neither loads nor stores
      boolean is_load = ALoad.conforms(instruction);
      boolean is_store = AStore.conforms(instruction);
      if(!is_load && !is_store) continue;
      
      // identify the operands according to the instruction type
      Operand source_operand = null;
      Operand target_operand = null;
      if(is_load) {
        // load instruction
        source_operand = ALoad.getPrimalSource(instruction);
        target_operand = ALoad.getResult(instruction);

	String name = source_operand.getName();
	String mem = (String)array_map.get(name);
	if (mem != null) {
	  Integer old = (Integer)test_read[cycle].get(mem);
	  if (old == null) old = new Integer(0);

	  //System.out.println(" mem "+mem+" cycle "+cycle+" old "+old);
	  Integer next = new Integer(old.intValue()+1);
	  Integer test = (Integer)mem_read.get(mem);
	  
	  if (next.intValue() > test.intValue()) {
	    throw new CheckPrimalWritesException("Multiple reads found!!!\n"+
						 "Memory "+mem+"\n"+
						 "Count "+next+"\n"+
						 "Cycle "+cycle+"\n"+
						 "Instruction "+instruction);
	  } else {
	    test_read[cycle].put(mem, next);
	  }

	} else {
	  throw new CheckPrimalWritesException("Unable to find array "+name);
	}
	  

      } else if(is_store);
        // store instruction
        source_operand = AStore.getValue(instruction);
        target_operand = AStore.getPrimalDestination(instruction);

	String name = target_operand.getName();
	String mem = (String)array_map.get(name);
	if (mem != null) {
	  Integer old = (Integer)test_write[cycle].get(mem);
	  if (old == null) old = new Integer(0);
	  Integer next = new Integer(old.intValue()+1);
	  Integer test = (Integer)mem_write.get(mem);
	  
	  if (next.intValue() > test.intValue()) {
	    throw new CheckPrimalWritesException("Multiple writes found!!!\n"+
						 "Memory "+mem+"\n"+
						 "Count "+next+"\n"+
						 "Cycle "+cycle+"\n"+
						 "Instruction "+instruction);
	  } else {
	    test_write[cycle].put(mem, next);
	  }

	} else {
	  throw new CheckPrimalWritesException("Unable to find array "+name);
	}
	  


    }
      
    


    return true;
  }

  public String name() { return "CheckPrimalWrites"; }


  private class CheckPrimalWritesException extends RuntimeException {
    public CheckPrimalWritesException() {
      super();
    }
    public CheckPrimalWritesException(String message) {
      super("\n"+message);
    }
  }

}

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
import fp.util.*;
import java.io.*;

import fp.hardware.*;
public class ASAPSchedule extends Schedule
{
  public ASAPSchedule(BlockNode node) {
    super(node);    
  }
  /*
  method: aSAP_AssignClkTck 

  this method peforms an ASAP scheduling of the list of instructions
  passed in.  It performs the scheduling by following the dataflow.
  It starts by searching for primals, which it knows must be inputs to
  the system.  Then, it saves the outputs from these primals, and
  searches for other instructions that attempt to use those primals.
  It continues this, repeatedly looping within a while loop,
  scheduling instructions as soon as possible.  (that is, if the only
  inputs are primaries, schedule this instruction on clock tick
  0. Otherwise, wait until, whatever temporary variable it has as
  input has been given a value.)  This method makes use of a couple of
  special flags.  All instructions are given a default execution time
  of -1 to signify that they have not yet been scheduled.
  Additionally, a execution time of -2, signifies that an instruction
  has only constants as inputs.  This was done so that, if a constant
  is loaded into a temporary variable, and then that temporary
  variable is only used in one other instruction as input, the loading
  of the constant and the second instruction could be scheduled in the
  same time step.  It needs, however, to be considered if this will
  really be done, and if temporary variables will actually be used, or
  if they will be deleted and the constant wired directly to the
  second instruction.  This method loops within a do-while loop
  attempting to make changes to the execution times for each
  instruction, and if it ever finds that it cannot do any more
  changes, the loop exits. Then a second loop goes through the list of
  instructions and uses the execution time (a float) to calculate an
  execution clock pulse.  This is done, with the assumption that the
  whole part of the execution time, corresponds to the number of clock
  pulses before execution and therefore, it simply concontenates the
  fraction part off of the execution time to determine the appropriate
  clock tick.
  */
 
  /** ASAP scheduling of the list of instructions in the hyperblock.  It has 
   *  the option of ignoring predicates when scheduling (which is its default 
   *  choice) except when writing to primal operands.
   * 
   * @param instrlist list of unscheduled instructions from hyperblock
   * @param ignorePredsNicht tells the scheduler to ignore predicates
   *	  except when the instruction is writing to a primal; true=do  not  
   *	  ignore them, false=ignore them
   * @param chipInfo contains chip and board information such as     
   *	  where    arrays have been stored to memory and the availability of
   *	  hardware.
   * @param dfg a dependence flow graph of the list of instructions  
   *	  on the block node
   * @return whether scheduling was successful
   */
  public boolean asapSchedule(ArrayList instrlist, 
			      boolean ignorePredsNicht, ChipDef chipInfo, 
			      DependenceFlowGraph dfg) {

    HashMap saveOutputs = new HashMap();
    HashMap saveConstsHM = new HashMap();
    HashMap deflistsHM = new HashMap();
    HashMap useLists = new HashMap();
    HashMap inst2NodeMap = new HashMap();
     
     
    int change;
    float maxRank;
    float maxExecTime = 0;
     
    float rankLimit = 1;
    
    rankLimit = initAPSchedulers(instrlist, dfg, inst2NodeMap, deflistsHM, 
                                 useLists, chipInfo, rankLimit, 
				 ignorePredsNicht);
    
    //System.out.println("Rank Limit "+rankLimit);

    //main part of ASAP scheduling:
    do {
      //flag to know if work is still being done:
      change = 0;
      //foreach instruction
      for (Iterator it = ((ArrayList)instrlist).iterator(); 
  	  it.hasNext(); ) {
  	Instruction instr = (Instruction)it.next();
  	//get the current execution time:
        
	maxRank = instr.getExecTime();

	//System.out.println("Try "+instr+" maxRank "+maxRank);

        
	//foreach of the instruction's uses
  	for (Iterator itin = ((ArrayList)useLists.get(instr)).iterator(); 
  	    itin.hasNext(); ) {
  	  Operand this_in = (Operand)itin.next();
  	   
  	  if(this_in == null)
  	    continue;
  	   
  	  //see if the connection is over the loop boundary
          Instruction tmpInstr = 
  		     (Instruction)(saveOutputs.get(this_in.toString()));
  	  boolean onlyBackPointingConn = 
	    isBackPointing(saveOutputs, instr, dfg, tmpInstr, inst2NodeMap);
          
	  //if there is an instruction that defines the input from this 
          //instruction, and if the connection is not over the loop
          //boundary, then set the execution time of this instruction equal to
          //the execution time of the other instruction plus its latency
          if((saveOutputs.containsKey(this_in.toString()))&&((dfg==null)||
  	       (!onlyBackPointingConn))) {
  	    float clktmp_int = tmpInstr.getExecTime();
  	    float runlength = getInstrRunLength(tmpInstr, chipInfo);
  	    if(maxRank < clktmp_int + runlength)
  	      maxRank = clktmp_int + runlength; 
  	  /*while(!chipInfo.analyzeHardwareUse(instr, (int)maxRank)) {
  	    maxRank++;
  	  }*/
  	  } 
          //if all the inputs to the predecessor instruction were constants
          //set its execution time to be equal to this one's
  	  else if (saveConstsHM.containsKey(this_in.toString())) {
  	    Instruction tmpInstr2 = 
        		  (Instruction)(saveConstsHM.get(this_in.toString()));
  	    float clktmp_int = tmpInstr2.getExecTime();
  	  /*while(!chipInfo.analyzeHardwareUse(instr, (int)maxRank)) {
  	    maxRank++;
  	  }*/
  	    if(maxRank > clktmp_int) {
  	      tmpInstr.setExecTime(maxRank);
	    }
  	    saveConstsHM.remove(this_in);
  	  } 
          //if all inputs are primals, types, constants, TRUE, or FALSE set 
          //execution time to 0
  	  else if((inputsAllPrimalsOrConsts(((ArrayList)useLists.get(instr))))
        	&&(maxRank < 0)) {
  	    maxRank=0;
  	    /*while(!chipInfo.analyzeHardwareUse(instr, (int)maxRank)) {
  	      maxRank++;
  	    }*/
  	  }
          //if all inputs are constants, set maxRank to -2 so that I can know
          //to save this instruction to handle later
  	  else if ((inputsAllConsts((ArrayList)useLists.get(instr)))&&
        	   (maxRank < 0)&&(areOutsNotPrimals((ArrayList)deflistsHM
        						      .get(instr)))) {
  	    maxRank = -2;
  	  }
          //if the outputs are primals and all the inputs are constants set 
          //the execution time to 0
  	  else if ((inputsAllConsts((ArrayList)useLists.get(instr)))&&
        	   (maxRank < 0)) {
  	    maxRank = 0;
  	    /*while(!chipInfo.analyzeHardwareUse(instr, (int)maxRank)) {
  	      maxRank++;
  	    }*/
  	  }
  	    /*while(!chipInfo.analyzeHardwareUse(_node, instr, (int)maxRank)) {
  	      maxRank++;
	      //if(maxRank >= rankLimit)
	        //rankLimit++;
  	    }*/
  	}
  	
  	
  	//if the execution time has been changed to a later time, then save
        if(maxRank>instr.getExecTime()) { //while opusecnt> num available, increase maxrank by 1 
  	  
          //instructions that run shorter than a single clock tick cannot 
          //begin and end in different clock ticks, and all pipelined 
          //instructions must start on the clock edge
          if(((getInstrRunLength(instr, chipInfo) < 1)&&
               ((int)maxRank) != ((int)(maxRank 
        	   + getInstrRunLength(instr, chipInfo)))))
            maxRank = ((int)(maxRank+ 0.99999)); // round up to next clock tick
          
          if((getInstrRunLength(instr, chipInfo) >= 1)&&
               (maxRank - ((int)maxRank) > 0.0001))
            maxRank = ((int)(maxRank+ 0.99999)); // round up to next clock tick
             
          
          //analyze hardware and bus count usage
          //while there is a problem, keep pushing the instruction forward in 
          //time
  	  while(!chipInfo.analyzeHardwareUse(_node, instr, (int)maxRank)) {
  	    maxRank++;
	      //if(maxRank >= rankLimit)
	        //rankLimit++;
  	  }
  	    
          chipInfo.saveNewHardwareUsage(_node, instr, (int)maxRank);
  	  
  	  instr.setExecTime(maxRank);
	  //System.out.println("Scheduled "+instr+" at "+maxRank);

  	  for (Iterator itout = ((ArrayList)deflistsHM.get(instr))
        				   .iterator(); itout.hasNext(); ) {
  	    Operand this_out = (Operand)itout.next();
  	    saveOutputs.put(this_out.toString(),instr);
  	  }
  	  change = 1;
  	  if(maxExecTime < maxRank)
  	    maxExecTime = maxRank;
  	  
  	}
  	else if (maxRank==-2) {
  	  for (Iterator itout = ((ArrayList)deflistsHM.get(instr)).iterator(); 
  	         itout.hasNext(); ) {
  	    Operand this_out = (Operand)itout.next();
  	    saveConstsHM.put(this_out.toString(),instr);
  	  }
  	} 
      }
      //do while there are still changes occuring, and while the times have 
      //not been pushed unreasonably far forward
    } while ((change == 1) && (rankLimit > maxExecTime));
     
    chipInfo.saveSharedOps();	   
    
    if(rankLimit < maxExecTime) {
      System.out.println("rankLimit "+rankLimit+" maxExecTime "+maxExecTime);
      throw new ScheduleException("Error with ASAP scheduling.  It was stuck i"
				  + "n a loop...");
    }
     
    
    for (Iterator it = ((ArrayList)instrlist).iterator(); 
      it.hasNext(); ) {
      Instruction instrtmp = (Instruction)it.next();
      float clktmp2 = instrtmp.getExecTime();
      if(clktmp2<0) {
  	throw new ScheduleException("Error with ASAP scheduling. Failed"
				    + " to schedule : " + instrtmp);
      }
      instrtmp.setExecClkCnt((int)clktmp2);
       
    }
    return true;
  }

  
  public boolean isBackPointing(HashMap saveOutputs, Instruction instr, 
                                DependenceFlowGraph dfg, Instruction tmpInstr,
				HashMap inst2NodeMap) {
    boolean onlyBackPointingConn = true;
    if (dfg != null) {
      DependenceFlowNode childNode = 
	(DependenceFlowNode)inst2NodeMap.get(instr);
      DependenceFlowNode parentNode = 
	(DependenceFlowNode)inst2NodeMap.get(tmpInstr);
      //this was copied and modified from findEdge in Graph.java:
      Set nodes = dfg.getAllNodes();
      if (nodes.contains(parentNode) && nodes.contains(childNode)) {
	for (Iterator it3 = parentNode.getOutEdges().iterator(); 
	     it3.hasNext();){
  	  DependenceFlowEdge edge = (DependenceFlowEdge) it3.next();
  	  if (edge.getSink() == childNode) {
  	    if(!edge.getisBackWardsPointing())
  	      onlyBackPointingConn = false;
  	  } // end of if ()
	} // end of for (Iterator  = .iterator(); .hasNext();)
      } // end of if ()
      
      
    } else {
      return false;
    }
    return onlyBackPointingConn;
  }

}

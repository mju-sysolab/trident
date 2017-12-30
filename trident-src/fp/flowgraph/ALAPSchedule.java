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
import fp.*;
import java.io.*;

import fp.hardware.*;
public class ALAPSchedule extends Schedule
{
  public ALAPSchedule(BlockNode node) {
    super(node);    
  }

  //method:  aLAP_AssignClkTck
  //this method performs ALAP scheduling of the list passed in to instrlist
  //this method searches through the list of instructions, saving the inputs to each instruction.  It then
  //finds instructions that uses these inputs and attempts to schedule that instruction, immidiately before
  //the instruction whose input its using.  Because this results in determining the scheduling going backwards, starting 
  //from the last instruction and working its way to the first, the result is a series of negative clock ticks (where the
  //last instruction is located at the largest negative value).  Therefore, after finishing this procedure, I reposition
  //the list so that the first instruction is executed at clock tick 0.  This is done, by saving the execution time and using this
  //to calculate an offset that will push all the values back up so that the lowest is at 0.  Also, the execution time is used to 
  //calculate the clock tick that each instruction will run, as done above for ASAP.
  /** ALAP scheduling of the list of instructions in the hyperblock.  It has 
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
  public boolean alapSchedule(ArrayList instrlist, 
  			      boolean ignorePredsNicht, ChipDef chipInfo, 
        		      DependenceFlowGraph dfg) {
    HashMap saveInputs = new HashMap();
    HashMap deflistsHM = new HashMap();
    HashMap useLists = new HashMap();
    HashMap inst2NodeMap = new HashMap();
    int change;
    float maxRank, minRank = -1;
    float rankLimit = 3;
     
    rankLimit = initAPSchedulers(instrlist, dfg, inst2NodeMap, deflistsHM, 
                                 useLists, chipInfo, rankLimit, 
				 ignorePredsNicht);
    for (Iterator it = ((ArrayList)instrlist).iterator(); 
  	  it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();
      for (Iterator itin = ((ArrayList)useLists.get(instr)).iterator();
  	    itin.hasNext(); ) {
  	Operand this_in = (Operand)itin.next();
  	if(this_in != null) {
  	  if(!(saveInputs.containsKey(this_in.toString())))
  	    saveInputs.put(this_in.toString(),new ArrayList());
  	  ArrayList tmpList = (ArrayList)saveInputs.get(this_in.toString());
  	  tmpList.add(instr);
  	}
      }
    }
    
    //begin main ALAP scheduling loop:
    do {
      change = 0;
      for (Iterator it = ((ArrayList)instrlist).iterator(); 
  	     it.hasNext(); ) {
  	Instruction instr = (Instruction)it.next();
        
  	//load current execution time:
  	maxRank = instr.getExecTime();
        
        //find out if recursive:
  	boolean connected = isLastNodeOfLoop(instr, inst2NodeMap, dfg);
  	
        //if the outputs are primals, and this is at the end of the loop (if
        //it is a loop, set the time to -2--remeber in ALAP, we're going 
        //backwards).
        if((!(areOutsNotPrimals((ArrayList)deflistsHM.get(instr))))&&
           (connected)/*&&(maxRank == -1)&&chipInfo.analyzeHardwareUse(_node, instr, -2)*/) {
  	  maxRank = -2;
	}
	else {
          //else for each output from the instruction
  	  boolean instScheduled = false;
	  for (Iterator itout = ((ArrayList)deflistsHM.get(instr)).iterator(); 
  		     itout.hasNext()&&!instScheduled; ) {
  	    Operand this_out = (Operand)itout.next();
  	    if((chipInfo.findMemoryBlock(this_out)!=null)/*&&
               (maxRank == -1)*/) {
  	      maxRank = -2;
	      instScheduled = true;
	    }
	    else {
              //check if it is an input to another instruction
	      boolean outFound = false;
	      ArrayList tmpInstrList = null;
	      for(Iterator ittmp = saveInputs.keySet().iterator(); 
  		       ittmp.hasNext();) {
		String key = (String)ittmp.next();

		if(key.equals(this_out.toString())) {
	          outFound = true;
		  tmpInstrList = (ArrayList)(saveInputs.get(key));
		}

	      }
  	      if (outFound) {

		//then for all instructions who use this output
  		for (Iterator it3 = ((ArrayList)tmpInstrList).iterator(); 
  		      it3.hasNext(); ) {

		  Instruction tmpInstr = (Instruction)it3.next();
  		  float clktmp_int = tmpInstr.getExecTime();
  		  float runlength = getInstrRunLength(instr, chipInfo);


		  //if all inputs are contants and all outputs not primals
        	  //set the execution time equal to that of the next node
        	  //(since they can be evaluated and wired directly to the 
        	  //input).
        	  //if((inputsAllConsts((ArrayList)useLists.get(instr)))&&
        	  //   (areOutsNotPrimals((ArrayList)deflistsHM.get(instr)))) {
        	  /*if(areOutsNotPrimals((ArrayList)deflistsHM.get(instr))) {
  		    maxRank = clktmp_int;
  		  System.out.println("inputs all consts and outs not prim");
		  //else set the execution time, to the execution time of the 
        	  //successor minus the successor's run latency.  
        	  }
		  else*/ 
		  if(maxRank > clktmp_int - runlength) {
  		    maxRank = clktmp_int - runlength; 
		  }
  		}
  	      }

  	    }
	  }
  	}
  	
  	  //check hardware requirements:
  	  //System.out.println("**************************");
	  /*while(!chipInfo.analyzeHardwareUse(_node, instr, (int)maxRank)) {
  	    maxRank--;
  	  //System.out.println("inst " + instr + " failed hardware check");
  	  //System.out.println("maxRank " + maxRank);
  	  }*/
  	  //System.out.println("**************************");

  	//if the execution time has changed:
        if(maxRank<instr.getExecTime()/*&&chipInfo.analyzeHardwareUse(_node, instr, (int)maxRank)*/) {
  	  
          
  	  //check hardware requirements:
  	  while(!chipInfo.analyzeHardwareUse(_node, instr, (int)maxRank)) {
  	    maxRank--;
  	  }
  	  
	   
          //chipInfo.saveNewHardwareUsage(instr, (int)maxRank);
  	   
          CorrectStartTimes adjustTimes 
	    = new CorrectStartTimes(instr, chipInfo, !GlobalOptions.packInstructions);
          float corrctedStart = adjustTimes.getCorrectedStartTime(maxRank, -1);
  	  /*while(!chipInfo.analyzeHardwareUse(_node, instr, (int)corrctedStart)) {
  	    corrctedStart--;
  	  }*/
          chipInfo.saveNewHardwareUsage(_node, instr, (int)corrctedStart);
  	  
	  
	  
	  //save the execution time, and set the change flag
          //and save the minimum execution time for use later in 
          //resetting the times to go up from 0 instead of down from -2
          instr.setExecTime(corrctedStart);
 	  minRank = Math.min(minRank, corrctedStart);
  	  change = 1;
  	}
      }
    }while((change==1)&&(minRank> - rankLimit));
    
    
    //handle sharing of operators:
    chipInfo.saveSharedOps();	   
     
    if(minRank <= - rankLimit) {
      throw new ScheduleException("Error with ALAP scheduling. It was stuck i"
        				  + "n a loop...");
    }
    for (Iterator it = ((ArrayList)instrlist).iterator(); 
      it.hasNext(); ) {
      Instruction instrtmp = (Instruction)it.next();
      float clktmp2 = instrtmp.getExecTime();
      int minRankTmp = (int)Math.floor(minRank);
      //reset the times to go from 0 up instead of -2 down:
      clktmp2 -= minRankTmp;
      if(clktmp2<0) {
  	throw new ScheduleException("Error with ALAP scheduling. Failed"
        				  + " to schedule : " + instrtmp);
      }
      instrtmp.setExecTime(clktmp2);
      instrtmp.setExecClkCnt((int)(clktmp2));
       
    }
    
    //operations that run less than a clock tick cannot be added together such
    //that one of them starts before the end of a clock edge and stops within 
    //the next clock, and pipelined instructions must start on clock edges.
    //This code makes sure that that happens:
    /*for (Iterator it = ((ArrayList)instrlist).iterator(); 
      it.hasNext(); ) {
      Instruction instrtmp = (Instruction)it.next();
      float clktmp2 = instrtmp.getExecTime();
      if((((getInstrRunLength(instrtmp, chipInfo) < 1)&&
          ((int)clktmp2) != ((int)(clktmp2 
        		     + getInstrRunLength(instrtmp, chipInfo)))))||
          ((getInstrRunLength(instrtmp, chipInfo) >= 1)&&
           (clktmp2 - ((int)clktmp2) > 0.0001))) {
         clktmp2 = ((int)(clktmp2+ 0.99999)); // round up to next clock tick
         moveSuccessors(instrlist, instrtmp, useLists, deflistsHM,
        		clktmp2 + getInstrRunLength(instrtmp, chipInfo), 
        		chipInfo);
      }
      instrtmp.setExecTime(clktmp2);
      instrtmp.setExecClkCnt((int)(clktmp2));
       
    }*/
    return true;
  }

  /** This method goes down recursively through the hierarchy of instructions,
   *  pushing them all further down as a result of some dependency higher up
   *  having been moved.  
   * 
   * @param instrlist list of instructions from hyperblock, whcih the 
   *	 force-directed scheduler is working on
   * @param parentInstr the starting instruction, from which this    
   *	  recursive algorithm should attempt to go down from through the    
   *	  dependence hierarchy
   * @param useLists a hashmap saving a list of all operands being   
   *	  used for a given instruction (key=instruction; value=list of used 
   *	  operands)
   * @param defLists a hashmap saving a list of all operands being   
   *	  defined by a given instruction (key=instruction; value=list of    
   *	  defined operands)
   * @param windowMin a hashmap saving the lower limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  lower limit
   * @param execTime a new minimum execution time for this	     
   *	  instruction.  It is determined after scheduling a predecessor
   * @param chipInfo contains chip and board information such as     
   *	  where    arrays have been stored to memory and the availability of
   *	  hardware.
   */
  public void moveSuccessors(ArrayList instrlist, Instruction parentInstr, 
  			     HashMap useLists, HashMap defLists, 
        		     float execTime, ChipDef chipInfo) {
    //foreach sucessor
    for (Iterator it = ((ArrayList)instrlist).iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();   
      //if(instr predecessor of above instr && new min > this instruction's old min)
      if((isInstrPred((ArrayList) useLists.get(instr), (ArrayList) defLists
        						   .get(parentInstr)))
              &&(areOutsNotPrimals((ArrayList) useLists.get(instr)))) {
  	//if(n_int + instruction execution time > old min)
  	if(execTime > instr.getExecTime()) {
          float newExecTime = execTime;
          if((((getInstrRunLength(instr, chipInfo) < 1)&&
            ((int)newExecTime) != ((int)(newExecTime 
        			  + getInstrRunLength(instr, chipInfo)))))||
            ((getInstrRunLength(instr, chipInfo) > 1)&&
             (newExecTime - ((int)newExecTime) > 0.0001))) 
             newExecTime = ((int)(newExecTime+ 0.99999)); // round up to next clock tick
          
          instr.setExecTime(newExecTime);
  	  instr.setExecClkCnt((int)(newExecTime));
          
          
  	  moveSuccessors(instrlist, instr, useLists, defLists, 
        		 newExecTime + getInstrRunLength(instr, chipInfo), chipInfo);
  	}
  	 
      }
    } //end foreach
  } 
  
  public boolean isLastNodeOfLoop(Instruction instr, HashMap inst2NodeMap, 
                                  DependenceFlowGraph dfg) {
  
    boolean connected = false;
    if(dfg!=null) {
      DependenceFlowNode node = (DependenceFlowNode)inst2NodeMap.get(instr);
      for (Iterator itEdge = ((Set)node.getOutEdges()).iterator(); 
    	    itEdge.hasNext() && (connected==false); ) {
    	DependenceFlowEdge this_edge = (DependenceFlowEdge)itEdge.next();
    	if((this_edge instanceof DependenceDataFlowEdge) && 
    	    (this_edge.getisBackWardsPointing()))
    	  connected=true;
      }
    }
    else
      return true;
    return connected;
  } 

}

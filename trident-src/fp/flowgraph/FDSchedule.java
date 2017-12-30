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
import java.util.Random;
import fp.hardware.*;

public class FDSchedule extends Schedule
{
  
  private float _schedCycleFraction = 40;
  private float _schedCycleFractionMod = (float)1;
  private DependenceFlowGraph _dfg;
  private  _Conns _connectionCalculator = new _Conns();
  private  OwnRanNumGen ranNum; 
  
  /*public FDSchedule(DependenceFlowGraph dfg) {
    super(false, null);
    _dfg = dfg;
    ranNum = new OwnRanNumGen((int)System.currentTimeMillis());
  }
  public FDSchedule(DependenceFlowGraph dfg, boolean packNicht) {
    super(packNicht, null);
    _dfg = dfg;
    ranNum = new OwnRanNumGen((int)System.currentTimeMillis());
  }*/
  public FDSchedule(DependenceFlowGraph dfg, BlockNode node) {
    super(node);    
    _dfg = dfg;
    ranNum = new OwnRanNumGen((int)System.currentTimeMillis());
  }

  /*
  force Directed Scheduling
     
  force directed scheduling attempts to spread out the execution of the same type of instructions (e.g. adds, muls, and divs)
    so that they are executed, as much as possible at different times.  
       
  step 1)
  calculate windows when instructions can be executed, based on the times found by ASAP and ALAP
  and calculate a "force" due to the same type of instruction being executed at the same time
    foreach instruction
      find the ASAP and ALAP execution times
  minTime = minimum of two
  maxTime = max of two
  save window (instruction, minTime, maxtime)
  for(n_int=minTime => maxTime)
    set weight(operator, n_int, += 1/(maxTime + instr_exec_time - minTime)
  end foreach
     
  Step 2)
  here is where actual scheduling is done.  this can be divided into three substeps
  foreach instruction
    substep a)
  calculate forces for instruction
    foreach instruction
      for(n_int=window(instr, min)=> window(instr,max))
        sum(instr) += weight(n_int)*(1/(max-min))
  end foreach
    for(n_int=window(instr, min)=> window(instr,max))
      force(n_int) = -(sum(instr) - 2 * weight(n_int)*(1/(max-min))) +
        recursivePredecessorForces(instr, n_int) +
  recursiveSuccessorForces(instr, n_int + this instruction's exec time);
  it is done this way, because the force is equal to weight(n_int)*(1/(max-min)) - weight(all other ns)*(1/(max-min))
    because we are calculating the change in force due to not placing the instructions in all those other ns, but
      placing it at n_int.  We also need to calculate how this move will change all predecessors and successors.
  
  recursivePredecessorForces is defined so:
  predecessorForce = 0
  foreach instruction
    if(instr predecessor of above instr && new max < this instruction's old max)
      sumtmp1 = sum(instr);
  for(n_int=max => new max - this instructions execution time; n_int--)
    sumtmp1-=2 * weight(n_int) * 1/(this instruction's max-min)
  predecessorForce = recursivePredecessorForces(this instruction, n_int)
  else
    sumtmp1 = 0;
  endif
    end foreach
      this calculates the change in weights due to shrinking or growing the size of the window
  return sumtmp1 + predecessorForce;
  
  recursiveSuccessorForces is defined so:
  successorForce = 0
  foreach instruction
    if(instr predecessor of above instr && new min > this instruction's old min)
      sumtmp1 = sum(instr);
  for(n_int=min => new min; n_int++)
    sumtmp1-=2 * weight(n_int) * 1/(this instruction's max-min)
  successorForce = recursiveSuccessorForces(this instruction, n_int + this instruction's exec time)
  else
    sumtmp1 = 0;
  endif
    end foreach
      this calculates the change in weights due to shrinking or growing the size of the window
  return sumtmp1 + successorForce;
   
  end for
     
  substep b)
  schedule instruction at the point of its minimum force
    for(n_int=min=>max)
      find min force, save n_int
        setexecTime(instruction, n_int)
   
  substep c)
  recalculate weights and windows
  savewindow(instruction, n_int, n_int + instruction execution time)
   
  setWindowSuccessors(inst, n_int + instruction execution time)
  setWindowPredecessors(inst, n_int + instruction execution time)
   
  where setWindowSuccessors is
  foreach sucessor
    if(n_int + instruction execution time > old min)
      setwindow(sucessor, n_int + instruction execution time, oldmax)
  setWindowSuccessors(list, n_int + instruction execution time)
  end foreach
     
  and where setWindowPredecessors is
  foreach sucessor
    if(n_int + instruction execution time < old max)
      setwindow(sucessor, oldmin, n_int + instruction execution time)
  setWindowPredecessors(list, n_int + instruction execution time)
  end foreach
     
  for(n_int= 0 => end of setweight database) 
    setweight(instr.getoperator, n_int, 0)
  foreach instruction in savewindows
    for(n_int=instr min => max) 
      setweight(instr.getoperator, n_int, += 1/(max + instruction exec time - min)
   
   
  */
  
  private class Randomator extends Random {
  
    public Randomator() {
      super();
    }
    public Randomator(long seed) {
      super(seed);
    }
    public double nextDouble() {
      return (double)ranNum.ran2();
      //return (double)ranNum.gaussRan();
    }
    public float nextFloat() {
      return ranNum.ran2();
      //return ranNum.gaussRan();
    }
  
  }
    
  /** This method performs a force-directed schedule of the list of instructions
   *  saved in @param instrlist.  It can ignore instruction predicates and 
   *  schedule operations to be executed regardless of whether the predicate is 
   *  true or not (except when the instruction writes to a primal).
   * 
   * @param instrlist list of unscheduled instructions from hyperblock
   * @param aSAPlist copy of the list of instructions from the           
   *      hyperblock, which has been ASAP scheduled
   * @param aLAPlist copy of the list of instructions from the           
   *      hyperblock, which has been ALAP scheduled
   * @param ignorePredsNicht tells the scheduler to ignore predicates    
   *      except when the instruction is writing to a primal; true=do not     
   *      ignore them, false=ignore them
   * @param chipDef contains chip and board information such as where   
   *      arrays have been stored to memory and the availability of hardware.
   * @return whether scheduling was successful
   */
  public boolean fD_Schedule(ArrayList instrlist, FDWindows windowMap, 
                             boolean ignorePredsNicht, ChipDef chipDef) {
    HashMap defLists = new HashMap();
    HashMap useLists = new HashMap();
    HashMap dG = new HashMap(); //force table
    
    //chipDef.resetBusCntsAtAllTimes();
    
    //initialize use and deflists and save how many of each type of operator 
    //are used
    initVariables(instrlist, defLists, useLists, chipDef, ignorePredsNicht);
		   
    
    resetDG(dG, windowMap, chipDef);

    
    //Step 2)
    //here is where actual scheduling is done.  this can be divided into three substeps
    //Random ranNum2 = new Random(System.currentTimeMillis());
    Random ranNum2 = (Random)new Randomator(System.currentTimeMillis());
    ArrayList instrucListCopy = new ArrayList(instrlist);
    while(0 < instrucListCopy.size()){
      Collections.shuffle(instrucListCopy, ranNum2);
      //instrucListCopy = orderInstructsForFD(instrucListCopy);
      
      //find the guy with the smallest window:      
      Instruction instr = windowMap.findInstWithMinWin(instrucListCopy);    
      //find random instruction (this is much slower since the windows are bigger):
      //Instruction instr = (Instruction)instrucListCopy.iterator().next();    
     
     //remove him from the list so he won't be scheduled again
      instrucListCopy.remove(instr);
      
      //substep a)
      //calculate forces for instruction
      ArrayList force = new ArrayList();
      if(!calcForceTable(dG, windowMap, chipDef, instrlist, instr, force, defLists,
                         useLists)) {
        System.out.println("failed calc force");
	return false;
      }
      //=========================================================================================================================== 
      //substep b)
      //schedule instruction at the point of its minimum force
      float smallestLoc = findScheduleTime(windowMap, chipDef, instr, force, 
                                           instrlist, useLists, defLists);


      if(smallestLoc == -1/*||
    	 (!chipDef.analyzeHardwareUse(_node, instr, (int)smallestLoc))*/) {
        System.out.println("failed on instr "+ instr);
        //System.out.println("smallestLoc "+ smallestLoc);
    	return false;
      }
      else {
      /*if((smallestLoc != -1)/*&&
    	 (chipDef.analyzeHardwareUse(instr, (int)smallestLoc))) {*/
    	  
        chipDef.saveNewHardwareUsage(_node, instr, (int)smallestLoc);
    	 
        //set the execution and clock times 
    	instr.setExecTime(smallestLoc);
    	instr.setExecClkCnt((int)smallestLoc);
    	 
    	//=========================================================================================================================== 
    	//substep c)
    	//recalculate weights and windows

        //reset window size for this instruction to its execution time
    	windowMap.putWin(instr, smallestLoc, smallestLoc);
    	
    	//reset his successor's and predecessor's windows
        //setWindowSuccessors(inst, n_int + instruction execution time)
    	if(!setWindowSuccessors(instrlist, instr, useLists, defLists, windowMap, 
        		        smallestLoc + getInstrRunLength(instr, chipDef) /*+
				          (1/_schedCycleFraction)*/, 
        		        chipDef)) {
          System.out.println("set window sucs");
	  return false;	
    	}
	//setWindowPredecessors(inst, n_int + instruction execution time)
    	if(!setWindowPredecessors(instrlist, instr, useLists, defLists, windowMap, 
        		          smallestLoc /*- (1/_schedCycleFraction)*/, chipDef)) {
          System.out.println("set window preds");
	  return false;	  
        }
    	//change weights:
	resetDG(dG, windowMap, chipDef);
    	

      } 
      //return false;
       
    }//end foreach instruction
    
    chipDef.saveSharedOps();	  
     
     
    return true;
       
  }
   
  //debug methods:
   
  /** This methode was written for helping to debug the force directed 
   *  scheduler.  It takes the list of instructions, @param instrlist, and 
   *  saves text representing the possible execution window for each 
   *  instruction.  This execution window is initialized with the ASAP 
   *  scheduled time as the minimum possible time for the Force-directed 
   *  scheduler to schedule an instruction, and with the ALAP as the maximum 
   *  possible.  This method goes recursively up through the instruction 
   *  hierarchy determining analyzing to know how to print.
   * 
   * @param instrlist list of instructions from hyperblock, whcih the 
   *	 force-directed scheduler is working on
   * @param childInstr the starting instruction, from which this     
   *	  recursive algorithm should attempt to go up from through the      
   *	  dependence hierarchy
   * @param useLists a hashmap saving a list of all operands being   
   *	  used for a given instruction (key=instruction; value=list of used 
   *	  operands)
   * @param defLists a hashmap saving a list of all operands being   
   *	  defined by a given instruction (key=instruction; value=list of    
   *	  defined operands)
   * @param windowMax a hashmap saving the upper limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  upper limit
   * @param windowMin a hashmap saving the lower limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  lower limit
   * @param n_int time step, which for which information is currently
   *	  being printed
   * @param printOutVarName a boolean variable telling how to print  
   *	  the output operand
   * @param depthInfo a hashmap saving text representing the	     
   *	  execution windows for each instruction
   * @param depth ???
   * @NewTag ...
   */
  public void printPredecessorWindows(ArrayList instrlist, 
  				      Instruction childInstr, HashMap useLists, 
        			      HashMap defLists, HashMap windowMax, 
  				      HashMap windowMin, int n_int, 
        			      boolean printOutVarName, HashMap depthInfo, 
        			      int depth) {
    for (Iterator it = ((ArrayList)instrlist).iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();   
      if((isInstrPred((ArrayList) useLists.get(childInstr), 
         (ArrayList) defLists.get(instr)))&&
         (areOutsNotPrimals((ArrayList) defLists.get(instr)))) {
  	printPredecessorWindows(instrlist, instr, useLists, defLists, 
        			windowMax, windowMin, n_int, 
        			printOutVarName, depthInfo, depth);
  	depth--;
  	if(!(depthInfo.containsKey(instr))) {
  	  depthInfo.put(instr, new ArrayList());
  	  ((ArrayList)(depthInfo.get(instr))).add(new Integer(depth));
  	  ((ArrayList)(depthInfo.get(instr))).add("");
  	}
  	else
  	  if(depth <  ((Integer)(((ArrayList)(depthInfo.get(instr)))
        					      .get(0))).intValue())
  	    ((ArrayList)(depthInfo.get(instr))).set(0, new Integer(depth));
  	 
  	 
  	if(!(printOutVarName)) {
  	  float minTime = ((Float)windowMin.get(instr)).floatValue();
  	  float maxTime = ((Float)windowMax.get(instr)).floatValue();
  	  if((n_int>=minTime)&&(n_int<=maxTime))
  	    ((ArrayList)(depthInfo.get(instr))).set(1, " * |"); 
  	  else
  	    ((ArrayList)(depthInfo.get(instr))).set(1, " # |"); 
  	}
  	else {
  	  ((ArrayList)(depthInfo.get(instr))).set(1, (((ArrayList) defLists
        				   .get(instr)).toString() + " |"));
  	}
      }
    } //end foreach
    
  }
  /** This methode was written for helping to debug the force directed 
   *  scheduler.  It takes the list of instructions, @param instrlist, and 
   *  saves text representing the possible execution window for each 
   *  instruction.  This execution window is initialized with the ASAP 
   *  scheduled time as the minimum possible time for the Force-directed 
   *  scheduler to schedule an instruction, and with the ALAP as the maximum 
   *  possible.  This method goes recursively up through the instruction 
   *  hierarchy determining analyzing to know how to print.
   * 
   * @param instrlist list of instructions from hyperblock, which the 
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
   * @param windowMax a hashmap saving the upper limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  upper limit
   * @param windowMin a hashmap saving the lower limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  lower limit
   * @param n_int time step, which for which information is currently
   *	  being printed
   * @param printOutVarName a boolean variable telling how to print  
   *	  the output operand
   * @param depthInfo a hashmap saving text representing the	     
   *	  execution windows for each instruction
   * @param depth a hashmap saving text representing the  execution  
   *	  windows for each instruction
   */
  public void printSucessorWindows(ArrayList instrlist, 
  				   Instruction parentInstr, HashMap useLists, 
        			   HashMap defLists, HashMap windowMax, 
        			   HashMap windowMin, int n_int, 
        			   boolean printOutVarName, HashMap depthInfo, 
        			   int depth) {
    for (Iterator it = ((ArrayList)instrlist).iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();   
      if((isInstrPred((ArrayList) useLists.get(instr), (ArrayList) defLists
               .get(parentInstr)))&&
          (areOutsNotPrimals((ArrayList) useLists.get(instr)))) {
  	depth++;
  	if(!(depthInfo.containsKey(instr))) {
  	  depthInfo.put(instr, new ArrayList());
  	  ((ArrayList)(depthInfo.get(instr))).add(new Integer(depth));
  	  ((ArrayList)(depthInfo.get(instr))).add("");
  	}
  	else
  	  if(depth >  ((Integer)(((ArrayList)(depthInfo.get(instr))).get(0)))
        	       .intValue())
  	    ((ArrayList)(depthInfo.get(instr))).set(0, new Integer(depth));
  	if(!(printOutVarName)) {
  	  float minTime = ((Float)windowMin.get(instr)).floatValue();
  	  float maxTime = ((Float)windowMax.get(instr)).floatValue();
  	  if((n_int>=minTime)&&(n_int<=maxTime))
  	    ((ArrayList)(depthInfo.get(instr))).set(1, " * |"); 
  	  else
  	    ((ArrayList)(depthInfo.get(instr))).set(1, " # |"); 
  	}
  	else {
  	  ((ArrayList)(depthInfo.get(instr))).set(1, (((ArrayList) 
        			   defLists.get(instr)).toString() + " |"));
  	}
  	printSucessorWindows(instrlist, instr, useLists, defLists, 
        		     windowMax, windowMin, n_int, printOutVarName, 
        		     depthInfo, depth);
  	 
  	 
      }
    } //end foreach
    
  }
  
  private class _Conns extends HashMap {
  
    private HashMap _instrToNodeMap = new HashMap();
    _Conns() {
    
    }
    
    public Boolean get(Instruction parentInstr, Instruction childInstr) {
      if(_dfg == null) return new Boolean(true);
      
      if(!this.containsKey(parentInstr))
        this.put(parentInstr, new HashMap());
      HashMap destMap = (HashMap)this.get(parentInstr);
      if(destMap.containsKey(childInstr))
        return (Boolean)destMap.get(childInstr);
      DependenceFlowNode parentNode = null;
      if(!_instrToNodeMap.containsKey(parentInstr)) {
        parentNode = _dfg.findNode(parentInstr);
	_instrToNodeMap.put(parentInstr, parentNode);
      }
      else
        parentNode = (DependenceFlowNode)_instrToNodeMap.get(parentInstr);
      DependenceFlowNode childNode = null;
      if(!_instrToNodeMap.containsKey(childInstr)) {
        childNode = _dfg.findNode(childInstr);
	_instrToNodeMap.put(childInstr, childNode);
      }
      else
        childNode = (DependenceFlowNode)_instrToNodeMap.get(childInstr);
      DependenceFlowEdge conn = (DependenceFlowEdge)_dfg.findEdge(parentNode, childNode);
      if(conn !=null && !conn.getisBackWardsPointing() && 
        (conn instanceof DependenceDataFlowEdge)) {
        destMap.put(childInstr, new Boolean(true));
	return new Boolean(true);
      }
      else
      {
	destMap.put(childInstr, new Boolean(false));
	return new Boolean(false);
      }
    }
  
  }
  
  public boolean isParentOf(Instruction parentInstr, Instruction childInstr, 
                            HashMap defLists) {
    if((_connectionCalculator.get(parentInstr, childInstr).booleanValue()) && 
       areOutsNotPrimals((ArrayList) defLists.get(parentInstr))) {
      //System.out.println("parentInstr " + parentInstr + " is parent of ");
      //System.out.println("childInstr " + childInstr);
      return true;
    }
    else
      return false;
  }
  
  //end debug methods
  //where setWindowSuccessors is
  /** This method goes down recursively through the hierarchy of instructions,
   *  resizing their execution windows by resetting their minimum execution 
   *  time.  This is performed after an instruction has been scheduled.  Its 
   *  successor instructions can now, no longer be executed any sooner than 
   *  this just scheduled instruction has completed execution.
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
   * @param chipDef contains chip and board information such as     
   *	  where    arrays have been stored to memory and the availability of
   *	  hardware.
   */
  public boolean setWindowSuccessors(ArrayList instrlist, 
  				  Instruction parentInstr, HashMap useLists, 
        			  HashMap defLists, FDWindows windowMap, 
  				  float execTime, ChipDef chipDef) {
    //foreach sucessor
    for (Iterator it = ((ArrayList)instrlist).iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();   
      
      //if(instr predecessor of above instr && new min > this instruction's old min)
     //if((isInstrPred((ArrayList) useLists.get(instr), (ArrayList) 
     //   				      defLists.get(parentInstr)))&&
     //   				      (((areOutsNotPrimals((ArrayList) 
     //   				      useLists.get(instr)))&&
	//				      (Load.conforms(instr)))||
	//				      (!Load.conforms(instr)))) {
      /*DependenceFlowNode parentNode = _dfg.findNode(parentInstr);
      DependenceFlowNode node = _dfg.findNode(instr);
      DependenceFlowEdge conn = (DependenceFlowEdge)_dfg.findEdge(parentNode, node);
      if(conn !=null && !conn.getisBackWardsPointing() && 
         (conn instanceof DependenceDataFlowEdge) &&
	 (areOutsNotPrimals((ArrayList) useLists.get(instr)))){*/
      if(isParentOf(parentInstr, instr, defLists)) {	
        //if(n_int + instruction execution time > old min)
  	if(execTime > windowMap.getMin(instr)) {
  	  //setwindow(sucessor, n_int + instruction execution time, oldmax)
          execTime = ((float)Math.round(execTime * _schedCycleFraction))/_schedCycleFraction;
	    //throw new ScheduleException("Error: Failed to create a valid Forc" +
	    //                             "e-Directed" + " Schedule");
	    
	  if(execTime > windowMap.getMax(instr)) {
	   /* System.out.println("failed setWindowSuccessors on instr " + instr);
	    System.out.println("execTime " + execTime);
	    System.out.println("windowMap.getMax(instr) " + windowMap.getMax(instr));
	    System.out.println("windowMap.getMin(instr) " + windowMap.getMin(instr));*/
	    return false;
	  }
	  windowMap.putMin(instr, execTime);
  	  if(!setWindowSuccessors(instrlist, instr, useLists, defLists, windowMap, 
        		          execTime + getInstrRunLength(instr, chipDef) /*+
			           (1/_schedCycleFraction)*/, 
            		          chipDef))
            return false;
  	}
  	 
      }
    } //end foreach
    return true;
  }	
  //and where setWindowPredecessors is
  /** This method goes up recursively through the hierarchy of instructions, 
   *  resizing their execution windows by resetting their maximum execution 
   *  time.  This is performed after an instruction has been scheduled.  Its 
   *  predecessor instructions can now, no longer be executed any later than 
   *  this just scheduled instruction has started.
   * 
   * @param instrlist list of instructions from hyperblock, whcih the 
   *	 force-directed scheduler is working on
   * @param childInstr the starting instruction, from which this     
   *	  recursive algorithm should attempt to go up from through the      
   *	  dependence hierarchy
   * @param useLists a hashmap saving a list of all operands being   
   *	  used for a given instruction (key=instruction; value=list of used 
   *	  operands)
   * @param defLists a hashmap saving a list of all operands being   
   *	  defined by a given instruction (key=instruction; value=list of    
   *	  defined operands)
   * @param windowMax a hashmap saving the upper limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  upper limit
   * @param execTime a new minimum execution time for this	     
   *	  instruction.  It is determined after scheduling a predecessor
   * @param chipDef contains chip and board information such as     
   *	  where    arrays have been stored to memory and the availability of
   *	  hardware.
   */
  public boolean setWindowPredecessors(ArrayList instrlist, 
  				    Instruction childInstr, HashMap useLists, 
        			    HashMap defLists, FDWindows windowMap, 
  				    float execTime, ChipDef chipDef) {
    //foreach sucessor
    for (Iterator it = ((ArrayList)instrlist).iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();   
      
      
      //if((isInstrPred((ArrayList) useLists.get(childInstr), (ArrayList) 
      //  		defLists.get(instr)))&&
      //  		(areOutsNotPrimals((ArrayList) defLists.get(instr)))){
      /*DependenceFlowNode childNode = _dfg.findNode(childInstr);
      DependenceFlowNode node = _dfg.findNode(instr);
      DependenceFlowEdge conn = (DependenceFlowEdge)_dfg.findEdge(node, childNode);
      if(conn !=null && !conn.getisBackWardsPointing() && 
         (conn instanceof DependenceDataFlowEdge) &&
	 (areOutsNotPrimals((ArrayList) useLists.get(childInstr)))) {*/
      if(isParentOf(instr, childInstr, defLists)) {	
        //if(n_int + instruction execution time < old max)
	
	if(execTime < windowMap.getMax(instr) 
        			       + getInstrRunLength(instr, chipDef)) {
  	  
	    //throw new ScheduleException("Error: Failed to create a valid Forc" +
	    //                             "e-Directed" + " Schedule");
	  //setwindow(sucessor, oldmin, n_int + instruction execution time)
          float execTimeNew = execTime - getInstrRunLength(instr, chipDef) /*-
	                      (1/_schedCycleFraction)*/;
          CorrectStartTimes adjustTimes 
	    = new CorrectStartTimes(instr, chipDef, !GlobalOptions.packInstructions);

	  float correctedCycle = adjustTimes.getCorrectedStartTime(execTimeNew);
	  if(correctedCycle > execTimeNew)
	    correctedCycle--;
  	  if((execTimeNew < windowMap.getMin(instr))) {
	    /*System.out.println("failed setWindowPredecessors on instr " + instr);
	    System.out.println("execTime " + execTime);
	    System.out.println("windowMap.getMax(instr) " + windowMap.getMax(instr));
	    System.out.println("windowMap.getMin(instr) " + windowMap.getMin(instr));*/
            return false;
	  }
          correctedCycle = ((float)Math.round(correctedCycle * _schedCycleFraction))/
			    _schedCycleFraction;
	  windowMap.putMax(instr, execTimeNew);
	  //setWindowPredecessors(list, n_int + instruction execution time)
  	  if(!setWindowPredecessors(instrlist, instr, useLists, defLists, 
        			    windowMap, execTimeNew, chipDef))
	    return false;
  	}
      }
    } //end foreach
    return true;
  }
   
  /** This method calculates forces acting on an instruction if it is placed 
   *  at a given time, as a result of later instructions in the hierarchy.
   * 
   * @param dG Weights as a result of other like operations (e.g. other add 
   *	 instructions) being executed or possibly being executed in tis time
   *	  step
   * @param instrlist list of instructions from hyperblock, whcih the
   *	  force-directed scheduler is working on
   * @param childInstr the starting instruction, from which this     
   *	  recursive algorithm should attempt to go up from through the      
   *	  dependence hierarchy
   * @param useLists a hashmap saving a list of all operands being   
   *	  used for a given instruction (key=instruction; value=list of used 
   *	  operands)
   * @param defLists a hashmap saving a list of all operands being   
   *	  defined by a given instruction (key=instruction; value=list of    
   *	  defined operands)
   * @param newMax ???
   * @param windowMin a hashmap saving the lower limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  lower limit
   * @param windowMax a hashmap saving the upper limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  upper limit
   * @param sum a sum of all forces acting in a timeslot
   * @param CanCalc keine ahnung aber bestimmt gibt's ein Grund fuer 
   *	  ihm
   * @param chipDef contains chip and board information such as     
   *	  where    arrays have been stored to memory and the availability of
   *	  hardware.
   * @return some of forces due to predecessors
   */
  public float recursivePredecessorForces(HashMap dG, ArrayList instrlist, 
  					  Instruction childInstr, 
        				  HashMap useLists, HashMap defLists, 
  					  float newMax, FDWindows windowMap, 
        				  /*HashMap sum,*/ Boolean CanCalc, 
        				  ChipDef chipDef) {
    float predecessorForce = 0;
    //float sumtmp1 = 0;
    //foreach instruction
    float force = 0;
    for (Iterator it = ((ArrayList)instrlist).iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();   
      float timeMod = 1;
      if(getInstrRunLength(instr, chipDef) < 1)
	timeMod = _schedCycleFraction * _schedCycleFractionMod;
      /*if((isInstrPred((ArrayList) useLists.get(childInstr),(ArrayList)defLists
        		      .get(instr))) && 
        		      (newMax - getInstrRunLength(instr, chipDef) < 
        		      windowMap.getMax(instr)) &&
  			(areOutsNotPrimals((ArrayList) defLists.get(instr)))){*/
      if((isParentOf(instr, childInstr, useLists))&& 
         (newMax - getInstrRunLength(instr, chipDef) < 
	                              windowMap.getMax(instr))){
  	//sumtmp1 = ((Float)(sum.get(instr))).floatValue();
  	//for(int n_int =  (int)windowMap.getMax(instr); 
        	//n_int >= newMax - getInstrRunLength(instr, chipDef); n_int--){
        ArrayList forceTableForOp = ((ArrayList)(dG.get(instr.operator())));
	float oldWin = windowMap.getWinSize(instr);
	oldWin = (int)(oldWin + 0.999);
	if(oldWin == 0) oldWin = 1;
  	for(int n_int =  (int)windowMap.getMin(instr); 
        	n_int < (int)newMax; n_int++){
  	  if(n_int<0) {
  	    CanCalc = new Boolean(false);
  	    return 0;
  	  }
	  float newWin = newMax - windowMap.getMax(instr);
	  newWin = (int)(newWin + 0.999);
	  if(newWin == 0) newWin = 1;
	  int minIndex = (int)(n_int* timeMod);
	  float maxIndex =  (n_int + getInstrRunLength(instr, chipDef)) * timeMod;
	  for(int x = minIndex; x < maxIndex; x++) {
	  
	    float offset = ((Float)forceTableForOp.get(x)).floatValue();
	    force += (1/newWin - 1/oldWin) * offset;
	  
	  }
	  
  	  //float selfforcetmp = 1;
  	  //if(windowMap.getWinSize(instr) > 1)
  	    //selfforcetmp = (float)((int)((1/windowMap.getWinSize(instr))
        	//			 + 0.9));
  	  //sumtmp1 -= 2 * ((Float)(((ArrayList)(dG.get(instr.operator())))
        	//				    .get(n_int))).floatValue()
        	  //   * selfforcetmp;
  	}
  	for(int n_int =  (int)newMax; 
        	n_int <= (int)(windowMap.getMax(instr)+0.999); n_int++){
  	  if(n_int<0) {
  	    CanCalc = new Boolean(false);
  	    return 0;
  	  }
	  int minIndex = (int)(n_int* timeMod);
	  float maxIndex =  (n_int + getInstrRunLength(instr, chipDef)) * timeMod;
	  for(int x = minIndex; x < maxIndex; x++) {
	  
	    float offset = ((Float)forceTableForOp.get(x)).floatValue();
	    force += - (1/oldWin) * offset;
	  
	  }
  	}
  	//newMax -= getInstrRunLength(instr, chipDef);
	predecessorForce = recursivePredecessorForces(dG, instrlist, instr, 
        					      useLists, defLists, 
        					      newMax, windowMap, /*sum, */
        					      CanCalc, chipDef);
      }
      //else
  	//sumtmp1 = 0;
      //endif
    }//end foreach
    //this calculates the change in weights due to shrinking or growing the size of the window
    return force + predecessorForce;
  }
  
  //recursiveSuccessorForces is defined so:
  /** This method calculates forces acting on an instruction if it is placed 
   *  at a given time, as a result of earlier instructions in the hierarchy.
   * 
   * @param dG Weights as a result of other like operations (e.g. other add 
   *	 instructions) being executed or possibly being executed in tis time
   *	  step
   * @param instrlist list of instructions from hyperblock, whcih the
   *	  force-directed scheduler is working on
   * @param parentInstr the starting instruction, from which this    
   *	  recursive algorithm should attempt to go down from through the    
   *	  dependence hierarchy
   * @param useLists a hashmap saving a list of all operands being   
   *	  used for a given instruction (key=instruction; value=list of used 
   *	  operands)
   * @param defLists a hashmap saving a list of all operands being   
   *	  defined by a given instruction (key=instruction; value=list of    
   *	  defined operands)
   * @param newMin ????
   * @param windowMin a hashmap saving the lower limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  lower limit
   * @param windowMax a hashmap saving the upper limit for scheduling
   *	  a given instruction (key=instruction; value=float value of the    
   *	  upper limit
   * @param sum a sum of all forces acting in a timeslot
   * @param CanCalc ????
   * @param chipDef contains chip and board information such as     
   *	  where    arrays have been stored to memory and the availability of
   *	  hardware.
   * @return some of forces due to predecessors
   */
  public float recursiveSuccessorForces(HashMap dG, ArrayList instrlist, 
  					Instruction parentInstr, 
        				HashMap useLists, HashMap defLists, 
  					float newMin, FDWindows windowMap, 
        				/*HashMap sum,*/ Boolean CanCalc, 
        				ChipDef chipDef) {
    float successorForce = 0;
    //float sumtmp1 = 0;
    float force = 0;
    //foreach instruction
    for (Iterator it = ((ArrayList)instrlist).iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();   
      float timeMod = 1;
      if(getInstrRunLength(instr, chipDef) < 1)
	timeMod = _schedCycleFraction * _schedCycleFractionMod;
      //if(instr predecessor of above instr && new min > this instruction's old min)
      /*if((isInstrPred((ArrayList) useLists.get(instr), 
        			    (ArrayList) defLists.get(parentInstr)))
         &&  (newMin > windowMap.getMin(instr)) 
  	 &&  (areOutsNotPrimals((ArrayList) useLists.get(instr)))) {*/
      if((isParentOf(parentInstr, instr, useLists))&&  
         (newMin > windowMap.getMin(instr))) {
         
  	//sumtmp1 = ((Float)(sum.get(instr))).floatValue();
  	//for(n_int=min => new min; n_int++)
  	for(int n_int =  (int)windowMap.getMin(instr); 
               //(n_int <= newMin) && (n_int <  windowMap.getabsMax()); 
                n_int < (int)newMin; 
	        n_int++) {
  	  if(n_int<0) {
  	    CanCalc = new Boolean(false);
  	    return 0;
  	  }
  	  //float selfforcetmp = 1;
  	  //if(windowMap.getWinSize(instr) > 1)
          //  selfforcetmp = (float)((int)((1/windowMap.getWinSize(instr))
          //		  + 0.9));

        		
          ArrayList forceTableForOp = ((ArrayList)(dG.get(instr.operator())));
	  //sumtmp1 -= 2 * ((Float)(forceTableForOp.get(n_int))).floatValue()
          //	     * selfforcetmp;
	  
	  float oldWin = windowMap.getWinSize(instr);
	  oldWin = (int)(oldWin + 0.999);
	  if(oldWin == 0) oldWin = 1;
	  int minIndex = (int)(n_int* timeMod);
	  float maxIndex =  (n_int + getInstrRunLength(instr, chipDef)) *  
            		     timeMod;
	  /*System.out.println("timeMod " + timeMod) ;
	  System.out.println("n_int " + n_int) ;
	  System.out.println("minIndex " + minIndex) ;
	  System.out.println("timeMod " + timeMod) ;
	  System.out.println("maxIndex " + maxIndex) ;
	  System.out.println("instr " + instr) ;
	  System.out.println("newMin " + newMin) ;
	  System.out.println("getInstrRunLength(instr, chipDef) " + getInstrRunLength(instr, chipDef)) ;
	  */
	  for(int x = minIndex; x < maxIndex; x++) {
	  
	    float offset = ((Float)forceTableForOp.get(x)).floatValue();
	    force += - (1/oldWin) * offset;
	  
	  }
	  
  	}
  	for(int n_int = (int)newMin; 
               //(n_int <= newMin) && (n_int <  windowMap.getabsMax()); 
                n_int <= (int)(windowMap.getMax(instr)+0.999); 
	        n_int++) {
  	  if(n_int<0) {
  	    CanCalc = new Boolean(false);
  	    return 0;
  	  }

        		
          ArrayList forceTableForOp = ((ArrayList)(dG.get(instr.operator())));
	  
	  float oldWin = windowMap.getWinSize(instr);
	  oldWin = (int)(oldWin + 0.999);
	  if(oldWin == 0) oldWin = 1;
	  float newWin = windowMap.getMax(instr) - newMin;
	  newWin = (int)(newWin + 0.999);
	  if(newWin == 0) newWin = 1;
	  int minIndex = (int)(n_int* timeMod);
	  float maxIndex =  (n_int + getInstrRunLength(instr, chipDef)) *  
            		     timeMod;
	  for(int x = minIndex; x < maxIndex; x++) {
	  
	    float offset = ((Float)forceTableForOp.get(x)).floatValue();
	    force += (1/newWin - 1/oldWin) * offset;
	  
	  }
	  
  	}
  	successorForce = recursiveSuccessorForces(dG, instrlist, instr, 
        					  useLists, defLists, 
        					  newMin + 
        				   getInstrRunLength(instr, chipDef), 
        					  windowMap, /*sum, */
        					  CanCalc, chipDef);
  	//successorForce = recursiveSuccessorForces(this instruction, n_int + this instruction's exec time)
      }
      //else
  	//sumtmp1 = 0;
      //endif
    } //end foreach
    //this calculates the change in weights due to shrinking or growing the size of the window
    return force + successorForce;
  } 

  public void initVariables(ArrayList instrlist, HashMap defLists, 
                            HashMap useLists, ChipDef chipDef, 
			    boolean ignorePredsNicht) {
    for (Iterator it = ((ArrayList)instrlist).iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();
      defLists.put(instr, new ArrayList());
      useLists.put(instr, new ArrayList());
      getDefandUseLists(instr,(ArrayList)defLists.get(instr),
                       (ArrayList)useLists.get(instr), ignorePredsNicht);
      chipDef.initOpUseLists(instr.operator());
    }
  }  
  /** this method was written to try and spread out instructions depending on 
   *  what kind of operator they used.  I wrote it to help with force-directed
   *  scheduling, but, it turns out it was unnecessary, because the great 
   *  Heather, had the answer to my problem all along!
   * 
   * @param instrList list of instructions from the hyperblock
   * @return new ordered list of instructions
   */
  public ArrayList orderInstructsForFD(ArrayList instrList) {
    HashMap opLists = new HashMap();
    ArrayList newList = new ArrayList();
    
    for (Iterator its = instrList.iterator(); 
      its.hasNext(); ) {
      Instruction inst = (Instruction)its.next();
      Operator op = inst.operator();
      if(!opLists.containsKey(op))
        opLists.put(op, new ArrayList());
      ((ArrayList)opLists.get(op)).add(inst);

    }
    
    Random rand = new Random();
    float randNum = 0;
    int leftToSchedule = instrList.size();
    while(leftToSchedule > 0) {
    
      randNum = rand.nextFloat();
      float prob = 0;
      for (Iterator its = opLists.keySet().iterator(); 
  	its.hasNext(); ) {
  	Operator op = (Operator)its.next();
        float oldProb = prob;
        prob += (float)((ArrayList)opLists.get(op)).size() / (float)leftToSchedule;
        if((oldProb < randNum)&&(randNum <= prob)) {
          newList.add(((ArrayList)opLists.get(op)).get(0));
          ((ArrayList)opLists.get(op)).remove(0);
          leftToSchedule--;
        }
      }  
    }
    /*for (Iterator its = instrList.iterator(); 
      its.hasNext(); ) {
      Instruction inst = (Instruction)its.next();
      Operator op = inst.operator();
      System.out.println("op " + op);
    }*/
    return  newList;
  }

  public void resetDG(HashMap dG, FDWindows windowMap, ChipDef chipDef) {
  
    dG.clear();
    
    for (Iterator it = (((HashMap)windowMap).keySet()).iterator(); 
    	 it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();
      Operator op = instr.operator();
      ArrayList listofOps = new ArrayList();
      dG.put(op, listofOps);
      float max = windowMap.getMax(instr) + 
    		  getInstrRunLength(instr, chipDef);
      
      //System.out.println("abs instr " + instr);
      //System.out.println("max " + max);
      //System.out.println("abs max " + windowMap.getabsMax());
      //System.out.println("abs max+ runlength " + (windowMap.getabsMax()+ getInstrRunLength(instr, chipDef)));
      float timeMod = 1;
      if(getInstrRunLength(instr, chipDef) < 1)
        timeMod = _schedCycleFraction * _schedCycleFractionMod;
      
      while(listofOps.size() <= (windowMap.getabsMax() + 
    		                getInstrRunLength(instr, chipDef))*
				timeMod)
        listofOps.add(new Float(0));

      //for(n_int=instr min => max) 
      //   setweight(instr.getoperator, n_int, += 1/(max + instruction exec time - min)
      int minIndex = (int)(windowMap.getMin(instr)*
                           timeMod);
      float maxIndex =  max *  timeMod;
				  
      for(int n_int = minIndex;
    	      //(n_int <  max) && (n_int <  windowMap.getabsMax()); 
    	      n_int <  maxIndex; 
	      n_int++) {
    	
	float newWeight;
    	float winSize = (int)(windowMap.getWinSize(instr) + 
    	                      getInstrRunLength(instr, chipDef)+0.999);
	if(winSize == 0) winSize = 1;
	
	/*if(windowMap.getWinSize(instr) + 
    	    getInstrRunLength(instr, chipDef) < 1) {
	  float oldWeight = ((Float)listofOps.get(n_int)).floatValue();
    	  newWeight = oldWeight + 1;
    	}
	else {
	  float oldWeight = ((Float)listofOps.get(n_int)).floatValue();
	  float adjustment = 1/(windowMap.getWinSize(instr) 
    		                + getInstrRunLength(instr, chipDef));
    	  newWeight = oldWeight + adjustment;
    	}*/
	float adjustment = 1/winSize;
	float oldWeight = ((Float)listofOps.get(n_int)).floatValue();
    	newWeight = oldWeight + adjustment;
	listofOps.set(n_int, new Float(newWeight));
      }
    } 
    
  
  }
  
  public boolean calcForceTable(HashMap dG, FDWindows windowMap, ChipDef chipDef,
                                ArrayList instrlist, Instruction instr, 
				ArrayList force, HashMap defLists, HashMap useLists) {
    //HashMap sum = new HashMap();
    float sum = 0;
    Operator op1 = instr.operator();
    ArrayList forceTableForOp1 = (ArrayList)dG.get(op1);
    
    //foreach instruction
    //for (Iterator it = ((ArrayList)instrlist).iterator(); 
       //  it.hasNext(); ) {
      //Instruction instrTmp = (Instruction)it.next();
      //float runlength = getInstrRunLength(instrTmp, chipDef);
      //Operator op = instrTmp.operator();
      //ArrayList forceTableForOp = (ArrayList)dG.get(op);
      //ArrayList forceTableForOp = (ArrayList)dG.get(op1);
      //sum.put(instrTmp, new Float(0));
      
      float timeMod = 1;
      if(getInstrRunLength(instr, chipDef) < 1)
        timeMod = _schedCycleFraction * _schedCycleFractionMod;
      for(int n_int =  (int)windowMap.getMin(instr); 
    	      n_int <= windowMap.getMax(instr); 
              n_int++) {
        
    	//float sumtmp = 1;
        //if(windowMap.getWinSize(instrTmp) > 1)  		  
        //  sumtmp = 1/windowMap.getWinSize(instrTmp);
        
	float winSize = windowMap.getWinSize(instr);
	winSize = (int)(winSize + 0.999); //round up because it is full cycles
	                                  //that the instruction affects
	if(winSize == 0) winSize = 1;
	int minIndex = (int)(n_int* timeMod);
	float maxIndex =  (n_int + getInstrRunLength(instr, chipDef)) *  
                           timeMod;
	for(int x = minIndex;
	        x < maxIndex;
		x++) {
	  //float oldSum = ((Float)sum.get(instrTmp)).floatValue();
	  float offset = ((Float)forceTableForOp1.get(x)).floatValue();
	  offset *= 1/winSize;
	  //sum.put(instrTmp, new Float(oldSum + offset));
	  sum += offset;
	}
      }
       
    //}//end foreach instruction
     
    //for(n_int=window(instr, min)=> window(instr,max))
    float initMaxIndex =  (windowMap.getabsMax() + 
                           getInstrRunLength(instr, chipDef) ) * timeMod;
    for(int n_int=0; n_int <= initMaxIndex; n_int++) {
      force.add(new Float(0.0));
    }
    for(int n_int=(int)windowMap.getMin(instr); 
    	    n_int <= windowMap.getMax(instr); 
            n_int++) {
    
      Boolean predForcesSetOk = new Boolean(true);
      Boolean succForcesSetOk = new Boolean(true);
      //float selfforcetmp = 1;
    
      //if(windowMap.getWinSize(instr) > 1)			
      //  selfforcetmp = 1/windowMap.getWinSize(instr);
	
      //float oldSum = ((Float)sum.get(instr)).floatValue();
      //offset *= selfforcetmp;
      float tmp = (n_int + getInstrRunLength(instr, chipDef));
      float recursivePredForces = recursivePredecessorForces(dG, instrlist, instr, 
    						  useLists, defLists, n_int,
    						  windowMap, /*sum,*/ 
    						  predForcesSetOk, chipDef);
      float recursiveSuccForces = recursiveSuccessorForces(dG, instrlist, instr, 
    						useLists, defLists, 
    						n_int + getInstrRunLength(instr, 
						                          chipDef),
    						windowMap, /*sum,*/ 
    						succForcesSetOk, chipDef);
    
      int minIndex = (int)(n_int* timeMod);
      float maxIndex =  (n_int + getInstrRunLength(instr, chipDef)) * timeMod;
      for(int x = minIndex;
	      x <= maxIndex;
	      x++) {
	float offset = ((Float)forceTableForOp1.get(x)).floatValue();
	float oldForce = ((Float)force.get(x)).floatValue();

	//force.set(x, new Float( offset - oldSum + oldForce + recursivePredForces 
	force.set(x, new Float( offset - sum + oldForce + recursivePredForces 
    		        	+ recursiveSuccForces));
      }
      //it is done this way, because the force is equal to weight(n_int)*(1/(max-min)) - weight(all other ns)*(1/(max-min))
      //because we are calculating the change in force due to not placing the instructions in all those other ns, but
      //placing it at n_int.  We also need to calculate how this move will change all predecessors and successors.
      if((!(predForcesSetOk.booleanValue()))||
    	 (!(succForcesSetOk.booleanValue())))
        return false;
    }//end for
    return true;
  }
  
  
  public float findScheduleTime(FDWindows windowMap, ChipDef chipDef, 
                                Instruction instr, ArrayList force,
				ArrayList instrlist, HashMap useLists, 
        			HashMap defLists ) {
      
    float smallestLoc = -1;
    float smallestForce = 999999999;
    HashSet alreadyTried = new HashSet();
    
    float winSize = windowMap.getWinSize(instr);
    float timeMod = 1;
    //Random ranNum = new Random(System.currentTimeMillis());
    //OwnRanNumGen ranNum = new OwnRanNumGen((int)System.currentTimeMillis());
    if(getInstrRunLength(instr, chipDef) < 1)
      timeMod = _schedCycleFraction * _schedCycleFractionMod;
    //System.out.println("instr " + instr);
    //System.out.println("timeMod " + timeMod);
    //winSize = (int)(winSize + 0.999); 
    float cycle = 0;
    if(winSize == 0) {
      cycle = windowMap.getMin(instr);
	  /*System.out.println("fd instr " + instr);
	  System.out.println("fd cycle " + cycle);
	  System.out.println("fd windowMap.getMin(instr) " + windowMap.getMin(instr));
	  System.out.println("fd windowMap.getMax(instr) " + windowMap.getMax(instr));*/
      if(chipDef.analyzeHardwareUse(_node, instr, (int)cycle))
        smallestLoc = cycle;
      else
        return (float)-1.0;
    }
    else {
      int roundingFactor = (int)_schedCycleFraction;
      if(getInstrRunLength(instr, chipDef) > 1)
        roundingFactor = 1;
      float stoppingPoint = winSize * roundingFactor;
      while(alreadyTried.size() <= stoppingPoint) {
	cycle = 0;
	float correctedCycle = 0;
	do {
	
          //cycle = (float)Math.random() * winSize + windowMap.getMin(instr);
          //System.out.println("ranNum.ran2() " + ranNum.ran2());
	  //int tries=0;
	  //boolean hardwareConflict = false;
	  //boolean stop = false;
	  do {
	    cycle = ranNum.ran2() * winSize + windowMap.getMin(instr);
          //cycle = ranNum.nextFloat() * winSize + windowMap.getMin(instr);
            cycle = ((float)Math.round(cycle * roundingFactor))/roundingFactor;
	    //if(tries>=winSize * roundingFactor)
	      //return (float)-1.0;
	    //tries++;
	  /*System.out.println("fd instr " + instr);
	  System.out.println("fd cycle " + cycle);
	  System.out.println("fd windowMap.getMin(instr) " + windowMap.getMin(instr));
	  System.out.println("fd windowMap.getMax(instr) " + windowMap.getMax(instr));*/
	  //System.out.println("fd cycle " + cycle);
	  //System.out.println("alreadyTried.contains(new Float((double)cycle)) " + alreadyTried.contains(new Float((double)cycle)));
	    //hardwareConflict = !chipDef.analyzeHardwareUse(_node, instr, (int)cycle);
	    /*if(hardwareConflict) {
	      if(alreadyTried.size() > stoppingPoint)
	        return (float)-1.0;
	      else
	        stop = false;
	    }
	    else if(alreadyTried.size() <= stoppingPoint)
	      stop = false;
	    else
	      stop = true;*/
	  //System.out.println("hardwareConflict " + hardwareConflict);
	    /*if(hardwareConflict) {
	      //stoppingPoint--;
	      alreadyTried.add(new Float((double)cycle));
	    }*/
	  //System.out.println("stoppingPoint) " + stoppingPoint);
	  //System.out.println("alreadyTried.size() " + alreadyTried.size());
	  //System.out.println("alreadyTried " + alreadyTried);
	  //}while(!stop);
	  } while((alreadyTried.contains(new Float((double)cycle)))/*&&
	           alreadyTried.size() <= stoppingPoint/*&&
	          (hardwareConflict)*/);
	  //if(alreadyTried.size() > stoppingPoint) return smallestLoc;
	  alreadyTried.add(new Float((double)cycle));
	  //System.out.println("instr " + instr);
	  //System.out.println("windowMap.getMin(instr) " + windowMap.getMin(instr));
	  //System.out.println("windowMap.getMax(instr) " + windowMap.getMax(instr));
	  //System.out.println("cycle " + cycle);
	  //we don't want operations less than a clock tick to start so that 
	  //spill over the cycle boundary
	  //if(((int)cycle) != ((int)(cycle + getInstrRunLength(instr, chipDef))))
	  //   stoppingPoint--;
          CorrectStartTimes adjustTimes = new CorrectStartTimes(instr, chipDef,
					                        !GlobalOptions.packInstructions);
	  correctedCycle = adjustTimes.getCorrectedStartTime(cycle);
	  //if(correctedCycle != cycle) {
	  //  alreadyTried.add(new Float((double)cycle));
	/*System.err.println("instr " + instr);
        System.err.println("windowMap.getMin(instr) " + windowMap.getMin(instr));
        System.err.println("windowMap.getMax(instr) " + windowMap.getMax(instr));
        System.err.println("b4 correctedCycle " + correctedCycle);
        System.err.println("cycle " + cycle);*/
	  //}
	  //System.err.println("alreadyTried.size() " + alreadyTried.size());
	  //System.err.println("stoppingPoint " + stoppingPoint);
        //System.err.println("in 2nd loop");

	}while((//(alreadyTried.contains(new Float((double)cycle)))||
               //(cycle < winSize + windowMap.getMin(instr) ) &&
	       /*(correctedCycle > windowMap.getMax(instr)) ||
	       (correctedCycle < windowMap.getMin(instr)) ||
	       */(!setWindowSuccessors(instrlist, instr, useLists, defLists, (FDWindows)windowMap.copy(), 
        		             correctedCycle + getInstrRunLength(instr, chipDef), chipDef)) ||
	       (!setWindowPredecessors(instrlist, instr, useLists, defLists, windowMap.copy(), 
        		               correctedCycle, chipDef)))&&
	       (alreadyTried.size() <= stoppingPoint));//&&
	        	 //+ getInstrRunLength(instr, chipDef))&&
	        //(((int)cycle) != ((int)(cycle + getInstrRunLength(instr, chipDef)))));
	//alreadyTried.add(new Float((double)cycle));
	cycle = correctedCycle;
        //System.out.println("exited 2nd while loop");

        //CorrectStartTimes adjustTimes = new CorrectStartTimes(instr, chipDef,
	//				                      GlobalOptions.doNotPackInstrucs);
	int forceTableIndex = (int)(cycle * timeMod);
	float forceAtTime = ((Float)force.get(forceTableIndex)).floatValue();

	if((forceAtTime < smallestForce)&&
    	   (chipDef.analyzeHardwareUse(_node, instr, (int)cycle))) {

          smallestForce = forceAtTime;
          smallestLoc = cycle;
	}
      }
    }
    return smallestLoc;
  }
  
  public void seed(int seed) { ranNum.seed(seed);}

  private class OwnRanNumGen { 
    //this is copied and adapted from Numerical Recipes in C, Cambridge
    //University Press, 
    private int _seed;
    /*private long _lword;
    private long _irword;
    private long c1[] = {0xbaa96887, 0x1e17d32c, 0x03bcdc3c,
        	   0x0f33d1b2};
    private long c2[] = {0x4b0f3b58, 0xe874f0c3, 0x6955c5a6,
                   0x55a7ca46};
    private long idums = 0;
    private long jflone = 0x3f800000;
    private long jflmsk = 0x007fffff;
    */
    public OwnRanNumGen() {/*_seed = -1;*/ ran2();}
    public OwnRanNumGen(int seed) {/*_seed = -1; ran2();*/ _seed = seed;}
    
    public void seed(int seed) {_seed = seed;}
    
    /*public void psdes() {
    
      long ia, ib, iswap, itmph=0, itmpl=0;
      for (int i=0;i<4;i++) {
        iswap = _irword;
	ia= iswap ^ c1[i];
	itmpl = ia & 0xffff;
	itmph = ia >> 16;
	ib = itmpl*itmpl + ~(itmph*itmph);
	ia = (ib >> 16) | ((ib & 0xffff) << 16);
	_irword = (_lword) ^ (((ia)^ c2[i])+itmpl*itmph);
	_lword = iswap;
      }
    }
    
    public float ran4() {
    
      long irword, itemp, lword;
      _seed = -1;
      if(_seed < 0) {
        idums = - _seed;
	_seed = 1;
      }
      irword = _seed;
      lword = idums;
      _lword = lword;
      _irword = irword;
      psdes();
      
      itemp = jflone | (jflmsk & _irword);
      ++_seed;
      System.out.println("itemp " + (float)itemp);
      return (float)itemp - (float)1.0;
    }*/

  
    private double standardDev = 0.75;
    private int idum2=123456789;
    private int iy=0;
    private int IM1 = 2147483563;
    private int IM2 = 2147483399;
    private float AM = (float)((float)1.0/(float)IM1);
    private int IMM1 = IM1 - 1;
    private int IA1 = 40014;
    private int IA2 = 40692;
    private int IQ1 = 53668;
    private int IQ2 = 52774;
    private int IR1 = 12211;
    private int IR2 = 3791;
    private int NTAB = 32;
    private float NDIV = (float)(1+(float)IMM1/(float)NTAB);
    private float EPS = (float)1.2e-7;
    private float RNMX = (float)(1.0-EPS);
    private int[] iv = new int[(int)NTAB];
    public float ran2() {

      int j;
      int k;
      float temp;
      if(_seed <= 0) {

	if(-_seed < 1) _seed=1;
	else _seed = -_seed;
	idum2=_seed;
	for(j=(int)(NTAB+7);j>=0;j--) {

          k=_seed/IQ1;
	  _seed=IA1*(_seed-k*IQ1)-k*IR1;
	  if(_seed<0) _seed += IM1;
	  if(j<NTAB) iv[j] = _seed;

	}
	iy=iv[0];

      }
      k=_seed/IQ1;
      _seed=IA1*(_seed-k*IQ1)-k*IR1;
      if(_seed<0) _seed += IM1;
      k=idum2/IQ2;
      idum2=IA2*(idum2-k*IQ2)-k*IR2;
      //System.out.println("start idum2 " + idum2);
      if(idum2<0) idum2+=IM2;
      //System.out.println("idum2 " + idum2);
      j=(int)(iy/NDIV);
      if(j==32) j--;
      /*System.out.println("iy " + iy);
      System.out.println("iy/NDIV " + iy/NDIV);
      System.out.println("(int)(iy/NDIV) " + (int)(iy/NDIV));
      System.out.println("(float)(iy/NDIV) " + (float)(iy/NDIV));
      System.out.println("(int)((float)(iy)/NDIV) " + (int)((float)(iy)/NDIV));
      System.out.println("j " + j);
      System.out.println("iv " + iv);
      System.out.println("iv[j] " + iv[j]);
      System.out.println("idum2 " + idum2);*/
      iy=iv[j]-idum2;
      //System.out.println("end iy " + iy);
      iv[j]=_seed;
      if(iy<1) iy += IMM1;
      if((temp=AM*iy)>RNMX) return RNMX;
      else return temp;

    }
    
    public float gaussRan() {
    
      double ranTmp = Math.log((double)ran2()*standardDev*Math.sqrt(2.0*Math.PI));
      return (float)Math.sqrt(-2.0*standardDev*standardDev*ranTmp);
    
    }
    
  }

}

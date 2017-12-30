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
import fp.util.*;
import fp.passes.*;
import fp.*;

/** This class contains methods for allocating arrays to memory and for deciding 
 *  if there is space to implement all desired logic components (and deciding 
 *  which to share if there is not).
 * 
 * @author Kris Peterson
 */
public class AnalyzeHardareConstraints
{
  
  HashMap _opUseCnts;
  /** la la la
   */
  public AnalyzeHardareConstraints() {
  }
   
  /** The main method from this class.  It compares the requirements of the 
   *  design with the available hardware on the chip and allocates arrays to 
   *  memory and decides whether and which logic modules to share in the 
   *  hardware implementation.
   * 
   * @param bGraph control flow graph of design
   * @param chipInfo chip information
   * @return 0 if successful, -1 if an error occurred
   */
  public float analyzeHardware(BlockGraph bGraph, ChipDef chipInfo, 
                               int scheduleChoice, boolean ignoreDataDep, 
			       boolean conserveArea, OperationSelection opSel) {
    
    init(bGraph, chipInfo, opSel);
    int i=0;
    long startTime = System.currentTimeMillis();
    long time = startTime;
    boolean scheduledOK = true;
    do{
      chipInfo.resetPorts();
      chipInfo.makeDummyBoard();
      schedule(bGraph, chipInfo, opSel, conserveArea);
      chipInfo.unDummyBoard(); //here?
      chipInfo.resetPorts();
      chipInfo.deallocateAll();
      if(!chipInfo.arrayAllocate(bGraph)) {
	chipInfo.displayMemsArrs();
	throw new HardwareException("error, memory full!!!");
      }
      if(!GlobalOptions.onePreAlloc)
        chipInfo.saveBestArrayAlloc(bGraph);
      System.out.println("================================================================");
      System.out.println("run " + i);
      chipInfo.displayMemsArrs();
      System.out.println("================================================================");
      selectOperators(bGraph, opSel);
      chipInfo.printRemainingArrays();
      //if(i++<2) chipInfo.resetArrayToBlockMap();
      i++;
      time = System.currentTimeMillis();
    }while((time < startTime + GlobalOptions.painThreshHold)&&
           (!GlobalOptions.onePreAlloc));
    if(!GlobalOptions.onePreAlloc)
      chipInfo.changeToBestArrayAlloc();
    selectOperators(bGraph, opSel);
    
    System.out.println("================================================================");
    System.out.println("final ");
    chipInfo.displayMemsArrs();
    System.out.println("================================================================");
    
    //sort the list of memories, based on the sum of the number of read and 
    //write ports they have. 
    /*======================================================
    sort(memBlockList);
    remember this!!!!!
    ======================================================
    */
    
    if(!(scheduledOK)) {
      throw new HardwareException("error, memory full!!!");
    }
    return 0;
  }
  
  public void schedule(BlockGraph bGraph, ChipDef chipInfo, 
                       OperationSelection opSel, boolean conserveArea) {
    for (Iterator vIt = new HashSet(bGraph.getAllNodes()).iterator(); 
    	 vIt.hasNext();) {
      BlockNode bNode = (BlockNode) vIt.next();
      if(bNode.getInstructions().size()==0) continue;

      MasterScheduler prelimSched = new MasterScheduler(bNode, opSel, 
    							chipInfo);
      prelimSched.schedule(bGraph);
      ArrayList scheduleList = new ArrayList(prelimSched.getInstructions());

      //save load and store times
      chipInfo.loadSchedule(bNode);

      //analyze schedule to see how much logic space is really needed:
      if(conserveArea) 
        _opUseCnts = getOpUseCnts(scheduleList);
      //unschedule graph:
      prelimSched.unSchedule(bGraph);
    } 
    //System.exit(1);
  }
  
  public void init(BlockGraph bGraph, ChipDef chipInfo, 
                   OperationSelection opSel) {
    chipInfo.loadIndexes(bGraph);
    if(GlobalOptions.slowestMem) {
      for (Iterator vIt = new HashSet(bGraph.getAllNodes()).iterator(); 
           vIt.hasNext();) {
	BlockNode bNode = (BlockNode) vIt.next();
	if(bNode.getInstructions().size()==0) continue;
	chipInfo.setInstructions(bNode.getInstructions());
      }
    }
    else {
      chipInfo.loadDesign(bGraph);
      if(!chipInfo.arrayAllocate(bGraph)) {
	chipInfo.displayMemsArrs();
	throw new HardwareException("error, memory full!!!");
      }
    }
    chipInfo.displayMemsArrs();
    selectOperators(bGraph, opSel);
  }
  
  public HashMap getOpUseCnts(ArrayList scheduleList) {
    HashMap blockOpCnts = new HashMap();
    HashMap opUseCnts = new HashMap();
    for (Iterator it = scheduleList.iterator(); 
    	 it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();
      int execCyle = instr.getExecClkCnt();
      Operator op = instr.operator();
      if(!blockOpCnts.containsKey(op))
        blockOpCnts.put(op, new HashMap());
      if(!((HashMap)blockOpCnts.get(op)).containsKey(new Integer(execCyle)))
        ((HashMap)blockOpCnts.get(op)).put(new Integer(execCyle), 
	                                   new Integer(0));
      int opCnt = ((Integer)((HashMap)blockOpCnts.get(op)).get(new Integer(execCyle))).intValue();
      ((HashMap)blockOpCnts.get(op)).put(new Integer(execCyle), 
                                         new Integer(++opCnt));
    }

    for (Iterator it = blockOpCnts.keySet().iterator(); 
    	 it.hasNext(); ) {
      Operator op = (Operator)it.next();
      HashMap times = (HashMap)blockOpCnts.get(op);
      for (Iterator it2 = times.values().iterator(); 
    	 it2.hasNext(); ) {
    	Integer cnt = (Integer)it2.next();
    	int cntTmp = cnt.intValue();
    	if(!opUseCnts.containsKey(op))
    	  opUseCnts.put(op, new Integer(0));
    	int savedCnt = ((Integer)opUseCnts.get(op)).intValue();
    	savedCnt = Math.max(savedCnt, cntTmp);
    	opUseCnts.put(op, new Integer(savedCnt));
      }
    }
    return opUseCnts;
  }

  public float calcLogicSpaceReq(BlockGraph bGraph, ChipDef chipInfo, 
			         boolean conserveArea) {
    
    //determine logic requirements for board:
    HashMap opCnts = new HashMap();
    ArrayList opListAL = new ArrayList();
    int sliceReqdI = 0;
     
    if(conserveArea) {
      HashMap opUseCnts = _opUseCnts;
      opCnts.putAll(opUseCnts);
      opListAL.addAll(opUseCnts.keySet());
      for (Iterator it = opUseCnts.keySet().iterator(); 
           it.hasNext(); ) {
        Operator op = (Operator)it.next();
        Integer cnt = (Integer)opUseCnts.get(op);
	sliceReqdI += op.getSlices() * cnt.intValue();
      }      
    }
    else {
      ArrayList instlist = new ArrayList();
      //get a list of all instructions from all nodes in the block graph
      for (Iterator vIt = bGraph.getAllNodes().iterator(); 
            vIt.hasNext();) {
	BlockNode bNode = (BlockNode) vIt.next();
       //System.out.println("bNode " + bNode.getLabel());
       //System.out.println("bNode.getInstructions() " + bNode.getInstructions());
	instlist.addAll(bNode.getInstructions());
      }
       //System.out.println("instlist " + instlist);
      //count up the number of each type of operator used
      //and make a list of al operators used
      for (Iterator it = ((ArrayList)instlist).iterator(); 
	it.hasNext(); ) {
	Instruction instr = (Instruction)it.next();
       //System.out.println("instr " + instr);
       Operator op_Operator = instr.operator();
       //System.out.println("op_Operator " + op_Operator);
	if(opCnts.containsKey(instr.operator())) {
          int opCnt = ((Integer)(opCnts.get(op_Operator))).intValue();
          opCnt++;
          opCnts.put(op_Operator, new Integer(opCnt));
	}
	else
          opCnts.put(op_Operator, new Integer(1));
	if(!(opListAL.contains(op_Operator)))
          opListAL.add(op_Operator);

	//add up the number of slices required for all operators
	sliceReqdI += op_Operator.getSlices();
      }
    }
    //save this info to the chipinfo object:
    chipInfo.setOpCntsNeeded(opCnts);
    chipInfo.setOpList(opListAL);
    //sort the list first in terms of operation latencies and then in terms of their required slice count.
    //if more space is_HashMap needed to fit the logic, multiple uses of an operator can be forced to reuse existing hardware.
    //when hardware is_HashMap reused, it goes through the list starting at those with the greatest latencies or slice
    //count requirements.  
    //note:  this information needs to be saved somewhere so that the final implementation knows when to reuse hardware!!!
    
    //sort the list of operators based on:
    //1) their latencies
    //2) their sizes
    sort(opListAL);
    int i = 0;
    System.out.println(" a slice requirement of " + sliceReqdI);
    System.out.println(" a slice availability of " + chipInfo.getSliceCnt() * chipInfo.getPercentUsage());
    //System.out.println("i " + i + " opListAL.size() " + opListAL.size());
    //while the number of required slices is greater than the number available
    //and while there are still operators left to share
    while((sliceReqdI > chipInfo.getSliceCnt() * chipInfo.getPercentUsage()) && 
                                                       (i < opListAL.size())) {
      Operator optmp = (Operator)opListAL.get(i);
      //System.out.println("operator: " + optmp.toString() + " is_HashMap reduced from " + ((Integer)(opCnts.get(optmp))).intValue() +
      //" instances to only 1, resulting in a change in the slice requirements from " + sliceReqdI);
       
       
      int oldCnt = ((Integer)(opCnts.get(optmp))).intValue();
       
      //skip to the next operator, when there is only one of this guy left
      if(oldCnt > 1) {
        //if the difference between desired and required is greater than 1
	//reduce the number available by half
	int newCntI = (int)(((float)oldCnt)/2 + 0.5); //round off
        //else if the difference between what is required and what is necessary 
	//is only 1, just subtract 1
	if(sliceReqdI - (chipInfo.getSliceCnt() * chipInfo.getPercentUsage()) 
	                                                  <= optmp.getSlices())
          newCntI = oldCnt - 1;
	  
         System.out.println("operator: " + optmp.toString() + " is_HashMap reduced from " + ((Integer)(opCnts.get(optmp))).intValue() +
         " instances to only " + newCntI + ", resulting in a change in the slice requirements from " + sliceReqdI);
        //sliceReqdI += optmp.getSlices() * (1 - ((Integer)(opCnts.get(optmp))).intValue());
        
	//recalculate the required slice count based on adjusting the count of 
	//this operator
	sliceReqdI += optmp.getSlices() * (newCntI - oldCnt);
         
        //save the new count of operators
	opCnts.put(optmp, new Integer(newCntI));
        System.out.println("to " + sliceReqdI);
      }
      else
        i++;
    }
    //save to the chipinfo object
    chipInfo.setOpCntsAvailable(opCnts);
     
    if((i >= opListAL.size())&&(opListAL.size()>0)) {
      throw new HardwareException("error, not enough room for the circuit!!!");
      //System.err.println("error, not enough room for the circuit!!!");
      //System.exit(-1);
    }
    //System.out.println("II due to hardware requirements: " + IIres);
     
    //end hardware check
    return 0;
     
  }
   
  public void selectOperators(BlockGraph graph, OperationSelection opSel) {
    for (Iterator vIt = graph.getAllNodes().iterator(); 
         vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      if(node.getInstructions().size()==0) continue;
      opSel.optimize(node);
    }
  } 
  
  /** sort operands based on their widths, operators based on their latencies or 
   *  size, memory blocks based on the number of data ports, and ArrayLists 
   *  based on their size.
   * 
   * @param o_list list to be sorted
   */
  private void sort(ArrayList o_list) {
    class DoubleCompare implements Comparator {      
      /** fancy compare for sort
       * 
       * @param o1 1st item
       * @param o2 2nd item
       * @return bigger, smaller, same
       */
      public int compare(Object o1, Object o2) {        
        if (o1 instanceof Operand             
         && o2 instanceof Operand) {          
          Operand p1 = (Operand)o1;          
          Operand p2 = (Operand)o2;          
       //System.out.println("p1 " + p1);
      //System.out.println("p2 " + p2);
         if (((Type)(p1.getType())).getWidth() > 
	       ((Type)(p2.getType())).getWidth()) {            
            return 1;          
          } else if (((Type)(p1.getType())).getWidth() < 
	                ((Type)(p2.getType())).getWidth()) {            
            return -1;         
          } else {            
            return 0;          
          }        
        } else if (o1 instanceof Operator             
                && o2 instanceof Operator) {          
          Operator p1 = (Operator)o1;          
          Operator p2 = (Operator)o2;          
          if( p1.getRunLength() < p2.getRunLength() ) {            
            return 1;          
          } else if( p1.getRunLength() > p2.getRunLength() ){            
            return -1;         
          } else if( p1.getSlices() < p2.getSlices() ){            
            return 1;          
          } else if( p1.getSlices() > p2.getSlices() ){            
            return -1;         
          } else {            
            return 0;          
          }        
        } else if (o1 instanceof ArrayList             
                && o2 instanceof ArrayList) {          
          ArrayList p1 = (ArrayList)o1;          
          ArrayList p2 = (ArrayList)o2;          
          if( p1.size() < p2.size()) {            
            return 1;          
          } else if( p1.size() > p2.size() ){            
            return -1;         
          } else {            
            return 0;          
          }        
        } else {          
          throw new ClassCastException("Not Instruction");        
        }      
      }    
    }        
    Collections.sort(o_list, new DoubleCompare());  
  }
  
  
}

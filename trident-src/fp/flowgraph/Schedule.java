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
import fp.GlobalOptions;


/** 
 * The Schedule class contains methods for perform all scheduling
 * except modulo scheduling of loop bodies of the list of instructions
 * in a hyperblock.  There are implementations of Force-Directed
 * scheduling, ASAP, and ALAP.  After determining an execution time
 * for each instruction, that time is saved on the instruction itself.
 * The compiler uses predicated instructions, which will or will not
 * be executed depending on the value of the predicate.  Due to the
 * extra processing power available from executing the input algorithm
 * as a circuit on an FPGA, the compiler can ignore the predicates and
 * execute all instructions regardless of the value of the predicate,
 * unless the instruction is writing to a primal.  Our compiler has
 * this feature, and will by default ignore all predicates when
 * scheduling except for on those instructions that write to a primal;
 * however, this option can be turned off, so that the schedulers will
 * not ignore predicates.  Also, the schedulers must consider hardware
 * constraints such as the number of any given operator available
 * (e.g. the number of multiply operators available for all the
 * desired multiply instructions) and the memory communication
 * bandwidth.  The schedulers must ensure that instructions are
 * scheduled such that there are no resource conflicts.
 * 
 * @author Kris Peterson
 */
public class Schedule {
  
  //obsolete, needs to be removed:
  private int[] _lookUpTable;
  private ArrayList _instList;
  
  //save windows for force directed scheduling (though these global variables
  //are actually only used for sorting--see sort function at end of file):
  public HashMap _windowMinGlobal;
  public HashMap _windowMaxGlobal;
  public BlockNode _node;  
  
    //this is only a temporary solution to save this here.  Later it will 
    //come from the options class
  //static public float cycleLength = (float)1.0;
  
  public Schedule(BlockNode node) {
    _instList = new ArrayList();
    _lookUpTable = new int[500];
    _node = node;
    
  }
           
  public float initAPSchedulers(ArrayList instrlist, DependenceFlowGraph dfg,
                                HashMap inst2NodeMap, HashMap deflistsHM,
			        HashMap useLists, ChipDef chipInfo, 
			        float rankLimit, boolean ignorePredsNicht) {
  
    //chipInfo.resetBusCntsAtAllTimes();
    chipInfo.saveNode(_node);
    
    for (Iterator it = ((ArrayList)instrlist).iterator(); 
  	 it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();
      
      // calculate max schedule time :
      // leave enough room for ceil(runLength)
      rankLimit +=  Math.ceil(instr.getRunLength());

      if(dfg != null)
  	for (Iterator it1 = ((Set)(dfg.getAllNodes())).iterator(); 
        	     it1.hasNext();) {
  	DependenceFlowNode nodeTmp = (DependenceFlowNode)it1.next();
  	if(nodeTmp.getInstruction() == null)
  	  continue;
  	if(nodeTmp.getInstruction().toString().compareTo(instr.toString())==0)
  	  inst2NodeMap.put(instr, nodeTmp);
      }
      if( (!(deflistsHM.containsKey(instr)))&&
          (!(useLists.containsKey(instr)))) {
        deflistsHM.put(instr, new ArrayList());
        useLists.put(instr, new ArrayList());
        getDefandUseLists(instr,(ArrayList)deflistsHM.get(instr),
        		  (ArrayList)useLists.get(instr), ignorePredsNicht);
        chipInfo.initOpUseLists(instr.operator());
      }    
    }
  
    return rankLimit;
  
  }
  
  /** for each node in the control flow graph, for each instruction in the 
   *  block, set the execution time back to -1 (the flag value for an 
   *  unscheduled instruction), and delete sharing information.
   * 
   * @param graph 
   */
  public void unSchedule() {
    for (Iterator it = _node.getInstructions().iterator(); 
        it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();
      instr.unSchedule();
    }
    
    /*ArrayList nodesToDelete = new ArrayList();
    
    graph.setAveOpsPCycle(0);
    graph.setMaxOpsPCycle(0);
    graph.setMinOpsPCycle(0);
    graph.setCyclesPerBlock(0);
    graph.setOpsPerBlock(0);
    graph.setTotOps(0);
    graph.setCycleCount(0);
    graph.setOperatorCounts(new HashMap());
    for (Iterator vIt = graph.getAllNodes().iterator(); 
              vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      ArrayList list = node.getInstructions();
       
      node.setAveOpsPCycle(0);
      node.setMaxOpsPCycle(0);
      node.setMinOpsPCycle(0);
      node.setTotOps(0);
      node.setCycleCount(0);
      node.setOperatorCounts(new HashMap());
      
      if(node.getName().indexOf("prolog_") >= 0) {
	
	ArrayList out_edges = new ArrayList();
	out_edges.addAll(node.getOutEdges());
	BlockEdge outEdge = (BlockEdge)out_edges.get(0);
	BlockNode bNode = (BlockNode)outEdge.getSink();
	
	Set in_edges = new HashSet();
	in_edges.addAll(node.getInEdges());
	for (Iterator it = in_edges.iterator(); it.hasNext();) {
          BlockEdge inEdge = (BlockEdge)it.next();
          inEdge.setSink(bNode);
	}
	nodesToDelete.add(node);
      }
      if(node.getName().indexOf("epilog_") >= 0) {
	
	ArrayList in_edges = new ArrayList();
	in_edges.addAll(node.getInEdges());
	BlockEdge inEdge = (BlockEdge)in_edges.get(0);
	BlockNode bNode = (BlockNode)inEdge.getSource();
        BooleanEquation edgePredicate = inEdge.getPredicate();
	
	Set out_edges = new HashSet();
	out_edges.addAll(node.getOutEdges());
	for (Iterator it = out_edges.iterator(); it.hasNext();) {
          BlockEdge outEdge = (BlockEdge)it.next();
          outEdge.setSource(bNode);
          outEdge.setPredicate(edgePredicate);
	}
	nodesToDelete.add(node);
      }
      
      for (Iterator it = ((ArrayList)list).iterator(); 
        it.hasNext(); ) {
        Instruction instr = (Instruction)it.next();
        instr.setExecTime(-1);
        instr.setExecClkCnt(-1);
        instr.setIsShared(false);
        instr.setShareSet(null);
      }
    }
    for (Iterator vIt = nodesToDelete.iterator(); 
              vIt.hasNext();) {
      BlockNode delNode = (BlockNode) vIt.next();
      graph.removeNode(delNode);
    }*/
  }
  //method: loadIntoDataBase
  //this method loads the data from the array into the _instList database.  It uses the execution clock tick, saved on each instruction
  //in the list passed in, to perform this.
  /** obsolete
   * 
   * @param instrlist list of instructions from the hyperblock
   */
  public void loadIntoDataBase(ArrayList instrlist) {
    int lastClkVlu = 0, n_int=0;
    _instList.add(0, new ClockInstrucList(0));
    for (Iterator its = ((ArrayList)instrlist).iterator(); its.hasNext(); ) {
      Instruction inst = (Instruction)its.next();
      int clktmp_int = inst.getExecClkCnt();
      if(clktmp_int < 0) {
	clktmp_int = 0;
	inst.setExecClkCnt(0);
      }
      float timetmp = inst.getExecTime();
      if(timetmp < 0) {
	inst.setExecTime(0);
      }
      if(clktmp_int != lastClkVlu) {
	lastClkVlu = clktmp_int;
	n_int++;
	_lookUpTable[lastClkVlu]=n_int;
	_instList.add(n_int, new ClockInstrucList(lastClkVlu));
      }
	      
	      
      ClockInstrucList InstTmp = (ClockInstrucList)_instList.get(n_int);
      ArrayList InstListtmp = InstTmp.getInstList();
      InstListtmp.add(inst);
      ((ClockInstrucList)_instList.get(n_int)).setClk(lastClkVlu);
	      
      _instList.trimToSize();
    }
  }
  
  
  //method:  printOutScheduledInsts
  //this method was written for debugging purposes, and it prints out the contents of the _instList database.
  /** obsolete debug helper function
   */
  public void printOutScheduledInsts() {
    System.out.println("********************************************************************************************************************");
    System.out.println("********************************************************************************************************************");
    System.out.println("********************************************************************************************************************");
    System.out.println("Scheduled List Printout:");
    System.out.println("********************************************************************************************************************");
    for (int i= 0; i<_instList.size();i++) {
      int InstListSettmp = _lookUpTable[i];
      int clk = ((ClockInstrucList)(_instList.get(InstListSettmp))).getClk();
      ArrayList ListOfInstr = (ArrayList)((ClockInstrucList)(_instList.get(InstListSettmp))).getInstList();
      if(ListOfInstr.size()>0) {
	System.out.println("Clock: " + clk + " instructions:");
	for (Iterator its = ((ArrayList)ListOfInstr.clone()).iterator(); 
	  its.hasNext(); ) {
	  Instruction inst = (Instruction)its.next();
	  
	  String InstAsString = inst.toString() + " | " + inst.getPredicate().toString();
	  
	  if(InstAsString.length()>0) {
	    System.out.println(InstAsString);
	    System.out.println("===================================");
	  }
	}
      }
       
    }
    
    
    System.out.println("********************************************************************************************************************");
    System.out.println("End of Scheduled List Printout:");
    System.out.println("********************************************************************************************************************");
    System.out.println("********************************************************************************************************************");
  }
  
  
  //method: getDefandUseLists
  //this method determines the list of Defs and Uses for each instruction and saves them to the ArrayLists: defList and useList
  /** This method finds all the defs and uses for a given instruction.
   * 
   * @param inst an instruction for which a list of defs and uses should be 
   *	 found
   * @param defList a list of operands defined by this instruction
   * @param useList a list of operands used by this instruction
   * @param ignorePredsNicht tells the method to ignore the operands 
   *	  used in the predicate
   */
  public static void getDefandUseLists(Instruction inst, ArrayList defList, 
				       ArrayList useList, boolean ignorePredsNicht) {
    Operand op_Operand;
    boolean outIsPrimal = false;
    if(AStore.conforms(inst)){
      outIsPrimal = true;
      defList.add(AStore.getPrimalDestination(inst));
      useList.add(AStore.getAddrDestination(inst));
      useList.add(AStore.getValue(inst));
    }
    else if(Getelementptr.conforms(inst)){
      //outIsPrimal = true;
      defList.add(Getelementptr.getResult(inst));
      int i = 0;
      while(Getelementptr.hasVal(inst, i)) {
        useList.add(Getelementptr.getValOperand(inst, i));
	i++;
      }
    }
    else
    {
      int numDefs = inst.getNumberOfDefs();
      for(int i = 0; i < numDefs; i++) {
	op_Operand = inst.getOperand(i);
	if(op_Operand != null) {
	  defList.add(op_Operand);
	  if(op_Operand.isPrimal())
	    outIsPrimal = true;
	}
      }

      int total = inst.getNumberOfOperands();
      for(int i = numDefs; i < total; i++) {
	op_Operand = inst.getOperand(i);
	if(op_Operand != null)
	  useList.add(op_Operand);
      }
    }
    
    //ignore predicates when scheduling unless, ignorePredsNicht is true or 
    //one of the defs is a primal
    if(outIsPrimal || ignorePredsNicht) {
      BooleanEquation predTmp = inst.getPredicate();
      if(predTmp != null) {
	LinkedList BoolsTmp = predTmp.listBooleanOperands();
	for (Iterator itin = ((LinkedList)BoolsTmp.clone()).iterator(); 
	     itin.hasNext(); ) {
	  op_Operand = (Operand)itin.next();
	  useList.add(op_Operand);
	}
      }
    }
  }
   
  //method: getDataBase
  //return the database, in case this might be useful someday...
  /** obsolete
   * 
   * @return NA
   */
  public ArrayList getDataBase() {return _instList;}
   
  /** checks if at least one input to an instruction is primal
   * 
   * @param uselist list of all operands used by an instruction
   * @return true if at least one input is primal; false if all inputs are  
   *	  nonprimal
   */
  public boolean insArePrimalsOrOtherBlckDefnd(ArrayList uselist) {
    boolean isPrimal = false;
    for (Iterator its = ((ArrayList)uselist.clone()).iterator(); 
      its.hasNext(); ) {
      Operand op_Operand = (Operand)its.next();
      if(op_Operand != null) {
	if(op_Operand.isPrimal())
	  isPrimal = true;
	
      }
    }
    return isPrimal;
  }
  
  /** checks that all defs are not primal
   * 
   * @param deflist list of defs for an instruction
   * @return true if no output is primal; false if at least one output is   
   *	  primal
   */
  public boolean areOutsNotPrimals(ArrayList deflist) {
    boolean isNotPrimal = true;
    for (Iterator its = ((ArrayList)deflist.clone()).iterator(); 
      its.hasNext(); ) {
      Operand op_Operand = (Operand)its.next();
      if(op_Operand != null)
	if(op_Operand.isPrimal())
	  isNotPrimal = false;
    }
    return isNotPrimal;
  }
  
  /** checks if all inputs to an instruction are constants
   * 
   * @param uselist list of inputs for an instruction
   * @return true if all inputs are constant; false if at least one input is
   *	  not constant
   */
  public boolean inputsAllConsts(ArrayList uselist) {
    boolean isConst = true;
    for (Iterator its = ((ArrayList)uselist.clone()).iterator(); 
      its.hasNext(); ) {
      Operand op_Operand = (Operand)its.next();
      if(op_Operand != null)
	if(!(op_Operand.isConstant())) 
	  isConst = false;
    }
    return isConst;
  }
  
  /** checks if all inputs to an instruction are primal or constant or a type 
   *  or a TRUE or FALSE
   * 
   * @param uselist list of all operands used by an instruction
   * @return true if all inputs are is one of the above listed operands;    
   *	  false if at least one input is not
   */
  public boolean inputsAllPrimalsOrConsts(ArrayList uselist) {
    boolean isAllPrimal = true;
    for (Iterator its = ((ArrayList)uselist.clone()).iterator(); 
      its.hasNext(); ) {
      Operand op_Operand = (Operand)its.next();
      if(op_Operand != null)
	if((!(op_Operand.isPrimal()))&&(!(op_Operand.isConstant()))&&
           (!(op_Operand.isType()))&&(op_Operand != BooleanOperand.TRUE)&&
             (op_Operand != BooleanOperand.FALSE))
	  isAllPrimal = false;
    }
    return isAllPrimal;
  }
  
  /** checks if all inputs to an instruction are primal
   * 
   * @param uselist list of all operands used by an instruction
   * @return true if all inputs are primal; false if at least input is      
   *	  nonprimal
   */
  public boolean inputsAllPrimals(ArrayList uselist) {
    boolean isAllPrimal = true;
    for (Iterator its = ((ArrayList)uselist.clone()).iterator(); 
      its.hasNext(); ) {
      Operand op_Operand = (Operand)its.next();
      if(op_Operand != null)
	if(!(op_Operand.isPrimal()))
	  isAllPrimal = false;
    }
    return isAllPrimal;
  }
  
  /** Given the uselist of a possible child instruction and the deflist for a 
   *  possible parent instruction, check if the parent instruction could be 
   *  the predecessor of the child.
   * 
   * @param uselist list of all operands used by the possible child 
   *	 instruction
   * @param deflist list of defs for the possible parent instruction
   * @return true if the one instruction is a predecessor of the another
   */
  public boolean isInstrPred(ArrayList uselist, ArrayList deflist) {
    boolean isPred = false;
    for (Iterator its = ((ArrayList)deflist.clone()).iterator(); 
      its.hasNext(); ) {
      Operand op_Operand = (Operand)its.next();
      if(op_Operand != null)
	if(uselist.contains(op_Operand)) {
	isPred = true;
	continue;
      }
    }
    return isPred;
  }
  
  /** this method has something to do with the resource limitations checking, 
   *  but unfortunately, I can't remember exactly what.
   * 
   * @param instrList list of instructions from the hyperblock
   * @param maxRank time to search for matches with in the    
   *	 instruction list
   * @return true if there are no times equal to maxRank
   */
  public boolean noDuplicateTimes(HashSet instrList, float maxRank) {
    for (Iterator its = ((HashSet)instrList.clone()).iterator(); 
      its.hasNext(); ) {
      Instruction inst = (Instruction)its.next();
      if(inst.getExecTime() == maxRank)
	return false;
	      
    }
    return true;
     
  }
  
  
  /** The execution time for a given instruction.  Normally, this function 
   *  just calls the Instruction method, getRunLength, but in the case of 
   *  AStores or ALoads, the latency saved in the chip information file is 
   *  used instead.
   * 
   * @param inst instruction
   * @param chipInfo contains chip and board information such as   
   *	 where arrays have been stored to memory and the availability of   
   *	 hardware.
   * @return latency for a given operation
   */
  public float getInstrRunLength(Instruction inst, ChipDef chipInfo) {
    float latency = inst.getRunLength();

    if(!GlobalOptions.packInstructions && latency<1)
      latency=1;
    //System.out.println(" instr "+inst+" latency "+latency);
    return latency;
  }
  
  /** sort lists of instructions or hashsets...
   * 
   * @param o_list a list which needs sorting
   */
  /*public void sort(ArrayList o_list) {
    class DoubleCompare implements Comparator {      
      /** compare function used by sort.  sorts hashmaps based on their size 
       *  and instructions in a list depending on the size of their execution 
       *  windows.  
       * 
       * @param o1 input 1
       * @param o2 input 2
       * @return -1, 0, or 1 depending on if o1 or o2 is greater or if they are 
       *     equal
       
      public int compare(Object o1, Object o2) {	
	if (o1 instanceof HashSet	      
	&& o2 instanceof HashSet) {	     
	  HashSet p1 = (HashSet)o1;	     
	  HashSet p2 = (HashSet)o2;	     
	  if( p1.size() < p2.size()) {  	  
	    return -1;  	
	    } else if( p1.size() > p2.size() ){ 	   
	    return 1;	      
	    } else {		
	    return 0;	       
	  }	   
	} else if (o1 instanceof Instruction		 
	&& o2 instanceof Instruction) { 	 
	  Instruction p1 = (Instruction)o1;	     
	  Instruction p2 = (Instruction)o2;	     
	  if( ((Float)(_windowMaxGlobal.get(p1))).floatValue()
              - ((Float)(_windowMinGlobal.get(p1))).floatValue() <
	      ((Float)(_windowMaxGlobal.get(p2))).floatValue()
              - ((Float)(_windowMinGlobal.get(p2))).floatValue()) {	       
	    return -1;  	
	  } else if( ((Float)(_windowMaxGlobal.get(p1))).floatValue()
        	     - ((Float)(_windowMinGlobal.get(p1))).floatValue() >
		     ((Float)(_windowMaxGlobal.get(p2))).floatValue()
        	     - ((Float)(_windowMinGlobal.get(p2))).floatValue() ){	      
	    return 1;	      
	    } else {		
	    return 0;	       
	  }	   
	/*} else if (o1 instanceof Instruction  	   
	&& o2 instanceof Instruction) { 	 
	  Instruction p1 = (Instruction)o1;	     
	  Instruction p2 = (Instruction)o2;	     
	  if (p1.getExecClkCnt() > p2.getExecClkCnt()) {	    
	    return 1;	       
	    } else if (p1.getExecClkCnt() < p2.getExecClkCnt()) {	     
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
  }*/
  /*private void sort(ArrayList o_list) {
    class DoubleCompare implements Comparator {      
      public int compare(Object o1, Object o2) {	
	if (o1 instanceof Instruction		  
	&& o2 instanceof Instruction) { 	 
	  Instruction p1 = (Instruction)o1;	     
	  Instruction p2 = (Instruction)o2;	     
	  if (((Integer)(((ArrayList)(compareInfo.get(p1))).get(0))).intValue() > ((Integer)(((ArrayList)(compareInfo.get(p2))).get(0))).intValue()) {  	  
	    return 1;	       
	    } else if (((Integer)(((ArrayList)(compareInfo.get(p1))).get(0))).intValue() < ((Integer)(((ArrayList)(compareInfo.get(p2))).get(0))).intValue()) { 	   
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
  }*/
}

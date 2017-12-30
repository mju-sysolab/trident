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
import fp.util.*;
import fp.hardware.*;
import fp.*;
import fp.hwdesc.Memory;


/** This pass is used, to calculate the following statistics about the generated
 *  schedules (per block and per design):
 *  
 *  1) Ops/Cycle --including high, low, and average
 *  2) Total Operations
 *  3) Operation Types
 *  4) Ops/Block
 * 
 * @author Kris Peterson
 */
public class GenSchedulerStats extends Pass implements GraphPass {

private static final String[] operatorTypes = new String[] {
     "nop",
     "ret",
     "goto",
     "br",
     "switch",
     "add",
     "sub",
     "mul",
     "div",
     "seteq",
     "setne",
     "setlt",
     "setgt",
     "setle",
     "setge",
     "and",
     "or",
     "xor",
     "shl",
     "shr",
     "inv",
     "not",
     "malloc",
     "load",
     "aload",
     "store",
     "astore",
     "getelementptr",
     "phi",
     "call",
     "cast",
     "select",
     "libop"
};




  public GenSchedulerStats(PassManager pm) {
    super(pm);
  }
   
  /** calculate stats.
   * 
   * @param graph_BlockGraph 
   * @return true
   */
  public boolean optimize(BlockGraph graph) {

    float designAveOpsPCycle = 0;
    float designMaxOpsPCycle = -99999999;
    float designMinOpsPCycle =  99999999;
    //memory access stats:
    //number of reads or writes per cycle
    float designAveReadsPCycle = 0;
    float designMaxReadsPCycle = -99999999;
    float designMinReadsPCycle =  99999999;
    float designAveWritesPCycle = 0;
    float designMaxWritesPCycle = -99999999;
    float designMinWritesPCycle =  99999999;
    //number of bits read or written per cycle
    float designAveReadDataPCycle = 0;
    float designMaxReadDataPCycle = -99999999;
    float designMinReadDataPCycle =  99999999;
    float designAveWriteDataPCycle = 0;
    float designMaxWriteDataPCycle = -99999999;
    float designMinWriteDataPCycle =  99999999;
    //number of bits read or written as a percentage of the maximum possible per
    //cycle
    float designAveRDPercentPCycle = 0;
    float designMaxRDPercentPCycle = -99999999;
    float designMinRDPercentPCycle =  99999999;
    float designAveWDPercentPCycle = 0;
    float designMaxWDPercentPCycle = -99999999;
    float designMinWDPercentPCycle =  99999999;

    float cyclesPerBlock =  0;
    float opsPerBlock =  0;
    int designTotOps = 0;
    int designCycleCount = 0;
    HashMap designOperatorCounts = new HashMap();
    graph.setOperatorCounts(new HashMap()); 
    
    float totPossReadData = 0;    
    float totPossWriteData = 0;    
    ArrayList memBlockList = GlobalOptions.chipDef.getMemoryBlockList();
    for (Iterator itsMem = memBlockList.iterator(); itsMem.hasNext(); ) {
      Memory memBlock = (Memory)itsMem.next();
      totPossReadData += ((float)memBlock.getWidth() * memBlock.getNumOfReadBus());
      totPossWriteData += ((float)memBlock.getWidth() * memBlock.getNumOfWriteBus());
    }
   
    for (Iterator vIt = graph.getAllNodes().iterator(); 
             vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      float blockAveOpsPCycle = 0;
      float blockMaxOpsPCycle = -99999999;
      float blockMinOpsPCycle =  99999999;
      //memory access stats:
      //number of reads or writes per cycle
      float blockAveReadsPCycle = 0;
      float blockMaxReadsPCycle = -99999999;
      float blockMinReadsPCycle =  99999999;
      float blockAveWritesPCycle = 0;
      float blockMaxWritesPCycle = -99999999;
      float blockMinWritesPCycle =  99999999;
      //number of bits read or written per cycle
      float blockAveReadDataPCycle = 0;
      float blockMaxReadDataPCycle = -99999999;
      float blockMinReadDataPCycle =  99999999;
      float blockAveWriteDataPCycle = 0;
      float blockMaxWriteDataPCycle = -99999999;
      float blockMinWriteDataPCycle =  99999999;
      //number of bits read or written as a percentage of the maximum possible per
      //cycle
      float blockAveRDPercentPCycle = 0;
      float blockMaxRDPercentPCycle = -99999999;
      float blockMinRDPercentPCycle =  99999999;
      float blockAveWDPercentPCycle = 0;
      float blockMaxWDPercentPCycle = -99999999;
      float blockMinWDPercentPCycle =  99999999;
      int blockTotOps = 0;
      int blockCycleCount = 0;
      HashMap rSchedule = new HashMap();
      HashMap rDSchedule = new HashMap();
      HashMap wSchedule = new HashMap();
      HashMap wDSchedule = new HashMap();
      HashMap schedule = new HashMap();
      HashMap blockOperatorCounts = new HashMap();
      ArrayList instList = node.getInstructions();
      blockTotOps = instList.size();
      node.setOperatorCounts(new HashMap());
      
      if(blockTotOps == 0) continue;
      
      for (Iterator iIt = instList.iterator(); 
             iIt.hasNext();) {
        Instruction inst = (Instruction) iIt.next();
        
	  if(inst.type() == null) continue;
	System.out.println("inst " + inst + " inst type " + inst.type() + " inst width " +
	                      inst.type().getWidth());
	int total = inst.getNumberOfOperands();
	for(int i = 0; i < total; i++) {
          Operand op = inst.getOperand(i);
	  if(op == null) continue;
	  if(op.getType() == null) continue;
	  System.out.println("inst " + inst + " op " + op + " op type " + op.getType() + " op width " +
	                      op.getType().getWidth());
	}
	int a = 6;
	int b = 7;
	int c = a * b;
	int d = (int)5.4;
	
	int cycle = inst.getExecClkCnt();
	
	for(int i = 0; i<operatorTypes.length-1;i++)
	{
	  if(inst.operator.toString().indexOf(operatorTypes[i]) >= 0)
	  {
	    String type = "";
	    if(inst.type() != null)
	    {
	      if(inst.operator.getInputClass() == Operator.FP)
		type = "fp_";
	      if(inst.operator.getInputClass() == Operator.INT)
		type = "int_";
	    }
	    
	    if(!blockOperatorCounts.containsKey(type + operatorTypes[i]))
	       blockOperatorCounts.put(type + operatorTypes[i], 
	                               new Integer(0));
	    int operCntTmp = 
		  ((Integer)blockOperatorCounts.get(type + operatorTypes[i]))
		                                     .intValue();
	    blockOperatorCounts.put(type + operatorTypes[i], 
	                            new Integer(++operCntTmp));
	    if(!designOperatorCounts.containsKey(type + operatorTypes[i]))
	       designOperatorCounts.put(type + operatorTypes[i], 
	                               new Integer(0));
	    operCntTmp = 
		  ((Integer)designOperatorCounts.get(type + operatorTypes[i]))
		                                      .intValue();
	    designOperatorCounts.put(type + operatorTypes[i], 
	                            new Integer(++operCntTmp));
	  }
	}
	
	if(ALoad.conforms(inst)) {
	  blockAveReadsPCycle++;
	  blockAveReadDataPCycle += (ALoad.getPrimalSource(inst)).getType().getWidth();
	  if(!rSchedule.containsKey(new Integer(cycle)))
	     rSchedule.put(new Integer(cycle), new Integer(0));
	  int readCntTmp = ((Integer)rSchedule.get(new Integer(cycle))).intValue();
	  rSchedule.put(new Integer(cycle), new Integer(++readCntTmp));
	  if(!rDSchedule.containsKey(new Integer(cycle)))
	     rDSchedule.put(new Integer(cycle), new Integer(0));
	  int rDCntTmp = ((Integer)rDSchedule.get(new Integer(cycle))).intValue();
	  rDCntTmp += (ALoad.getPrimalSource(inst)).getType().getWidth();
	  rDSchedule.put(new Integer(cycle), new Integer(rDCntTmp));
	}
	if(AStore.conforms(inst)) {
	  blockAveWritesPCycle++;
	  blockAveWriteDataPCycle += (AStore.getPrimalDestination(inst)).getType().getWidth();
	  if(!wSchedule.containsKey(new Integer(cycle)))
	     wSchedule.put(new Integer(cycle), new Integer(0));
	  int readCntTmp = ((Integer)wSchedule.get(new Integer(cycle))).intValue();
	  wSchedule.put(new Integer(cycle), new Integer(++readCntTmp));
	  if(!wDSchedule.containsKey(new Integer(cycle)))
	     wDSchedule.put(new Integer(cycle), new Integer(0));
	  int wDCntTmp = ((Integer)wDSchedule.get(new Integer(cycle))).intValue();
	  wDCntTmp += (AStore.getPrimalDestination(inst)).getType().getWidth();
	  wDSchedule.put(new Integer(cycle), new Integer(wDCntTmp));
	}
	
	blockCycleCount = Math.max(blockCycleCount, cycle);
	if(!schedule.containsKey(new Integer(cycle)))
	   schedule.put(new Integer(cycle), new Integer(0));
	int opCntTmp = ((Integer)schedule.get(new Integer(cycle))).intValue();
	schedule.put(new Integer(cycle), new Integer(++opCntTmp));
      
      }
      blockCycleCount++; //since the 1st cycle is 0
      blockAveOpsPCycle = ((float)blockTotOps) / ((float)blockCycleCount);
      
      designAveReadsPCycle += blockAveReadsPCycle;
      designAveReadDataPCycle += blockAveReadDataPCycle;
      designAveWritesPCycle += blockAveWritesPCycle;
      designAveWriteDataPCycle += blockAveWriteDataPCycle;
      
      designTotOps += blockTotOps;
      designCycleCount += blockCycleCount;
      
      blockAveReadsPCycle /= blockCycleCount;
      blockAveReadDataPCycle /= blockCycleCount;
      blockAveWritesPCycle /= blockCycleCount;
      blockAveWriteDataPCycle /= blockCycleCount;
      
      for (Iterator iIt = schedule.keySet().iterator(); 
             iIt.hasNext();) {
        Integer cycle = (Integer) iIt.next();
       
        int opCntTmp = ((Integer)schedule.get(cycle)).intValue();
	blockMaxOpsPCycle = Math.max(blockMaxOpsPCycle, opCntTmp);
	blockMinOpsPCycle = Math.min(blockMinOpsPCycle, opCntTmp);
       
      }
      //blockMaxOpsPCycle /= blockCycleCount;
      //blockMinOpsPCycle /= blockCycleCount;
      for (Iterator iIt = rSchedule.keySet().iterator(); 
             iIt.hasNext();) {
        Integer cycle = (Integer) iIt.next();
       
        int readCntTmp = ((Integer)rSchedule.get(cycle)).intValue();
	blockMaxReadsPCycle = Math.max(blockMaxReadsPCycle, readCntTmp);
	blockMinReadsPCycle = Math.min(blockMinReadsPCycle, readCntTmp);
       
      }
      if(rSchedule.size()<blockCycleCount) {
	blockMinReadsPCycle = 0;
      }
      if(rSchedule.size()==0) {
	blockMaxReadsPCycle = 0;
	blockMinReadsPCycle = 0;
      }
      //blockMaxReadsPCycle /= blockCycleCount;
      //blockMinReadsPCycle /= blockCycleCount;
      for (Iterator iIt = rDSchedule.keySet().iterator(); 
             iIt.hasNext();) {
        Integer cycle = (Integer) iIt.next();
       
        int rDCntTmp = ((Integer)rDSchedule.get(cycle)).intValue();
	blockMaxReadDataPCycle = Math.max(blockMaxReadDataPCycle, rDCntTmp);
	blockMinReadDataPCycle = Math.min(blockMinReadDataPCycle, rDCntTmp);
       
      }
      if(rDSchedule.size()<blockCycleCount) {
	blockMinReadDataPCycle = 0;
      }
      if(rDSchedule.size()==0) {
	blockMaxReadDataPCycle = 0;
	blockMinReadDataPCycle = 0;
      }
      //blockMaxReadDataPCycle /= blockCycleCount;
      //blockMinReadDataPCycle /= blockCycleCount;
      for (Iterator iIt = wSchedule.keySet().iterator(); 
             iIt.hasNext();) {
        Integer cycle = (Integer) iIt.next();
       
        int writeCntTmp = ((Integer)wSchedule.get(cycle)).intValue();
	blockMaxWritesPCycle = Math.max(blockMaxWritesPCycle, writeCntTmp);
	blockMinWritesPCycle = Math.min(blockMinWritesPCycle, writeCntTmp);
       
      }
      if(wSchedule.size()<blockCycleCount) {
	blockMinWritesPCycle = 0;
      }
      if(wSchedule.size()==0) {
	blockMaxWritesPCycle = 0;
	blockMinWritesPCycle = 0;
      }
      //blockMaxWritesPCycle /= blockCycleCount;
      //blockMinWritesPCycle /= blockCycleCount;
      for (Iterator iIt = wDSchedule.keySet().iterator(); 
             iIt.hasNext();) {
        Integer cycle = (Integer) iIt.next();
       
        int wDCntTmp = ((Integer)wDSchedule.get(cycle)).intValue();
	blockMaxWriteDataPCycle = Math.max(blockMaxWriteDataPCycle, wDCntTmp);
	blockMinWriteDataPCycle = Math.min(blockMinWriteDataPCycle, wDCntTmp);
       
      }
      if(wDSchedule.size()<blockCycleCount) {
	blockMinWriteDataPCycle = 0;
      }
      if(wDSchedule.size()==0) {
	blockMaxWriteDataPCycle = 0;
	blockMinWriteDataPCycle = 0;
      }
      //blockMaxWriteDataPCycle /= blockCycleCount;
      //blockMinWriteDataPCycle /= blockCycleCount;
      
      blockAveRDPercentPCycle = blockAveReadDataPCycle / totPossReadData;
      blockMaxRDPercentPCycle = blockMaxReadDataPCycle / totPossReadData;
      blockMinRDPercentPCycle = blockMinReadDataPCycle / totPossReadData;
      blockAveWDPercentPCycle = blockAveWriteDataPCycle / totPossReadData;
      blockMaxWDPercentPCycle = blockMaxWriteDataPCycle / totPossReadData;
      blockMinWDPercentPCycle = blockMinWriteDataPCycle / totPossReadData;
      
      designAveRDPercentPCycle += blockAveRDPercentPCycle;
      designAveWDPercentPCycle += blockAveWDPercentPCycle;
      
      designMaxOpsPCycle = Math.max(designMaxOpsPCycle, blockMaxOpsPCycle);
      designMinOpsPCycle = Math.min(designMinOpsPCycle, blockMinOpsPCycle);
      designMaxReadsPCycle = Math.max(designMaxReadsPCycle, blockMaxReadsPCycle);
      designMinReadsPCycle = Math.min(designMinReadsPCycle, blockMinReadsPCycle);
      designMaxWritesPCycle = Math.max(designMaxWritesPCycle, 
                                       blockMaxWritesPCycle);
      designMinWritesPCycle = Math.min(designMinWritesPCycle, 
                                       blockMinWritesPCycle);
      designMaxReadDataPCycle = Math.max(designMaxReadDataPCycle, 
                                         blockMaxReadDataPCycle);
      designMinReadDataPCycle = Math.min(designMinReadDataPCycle, 
                                         blockMinReadDataPCycle);
      designMaxWriteDataPCycle = Math.max(designMaxWriteDataPCycle, 
                                          blockMaxWriteDataPCycle);
      designMinWriteDataPCycle = Math.min(designMinWriteDataPCycle, 
                                          blockMinWriteDataPCycle);
      designMaxRDPercentPCycle = Math.max(designMaxRDPercentPCycle, 
                                          blockMaxRDPercentPCycle);
      designMinRDPercentPCycle = Math.min(designMinRDPercentPCycle, 
                                          blockMinRDPercentPCycle);
      designMaxWDPercentPCycle = Math.max(designMaxWDPercentPCycle, 
                                          blockMaxWDPercentPCycle);
      designMinWDPercentPCycle = Math.min(designMinWDPercentPCycle, 
                                          blockMinWDPercentPCycle);
      
      node.setAveOpsPCycle(blockAveOpsPCycle);
      node.setMaxOpsPCycle(blockMaxOpsPCycle);
      node.setMinOpsPCycle(blockMinOpsPCycle);
      node.setTotOps(blockTotOps);
      node.setCycleCount(blockCycleCount);
      node.setOperatorCounts(blockOperatorCounts);
    
      System.out.println("Block Stats:");
      System.out.println("Block:" + node.getLabel());

      //read stats
      System.err.println("Average Reads/Cycle:" + blockAveReadsPCycle);
      System.err.println("Max Reads/Cycle:" + blockMaxReadsPCycle);
      System.err.println("Min Reads/Cycle:" + blockMinReadsPCycle);
      System.err.println("Average # bits read/Cycle:" + blockAveReadDataPCycle);
      System.err.println("Max # bits read/Cycle:" + blockMaxReadDataPCycle);
      System.err.println("Min # bits read/Cycle:" + blockMinReadDataPCycle);
      System.err.println("Average % of possible read/Cycle:" + blockAveRDPercentPCycle);
      System.err.println("Max % of possible read/Cycle:" + blockMaxRDPercentPCycle);
      System.err.println("Min % of possible read/Cycle:" + blockMinRDPercentPCycle);
      
      //write stats
      System.err.println("Average Writes/Cycle:" + blockAveWritesPCycle);
      System.err.println("Max Writes/Cycle:" + blockMaxWritesPCycle);
      System.err.println("Min Writes/Cycle:" + blockMinWritesPCycle);
      System.err.println("Average # bits written/Cycle:" + blockAveWriteDataPCycle);
      System.err.println("Max # bits written/Cycle:" + blockMaxWriteDataPCycle);
      System.err.println("Min # bits written/Cycle:" + blockMinWriteDataPCycle);
      System.err.println("Average % of possible write/Cycle:" + blockAveWDPercentPCycle);
      System.err.println("Max % of possible write/Cycle:" + blockMaxWDPercentPCycle);
      System.err.println("Min % of possible write/Cycle:" + blockMinWDPercentPCycle);
      System.err.println("============================================");

     
    }
    cyclesPerBlock = ((float)designCycleCount) /
                     ((float)graph.getAllNodes().size());
    designAveOpsPCycle = ((float)designTotOps) / ((float)designCycleCount);
    designAveReadsPCycle = ((float)designAveReadsPCycle) / 
                           ((float)designCycleCount);
    designAveWritesPCycle = ((float)designAveWritesPCycle) / 
                            ((float)designCycleCount);
    designAveReadDataPCycle = ((float)designAveReadDataPCycle) / 
                              ((float)designCycleCount);
    designAveWriteDataPCycle = ((float)designAveWriteDataPCycle) / 
                               ((float)designCycleCount);
    designAveRDPercentPCycle = ((float)designAveRDPercentPCycle) / 
                               ((float)designCycleCount);
    designAveWDPercentPCycle = ((float)designAveWDPercentPCycle) / 
                               ((float)designCycleCount);
    			   
    opsPerBlock = ((float)designTotOps) / ((float)graph.getAllNodes().size());
    
    graph.setAveOpsPCycle(designAveOpsPCycle);
    graph.setMaxOpsPCycle(designMaxOpsPCycle);
    graph.setMinOpsPCycle(designMinOpsPCycle);
    graph.setCyclesPerBlock(cyclesPerBlock);
    graph.setOpsPerBlock(opsPerBlock);
    graph.setTotOps(designTotOps);
    graph.setCycleCount(designCycleCount);
    graph.setOperatorCounts(designOperatorCounts);
    
    System.out.println("Block Stats:");
    for (Iterator vIt = graph.getAllNodes().iterator(); 
             vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      System.out.println("Block:" + node.getLabel());
      
      System.out.println("Average Ops/Cycle:" + node.getAveOpsPCycle());
      System.out.println("Max Ops/Cycle:" + node.getMaxOpsPCycle());
      System.out.println("Min Ops/Cycle:" + node.getMinOpsPCycle());
      System.out.println("Total Ops:" + node.getTotOps());
      System.out.println("Cycle Count:" + node.getCycleCount());
      HashMap opCnts = node.getOperatorCounts();
      for (Iterator oIt = opCnts.keySet().iterator(); 
             oIt.hasNext();) {
        String operator = (String) oIt.next();
        
	int opCnt = ((Integer)opCnts.get(operator)).intValue();
	System.out.println("operator " + operator + " Count:" + opCnt);
	
      }
      System.out.println("============================================");
    
    }
    System.err.println("Design Stats:");
      
    System.out.println("Average Ops/Cycle:" + graph.getAveOpsPCycle());
    System.out.println("Max Ops/Cycle:" + graph.getMaxOpsPCycle());
    System.out.println("Min Ops/Cycle:" + graph.getMinOpsPCycle());
    System.out.println("Ops/Block:" + graph.getOpsPerBlock());
    System.out.println("Cycle/Block:" + graph.getCyclesPerBlock());
    System.out.println("Total Ops:" + graph.getTotOps());
    System.out.println("Cycle Count:" + graph.getCycleCount());
    System.out.println("block Count:" + graph.getAllNodes().size());
    HashMap opCnts = graph.getOperatorCounts();
    for (Iterator oIt = opCnts.keySet().iterator(); 
    	   oIt.hasNext();) {
      String operator = (String) oIt.next();
      
      int opCnt = ((Integer)opCnts.get(operator)).intValue();
      System.out.println("operator " + operator + " Usage Count:" + opCnt);

    }
    HashMap opCnts2 = GlobalOptions.chipDef.getOpCntsAvailable();
    for (Iterator oIt = opCnts2.keySet().iterator(); 
    	   oIt.hasNext();) {
      Operator operator = (Operator) oIt.next();
      
      String opName= new String();
      for(int i = 0; i<operatorTypes.length-1;i++)
      {
	if(operator.toString().indexOf(operatorTypes[i]) >= 0)
        {
          String type = "";
          if(operator.getInputClass() == Operator.FP)
            type = "fp_";
          if(operator.getInputClass() == Operator.INT)
            type = "int_";
	  opName = type + operatorTypes[i];
        }
      }
      int opCnt = ((Integer)opCnts2.get(operator)).intValue();
      System.out.println("operator " + opName + " Count:" + opCnt);

    }
    //read stats
    System.err.println("Average Reads/Cycle:" + designAveReadsPCycle);
    System.err.println("Max Reads/Cycle:" + designMaxReadsPCycle);
    System.err.println("Min Reads/Cycle:" + designMinReadsPCycle);
    System.err.println("Average # bits read/Cycle:" + designAveReadDataPCycle);
    System.err.println("Max # bits read/Cycle:" + designMaxReadDataPCycle);
    System.err.println("Min # bits read/Cycle:" + designMinReadDataPCycle);
    System.err.println("Average % of possible read/Cycle:" + designAveRDPercentPCycle);
    System.err.println("Max % of possible read/Cycle:" + designMaxRDPercentPCycle);
    System.err.println("Min % of possible read/Cycle:" + designMinRDPercentPCycle);
    
    //write stats
    System.err.println("Average Writes/Cycle:" + designAveWritesPCycle);
    System.err.println("Max Writes/Cycle:" + designMaxWritesPCycle);
    System.err.println("Min Writes/Cycle:" + designMinWritesPCycle);
    System.err.println("Average # bits written/Cycle:" + designAveWriteDataPCycle);
    System.err.println("Max # bits written/Cycle:" + designMaxWriteDataPCycle);
    System.err.println("Min # bits written/Cycle:" + designMinWriteDataPCycle);
    System.err.println("Average % of possible write/Cycle:" + designAveWDPercentPCycle);
    System.err.println("Max % of possible write/Cycle:" + designMaxWDPercentPCycle);
    System.err.println("Min % of possible write/Cycle:" + designMinWDPercentPCycle);
    System.out.println("============================================");
    
    
         
    return true;
  }
   
   
   
  public String name() { 
    return "GenSchedulerStats";
  }
}

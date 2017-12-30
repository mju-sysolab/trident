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


/** this pass was created, because for debugging force-directed scheduling, it 
 * is useful to compare with the ASAP and ALAP schedules.  However, the 
 * schedulers will not work, if they receive a block as input that is already 
 * scheduled.  This pass will reset the instruction executioin times to 0 and 
 * will unshare all operators.
 * 
 * @author Kris Peterson
 */
public class UnSchedulePass extends Pass implements GraphPass {
   
  private HashMap _scheduleList;
   
  public UnSchedulePass(PassManager pm) {
    super(pm);
    _scheduleList = new HashMap();
  }
  
  /** for each node in the control flow graph, for each instruction in the 
   *  block, set the execution time back to -1 (the flag value for an 
   *  unscheduled instruction), and delete sharing information.
   * 
   * @param graph_BlockGraph 
   */
  public boolean optimize(BlockGraph graph) {
    
    for (Iterator vIt = (new HashSet(graph.getAllNodes())).iterator(); vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      if(node.getInstructions().size()==0) continue;
      Schedule sched = new Schedule(node);
      sched.unSchedule();
    }    
    //Schedule.unSchedule();
    
    /*graph_BlockGraph.setAveOpsPCycle(0);
    graph_BlockGraph.setMaxOpsPCycle(0);
    graph_BlockGraph.setMinOpsPCycle(0);
    graph_BlockGraph.setCyclesPerBlock(0);
    graph_BlockGraph.setOpsPerBlock(0);
    graph_BlockGraph.setTotOps(0);
    graph_BlockGraph.setCycleCount(0);
    graph_BlockGraph.setOperatorCounts(new HashMap());
    for (Iterator vIt = graph_BlockGraph.getAllNodes().iterator(); 
              vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      ArrayList list_ArrayList = node.getInstructions();
       
      node.setAveOpsPCycle(0);
      node.setMaxOpsPCycle(0);
      node.setMinOpsPCycle(0);
      node.setTotOps(0);
      node.setCycleCount(0);
      node.setOperatorCounts(new HashMap());
      
      for (Iterator it = ((ArrayList)list_ArrayList).iterator(); 
        it.hasNext(); ) {
        Instruction instr = (Instruction)it.next();
        instr.setExecTime(-1);
        instr.setExecClkCnt(-1);
        instr.setIsShared(false);
        instr.setShareSet(null);
      }
    }*/
    return true;
  }
  
   
  public String name() { 
    return "UnSchedulePass";
  }
   
  private void sort(ArrayList o_list) {
    class DoubleCompare implements Comparator {      
      public int compare(Object o1, Object o2) {        
        if (o1 instanceof Instruction             
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
  }
}


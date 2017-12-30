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
import java.math.BigDecimal;

import fp.flowgraph.*;
import fp.hwdesc.Memory;

/** This class is used to calculate the minimum initiation interval due to 
 *  resources.
 */
public class CalculateII
{
  
  /** Alle achtung! Offizier im Gang!
   */
  public CalculateII() {
  }
   
  /** This method implements the calculation of MResII,  As MResII is used for
   *  pipelining loops, to find it, I must determine just how many memory 
   *  accesses can be performed per clock tick if the entire loop body were 
   *  executed at once.  Therefore, I go through the whole loop, looking for 
   *  reads and writes to memory.  The classes Chipdef and MemBlock each have 
   *  methods for handling most of the counting and they take into account how 
   *  many busses there are and in which memory, which array is, but 
   *  calculateTheII needs to handle half dual-ported RAMs differently.  These 
   *  are RAMs with a read data port and a write data port, but only one address 
   *  bus.  It can handle a read and a write concurrently only if it is to the 
   *  same address.  This method checks for these half dual-port RAMs and uses 
   *  the memBlock bus count methods differently.
   * 
   * @param node_BlockNode hyperblock node being analyzed
   * @param chipInfo chip information
   * @return ii, if calculation was successful, else -1
   */
  public float calculateTheII(BlockNode node_BlockNode, ChipDef chipInfo) {
    int iI = 0;
    ArrayList defList = new ArrayList();
    ArrayList useList = new ArrayList(); 
    ArrayList allVarsList = new ArrayList();
    HashMap varLocationsHM = new HashMap();
    if(chipInfo.getOpList()==null) {
      throw new HardwareException("could not make iI calculation, because the" +
                                  " hardware analyzation did not complete " +
				  "successfully");
    }
    if(isBlockInLoop(node_BlockNode)) {
       
      //for debugging:
      int iImem = chipInfo.calcLoad(node_BlockNode, node_BlockNode.getInstructions());
      System.out.println("iI due to memory requirements: " + iImem);
       
      //end memory analysis
       
      //analyze hardware
      int iIres = 1;
      ArrayList opList = chipInfo.getOpList();
      for (Iterator its = (opList).iterator(); 
        its.hasNext(); ) {
        Operator op_Operand = (Operator)its.next();
         
        float needed = (float)chipInfo.getNumNeededForOp(op_Operand);
        float had = (float)chipInfo.getNumAvailableForOp(op_Operand);
        int iItmp = (int)(needed/had + 0.99999999999); //round up
        if(iItmp > iIres)
          iIres = iItmp;
         
      }
      System.out.println("iI due to hardware requirements: " + iIres);
      if(iIres > iImem)
        iI = iIres;
      else
        iI = iImem;
       
      System.out.println("iI: " + iI);
       
      //save ii to the blocknode object
      node_BlockNode.setII(iI);
      return iI;
    }
    else
      return -1;
     
  }
   
  /** checks the edges on a block in the control flow graph to see if any loop 
   *  back to this block, in which case, it must be a loop.
   * 
   * @param node_BlockNode hyperblock being analyzed
   * @return true if a loop
   */
  public boolean isBlockInLoop(BlockNode node_BlockNode) {
    boolean isBlockInLoop = false;
    Set inEdges = node_BlockNode.getInEdges();
    Set outEdges = node_BlockNode.getOutEdges();
    for (Iterator its = ((Set)inEdges).iterator(); 
      its.hasNext(); ) {
      BlockEdge edge = (BlockEdge)its.next();
      if(edge != null) {
        if(outEdges.contains(edge))
          isBlockInLoop = true;
        
      }
    }
    return isBlockInLoop;
  }
  
  
  /** sort operands based on their widths and operators based on their latencies 
   *  or size
   * 
   * @param o_list list to be sorted
   */
  private void sort(ArrayList o_list) {
    class DoubleCompare implements Comparator {      
      /** custom compare for sort
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
        } else {          
          throw new ClassCastException("Not Instruction");        
        }      
      }    
    }        
    Collections.sort(o_list, new DoubleCompare());  
  }
  
  
}

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
import fp.hardware.*;
import fp.*;


/** this pass is for calculating the MResII (minimum initiation interval based 
 *  on resources).
 * 
 * @author Kris Peterson
 */
public class CalcIIPass extends Pass implements GraphPass {
   
   
  public CalcIIPass(PassManager pm) {
    super(pm);
  }
  
  /** call calculateTheII from CalculateII.java to find MResII
   * 
   * @param graph_BlockGraph 
   */
  public boolean optimize(BlockGraph graph_BlockGraph) {
    for (Iterator vIt = graph_BlockGraph.getAllNodes().iterator(); 
          vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      CalculateII iICalc_CalculateII = new CalculateII();
      iICalc_CalculateII.calculateTheII(node, GlobalOptions.chipDef);
    }
     
    return true;
  }
  
   
  public String name() { 
    return "CalcIIPass";
  }
   
}


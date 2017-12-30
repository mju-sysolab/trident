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

import fp.*;
import fp.flowgraph.*;
import fp.util.*;
import fp.hardware.*;


/** This pass starts the schedulers.  I use one pass now, because there are 
 *  several places that scheduling is requested, and I wanted to make it so
 *  there was one general setup for all.
 * 
 * @author Kris Peterson
 */
public class SchedulerPass extends Pass implements GraphPass {

  private OperationSelection _opSel;
  
  //this constructor should never be called or there will be problems:
  public SchedulerPass(PassManager pm) {
    this(pm, null);
    System.err.println("Not passing in an operationSelection object will caus" +
                       "e problems!");
  }
  
  public SchedulerPass(PassManager pm, OperationSelection opSel) {
    super(pm);
    _opSel = opSel;
  }

  public boolean optimize(BlockGraph graph) {
     
    HashSet allNodes = new HashSet(graph.getAllNodes());
    for (Iterator vIt = allNodes.iterator(); 
             vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      
      //this class was written to handle the setup for all the schedulers
      //so that whether a scheduler is called from a pass, from
      //AnalyzeHardwareConstraints, the Modulo Scheduler, or any other place,
      //there can be one setup:
      MasterScheduler prelimSched = new MasterScheduler(node, _opSel, 
                                                        GlobalOptions.chipDef);
      prelimSched.schedule(graph);
    }
     
    return true;
  }

  public String name() { 
    return "SchedulerPass";
  }


}

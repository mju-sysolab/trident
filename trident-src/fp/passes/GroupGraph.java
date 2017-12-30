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

import fp.flowgraph.BlockGraph;
import fp.flowgraph.BlockNode;

public class GroupGraph extends Pass implements GraphPass {

  /*
    The purpose of this pass is to group a set of block passes
    into a single graph pass.  This is a convenience class for 
    the pass manager and is not used directly or should not be 
    used directly.

    It is automagic!
  */
  
  private LinkedList _pass_schedule;

  public GroupGraph(PassManager pm) {
    super(pm);
    _pass_schedule = new LinkedList();
  }

  public boolean optimize(BlockGraph graph) {
    int verbose = pm.getVerbose();

    for (Iterator vIt = graph.getAllNodes().iterator(); vIt.hasNext();){
      BlockNode node = (BlockNode) vIt.next();
      for(Iterator iter = _pass_schedule.iterator(); iter.hasNext(); ) {
	BlockPass pass = (BlockPass) iter.next();
	long time = 0;

	if (verbose >= PassManager.VERBOSE_L1) {
	  time = System.currentTimeMillis();
	  pm.print(PassManager.VERBOSE_L2, " BlockPass : "+pass.name() + " start ");
	  pm.print(PassManager.VERBOSE_L3, new Date().toString());
	  pm.println(PassManager.VERBOSE_L2, "");
	}
	
	// error checking ???
	pass.optimize(node);

	if (verbose >= PassManager.VERBOSE_L1) {
	  time = System.currentTimeMillis() - time;
	  String pass_name = " BlockPass : "+pass.name();
	  pm.println(PassManager.VERBOSE_L3, pass_name + " run time "+time+" ms");
          pm.print(PassManager.VERBOSE_L2, pass_name + " stop ");
          pm.print(PassManager.VERBOSE_L3, new Date().toString());
          pm.println(PassManager.VERBOSE_L2, "");

	  pm.getStats().add(new PassStat("  "+pass.name(), time));
	}
      }
    }      
    return true;
  }

  public void add(Pass pass) {
    // check dependencies 
    // see if CFG is consistent 
    // and if this requires it to be.
    // add appropriate passes.
    _pass_schedule.add(pass);
  }


  public String name() { 
    return "GroupGraph";
  }



}


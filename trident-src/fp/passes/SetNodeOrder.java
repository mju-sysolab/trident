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

import fp.flowgraph.BlockGraph;

public class SetNodeOrder extends Pass implements GraphPass {

  /*
    This pass recalculates the pre, post, reversse-post orders
    of the block graph.  This is necessary, if nodes have been
    eliminated through merging or other optimizations.

    It could be combined with other passes that recalculate 
    stuff as well -- like the loop stuff.
  */


  public SetNodeOrder(PassManager pm) {
    super(pm);
  }

  public boolean optimize(BlockGraph graph) {
    // this is not a very good name.
    graph.markEdges();
    graph.resetMarkers();

    return true;
  }


  public String name() { 
    return "SetNodeOrder";
  }

}



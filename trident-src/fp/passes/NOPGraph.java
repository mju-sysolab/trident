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

public class NOPGraph extends Pass implements GraphPass {

  /*
    The purpose of this pass is to do nothing.  However, it probably
    will collect some statistics ... maybe.  This gives an indication
    about how much the cost of starting the JVM is.
  */
  

  public NOPGraph(PassManager pm) {
    super(pm);
  }

  public boolean optimize(BlockGraph graph) {
    return true;
  }

  public String name() { 
    return "NOPGraph";
  }


}


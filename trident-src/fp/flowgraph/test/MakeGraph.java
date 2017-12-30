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


package fp.flowgraph.test;

import fp.flowgraph.*;

public class MakeGraph implements Operators {
  BlockGraph cfg;

  MakeGraph() {
    cfg = new BlockGraph();

    BlockNode a;
    a = (BlockNode)cfg.addNode();

    MakeBlock mb = new MakeBlock(a);
    
    ((ControlFlowEdge)cfg.addEdge(cfg.ENTRY, a)).setLabel(Boolean.TRUE);
    ((ControlFlowEdge)cfg.addEdge(a,a)).setLabel(Boolean.TRUE);
    ((ControlFlowEdge)cfg.addEdge(a,cfg.EXIT)).setLabel(Boolean.FALSE);

  }


  public static void main(String args[]) {
    MakeGraph mg = new MakeGraph();
    mg.cfg.writeDotFile("mg_test.dot");

  }

}

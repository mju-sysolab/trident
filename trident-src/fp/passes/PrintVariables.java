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
import fp.flowgraph.LoopDotGraph;

public class PrintVariables extends Pass implements GraphPass {

  /*
    The purpose of this pass is to print the variables
  */
  

  private String _name = null;
  static private int count = 0;


  public PrintVariables(PassManager pm) {
    super(pm);
  }

  public PrintVariables(PassManager pm, String s) {
    this(pm);
    _name = s;
  }

  public boolean optimize(BlockGraph graph) {
    String debug = "";
    String count_string = "";

    if (_name != null) {
      debug = _name;
      count_string = "_"+(count++);
    }

    graph.printVariables();

    return true;
  }

  public String name() { 
    return "PrintVariables";
  }


}


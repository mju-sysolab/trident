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

public class PrintLoopGraph extends Pass implements GraphPass {

  /*
    The purpose of this pass is to print a copy of the current graph.
    This might be used to see what the result of various passes are and
    for debugging.

    It could make a big dot file -- but not nearly as big as the final 
    circuit as a dot file. :)
  */
  

  private String _name = null;
  static private int count = 0;


  public PrintLoopGraph(PassManager pm) {
    super(pm);
  }

  public PrintLoopGraph(PassManager pm, String s) {
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

    LoopDotGraph ldg = new LoopDotGraph(graph.getRootLoop(), graph.getName() );

    // this could include the whole path
    String file_name = ldg.getName();

    // we need to get just the file basename and path from it
    int index = file_name.lastIndexOf(System.getProperty("file.separator"));
    String basename = file_name.substring(index+1);
    String path = file_name.substring(0, index+1);

    // now create the changed file name
    file_name = path + debug + basename + count_string + ".dot";

    ldg.writeDotFile(file_name);
    // should this be static ???
    // this should ask pm about a verbosity level ...
    if (pm.getVerbose() >= PassManager.VERBOSE_L2) 
      System.err.println("writing "+file_name);

    return true;
  }

  public String name() { 
    return "PrintLoopGraph";
  }

  // ??
  public static int incrementCount() { return count++; }


}


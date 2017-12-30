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


package fp.flowgraph;

import fp.graph.Edge;

/**
 * A directed edge in a data-flow graph
 *
 * @author Nathan Kitchen
 */
public class DataFlowEdge extends Edge {
  /**
   * Construct a new data-flow edge with no tag, no source, and no sink
   */
  public DataFlowEdge() {
    this(null);
  }

  /**
   * Construct a new data-flow edge with the given tag, no source, and
   * no sink
   */
  public DataFlowEdge(Object tag) {
    super(tag);
    setDotAttribute("label", "");
  }

  /**
   * Construct a new data-flow edge with no tag from <code>source</code>
   * to <code>sink</code>
   */
  public DataFlowEdge(DataFlowNode source, DataFlowNode sink) {
    this(source, sink, null);
  }

  /**
   * Construct a new data-flow edge with the given tag from
   * <code>source</code> to <code>sink</code>
   */
  public DataFlowEdge(DataFlowNode source, DataFlowNode sink, Object tag) {
    super(source, sink, tag);
    setDotAttribute("label", "");
  }
}

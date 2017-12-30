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
 * copied and modified by Kris Peterson
 */
public class DependenceDataFlowEdge extends DependenceFlowEdge {
  /** 
   * An object that identifies the condition for which this
   * transition is taken
   */
  //Object _label;
   
  //does this edge effect the next iteration of the loop, or the current?
  
  /** 
   * Construct a new data-flow edge with no tag, no source, and no sink
   */
  public DependenceDataFlowEdge() {
    this(null);
  }
  
  /** 
   * Construct a new data-flow edge with the given tag, no source, and
   * no sink
   * 
   * @param tag 
   */
  public DependenceDataFlowEdge(Object tag) {
    super(tag);
    setDotAttribute("label", "");
    setDotAttribute("color", "blue");
    setIsDataEdge();
  }
  
  /** 
   * Construct a new data-flow edge with no tag from <code>source</code>
   * to <code>sink</code>
   * 
   * @param source 
   * @param sink 
   */
  public DependenceDataFlowEdge(DependenceFlowNode source, 
                                DependenceFlowNode sink) {
    this(source, sink, null);
  }
  
  /** 
   * Construct a new data-flow edge with the given tag from
   * <code>source</code> to <code>sink</code>
   */
  public DependenceDataFlowEdge(DependenceFlowNode source, 
                                DependenceFlowNode sink, Object tag) {
    super(source, sink, tag);
    setDotAttribute("label", "");
    setDotAttribute("color", "blue");
    setIsDataEdge();
    setLabel("Data Dependency Flow Edge: forwards pointing");
  }
  
  //public boolean getIsDataEdge(){return true;}
   
  /** 
   * Assigns the object that identifies the condition for which this
   * transition is taken
   * 
   * @param label 
   */
  public void setLabel(Object label) {
    _label = label;
    setDotAttribute("label", label == null ? "" : label.toString());
  }
}

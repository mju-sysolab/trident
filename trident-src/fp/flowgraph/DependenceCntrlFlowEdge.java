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
public class DependenceCntrlFlowEdge extends DependenceFlowEdge {
  /** 
   * An object that identifies the condition for which this
   * transition is taken
   */
  //Object _label;
   
   
  /** 
   * Construct a new data-flow edge with no tag, no source, and no sink
   */
  public DependenceCntrlFlowEdge() {
    this(null);
  }
  
  /** 
   * Construct a new data-flow edge with the given tag, no source, and
   * no sink
   * 
   * @param tag 
   */
  public DependenceCntrlFlowEdge(Object tag) {
    super(tag);
    setDotAttribute("label", "");
    setDotAttribute("color", "red");
  }
  
  /** 
   * Construct a new data-flow edge with no tag from <code>source</code>
   * to <code>sink</code>
   * 
   * @param source source node this edge is connected to
   * @param sink sink node this edge is connected to
   */
  public DependenceCntrlFlowEdge(DependenceFlowNode source, 
                                 DependenceFlowNode sink) {
    this(source, sink, null);
  }
  
  //public boolean getIsDataEdge(){return false;}
  
  /** 
   * Construct a new data-flow edge with the given tag from
   * <code>source</code> to <code>sink</code>
   * 
   * @param source source node this edge is connected to
   * @param sink sink node this edge is connected to
   * @param tag 
   */
  public DependenceCntrlFlowEdge(DependenceFlowNode source, 
                                 DependenceFlowNode sink, Object tag) {
    super(source, sink, tag);
    setDotAttribute("label", "");
    setDotAttribute("color", "red");
    setLabel("Control Dependency Flow Edge: forwards pointing");
  }
   
  
  /** 
   * Assigns the object that identifies the condition for which this
   * transition is taken
   * 
   * @param label text label of edge
   */
  public void setLabel(Object label) {
    _label = label + " is backward pointing? " + _isBackWardsPointing;
    setDotAttribute("label", label == null ? "" : label.toString());
  }
}

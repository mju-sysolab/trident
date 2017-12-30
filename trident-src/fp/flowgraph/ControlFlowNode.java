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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import fp.graph.Node;



/**
 * A node in a control-flow graph.  Each control-flow node corresponds to a
 * basic block in the procedure/method/function.
 *
 * @author Nathan Kitchen
 */
public class ControlFlowNode extends Node implements LoopNode {
  private DataFlowGraph _dataFlowGraph;

  ControlFlowNode immediate_dominator = null;
  LinkedList dominator_children = new LinkedList();
  HashSet dominated_set = new HashSet();

  private LoopNode _parent = null;
  private LoopNode _entry = null;
  private Set loop_nodes = new HashSet();
  
  int preorder = 0;
  int postorder = 0;
  int reverse_postorder = 0;
  
  
  /**
   * Construct a new control-flow node with no tag
   */
  public ControlFlowNode(){
    this(null);
  }

  /**
   * Construct a new control-flow node with the given tag
   */
  public ControlFlowNode(Object tag) {
    super(tag);
  }

  /**
   * Returns the data-flow graph for this node's basic block
   */
  public DataFlowGraph getDataFlowGraph() {
    return _dataFlowGraph;
  }

  /**
   * Assigns the data-flow graph for this node's basic block
   */
  public void setDataFlowGraph(DataFlowGraph dfg) {
    _dataFlowGraph = dfg;
  }

  public int getPreOrder() { return preorder; }
  void setPreOrder(int i) { preorder = i; }
  public int getPostOrder() { return postorder; }
  void setPostOrder(int i) { postorder = i; }
  public int getReversePostOrder() { return reverse_postorder; }
  void setReversePostOrder(int i) { reverse_postorder = i; }

  public void clearOrderInfo() {
    preorder = 0;
    postorder = 0;
    reverse_postorder = 0;
  }

  public ControlFlowNode idom() { return immediate_dominator; }
  public void setIdom(ControlFlowNode i) { immediate_dominator = i; }
  
  public Set getDominatedSet() { return dominated_set; }
  public boolean dominates(Node node) { return dominated_set.contains(node); }
  public void addDominated(Node node) { dominated_set.add(node); }

  public LinkedList children() { return dominator_children; }

  public void addChildren(Node child) { 
    for (Iterator it = getOutEdges().iterator(); it.hasNext();) {
      ControlFlowEdge edge = (ControlFlowEdge) it.next();
      if (child == ((Node)edge.getSink())) {
        dominator_children.addFirst(child);
        return;
      }
    }

    // if it is not a sucessor of B, then put at the end.
    // is this what is meant by non-sucessor?
    dominator_children.add(child);
  }

  public LoopNode loopParent() { return _parent; }
  public void     setLoopParent(LoopNode node) { _parent = node; }

  public boolean loopContains(LoopNode node) { 
    return loop_nodes.contains(node); 
  }
  public void addLoopNode(LoopNode node) { loop_nodes.add(node); }
 
  public LoopNode loopEntry() { return _entry; }
  public void     setLoopEntry(LoopNode node) { _entry = node; }

  public String toLoopDot() {
    return "hello";
  }



}

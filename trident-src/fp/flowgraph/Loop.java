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


package  fp.flowgraph;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

public class Loop implements LoopNode {
  
  public static int NORMAL = 0;
  public static int MULTIPLE_ENTRY = -1;
  public static int INFINITE = -2;
  public static int ROOT = 1;

  static String names[] = {"Infinite", "Multiple Entry", "", "Root"};

  private static int count = 0;

  private LoopNode _parent = null;
  private LoopNode _entry = null;
  private Set loop_nodes = new HashSet();

  private int _type;
  private String _name;
  private Set back_edges = new HashSet();
  
  public Loop(String name, int type) {
    _name = name;
    _type = type;
  }

  public Loop(int type) {
    this("Loop-"+count,type);
    count++;
  }

  public String getName() { return _name; }

  public int getType() { return _type; }

  public boolean isRoot() { return _type == ROOT; }
  public boolean isInfinite() { return _type == INFINITE; }
  public boolean isMultipleEntry() { return _type == MULTIPLE_ENTRY; }

  public Set getNodes() { return loop_nodes; }

  public LoopNode loopParent() { return _parent; }
  public void     setLoopParent(LoopNode node) { _parent = node; }

  public boolean loopContains(LoopNode node) { 
    return loop_nodes.contains(node); 
  }
  public void addLoopNode(LoopNode node) { loop_nodes.add(node); }

  public LoopNode loopEntry() { return _entry; }
  public void     setLoopEntry(LoopNode node) { _entry = node; }


  public Set getBackEdges() { return back_edges; }
  public void addBackEdges(Object o) { back_edges.add(o); }
  public void clearBackEdges() { back_edges.clear(); }
  public int getBackEdgeCount() { return back_edges.size(); }
  

  public String toLoopDot() {
    return "hello";
  } 

  public String toString() {
    StringBuffer buf = new StringBuffer(_name);
    buf.append(" : ").append(names[_type+2]).append("\n");
    buf.append("\tentry : ").append(loopEntry()).append("\n");
    buf.append("\tparent : ");
    LoopNode parent = loopParent();
    if (parent instanceof Loop) {
      buf.append(((Loop)parent).getName());
    } else {
      buf.append(parent);
    }
    buf.append("\n");
      
    StringBuffer node_buf = new StringBuffer("[");
    for(Iterator it=loop_nodes.iterator(); it.hasNext();) {
      LoopNode loop_node = (LoopNode) it.next();
      
      if (loop_node instanceof Loop)  {
	node_buf.append("Loop: ");
	node_buf.append(((Loop)loop_node).getName());
      } else  {
	node_buf.append("Block: ");
	node_buf.append(loop_node);
      }
      
      if (it.hasNext()) 
	node_buf.append(", ");
      else 
	node_buf.append("]");
    }

    buf.append("\tchildren : ").append(node_buf);
    return buf.toString();
  }
}




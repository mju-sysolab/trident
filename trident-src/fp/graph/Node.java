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


package fp.graph;

import fp.util.UniqueName;

import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Properties;

/**
* A node in a directed graph.  Each node may be tagged with an arbitary
* Object.
*
* Nodes may be assigned attributes which will be used when producing
* input files for AT&T's graph drawing program "dot."
  *
* @author Nathan Kitchen
* @author based on a package by Mike Wirthlin and Carl Worth
*/
public class Node extends AbstractGraphComponent {
  /**
  * Provides nodes with unique names for dot
    */
  static private UniqueName uniqueNamer = new UniqueName();
  
  /**
  * The name of this node in a dot file
  */
  private String _dotName;
   
  /**
  * Contains all the edges going out of this node, in other words, the
  * edges of which this node is the source
  */
  private Set _outEdgeSet;
  
  /**
  * Contains all the edges coming into this node, in other words, the
  * edges of which this node is the sink
  */
  private Set _inEdgeSet;
  
  //isSCC flags:
  boolean isSCC;
  boolean isSCCFlagValid;
  
  
  /**
  * Constructs a new node with no tag
  */
  public Node() {
    this(null);
  }
  
  /*
  * Constructs a new node with the given tag
  */
  public Node(Object tag) {
    super(tag);
    
    _outEdgeSet = newEdgeSet();
    _inEdgeSet = newEdgeSet();
    
    _dotName = uniqueNamer.getUniqueName("n_String");    
    isSCC = false;
    isSCCFlagValid = false;
  }
  
  /**
  * Returns a string to use as the name of this node when producing output
  * for dot.  No two nodes will ever have the same dot name.
    */
  public final String getDotName() {
    return _dotName;
  }
  
  /**
  * Returns the number of edges entering this node
  */
  public int getInDegree() {
    return _inEdgeSet.size();
  }
  
  /**
  * Returns a Set of all the edges entering this node
  */
  public Set getInEdges() {
    return _inEdgeSet;
  }
  
  public void setIsSCCFlagValid(boolean b){ isSCCFlagValid = b; }
  
  /**
  * Returns the number of edges leaving this node
  */
  public int getOutDegree() {
    return _outEdgeSet.size();
  }
  
  /**
  * Returns a Set of the edges leaving this node
  */
  public Set getOutEdges() {
    return _outEdgeSet;
  }
  
  /**
  * Instantiates a set to store edges in.  Subclasses may want to override
  * this.
  */
  protected Set newEdgeSet() {
    return new OrderedSet();
  }
  
  /**
  * Returns a representation of this node in the language used by AT&T's
  * graph drawing program "dot"
  */
  public String toDot() {
    StringBuffer buf = new StringBuffer(80);
    String dotName = getDotName();
    buf.append(dotName);
    if (getNumDotAttributes() > 0) {
      buf.append(' ').append(getDotAttributeList());
    } // end of if ()
    return buf.toString();
  }
   
  //added by kris
  //check if a node is a strongly connected component
  public boolean isSCC() {
    if(isSCCFlagValid)
      return isSCC;
    else {
      isSCCFlagValid = true;
      isSCC = isSCCRecursive(this, this, new ArrayList());
      return isSCC;
    }
  }
   
  //check if a node is a strongly connected component
  //check if this node is in the same SCC network as another node.
  //if it is possible to get from 1 to the other and back, then they
  //must be
  public boolean isInSameSCC(Node otherNode) {
    return ((isSCCRecursive(this, otherNode, new ArrayList())) &&
            (isSCCRecursive(otherNode, this, new ArrayList())));
  }
  
  //depth first search of directed graph. If the start node is ever found
  //then we must be in a SCC (we need to make sure we don't get caught in 
  //an infinite loop following an SCC through a different path
  //that doesn't lead back to startNode_Node; that's why I use the arraylist
  public boolean isSCCRecursive(Node startNode, Node currentNode,
                                ArrayList alreadyVisited) {
    Set outEdges = currentNode.getOutEdges();
    boolean flag = false;
    if(outEdges.size()>0){
      for (Iterator it2 = outEdges.iterator(); it2.hasNext() && 
        flag == false;) {
        Edge outEdge = (Edge)it2.next();
        if(outEdge.getSink() == startNode)
          return true;
        else if(!alreadyVisited.contains(outEdge.getSink())) {
          alreadyVisited.add(outEdge.getSink());
          flag = isSCCRecursive(startNode, outEdge.getSink(), alreadyVisited);
        }
      }
    }
    return flag;
     
  }
  
  /**
  * Returns a Set of the child nodes
  */
  public Set getChildNodes() {
    Set childNodes = new OrderedSet();
    Iterator iter = _outEdgeSet.iterator();
    while (iter.hasNext()) {
      childNodes.add(((Edge)iter.next()).getSink());
    }
    return childNodes;
  }
  /**
  * Returns a Set of the parent nodes
  */
  public Set getParentNodes() {
    Set parentNodes = new OrderedSet();
    Iterator iter = _inEdgeSet.iterator();
    while (iter.hasNext()) {
      Edge edgeTmp = (Edge)iter.next();
      parentNodes.add(edgeTmp.getSource());
    }
    return parentNodes;
  }
  
}

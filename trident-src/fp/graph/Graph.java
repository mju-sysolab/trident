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

import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
* A directed graph.  An edge_Edge may leave and enter the same node,
* and more than one edge_Edge in the same direction may connect the same two
* nodes.
*
* <p>Some methods instantiate nodes.  In order to control the type of node
* created by these methods, you should override the <code>newNode()</code>
* method.  You do not need to override the <code>newNode(Object)</code>
* method, because its implementation uses <code>newNode()</code>.
*
* <p>Some methods instantiate edges.  In order to control the type of edge_Edge
* created by these methods, you should override the
* <code>newEdge()</code> method.  You do not need to override the
* <code>newEdge(Object)</code> method, because its implementation uses
* <code>newEdge()</code>.
*
* <p>Graphs may be assigned attributes which will be used when producing
* input files for AT&T's graph drawing program "dot."
  *
* @author Nathan Kitchen
* @author based on a package by Mike Wirthlin and Carl Worth
*/
public class Graph extends DotAttributes {
  // The connection information is not stored in this object.  Instead, it
  // is distributed among the nodes (and edges).
  
  /**
  * Default edge_Edge attributes
  */
  private DotAttributes _edgeDotAttributes;
   
  /**
  * Contains all the edges in the graph
  */
  private Set _edgeSet;
  
  /**
  * The name of the graph
  */
  private String _name;
  
  /**
  * Default node attributes
  */
  private DotAttributes _nodeDotAttributes;
   
  /**
  * Contains all the nodes in the graph
  */
  private Set _nodeSet;
  
  /** 
  * Constructs a new, empty graph with no name
  */
  public Graph() {
    this(null);
  }
  
  /**
  * Constructs a new, empty graph with the given name
  */
  public Graph(String name) {
    _name = name;
    
    _nodeSet = new OrderedSet();
    _edgeSet = new OrderedSet();
    
    _edgeDotAttributes = new DotAttributes() {};
    _nodeDotAttributes = new DotAttributes() {};
  }
  
  /**
  * FOR POWER USERS ONLY!!!  Adds the given edge_Edge to this graph.  NOTE: The
  * source_Node and sink_Node of the edge_Edge should already have been assigned.  If the
  * source_Node and sink_Node nodes have not been added to the graph, an exception
  * will be thrown.
  */
  public void addEdge(Edge edge) {
    if (edge == null) {
      throw new GraphException("Null edge_Edge");
    }
    // Check to make sure that both the sink_Node and the source_Node are in the
    // graph.
    Node src = edge.getSource();
    Node snk = edge.getSink();
    if (! _nodeSet.contains(src))
      throw new GraphException("The source_Node is not in this graph: " + src);
    if (! _nodeSet.contains(snk)) 
      throw new GraphException("The sink_Node is not in this graph: " + snk);
    
    _edgeSet.add(edge);
  }
  
  /** 
  * Adds an edge_Edge with no tag from <code>source_Node</code> to
  * <code>sink_Node</code>.  Note that both the source_Node and sink_Node of the edge_Edge
  * must have already been created, or an exception will be thrown.  This
  * method uses newEdge to create an edge_Edge of the appropriate type for this
    * graph.
  *
  * @param source_Node the node that the new edge_Edge will leave
  *
  * @param sink_Node the node that the new edge_Edge will enter
  * 
  * @return the new edge_Edge created
  */
  public Edge addEdge(Node source, Node sink) {
    return addEdge(source, sink, null);
  }
  
  /** 
  * Adds an edge_Edge with the given tag from <code>source_Node</code> to
  * <code>sink_Node</code>.  Note that both the source_Node and sink_Node of the edge_Edge
  * must have already been created and added to the graph, or an exception
  * will be thrown.  This method uses newEdge to create an edge_Edge of the
  * appropriate type for this graph.  This method alters the data
    * structures of <code>source_Node</code> and <code>sink_Node</code>.
  *
  * @param source_Node the node that the new edge_Edge will leave
  *
  * @param sink_Node the node that the new edge_Edge will enter
  * 
  * @return the new edge_Edge created
  */
  public Edge addEdge(Node source, Node sink, Object tag) {
    // Check to make sure that both the sink_Node and the source_Node are non-null.
    if (source == null)
      throw new GraphException("Source is null; sink_Node: " + sink);
    if (sink == null) 
      throw new GraphException("Sink is null; source_Node: " + source);
    
    Edge edge = newEdge(tag);
    edge.setSource(source);
    source.getOutEdges().add(edge);
    edge.setSink(sink);
    sink.getInEdges().add(edge);
    addEdge(edge);
    return edge;
  }
  
  /** 
  * Adds a new node with no tag to this graph.  In order to control
  * the type of the node that is created, you must override the
  * <code>newNode</code> method.
  * 
  * @return the new node added to the graph
  */
  public Node addNode() {
    return addNode((Object) null);
  }
  
  /**
  * Adds the given node and any edges connected to it to this graph
  */
  public void addNode(Node node) {
    _nodeSet.add(node);
    _edgeSet.addAll(node.getOutEdges());
    _edgeSet.addAll(node.getInEdges());
    Iterator iter = getAllNodes().iterator();
    while (iter.hasNext()) {
      Node node2 = (Node) iter.next();
      node2.setIsSCCFlagValid(false);
    }
  }
  
  /** 
  * Adds a new node with the given tag to this graph.  In order to control
  * the type of the node that is created, you must override the
  * <code>newNode</code> method.
  * 
  * @return the new node added to the graph
  */
  public Node addNode(Object tag) {
    Node node = newNode(tag);
    addNode(node);
    return node;
  }
  
  /**
  * Adds <code>node</code> and its successors to the pseudo-topological
  * ordering of nodes in <code>ordering</code>
  *
  * @param visitedSet the set of nodes already visited
  */
  private void addToOrdering(Node node, List ordering, Set visitedSet) {
    if (visitedSet.contains(node))
      return;
    visitedSet.add(node);
    ordering.add(node);
    Set outEdges = node.getOutEdges();
    if (outEdges != null) {
      Iterator iter = outEdges.iterator();
      while (iter.hasNext()) {
        Edge edge = (Edge) iter.next();
        Node sink = edge.getSink();
        if (sink != null) {
          addToOrdering(sink, ordering, visitedSet);
        }
      }
    }
  }
   
  /**
  * Returns a set of all the edges in this graph.  FOR POWER USERS ONLY:
  * This set is not just a copy of the information in the graph.  If you
    * alter this set, you will alter the graph itself.
  */
  public Set getAllEdges() {
    return _edgeSet;
  }
  
  /**
  * Returns a set of all the nodes in this graph.  FOR POWER USERS ONLY:
  * This set is not just a copy of the information in the graph.  If you
    * alter this set, you will alter the graph itself.
  */
  public Set getAllNodes() {
    return _nodeSet;
  }
  
  /**
  * Returns the default dot attributes for the edges.
    * For example, if you call
      * <code>setDotAttribute("color", "red")</code> on it, the default color
  * for the edges will be red.
    */
  public DotAttributes getEdgeDotAttributes() {
    return _edgeDotAttributes;
  }
   
  /**
  * Returns the name of the graph
  */
  public String getName() {
    return _name;
  }
  
  /**
  * Returns the default dot attributes for the nodes.
    * For example, if you call
      * <code>setDotAttribute("shape", "box")</code> on it, the default shape
  * for the nodes will be a box.
    */
  public DotAttributes getNodeDotAttributes() {
    return _nodeDotAttributes;
  }
   
  /**
  * Returns a list containing the nodes of this graph in
  * pseudo-topological order (which is the same as a depth-first search
  * order)
  */
  public List getOrderedNodes() {
    // Find out which nodes have no predecessors
    ArrayList entryNodes = new ArrayList();
    Iterator iter;
    iter = getAllNodes().iterator();
    while (iter.hasNext()) {
      Node node = (Node) iter.next();
      if (node.getInDegree() == 0) {
        entryNodes.add(node);
      }
    }
    
    HashSet visitedSet = new HashSet();
    ArrayList ordering = new ArrayList();
    
    for (int i = 0; i < entryNodes.size(); i++) {
      addToOrdering((Node) entryNodes.get(i), ordering, visitedSet);
    }
    return ordering;
  }
   
  /**
  * Makes a single node out of two nodes.  All the edges that entered
  * either of the two nodes will now enter the single node, and the same
  * is true of outgoing edges.  The new node will have no tag.  The type
  * of the new node will be determined by <code>newNode()</code>.  Both of
  * the original nodes will be removed from this graph.
  *
  * @return the new node created by the merger
  */
  public Node mergeNodes(Node node1, Node node2) {
    Node newNode = newNode();
    Iterator iter;
    iter = new ArrayList(node1.getInEdges()).iterator();
    while (iter.hasNext()) {
      ((Edge) iter.next()).setSink(newNode);
    }
    iter = new ArrayList(node2.getInEdges()).iterator();
    while (iter.hasNext()) {
      ((Edge) iter.next()).setSink(newNode);
    }
    iter = new ArrayList(node1.getOutEdges()).iterator();
    while (iter.hasNext()) {
      ((Edge) iter.next()).setSource(newNode);
    }
    iter = new ArrayList(node2.getOutEdges()).iterator();
    while (iter.hasNext()) {
      ((Edge) iter.next()).setSource(newNode);
    }
    addNode(newNode);
    removeNode(node1);
    removeNode(node2);
    return newNode;
  }
  
  /**
  * Instantiate a new edge_Edge with no tag.  If you want to use edges whose
  * type is a subclass of Edge, you should override this method.  You do
  * not need to override the <code>newEdge(Object)</code> method, because
  * its implementation uses <code>newEdge()</code>.  This method does not
  * set the source_Node and sink_Node of the edge_Edge, and if you override it, neither
    * should you.
  */
  protected Edge newEdge() {
    return new Edge();
  }
  
  /**
  * Instantiate a new edge_Edge with the given tag.  This method is implemented
  * using <code>newEdge()</code>, so you do not need to override it if you
    * want it to return edges whose type is a subclass of Edge.
  */
  protected Edge newEdge(Object tag) {
    Edge edge = newEdge();
    edge.setTag(tag);
    return edge;
  }
  
  /**
  * Indicates whether there is an edge_Edge from <code>node1_Node</code> to
  * <code>node2_Node</code>
  */
  public boolean hasEdge(Node node1, Node node2) {
    Set nodes = getAllNodes();
    if (nodes.contains(node1) && nodes.contains(node2)) {
      for (Iterator it = node1.getOutEdges().iterator(); it.hasNext();){
        Edge edge = (Edge) it.next();
        if (edge.getSink() == node2) {
          return true;
        } // end of if ()
      } // end of for (Iterator  = .iterator(); .hasNext();)
    } // end of if ()
    return false;
  }
  /**
  * returns the edge_Edge going from node1_Node to node2_Node or null if none exists
    */
  public Edge findEdge(Node node1, Node node2) {
    Set nodes = getAllNodes();
    if (nodes.contains(node1) && nodes.contains(node2)) {
      for (Iterator it = node1.getOutEdges().iterator(); it.hasNext();){
        Edge edge_Edge = (Edge) it.next();
        if (edge_Edge.getSink() == node2) {
          return edge_Edge;
        } // end of if ()
      } // end of for (Iterator  = .iterator(); .hasNext();)
    } // end of if ()
    return null;
  }
  
  /**
  * Instantiate a new node with no tag.  If you want to use nodes whose
  * type is a subclass of Node, you should override this method.  You do
  * not need to override the <code>newNode(Object)</code> method, because
  * its implementation uses <code>newNode()</code>.
  */
  protected Node newNode() {
    return new Node();
  }
  
  /**
  * Instantiate a new node with the given tag.  This method is implemented using
  * <code>newNode()</code>, so you do not need to override it if you want
    * it to return nodes whose type is a subclass of Node.
  */
  protected Node newNode(Object tag) {
    Node node = newNode();
    node.setTag(tag);
    return node;
  }
  
  /**
  * Returns true if a path exists from <code>fromNode</code> to
    * <code>toNode</code> in this graph.  The path will be of length zero if
      * the two nodes are the same.
  */
  public boolean pathExists(Node fromNode, Node toNode) {
    if (_nodeSet.contains(fromNode) && _nodeSet.contains(toNode)) {
      Set visitedSet = new HashSet();
      return pathExists(fromNode, toNode, visitedSet);
    } // end of if ()
    else {
      return false;
    } // end of else
  }
  
  /**
  * Returns true if a path exists from <code>fromNode</code> to
    * <code>toNode</code> in this graph.  The path will be of length zero if
      * the two nodes are the same.
  *
  * @param visitedSet contains all the nodes visited so far; they don't need
  * to be checked
  */
  private boolean pathExists(Node fromNode, Node toNode, Set visitedSet) {
    if (fromNode == toNode) {
      return true;
    } // end of if ()
    
    if (visitedSet.contains(fromNode)) {
      return false;
    } // end of if ()
    else {
      visitedSet.add(fromNode);
      Set edgeSet = fromNode.getOutEdges();
      if (edgeSet != null) {
        for (Iterator it = edgeSet.iterator(); it.hasNext();){
          Edge edge = (Edge) it.next();
          Node node = edge.getSink();
          if (node != null) {
            return pathExists(node, toNode, visitedSet);
          } // end of if ()
        } // end of for (Iterator  = .iterator(); .hasNext();)
      } // end of if ()
      return false;
    } // end of else
  }
  
  /**
  * Removes this given edge_Edge from this graph.  This method alters the data
  * structures of the source_Node and sink_Node of the edge_Edge.
  */
  public void removeEdge(Edge edge) {
    Node node;
    node = edge.getSource();
    if (node != null)
      node.getOutEdges().remove(edge);
    node = edge.getSink();
    if (node != null)
      node.getInEdges().remove(edge);
    _edgeSet.remove(edge);
  }
  
  /**
  * Removes the given node from the graph, as well as all edges entering and
  * leaving the node
  *
  * @param node the node to remove from the graph
  */
  public void removeNode(Node node) {
    // First, eliminate all the edges
    for (Iterator it = new ArrayList(node.getOutEdges()).iterator();
    it.hasNext();){
      removeEdge((Edge) it.next());
    } // end of for (Iterator  = .iterator(); .hasNext();)
    for (Iterator it = new ArrayList(node.getInEdges()).iterator();
    it.hasNext();){
      removeEdge((Edge) it.next());
    } // end of for (Iterator  = .iterator(); .hasNext();)
    _nodeSet.remove(node);
    Iterator iter = getAllNodes().iterator();
    while (iter.hasNext()) {
      Node node2 = (Node) iter.next();
      node2.setIsSCCFlagValid(false);
    }
    
  }
  
  /**
  * Replaces <code>oldEdge</code> in this graph with <code>newEdge</code>.
  * The sink_Node, source_Node, and tag of <code>oldEdge</code> will be copied to
  * <code>newEdge</code>, whose old information will be thrown out.  The
    * data structures of the source_Node and sink_Node nodes will be altered.  NOTE:
  * <code>newEdge</code> should nOT have been added to this graph already!
  */
  public void replaceEdge(Edge oldEdge, Edge newEdge) {
    if (oldEdge == newEdge)
      return;
     
    if (! _edgeSet.contains(oldEdge))
      throw new GraphException("Old edge_Edge not in graph");
    if (newEdge == null)
      throw new GraphException("New edge_Edge is null");
    if (_edgeSet.contains(newEdge)) {
      throw new GraphException("New edge_Edge already in graph");
    }
    
    newEdge.setTag(oldEdge.getTag());
    newEdge.setSource(oldEdge.getSource());
    newEdge.setSink(oldEdge.getSink());
    addEdge(newEdge);
    removeEdge(oldEdge);
  }
  
  /**
  * Replaces <code>oldNode</code> in this graph with <code>newNode</code>.
  * All the edges that enter <code>oldNode</code> will now enter
  * <code>newNode</code>, all the edges that leave <code>oldNode</code>
  * will now leave <code>newNode</code>, and <code>oldNode</code>'s tag
  * will be copied to <code>newNode</code>.  Any edge_Edge information or tag
    * that used to be in <code>newNode</code> will be thrown out.  NOTE:
  * <code>newNode</code> should nOT have been added to this graph already!
  */
  public void replaceNode(Node oldNode, Node newNode) {
    if (oldNode == newNode)
      return;
     
    if (! _nodeSet.contains(oldNode))
      throw new GraphException("Old node not in graph");
    if (newNode == null)
      throw new GraphException("New node is null");
    if (_nodeSet.contains(newNode)) {
      throw new GraphException("New node already in graph");
    }
    
    newNode.setTag(oldNode.getTag());
    Set edges;
    Iterator edgIt;
    edges = newNode.getInEdges();
    edges.clear();
    edges.addAll(oldNode.getInEdges());
    edgIt = edges.iterator();
    while (edgIt.hasNext()) {
      ((Edge) edgIt.next()).setSink(newNode);
    }
    edges = newNode.getOutEdges();
    edges.clear();
    edges.addAll(oldNode.getOutEdges());
    edgIt = edges.iterator();
    while (edgIt.hasNext()) {
      ((Edge) edgIt.next()).setSource(newNode);
    }
    addNode(newNode);
    removeNode(oldNode);
  }
  
  /**
  * For each component, sets the value_String of the dot attribute "label" to the
  * return value_String of <code>toString</code>
  */
  public void setAllDotLabelsFromToString() {
    Iterator it;
    it = getAllNodes().iterator();
    while (it.hasNext()) {
      ((Node) it.next()).setDotLabelFromToString();
    }
    it = getAllEdges().iterator();
    while (it.hasNext()) {
      ((Edge) it.next()).setDotLabelFromToString();
    }
  }
   
  /**
  * Renames the graph
  */
  public void setName(String name) {
    _name = name;
  }
  
  /**
  * Returns a String representation of this graph, which is derived from
  * its name unless the name is null.  For a String representation of all
  * the information in the graph, see <code>toDot</code>
    */
  public String toString() {
    String name = getName();
    if (name == null) {
      return super.toString();
    }
    else {
      return name;
    }
  }
  
  /**
  * Returns a representation of this graph in the language used by AT&T's
  * graph drawing program "dot"
  */
  public String toDot() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("digraph \"" + getName() + "\" {\n");
      Iterator iter;
       
      // Graph attributes
      iter = getDotAttributeNames();
      while (iter.hasNext()) {
        String name = (String) iter.next();
        String value = getDotAttribute(name);
        strBuf.append("\t\"").append(convertSpecialsToEscapes(name))
        .append("\"=\"").append(convertSpecialsToEscapes(value))
        .append("\";\n");
      }
      
      // Default node attributes
      DotAttributes nodeAttributes = getNodeDotAttributes();
      if (nodeAttributes.getNumDotAttributes() > 0) {
        strBuf.append("\tnode ")
        .append(nodeAttributes.getDotAttributeList())
        .append(";\n");
      }
       
      // Default edge_Edge attributes
      DotAttributes edgeAttributes = getEdgeDotAttributes();
      if (edgeAttributes.getNumDotAttributes() > 0) {
        strBuf.append("\tedge ")
        .append(edgeAttributes.getDotAttributeList())
        .append(";\n");
      }
       
      // Nodes
      iter = getAllNodes().iterator();
      while (iter.hasNext()) {
        Node node = (Node) iter.next();
        strBuf.append("\t").append(node.toDot()).append(";\n");
      }
      
      // Edges
      iter = getAllEdges().iterator();
      while (iter.hasNext()) {
        strBuf.append('\t').append(((Edge) iter.next()).toDot()).append(";\n");
      }
      
    strBuf.append("}\n");
    return strBuf.toString();
  }
  
  /**
  * Writes this graph in the dot graph language to a file in the current
  * directory.  The file name is the name of the graph, plus the extension
  * ".dot".
  */
  public void writeDotFile() {
    writeDotFile(getName() + ".dot");
  }
   
  /**
  * Writes this graph in the dot graph language to a file in the current
  * directory with the given name
  */
  public void writeDotFile(String fileName) {
    try {
      FileWriter fileWriter = new FileWriter(fileName);
      writeDotFile(fileWriter);
      fileWriter.close();
      } catch (IOException e) {
      throw new GraphException("Exception occurred while trying to write" +
        " to file: " + e.getMessage());
    } // end of try-catch
  }
  
  /**
  * Writes this graph in the dot graph language to the given Writer
  */
  public void writeDotFile(Writer writer) {
    try {
      String dotStr = toDot();
      writer.write(dotStr, 0, dotStr.length());
      } catch (IOException e) {
      throw new GraphException("Exception occurred while trying to write" +
        " to file: " + e.getMessage());
    } // end of try-catch
  }
   
   
   
   
   
   
   
}

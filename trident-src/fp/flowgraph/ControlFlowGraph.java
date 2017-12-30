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
import fp.graph.Graph;
import fp.graph.Node;

import fp.hardware.*;

import java.io.*;
import java.util.*;

/**
 * A control-flow graph for a procedure/method/function.  Each node
 * represents a basic block.
 *
 * @author Nathan Kitchen
 */
public class ControlFlowGraph extends Graph {

  public ControlFlowNode ENTRY;
  public ControlFlowNode EXIT;
  
  
  private LoopNode _root_loop = null;

  /**
   * Constructs a new unnamed control-flow graph
   */
  public ControlFlowGraph() {
    this(null);
  }
  
  /**
   * Constructs a new named control-flow graph
   */
  public ControlFlowGraph(String name) {
    super(name);

    ENTRY = (ControlFlowNode)addNode();
    EXIT = (ControlFlowNode)addNode();
  }


  /**
   * Returns the set of nodes for the basic blocks that the
   * procedure/method/function can be exited from
   */
  public Set getExitNodes() {
    Set result = new HashSet();
    for(Iterator iter = EXIT.getInEdges().iterator(); iter.hasNext();){
      Edge edge = (Edge)iter.next();
      result.add(edge.getSource());
    }
    return result;
  }

  /**
   * Instantiates a ControlFlowEdge
   */
  protected Edge newEdge() {
    return new ControlFlowEdge();
  }

  /**
   * Instantiates a ControlFlowNode
   */
  protected Node newNode() {
    return new ControlFlowNode();
  }

  public void setRootLoop(LoopNode loop) { _root_loop = loop; }
  public LoopNode getRootLoop() { return _root_loop; }

  /*
    preorder - breadthfirst
    postorder - depthfirst

  */

  int preorder;
  int rpostorder;
  int postorder;

  public void markEdges() {
    //System.out.println("markEdges !!");
    preorder = 0;
    postorder = 0;
    rpostorder = getAllNodes().size();
    // set all preorder and rpostorder to 0;
    clearNodeOrder();
    
    depthFirstSearch(ENTRY);
  }
  
  private void clearNodeOrder() {
    for(Iterator iter=getAllNodes().iterator(); iter.hasNext(); ) {
      ControlFlowNode node = (ControlFlowNode)iter.next();
      node.setPreOrder(0);
      node.setPostOrder(0);
      node.setReversePostOrder(0);
    }
  }

  private void depthFirstSearch(ControlFlowNode node) {
    //System.out.println(" node "+((BlockNode)node).getName());
    node.setPreOrder(preorder++);
    
    //System.out.println(" node out edges "+node.getOutEdges());

    for (Iterator it = node.getOutEdges().iterator(); it.hasNext();){
      ControlFlowEdge edge = (ControlFlowEdge) it.next();
      ControlFlowNode sink = (ControlFlowNode) edge.getSink();

      //System.out.println(" edge "+edge);
      //System.out.println(" sink PreOrder "+sink.getPreOrder());

      if (sink.getPreOrder() == 0) {
        edge.setTreeEdge();
        depthFirstSearch(sink);
      } else if (sink.getReversePostOrder() == 0) {
        edge.setBackwardEdge();
      } else if (node.getPreOrder() < sink.getPreOrder()) {
        edge.setForwardEdge();
      } else {
        edge.setCrossEdge();
      }
      //System.out.println(" set edge "+edge+" is_forward "+edge.isForward());
    }
    node.setReversePostOrder(rpostorder--);
    node.setPostOrder(postorder++);
  }


  /**
   * Writes the data-flow graphs for all the nodes in this graph in the dot
   * graph language to a single file in the current directory.  The file
   * name is the name of the graph, plus the extension ".dot".
   */
  public void writeDataFlowDotFile() {
    writeDataFlowDotFile(getName() + ".dot", false);
  }
  
  /**
   * Writes the data-flow graphs for all the nodes in this graph in the dot
   * graph language to a single file in the current directory with the given name
   */
  public void writeDataFlowDotFile(String fileName) {
    writeDataFlowDotFile(fileName, false);
  }

  /**
   * Writes the data-flow graphs for this graph in the dot graph language
   * to the given Writer
   */
  public void writeDataFlowDotFile(Writer writer) {
    writeDataFlowDotFile(writer, false);
  }

  /**
   * Writes the data-flow graphs for all the nodes in this graph in the dot
   * graph language to a single file in the current directory, possible with
   * visible edges between related storage nodes.  The file name is the name
   * of the graph, plus the extension ".dot".
   *
   * @param connectStorageNodes true if you want visible edges between
   * related storage nodes
   */
  public void writeDataFlowDotFile(boolean connectStorageNodes) {
    writeDataFlowDotFile(getName() + ".dot", connectStorageNodes);
  }
  
  /**
   * Writes the data-flow graphs for all the nodes in this graph in the dot
   * graph language to a single file in the current directory with the given name
   *
   * @param connectStorageNodes true if you want visible edges between
   * related storage nodes
   */
  public void writeDataFlowDotFile(String fileName,
				   boolean connectStorageNodes) {
    try {
      FileWriter fileWriter = new FileWriter(fileName);
      writeDataFlowDotFile(fileWriter, connectStorageNodes);
      fileWriter.close();
    } catch (IOException e) {
      throw new ControlFlowException("Exception occurred while trying to " + 
				     "write to file: " + e.getMessage());
    } // end of try-catch
  }

  /**
   * Writes the data-flow graphs for this graph in the dot graph language
   * to the given Writer
   *
   * @param connectStorageNodes true if you want visible edges between
   * related storage nodes
   */
  public void writeDataFlowDotFile(Writer writer,
				   boolean connectStorageNodes) {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("digraph \"" + getName() + "\" {\n");
    Iterator iter;
    ControlFlowNode node;

    // We would like to list the subgraphs in a meaningful order, so we'll
    // use a pseudo-topological order.
    iter = getOrderedNodes().iterator();
    while (iter.hasNext()) {
      node = (ControlFlowNode) iter.next();
      DataFlowGraph dfg = node.getDataFlowGraph();
      StringBuffer dfgDotBuf;
      String blockLabel = node.getDotAttribute("label");
      if (dfg == null) {
	dfgDotBuf = new StringBuffer();
	dfgDotBuf.append("subgraph cluster_").append(node.getDotName()).append(" {}\n");
      }
      else {
	dfg.setName(blockLabel);
	String dfgDotStr = dfg.toDot();
	dfgDotBuf = new StringBuffer(dfgDotStr);
	// First line should be only "digraph NAME {"
	// Replace it
	int n = 0;
	while (dfgDotBuf.charAt(n) != '\n') {
	  n++;
	} // end of while ()
	// Label the block
	dfgDotBuf.insert(n + 1, "\tlabel=\"" + blockLabel + "\";\n");
	dfgDotBuf.replace(0, n + 1,
			  "subgraph cluster_" + node.getDotName() + " {");
      }
      strBuf.append(dfgDotBuf.toString());
      /*
      if (connectStorageNodes) {
	for (Iterator stItr = dfg.getAllNodes().iterator(); stItr.hasNext();){
	  DataFlowNode dnode = (DataFlowNode) stItr.next();
	  if (dnode instanceof StorageNode) {
	    StorageNode storeNode = (StorageNode) dnode;
	    for (Iterator infItr = storeNode.getGenerateSet().iterator();
		 infItr.hasNext();){
	      StorageNode.Info info = (StorageNode.Info) infItr.next();
	      strBuf.append('\t').append(info.node.getDotName())
		.append(" -> ").append(storeNode.getDotName())
		.append(" [ style=dashed ];\n");
	    } // end of for (Iterator  = .iterator(); .hasNext();)
	  } // end of if ()
     
	} // end of for (Iterator  = .iterator(); .hasNext()
      } // end of if ()
      */
    }

    strBuf.append("}\n");

    try {
      writer.write(strBuf.toString(), 0, strBuf.length());
    } catch (IOException e) {
      throw new ControlFlowException("Exception occurred while trying to " + 
				     "write to file: " + e.getMessage());
    } // end of try-catch
  }
}


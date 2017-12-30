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
import fp.graph.GraphException;
import fp.graph.Node;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;

/**
 * A data-flow graph for a basic block in a procedure/method/function
 *
 * @author Nathan Kitchen
 */
public class DataFlowGraph extends Graph {
  /**
   * The nodes with no incoming edges
   */
  private HashSet _topSources;

  /**
   * The nodes with no outgoing edges */
  private HashSet _bottomSinks;
  
  /**
   * Constructs a new unnamed data flow graph
   */
  public DataFlowGraph() {
    this(null);
  }
  
  /**
   * Constructs a new named data flow graph
   */
  public DataFlowGraph(String name) {
    super(name);
    _topSources = new HashSet();
    _bottomSinks = new HashSet();
  }

  /**
   * Returns the nodes that have no incoming edges.  If all the edges are
   * drawn to point down, these nodes are at the top of the graph.  NOTE:
   * This set is not just a copy.  If you alter it, you will alter the
   * graph itself.
   */
  public Set getTopSources() {
    return _topSources;
  }

  /**
   * Returns the nodes that have no outgoing edges.  If all the edges are
   * drawn to point down, these nodes are at the bottom of the graph.  NOTE:
   * This set is not just a copy.  If you alter it, you will alter the
   * graph itself.
   */
  public Set getBottomSinks() {
    return _bottomSinks;
  }

  /**
   * Returns the name of <code>cls</code>, which is simple except for arrays
   */
  private String getName(Class cls) {
    if (cls == null) {
      return "null";
    } // end of if ()
    else if (cls.isArray()) {
      return getName(cls.getComponentType()) + "[]";
    } // end of else if ()
    else {
      return cls.getName();
    } // end of else
  }

  /**
   * Instantiates a DataFlowNode
   */
  protected Node newNode() {
    return new DataFlowNode(null, null, false, false);
  }

  /**
   * Instantiates a DataFlowEdge
   */
  protected Edge newEdge() {
    return new DataFlowEdge();
  }

  /**
   * Also replaces <code>oldNode</code> in the set of top sources and bottom
   * sinks
   */
  public void replaceNode(Node oldNode, Node newNode) {
    super.replaceNode(oldNode, newNode);
    Set topSources = getTopSources();
    if (topSources.contains(oldNode)) {
      topSources.remove(oldNode);
      topSources.add(newNode);
    } // end of if ()
    Set bottomSinks = getBottomSinks();
    if (bottomSinks.contains(oldNode)) {
      bottomSinks.remove(oldNode);
      bottomSinks.add(newNode);
    } // end of if ()
  }

  /**
   * Replaces references to <code>oldNode</code> to references to
   * <code>newNode</code>.  An example of a node reference is the instance
   * node for an instance field node.
   */
  /*
  public void replaceNodeReferencesToWith(DataFlowNode oldNode,
					  DataFlowNode newNode) {
    for (Iterator nit = getAllNodes().iterator(); nit.hasNext();){
      Object o = nit.next();
      if (o instanceof ArrayOpNode) {
	if (((ArrayOpNode) o).getArrayNode() == oldNode) {
	  ((ArrayOpNode) o).setArrayNode(newNode);
	} // end of if ()
	if (o instanceof ArrayElementNode) {
	  ArrayElementNode aen = (ArrayElementNode) o;
	  if (aen.getIndexNode() == oldNode) {
	    aen.setIndexNode(newNode);
	  } // end of if ()
	  if (aen.getValueNode() == oldNode) {
	    aen.setValueNode(newNode);
	  } // end of if ()
	} // end of if ()
      } // end of if ()
      else if (o instanceof CallNode) {
	List argNodes = ((CallNode) o).getArgNodes();
	for (ListIterator it = argNodes.listIterator(); it.hasNext();){
	  if (it.next() == oldNode) {
	    it.set(newNode);
	  } // end of if ()
	} // end of for (Iterator  = .iterator(); .hasNext();)
	if (o instanceof InstanceMethodNode &&
	    ((InstanceMethodNode) o).getInstanceNode() == oldNode) {
	  ((InstanceMethodNode) o).setInstanceNode(newNode);
	} // end of if ()
      } // end of else if ()
      else if (o instanceof BinaryOpNode) {
	BinaryOpNode bon = (BinaryOpNode) o;
	if (bon.getLeftOperand() == oldNode) {
	  bon.setLeftOperand(newNode);
	} // end of if ()
	if (bon.getRightOperand() == oldNode) {
	  bon.setRightOperand(newNode);
	} // end of if ()
      } // end of else if ()
      else if (o instanceof UnaryOpNode) {
	UnaryOpNode uon = (UnaryOpNode) o;
	if (uon.getOperand() == oldNode) {
	  uon.setOperand(newNode);
	} // end of if ()
      } // end of else if ()
      else if (o instanceof ReturnNode) {
	ReturnNode rn = (ReturnNode) o;
	if (rn.getValueNode() == oldNode) {
	  rn.setValueNode(newNode);
	} // end of if ()
      } // end of else if ()
    } // end of for (Iterator  = .iterator(); .hasNext();)
  }
  */

  public void writeDotFile(String fileName, boolean rankNodes) {
    try {
      FileWriter fileWriter = new FileWriter(fileName);
      writeDotFile(fileWriter, rankNodes);
      fileWriter.close();
    } catch (IOException e) {
      throw new GraphException("Exception occurred while trying to write" +
	  " to file: " + e.getMessage());
    } // end of try-catch
  }

     /**
      * Writes this graph in the dot graph language to the given Writer
     */
  public void writeDotFile(Writer writer, boolean rankNodes) {
    try {
      String dotStr = toDot(rankNodes);
      writer.write(dotStr, 0, dotStr.length());
    } catch (IOException e) {
      throw new GraphException("Exception occurred while trying to write" +
	  " to file: " + e.getMessage());
    } // end of try-catch
  }

  /**
   * Appends the type of each node to its label before calling
   * <code>super.toDot()</code>
   */
  public String toDot() {
    return toDot(false);
  }

  public String toDot(boolean rankNodes) {
    Iterator iter = getAllNodes().iterator();
    StringBuffer sb = new StringBuffer();
    while (iter.hasNext()) {
      DataFlowNode node = (DataFlowNode) iter.next();
      sb.delete(0, sb.length());
      sb.append(node.getPrimaryDotLabel()).append('\n');
//      Class type = node.getType();
//      sb.append(getName(type));
      int line = node.getSourceLine();
      if (line != DataFlowNode.UNKNOWN_LINE) {
	sb.append('\n').append("line: ").append(line);
      } // end of if ()
//        String file = node.getSourceFile();
//        if (file != null) {
//  	sb.append('\n').append("file: ").append(file);
//        }
      node.setDotAttribute("label", sb.toString());
    }

    String dotString = super.toDot();
    
    if (!rankNodes) { 
      return dotString;
    }

    // Add on ranking information for instructions.
    // That is, each instruction that begins at the same clock tick should 
    // appear at the same level in the graph.

    // Find last brace, so we can insert ranking before it
    int lastIndex = dotString.lastIndexOf("}");
    dotString = dotString.substring(0, lastIndex-1);

    // Get the maximum clock count
    int maxClk = 0;
    for (Iterator it = getAllNodes().iterator(); it.hasNext();) {
      DataFlowNode node = (DataFlowNode)it.next();
      if (!node.isSourceOrSink()){
	Instruction inst = (Instruction)node.getTag();
	int clkCnt = inst.getExecClkCnt();
	if (clkCnt > maxClk) {
	  maxClk = clkCnt;
	}
      }
    }

    // Add clock count subgraph to the dotty string
    dotString = dotString + "{node [shape=plaintext, fontsize=16];\n";
    for (int i = 0; i < maxClk; i++) {
      dotString = dotString + i + " -> ";
    }
    dotString = dotString + maxClk + ";}\n";

    // Add ranking information
    // for each clock value from 0 to maxClk
    for (int i = 0; i <= maxClk; i++) {
      dotString = dotString + "{rank = same; " + i + "; ";

      // check what nodes have this clock value and print them out
      for (Iterator it = getAllNodes().iterator(); it.hasNext();) {
	DataFlowNode node = (DataFlowNode) it.next();
	if (!node.isSourceOrSink()) {
	  Instruction inst = (Instruction)node.getTag();
	  int clkCnt = inst.getExecClkCnt();
	  if (i == clkCnt) {
	    dotString = dotString + node.getDotName() + "; ";
	  }
	}
      }
      dotString = dotString + "}\n";
    }

    // put the final brace back on
    dotString = dotString + "}\n";
    return dotString;
  }
}

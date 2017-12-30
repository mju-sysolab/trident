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

import fp.graph.*;
import java.util.*;

/** 
 * A node in a data-flow graph, which represents a typed value.  The type
 * may be void.
 * 
 * @author Nathan Kitchen
 * copied and modified by Kris Peterson
 */
public class DependenceFlowNode extends Node {
  // This class depends on the Soot J*va Optimization Framework by the
  // Sable research group, even though it should be language-independent.
  // We made this decision because it was convenient.
   
  /** 
   * The type of this node
   */
  private Class _type;
  
  /** 
   * The label for this node in dot language will be this field with the type
   * appended
   */
  private String _primaryDotLabel;
  
  /** 
   * The name of the source file for this node
   */
  private String _sourceFile;
   
  /** 
   * The line of source code
   */
  private int _sourceLine;
   
   
  /** instruction for node
   */
  private Instruction _instruction;
  private InstructionList.InstructionObject _instObj;
   
  /** 
   * Constructs a new data-flow node with the given type and no tag
   */
  public DependenceFlowNode(Class type){
    this(type, null);
  }
  
  /** 
   * Constructs a new data-flow node with the given type and tag
   */
  public DependenceFlowNode(Class type, Object tag) {
    super(tag);
    setType(type);
    setSourceLine(UNKNOWN_LINE);
  }
  
  /** save an Instruction to this node
   * 
   * @param i_Instruction instruction to save to this node
   */
  public void setInstruction(Instruction i){ _instruction = i;}
  /** return Instruction from this node
   * 
   * @return returns Instruction from this node
   */
  public Instruction getInstruction(){ return _instruction;}
  /** save an Instruction to this node
   * 
   * @param i_Instruction instruction to save to this node
   */
  public void setInstructionObj(InstructionList.InstructionObject i){ 
    _instObj = i;
  }
  /** return Instruction from this node
   * 
   * @return returns Instruction from this node
   */
  public InstructionList.InstructionObject getInstructionObj(){ 
    return _instObj;
  }
  
  
  /** 
   * Returns the label for this node that will be used when producing dot
   * language output.  The type of this node is only appended at that time.
   */
  public String getPrimaryDotLabel() {
    return _primaryDotLabel;
  }
  
  /** 
   * The value used for the source line when it is unknown
   * 
   * @see #getSourceLine
   * @see #setSourceLine
   */
  public static final int UNKNOWN_LINE = -1;
   
  /** 
   * Returns the name of the file that contains the source code for this graph
   * (may be null)
   */
  public String getSourceFile() {
    return _sourceFile;
  }
  
  /** 
   * Returns the line in the source code for this node
   * 
   * @see #UNKNOWN_LINE
   */
  public int getSourceLine() {
    return _sourceLine;
  }
   
  /** 
   * Returns the type of this node
   */
  public Class getType() {
    return _type;
  }
  
  /** 
   * Returns a set that only accepts DependenceFlowEdges
   */
  private class BobTheSpecialHash extends HashSet {
    public boolean add(Object o) {
      if (! (o instanceof DependenceCntrlFlowEdge ||
  	     o instanceof DependenceDataFlowEdge )) {
  	throw new DependenceFlowException("Not a DependenceFlowEdge: " + o 
        				  + " (" + o.getClass() + ")");
      } // end of if ()
      return super.add(o);
    }
  }
  protected Set newEdgeSet() {
    BobTheSpecialHash bob = new BobTheSpecialHash(); 
    return bob;
  }

  private class NodeCarrier {
   public DependenceFlowNode n;
   public NodeCarrier() {}
  }
  
  /** This function was written to help in determining if there is not a 
   *  recursive connection over a loop boundary between an ASTORE and an ALOAD.  
   *  To perform this check, the relationship between the two index variables 
   *  over iterations of the loop must be determined.  First a path from indvar 
   *  to the other array's index is found and then the path from indvar back to 
   *  the backedge crossing over to the last iteration of the loop. If either of 
   *  these paths is not found, this function returns false.
   * 
   * @param otherNode node defining other node's index variable (indvar's 
   *     definition is this)
   * @param backEdgeToNode1Path path from back edge to indvar
   * @param node1ToNode2Path path from indvar to other node
   * @return if the paths were found
   */
  public boolean findPathsForRecursiveArrayCheck(DependenceFlowNode loadNode, 
                                                 ArrayList backEdgeToLoopEnd, 
                                                 ArrayList loopEndToLoad, 
						 ArrayList loopEndToStore) {
    /*return ((getNode2BackToNode1Path(this, otherNode, 
                                     node1ToNode2Path, new ArrayList())) &&
            (getBackEdgeToNode1Path(this, backEdgeToNode1Path, 
	                            new ArrayList())));*/
    //System.out.println("this " + this);
    //System.out.println("loadNode " + loadNode);
    //Node loopEnd = null;
    NodeCarrier loopEnd = new NodeCarrier();
    return (this.backEdgeToStore(backEdgeToLoopEnd, loopEndToStore, loopEnd) &&
	    loopEnd.n.getNode2BackToNode1Path(loadNode, loopEndToLoad, 
	                                      new ArrayList()));
  }
  
  public boolean backEdgeToStore(ArrayList backEdgeToLoopEnd,
                                 ArrayList loopEndToStore, NodeCarrier loopEnd) {
  
    //ArrayList pathTemp = new ArrayList();
    if(!this.getBackEdgeToNode1Path(loopEndToStore, new ArrayList(), loopEnd))
      return false;
    Node node;
    //int i = 0;
    do {
      node = (Node)loopEndToStore.get(0);
      loopEndToStore.remove(0);
      backEdgeToLoopEnd.add(0, node);
    }while((node != loopEnd.n) && (0<loopEndToStore.size()));
    
    return true;
  
  } 
  
  //depth first search of directed graph. 
  /** recursive method for finding the path backwards from currentNode to 
   *  startNode
   * 
   * @param startNode target node
   * @param currentNode current node
   * @param path path so ollowed so far
   * @param alreadyVisited list of already visited nodes. this is used to
   *      prevent getting stuck in infinite loops
   * @return whether a path was found
   */
  public boolean getNode2BackToNode1Path(DependenceFlowNode currentNode,
                                         ArrayList path, 
					 ArrayList alreadyVisited) {
    //Set inEdges = currentNode.getInEdges();
    boolean flag = false;
    if(getInEdges().size()>0){
      for (Iterator it2 = getInEdges().iterator(); it2.hasNext() && 
           flag == false;) {
        Edge inEdge = (Edge)it2.next();
	if(((DependenceFlowNode)inEdge.getSource()).getInstruction()==null)
	  continue;
        if(inEdge.getSource() == this)
          return true;
        else if(!alreadyVisited.contains(inEdge.getSource())) {
	  ArrayList pathtmp = (ArrayList)path.clone();
	  pathtmp.add(0, inEdge.getSource());
          alreadyVisited.add(inEdge.getSource());
          //System.out.println("path b4 " + pathtmp);
	  flag = this.getNode2BackToNode1Path(((DependenceFlowNode)inEdge.getSource()), 
	                                      pathtmp, alreadyVisited);
          //System.out.println("path after " + pathtmp);
	  path.clear();
	  path.addAll(pathtmp);
        }
      }
    }
    return flag;
     
  }
  
  /** recursive method for finding the path backwards from currentNode to a back 
   *  edge
   * 
   * @param currentNode current node
   * @param path path so ollowed so far
   * @param alreadyVisited 
   * @return whether a path was found
   */
  public boolean getBackEdgeToNode1Path(ArrayList path, 
					ArrayList alreadyVisited, 
					NodeCarrier loopEnd) {
	  //System.out.println("currentNode " + currentNode);
    //Set inEdges = currentNode.getInEdges();
    boolean flag = false;
    if(getInEdges().size()>0){
      for (Iterator it2 = getInEdges().iterator(); it2.hasNext() && 
           flag == false;) {
        Edge inEdge = (Edge)it2.next();
	
	if((((DependenceFlowNode)inEdge.getSource()).getInstruction()==null)||
	   (ALoad.conforms(((DependenceFlowNode)inEdge.getSource()).getInstruction()))||
	   (AStore.conforms(((DependenceFlowNode)inEdge.getSource()).getInstruction())))
	  continue;
        if(((DependenceFlowEdge)inEdge).getisBackWardsPointing()) {
	  //System.out.println("back pointing inEdge.getSource() " + inEdge.getSource());
          loopEnd.n = ((DependenceFlowNode)inEdge.getSource());
	  //System.out.println("loopEnd " + loopEnd);
	  return true;
        }
	else if(!alreadyVisited.contains(inEdge.getSource())) {
	  ArrayList pathtmp = (ArrayList)path.clone();
	  pathtmp.add(0, inEdge.getSource());
	  //System.out.println("inEdge.getSource() " + inEdge.getSource());
          alreadyVisited.add(inEdge.getSource());
          flag = ((DependenceFlowNode)inEdge.getSource()).getBackEdgeToNode1Path(pathtmp,  
	                                                                         alreadyVisited, 
	                                                                         loopEnd);
	  path.clear();
	  path.addAll(pathtmp);
        }
      }
    }
    return flag;
     
  }
  
  /** 
   * Assigns the label for this node that will be used when producing dot
   * language output.  The type of this node is only appended at that time.
   * 
   * @param label 
   */
  public void setPrimaryDotLabel(String label) {
    _primaryDotLabel = label;
  }
  
  /** 
   * Assigns the name of the file that contains the source code for this graph
   */
  public void setSourceFile(String sourceFile) {
    _sourceFile = sourceFile;
  }
  
  /** 
   * Assigns the line in the source code for this node
   * 
   * @see #UNKNOWN_LINE
   */
  public void setSourceLine(int line) {
    _sourceLine = line;
  }
  
  /** 
   * Sets the type of this node
   * 
   * @param type 
   */
  public void setType(Class type) {
    _type = type;
  }
  
  /** 
   * Uses <code>getPrimaryDotLabel</code>
   */
  public String toString() {
    String primary = getPrimaryDotLabel();
    if (primary == null) {
      return super.toString();
    } // end of if ()
    else {
      return primary;
    } // end of else
  }
}

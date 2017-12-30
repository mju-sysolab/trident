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



package fp.passes;

import fp.flowgraph.*;
import fp.graph.Edge;
import fp.graph.Node;
import fp.graph.GraphException;
import java.util.*;

public class VerifyBlockGraph extends Pass implements GraphPass {

  /*
    The purpose of this pass is to:
    1. Ensure that all edges out of the block go some where (same with
    	in edges)

    2. Make sure all branches have corresponding edges (?)

    3. Check block has instructions in it.

    4. Make sure non-exit blocks have at least one outgoing edge.

  */
  

  private String _name = null;
  static private int count = 0;


  public VerifyBlockGraph(PassManager pm) {
    super(pm);
  }

  public VerifyBlockGraph(PassManager pm, String s) {
    this(pm);
    _name = s;
  }

  public boolean optimize(BlockGraph g) {
    String debug = "";
    String count_string = "";

    BlockNode bn;
    Instruction i = null;
    Set allNodes = g.getAllNodes();
    Set reachedNodes = new HashSet();

    // go thru all nodes 
    for (Iterator it = g.getAllNodes().iterator(); it.hasNext(); ) {
      bn = (BlockNode) it.next();
      Set outEdges = bn.getOutEdges();
      Set inEdges = bn.getInEdges();
      int outDegree = bn.getOutDegree();
      int edgeCount = 0;

      try {
	// check for criteria 1
	if (bn == g.ENTRY) {
	  if (outDegree < 1) {
	    throw new GraphException("Entry outdegree is less than one.");
	  }
	  if (bn.getInDegree() > 0) {
	    throw new GraphException("Entry indegree is greater than zero.");
	  }
	  for (Iterator it2 = outEdges.iterator(); it2.hasNext();) {
	    Edge e = (Edge)it2.next();
	    Node sink = e.getSink();
	    if (!allNodes.contains((BlockNode)sink)) {
	      throw new GraphException("Cannot find sink node in edge from entry node");
	    }
	    reachedNodes.add(sink);
	  }
	  reachedNodes.add(g.ENTRY);
	  continue;
	}

	for (Iterator it2 = inEdges.iterator(); it2.hasNext();) {
	  Edge e = (Edge)it2.next();
	  Node source = e.getSource();
	  if (!allNodes.contains((BlockNode)source)) {
	    throw new GraphException("Cannot find source node in edge to current node");
	  }
	}

	if (bn == (BlockNode)g.EXIT) {
	  if (outDegree > 0) {
	    throw new GraphException("Exit outdegree is greater than zero.");
	  }
	  if (bn.getInDegree() < 1) {
	    throw new GraphException("Exit indegree is less than one.");
	  }
	  continue;
	}

	if (outDegree < 1) {
	  throw new GraphException ("Non-exit block node must have at least one edge out");
	}

	ArrayList iList = bn.getInstructions();

	// check for criteria 3
	if  (iList.size() == 0) {
	  throw new GraphException ("Non-exit/entry block node must have at least one instruction");
	}

	// check for criteria 2 and 1 
	for (Iterator it2 = bn.getInstructions().iterator(); it2.hasNext(); ) {
	  i = (Instruction) it2.next();
	  BlockNode targetBlockNode = null; 
	  LabelOperand target = null;
	  switch (i.operator().opcode) {
	    case Operators.BR_opcode:
	      edgeCount = edgeCount + 2;
	      target = Branch.getTarget1(i);

	      if ((targetBlockNode=g.findBlock(target)) == null) {
		throw new GraphException ("Cannot find block for branch true label");
	      } 
	      if (!g.hasEdge(bn, targetBlockNode)) {
		throw new GraphException("Cannot find edge to branch true block"); 
	      } 
	      reachedNodes.add(targetBlockNode);

	      target = Branch.getTarget2(i);

	      if ((targetBlockNode=g.findBlock(target)) == null) {
		throw new GraphException ("Cannot find block for to branch false label");
	      } 
	      if (!g.hasEdge(bn, targetBlockNode)) {
		throw new GraphException("Cannot find edge to branch false block"); 
	      }
	      reachedNodes.add(targetBlockNode);
	      break;

	    case Operators.GOTO_opcode:
	      edgeCount = edgeCount + 1;
	      target = Goto.getTarget(i);

	      if ((targetBlockNode=g.findBlock(target)) == null) {
		throw new GraphException("Cannot find block for Goto target label");
	      } 
	      if (!g.hasEdge(bn, targetBlockNode)) {
		throw new GraphException("Cannot find edge to goto target"); 
	      } 
	      reachedNodes.add(targetBlockNode);
	      break;

	    case Operators.RET_opcode:
	      edgeCount = edgeCount + 1;
	      targetBlockNode = (BlockNode)g.EXIT;

	      if (!g.hasEdge(bn, targetBlockNode)) {
		throw new GraphException("Cannot find edge to return EXIT block"); 
	      } 
	      reachedNodes.add(targetBlockNode);
	      break;

	    case Operators.SWITCH_opcode:
	      target = Switch.getDefault(i);

	      if ((targetBlockNode=g.findBlock(target)) == null) {
		throw new GraphException("Cannot find block for switch default target"); 
	      }
	      if (!g.hasEdge(bn, targetBlockNode)) {
		throw new GraphException("Cannot find edge to switch default target"); 
	      } 
	      reachedNodes.add(targetBlockNode);

	      int c = 0;
	      // now check each case label edge
	      for (c = 0;  Switch.hasCase(i, c); c++) {
		target = Switch.getCaseLabel(i, c);
		if ((targetBlockNode=g.findBlock(target)) == null) {
		  throw new GraphException("Cannot find block for switch case target:  " + c); 
		}
		if (!g.hasEdge(bn, targetBlockNode)) {
		  throw new GraphException("Cannot find edge to switch case target:  " + c); 
		} 
		reachedNodes.add(targetBlockNode);
	      }
	      edgeCount = edgeCount + c + 1;
	      break;

	    default:
	      break;
	  }
	}

	// case where graph may have more outgoing edges than can be
	// discerned from the instructions in the block	
	if (edgeCount != outDegree) {
	  throw new GraphException("Edges traversed from block does not match its outdegree");
	}

      } catch (GraphException e) {
	System.err.println("Error with block: [" + bn.toString() + "]"); 
	throw(e);
      }

    }

    try {
      if (reachedNodes.size() != allNodes.size()) {
	throw new GraphException("Mismatch between number of block nodes traversed and number in graph");
      }

    } catch (GraphException e) {
      System.err.println("Error in graph:  "); 
      throw(e);
    }
    return(true);
  }


  public String name() { 
    return "VerifyBlockGraph";
  }

  // ??
  public static int incrementCount() { return count++; }


}


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

import java.util.*;

import fp.flowgraph.BlockGraph;
import fp.flowgraph.BlockNode;
import fp.flowgraph.BlockEdge;
import fp.flowgraph.BooleanOperand;
import fp.flowgraph.Branch;
import fp.flowgraph.Operand;


import fp.util.BooleanEquation;


public class AddPredicates extends Pass implements GraphPass {
  private BlockGraph _current_graph;

  public AddPredicates(PassManager pm) {
    super(pm);
  }


  /*
    This pass will require that Switch statements be converted into
    blocks.  Otherwise I cannot make the assumption about 
    node.getOutDegree() <= 2.

    This is important to note.

  */
  
  public boolean optimize(BlockGraph graph) {
    _current_graph = graph;
    
    //BlockNode source = (BlockNode)graph.getEntryNode(); // ?

    // should this be a pass?
    //graph.markEdges();
    //source.clearMark();

    recurseAddEdgePredicates((BlockNode)graph.ENTRY);

    /*
    ((BlockEdge)source.getInEdges().iterator().next()).setPredicate(new BooleanEquation(true));
    */

    addNodePredicates(graph);
    _current_graph = null;

    return true;
  }

  public void setup() { }

  public String name() {
    return "AddPredicates";
  }

  private LinkedList other_passes = new LinkedList();
  public LinkedList requires() {
    return other_passes;
  }

  

  /**
   * This method is supposed to go through all of the vertices and merge those 
   * which have the same source but different predicates.
   **/

  void recurseAddEdgePredicates(BlockNode node) {
    //System.out.println("RecurseAddEdgePredicates "+node.getName());
    // collect all of the predicates from the forward edges
    BooleanEquation terms = new BooleanEquation();
    for (Iterator it = node.getInEdges().iterator(); it.hasNext();){
      BlockEdge edge = (BlockEdge) it.next();
      // if there is null forward edge, wait until it is assigned, return now
      if( edge.isForward() && edge.getPredicate() == null ) {
        return;
      // OR the terms of the forward edge to the new boolean equation
      } else if( edge.isForward() ) {
        terms.or( edge.getPredicate() );
      }
    } // end of for (Iterator  = .iterator(); .hasNext();)
    
    // if there were no terms coming in, it must be the first pred and is true
    if( terms.isFalse() ) {
      terms.setTrue();
    }

    boolean hasBranchLabel; // ??
    BlockEdge edge = null;
    Object label = null;

    if (node.getOutDegree() > 0) {
      edge = (BlockEdge) node.getOutEdges().iterator().next();
      label = edge.getLabel();
    } // end of if ()

    hasBranchLabel = (Boolean.TRUE.equals(label) ||
                      Boolean.FALSE.equals(label));
    // if there is just one output, just append the current equation
    if(node.getOutDegree() == 1 /* && ! hasBranchLabel */ ) {
      //System.out.println( "outDegree == 1" );
      edge.setPredicate( terms );
      // recurse down the edge if it is a forward edge
      BooleanEquation cloned_eq = (BooleanEquation)terms.clone();
      if( edge.isForward() ) {
        recurseAddEdgePredicates( (BlockNode)edge.getSink() );
      }
    } // end of if ()
    // if there are two outputs, append the predicate and recurse down
    else if( node.getOutDegree() <= 2 ) {
      //System.out.println( "outDegree == 2" );
      // get the predicate from each edge, AND it, and recurse down
      for (Iterator it = node.getOutEdges().iterator(); it.hasNext();){
        edge = (BlockEdge) it.next();
        Boolean blabel = (Boolean) edge.getLabel();

        BooleanOperand predOp = getPredicateOperand(node);
	//System.out.println("Predicate operand "+predOp+" "+blabel);

	BooleanEquation pred = new BooleanEquation(predOp, 
						   blabel.booleanValue());
        BooleanEquation cloned_eq = (BooleanEquation)terms.clone();

	//System.out.println(" cloned_eq "+cloned_eq);
        cloned_eq.and( pred );
	//System.out.println(" cloned_eq "+cloned_eq);

        edge.setPredicate( cloned_eq );
        // recurse down if forward
        if( edge.isForward() ) {
          recurseAddEdgePredicates( (BlockNode)edge.getSink() );
        }
      } // end of for (Iterator  = .iterator(); .hasNext();)
    }
    
    // if there are more than two edges at this point, there is a problem
    // this assumes that switches have been already removed ...
    else if( node.getOutDegree() > 2 ) {
      String msg = "This node has more than 2 edges on a node. " +
        "This condition shouldn't happen now.";
      System.err.println( msg );
      System.exit( 1 );
    }
  }


  BooleanOperand getPredicateOperand(BlockNode node) {
    // we can assume that the statement is last.  Perhaps if it is not
    // we can go looking for it.  There should be only one at this time.
    //System.out.println(" Decision "+node.getDecision());
    return Branch.getBoolean(node.getBranch());
  }
    

  void addNodePredicates(BlockGraph graph) {
    for (Iterator nIt = graph.getAllNodes().iterator(); nIt.hasNext();) {
      BlockNode node = (BlockNode) nIt.next();

      //System.out.println(" Node : "+node.getName());

      BooleanEquation terms = new BooleanEquation();
      for (Iterator eIt = node.getInEdges().iterator(); eIt.hasNext();){
        BlockEdge edge = (BlockEdge) eIt.next();
        // OR all of the terms together from each edge
        if( edge.isForward() ) {
	  //System.out.println(" Edge "+edge);
	  //System.out.println(" predicate "+edge.getPredicate());
	  if (edge.getPredicate() != null) {
	    terms.or( edge.getPredicate() );
	  }
        }
      } // end of for (Iterator  = .iterator(); .hasNext();)
      // at this point there are no false edges
      if( terms.isFalse() ) {
        terms.setTrue();
      }
      // set the predicate
      node.setPredicates( terms );
    } // end of for (Iterator  = .iterator(); .hasNext();)
  }

    


} 
  

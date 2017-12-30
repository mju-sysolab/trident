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


public class MergeParallelBlocks extends Pass implements GraphPass {

  public MergeParallelBlocks(PassManager pm) {
    super(pm);
  }

  public boolean optimize(BlockGraph graph) {

    BlockNode source = (BlockNode)graph.ENTRY;
    
    /// give a magic number a name
    boolean parent_is_decision = false;

    // merge parallel vertices
    // should we invent some "visitor style" classes and 
    // have a recursive visit ?
    recurseMergeBlocks(graph, source, parent_is_decision);
    // do this before ? or everyone always fixes them
    graph.resetMarkers(); 


    return true;
  }

  public void setup() { }

  public String name() {
    return "MergeParallelBlocks";
  }

  private LinkedList other_passes = new LinkedList();
  public LinkedList requires() {
    return other_passes;
  }

  

  /**
   * This method is supposed to go through all of the vertices and merge those 
   * which have the same source but different predicates.
   **/

  BlockNode recurseMergeBlocks(BlockGraph graph,
			       BlockNode node,
			       boolean parent_is_decision ) {

    //System.out.println("recurseParallelMergeVertices: vertex = " 
    //+ node.getLabel());
    // base case, the node has been seen before
    
    if( node.isMarked()) {
      return node;
    }
    // mark this vertex as visited
    node.setMark();

    // 
    //if( node.containsConditionalBranch() && node.getOutDegree() == 2 ) {
    if (node.getOutDegree() == 2) {
      //System.out.println("decision and 2");

      // get the out edges
      BlockEdge merged_edge = null;
      Iterator out_edge_it = node.getOutEdges().iterator();
      
      BlockEdge edge1 = (BlockEdge) out_edge_it.next();
      BlockEdge edge2 = (BlockEdge) out_edge_it.next();
      
      // merge all lower vertices
      BlockNode merged_node = null;
      BlockNode node1 = recurseMergeBlocks(graph,
					   (BlockNode)edge1.getSink(), true);
      BlockNode node2 = recurseMergeBlocks(graph,
					  (BlockNode)edge2.getSink(), true);

      // if both edges go to the same node (See Merge3Test) combine them
      if (node1 == node2) {
        graph.mergeEdges(node1);
        edge1 = (BlockEdge)node.getOutEdges().iterator().next();
        // merge up
        if (node1.isMergeable()) {
          //System.out.println("merge up1");
	  graph.mergeSerial(node, edge1, node1);
          graph.mergeNode(node, node1);
        }
      } else if( node1.isMergeable() && node2.isMergeable() ) {
      // now merge the deeper node into the more shallow one
        if( node1.getDepth() < node2.getDepth() ) {
	  // I think I will change this to be graph code.
          graph.mergeParallel(node2, node1 );       
          graph.mergeNode( node2, node1 );
          merged_edge = edge2;
          merged_node = node2;
        } else {
	  graph.mergeParallel(node1, node2);
          graph.mergeNode( node1, node2 );
          merged_edge = edge1;
          merged_node = node1;
        }
        // merge up
        if( merged_node.isMergeable() ) {
          //System.out.println("merge up2");
	  graph.mergeSerial(node, merged_edge, merged_node );
          graph.mergeNode( node, merged_node );
        }

      }
      //} else if (node.containsConditionalBranch() && node.getOutDegree() == 1) {
    } else if (node.getOutDegree() == 1 && node != graph.ENTRY) {
      //System.out.println("decision and 1");
      // get the out edge
      BlockEdge edge1 = (BlockEdge)node.getOutEdges().iterator().next();

      BlockNode node1 = recurseMergeBlocks(graph,
					   (BlockNode)edge1.getSink(), true);
      // merge all lower vertices
      if (node1.isMergeable()) {
        //System.out.println("merge up3");
	graph.mergeSerial(node, edge1, node1);
        graph.mergeNode(node, node1);
      }
    }
    // now go to the next vertices and check
    for (Iterator it = node.getOutEdges().iterator(); it.hasNext();){
      BlockEdge out = (BlockEdge) it.next();
      recurseMergeBlocks(graph, (BlockNode)out.getSink(), false );
    }
    // finally return the collapsed node
    
    return node;
  }



} 
  

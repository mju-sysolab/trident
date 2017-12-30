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


public class MergeSerialBlocks extends Pass implements GraphPass {

  static int print_num = 0;

  public MergeSerialBlocks(PassManager pm) {
    super(pm);
  }

  public boolean optimize(BlockGraph graph) {
    return optimize(graph, false);
  }

  public boolean optimize(BlockGraph graph, boolean ignore_channels) {
    serialMergeVertices(graph, ignore_channels);

    graph.resetMarkers();
    return true;
  }

  public String name() { return "MergeSerialBlocks"; }

   /** 
   * This method merges vertices starting from above down to the next one.
   * The criterion for merging is simple :P--A vertex cannot be removed if it
   * has an edge back to itself, it is a channel vertex, it has no output, or
   * it has more than one input UNLESS each of those inputs comes from the
   * same vertex above.
   */
  private void serialMergeVertices(BlockGraph graph, boolean ignore_channels) {
    //System.out.println( "PSSAGraph.serialMergeVertices()");
    // get all of the vertices in the control flow graph and iterate over
    // them

    Set replacedVertices = new HashSet();
    Iterator vIt = new ArrayList(graph.getAllNodes()).iterator();
    boolean getNext = true;
    BlockNode curr_node = null;
    while (vIt.hasNext()) {
      if (getNext) {
        curr_node = (BlockNode)vIt.next();
      } // end of if ()
      getNext = true;
      
      if (replacedVertices.contains(curr_node)) {
        continue;
      } // end of if ()
      
      curr_node.setMark(); // why if we never look ?

      //graph.writeDotFile( ++print_num+"serial.dot" );
      //System.out.println(" writing "+print_num+"serial.dot");
      
      // ??
      // if this is a leaf or channel vertex don't remove it
      /*
      if( curr_node.isChannelVertex() && !ignore_channels) {
        //System.out.println( curr_vertex + " not mergeable");
        continue;
      }
      */

      // do I need to keep these?
      if (curr_node == graph.ENTRY) continue;
      if (curr_node == graph.EXIT) continue;

      
      // now look down all of the out edges
      for (Iterator outIt = new ArrayList(curr_node.getOutEdges()).iterator();
           outIt.hasNext();){
        BlockEdge out_edge = (BlockEdge) outIt.next();
        BlockNode next_node = (BlockNode)out_edge.getSink();
        
        //System.out.println( "next_vertex: " + next_vertex );

        // don't remove if this is a loopback edge or a channel or a leaf
        if( next_node == curr_node 
	    || (next_node == graph.ENTRY) 
	    || (next_node == graph.EXIT) 
	    /* || (next_node.isChannelVertex() && !ignore_channels) */ ) {
          //System.out.println( next_vertex + " not mergeable");
          continue;
        }
        
        //System.out.println(" next_vertex.getInDegree() "+next_vertex.getInDegree());

        // if the in-degree is greater than one, check if same source
        if( next_node.getInDegree() > 1 ) {
          //BooleanEquation eq = new BooleanEquation();
          Set in_edges = next_node.getInEdges();
          BlockNode prev_node = null;
          boolean different_sources = false;

          for (Iterator inIt = in_edges.iterator(); inIt.hasNext();){
            BlockEdge in_edge = (BlockEdge)inIt.next();
            if( prev_node == null || prev_node == in_edge.getSource() ) {
              prev_node = (BlockNode)in_edge.getSource();
              //eq.orTerms( in_edge.getPredicate() );
            } else {
              different_sources = true;
            }
          }
          // if there are different sources, leave this one in
          if( different_sources ) {
            continue;
          }
          // else, remove all of the edges
          else {
            for (Iterator eIt = new ArrayList(in_edges).iterator();
                 eIt.hasNext();){
              BlockEdge in_edge = (BlockEdge) eIt.next();
              graph.removeEdge( in_edge );
            }
          }     
        }
        //next_node.orTerms( eq );
        graph.removeEdge( out_edge );
        
        if (replacedVertices.contains(next_node)) {
          continue;
        } 

        graph.mergeSerial( curr_node, out_edge, next_node );
        graph.mergeNode( curr_node, next_node );
        replacedVertices.add(next_node);
        //System.out.println(" Adding "+next_node+" to replaced vertices ");
        getNext = false;
        //graph.writeDotFile( ++print_num+"serialmerged.dot" );
        //System.out.println(" writing "+print_num+"serialmerged.dot");
      }
    }
  }
}


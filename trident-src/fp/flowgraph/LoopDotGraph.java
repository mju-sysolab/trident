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
import fp.graph.Node;
import fp.graph.Graph;

import java.util.*;
import java.io.*;

public class LoopDotGraph extends Graph {
  

public LoopDotGraph(LoopNode entry, String name) {
  setName(name);
  buildLoopDotGraph((Node)null, entry);
}

void buildLoopDotGraph(Node parent, LoopNode loopNode) {
  Node n = null;

  //if loop, create node LOOP
  if (loopNode instanceof Loop) {
    n = new Node("LOOP: " + ((Loop)loopNode).getName());
    n.setDotAttribute("shape", "ellipse");
  } else {
  //if block, create node BLOCK
    n = new Node("BLOCK: " + ((BlockNode)loopNode).getName());
    n.setDotAttribute("shape", "box");
  }

  //set its label
  n.setDotLabelFromToString();

  //add Node to graph
  addNode(n);

  //connect to parent (if there is one)
  if (parent != null) {
    addEdge(parent, n);
  }

  //get children
  if (loopNode instanceof Loop) {
    Set children = ((Loop)loopNode).getNodes();

    for(Iterator it=children.iterator(); it.hasNext();) {
      LoopNode nextChild = (LoopNode) it.next();

      //buildGraph for each child
      buildLoopDotGraph(n, nextChild);
    }
  }
}

}


				  

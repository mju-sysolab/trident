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
import fp.util.*;
import java.util.*;

public class PrintDataFlow extends Pass implements BlockPass {

  /*
    The purpose of this pass is to create a dataflow graph for each 
    block in the block graph.  The "IN" nodes have edges with primals 
    as labels to instruction nodes that use the primals.  From there, 
    defined values are on the outgoing edges to the next instruction 
    node, until finally primals are on edges to "OUT" nodes after they are stored.
    You can give a ranking parameter to tell it to place nodes that are
    scheduled at a given clock cycle on the same level in the graph.
    If you are ranking them, you need to place this pass after a scheduling
    pass.
  */
  

  private String _name = null;
  private boolean _rankNodes = false;

  public PrintDataFlow(PassManager pm) {
    super(pm);
  }

  public PrintDataFlow(PassManager pm, String s, boolean rankNodes) {
    this(pm);
    _name = s;
    _rankNodes = rankNodes;
  }
  
  public PrintDataFlow(PassManager pm, String s) {
    this(pm);
    _name = s;
    _rankNodes = false;
  }

  public boolean optimize(BlockNode bn) {
    String debug = "";
    String count_string = "";

    ArrayList iList = bn.getInstructions();
    UseHash useHash = bn.getUseHash();

    // Create a data flow graph with the name of this block
    DataFlowGraph dfg = new DataFlowGraph(_name + bn.getLabel().getName());
    DataFlowNode fromNode, toNode = null;
    DataFlowEdge dfe = null;

    // get all operands used
    Set allUses = useHash.keySet();

    // hashmap of an input and the node it came from
    HashMap inputsHash = new HashMap();
    
    // hashmap of a result and the instruction (node) it came from
    HashMap resultsHash = new HashMap();

    // temp
    HashMap tempHash = null;

    // hashmap of an instruction and the data flow node representing it
    HashMap instHash = new HashMap();


    // sort through and just get primals 
    for (Iterator it = allUses.iterator(); it.hasNext(); ) {
      Operand op = (Operand)it.next();
      //System.out.println("Considering operand: " + op.getName());
      if (op.isPrimal()) {
	//System.out.println("Considering primal: " + ((PrimalOperand)op).getName());
	
	// make a node from the primal
	fromNode = new DataFlowNode(Instruction.class, true, false);
	fromNode.setPrimaryDotLabel("IN");
	dfg.addNode(fromNode);
	inputsHash.put(op, fromNode);
      }
    }
    
    // start with the inputs and then keep building dataflow from there --
    // one level for each iteration of the while loop    
    while (!inputsHash.isEmpty()) {
      for (Iterator it = inputsHash.keySet().iterator(); it.hasNext();) {
	Operand op = (Operand)it.next();
        fromNode = (DataFlowNode)(inputsHash.get(op));

	// special case:  if the operand is a primal and it is a sink, then
	// make an "OUT" node and don't look for more uses of it.
	if (op.isPrimal() && (!fromNode.isSource())) {
	  toNode = null;

	  //check if it already has an out node
	  boolean hasOutNode = false;

	  for (Iterator it2 =  dfg.getAllNodes().iterator(); it2.hasNext()&&!hasOutNode;) {
	    toNode = (DataFlowNode)it2.next();
	    if (toNode.isSink() && dfg.hasEdge(fromNode, toNode)) {
	      hasOutNode = true;
	    }
	  }
	
	  // If it doesn't already have an out node, make one 
	  if (!hasOutNode) { 
	    toNode = new DataFlowNode(Instruction.class, false, true);
	    toNode.setPrimaryDotLabel("OUT");
	    dfg.addNode(toNode);
	    dfe = new DataFlowEdge(fromNode, toNode, ((PrimalOperand)op).getName()); 
	    dfe.setDotAttribute("label", ((PrimalOperand)op).toString());
	    dfg.addEdge(dfe);
	  }
	  continue;  // don't look for uses of this primal
	}

        // get the instructions that use it 
	ArrayList instList = (ArrayList)useHash.get(op);
        //System.out.println("Considering use instruction list for operand: " + op.getName());
        if (instList != null) {
	  for (Iterator it2 = instList.iterator(); it2.hasNext();) {
	    Instruction inst = (Instruction)it2.next();
	    //System.out.println("Instruction list: " + inst.toString());

	    // first check if instruction already has node for it
	    toNode = (DataFlowNode)instHash.get(inst);
	    if (toNode == null) {
	      toNode = new DataFlowNode(inst.getClass(), inst, false, false);
	      toNode.setPrimaryDotLabel(inst.toDotLabel());
	      dfg.addNode(toNode);
	      instHash.put(inst, toNode);
	    }

	    // first check if there is already an edge
	    if (!dfg.hasEdge((DataFlowNode)(inputsHash.get(op)), toNode)) {
	      dfe = new DataFlowEdge(fromNode, toNode, op.getName()); 
	      dfe.setDotAttribute("label", op.toString());
	      dfg.addEdge(dfe);
	    }

	    // Add all results of primal use to the results hashmap
	    int numDefs = inst.getNumberOfDefs();
	    for(int i = 0; i < numDefs; i++) {
	      Operand defOp = inst.getOperand(i);
	      if (defOp != null) {
		resultsHash.put(defOp, toNode);
	      }
	    }
	  }
	}
      }

      // set the inputs to the results for the next iteration 
      inputsHash.clear(); // don't need this information anymore
      tempHash = inputsHash;
      inputsHash = resultsHash;
      resultsHash = tempHash;
    }

    // Don't make a dotty file if the graph is empty
    if (dfg.getAllNodes().isEmpty()) {
      return true;
    }

    // this could include the whole path
    String file_name = dfg.getName();

    // we need to get just the file basename and path from it
    int index = file_name.lastIndexOf(System.getProperty("file.separator"));
    String basename = file_name.substring(index+1);
    String path = file_name.substring(0, index+1);

    // now create the changed file name
    file_name = path + debug + basename + count_string + ".dot";

    dfg.writeDotFile(file_name, _rankNodes);
    // should this be static ???
    // this should ask pm about a verbosity level ...
    if (pm.getVerbose() >= PassManager.VERBOSE_L2)
      System.err.println("writing "+file_name);

    return true;
  }

  public String name() { 
    return "PrintDataFlow";
  }


}


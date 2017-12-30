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
import fp.util.UniqueName;
import java.util.*;

public class SwitchIfConvert extends Pass implements GraphPass {

  /*
    The purpose of this pass is to convert switch statements to a series of
    if-then statements (conditional branches).
  */
  

  private String _name = null;
  static private int count = 0;
  private final static UniqueName _names = new UniqueName();


  public SwitchIfConvert(PassManager pm) {
    super(pm);
  }

  public SwitchIfConvert(PassManager pm, String s) {
    this(pm);
    _name = s;
  }

  public boolean optimize(BlockGraph g) {
    String debug = "";
    String count_string = "";
    
    if (_name != null) {
      debug = _name;
      count_string = "_"+(count++);
    }

    BlockNode bn;
    Set reachedNodes = new HashSet();
    
    // get all of the block nodes in the cfg
    Set allNodes = g.getAllNodes();

    // Copy the block nodes into a list--we can't iterate over them directly, or
    // we won't be able to add new block nodes to the graph.
    List bnList = new ArrayList(g.getAllNodes());
    for (int i = 0; i < bnList.size(); i++) {
      bn = (BlockNode) bnList.get(i);
      
      //Searching for a switch.
      Instruction switch_i = findSwitchStatementOperand(bn);
      
      // If we don't have a switch -- keep going.
      if (switch_i == null) {
	continue;
      }

      // Need to know where to go if everything is false.
      BlockNode default_bn 
	= findDefaultBlockNodeAndRemoveExtraEdges(bn, g);

      // The last case to deal with.
      BlockNode last_bn = addNewIfBlockNodes(bn, switch_i, g);

      // must add special edge ...  The last one.
      ((ControlFlowEdge)g.addEdge(last_bn, default_bn)).setLabel(Boolean.FALSE);

    } // end block node iteration

    return true;
  }
  
  /**
    find a switch statement in the current block node.  If there is none, 
    this function returns null.
   */

  private Instruction findSwitchStatementOperand(BlockNode bn) {
    List iList = bn.getInstructions();
    ListIterator iter = iList.listIterator();
    Instruction switch_i = null;
      
    //Searching for a switch.
    while(iter.hasNext()) {
      Instruction i = (Instruction)iter.next();
      if (i.operator().opcode == Operators.SWITCH_opcode) {
	switch_i = i;
	break;
      } // end of if ()
    }
    
    return switch_i;
  }

  /**
    findDefaultBlockNodeAndRemoveExtraEdges may seem like too much in one
    step, but basically we are looking for the default edge and any other 
    edges that point to the default block node are superfluous.  We get there
    using the default case.  This thinking may not be as fast as other
    methods, since the cases that are obviously default will not be 
    handled faster -- so if you want a case to be handled faster -- make 
    it a specific case.
  */
  private BlockNode findDefaultBlockNodeAndRemoveExtraEdges(BlockNode bn, BlockGraph g) {
    BlockNode default_bn = null;
    Set outEdges = bn.getOutEdges();
    // Find a default edge
    for (Iterator edgeIt = outEdges.iterator(); edgeIt.hasNext();){
      BlockEdge edge = (BlockEdge) edgeIt.next();
      if (edge.getLabel().equals("default")) {
	default_bn= (BlockNode) edge.getSink();
	break;
      } // end of if ()
    }

    if (default_bn != null) {
      // Remove excess edges
      for (Iterator it = new ArrayList(outEdges).iterator(); it.hasNext();){
	BlockEdge edge = (BlockEdge) it.next();
	if (edge.getSink() == default_bn) {
	  g.removeEdge(edge);
	} // end of if ()
      } // end of for (Iterator  = .iterator(); .hasNext();)
    } // end of if ()

    return default_bn;
  }

  private BlockNode addNewIfBlockNodes(BlockNode curr_bn, Instruction switch_i, BlockGraph g) {
    Set outEdges = curr_bn.getOutEdges();

    BlockNode new_bn = null;
    int counter = 0;

    curr_bn.removeInstruction(switch_i);

    for (Iterator outIt = new ArrayList(outEdges).iterator();
	outIt.hasNext();){ 
      BlockEdge edge = (BlockEdge) outIt.next();

      BlockNode sink_bn = (BlockNode) edge.getSink();

      // as early as possible
      g.removeEdge(edge);

      //create seteq instruction and add to curr_bn
      BooleanOperand boolOp = Operand.newBoolean(_names.getUniqueName("%bool"));
      Instruction seteqI = Test.create(Operators.SETEQ, Type.Bool, boolOp, 
	  Switch.getTest(switch_i), Switch.getCaseValue(switch_i, counter));
      curr_bn.addInstruction(seteqI);

      //If not last case, create a block (new_bn) for false edge
      LabelOperand labOp;
      if (outIt.hasNext()) {
	new_bn = new BlockNode();
        String new_bn_name = _names.getUniqueName("%converted.switch.to.if");
	new_bn.setName(new_bn_name);
	g.addNode(new_bn);
	labOp = Operand.newLabel(new_bn_name);
      } else {
	labOp = Switch.getDefault(switch_i);
      }

      //if its the last case, then the false edge goes to the default

      //create branch instruction and add to curr_bn
      Instruction branchI = Branch.create(Operators.BR, boolOp, 
	  Switch.getCaseLabel(switch_i, counter), labOp);
      curr_bn.addInstruction(branchI);


      //connect curr_bn true edge to next case
      //connect curr_bn false edge to new_bn if there is one,
      //otherwise it gets connected to default node back in the calling routine
      ((ControlFlowEdge)g.addEdge(curr_bn, sink_bn)).setLabel(Boolean.TRUE);
      if (outIt.hasNext()) {
	((ControlFlowEdge)g.addEdge(curr_bn, new_bn)).setLabel(Boolean.FALSE);
	curr_bn = new_bn;
      }

      counter++;
    } // end of for (Iterator  = .iterator(); .hasNext();)
    
    return curr_bn;
  }


  public String name() { 
    return "SwitchIfConvert";
  }

  // ??
  public static int incrementCount() { return count++; }


}


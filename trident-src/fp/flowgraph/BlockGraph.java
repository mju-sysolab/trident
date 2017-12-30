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
import fp.hardware.*;
import fp.parser.LlvmLexer;
import fp.parser.LlvmParser;
import fp.util.BooleanEquation;
import fp.util.BEqToList;
import fp.util.DefHash;
import fp.util.UniqueName;
import fp.util.UseHash;

import antlr.TokenStreamException;
import antlr.RecognitionException;

import java.util.*;
import java.io.*;

public class BlockGraph extends ControlFlowGraph {
  

  // Data
  private String _source;
  private String _function;
  private BlockNode _firstBlock; // the one the entry block will point to

  private UniqueName _variableNamer;

  private HashMap _variable_hash;

  //save chip info:
  //===> moved to GlobalOptions.java
  //private ChipDef _chipDef;
  private float _aveOpsPCycle;
  private float _maxOpsPCycle;
  private float _minOpsPCycle;
  private float _opsPerBlock;
  private float _cyclesPerBlock;
  private int _totOps;
  private int _cycleCount;
  private HashMap _operatorCounts;


  // this may only be for testing...
  public BlockGraph() {
    this("onTheFly", "run", false, false);
  }

  public BlockGraph(String source, String function) {
    this(source, function, true, true);
  }

  public BlockGraph(String source, String function, boolean parse, 
		    boolean connect) {
    super(source);
    _source = source;
    _function = function;
    
    _variableNamer = new UniqueName();

    _variable_hash = new HashMap();

    setDotAttribute("label", _source + ":" + _function);
    
    ((BlockNode)ENTRY).setName("GlobalEntry");
    ((BlockNode)EXIT).setName("GlobalExit");
    
    /*
      scheduler statistics for design
    */
    _aveOpsPCycle = 0;
    _maxOpsPCycle = 0;
    _minOpsPCycle = 0;
    _opsPerBlock = 0;
    _cyclesPerBlock = 0;
    _totOps = 0;
    _cycleCount = 0;
    _operatorCounts = new HashMap(); 


    /*
      All the work of building the control-flow block is to be done
      here.

    */
    if (parse) {
      //System.out.println("Parsing llv file.");
      parseFile();
      //System.out.println("Done Parsing llv file.");
    }

    if (connect)
      connectBlocks();

    //cleanUp(something);

    // addPredicates();

  }


  private void parseFile() {
    InputStream s = null;
    try {
      s = new FileInputStream(_source);
    } catch (IOException e) {
      // I should probably invent my own set of execptions.
      System.err.println("Exception occured when reading " + _source);
      e.printStackTrace();
      System.exit(-1);
    }

    LlvmLexer lLexer = new LlvmLexer(s);
    LlvmParser lParser = new LlvmParser(lLexer);
    // this is where we would pass in "this"
    try {
      lParser.module(this); 
    } catch (TokenStreamException e) {
      System.err.println("Error parsing file "+ _source);
      System.exit(-1);
    } catch (RecognitionException e) {
      System.err.println("Error parsing file "+ _source);
      System.exit(-1);
    }
  }

  public void setSource(String s) { 
    _source = s; 
    setDotLabel();
  }

  public void setFunction(String f) { 
    _function = f; 
    setDotLabel();
  }

  //scheduler stats get and set methods:
  public void setAveOpsPCycle(float i) { _aveOpsPCycle = i; }
  public float getAveOpsPCycle() { return _aveOpsPCycle; }
  public void setMaxOpsPCycle(float i) { _maxOpsPCycle = i; }
  public float getMaxOpsPCycle() { return _maxOpsPCycle; }
  public void setMinOpsPCycle(float i) { _minOpsPCycle = i; }
  public float getMinOpsPCycle() { return _minOpsPCycle; }
  public void setOpsPerBlock(float i) { _opsPerBlock = i; }
  public float getOpsPerBlock() { return _opsPerBlock; }
  public void setCyclesPerBlock(float i) { _cyclesPerBlock = i; }
  public float getCyclesPerBlock() { return _cyclesPerBlock; }
  public void setTotOps(int i) { _totOps = i; }
  public int getTotOps() { return _totOps; }
  public void setCycleCount(int i) { _cycleCount = i; }
  public int getCycleCount() { return _cycleCount; }
  public void setOperatorCounts(HashMap i) { _operatorCounts = i; }
  public HashMap getOperatorCounts() { return _operatorCounts; }


  public void setFirstBlock(BlockNode bn) {
    _firstBlock = bn;
  }

  private void setDotLabel() {
    setDotAttribute("label", _source + ":" + _function);
  }

  protected Node newNode() {
    return new BlockNode();
  }

  protected Edge newEdge() {
    return new BlockEdge();
  }

  // convenience method for clearing all block merkers.
  public void resetMarkers() {
    for(Iterator it = getAllNodes().iterator(); it.hasNext(); ) {
      ((BlockNode) it.next()).clearMark();
    }
  }

  
  // eventually this should be private 
  protected void addPredicates() {
    //do this as a pass

  }
    

  public void addVariable(Variable var) {
    _variable_hash.put(var.getOperand(), var);
  }

  public Variable getVariable(Operand op) {
    return (Variable)_variable_hash.get(op);
  }

  public boolean hasVariable(Operand op) {
    return getVariable(op) != null;
  }

  public boolean hasExternalVariable(Operand op) {
    Variable var = getVariable(op);
    if (var != null) 
      return var.isExternal();
    else
      return false;
  }

  public ArrayList getExternalOperandNames() {
    ArrayList external = new ArrayList();
    // For each variable in this blockgraph...
    for(Iterator it = _variable_hash.values().iterator(); it.hasNext(); ) {
      Variable var = (Variable) it.next();
      if(var.isExternal())
	external.add(var.getOperand().getFullName());
    }
    return external;
  }

  public HashMap getVariableHash() { return _variable_hash; }

  public void printVariables() {
    Variable var = null;
    if (!_variable_hash.isEmpty()) {
      System.out.println("*************BEGIN VARS**************************");
      for (Iterator it = _variable_hash.values().iterator(); it.hasNext();) {
	var = (Variable)it.next();
	if (var.isExternal()) {
	  System.out.print("External Variable: ");
	} else {
	  System.out.print("Global Variable: ");
	}
	System.out.println(var.toString());
      }
      System.out.println("**************END VARS*************************");
    }
  }


  // Kris's ChipInfo
  //===> moved to GlobalOptions.java
  /*public void saveChipInfo(int sliceCnt, float percentUsage) {
    _chipDef = new ChipDef(sliceCnt, percentUsage);
  }
  
  public ChipDef getChipInfo() { return _chipDef; }
  */

  /*
   *  BLOCK CODE *
   */

  private void addBlocks(Object something) {
    
  }
 
  public BlockNode findBlock(LabelOperand labelOperand) {
    BlockNode bn;
    String labelName = labelOperand.toString();
    // System.out.println("Finding target: " + labelName);
    for (Iterator it = getAllNodes().iterator(); it.hasNext(); ) {
      bn = (BlockNode) it.next();
      if (bn.getName().compareTo(labelName) == 0) {
	return bn;
      }
    }
    return null;
  }


  private void connectBlocks() {
    BlockNode bn;

    // first connect first block to ENTRY node
    if (_firstBlock == null) {
      System.err.println("Cannot find first block in block graph.");
      return;
    }
    ((ControlFlowEdge)addEdge((BlockNode)ENTRY, _firstBlock)).setLabel(Boolean.TRUE);

    // go thru all nodes and look at last instruction
    for (Iterator it = getAllNodes().iterator(); it.hasNext(); ) {
      bn = (BlockNode) it.next();
      //System.out.println("Connecting node: " + bn.getName());

      if ((bn == ENTRY) || (bn == EXIT)) {
	continue;
      }

      ArrayList iList = bn.getInstructions();

      if  (iList.size() == 0) {
	System.err.println("BlockNode has 0 instructions.");
      }

      Instruction lasti = (Instruction)iList.get(iList.size()-1);

      if (!lasti.isTerminator()) {
	System.err.println("Last instruction in block is not terminator.");
	return;
      }

      // Depending on what terminator instruction, connect block and label edge 
      BlockNode targetBlockNode = null; 
      LabelOperand target = null;

      if (lasti.isUnconditionalBranch()) {
	//System.out.println("Connecting Goto...");
	target = Goto.getTarget(lasti);

	if ((targetBlockNode=findBlock(target)) != null) {
	  ((ControlFlowEdge)addEdge(bn, targetBlockNode)).setLabel(Boolean.TRUE);
	} else System.err.println("Cannot find goto target");

      } else if (lasti.isConditionalBranch() && lasti.isVariableUses()) {
	//System.out.println("Connecting Switch...");

	// first connect default edge
        target = Switch.getDefault(lasti);
	if ((targetBlockNode=findBlock(target)) != null) {
	  ((ControlFlowEdge)addEdge(bn, targetBlockNode)).setLabel("default");
	} else System.err.println("Cannot find block for switch default target");

	// now connect each case label edge
	for (int c = 0;  Switch.hasCase(lasti, c); c++) {
	  target = Switch.getCaseLabel(lasti, c);
	  if ((targetBlockNode=findBlock(target)) != null) {
	    ConstantOperand caseValue = Switch.getCaseValue(lasti, c);
	    ((ControlFlowEdge)addEdge(bn, targetBlockNode)).setLabel(caseValue.getName());
	  } else System.err.println("Cannot find block for switch case: " + c );
	}
      } else if (lasti.isConditionalBranch()) {
	//System.out.println("Connecting Branch...");
	target = Branch.getTarget1(lasti);

	if ((targetBlockNode=findBlock(target)) != null) {
	  ((ControlFlowEdge)addEdge(bn, targetBlockNode)).setLabel(Boolean.TRUE);
	} else System.err.println("Cannot find block for branch true target");

	target = Branch.getTarget2(lasti);

	if ((targetBlockNode=findBlock(target)) != null) {
	  ((ControlFlowEdge)addEdge(bn, targetBlockNode)).setLabel(Boolean.FALSE);
	} else System.err.println("Cannot find block for branch false target");

      } else if (lasti.isReturn()) {
	//System.out.println("Connecting Return...");
	((ControlFlowEdge)addEdge(bn, (BlockNode)EXIT)).setLabel(Boolean.TRUE);
      } else System.err.println("Last instruction in block is unknown");
    }

    // if EXIT wasn't connected to anything, signal error
    if (EXIT.getInDegree() < 1) {
      System.err.println("Exit indegree is less than one.");
      return;
    }

  }


  /**
   * Node B is aborbed into Node A
   */
  public void mergeNode(BlockNode a, BlockNode b) {
    // merge the in edges of B onto A
    //System.out.println( "vertexA: " + A.toString() + " vertexB: " + 
    //          B.toString() );

    for (Iterator it = new ArrayList(b.getInEdges()).iterator();
         it.hasNext();){
      BlockEdge edge = (BlockEdge) it.next();
      edge.setSink(a);
    } // end of for (Iterator  = .iterator(); .hasNext();)
    for (Iterator it = new ArrayList(b.getOutEdges()).iterator();
         it.hasNext();){
      BlockEdge edge = (BlockEdge) it.next();
      edge.setSource(a);
    } // end of for (Iterator  = .iterator(); .hasNext();)
    removeNode(b);
    mergeEdges( (BlockNode)a);
  }


  // mergeSerial
   /**
   * These methods do the subsumption
   **/
  public void mergeSerial( BlockNode nodeA, BlockEdge edge, BlockNode nodeB) {
    //System.out.println("BlockGraph::mergeSerial()");

    //System.out.println(" nodeA\n "+nodeA.getDefHash()+"\n"+nodeA.getUseHash());
    //System.out.println(" nodeB\n "+nodeB.getDefHash()+"\n"+nodeB.getUseHash());

    // remove the edge between nodeA -> nodeB
    removeEdge( edge );

    /*
      This function takes the outputs of the this vertex and tests
      them agains the inputs of THE vertex.  If there is correspondance,
      then a new operand is created to move the value of a given variable
      q from the old block to the new block.  This does not try to optimize
      the aliasing that is going on.
     */

    mergeSerialCrossCancel(nodeA, nodeB);

    /*
      This probably can happen but it is not a big deal.
      It says that for a given input q, this vertex reads
      q and old vertex reads q.  It is not a problem, but 
      we would like them to both read from the same variable.
      They should not be reading different values since that should
      have been cleared up in the previous check.
    */ 
    mergeSerialOutputs(nodeA, nodeB);

    mergeInstructions(nodeA, nodeB);
    
    //System.out.println(" nodeA\n"+nodeA.getDefHash()+"\n"+nodeA.getUseHash());

  }
  
  
  private void mergeInstructions(BlockNode a, BlockNode b) {
    ArrayList a_instructions = a.getInstructions();
    for(ListIterator iter = b.getInstructions().listIterator(); 
	iter.hasNext(); ) {
      Instruction temp = (Instruction)iter.next();
      if (!temp.isNOP()) 
	a.addInstruction(temp);
    }
  }


  /*
    This takes two nodes that are connected via an edge in a serial fashion and
    fixes the nodeA -> outputs collisions with inputs -> nodeB.
  */
  private void mergeSerialCrossCancel(BlockNode nodeA, BlockNode nodeB) {
    
    //System.out.println("BlockGraph::serialCrossCancel()");
    DefHash def_hash = nodeA.getDefHash();
    UseHash merging_use_hash = nodeB.getUseHash();
    for (Iterator it = ((DefHash)def_hash.clone()).keySet().iterator(); 
	 it.hasNext(); ) {

      Operand this_out = (Operand)it.next();
      // unnecessary to examine constants
      if (this_out.isConstant()) continue;
      if (!this_out.isPrimal()) continue;

      ArrayList collisions = (ArrayList)merging_use_hash.get(this_out);
      // no collisions, no work. -- this_out must be primal, right?
      if (collisions == null) continue;
      collisions = (ArrayList)collisions.clone();

      Instruction source = (Instruction)def_hash.get(this_out);
      // find collisions for now ...
      // print them ?
      
      //System.out.println("SerialCrossCancel collisions:");
      //System.out.println(collisions);

      // this may not be optimal, if we are adding selects for lots of collisions.
      for(ListIterator collision_iter = collisions.listIterator(); 
	  collision_iter.hasNext(); ) {
	Instruction collision = (Instruction)collision_iter.next();

	Operand new_op = this_out.getNextNonPrime();
	Instruction mine = source, his = collision;
	BooleanEquation his_pred = his.getPredicate();
	// I don't need to remove mine.
	nodeB.removeInstruction(his);

	
	//PrimalOperand primal_op = (PrimalOperand)this_out;
      
	BooleanEquation mux_eq = new BooleanEquation(mine.getPredicate());
      
	BEqToList eq_list = new BEqToList(mux_eq, new BooleanEquation(true));
	for(Iterator eq_iter = eq_list.iterator(); eq_iter.hasNext(); ) {
	  nodeA.addInstruction((Instruction)eq_iter.next());
	}
	
	BooleanOperand mux_op = eq_list.getResult();

	Instruction mux = Select.create(Operators.SELECT, mine.type(),
					Load.getResult(his), mux_op, 
					Store.getValue(mine), new_op);

	BooleanEquation mux_pred = new BooleanEquation(his_pred);
	mux.setPredicate(mux_pred);

	Load.setResult(his, new_op);

	// little strange to add these to nodeB.
	nodeB.addInstruction(mux);
	nodeB.addInstruction(his);
      }

    }
  }

  
  void mergeSerialOutputs(BlockNode nodeA, BlockNode nodeB) {
    //System.out.println("BlockGraph::mergeSerialOutputs()");
    DefHash def_hash = nodeA.getDefHash();
    DefHash merging_def_hash = nodeB.getDefHash();
    for(Iterator it = ((HashMap)def_hash.clone()).keySet().iterator(); 
	it.hasNext(); ) {
      Operand this_out = (Operand)it.next();
      if (this_out.isConstant()) continue;

      Instruction collision = (Instruction)merging_def_hash.get(this_out);
      if (collision == null) continue;

      Instruction source = (Instruction)def_hash.get(this_out);
      
      // print them ?
      	    //System.err.println("Output collision : "+collision);
            //System.err.println("Output source	 : "+source);
      Operand new_op = this_out.getNextNonPrime();
      BooleanEquation new_pred = new BooleanEquation();
      Instruction mine = source, his = collision;
      
      BooleanEquation his_pred = his.getPredicate();
      BooleanEquation mine_pred = mine.getPredicate();
      
      PrimalOperand primal_op = (PrimalOperand)this_out;
      
      Instruction store = Store.create(Operators.STORE, mine.type(), 
				       primal_op, new_op);
      
      BooleanEquation mux_eq = new BooleanEquation(his_pred);
      
      BEqToList eq_list = new BEqToList(mux_eq, new BooleanEquation(true));
      for(Iterator eq_iter = eq_list.iterator(); eq_iter.hasNext(); ) {
	nodeA.addInstruction((Instruction)eq_iter.next());
      }

      BooleanOperand mux_op = eq_list.getResult();

      Instruction mux = Select.create(Operators.SELECT, mine.type(),
				      new_op, mux_op, 
				      Store.getValue(his),				      
				      Store.getValue(mine));

      BooleanEquation mux_pred = new BooleanEquation(his_pred);
      BooleanEquation store_pred = new BooleanEquation(mine_pred);
      store_pred.or(mux_pred);
      mux_pred.or(store_pred);

      store.setPredicate(store_pred);
      mux.setPredicate(mux_pred);

      nodeA.removeInstruction(mine);
      nodeB.removeInstruction(his);

      nodeA.addInstruction(store);
      nodeA.addInstruction(mux);

    }
  }


  // mergeParallel
  public void mergeParallel(BlockNode a, BlockNode b) {
    mergeParallelOutputs(a, b);
    
    mergeInstructions(a, b);
  }
  

  void mergeParallelOutputs(BlockNode a, BlockNode b) {
    DefHash def_hash = a.getDefHash();
    DefHash merging_def_hash = b.getDefHash();

    for(Iterator it = ((DefHash)def_hash.clone()).keySet().iterator(); it.hasNext(); ) {
      Operand this_out = (Operand)it.next();
      // constants, although primal are not interesting.
      if (this_out.isConstant()) continue;
      
      Instruction collision = (Instruction)merging_def_hash.get(this_out);
      // nothing to see, move along.
      if (collision == null) continue;
     
      Instruction source = (Instruction)def_hash.get(this_out);
      
      //System.out.println(" Mine "+source);
      //System.out.println(" his  "+collision);

      Operand new_op = this_out.getNextNonPrime();
      
      BooleanEquation new_pred = new BooleanEquation();
      Instruction mine = source, his = collision;
      
      BooleanEquation his_pred = his.getPredicate();
      BooleanEquation mine_pred = mine.getPredicate();

      //System.err.println("collision "  + collision);
      //System.err.println("source "  + source);
      //System.err.println(" bad var  "+this_out);
      PrimalOperand primal_op = (PrimalOperand)this_out;

      // Now since we have found a collision -- we will search through
      // all of the instructions to find where this thing was used.
          
      Instruction store = Store.create(Operators.STORE, mine.type(), 
                                       (Operand)primal_op, new_op);
      
      BooleanEquation mux_eq = new BooleanEquation(his_pred);

      BEqToList eq_list = new BEqToList(mux_eq, new BooleanEquation(true));
      for(Iterator eq_iter = eq_list.iterator(); eq_iter.hasNext(); ) {
        a.addInstruction((Instruction)eq_iter.next());
      }
      
      BooleanOperand mux_op = eq_list.getResult();

      Instruction mux = Select.create(Operators.SELECT, mine.type(),
				      new_op, mux_op,
				      Store.getValue(his),
				      Store.getValue(mine));
      
      BooleanEquation mux_pred = new BooleanEquation(his_pred);
      BooleanEquation store_pred = new BooleanEquation(mine_pred);
      store_pred.or(mux_pred);
      mux_pred.or(store_pred);

      store.setPredicate(store_pred);
      mux.setPredicate(mux_pred);

      a.removeInstruction(mine);
      b.removeInstruction(his);

      a.addInstruction(store);
      a.addInstruction(mux);
    }
  }

	
  private void cleanUp(Object something) {

  }

  // this assumes basic block structure.
  // will not work on merged structures.
  // This also assumes no predicates.
  public void splitBlock(BlockNode node, Instruction inst) {
    // create new node
    // move out edges from old node to new node
    // make out edge from old to new
    // remove all instructions from old node
    // add partial list to old node
    // add the rest to new node

    ArrayList old_list = (ArrayList)node.getInstructions().clone();
    // if this is goofy, we should throw an exception or something.
    int index = old_list.indexOf(inst);
    int size = old_list.size();

    // how does this fit into keeping track of loop stuff?
    BlockNode new_node = (BlockNode)addNode();
    new_node.setName("split_"+node.getLabel().getFullName());
    Set out_edges = new HashSet();
    out_edges.addAll(node.getOutEdges());

    for(Iterator sIt = out_edges.iterator(); sIt.hasNext(); ) {
      BlockEdge edge = (BlockEdge)sIt.next();
      edge.setSource(new_node);
    }
    out_edges.clear();
    BlockEdge edge = (BlockEdge)addEdge(node, new_node);
    edge.setPredicate(new BooleanEquation(true));
    
    List node_list = old_list.subList(0, index);
    List new_list = old_list.subList(index, size);

    node.removeAllInstructions();
    node.addInstructions(node_list);
    new_node.addInstructions(new_list);

  }


  /*
   *  EDGE CODE *
   */

  /**
   * This method takes two edges that should have the same source and sink,
   * ORs their predicates and then replaces them with the new ORed edge
   **/
    
  public void mergeEdges(BlockNode node) { 
    // combine edges going in that have the same source
    mergeEdges(node.getInEdges(), true);
    // combine edges going out that have the same sink
    mergeEdges(node.getOutEdges(), false);
  }
  
  /**
   * @param lookForSameSource true if edges should be merged when they have
   * the same source, false if edges should be merged when they have the same
   * sink
   */
  private void mergeEdges( Set edgeSet, boolean lookForSameSource ) {
    // Map sources to lists of edges that have predicates
    Map endPointsToEdges = new HashMap() {
        public Object get(Object key) {
          Object value = super.get(key);
          if (value == null) {
            value = new ArrayList();
            super.put(key, value);
          } // end of if ()
          return value;
        }
	
        public Object put(Object key, Object value) {
          List l = (List) get(key);
          l.add(value);
          return l;
        }
      };
    for (Iterator in_edges = edgeSet.iterator(); in_edges.hasNext();) {
      BlockEdge edge = (BlockEdge)in_edges.next();
      if (edge.getPredicate() != null) {
        String key = null;
        // look for edges with the same source going to the same sink port
        if (lookForSameSource) {
          key = edge.getSource() + ":" + edge.getDotSinkPort();
        } else {
	  // look for edges with the same sink coming from the same source port
          key = edge.getSink() + ":" + edge.getDotSourcePort();
        }
        endPointsToEdges.put(key, edge);
      } // end of if ()
    }
    for (Iterator mit = endPointsToEdges.values().iterator();
         mit.hasNext();){
      List edges = (List) mit.next();
      if (edges.size() > 1) {
        // Multiple edges have the same source
        BlockEdge edge1 = (BlockEdge) edges.get(0);
        BooleanEquation edge1pred = edge1.getPredicate();
        for (Iterator lit = edges.listIterator(1); lit.hasNext();){ 
          BlockEdge edge2 = (BlockEdge) lit.next();
          edge1pred.or( edge2.getPredicate() );
	  // jt: I don't think this is required.
          //edge1.combineIfStatements(edge2);

          removeEdge( edge2 );
        } 
      } // end of if ()
    } // end of for (Iterator  = .iterator(); .hasNext();)
  }



}    


				  

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

import java.util.*;
import fp.util.*;
import java.io.*;
import java.lang.Math;

import fp.hardware.*;
import fp.graph.*;
import fp.passes.*;
import fp.*;


/** This class contains the top level stuff for the mod scheduler, and if that's
 *  not enough explanation, I don't know what is.
 * 
 * @author Kris Peterson
 */
public class ModSched extends Schedule
{  

  /**
  Please refer to Rau's paper on modulo scheduling for a more full discussion of
  modulo scheduling.  However, the result is a block of instructions which is
  the equivalent of multiple iterations of a loop running in parallel in
  staggered starts.  This block of instructions, however, only contains those
  instructions that make up a repeating pattern when all those parallel
  iterations line up and are all running.  That is, when execution of the loop
  first starts for a short period only one iteration is running and then
  only two and so on.  The modulo scheduled block of instructions expects that
  all iterations are already up and running at the moment it starts exection. 
  To handle start up and execution of these first few iterations, a block called
  a prolog is created, which only contains the instructions for these first few
  iterations until enough iterations have started that full parallelism can
  begin.  The same problem occurs at the end of executing the loop when 
  different iterations come to a conclusion and, once again, fewer than all 
  possible iterations are running in parallel.  To handle execution of these
  last iterations, another block, called the epilog is created.  At the end of
  this block, all calculated values are stored to registers.  There is a problem
  however, due to these multiple iterations all trying to write to the same
  registers.  Actually, only one iteration should be allowed to write to the
  registers.  To control which iteration performs the write, the outputs from
  all the iterations are placed through a tree of muxes.  This nested class,
  MuxAdder, has methods for identifying when to add muxes, to do some editing of
  the epilog in preparation for adding the muxes, and a method for creation and
  scheduling of the prolog and epilog hyperblocks (this method probably
  shouldn't be here--I'm not sure what I was thinking when I did that).  However
  the main mux adding code is in the file MuxTableGenerator.java, and the
  comments there should be reffered to for more understanding.
  */
  private class MuxAdder {
  
    /**
    each primal that is getting stored to needs a different tree of muxes.  Also
    we need to be able to tell the difference between outputs from different
    iterations.  To help with this, as each iteration calls this method, a count
    for each primal is done, which is used to create iteration specific block
    operands to store the data from that iteration which would have been stored
    to that primal.  
    */
    private HashMap _muxCnts = new HashMap();
    
    /**
    I need block variables to hold the data for each iteration that will be
    stored to a primal.  To do this, I create a block variable named after the
    primal and on the iteration count.  So for example, given a store to a
    primal "a", the first iteration requesting a mux will create a block
    variable called a_0 (note, the iteration that first requests a mux is
    actually the last iteration of the loop and so actually a_5 is from an 
    earlier iteration than a_0).  After this block operand has been created, the
    store instruction can be deleted, and the instruction that defined the block
    operand that was being stored to the register can be changed to define a_0 
    instead. In addition, any other instructions using the input to the store 
    instruction need to be changed, to use a_0 instead, since the old block is
    no longer being defined.  To clarify more, if we have these instructions:
    
    (BLOCK) tmp_1 = aaa_iadd (BLOCK) tmp_2, (BLOCK) tmp_3
    (BLOCK) tmp_5 = aaa_iadd (BLOCK) tmp_1, (BLOCK) tmp_4
    (PRIMAL) a = aaa_store (BLOCK) tmp_1
    
    we need to delete the store and change the others to this (for the 1st
    iteration):
    
    (BLOCK) a_0 = aaa_iadd (BLOCK) tmp_2, (BLOCK) tmp_3
    (BLOCK) tmp_5 = aaa_iadd (BLOCK) a_0, (BLOCK) tmp_4

    and
    
    (BLOCK) a_1 = aaa_iadd (BLOCK) tmp_2, (BLOCK) tmp_3
    (BLOCK) tmp_5 = aaa_iadd (BLOCK) a_1, (BLOCK) tmp_4
    
    for the second iteration and so on for all necessary iterations in the
    epilog.
    
    (remember, later a new store instruction will be inserted that stores the
    output from the mux tree to primal a).
    
    This HashMap saves the association between who needs to be replaced and who
    does the replacing.  So
    key = to be replaced (in above example, tmp_1)
    value = operand to replace it with (in above example, a_0 or a_1)
    */
    private HashMap primReplace = new HashMap();
    /**
    Sometimes, a single block variable is stored to multiple primals.  After
    performing, the above described replacement, the block operand will have
    been renamed for one of the primals, but not for the other.  This will
    create a problem later, because MuxTableGenerator will not know the correct
    input for the second primal.  That is given this code:
    
    (BLOCK) tmp_1 = aaa_iadd (BLOCK) tmp_2, (BLOCK) tmp_3
    (BLOCK) tmp_5 = aaa_iadd (BLOCK) tmp_1, (BLOCK) tmp_4
    (PRIMAL) a = aaa_store (BLOCK) tmp_1
    (PRIMAL) b = aaa_store (BLOCK) tmp_1
    
    after doing the above described changes and creating a_0 and b_0 block
    operands, which the MuxTableGenerator will expect, only a_0 will be defined:
    
    (BLOCK) a_0 = aaa_iadd (BLOCK) tmp_2, (BLOCK) tmp_3
    (BLOCK) tmp_5 = aaa_iadd (BLOCK) a_0, (BLOCK) tmp_4
    
    This hashMap was written to notice this and allow duplicate definitions to
    handle b_0 as well.  It is used to copy the definition of the source block
    operand, where the copy defines b_0 instead of a_0:
    
    (BLOCK) a_0 = aaa_iadd (BLOCK) tmp_2, (BLOCK) tmp_3
    (BLOCK) b_0 = aaa_iadd (BLOCK) tmp_2, (BLOCK) tmp_3
    (BLOCK) tmp_5 = aaa_iadd (BLOCK) a_0, (BLOCK) tmp_4
    
    this is not ideal as now we have two iadds, and it could be two floating
    point square roots, and this actually could be changed, since
    MuxTableGenerator is told what to expect as input.  For all it cares, it 
    could use a_0 (or even "bob_the_block_variable") instead of b_0.  
    
    update:  I changed it to get rid of the copying and it works!
    
    */
    //private HashMap primReplace2 = new HashMap();
    /**
    The select input to the muxes is controlled by looking at the predicates for
    each iteration.  However, we need to rename the predicate operands so that
    each iteration uses different ones and the control logic for the mux tree
    can tell them apart.  This hashmap is simple:
    key = old predicate operand name
    value = new predicate operand name
    */
    private HashMap predReplace = new HashMap();
    /**
    This is used with primReplace2, to find instructions to copy. When I find
    that one block operand is being stored to two primals, the block operand is
    saved here.  Later, the instructions are searched over for one who defines
    any operand in this set.  When one is found, it is copied, and the copy
    changed to define the appropriate block operand in primReplace2 
    */
    private HashSet _copyDefinition = new HashSet();
    
    public MuxAdder() {}
    
    public void addMuxes(InstructionList iList, MuxTableGenerator muxGen, 
                         BooleanEquation loopExitPredicate) {

      primReplace = new HashMap();
      //primReplace2 = new HashMap();
      predReplace = new HashMap();
      //foreach time, foreach instruction in the set at that time
      for (Iterator vIt = ((HashMap)iList.clone()).keySet().iterator(); 
                 vIt.hasNext();) {
	Float execTimeFl = (Float) vIt.next();
	float execTime = execTimeFl.floatValue();
	HashSet iList2 = iList.getAllAtTime(execTime);
	for (Iterator vIt2 = ((HashSet)iList2.clone()).iterator(); 
	           vIt2.hasNext();) {
	  InstructionList.InstructionObject instObj = 
	                      (InstructionList.InstructionObject) vIt2.next();
	  Instruction inst = instObj.inst;
	  
	  //if the instruction is a store
	  if(Store.conforms(inst)) {
            //BooleanEquation predTmp = inst.getPredicate();
	    //if(predTmp != null) {
              //LinkedList BoolsTmp = predTmp.listBooleanOperands();
	      
	      //if the store does not have a predicate, we can't make any
	      //control logic for out mux table, and also, this should weed out
	      //any constant stores to block operands 
	      if(instObj.listOfPredsUsed.size() != 0) {
	      //if(BoolsTmp.size() != 0) {
	      
		//just to make sure, check that it is storing to a primal, but
		//also make sure it's not one of the modPrims since we don't
		//care about their values after the loop, and any leftover
		//stores to modprims need to be simply deleted.
		if((Store.getDestination(inst).isPrimal())&&
		   (Store.getDestination(inst).getFullName().indexOf("modPrim")==-1)) {
		  PrimalOperand out = (PrimalOperand)Store.getDestination(inst);
		  //BooleanEquation neweq = new BooleanEquation(loopExitPredicate);
		  BooleanEquation neweq = new BooleanEquation((Bool)instObj.getItPred());
		  
		  //get the iteration count (and remember, the lower this
		  //number, the later the iteration, since later iterations
		  //request their muxes earlier
		  if(!_muxCnts.containsKey(out))
		    _muxCnts.put(out, new Integer(0));
		  int cnt =  ((Integer)_muxCnts.get(out)).intValue();
		  
		  //create new booleans for all the operands in the predicate,
		  //change the predicate accordingly and save the mapping to 
		  //predReplace, so that the definitions of these operands can
		  //also be changed
		  //for (Iterator itin = instObj.listOfPredsUsed.iterator(); 
		  /*for (Iterator itin = neweq.listBooleanOperands().iterator(); 
        	       itin.hasNext(); ) {
        	    Operand op = (Operand)itin.next();
		    Operand opCopy = Operand.newBoolean(op.getFullName() + "_" + cnt);
		    predReplace.put(op, opCopy);
		    neweq = neweq.replaceBool((Bool)op, (Bool)opCopy);
		    System.out.println("op " + op);
		    System.out.println("opCopy " + opCopy);
		    //loopExitPredicate = neweq;
        	  }*/
		  
		  //create the new block operand to hold this iterations output
		  Operand newOut = Operand.newBlock(out.getFullName() + "_" + cnt);
		  Operand input = Store.getValue(inst);
		  
		  //however, if input is stored to multiple primals, use the 
		  //existing new block
		  if(primReplace.containsKey(input)) {
		    newOut = (Operand)primReplace.get(input);
		  }
		  //tell MuxTableGenerator about this input to the mux tree
		  muxGen.add(out, neweq, newOut);
		  
		  //increment the iteration count for this primal:
		  _muxCnts.put(out, new Integer(++cnt));
		  
		  //sometimes, a constant is being stored to the primal.  The 
		  //constant will, obviously, not be defined anywhere else, and
		  //so unlike in all other cases, the store instruction must not
		  //be deleted, but instead changed to define the new block 
		  //operand.  (And note, just because in this iteration a 
		  //constant is being stored to the primal does not mean that
		  //other iterations will all also store this constant to the 
		  //primal, meaning, we cannot assume that we only need one 
		  //store in this case and no mux tree.) 
		  if(input.isConstant())
		    instObj.replaceOperand(out, newOut);
		  else {
		    /*if(primReplace.containsKey(input)) {
		      _copyDefinition.add(input);
		      primReplace2.put(input, newOut);
		    }
		    else {*/
		    
		    //save the mapping from the old block operand to the new
		      primReplace.put(input, newOut);
		    //}
		    //delete the store instruction:
		    iList.remove(execTime, instObj);
		  }
	        }
	      }
            //}

	  }
	}
      }
      //this method performs all the replacements:
      replaceOperands(iList);
    }
    
    /**
    This method replaces operands as requested by addMuxes
    */
    public void replaceOperands(InstructionList iList) {
    
      /*for (Iterator it1 = _copyDefinition.iterator(); it1.hasNext();) {
        Operand prim = (Operand)it1.next();
        Operand primReplacement = (Operand)primReplace2.get(prim);
	for (Iterator it2 = ((HashSet)iList.getInstSet().clone()).iterator(); 
                     it2.hasNext();) {
	  InstructionList.InstructionObject instObj = 
                                	  (InstructionList.InstructionObject)it2.next();
	  if(instObj.getInstOuts().contains(prim)) {
	    InstructionList.InstructionObject instObjCopy = instObj.copySaveOps();
	    System.out.println("prim replace " + instObjCopy.inst);
	    System.out.println("prim " + prim);
	    System.out.println("primReplacement " + primReplacement);
	    instObjCopy.replaceOperand(prim, primReplacement);
	    iList.add(-1, instObjCopy);
	  }
	}
      }*/
      
      for (Iterator it1 = ((HashSet)iList.getInstSet().clone()).iterator(); 
                   it1.hasNext();) {
	InstructionList.InstructionObject instObj = 
                                	(InstructionList.InstructionObject)it1.next();
	//Instruction inst = instObj.inst;
	
	//replace the block operands with the iteration and primal specific 
	//operands
	for (Iterator itin = ((Set)primReplace.keySet()).iterator(); 
             itin.hasNext(); ) {
          Operand prim = (Operand)itin.next();
          Operand primReplacement = (Operand)primReplace.get(prim);
          instObj.replaceOperand(prim, primReplacement);
	}
	//replace the predicate definitions with the new predicate operand names
	/*for (Iterator itin = ((Set)predReplace.keySet()).iterator(); 
            itin.hasNext(); ) {
	  Operand pred = (Operand)itin.next();
          Operand predReplacement = (Operand)predReplace.get(pred);
          instObj.replaceOperand(pred, predReplacement);
	  BooleanEquation eq = (BooleanEquation)instObj.inst.getPredicate();
	  BooleanEquation neweq = eq.replaceBool((Bool)pred, 
	                                         (Bool)predReplacement);
	  instObj.inst.setPredicate(neweq);
	}*/

      }    

    }

    /**
    This method creates hyperblock nodes in the BlockGraph for the prolog and
    epilog, and it schedules those blocks using the user requested, nonmodulo
    scheduling algorithm
    */
    public void createProNEpiNodes(BlockNode bNode, BlockGraph graph,
                                   InstructionList pList, InstructionList eList,
				   ChipDef chipDef) {

      //just to make sure that there actually are instructions in the prolog
      if(pList.size()>0) {
	BlockNode prologNode = (BlockNode)graph.addNode();
	LabelOperand blockName = bNode.getLabel();
	String blockNameStr = blockName.getFullName();
	//name the prolog after the loop body, with prolog_ in front
	prologNode.setName("prolog_"+ blockNameStr);


	//change the loops in edges (except the loop edge) to point at the 
	//prolog instead
	Set in_edges = new HashSet();
	in_edges.addAll(bNode.getInEdges());
	for (Iterator it = in_edges.iterator(); it.hasNext();) {
	  BlockEdge inEdge = (BlockEdge)it.next();
	  if(!inEdge.isBackwardEdge()) {
    	    inEdge.setSink(prologNode);
	  }
	}

	//create a new edge from the prolog to the loop body (the kernal) and
	//set its predicate to true
	BlockEdge newEdge = (BlockEdge)graph.addEdge(prologNode, bNode);
	BooleanEquation trueBoolEq = new BooleanEquation(true);
	newEdge.setPredicate(trueBoolEq);
	
	//place all the instructions in the block, do a new hardware analysis,
	//and schedule the block
	prologNode.addInstructions(new ArrayList(pList.getInstructions()));
        if(!chipDef.isDummyBoard())
	  AnalyzeHrdWrConstraintsPass.iICalc.calcLogicSpaceReq(graph, chipDef, 
                                                               GlobalOptions.conserveArea);
        initOpUseLists(prologNode, chipDef);
	MasterScheduler proSched = new MasterScheduler(prologNode, _opSel, 
	                                               chipDef);
	proSched.schedule(graph);
	
	//sort the instructions so that they will be easier to read in the dotty
	//sort(prologNode.getInstructions());
      }

      //do all the same for the epilog except, ...
      if(eList.size()>0) {
	BlockNode epilogNode = (BlockNode)graph.addNode();
	LabelOperand blockName = bNode.getLabel();
	String blockNameStr = blockName.getFullName();
	epilogNode.setName("epilog_" + blockNameStr);


	//remap the kernal's out edges so they go from the epilog instead of
	//from the kernal and set their predicate to true
	Set out_edges = new HashSet();
	out_edges.addAll(bNode.getOutEdges());
	BooleanEquation edgePredicate = null;
	for (Iterator it = out_edges.iterator(); it.hasNext();) {
          BlockEdge outEdge = (BlockEdge)it.next();
          if(!outEdge.isBackwardEdge()) {
            BlockEdge newEdgeTmp = (BlockEdge)graph.newEdge();
            outEdge.setSource(epilogNode);
	    newEdgeTmp.setPredicate(new BooleanEquation(true));
            graph.replaceEdge(outEdge, newEdgeTmp);
          }
	  else {
	    BooleanEquation outEdgeCopy = (BooleanEquation)outEdge.getPredicate().clone();
            edgePredicate = new BooleanEquation(outEdgeCopy.not());
	  }

	}

	//BooleanEquation newKernalToEpiPred = new BooleanEquation(edgePredicate);
	//create a new edge from the kernal to the epilog, with the kernal exit
	//predicate
	BlockEdge newEpiEdge = 
                  (BlockEdge)graph.addEdge(bNode, epilogNode);
	epilogNode.addInstructions(new ArrayList(eList.getInstructions()));
	//newEpiEdge.setPredicate(newKernalToEpiPred);
        newEpiEdge.setPredicate(edgePredicate);
	
	
	if(!chipDef.isDummyBoard())
	  AnalyzeHrdWrConstraintsPass.iICalc.calcLogicSpaceReq(graph, chipDef, 
        						       GlobalOptions.conserveArea);
        
	
        initOpUseLists(epilogNode, chipDef);
	MasterScheduler epiSched = new MasterScheduler(epilogNode, _opSel, 
	                                               chipDef);
	epiSched.schedule(graph);
	//sort(epilogNode.getInstructions());
      
      
      }
    }
  
  } //end class MuxAdder

   /**
   *  This little class was created to pass these three numbers between methods.
   *  They are used for creating the prolog and epilog (see below for how).
   */
   public class NumberPasser {
     /**
     the length of the unrolled loop body
     */
     public float schedLength;
     /**
     the modulo scheduled execution time of the last instruction in the unrolled
     loop.
     */
     public float lastInstExecTime;
     /**
     the modulo scheduled execution time for the first instruction in the
     unrolled loop
     */
     public float fstStartTime;
   } //end class NumberPasser
   
   /**
   a factor for determining how many times to attempt modulo scheduling for any
   given II.
   */
   private OperationSelection _opSel;
   
   public ModSched(OperationSelection opSel, 
                   BlockNode node) {
     super(node);
     _opSel = opSel;
   }

   /**
   Sometimes primals are stored to in the loop body without any predicate.  This
   is fine within the kernal, but in the epilog, I need to know which of the
   final iterations needs to be stored.  I need this for the control logic for
   the mux tree.  This method, adds the loop control predicate to all primal
   stores in the loop (that is the loop exit predicate, so that stores would
   only occur when the loop exit condition becomes true).  _oldPreds is a
   hashmap that I use to save the old predicates, so they can be restored within
   the kernal after scheduling, where 
   key = instruction
   value = old predicate (most likely "true" or this method would be ignoring 
           the instruction)
   */
   private HashMap _oldPreds = new HashMap();
   public void addPredsToPrimalStores(BlockNode bNode) {
     //HashSet storeInsts = new HashSet();
     //HashMap opsToReplace = new HashMap();
     //HashMap primsToReplace = new HashMap();
     
     Set out_edges = new HashSet();
     out_edges.addAll(bNode.getOutEdges());
     BooleanEquation edgePredicate = null;
     for (Iterator it = out_edges.iterator(); it.hasNext();) {
       BlockEdge outEdge = (BlockEdge)it.next();
       if(outEdge.isBackwardEdge()) {
    	 BooleanEquation edgePredicateCopy = (BooleanEquation)outEdge.getPredicate().clone();
	 edgePredicate = edgePredicateCopy.not();
       }
     }
     for (Iterator it1 = bNode.getInstructions().iterator(); 
	  it1.hasNext();) {
       Instruction inst = (Instruction)it1.next();
       if(Store.conforms(inst)) {
	 Operand out = Store.getDestination(inst);
	 //Operand in = Store.getValue(inst);
	 if((out.isPrimal())&&
            (inst.getPredicate().listBooleanOperands().size()==0)) {
           BooleanEquation oldPred = (BooleanEquation)inst.getPredicate().clone();
	   inst.setPredicate(edgePredicate);
	   _oldPreds.put(inst, oldPred);  
	   //storeInsts.add(inst);
	   //Operand newOp = Operand.nextBlock(out.getFullName());
	   //opsToReplace.put(out, newOp);
	   //opsToReplace.put(in, newOp);
	   //inst.replaceOperand(in, newOp);
	 }
       }
     }
     /*for (Iterator it1 = bNode.getInstructions().iterator(); 
	  it1.hasNext();) {
       Instruction inst = (Instruction)it1.next();
       if(!storeInsts.contains(inst)) {
	 for (Iterator it2 = opsToReplace.keySet().iterator(); 
	      it2.hasNext();) {
           Operand op = (Operand)it2.next();
	   Operand newOp = (Operand)opsToReplace.get(op);
	   inst.replaceOperand(op, newOp);
	 }
       }
     }*/
   }
   
   /**
   This method changes the predicates back to what they were before 
   addPredsToPrimalStores
   */
   public void changePrimalStoresBack(MSchedHash modSched) {
      for (Iterator vIt = modSched.getInstSet().iterator(); vIt.hasNext();) {
	MSchedHash.MSchedInstObject instObj = 
	                          (MSchedHash.MSchedInstObject) vIt.next();
	Instruction inst = instObj.inst;
	if(_oldPreds.containsKey(inst))
	  inst.setPredicate((BooleanEquation)_oldPreds.get(inst));
      }
   }

   /**
   This is the top level method for the modulo scheduler.  It prepares the loop
   body, calls iterativeSchedule to schedule it, and calls all the methods to
   create and edit the prolog and epilog.
   */
   public void moduloScheduler(BlockNode node, ChipDef chipInfo, BlockGraph graph) {
     //add predicates to primal stores:
     //addPredsToPrimalStores(node);
     
     //MuxTableGenerator does some initial analysis of the loop to add store
     //instructions to save the predicates used by primal store instructions
     //(please refer to MuxTableGenerator's comments for more)
     MuxTableGenerator muxGen = new MuxTableGenerator();
     HashSet instSet = new HashSet(node.getInstructions());
     Set out_edges = new HashSet();
     out_edges.addAll(node.getOutEdges());
     BooleanEquation loopExitPredicate = null;
     for (Iterator it = out_edges.iterator(); it.hasNext();) {
       BlockEdge outEdge = (BlockEdge)it.next();
       if(outEdge.isBackwardEdge()) {
    	 BooleanEquation edgePredicateCopy = (BooleanEquation)outEdge.getPredicate().clone();
	 loopExitPredicate = edgePredicateCopy.not();
       }
     }
     ArrayList newInsts = new ArrayList(muxGen.getKernalPreds(instSet, loopExitPredicate));
     //perform operation selection on the new instructions
     for (Iterator it = newInsts.iterator(); 
       it.hasNext(); ) {
       Instruction instr = (Instruction)it.next();
       _opSel.select(instr);
     }
     node.addInstructions(newInsts);
     
     //create a dependence flow graph for the node with the new instructions
     //DependenceFlowGraph dfg = node.getDepFlowGraph();
     DependenceFlowGraph dfg = new DependenceFlowGraph(graph.getName() + "_" + 
                                                       node.getLabel().getName() 
						       + "_mod_scheduled");
     //create the MSchedHash and tell it to read in the instructions from the
     //node
     MSchedHash modSched = new MSchedHash(chipInfo, dfg, _opSel, node);
     modSched.readInList(node);
     for (Iterator itin = muxGen.savePredOps.keySet().iterator(); 
       itin.hasNext(); ) {
       Instruction i = (Instruction)itin.next();
       Operand op = (Operand)muxGen.savePredOps.get(i);
       modSched.addNewIns(i, op);
     }
     for (Iterator itin = muxGen.saveSuccOps.keySet().iterator(); 
       itin.hasNext(); ) {
       Instruction i = (Instruction)itin.next();
       if(i==null) {continue;}
       Operand op = (Operand)muxGen.saveSuccOps.get(i);
       modSched.addNewOuts(i, op);
     }
     
     dfg.generateGraph(node, modSched);
     node.saveDepFlowGraph(dfg);
     //dfg.writeDotFile("trouble_maker");
     
     //do hardware analysis:
     if(!chipInfo.isDummyBoard())
       AnalyzeHrdWrConstraintsPass.iICalc.calcLogicSpaceReq(graph, chipInfo, 
        						    GlobalOptions.conserveArea);
     chipInfo.saveNode(node);
     initOpUseLists(node, chipInfo);
        

     //find minimum ii:
     chipInfo.resetPorts();
     int mResII = node.getII();
     chipInfo.resetPorts();
     int mII = modSched.calc_MII(mResII);

     //schedule:
     int ii = mII;
     /*System.out.println("before scheduling ");
     modSched.printSchedule();
     System.out.println(" ");
     System.out.println(" ");
     System.out.println(" ");
     System.out.println(" ");*/
     chipInfo.resetPorts();
     while((!modSched.iterativeSchedule(ii, (int)(GlobalOptions.budgetRatio * modSched.size())))&&
    	   (ii<=modSched.getMaxRunTime())) {
       ii++;
       //these are various initializations to reset hardware usage so that 
       //chipInfo won't tell the scheduler there are hardware conflicts where
       //there aren't any, because it is remembering instructions scheduled to 
       //times in the last attempt at scheduling
       chipInfo.resetPorts();
       chipInfo.completeInitialize();
       chipInfo.saveNode(node);
       initOpUseLists(node, chipInfo);
       modSched.calcMinDistMat(ii);
     }
     /*System.err.println("ii " + ii);
     System.err.println("modSched.getMaxRunTime() " + modSched.getMaxRunTime());*/
     //if ii gets above maxRunTime, it failed
     if(ii > Math.round(modSched.getMaxRunTime()))
     	 throw new ScheduleException("modulo scheduling failed!  exiting..");
     
     //save ii and the fact that this is modulo scheduled for the synthesizer:
     node.setII(ii);
     node.setIsModuloScheduled(true);
     
     //I'm leaving a lot of these print statements in (but commented out), 
     //because it is often very useful to track the changes to the schedule
     /*System.out.println("used ii of " + ii);
     System.out.println("b4 ls and ss ");
     modSched.printSchedule();
     System.out.println(" ");
     System.out.println(" ");*/
     //save the predicate store and logic created above:
     //modSched.savePredLogicInsts(muxGen.getPredLogic());
     //make all mini loops in the schedule the same length
     modSched.stretchOutLoops(ii);
     //put in loads and stores at the top and bottom end of the mod scheduled 
     //block to transfer data between iterations
     //dfg.writeDotFile("b4addls");
     modSched.addLoadsAndStores(ii);
     //dfg.writeDotFile("aftaaddls");
     
     /*System.out.println("after ls and ss ");
     modSched.printSchedule();
     System.out.println(" ");
     System.out.println(" ");
     System.out.println("unrolled schedule ");*/
     
     //now it's time to create the prolog and epilog.  First, we need to unroll
     //the schedule.  
     NumberPasser nums = new NumberPasser();
     InstructionList unrolledList = modSched.getUnrolledSched(nums, ii);
     
     float time = ii - 1 - modSched.getStoreTime();
     //add the actual predicate store instructions to the kernal
     modSched.addAllInstsToTime(time, muxGen.getPredStores());

     //put the old predicates back in place in the kernal
     changePrimalStoresBack(modSched);
     //save the schedule times to the instructions
     modSched.scheduleList(node);
     //sort(node.getInstructions());
     /*node.removeAllInstructions();
     ArrayList schedList = new ArrayList(modSched.getInstructions());
     sort(schedList);
     node.addInstructions(schedList);
     sort(schedList);*/
     
     //clean up some memory:
     modSched = null;
     
     /*unrolledList.printSchedule();
     System.out.println(" ");
     System.out.println(" ");
     System.out.println("unrolled schedule after remove stuff");*/
     //delete the predicate store, logic instructions from the unrolled list,
     //because they are for the kernal only (to understand this please look at
     //MuxTableGenerator.java)
     unrolledList.removeInsts(newInsts);
     /*unrolledList.printSchedule();
     System.out.println(" ");
     System.out.println(" ");*/
     MuxAdder muxAdder = new MuxAdder();
     
     /**
     ******************************************************************
     prolog creation
     ******************************************************************
     */
     InstructionList proList = new InstructionList();
     /**
     the prolog consists of start of the first few iterations of the loop up
     until the point where full parallelism is possible at which point the
     kernal can run by itself.  To know which instructions should go in the
     prolog, we start with the unrolled loop.  For each iteration we need to
     copy all the instructions that would execute before full parallelism
     occurred to the prolog.  That is, if we took the original loop body and
     started it in staggerred starts, at some point there would be a repeating
     group of instructions which come from different iterations of the main
     loop.  The repeating pattern is the kernal of the modulo scheduled loop. 
     The part before this repeating pattern is the prolog.  To find the prolog
     we need to find each of these iterations, which are all the iterations
     before the first one which can start within the kernal and also end in it. 
     The first iteration before that will run within the kernal for
     int(length_of_unrolled_loop/ii)-1 iterations of the kernal, where only the
     start of the loop will be outside the kernal.  This is very hard to
     explain.  Let's try an example.  If we have the following schedule for a
     loop:
     
     11
     10
     9
     8
     7
     6
     5
     4
     3
     2
     1
     0
     
     where each, number represents a cycle, and for each of these cycles there
     is a set of instructions being executed.  Now let's say we can pipeline
     this loop with an II of 4.  That means we can run it like this:
     
     11
     10 
     9
     8
     7  11
     6  10
     5   9
     4   8
     3   7   11
     2   6   10
     1   5    9
     0   4    8
         3    7   11
	 2    6   10 
	 1    5    9
	 0    4    8
	      3    7
	      2    6
	      1    5
	      0    4
	           3
		   2
		   1
		   0
     
     As, can be seen, there is a repeating pattern of instructions that looks
     like this:
     
     3   7   11
     2   6   10
     1   5    9
     0   4    8
     
     This block can be repeated over, and in effect run three iterations of the
     original loop in parallel.  This is the kernal, created during modulo
     scheduling.  But there is time before this block first
     appears:
     
	      3    7
	      2    6
	      1    5
	      0    4
	           3
		   2
		   1
		   0
     
     This is the prolog.  To find this, we need to know how long the schedule is
     (in our example 12), where that instruction is in the kernal (in our case
     at cycle 3), and ii.  Then we can copy each of these first iterations into
     the prolog.  The first iteration runs from 8 to 11 within the kernal.  We
     can find out how much ran before by subracting ii from the height of the
     loop.  That is 11-4 = 7, and we need to copy instructions from the unrolled
     loop from cycle 0 to 7.  The second iteration runs two full iterations of
     the kernal and it goes from 0 to 11 - 2*ii = 3 outside the kernal.  So
     first, I calculate how far into the kernal, the first iteration reaches
     (which is actually the length of the loop - the execution time in the
     kernal of its last instructions, so if we had a schedule of length 10 with
     this kernal:
     
     3   7   
     2   6   
     1   5    9
     0   4    8
     
     we would need to do 9-2 = 7, to find the top of the first iteration.  Then
     We just need to go up in increments of ii, copying 0 to 7, then 0 to 3, and
     when more iterations run in parallel, more copies are necessary.
     
     */
     int max = (int)(nums.schedLength - nums.lastInstExecTime-1);
     //for each iteration of the prolog
     for(int start = 0; start<max; start+=ii) {
       //copy its instructions from the unrolled loop to the prolog list
       InstructionList oneItProList = unrolledList.partialListCopy(0, max-start);
       
       /*System.out.println("one iteration of prolog ");
       oneItProList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");
       System.out.println("prolog after killing P's "); */
       
       /*DependenceFlowGraph dfg2 = new DependenceFlowGraph(graph.getName() + "_" +  node.getLabel().getName() 
	  						 + "_tmp_mod_scheduled");
       BlockNode nodeTmp = new BlockNode();
       nodeTmp.addInstructions(new ArrayList(oneItProList.getInstructions()));
     System.out.println("add instructions to node and instantiate DFG");
       dfg2.generateGraph(nodeTmp, oneItProList);
       nodeTmp = null;
     System.out.println("created DFG");
       oneItProList.saveDFG(dfg2);
     System.out.println("saved DFG");*/
       //delete chains of loads and stores to get rid of the mod prim variables
       oneItProList.killLSChains();
       /*oneItProList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");
       System.out.println("prolog after removing block to block copies ");*/
       //delete block to block aliases
       oneItProList.killEmBlockToBlockCopies();
       /*oneItProList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");
       System.out.println("prolog after removing block defining ckts ");*/
       //delete groups of instructions that result only in the definition of
       //block operands 
       oneItProList.killBlockDefiningCircuits();
       /*oneItProList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");*/
       //add this iteration to the whole prolog list
       proList.addAll(oneItProList);
     }
       proList.removeDuplicateInsts();
       /*System.out.println("whole pro before removing duplicates ");
     proList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");*/
     //remove duplicate sets of instructions
     //proList.removeDuplicateInsts();
       /*System.out.println("final pro ");
     proList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");

       System.out.println(" ");
       System.out.println(" ");*/
     /**
     ******************************************************************
     epilog creation
     ******************************************************************
     
     given the example above:
     
     11
     10 
     9
     8
     7  11
     6  10
     5   9
     4   8
     3   7   11
     2   6   10
     1   5    9
     0   4    8
         3    7   11
	 2    6   10 
	 1    5    9
	 0    4    8
	      3    7
	      2    6
	      1    5
	      0    4
	           3
		   2
		   1
		   0
     
     the epilog is this:
     
     11
     10 
     9
     8
     7  11
     6  10
     5   9
     4   8
     
     For this we just need to copy all the instructions from the end back to
     ii*iteration count.  So for the second to last iteration 11 - ii = 7 and
     for the final 11- ii*2 = 3.
     
     
     */
     max = (int)(Math.ceil(nums.schedLength/ii)*ii);
     InstructionList epiList = new InstructionList();
     //for(int start=ii; start<=max+max%(ii); start+=ii) {
     //foreach iteration
     for(int start=ii; start<=max; start+=ii) {
       //System.out.println("start " + start);
       //System.out.println("ii " + ii);
       //System.out.println("max " + max);
       //int min = (int)(start - nums.fstStartTime + 1);
       //  System.out.println("min " + min);
       //copy the iteration from the unrolled loop, but use new block variables
       //for each iteration, so that there will be no collisions of data
       InstructionList oneItEpiList =
                          unrolledList.partialListCopyNewBlocks(start, max);
       
       /*System.out.println("one iteration of epilog ");
       oneItEpiList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");
       System.out.println("epilog after killing P's ");*/
       
      /* DependenceFlowGraph dfg3 = new DependenceFlowGraph(graph.getName() + "_" +  node.getLabel().getName() 
	  						  + "_tmp_mod_scheduled");
       BlockNode nodeTmp2 = new BlockNode();
       nodeTmp2.addInstructions(new ArrayList(oneItEpiList.getInstructions()));
       dfg3.generateGraph(nodeTmp2, oneItEpiList);
       oneItEpiList.saveDFG(dfg3);*/
       
       //delete chains of loads and stores which were used to communicate over 
       //the kernal loop boundaries
       oneItEpiList.killLSChains();
       
       /*oneItEpiList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");
       System.out.println("epilog after removing block to block copies ");*/
       
       //delete aliases caused by loading or storing blocks to blocks
       oneItEpiList.killEmBlockToBlockCopies();
       
       /*oneItEpiList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");*/
       
       //call addMuxes, to take note of primals that need mux trees and to
       //change the instructions to define an iteration and primal specific 
       //block operand instead of storing to whichever primal it was previously
       //storing to
       muxAdder.addMuxes(oneItEpiList, muxGen, loopExitPredicate);
       
       /*System.out.println("epilog after adding muxes ");
       oneItEpiList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");*/
       
       //add this iteration to the whole epilog list:
       epiList.addAll(oneItEpiList);
     }
     
     //often for many of the iterations, the final result that is to be stored
     //in the output primal has been calculated in the kernal and saved to one 
     //of the modPrim transfer primals.  In addition, often, many of the 
     //iterations read from the same primal.  If this is the case, the mux tree
     //can possibly be collapsed so that instead of trying to choose between
     //multiple loads of the same primal, it will simply take the value in the 
     //primal once.  Additionally, we only need one load, but multiple 
     //iterations may
     //be loading that primal, each of which would use its own load instruction
     //to do so.  Therefore, when I call findModPrimSources, to find out which
     //iterations use the value in a modPrim variable, so that mux tree
     //collapsing can occur, I also delete all those load statements, so 
     //MuxTableGenerator can create one at the end for all. 
     epiList.removeInsts(muxGen.findModPrimSources(epiList.getInstructions()));
     //Since MuxTableGenerator is creating its own modPrim loads, it can rewire
     //all uses of that modPrim to use its load.  To do this, it first creates
     //a new block variable to hold the value after loading and then, changes
     //all instructions to use this.
     muxGen.replaceOpsWithNewMPrimBs(epiList.getInstructions());
     
       /*System.out.println("removing mod prim loads ");
     epiList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");*/
     
     //Here we add the mux tree, its control logic, the kernal predicate stuff,
     //and the new modPrim loads (see MuxTableGenerator.java for more)
     epiList.addAllInsts(muxGen.genMuxTable());
     
       /*System.out.println("adding muxes ");
     epiList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");
     System.out.println("epilog after removing block defining ckts ");
     System.out.println("epilog after cseremoval ");*/
     
     /**
     this is some really bad coding style, but sorry, I was feeling lazy
     */
     //BlockNode nodeTmp = new BlockNode();
     //HashSet cseIinstSet = epiList.getInstructions();
     //epiList.removeInsts(cseIinstSet);
     //nodeTmp.addInstructions(new ArrayList(cseIinstSet));
     //CSERemovalCode theCSErminator = new CSERemovalCode();
       /*System.out.println("====================================================== ");
       System.out.println(" ");
       System.out.println("b4 " + nodeTmp.getInstructions());*/
     //theCSErminator.removeEmCSEs(nodeTmp);
     //theCSErminator.removeEmCSEs(nodeTmp);
     //theCSErminator.removeEmCSEs(nodeTmp);
       /*System.out.println("afta " + nodeTmp.getInstructions());
       System.out.println(" ");
       System.out.println("====================================================== ");*/
     //epiList = new InstructionList();
     //epiList.addAllInsts(nodeTmp.getInstructions());
      /*epiList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");
    
     //delete all groups of instructions which only define a block variable
     System.out.println("epilog after removing block defining ckts ");*/
     epiList.killBlockDefiningCircuits();
     
     /*epiList.printSchedule();
       System.out.println(" ");
       System.out.println(" ");*/
     
     //delete duplicate instructions
     epiList.removeDuplicateInsts();
     
     //delete any leftover stores to modprims since nobody else except the mod
     //sched blocks care about them
     deleteStoresToModPrims(epiList);
     
       /*System.out.println("final epi ");
     epiList.printSchedule();*/
     
     //run op select on all the new instructions added by MuxTableGenerator
     for (Iterator it = epiList.getInstructions().iterator(); 
       it.hasNext(); ) {
       Instruction instr = (Instruction)it.next();
       _opSel.select(instr);
     }
     
     //create the epilog and prolog nodes:
     muxAdder.createProNEpiNodes(node, graph, proList, epiList, chipInfo);
     
     
  }
  
  /**
  The mod prim operands are registers used to transfer data between iterations 
  of the kernal.  In the example above:
  
     3   7   11
     2   6   10
     1   5    9
     0   4    8
  
  At the end of cycle 3, all important values would be stored to a mod prim
  register, and loaded again in cycle 4, and the same would happen between 7 and
  8. When the loop is unrolled and the epilog and prolog are created, these
  loads and stores are still there.  The epilog does not need anymore of these
  mod prim operands, since the rest of the circuit does not care about internal
  loop values.  Therefore, if there are any left after all the other changes to
  the epilog, this method is called to delete all stores left in the epilog to
  mod prim operands.  
  */
  public void deleteStoresToModPrims(InstructionList epiList) {
    for (Iterator vIt = ((HashSet)epiList.getInstSet().clone()).iterator(); 
                  vIt.hasNext();) {
      InstructionList.InstructionObject instObj = 
        			(InstructionList.InstructionObject) vIt.next();
      Instruction inst = instObj.inst;
      if((Store.conforms(inst))&&
         (Store.getDestination(inst).getFullName().indexOf("modPrim")>=0))
        epiList.remove(instObj);
    }
  }
  
  /**
  This method should probably be moved to the chipdef class.  It initializes the
  operator use handlers (that is the code that counts how often individual
  modules are used and checks for overuse).  
  */
  public void initOpUseLists(BlockNode node, ChipDef chipDef) {
    for (Iterator it = node.getInstructions().iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();
      chipDef.initOpUseLists(instr.operator());
    }
  }
  
  private void sort(ArrayList o_list) {
    class DoubleCompare implements Comparator {      
      public int compare(Object o1, Object o2) {        
        if (o1 instanceof Instruction             
        && o2 instanceof Instruction) {          
          Instruction p1 = (Instruction)o1;          
          Instruction p2 = (Instruction)o2;          
          if (p1.getExecTime() > p2.getExecTime()) {            
            return 1;          
            } else if (p1.getExecTime() < p2.getExecTime()) {            
            return -1;         
            } else {            
            return 0;          
          }        
          } else {          
          throw new ClassCastException("Not Instruction");        
        }      
      }    
    }        
    Collections.sort(o_list, new DoubleCompare());  
  }
}

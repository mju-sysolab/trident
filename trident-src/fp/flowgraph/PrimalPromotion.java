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

/** This class contains the methods for performing primal promotion.  Originally
 *  all this code was in ConvInterBlockNonPrimToPrimal.java, but I moved it here,
 *  so that the methods would be available to ModuloSchedule.java.  This is a 
 *  brand new version of this class.  I rewrote it because I couldn't understand
 *  the old one anymore, and I wanted to rewrite it to make it more readable and 
 *  simple.  I hope I didn't add some errors.  
 * 
 * @author Kris Peterson
 */
public class PrimalPromotion 
{
  
  private class ChildNodeFinder {
  
    private LabelOperand _c1, _c2;
    
    public ChildNodeFinder() { _c1 = null; _c2 = null; }
    
    public BooleanOperand findBrBool(BlockNode node, HashSet nodeSet) {
      for (Iterator instIt = node.getInstructions().iterator(); 
           instIt.hasNext();) {
        Instruction i = (Instruction) instIt.next();
	if(Goto.conforms(i)) {
	  _c1 = Goto.getTarget(i);
	  return null;
	}
	if(Branch.conforms(i)) {
	  _c1 = Branch.getTarget1(i);
	  _c2 = Branch.getTarget2(i);
	  return Branch.getBoolean(i);
	}
      }
      if(node.getOutEdges().size()==1) {
        Iterator cNodeIt = node.getOutEdges().iterator();
        BlockNode cNode = (BlockNode)((BlockEdge)cNodeIt.next()).getSink();
	if(nodeSet.contains(cNode))
	  _c1 = cNode.getLabel();
      }
      return null;
    }
    
    public BlockNode getC1(BlockNode pNode, HashSet nodeSet) {
    
      for (Iterator cNodeIt = pNode.getOutEdges().iterator(); 
           cNodeIt.hasNext();) {
        BlockEdge outEdge = (BlockEdge) cNodeIt.next();
	BlockNode possibleCNode = (BlockNode)outEdge.getSink();
	if(possibleCNode == pNode) continue;
	if(!nodeSet.contains(possibleCNode)) continue;
	if(possibleCNode.getLabel() == _c1)
	  return possibleCNode;
      } 
      return null;
    }
  
    public BlockNode getC2(BlockNode pNode, HashSet nodeSet) {
    
      for (Iterator cNodeIt = pNode.getOutEdges().iterator(); 
           cNodeIt.hasNext();) {
        BlockEdge outEdge = (BlockEdge) cNodeIt.next();
	BlockNode possibleCNode = (BlockNode)outEdge.getSink();
	if(possibleCNode == pNode) continue;
	if(!nodeSet.contains(possibleCNode)) continue;
	if(possibleCNode.getLabel() == _c2)
	  return possibleCNode;
      } 
      return null;
    }
  
  }
  
  /**
  This nested class was written to help create load and store instructions
  and to generate the new primal operands and new block operands
  */
  private class OperandCreator {
  
    private int _lkwCntr;
    private HashMap _def2PrimMap;
    private HashMap _def2NonPrimMap;
    private HashMap _newInsts;
    
    public OperandCreator() {
      _lkwCntr = 0;
      _def2PrimMap = new HashMap();
      _def2NonPrimMap = new HashMap();
      _newInsts = new HashMap();
    }
    
    private class InstRank {
    
      public Instruction i;
      public int rank;
      public InstRank() {}
    }
    
    /**
    I don't add the load and store instructions until the end, and so they
    need to be saved until ready to be added.  This method saves them to a
    private variable hashset called _newInsts, which saves a list of 
    instructions to add to which node.
    */
    public void addNewInst(BlockNode pBlock, Instruction inst, int rank) {
      if(!(_newInsts.containsKey(pBlock)))
        _newInsts.put(pBlock, new HashSet());
      
      InstRank i = new InstRank();
      i.i = inst;
      i.rank = rank;
      ((HashSet)_newInsts.get(pBlock)).add(i);
    }
    
    
    public PrimalOperand makePrimal(Operand op, Type type) {
      if(_def2PrimMap.containsKey(op)) { 
	PrimalOperand newVar = (PrimalOperand)_def2PrimMap.get(op);
        return newVar;
      }
      String name = "lkw" + _lkwCntr;
      _lkwCntr++;
      PrimalOperand newVar = Operand.newPrimal(name);
      /*Variable v = new Variable(newVar, type, true);
      _graph.addVariable(v);*/
      _def2PrimMap.put(op, newVar);
      return newVar;
    
    }
    
    /**
    This method helps me to create a new primal for a given operand that I wish 
    to pass between blocks, unless a primal already exists for that operand.
    */
    //public void addStore(Operand def, Operand otherOp, BlockNode pBlock, Type type, 
    public void addStore(Operand def, Instruction dInst, Operand otherOp, 
                         BlockNode pBlock, Type type, BooleanEquation eq, 
			 HyperBlockList hyperBlockGroups) {
      /**
      make the new primal and its store
      */
      ArrayList nodeInstList = pBlock.getInstructions();
      if(nodeInstList.contains(dInst)) {
        int defRank = nodeInstList.indexOf(dInst) + 1;
        PrimalOperand newVar = makePrimal(def, type);
        //Operand otherOp = makeNonPrim(def);
        Instruction sInst = Store.create(Operators.STORE, type, newVar, otherOp/*, eq*/);
        addNewInst(pBlock, sInst, defRank);
        //return otherOp;
      }
    }
    
    /**
    create or get a new copy of the non primal operand
    */
    public void addLoad(Operand def, BlockNode cBlock, 
                        HyperBlockList hyperBlockGroups, Instruction lInst
			 /*BooleanEquation eq, boolean makeLoad*/) {
    
      //Operand newVar = makeNonPrim(def);
      //get the primal, so that it can be used in the load instruction
      ArrayList instList = hyperBlockGroups.getInstructionList(cBlock);
      UseHash hBlockUseHash = hyperBlockGroups.getUseHash(cBlock);
      int useMinRank = 9999;
      Instruction fstUse = null;
      for(Iterator useIt = ((ArrayList)hBlockUseHash.get(def)).iterator();
           useIt.hasNext();) {
        Instruction useInst = (Instruction) useIt.next();
        if((instList.indexOf(useInst) < useMinRank)&&
	   (useInst != lInst)) {
          useMinRank = instList.indexOf(useInst);
	  fstUse = useInst;
        }
      }
      for(Iterator esIt = hyperBlockGroups.getNodeSet(cBlock).iterator();
    	     esIt.hasNext();){
	BlockNode cNode = (BlockNode) esIt.next();
        UseHash useHash = cNode.getUseHash();
	ArrayList nodeInstList = cNode.getInstructions();

	if(nodeInstList.contains(fstUse)) {
	  int useRank = nodeInstList.indexOf(fstUse);
	  addNewInst(cNode, lInst, useRank);
	}
      }
    }
    
    public void addLoad(Operand def, Operand newVar, BlockNode cBlock, Type type, 
                        HyperBlockList hyperBlockGroups) {
      PrimalOperand lkwVar = makePrimal(def, type);
      BooleanEquation trueBoolEq = new BooleanEquation(true);
      Instruction lInst = Load.create(Operators.LOAD, type, newVar, lkwVar/*, trueBoolEq*/);
      addLoad(def, cBlock, hyperBlockGroups, lInst);
    }
    /**
    add all the instructions to their respective blocks
    */
    public void addInstructs() {
    
      
      for (Iterator opIt = _newInsts.keySet().iterator();
           opIt.hasNext();){
        BlockNode node = (BlockNode) opIt.next();
	ArrayList instList = new ArrayList(((HashSet)_newInsts.get(node)));
	sort(instList);
	int cnt = 0;
	for (Iterator instIt = instList.iterator();
           instIt.hasNext();){
          InstRank i = (InstRank) instIt.next();
	  Instruction inst = i.i;
	  int rank = i.rank + cnt++;
	  node.addInstruction(inst, rank);
	}
      }
      _newInsts = new HashMap();
    }
  
    private void sort(ArrayList o_list) {
      class InstRankCompare implements Comparator {      
	public int compare(Object o1, Object o2) {	
  	  if (o1 instanceof InstRank		  
  	  && o2 instanceof InstRank) { 	 
  	    InstRank p1 = (InstRank)o1;	     
  	    InstRank p2 = (InstRank)o2;	     
  	    if( p1.rank > p2.rank ){  	  
  	      return 1;	       
  	      } else if( p1.rank < p2.rank ){ 	   
  	      return -1;         
  	      } else {		
  	      return 0;	       
  	    }	   
  	    } else {	    
  	    throw new ClassCastException("Not Instruction");	  
  	  }      
	}    
      }	     
      Collections.sort(o_list, new InstRankCompare());  
    }

  }
  

  private OperandCreator _operandCreator;
  private BlockGraph _graph;
  public PrimalPromotion(BlockGraph graph) {
    _operandCreator = new OperandCreator();
    _graph = graph;
  }
  
  /**
  recursive analysis of graph, for primal promotion.  It descends the graph 
  searching for non primals that are defined by a ancestor of some descentent
  block and adds a load and store to a primal, to carry the value between blocks
  and renames the non primal.
  
  */
  private HashMap saveChildUses = new HashMap();
  private HashMap saveParentDefs = new HashMap();
  public void primalPromotion(HyperBlockList mergeBlockGroups) {
    
    primalPromotionRec((BlockNode)_graph.ENTRY, mergeBlockGroups,
		       new HashSet(), new HashSet()/*, saveChildUses, 
		       saveParentDefs*/);
    for(Iterator esIt = saveChildUses.keySet().iterator();
    	   esIt.hasNext();){
      BlockNode node = (BlockNode) esIt.next();
      MultiDefHash def_hash = mergeBlockGroups.getMultiDefHash(node);
      UseHash use_hash = mergeBlockGroups.getUseHash(node);
      HashSet usedByChildren = (HashSet)saveChildUses.get(node);
      //usedByChildren.addAll(use_hash.keySet());
      HashSet savedDefsTmp = (HashSet)saveParentDefs.get(node);
      //savedDefsTmp.addAll(def_hash.keySet());
      HashSet allTransferVars = new HashSet();
      allTransferVars.addAll(savedDefsTmp);
      allTransferVars.addAll(usedByChildren);
      for(Iterator opIt = allTransferVars.iterator();
    	     opIt.hasNext();){
	Operand op = (Operand) opIt.next();
	if((!op.isPrimal())&&(!op.isConstant()))
          addLoadsAndStores(def_hash, use_hash, op, savedDefsTmp, usedByChildren,
        		    node, mergeBlockGroups);
      } 
      
    }
    _operandCreator.addInstructs();
  }
      
  public HashSet primalPromotionRec(BlockNode currentHyperBlock,
                                    HyperBlockList mergeBlockGroups,
				    HashSet alreadyVisited,
				    HashSet savedDefs/*,
				    HashMap saveChildUses,
				    HashMap saveParentDefs*/) {
  
    alreadyVisited.add(currentHyperBlock);
    MultiDefHash def_hash = mergeBlockGroups.getMultiDefHash(currentHyperBlock);
    HashSet savedDefsTmp = new HashSet(savedDefs);
    HashSet savedDefsNew = new HashSet(savedDefs);
    savedDefsNew.addAll(def_hash.keySet());
    HashSet usedByChildren = new HashSet();
    for(Iterator esIt = mergeBlockGroups.getChildren(currentHyperBlock).iterator();
    	   esIt.hasNext();){
      BlockNode cNode = (BlockNode) esIt.next();
      if(!alreadyVisited.contains(cNode)) {
	HashSet usedByChildrenTmp = null;
	usedByChildrenTmp = primalPromotionRec(cNode, mergeBlockGroups, 
	                                       alreadyVisited, savedDefsNew/*,
					       saveChildUses, saveParentDefs*/);
        usedByChildren.addAll(usedByChildrenTmp);
      }
      else if((currentHyperBlock != cNode)&&(cNode != _graph.EXIT)) {
	if(saveChildUses.containsKey(cNode)) {
	  usedByChildren.addAll((HashSet)saveChildUses.get(cNode));
        }
	/*else {
          usedByChildren.addAll(mergeBlockGroups.getUseHash(currentHyperBlock).keySet());
	}*/
        HashSet cNodeDefs = new HashSet();
	if(saveParentDefs.containsKey(cNode))
	  cNodeDefs = (HashSet)saveParentDefs.get(cNode);
	cNodeDefs.addAll(def_hash.keySet());
	saveParentDefs.put(cNode, cNodeDefs);
      }
      else if((currentHyperBlock == cNode)&&(cNode != _graph.EXIT)) {
	//HashSet boundryXingOps = new HashSet();
        //boundryXingOps.addAll(getRealLoopUses(mergeBlockGroups, cNode));
	//usedByChildren.addAll(boundryXingOps);
	//savedDefsTmp.addAll(boundryXingOps);
	savedDefsTmp.addAll(def_hash.keySet());
      }
    }
    //usedByChildren.addAll(mergeBlockGroups.getUseHash(currentHyperBlock).keySet());
    //if((currentHyperBlock!=_graph.EXIT)&&(currentHyperBlock!=_graph.ENTRY)) {
      usedByChildren.addAll(mergeBlockGroups.getUseHash(currentHyperBlock).keySet());
      saveChildUses.put(currentHyperBlock, usedByChildren);
      if(saveParentDefs.containsKey(currentHyperBlock)) {
        HashSet cNodeDefs = (HashSet)saveParentDefs.get(currentHyperBlock);
        //cNodeDefs.addAll(def_hash.keySet());
	savedDefsTmp.addAll(cNodeDefs);
      }
      saveParentDefs.put(currentHyperBlock, savedDefsTmp);
    //}
    return usedByChildren;
  
  }
  
  
  /**
  This method analyzes a hyperblock to decide how to add loads and stores for primal promotion
  and how to replace the existing nonprimal operands so that no aliases are created and taking
  into account whether the lifetime of an operand crosses over a loop boundary, etc.
  
  There are several cases that need to be considered.  
  
  1) an operand is only used by a hyperblock and not defined and is defined by an earlier hyperblock
  in the graph.  It is important to note, that to test for this, we need to check not only that it is
  in the usehash and not in the defhash, but also that it was defined by an earlier block.  However,
  primalPromotionRec does the actual checking to see that it is defined by an earlier block and that it
  is used by this block before calling this method.  In this simple case, we need to add a load to load
  the primal version of this operand into a newly named local copy of the nonprimal.  Additionally, all 
  uses of the original nonprimal, need to be changed to use the new local copy of the nonprimal instead
  of the original nonprimal.  Any aliases existing or created are not removed.
  
  2) an operand is only defined by a hyperblock and not used. Again, primalPromotionRec checks to make
  sure that this operand is in this hyperblock's defhash and is used by a child node.  The only check
  performed here is that it is only defined.  For this case, a store instruction must be added, and a 
  new local nonprimal copy of the original nonprimal must be created and each definition of the
  old nonprimal must be replaced by the new nonprimal.  
  
  3)  if an operand is used and defined within a hyperblock, there are 2 different sub cases.
  
  A)  If an operand is used and defined within a hyperblock and is defined before it is used.  This
  case is very simple, because it overwrites any earlier definitions of the operand and so no load 
  is necessary. There are two sub cases of this subcase:
  
  i) This operand is not used by any of the hyperblock's descendants.  In this case, no loads or stores
  are necessary, and the only thing to do is to rename the nonprimal so it doesn't conflict with other
  versions in the graph. Only one new name should be generated and it should be used for both the uses
  and defs of this operand.
  
  ii) This operand is used by at least one of the hyperblock's descendants.  In this case, a store must be
  added to save the result of this non primal to a primal, which can transfer its value down to the 
  descendant node.  Additionally, the nonprimals should be renamed in the hyperblock, using the same
  name for both all uses and all defs.
  
  B)  If an operand is used and defined within a hyperblock and it is used before it is defined.  This 
  case is slightly more complicated.  For this to be the case, there must be a definition in an 
  ancestor hyperblock, to give that first use its original value.  Because of that, a load will always
  be necessary, although just to be careful, I will first check that it was defined by an ancestor.  If
  this operand is used by a descendant (which could be the next iteration of a loop hyperblock) a store
  instruction must be added.  In this case, two new nonprimal versions of the original nonprimal must be
  created--one for the uses, and as the result of the load, and the other for the defs and for the source
  for the store instruction.  
  
  For cases 2 and 3, if there are multiple definitions of a non prim, they each need to get a different
  new non prim operand, but be saved to the same new primal.  This may create aliases, but the remove 
  alias path will take care of that, and the multiple stores to the same primal can be optimized to one
  store and select statements, but this will be done by the merge blocks pass.
  
  ==================================================================================================
  Note:
  Only 1 load or store is added per hyperblock!!! (I don't know why I used exclamation marks, or 
  even more, why I used 3, but evidently I found this statement highly significant at the time.
  Also this is no longer true.  Multiple stores may be added.)
  ==================================================================================================
  
  
  */
  public void addLoadsAndStores(MultiDefHash defHash, UseHash useHash, Operand op, 
                                HashSet definedByParents, HashSet usedByChildren, 
				BlockNode hyperBlockRoot, 
				HyperBlockList hyperBlockGroups) {
    BooleanEquation trueEq = new BooleanEquation(true);
    //case 1:
    if((useHash.containsKey(op))&&(!defHash.containsKey(op))&&
       (definedByParents.contains(op))) { //check if used but not defined
      Operand newNonPrimOp = op.getNextNonPrime();
      Instruction inst = (Instruction)(((ArrayList)useHash.get(op)).get(0)); //assuming 1st def
        								     //has same type as 
        								     //others
      if(replaceUses(op, newNonPrimOp, useHash, hyperBlockRoot, hyperBlockGroups))
        _operandCreator.addLoad(op, newNonPrimOp, hyperBlockRoot, inst.type(),
	                        hyperBlockGroups);
    }
    //case 2:
    else if((!useHash.containsKey(op))&&(defHash.containsKey(op))&&
            (usedByChildren.contains(op))) { //check if defined but not used
      
      for(Iterator defInstIt = ((ArrayList)defHash.get(op)).iterator(); 
             defInstIt.hasNext();) {
        Instruction inst = (Instruction) defInstIt.next();
        _operandCreator.addStore(op, inst, op, hyperBlockGroups.findNode(inst, hyperBlockRoot), 
	                         inst.type(), trueEq, hyperBlockGroups);
      }
    }
    //case 3:
    else if((useHash.containsKey(op))&&(defHash.containsKey(op))) { //check if used and defined
      //case 3A:
      if(hyperBlockGroups.isDefinedBeforeUsed(op, hyperBlockRoot)) {
	
	//case 3A i:
	if(!usedByChildren.contains(op)) {
	  Operand newNonPrimOp = op.getNextNonPrime();
	  replaceOp(op, newNonPrimOp, useHash, defHash, hyperBlockRoot, hyperBlockGroups); 
	}
	//case 3A ii:
	else {
          for(Iterator defInstIt = ((ArrayList)defHash.get(op)).iterator(); 
    	         defInstIt.hasNext();) {
            Instruction inst = (Instruction) defInstIt.next();
	    _operandCreator.addStore(op, inst, op, hyperBlockGroups.findNode(inst, hyperBlockRoot), inst.type(), 
	                             trueEq, hyperBlockGroups);
	  }
	}
      }
      //case 3B:
      else {
	Operand newNonPrimOp = op.getNextNonPrime();
	//if((definedByParents.contains(op))&&(replaceUses(op, newNonPrimOp, useHash, 
	//  					       hyperBlockRoot, hyperBlockGroups))) {
	if(replaceUses(op, newNonPrimOp, useHash, hyperBlockRoot, hyperBlockGroups)) {
	//if((definedByParents.contains(op))&&
	//   (replaceUses(op, newNonPrimOp, useHash, hyperBlockRoot, hyperBlockGroups))) {
          Instruction inst = (Instruction)(((ArrayList)useHash.get(op)).get(0)); //assuming 1st use
        									 //has same type as 
        									 //others
          _operandCreator.addLoad(op, newNonPrimOp, hyperBlockRoot, inst.type(),
        			  hyperBlockGroups);
	}



	for(Iterator defInstIt = ((ArrayList)defHash.get(op)).iterator(); 
               defInstIt.hasNext();) {
          Instruction inst = (Instruction) defInstIt.next();
          Operand newNonPrimDef = op.getNextNonPrime();
          //_operandCreator.addStore(op, newNonPrimDef, hyperBlockRoot, inst.type(), trueEq,
          _operandCreator.addStore(op, inst, newNonPrimDef, hyperBlockGroups.findNode(inst, hyperBlockRoot), 
	                           inst.type(), trueEq, hyperBlockGroups);
          inst.replaceOperand(op, newNonPrimDef);
	}
       
      }
    }
  }
  
  
  /**
  This method replaces uses of an operand that is being primal promoted, with the new local copy of this
  operand.  If it finds that it will create an alias (i.e. the old nonprimal is loaded or stored into 
  another nonprimal), it replaces the source of the load or store with the new local copy of the nonprim
  and converts any stores into loads.
  */
  public boolean replaceUses(Operand oldUse, Operand newUse, UseHash useHash, 
                             BlockNode cBlock, HyperBlockList hyperBlockGroups) {
      
    //if at least one def is from an alias there will already be a store inst
    boolean addLoad = true;
    Operand newOp = newUse;
    for (Iterator useInstIt = ((ArrayList)((ArrayList)((UseHash)useHash.clone()).get(oldUse)).clone()).iterator(); 
    	 useInstIt.hasNext();) {
      Instruction inst = (Instruction) useInstIt.next();
      //useHash.remove(inst);
      inst.replaceOperand(oldUse, newOp);
      //useHash.add(inst);
    }
    return addLoad;
      
  }
  
  
  /**
  This method replaces defs of an operand that is being primal promoted, with the new local copy of this
  operand.  If it finds that it will create an alias (i.e. the old nonprimal is loaded or stored into 
  another nonprimal), it replaces the destination of the load or store with the new local copy of the 
  nonprim and converts any loads into stores.
  */
  public boolean replaceDefs(Operand oldDef, Operand newDef, MultiDefHash defHash) {
      
    
    boolean addStore = true;
    
    for (Iterator defInstIt = ((ArrayList)defHash.get(oldDef)).iterator(); 
    	 defInstIt.hasNext();) {
      Instruction inst = (Instruction) defInstIt.next();
      inst.replaceOperand(oldDef, newDef);
    }
    return addStore;
      
  }
  
  /**
  This method replaces the old nonprim with a new one.  It replaces both the defs and uses of the 
  nonprim.  If it finds that it would create an alias (i.e. the evil loads and stores to another 
  nonprim), this time, it deletes the load/store instruction and replaces the alias everywhere with
  the new nonprim by calling replaceOp again with the alias operand as an input.  
  */
  public void replaceOp(Operand oldOp, Operand newOp, UseHash useHash, MultiDefHash defHash,
                        BlockNode hyperBlockRoot, HyperBlockList hyperBlockGroups ) {
      
    for (Iterator opInstIt = ((ArrayList)((ArrayList)useHash.get(oldOp)).clone()).iterator(); 
    	 opInstIt.hasNext();) {
      Instruction inst = (Instruction) opInstIt.next();
      inst.replaceOperand(oldOp, newOp);
    }
    
    for (Iterator opInstIt = ((ArrayList)((ArrayList)defHash.get(oldOp)).clone()).iterator(); 
    	 opInstIt.hasNext();) {
      Instruction inst = (Instruction) opInstIt.next();
      inst.replaceOperand(oldOp, newOp);
    }
      
  }
  
  
  /**
  This method looks for which operands have lifetimes over the loop boundary and are used by the next 
  iteration of the loop.  It determines this by checking if the operand was used before being defined.
  */
  public HashSet getRealLoopUses(HyperBlockList hyperBlockGroups, BlockNode hyperblockRootNode) {
    MultiDefHash defHash = hyperBlockGroups.getMultiDefHash(hyperblockRootNode);
    UseHash useHash = hyperBlockGroups.getUseHash(hyperblockRootNode);
    HashSet realUses = new HashSet();
    for(Iterator defIt = defHash.keySet().iterator();
    	   defIt.hasNext();){
      Operand def = (Operand) defIt.next();
      if(useHash.containsKey(def)) {
        if(!hyperBlockGroups.isDefinedBeforeUsed(def, hyperblockRootNode)) {
	  realUses.add(def);
        }
      }
    }
    return realUses; 
  }


}

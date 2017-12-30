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


public class HyperBlockList extends HashMap {

  private class HyperBlockListData {
    public BlockNode root;
    public HashSet nodeSet = new HashSet();
    public UseHash useHash = new UseHash();
    public MultiDefHash defHash = new MultiDefHash();
    public HashSet inEdges = new HashSet();
    public HashSet outEdges = new HashSet();
    public HashSet nextNodes = new HashSet();
    public ArrayList instructions = new ArrayList();
    public HashMap inst2NodeMap = new HashMap();
  }
  
  private class MergeObject {
    public BlockNode node1;
    public BlockNode node2;
    public BlockEdge mergeEdge; //needed for merge serial
    public boolean isParallelMerge = false;
  }
  
  private class MergeStack extends ArrayList {
  
    public MergeStack() {super();}
    public void push(BlockNode node1, BlockNode node2, BlockEdge mergeEdge, 
                    boolean isParallelMerge) {
      MergeObject saveMergeInfo = new MergeObject();
      saveMergeInfo.node1 = node1;
      saveMergeInfo.node2 = node2;
      saveMergeInfo.mergeEdge = mergeEdge;
      saveMergeInfo.isParallelMerge = isParallelMerge;
      add(saveMergeInfo);
    }
    public void push(BlockNode node1, BlockNode node2, 
                    boolean isParallelMerge) {
      MergeObject saveMergeInfo = new MergeObject();
      saveMergeInfo.node1 = node1;
      saveMergeInfo.node2 = node2;
      saveMergeInfo.isParallelMerge = isParallelMerge;
      add(saveMergeInfo);
    }
    /**
    These pushes and pop methods are actually misnamed because this is actually 
    a FIFO and not a stack..I just don't know what the names are for the 
    equivalent instructions in a FIFO.
    */
    public boolean pop(BlockGraph graph) {
      if(size() == 0) //when all merges have been performed return false
        return false;
      MergeObject saveMergeInfo = (MergeObject)get(0);
      remove(0);
      BlockNode node1 = saveMergeInfo.node1;
      BlockNode node2 = saveMergeInfo.node2;
      BlockEdge mergeEdge = saveMergeInfo.mergeEdge;
      boolean isParallelMerge = saveMergeInfo.isParallelMerge;
      
      
      if(saveMergeInfo.isParallelMerge) { //parallel merge
        //graph.mergeEdges(node1);
	graph.mergeParallel(node1, node2);
	graph.mergeNode(node1, node2);
      }
      else { //serial merge
        graph.mergeEdges(node1); //or remove edge??
	graph.mergeSerial(node1, mergeEdge, node2);
	graph.mergeNode(node1, node2);
      }
      return true; //a merge was performed
    }
  
  }
  
  private MergeStack _mergeStack = new MergeStack();
  
  private class Node2HPBlockMap extends HashMap {
    private Node2HPBlockMap() {super();}
    public BlockNode get(BlockNode n) {
      if(containsKey(n))
        return (BlockNode)super.get(n);
      else
        return n;
    }
  }
  
  private Node2HPBlockMap _node2HyperBlockMap = new Node2HPBlockMap();
  
  public HyperBlockList() {super();}
  
  public void createHyperBlock(BlockNode root) {
    if(super.containsKey(root))  
      return;
    HyperBlockListData newHyperBlock = new HyperBlockListData();
    newHyperBlock.root = root;
    _node2HyperBlockMap.put(root, root);
    super.put(root, newHyperBlock);
    
    
    //need to do this by hand because the methods will skip over the data
    newHyperBlock.nodeSet.add(root);
    addInstructions(root, root.getInstructions());
    HashSet prevNodes = new HashSet();
    for (Iterator inEdgIt = root.getInEdges().iterator(); inEdgIt.hasNext();){
      BlockEdge inEdge = (BlockEdge)inEdgIt.next();
      BlockNode prevNodeTmp = (BlockNode)inEdge.getSource();
      BlockNode prevNode = _node2HyperBlockMap.get(prevNodeTmp);
      if(!prevNodes.contains(prevNode)) {
        newHyperBlock.inEdges.add(inEdge);
	prevNodes.add(prevNode);
      }
    }
    HashSet nextNodes = new HashSet();
    for (Iterator outEdgIt = root.getOutEdges().iterator(); outEdgIt.hasNext();){
      BlockEdge outEdge = (BlockEdge)outEdgIt.next();
      BlockNode nxtNodeTmp = (BlockNode)outEdge.getSink();
      BlockNode nxtNode = _node2HyperBlockMap.get(nxtNodeTmp);
      
      if(!nextNodes.contains(nxtNode)) {
        saveNextNode(root, nxtNode);
        newHyperBlock.outEdges.add(outEdge);
	nextNodes.add(nxtNode);
      }
    }
    
  }
    
  public BlockNode findNode(Instruction inst, BlockNode root) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    if(newHyperBlock.inst2NodeMap.containsKey(inst))
      return (BlockNode)newHyperBlock.inst2NodeMap.get(inst);
    
    for (Iterator blockIt = newHyperBlock.nodeSet.iterator(); 
	 blockIt.hasNext();) {
      BlockNode node = (BlockNode) blockIt.next();
      if(node.getInstructions().contains(inst)) {
        newHyperBlock.inst2NodeMap.put(inst, node);
	return node;
      }
    }
    
    return null;
    
  }
  
  public boolean popMerge(BlockGraph graph) {
    return _mergeStack.pop(graph);
  }
  
  /**
  This method checks to see if an operand is defined before it is used.  It does this
  by checking how deep in the ArrayList of instructions for this hyperblock, the 
  operand is defined and compares this with the earliest instruction that uses 
  the operand.  Please see comments for the method makeHyperBlockInstructionList
  for a discussion on the order that the instuctions are placed in the instruction list.
  */
  public boolean isDefinedBeforeUsed(Operand op, BlockNode root) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    ArrayList instList = newHyperBlock.instructions;
    MultiDefHash defHash = newHyperBlock.defHash;
    UseHash useHash = newHyperBlock.useHash;
   
    //Instruction defInst = (Instruction)defHash.get(op);
    int minDefRank = 9999;
    for(Iterator defIt = ((ArrayList)defHash.get(op)).iterator();
	 defIt.hasNext();) {
      Instruction defInst = (Instruction) defIt.next();
      if(instList.indexOf(defInst) < minDefRank)
        minDefRank = instList.indexOf(defInst);
    }
    
    int useMinRank = 9999;
    for(Iterator useIt = ((ArrayList)useHash.get(op)).iterator();
	 useIt.hasNext();) {
      Instruction useInst = (Instruction) useIt.next();
      if(instList.indexOf(useInst) < useMinRank)
        useMinRank = instList.indexOf(useInst);
    }
    
    if(minDefRank < useMinRank)
      return true;
    else
      return false;
  }
  
  public ArrayList getInstructionList(BlockNode root) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    if(newHyperBlock != null)
      return newHyperBlock.instructions;
    else
      return root.getInstructions();
  }
  
  public void addInstructions(BlockNode root, ArrayList iList) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    addInstructions(root, iList, newHyperBlock.instructions.size());
  }
  
  public void addInstructions(BlockNode root, ArrayList iList, int rank) {
    mergeInMultiDefHash(root, iList);
    saveToUseHash(root, iList);
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    newHyperBlock.instructions.addAll(rank, iList);
  }
  
  public void saveToUseHash(BlockNode root, Collection instructions) {
  
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    UseHash use_hash_tmp = newHyperBlock.useHash;
    use_hash_tmp.addinstructions(instructions);
  }
  
  public void removeInst(BlockNode root, Instruction inst) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    for (Iterator blockIt = newHyperBlock.nodeSet.iterator(); 
	 blockIt.hasNext();) {
      BlockNode node = (BlockNode) blockIt.next();
      node.removeInstruction(inst);
    }
    newHyperBlock.instructions.remove(inst);
    newHyperBlock.useHash.remove(inst);
    newHyperBlock.defHash.remove(inst);
  }
  
  public void clearUseHash(BlockNode root) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    //MultiDefHash def_hash_tmp = newHyperBlock.defHash;
    //def_hash_tmp.clear();
    UseHash use_hash_tmp = newHyperBlock.useHash;
    use_hash_tmp.clear();
  }
  
  public void addToUseHash(BlockNode root, Instruction i) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    //MultiDefHash def_hash_tmp = newHyperBlock.defHash;
    //def_hash_tmp.add(i);
    UseHash use_hash_tmp = newHyperBlock.useHash;
    use_hash_tmp.add(i);
  }
  
  /**
  initializes a def hash for a given node
  */
  public void putAllInMultiDefHash(BlockNode root, MultiDefHash def_hash) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    MultiDefHash def_hash_tmp = new MultiDefHash();
    newHyperBlock.defHash = def_hash_tmp;
    def_hash_tmp.putAll(def_hash);
    for (Iterator vIt2 = def_hash.keySet().iterator(); 
	vIt2.hasNext();) {
      Operand def = (Operand) vIt2.next();
      Instruction i = (Instruction)def_hash.get(def);
      def.setType(i.type());
    }
  }
  
  /**
  puts an instruction into the defhash that defines operand op and is in the HyperBlock
  associated with root node root
  */
  public void putInMultiDefHash(BlockNode root, Operand op, Instruction i) {
  
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    MultiDefHash def_hash_tmp = newHyperBlock.defHash;
    def_hash_tmp.put(op, i);
    op.setType(i.type());
  
  }

  public MultiDefHash getMultiDefHash(BlockNode root) {
  
    //this will make an empty hyperblock.  It is only necessary for GlobalEntry, because my pass never comes here
    if(!containsKey(root))
      createHyperBlock(root);
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    return newHyperBlock.defHash;
  
  }

  public void saveMultiDefHash(BlockNode root, MultiDefHash defHash) {
  
    putAllInMultiDefHash(root, defHash);
    //HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    //newHyperBlock.defHash = defHash;
  
  }
  
  public void getCombineMultiDefHashes(BlockNode root, MultiDefHash defHash) {
  
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    MultiDefHash def_hash_tmp = newHyperBlock.defHash;
    for(Iterator instListsIt2 = defHash.values().iterator();
	   instListsIt2.hasNext();){
      ArrayList instLists = (ArrayList) instListsIt2.next();
      def_hash_tmp.addinstructions(instLists);
    }
  }
  
  public void mergeInMultiDefHash(BlockNode root, Collection instructions) {
  
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    MultiDefHash def_hash_tmp = newHyperBlock.defHash;
    def_hash_tmp.addinstructions(instructions);
  }
  

  /**
  this method initializes the usehash saved for a specific set of blocks (I keep track
  of the set, based on one root, which was the first discovered in my traversal of the tree)
  */
  public void putAllInUseHash(BlockNode root, UseHash use_hash) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    UseHash use_hash_tmp = new UseHash();
    newHyperBlock.useHash = use_hash_tmp;
    //use_hash_tmp.putAll(use_hash);
    for (Iterator opIt = ((UseHash)use_hash.clone()).keySet().iterator();
	 opIt.hasNext();){
      Operand op = (Operand) opIt.next();   
      use_hash_tmp.put(op, new ArrayList((ArrayList)use_hash.get(op)));
    }
  }
 
  /**
  this method, takes a usehash for a node, and combines its contents with those already
  in the super blocks usehash
  */
  public void mergeInUseHash(BlockNode root, UseHash use_hash) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    UseHash use_hash_tmp = newHyperBlock.useHash;
    for (Iterator opIt = ((UseHash)use_hash.clone()).keySet().iterator();
	 opIt.hasNext();){
      Operand op = (Operand) opIt.next();   
      if(use_hash_tmp.containsKey(op))
       ((ArrayList)use_hash_tmp.get(op)).addAll((ArrayList)use_hash.get(op));
      else
	use_hash_tmp.put(op, new ArrayList((ArrayList)use_hash.get(op)));
    }
    
  }

  public UseHash getUseHash(BlockNode root) {
    if(!containsKey(root))
      createHyperBlock(root);
  
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    return newHyperBlock.useHash;
  
  }
  
  public boolean containsNode(BlockNode root, BlockNode node) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    if(newHyperBlock != null)
      return newHyperBlock.nodeSet.contains(node);
    else {
      return false;
    }
  }
  
  public HashSet getNodeSet(BlockNode root) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    if(newHyperBlock != null)
      return newHyperBlock.nodeSet;
    else {
      HashSet nodeSetTmp = new HashSet();
      nodeSetTmp.add(root);
      return nodeSetTmp;
    }
  }
  
  public void addToSet(BlockNode root, BlockNode newNode) {
    addToSet(root, newNode, getNodeSet(newNode));
  }
  
  public void killNode(BlockNode root) {
    if(_node2HyperBlockMap.containsKey(root)) 
      remove(root);
  }
  
  public void addToSet(BlockNode root, BlockNode newNode, HashSet nodeSet) {
    HyperBlockListData oldHyperBlock = ((HyperBlockListData)super.get(root));
    for (Iterator nodeIt = nodeSet.iterator(); nodeIt.hasNext();){
      BlockNode nodeTmp = (BlockNode)nodeIt.next();
      _node2HyperBlockMap.put(nodeTmp, root);
      oldHyperBlock.nodeSet.add(nodeTmp);
      addInstructions(root, getInstructionList(nodeTmp));
    }
  }
  
  public void saveInEdges(BlockNode root, HashSet inEdges) {
    HyperBlockListData hyperBlock = ((HyperBlockListData)super.get(root));
    hyperBlock.inEdges = inEdges;
  }
  public HashSet getInEdges(BlockNode root) {
    HyperBlockListData hyperBlock = ((HyperBlockListData)super.get(root));
    if(hyperBlock != null)
      return hyperBlock.inEdges;
    else
      return new HashSet(root.getInEdges());
  }
  public int getInDegree(BlockNode root) {
    HyperBlockListData hyperBlock = ((HyperBlockListData)super.get(root));
    if(hyperBlock != null)
      return hyperBlock.inEdges.size();
    else
      return root.getInDegree();
  }
  public void saveOutEdges(BlockNode root, HashSet outEdges) {
    HyperBlockListData hyperBlock = ((HyperBlockListData)super.get(root));
    hyperBlock.outEdges = outEdges;
  }
  public HashSet getOutEdges(BlockNode root) {
    HyperBlockListData hyperBlock = ((HyperBlockListData)super.get(root));
    if(hyperBlock != null)
      return hyperBlock.outEdges;
    else
      return new HashSet(root.getOutEdges());
  }
  public int getOutDegree(BlockNode root) {
    HyperBlockListData hyperBlock = ((HyperBlockListData)super.get(root));
    if(hyperBlock != null)
      return hyperBlock.outEdges.size();
    else
      return root.getOutDegree();
  }
  
  public void resetChildList(BlockNode root) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    newHyperBlock.nextNodes = new HashSet();
  }
  
  public void saveNextNode(BlockNode root, BlockNode nextNode) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    newHyperBlock.nextNodes.add(nextNode);
  }
  
  public void deleteFromNextSet(BlockNode root, BlockNode nextNode) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    newHyperBlock.nextNodes.remove(nextNode);
  }
  
  public HashSet getChildren(BlockNode root) {
    HyperBlockListData newHyperBlock = ((HyperBlockListData)super.get(root));
    if(newHyperBlock != null)
      return newHyperBlock.nextNodes;
    else {
      HashSet children = new HashSet();
      for (Iterator it = getOutEdges(root).iterator(); it.hasNext();){
        BlockEdge out = (BlockEdge) it.next();
        BlockNode nodetmp = _node2HyperBlockMap.get((BlockNode)out.getSink());
        children.add(nodetmp);
      }
      return children;
    }
  }
  
  /**
  This method is similar to the isMergeable method in BlockNode, except that it
  does the same for HyperBlocks
  */
  public boolean isMergeable(BlockNode root) {
    //check if this is not the root of a hyperblock and if not use BlockNode Method:
    if(!super.containsKey(root)) 
      return root.isMergeable();
      
    String name = root.getLabel().getFullName();
    // lets not merge the entry and exit blocks
    if ("GlobalEntry".equals(name) || "GlobalExit".equals(name)) 
      return false;
    return (getInDegree(root) == 1);
  }
  
  
  /**
  This method lets me put together in one place all the tasks necessary to 
  save the fact that two nodes have been merged serially.  Note: it is not
  actually merging the blocks--it is only saving the fact that they need
  to be merged serially (or cereally if you're hungry and it's time for 
  breakfast).
  */
  public void mergeSerial(BlockNode root, BlockNode node, BlockEdge mergeEdge) {
  
    //make HyperBlock for root
    createHyperBlock(root);
    //_node2HyperBlockMap.put(node, root);
    
    //save node to set of nodes in this HyperBlock
    addToSet(root, node);
    
    //save in edges
    HashSet inEdges = new HashSet();
    for (Iterator inEdgIt = getInEdges(root).iterator(); inEdgIt.hasNext();){
      BlockEdge inEdge = (BlockEdge)inEdgIt.next();
      inEdges.add(inEdge);
      BlockNode inNode = _node2HyperBlockMap.get((BlockNode)inEdge.getSource());
      if(inNode != root)
        updateOutputs(inNode);
      
    }
    for (Iterator inEdgIt = getInEdges(node).iterator(); inEdgIt.hasNext();){
      BlockEdge inEdge = (BlockEdge)inEdgIt.next();
      if(inEdge != mergeEdge) {
    	inEdges.add(inEdge);
        BlockNode inNode = _node2HyperBlockMap.get((BlockNode)inEdge.getSource());
        updateOutputs(inNode);
      }
    }
    saveInEdges(root, inEdges); //this overwrites what was there before
    
    //save out edges and next nodes:
    HashSet outEdges = new HashSet();
    resetChildList(root);
    for (Iterator outEdgIt = getOutEdges(root).iterator(); outEdgIt.hasNext();){
      BlockEdge outEdge = (BlockEdge)outEdgIt.next();
      BlockNode outNode = _node2HyperBlockMap.get((BlockNode)outEdge.getSink());
      if((outEdge != mergeEdge)&&//this tests the same as the anded part, but can't get all cases but is faster
         ((!getNodeSet(root).contains(outNode))||
	  (outNode == root))) {
    	outEdges.add(outEdge);
	saveNextNode(root, outNode);
        if(outNode == root) continue;
        updateInputs(outNode);
      }
    }
    for (Iterator outEdgIt = getOutEdges(node).iterator(); outEdgIt.hasNext();){
      BlockEdge outEdge = (BlockEdge)outEdgIt.next();
      if(!getChildren(root).contains(outEdge.getSink())) {
    	outEdges.add(outEdge);
        BlockNode outNode = _node2HyperBlockMap.get((BlockNode)outEdge.getSink());
	saveNextNode(root, outNode);
        if(outNode == root) continue;
        updateInputs(outNode);
      }
    }
    saveOutEdges(root, outEdges); //this overwrites what was there before
    
    //if we've merged root with a hyperblock, we need to delete the second hyperblock
    killNode(node); //this isn't killing the node from the graph, but rather
                    //from a list of nodes we must consider.
  }
  
  /**
  * This method generates groups of nodes that will be merged together They
  should be treated the as one block when deciding whether to place new Loads
  and Stores in for copying block variables.
      
      copied and modified from fp/passes/MergeSerialBlocks.java
  */
  public void genMergeBlockGroupsSerial(BlockGraph graph) {
    Set replacedVertices = new HashSet();
    boolean getNext = true;
    BlockNode curr_node = null;
    BlockNode root = null;
    HashMap edgeSets = new HashMap();
    Iterator vIt = new ArrayList(graph.getAllNodes()).iterator();
    while (vIt.hasNext()) {
      if (getNext) {
        curr_node = (BlockNode)vIt.next();
      } // end of if ()
      
      if((_node2HyperBlockMap.containsKey(curr_node))&&
         (curr_node != _node2HyperBlockMap.get(curr_node)))
        continue;
      
      if (replacedVertices.contains(curr_node)) {
        continue;
      } // end of if ()
      else if(getNext){
	root = curr_node;
		
      }
      getNext = true;
      

      // do I need to keep these?
      if (curr_node == graph.EXIT) continue;

      
      // now look down all of the out edges
      for (Iterator outIt = getOutEdges(curr_node).iterator();
           outIt.hasNext();){
        BlockEdge out_edge = (BlockEdge) outIt.next();
        BlockNode next_node = _node2HyperBlockMap.get((BlockNode)out_edge.getSink());
	
	// don't remove if this is a loopback edge or a channel or a leaf
        if( next_node == curr_node 
	    || (next_node == graph.ENTRY) 
	    || (next_node == graph.EXIT) ) {
          continue;
        }
        if (curr_node == graph.ENTRY) continue;
        

        // if the in-degree is greater than one, check if same source
        if( getInDegree(next_node) > 1 ) {
          HashSet in_edges = getInEdges(next_node);
          BlockNode prev_node = null;
          boolean different_sources = false;

          for (Iterator inIt = in_edges.iterator(); inIt.hasNext();){
            BlockEdge in_edge = (BlockEdge)inIt.next();
            if( prev_node == null || 
	        prev_node == _node2HyperBlockMap.get((BlockNode)in_edge.getSource())) {
              prev_node = _node2HyperBlockMap.get((BlockNode)in_edge.getSource());
            } else {
              different_sources = true;
            }
          }
          // if there are different sources, leave this one in
          if( different_sources ) {
            continue;
          }
        }
        
	
        if (replacedVertices.contains(next_node)) {
          continue;
        } 
	_mergeStack.push(root, next_node, out_edge, false);
	
	mergeSerial(root, next_node, out_edge);
		
        replacedVertices.add(next_node);
        getNext = false;
      }
    }
  }
  
  /**
  This is for updating the set of input edges for a hyperblock after a parent hyperblock
  has been changed
  */
  public void updateInputs(BlockNode node) {
    createHyperBlock(node);
    HashSet inEdges = new HashSet();
    HashSet prevNodes = new HashSet();
    for (Iterator inEdgIt = getInEdges(node).iterator(); inEdgIt.hasNext();){
      BlockEdge inEdge = (BlockEdge)inEdgIt.next();
      BlockNode prevNodeTmp = (BlockNode)inEdge.getSource();
      BlockNode prevNode = _node2HyperBlockMap.get(prevNodeTmp);
      if(!prevNodes.contains(prevNode)) {
        inEdges.add(inEdge);
	prevNodes.add(prevNode);
      }
    }
    saveInEdges(node, inEdges); //this overwrites what was there before
    
  }
  /**
  This is for updating the set of out edges for a hyperblock after a child hyperblock
  has been changed
  */
  public void updateOutputs(BlockNode node) {
  
    createHyperBlock(node);
    HashSet outEdges = new HashSet();
    resetChildList(node);
    HashSet nextNodes = new HashSet();
    for (Iterator outEdgIt = getOutEdges(node).iterator(); outEdgIt.hasNext();){
      BlockEdge outEdge = (BlockEdge)outEdgIt.next();
      BlockNode nxtNodeTmp = (BlockNode)outEdge.getSink();
      BlockNode nxtNode = _node2HyperBlockMap.get(nxtNodeTmp);
      if(!nextNodes.contains(nxtNode)) {
        saveNextNode(node, nxtNode);
        outEdges.add(outEdge);
	nextNodes.add(nxtNode);
      }
    }
    saveOutEdges(node, outEdges); //this overwrites what was there before
  }  
  /**
  This method lets me put together in one place all the tasks necessary to 
  save the fact that two nodes have been merged parallel.  Note: it is not
  actually merging the blocks--it is only saving the fact that they need
  to be merged parallely.
  */
  public void mergeParallel(BlockNode node1, BlockNode node2) {
  
    //make HyperBlock for node1 (if one does not already exist)
    createHyperBlock(node1);
    _node2HyperBlockMap.put(node2, node1);
    
    
    //save node2 to set of nodes in this HyperBlock
    addToSet(node1, node2);
    
    //save in edges
    HashSet inEdges = new HashSet();
    for (Iterator inEdgIt = getInEdges(node1).iterator(); inEdgIt.hasNext();){
      BlockEdge inEdge = (BlockEdge)inEdgIt.next();
      inEdges.add(inEdge);
      BlockNode inNode = _node2HyperBlockMap.get((BlockNode)inEdge.getSource());
      if(inNode == node1) continue;
      updateOutputs(inNode);
      
    }
    //we only need to save node1's in edges, because for a parallel merge to happen 
    //each node must have only one input edge and the input edge from each of them
    //must have one source.  We only want to remember the edge going from the source
    //to the new root node
    saveInEdges(node1, inEdges); //this overwrites what was there before
    
    //save out edges and next nodes:
    HashSet outEdges = new HashSet();
    resetChildList(node1);
    for (Iterator outEdgIt = getOutEdges(node1).iterator(); outEdgIt.hasNext();){
      BlockEdge outEdge = (BlockEdge)outEdgIt.next();
      outEdges.add(outEdge);
      BlockNode outNode = _node2HyperBlockMap.get((BlockNode)outEdge.getSink());
      saveNextNode(node1, outNode);
      if(outNode == node1) continue;
      updateInputs(outNode);
    }
    for (Iterator outEdgIt = getOutEdges(node2).iterator(); outEdgIt.hasNext();){
      BlockEdge outEdge = (BlockEdge)outEdgIt.next();
      BlockNode outNode = _node2HyperBlockMap.get((BlockNode)outEdge.getSink());
      if((!getChildren(node1).contains(outNode))&&
          ((!getNodeSet(node1).contains(outEdge.getSink()))||
	  (outEdge.getSink() == node1))) {
    	outEdges.add(outEdge);
	saveNextNode(node1, outNode);
        if(outNode == node1) continue;
        updateInputs(outNode);
      }
    }
    saveOutEdges(node1, outEdges); //this overwrites what was there before
    
    //if we've merged node1 with a hyperblock, we need to delete the second hyperblock
    killNode(node2); //this isn't killing the node from the graph, but rather
                     //from a list of nodes we must consider.
  }
  
  /**
      
      copied and modified from fp/passes/MergeParallelBlocks.java
  */
  //private boolean _merged;
  public void genMergeBlockGroupsParallel(BlockGraph graph) {
  
    BlockNode source = (BlockNode)graph.ENTRY;
    
    /// give a magic number a name
    //the code that used this in mergeparallelblocks was commented out!
    //boolean parent_is_decision = false;

    // merge parallel vertices
    // should we invent some "visitor style" classes and 
    // have a recursive visit ?
    recurseMergeBlocks(graph, source);

    // do this before ? or everyone always fixes them
    graph.resetMarkers(); 
    
  
  }
  
  /**
   * This method is supposed to go through all of the vertices and merge those 
   * which have the same source but different predicates.
   **/

  public BlockNode recurseMergeBlocks(BlockGraph graph, BlockNode node) {

    // base case, the node has been seen before
    
    if( node.isMarked()) {
      return node;
    }
    // mark this vertex as visited
    node.setMark();

    if (getOutDegree(node) == 2) {

      // get the out edges
      BlockEdge merged_edge = null;
      Iterator out_edge_it = getOutEdges(node).iterator();
      
      BlockEdge edge1 = (BlockEdge) out_edge_it.next();
      BlockEdge edge2 = (BlockEdge) out_edge_it.next();
      
      // merge all lower vertices
      BlockNode merged_node = null;
      BlockNode node1tmp = recurseMergeBlocks(graph, (BlockNode)edge1.getSink());
      BlockNode node1 = _node2HyperBlockMap.get(node1tmp);
      BlockNode node2tmp = recurseMergeBlocks(graph, (BlockNode)edge2.getSink());
      BlockNode node2 = _node2HyperBlockMap.get(node2tmp);

      // if both edges go to the same node (See Merge3Test) combine them
      if (node1 == node2) {
        edge1 = (BlockEdge)getOutEdges(node).iterator().next();
        // merge up
        if (isMergeable(node1)) {
	  mergeSerial(node, node1, edge1);
	  _mergeStack.push(node, node1, edge1, false);
        }
      } else if( isMergeable(node1) && isMergeable(node2) ) {
      // now merge the deeper node into the more shallow one
        //need to come back here and decide how to handle parallel when really merging
	if( node1.getDepth() < node2.getDepth() ) {
	  // I think I will change this to be graph code.
          _mergeStack.push(node2, node1, true);
	  merged_edge = edge2;
          merged_node = node2;
	  mergeParallel(node2, node1);
        } else {
          _mergeStack.push(node1, node2, true);
          merged_edge = edge1;
          merged_node = node1;
	  mergeParallel(node1, node2);
        }
	
	
	//check if new node would be mergable
        // merge up
	if(( isMergeable(merged_node))&&
	   (_node2HyperBlockMap.get(node) != 
	    _node2HyperBlockMap.get(merged_node)) ) {
	  mergeSerial(node, merged_node, merged_edge);
	  _mergeStack.push(node, merged_node, merged_edge, false);
        }

      }
    } else if (getOutDegree(node) == 1 && node != graph.ENTRY) {
      // get the out edge
      BlockEdge edge1 = (BlockEdge)getOutEdges(node).iterator().next();

      BlockNode node1tmp = recurseMergeBlocks(graph, (BlockNode)edge1.getSink());
      BlockNode node1 = _node2HyperBlockMap.get(node1tmp);
     // merge all lower vertices
      if (isMergeable(node1)) {
	mergeSerial(node, node1, edge1);
	_mergeStack.push(node, node1, edge1, false);
      }
    }
    // now go to the next vertices and check
    //for (Iterator it = getOutEdges(node).iterator(); it.hasNext();){
    for (Iterator it = getChildren(node).iterator(); it.hasNext();){
      BlockNode nodetmp = (BlockNode) it.next();
      recurseMergeBlocks(graph, nodetmp);
    }
    return node;
  }
  
  /*public void mergeNodesIntoHyperBlock(BlockNode setNode, BlockNode newNode,
                                       boolean addInstsb4) {
    HyperBlockListData hyperBlock = (HyperBlockListData)_node2HyperBlockMap.get(setNode);
    BlockNode root = hyperBlock.root;
    addToSet(root, newNode);
    
    /**
    in the parallel merge algorithm, sometimes blocks are added later than they are in the 
    graph, which messes up the instruction order.  An example of this is when one block
    is connected to two parallel blocks.  If the two parallel blocks can be merged, and if 
    the resultant block can be merged up into the top block, this algorithm would first save
    the list of instructions from each of the parallel blocks, and then from the block above,
    but the block above's instructions should be earlier in the list, since they should happen
    before.  So in these cases I add the instructions to the top of the list instead of the end.
    Another example of when this might be necessary, is if a child block was somehow reached and 
    saved to the hyperblock before one of its parents.  Its instructions will have already been 
    saved to the hyperblock instruction list.  But the parent who is being added now, needs to have
    its instructions saved above the child's.  
    
    There is one serious problem with this!  The instructions may not need to be placed at 0!  
    There may be other blocks above this parent!  But child blocks parallel to the child being
    considered at the moment may be above this child in the list, and the parent needs to be 
    above those children.  How to know how far up to put the parent instructions???
    
    if(addInstsb4) {
      addInstructions(root, newNode.getInstructions(), 0);
    }
    else
      addInstructions(root, newNode.getInstructions());
    deleteFromNextSet(root, newNode);
    _node2HyperBlockMap.put(newNode, hyperBlock);

    mergeInMultiDefHash(root, newNode.getInstructions());

    UseHash use_hash_tmp = newNode.getUseHash();
    mergeInUseHash(root, use_hash_tmp);
    for (Iterator it = newNode.getOutEdges().iterator(); it.hasNext();){
      BlockEdge out = (BlockEdge) it.next();
      BlockNode child = (BlockNode)out.getSink();
      if(!getNodeSet(root).contains(child))
        saveNextNode(root, child);
      else
        saveNextNode(root, root);
    }
  
  }
  
  public boolean mergeNodes(BlockNode node1, BlockNode node2) {
    if((!_node2HyperBlockMap.containsKey(node1))&&
       (!_node2HyperBlockMap.containsKey(node2))) {
      createHyperBlock(node1);
      _node2HyperBlockMap.put(node1, get(node1));
      mergeInMultiDefHash(node1, node1.getInstructions());

      UseHash use_hash_tmp0 = node1.getUseHash();
      putAllInUseHash(node1, use_hash_tmp0);
      
      mergeNodesIntoHyperBlock(node1, node2, false);
      
      mergeNodesIntoHyperBlock(node2, node1, true);

      
      
    }
    else if((_node2HyperBlockMap.containsKey(node1))&&
    	    (!_node2HyperBlockMap.containsKey(node2))) {
      
      mergeNodesIntoHyperBlock(node1, node2, false);

    }
    else if((!_node2HyperBlockMap.containsKey(node1))&&
    	    (_node2HyperBlockMap.containsKey(node2))) {
      
      mergeNodesIntoHyperBlock(node2, node1, true);

    }
    else if((_node2HyperBlockMap.containsKey(node1))&&
    	    (_node2HyperBlockMap.containsKey(node2))&&
	    (_node2HyperBlockMap.get(node1) != _node2HyperBlockMap.get(node2))) {
      HyperBlockListData rootHyperBlock = (HyperBlockListData)_node2HyperBlockMap.get(node1);
      BlockNode root = rootHyperBlock.root;
      HyperBlockListData childHyperBlock = (HyperBlockListData)_node2HyperBlockMap.get(node2);
      BlockNode cRoot = childHyperBlock.root;
      
      rootHyperBlock.nodeSet.addAll(childHyperBlock.nodeSet);
      for(Iterator esIt2 = childHyperBlock.nodeSet.iterator();
             esIt2.hasNext();){
        BlockNode nodeTmp = (BlockNode) esIt2.next();
        _node2HyperBlockMap.put(nodeTmp, rootHyperBlock);
      }     
      for(Iterator esIt2 = childHyperBlock.nextNodes.iterator();
             esIt2.hasNext();){
        BlockNode nextNodeTmp = (BlockNode) esIt2.next();
        if(!getNodeSet(root).contains(nextNodeTmp))
          saveNextNode(root, nextNodeTmp);
	else
	  saveNextNode(root, root);
      }     
      //rootHyperBlock.nextNodes.addAll(childHyperBlock.nextNodes);
      addInstructions(root, childHyperBlock.instructions);
      MultiDefHash def_hash_tmp = childHyperBlock.defHash;
      getCombineMultiDefHashes(root, def_hash_tmp);
      UseHash use_hash_tmp = childHyperBlock.useHash;
      mergeInUseHash(root, use_hash_tmp);
      
      remove(cRoot);
    }
    else 
      return false;
    return true;
  }*/
  
}

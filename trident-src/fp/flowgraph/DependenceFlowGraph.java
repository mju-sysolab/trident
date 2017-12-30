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
import fp.graph.Graph;
import fp.graph.Node;
import fp.util.*;
import fp.hardware.*;

import java.util.*;

/** 
 * A data-flow graph for a basic block in a procedure/method/function
 * 
 * @author Nathan Kitchen
 * copied and modified by Kris Peterson
 */
public class DependenceFlowGraph extends Graph {
  static private int count = 0;
  /** 
   * The nodes with no incoming edges
   */
  private HashSet _topSources;
  
  /** 
   * The nodes with no outgoing edges
   */
  private HashSet _bottomSinks;
   
  //saving top and bottom of graph:
  private DependenceFlowNode _sTART;
  private DependenceFlowNode _sTOP;
  
  /**
  these both map instructions to Nodes, but the second one creates a new node if
  no mapping is found.
  */
  private HashMap _inst2NodeMap;
  private InstToNodeMap _inst2NodeGenAndMapper;
  
  private ALoadNAStoreHandler _loadStoreHandler;
  
  /** 
   * Constructs a new unnamed data flow graph
   */
  public DependenceFlowGraph() {
    this(null);
  }
   
  /** 
   * Constructs a new named data flow graph
   * 
   * @param name 
   */
  public DependenceFlowGraph(String name) {
    super(name);
    _topSources = new HashSet();
    _bottomSinks = new HashSet();
    _sTART = new DependenceFlowNode(null);
    _sTOP = new DependenceFlowNode(null);
    _sTART.setPrimaryDotLabel("sTART");
    _sTOP.setPrimaryDotLabel("sTOP");
    this.addNode(_sTART);
    this.addNode(_sTOP);
    _inst2NodeMap = new HashMap();
    _inst2NodeGenAndMapper = new InstToNodeMap();
    _loadStoreHandler  = new ALoadNAStoreHandler();
  }
  
  /** return the start node
   * 
   * @return return the start node
   */
  public DependenceFlowNode sTART(){return _sTART;}
  /** return the stop node
   * 
   * @return return the stop node
   */
  public DependenceFlowNode sTOP(){return _sTOP;}
  /** save the start node
   * 
   * @param s_DependenceFlowNode 
   */
  public void saveSTART(DependenceFlowNode s){_sTART = s;}
  /** save the stop node
   * 
   * @param s_DependenceFlowNode 
   */
  public void saveSTOP(DependenceFlowNode s){_sTOP = s;}
  
  
  /** 
   * Returns the nodes that have no incoming edges.  If all the edges are
   * drawn to point down, these nodes are at the top of the graph.  NOTE:
   * This set is not just a copy.  If you alter it, you will alter the
   * graph itself.
   */
  public Set getTopSources() {
    return _topSources;
  }

  public DependenceFlowNode findNode(Instruction i) {
    
    if(_inst2NodeMap.containsKey(i))
      return (DependenceFlowNode)_inst2NodeMap.get(i);
    for (Iterator it = getAllNodes().iterator(); it.hasNext(); ) {
      DependenceFlowNode node = (DependenceFlowNode)it.next();
      if(node.getInstruction() == null) continue;   
      if(node.getInstruction().toString().equals(i.toString())) {
        _inst2NodeMap.put(i, node);
	return node;
      }
    }
    
    return null;
  }

  private class InstToNodeMap extends HashMap {
  
    public InstToNodeMap(){super();}
    
    public DependenceFlowNode get(InstructionList.InstructionObject instObj) {
      if(containsKey(instObj))
        return (DependenceFlowNode)super.get(instObj);
      else {
        
	Instruction inst = instObj.inst;
	DependenceFlowNode dfn = dfn = new DependenceFlowNode(inst.getClass(), 
	                                                      inst);
        dfn.setPrimaryDotLabel(inst.toDotLabel());
        dfn.setInstruction(inst);
        dfn.setInstructionObj(instObj);
        addNode(dfn);
        DependenceCntrlFlowEdge dcfe = new DependenceCntrlFlowEdge(_sTART, dfn, 
	                                                           "");
        addEdge(dcfe);
        dcfe.setLabel("Entry Edge");
        dcfe = new DependenceCntrlFlowEdge(dfn, _sTOP, "");
        addEdge(dcfe);
        dcfe.setLabel("Exit Edge");
        
	put(instObj, dfn);
	_inst2NodeMap.put(inst, dfn);
	return dfn;
      
      }
    }
  
  }
  
  private class ALoadNAStoreHandler extends HashSet{
    private class ALoadAStorePair {
      public Instruction aloadInst;
      public Instruction astoreInst;
      public ALoadAStorePair() {}
      public ALoadAStorePair(Instruction aloadI, Instruction astoreI) {
        this();
	aloadInst = aloadI;
	astoreInst = astoreI;
      }
    }
    
    public ALoadNAStoreHandler() {
      super();
    }
    
    public void add(Instruction aloadI, Instruction astoreI) {
    
      ALoadAStorePair newPair = new ALoadAStorePair(aloadI, astoreI);
      super.add(newPair);
    
    }
    
    public boolean makeConns(boolean isLoop) {
      boolean isBlockRecursive = false;
      for (Iterator it = this.iterator(); it.hasNext(); ) {
        ALoadAStorePair pair = (ALoadAStorePair)it.next();
	if(keinRecCon(pair)) {
          DependenceDataFlowEdge ddfe = new DependenceDataFlowEdge(
	  	       (DependenceFlowNode)_inst2NodeMap.get(pair.astoreInst), 
	               (DependenceFlowNode)_inst2NodeMap.get(pair.aloadInst),"");
          addEdge(ddfe);
          ddfe.setBackWardsPointing();
          if(isLoop)
            isBlockRecursive = true;
          //ddfe.setLabel("Data Dependency Flow Edge: backwards pointing");
	  
	}
      }
      return isBlockRecursive;
    }
    
    private boolean keinRecCon(ALoadAStorePair pair) {
      DependenceFlowNode store = 
                         (DependenceFlowNode)_inst2NodeMap.get(pair.astoreInst);
      DependenceFlowNode load = 
                         (DependenceFlowNode)_inst2NodeMap.get(pair.aloadInst);
      ArrayList backEdgeToLoopEnd = new ArrayList();
      ArrayList loopEndToLoad = new ArrayList();
      ArrayList loopEndToStore = new ArrayList();
      
      if(!(store.findPathsForRecursiveArrayCheck(load, backEdgeToLoopEnd, 
        					 loopEndToLoad, loopEndToStore)))
      //if(true)
        return true;
      
      //System.out.println("backEdgeToLoopEnd " + backEdgeToLoopEnd);
      //System.out.println("loopEndToLoad " + loopEndToLoad);
      //System.out.println("loopEndToStore " + loopEndToStore);
      
      float start0 = 0;
      float loopEnd0 = start0;
      for (Iterator it2 = backEdgeToLoopEnd.iterator(); it2.hasNext();) {
        DependenceFlowNode nodeTmp = (DependenceFlowNode)it2.next();
	Instruction inst = nodeTmp.getInstruction();
        if(inst == null) continue;
        //System.out.println("inst " + inst); 
         
        if((Cast.conforms(inst))||
           (Store.conforms(inst))||
           (Load.conforms(inst))||
	   (Getelementptr.conforms(inst)))
          continue;
        else if(Binary.conforms(inst)) {
          String opName = inst.operator().toString();
          Operand firstOp = Binary.getVal1(inst);
          Operand secndOp = Binary.getVal2(inst);
          Float fstOp = null; 
          Float scndOp = null; 
	  
	  if(firstOp.isFloatConstant())
	    fstOp = new Float(((FloatConstantOperand)firstOp).getValue());
	  else if(firstOp.isDoubleConstant())
	    fstOp = new Float(((DoubleConstantOperand)firstOp).getValue());
	  else if(firstOp.isIntConstant())
	    fstOp = new Float(((IntConstantOperand)firstOp).getValue());
	  else if(firstOp.isLongConstant())
	    fstOp = new Float(((LongConstantOperand)firstOp).getValue());
	  
	  if(secndOp.isFloatConstant())
	    scndOp = new Float(((FloatConstantOperand)secndOp).getValue());
	  else if(secndOp.isDoubleConstant())
	    scndOp = new Float(((DoubleConstantOperand)secndOp).getValue());
	  else if(secndOp.isIntConstant())
	    scndOp = new Float(((IntConstantOperand)secndOp).getValue());
	  else if(secndOp.isLongConstant())
	    scndOp = new Float(((LongConstantOperand)secndOp).getValue());
	  
	  float value;
	  if(fstOp == null && scndOp == null) {
	    /*System.out.println("fst and 2nd ops null " +  inst);
	    System.out.println("fst ops " +  firstOp);
	    System.out.println("fst ops " +  fstOp);
	    System.out.println("2nd ops " +  scndOp);
	    System.out.println("2nd ops " +  secndOp);*/
	    return true;
	  }
	  value = fstOp == null ? scndOp.floatValue() : fstOp.floatValue();
	  
          if(opName.matches(".*[aA][dD][dD].*")) {
            loopEnd0 += value;
          }
          else if(opName.matches(".*[sS][uU][bB].*")) {
            loopEnd0 = fstOp == null ? loopEnd0-value : value-loopEnd0;
	    //loopEnd0 -= value;
          }
          else if(opName.matches(".*[mM][uU][lL].*")) {
	    loopEnd0 *= value;
          }
          else if(opName.matches(".*[dD][iI][vV].*")) {
            loopEnd0 = fstOp == null ? loopEnd0/value : value/loopEnd0;
	    //loopEnd0 /= value;
          }
          else if(opName.matches(".*[sS][hH][lL].*")) {
	    loopEnd0 *= Math.pow(2, value);
          }
          else if(opName.matches(".*[sS][hH][rR].*")) {
	    loopEnd0 *= Math.pow(1/2, value);
          }
           
           
        }
        else {
          return true;
	  //System.out.println("else " + inst);
	}
      }
      System.out.println("start0 " + start0 + " loopEnd0 " + loopEnd0);
      
      float aload0 = loopEnd0;
      for (Iterator it2 = loopEndToLoad.iterator(); it2.hasNext();) {
        DependenceFlowNode nodeTmp = (DependenceFlowNode)it2.next();
	Instruction inst = nodeTmp.getInstruction();
        if(inst == null) continue;
        //System.out.println("inst " + inst); 
        if((Cast.conforms(inst))||
           (Store.conforms(inst))||
           (Load.conforms(inst))||
	   (Getelementptr.conforms(inst)))
          continue;
        else if(Binary.conforms(inst)) {
          String opName = inst.operator().toString();
          Operand firstOp = Binary.getVal1(inst);
          Operand secndOp = Binary.getVal2(inst);
          Float fstOp = null; 
          Float scndOp = null; 
	  
	  if(firstOp.isFloatConstant())
	    fstOp = new Float(((FloatConstantOperand)firstOp).getValue());
	  else if(firstOp.isDoubleConstant())
	    fstOp = new Float(((DoubleConstantOperand)firstOp).getValue());
	  else if(firstOp.isIntConstant())
	    fstOp = new Float(((IntConstantOperand)firstOp).getValue());
	  else if(firstOp.isLongConstant())
	    fstOp = new Float(((LongConstantOperand)firstOp).getValue());
	  
	  if(secndOp.isFloatConstant())
	    scndOp = new Float(((FloatConstantOperand)secndOp).getValue());
	  else if(secndOp.isDoubleConstant())
	    scndOp = new Float(((DoubleConstantOperand)secndOp).getValue());
	  else if(secndOp.isIntConstant())
	    scndOp = new Float(((IntConstantOperand)secndOp).getValue());
	  else if(secndOp.isLongConstant())
	    scndOp = new Float(((LongConstantOperand)secndOp).getValue());
	  
	  float value;
	  if(fstOp == null && scndOp == null) {
	    /*System.out.println("fst and 2nd ops null " +  inst);
	    System.out.println("fst ops " +  firstOp);
	    System.out.println("fst ops " +  fstOp);
	    System.out.println("2nd ops " +  scndOp);
	    System.out.println("2nd ops " +  secndOp);*/
	    return true;
	  }
	  value = fstOp == null ? scndOp.floatValue() : fstOp.floatValue();
	  
          if(opName.matches(".*[aA][dD][dD].*")) {
            aload0 += value;
          }
          else if(opName.matches(".*[sS][uU][bB].*")) {
            aload0 = fstOp == null ? aload0-value : value-aload0;
	    //aload0 -= value;
          }
          else if(opName.matches(".*[mM][uU][lL].*")) {
	    aload0 *= value;
          }
          else if(opName.matches(".*[dD][iI][vV].*")) {
            aload0 = fstOp == null ? aload0/value : value/aload0;
	    //aload0 /= value;
          }
          else if(opName.matches(".*[sS][hH][lL].*")) {
	    aload0 *= Math.pow(2, value);
          }
          else if(opName.matches(".*[sS][hH][rR].*")) {
	    aload0 *= Math.pow(1/2, value);
          }
           
           
        }
        else {
          return true;
	  //System.out.println("else " + inst);
	}
      }
      System.out.println("aload0 " + aload0 + " loopEnd0 " + loopEnd0);
      
      float astore0 = loopEnd0;
      for (Iterator it2 = loopEndToStore.iterator(); it2.hasNext();) {
        DependenceFlowNode nodeTmp = (DependenceFlowNode)it2.next();
	Instruction inst = nodeTmp.getInstruction();
        if(inst == null) continue;
        //System.out.println("inst " + inst); 
        if((Cast.conforms(inst))||
           (Store.conforms(inst))||
           (Load.conforms(inst))||
	   (Getelementptr.conforms(inst)))
          continue;
        else if(Binary.conforms(inst)) {
          String opName = inst.operator().toString();
          Operand firstOp = Binary.getVal1(inst);
          Operand secndOp = Binary.getVal2(inst);
          Float fstOp = null; 
          Float scndOp = null; 
	  
	  if(firstOp.isFloatConstant())
	    fstOp = new Float(((FloatConstantOperand)firstOp).getValue());
	  else if(firstOp.isDoubleConstant())
	    fstOp = new Float(((DoubleConstantOperand)firstOp).getValue());
	  else if(firstOp.isIntConstant())
	    fstOp = new Float(((IntConstantOperand)firstOp).getValue());
	  else if(firstOp.isLongConstant())
	    fstOp = new Float(((LongConstantOperand)firstOp).getValue());
	  
	  if(secndOp.isFloatConstant())
	    scndOp = new Float(((FloatConstantOperand)secndOp).getValue());
	  else if(secndOp.isDoubleConstant())
	    scndOp = new Float(((DoubleConstantOperand)secndOp).getValue());
	  else if(secndOp.isIntConstant())
	    scndOp = new Float(((IntConstantOperand)secndOp).getValue());
	  else if(secndOp.isLongConstant())
	    scndOp = new Float(((LongConstantOperand)secndOp).getValue());
	  
	  float value;
	  if(fstOp == null && scndOp == null) {
	    /*System.out.println("fst and 2nd ops null " +  inst);
	    System.out.println("fst ops " +  firstOp);
	    System.out.println("fst ops " +  fstOp);
	    System.out.println("2nd ops " +  scndOp);
	    System.out.println("2nd ops " +  secndOp);*/
	    return true;
	  }
	  value = fstOp == null ? scndOp.floatValue() : fstOp.floatValue();
	  
          if(opName.matches(".*[aA][dD][dD].*")) {
            astore0 += value;
          }
          else if(opName.matches(".*[sS][uU][bB].*")) {
            astore0 = fstOp == null ? astore0-value : value-astore0;
	    //astore0 -= value;
          }
          else if(opName.matches(".*[mM][uU][lL].*")) {
	    astore0 *= value;
          }
          else if(opName.matches(".*[dD][iI][vV].*")) {
            astore0 = fstOp == null ? astore0/value : value/astore0;
	    //astore0 /= value;
          }
          else if(opName.matches(".*[sS][hH][lL].*")) {
	    astore0 *= Math.pow(2, value);
          }
          else if(opName.matches(".*[sS][hH][rR].*")) {
	    astore0 *= Math.pow(1/2, value);
          }
           
           
        }
        else {
          return true;
	  //System.out.println("else " + inst);
	}
      }
      System.out.println("astore0 " + astore0 + " loopEnd0 " + loopEnd0);
      
      float start1 = loopEnd0;
      float loopEnd1 = start1;
      for (Iterator it2 = backEdgeToLoopEnd.iterator(); it2.hasNext();) {
        DependenceFlowNode nodeTmp = (DependenceFlowNode)it2.next();
	Instruction inst = nodeTmp.getInstruction();
        if(inst == null) continue;
        //System.out.println("inst " + inst); 
         
        if((Cast.conforms(inst))||
           (Store.conforms(inst))||
           (Load.conforms(inst))||
	   (Getelementptr.conforms(inst)))
          continue;
        else if(Binary.conforms(inst)) {
          String opName = inst.operator().toString();
          Operand firstOp = Binary.getVal1(inst);
          Operand secndOp = Binary.getVal2(inst);
          Float fstOp = null; 
          Float scndOp = null; 
	  
	  if(firstOp.isFloatConstant())
	    fstOp = new Float(((FloatConstantOperand)firstOp).getValue());
	  else if(firstOp.isDoubleConstant())
	    fstOp = new Float(((DoubleConstantOperand)firstOp).getValue());
	  else if(firstOp.isIntConstant())
	    fstOp = new Float(((IntConstantOperand)firstOp).getValue());
	  else if(firstOp.isLongConstant())
	    fstOp = new Float(((LongConstantOperand)firstOp).getValue());
	  
	  if(secndOp.isFloatConstant())
	    scndOp = new Float(((FloatConstantOperand)secndOp).getValue());
	  else if(secndOp.isDoubleConstant())
	    scndOp = new Float(((DoubleConstantOperand)secndOp).getValue());
	  else if(secndOp.isIntConstant())
	    scndOp = new Float(((IntConstantOperand)secndOp).getValue());
	  else if(secndOp.isLongConstant())
	    scndOp = new Float(((LongConstantOperand)secndOp).getValue());
	  
	  float value;
	  if(fstOp == null && scndOp == null) {
	    /*System.out.println("fst and 2nd ops null " +  inst);
	    System.out.println("fst ops " +  firstOp);
	    System.out.println("fst ops " +  fstOp);
	    System.out.println("2nd ops " +  scndOp);
	    System.out.println("2nd ops " +  secndOp);*/
	    return true;
	  }
	  value = fstOp == null ? scndOp.floatValue() : fstOp.floatValue();
	  
          if(opName.matches(".*[aA][dD][dD].*")) {
            loopEnd1 += value;
          }
          else if(opName.matches(".*[sS][uU][bB].*")) {
            loopEnd1 = fstOp == null ? loopEnd1-value : value-loopEnd1;
	    //loopEnd1 -= value;
          }
          else if(opName.matches(".*[mM][uU][lL].*")) {
	    loopEnd1 *= value;
          }
          else if(opName.matches(".*[dD][iI][vV].*")) {
            loopEnd1 = fstOp == null ? loopEnd1/value : value/loopEnd1;
	    //loopEnd1 /= value;
          }
          else if(opName.matches(".*[sS][hH][lL].*")) {
	    loopEnd1 *= Math.pow(2, value);
          }
          else if(opName.matches(".*[sS][hH][rR].*")) {
	    loopEnd1 *= Math.pow(1/2, value);
          }
           
           
        }
        else {
          return true;
	  //System.out.println("else " + inst);
	}
      }
      System.out.println("start1 " + start1 + " loopEnd1 " + loopEnd1);
      
      float aload1 = loopEnd1;
      for (Iterator it2 = loopEndToLoad.iterator(); it2.hasNext();) {
        DependenceFlowNode nodeTmp = (DependenceFlowNode)it2.next();
	Instruction inst = nodeTmp.getInstruction();
        if(inst == null) continue;
        //System.out.println("inst " + inst); 
        if((Cast.conforms(inst))||
           (Store.conforms(inst))||
           (Load.conforms(inst))||
	   (Getelementptr.conforms(inst)))
          continue;
        else if(Binary.conforms(inst)) {
          String opName = inst.operator().toString();
          Operand firstOp = Binary.getVal1(inst);
          Operand secndOp = Binary.getVal2(inst);
          Float fstOp = null; 
          Float scndOp = null; 
	  
	  if(firstOp.isFloatConstant())
	    fstOp = new Float(((FloatConstantOperand)firstOp).getValue());
	  else if(firstOp.isDoubleConstant())
	    fstOp = new Float(((DoubleConstantOperand)firstOp).getValue());
	  else if(firstOp.isIntConstant())
	    fstOp = new Float(((IntConstantOperand)firstOp).getValue());
	  else if(firstOp.isLongConstant())
	    fstOp = new Float(((LongConstantOperand)firstOp).getValue());
	  
	  if(secndOp.isFloatConstant())
	    scndOp = new Float(((FloatConstantOperand)secndOp).getValue());
	  else if(secndOp.isDoubleConstant())
	    scndOp = new Float(((DoubleConstantOperand)secndOp).getValue());
	  else if(secndOp.isIntConstant())
	    scndOp = new Float(((IntConstantOperand)secndOp).getValue());
	  else if(secndOp.isLongConstant())
	    scndOp = new Float(((LongConstantOperand)secndOp).getValue());
	  
	  float value;
	  if(fstOp == null && scndOp == null) {
	    /*System.out.println("fst and 2nd ops null " +  inst);
	    System.out.println("fst ops " +  firstOp);
	    System.out.println("fst ops " +  fstOp);
	    System.out.println("2nd ops " +  scndOp);
	    System.out.println("2nd ops " +  secndOp);*/
	    return true;
	  }
	  value = fstOp == null ? scndOp.floatValue() : fstOp.floatValue();
	  
          if(opName.matches(".*[aA][dD][dD].*")) {
            aload1 += value;
          }
          else if(opName.matches(".*[sS][uU][bB].*")) {
            aload1 = fstOp == null ? aload1-value : value-aload1;
	    //aload1 -= value;
          }
          else if(opName.matches(".*[mM][uU][lL].*")) {
	    aload1 *= value;
          }
          else if(opName.matches(".*[dD][iI][vV].*")) {
            aload1 = fstOp == null ? aload1/value : value/aload1;
	    //aload1 /= value;
          }
          else if(opName.matches(".*[sS][hH][lL].*")) {
	    aload1 *= Math.pow(2, value);
          }
          else if(opName.matches(".*[sS][hH][rR].*")) {
	    aload1 *= Math.pow(1/2, value);
          }
           
           
        }
        else {
          return true;
	  //System.out.println("else " + inst);
	}
      }
      System.out.println("aload1 " + aload1 + " loopEnd1 " + loopEnd1);
      
      float astore1 = loopEnd1;
      for (Iterator it2 = loopEndToStore.iterator(); it2.hasNext();) {
        DependenceFlowNode nodeTmp = (DependenceFlowNode)it2.next();
	Instruction inst = nodeTmp.getInstruction();
        if(inst == null) continue;
        //System.out.println("inst " + inst); 
        if((Cast.conforms(inst))||
           (Store.conforms(inst))||
           (Load.conforms(inst))||
	   (Getelementptr.conforms(inst)))
          continue;
        else if(Binary.conforms(inst)) {
          String opName = inst.operator().toString();
          Operand firstOp = Binary.getVal1(inst);
          Operand secndOp = Binary.getVal2(inst);
          Float fstOp = null; 
          Float scndOp = null; 
	  
	  if(firstOp.isFloatConstant())
	    fstOp = new Float(((FloatConstantOperand)firstOp).getValue());
	  else if(firstOp.isDoubleConstant())
	    fstOp = new Float(((DoubleConstantOperand)firstOp).getValue());
	  else if(firstOp.isIntConstant())
	    fstOp = new Float(((IntConstantOperand)firstOp).getValue());
	  else if(firstOp.isLongConstant())
	    fstOp = new Float(((LongConstantOperand)firstOp).getValue());
	  
	  if(secndOp.isFloatConstant())
	    scndOp = new Float(((FloatConstantOperand)secndOp).getValue());
	  else if(secndOp.isDoubleConstant())
	    scndOp = new Float(((DoubleConstantOperand)secndOp).getValue());
	  else if(secndOp.isIntConstant())
	    scndOp = new Float(((IntConstantOperand)secndOp).getValue());
	  else if(secndOp.isLongConstant())
	    scndOp = new Float(((LongConstantOperand)secndOp).getValue());
	  
	  float value;
	  if(fstOp == null && scndOp == null) {
	    /*System.out.println("fst and 2nd ops null " +  inst);
	    System.out.println("fst ops " +  firstOp);
	    System.out.println("fst ops " +  fstOp);
	    System.out.println("2nd ops " +  scndOp);
	    System.out.println("2nd ops " +  secndOp);*/
	    return true;
	  }
	  value = fstOp == null ? scndOp.floatValue() : fstOp.floatValue();
	  
          if(opName.matches(".*[aA][dD][dD].*")) {
            astore1 += value;
          }
          else if(opName.matches(".*[sS][uU][bB].*")) {
            astore1 = fstOp == null ? astore1-value : value-astore1;
	    //astore1 -= value;
          }
          else if(opName.matches(".*[mM][uU][lL].*")) {
	    astore1 *= value;
          }
          else if(opName.matches(".*[dD][iI][vV].*")) {
            astore1 = fstOp == null ? astore1/value : value/astore1;
	    //astore1 /= value;
          }
          else if(opName.matches(".*[sS][hH][lL].*")) {
	    astore1 *= Math.pow(2, value);
          }
          else if(opName.matches(".*[sS][hH][rR].*")) {
	    astore1 *= Math.pow(1/2, value);
          }
           
           
        }
        else {
          return true;
	  //System.out.println("else " + inst);
	}
      }
      System.out.println("astore1 " + astore1 + " loopEnd1 " + loopEnd1);
      if(aload1>astore1)
        return false;
      //System.exit(1);
      
      return true;
    }
  }
  
  /** perhaps, this is bad programming style, but all t`he main guts of the pass 
   *  are here.  It follows 7 steps:
   * 
   * 1) create STOP and START pseudo nodes (which Rau's algorithm uses)
   * 2) create node for all instructions and add input connection from START and 
   *    output to STOP
   * 3) add data dependency edges (same as data flow) and control dependency 
   *    edges for predicates
   * 4) check for edges going over loop edge and label them as recursive 
   *    connections
   * 5) add data dependency edges for arrays
   * 6) add control edges for loop control
   * 7) write the graph to a dotty file
   * 
   * @param blockGraph 
   */
  public boolean generateGraph(BlockNode bn, InstructionList iList) {
    
    //step 0: checks if the block node is a loop
    boolean isLoop = checkIfBlockNodeIsLoop(bn);
    
    //steps 1-3, create dependence flow nodes and connect them with control and data
    //flow edges
    makeAndConnectNodes(bn, iList);
        //System.out.println("after make and connect  ");
	//printEdges();
    
    //step 4: see which edges should be labeled as recursive (that is is part of a 
    //interloop dependency)
    boolean isBlockRecursive = setRecursiveEdges(isLoop);
        //System.out.println("after set recursive this.getAllEdges() ");
	//printEdges();
    
    //step 4b: look for extra data dependency loop edges that were hidden
    //because there was no load and store to a primal
    isBlockRecursive |= findOtherRecursiveDataEdges(bn, isLoop, iList);
    
    //step 5: add recursive edges from AStores to ALoads if necessary 
    isBlockRecursive = isBlockRecursive | 
                       _loadStoreHandler.makeConns(isLoop);
         //System.out.println("after load store handling this.getAllEdges() ");
	//printEdges();
   bn.setIsRecursive(isBlockRecursive);
    
    //step 6: add control edges (if the Block is a loop, edges from where the loop
    //exit predicate is calculated to every node in the DFG need to be added):
    addCntrlEdges(bn, iList.getInstructions());
         //System.out.println("after add control edges this.getAllEdges() ");
	//printEdges();
    return true;
  }
  
  public boolean findOtherRecursiveDataEdges(BlockNode node, boolean isLoop, 
                                             InstructionList iList) {
  
    if(findOtherRecDataEdgesRec(node, _sTART, new HashSet(), iList) && isLoop)
      return true;
    return false;
  }
  
  public boolean findOtherRecDataEdgesRec(BlockNode node, 
                                          DependenceFlowNode pdfn, 
					  HashSet alreadyVisited,
					  InstructionList iList) {
  
    boolean isRec = false;
    alreadyVisited.add(pdfn);
    HashSet outEdges = new HashSet(pdfn.getOutEdges());
    for (Iterator it5 = outEdges.iterator(); it5.hasNext();) {
      DependenceFlowEdge edge = (DependenceFlowEdge)it5.next();
      if(!edge.getIsDataEdge() && pdfn != _sTART) continue;
      if(edge.getisBackWardsPointing()) continue;
      DependenceFlowNode cdfn = (DependenceFlowNode)edge.getSink();
      
      if((alreadyVisited.contains(cdfn))&&
         (pdfn != _sTART)) {
        removeEdge(edge);
        insertLoadStore(node, pdfn, cdfn, iList);
	isRec = true;
      }
      else if(!alreadyVisited.contains(cdfn)) {
        HashSet alreadyVisitedCopy = new HashSet();
	alreadyVisitedCopy.addAll(alreadyVisited);
	isRec = findOtherRecDataEdgesRec(node, cdfn, alreadyVisitedCopy, iList);
      }
      
    }
    return isRec;
  }
  
  private int _lkwCntr = 0;
  public void insertLoadStore(BlockNode node, DependenceFlowNode pdfn, 
                              DependenceFlowNode cdfn, InstructionList iList) {
    Instruction pInst = pdfn.getInstruction();
    Instruction cInst = cdfn.getInstruction();
    InstructionList.InstructionObject pObj = iList.findInstObj(pInst);
    InstructionList.InstructionObject cObj = iList.findInstObj(cInst);
    
    
    HashSet connectingOps = pObj.getSuccConns(cInst);
    
    for (Iterator it1 = connectingOps.iterator(); it1.hasNext();) {
      Operand op = (Operand)it1.next();
      if(op.isPrimal()) continue;  //if an operand is primal it does not
	                           //need to be loaded and stored
      
      Operand newOp = op.getNextNonPrime();
      node.removeFromHashes(cInst);
      cObj.replaceOperand(op, newOp);
      node.updateHashes(cInst);
      String name = "lkw_dpf" + _lkwCntr;
      _lkwCntr++;
      PrimalOperand newVar = Operand.newPrimal(name);
      Type type = pInst.type();
      BooleanEquation eq = pInst.getPredicate();
      
      Instruction lInst = Load.create(Operators.LOAD, type, newOp, newVar, eq);
      node.addInstruction(lInst);
      InstructionList.InstructionObject load_Obj = iList.newInstructionObject(lInst);
      DependenceFlowNode load_dfn = _inst2NodeGenAndMapper.get(load_Obj);
      iList.add((float)-1.0, load_Obj);
      DependenceDataFlowEdge ddfe = new DependenceDataFlowEdge(load_dfn, cdfn, "");
      this.addEdge(ddfe);
      
      BooleanEquation instPred = load_Obj.inst.getPredicate();
      if(instPred != null) {
        LinkedList BoolsTmp = instPred.listBooleanOperands();
       for(Iterator it3 = BoolsTmp.iterator(); it3.hasNext();) {
          Operand pred = (Operand)it3.next();
	  DefHash defs = node.getDefHash();
	  Instruction parentInst = (Instruction)defs.get(pred);
	  if(parentInst == null) continue;
	  DependenceFlowNode cfnd =
	               _inst2NodeGenAndMapper.get(iList.instToObjMap.get(parentInst));
          DependenceCntrlFlowEdge dcfe = new DependenceCntrlFlowEdge(cfnd, 
								     load_dfn,"");
          this.addEdge(dcfe);
        }
      }
      
      Instruction sInst = Store.create(Operators.STORE, type, newVar, op, 
                                       eq);
      node.addInstruction(sInst);
      InstructionList.InstructionObject store_Obj = iList.newInstructionObject(sInst);
      DependenceFlowNode store_dfn = _inst2NodeGenAndMapper.get(store_Obj);
      iList.add((float)-1.0, store_Obj);
      DependenceDataFlowEdge ddfe2 = new DependenceDataFlowEdge(pdfn, store_dfn, "");
      this.addEdge(ddfe2);
      
      
      DependenceDataFlowEdge ddfe3 = new DependenceDataFlowEdge(store_dfn, 
                                                                load_dfn, "");
      this.addEdge(ddfe3);
      ddfe3.setBackWardsPointing();
      
      instPred = store_Obj.inst.getPredicate();
      if(instPred != null) {
        LinkedList BoolsTmp = instPred.listBooleanOperands();
       for(Iterator it3 = BoolsTmp.iterator(); it3.hasNext();) {
          Operand pred = (Operand)it3.next();
	  DefHash defs = node.getDefHash();
	  Instruction parentInst = (Instruction)defs.get(pred);
	  if(parentInst == null) continue;
	  DependenceFlowNode cfnd =
	               _inst2NodeGenAndMapper.get(iList.instToObjMap.get(parentInst));
          DependenceCntrlFlowEdge dcfe = new DependenceCntrlFlowEdge(cfnd, 
								     store_dfn,"");
          this.addEdge(dcfe);
        }
      }
   
    }
  }
  
  public void printEdges() {
    for (Iterator it = getAllEdges().iterator(); it.hasNext();) {
      DependenceFlowEdge edge = (DependenceFlowEdge)it.next();
      System.out.println("edge from " + edge.getSource() + " to " + edge.getSink());
    }
  
  }
  
  public boolean checkIfBlockNodeIsLoop(BlockNode bn) {
    for (Iterator it = bn.getOutEdges().iterator(); it.hasNext();) {
      BlockEdge outEdge = (BlockEdge)it.next();
      //if(outEdge.isBackwardEdge()) 
      if(outEdge.getSource() == outEdge.getSink()) 
        return true;
    }
    return false;
  }
  
  public void makeAndConnectNodes(BlockNode bn, InstructionList iList) {
    for (Iterator it = iList.getInstSet().iterator(); it.hasNext(); ) {
      InstructionList.InstructionObject instObj = 
                                   (InstructionList.InstructionObject)it.next();
      
      
      DependenceFlowNode pdfn = _inst2NodeGenAndMapper.get(instObj);
      //if(instObj.getNoSuccs()) continue;
      for (Iterator it2 = instObj.getSuccsForDFG().iterator(); it2.hasNext(); ) {
        InstructionList.InstructionObject childObj = 
	                          (InstructionList.InstructionObject)it2.next();
	if(ALoad.conforms(childObj.inst) && AStore.conforms(instObj.inst)) {
          _loadStoreHandler.add(childObj.inst, instObj.inst);
	  continue;
        }
	//System.out.println("making node for " +  childObj);
	//System.out.println("with parent  " +  instObj);
	DependenceFlowNode cdfn = _inst2NodeGenAndMapper.get(childObj);
	//System.out.println("cdfn  " + cdfn);
        DependenceDataFlowEdge ddfe = new DependenceDataFlowEdge(pdfn, cdfn,"");
	if(Load.conforms(childObj.inst) && Store.conforms(instObj.inst)) {
          ddfe.setBackWardsPointing();
        }
        //System.out.println("add data conn between " + pdfn + " and " + cdfn);
        //System.out.println("ddfe " + ddfe);
	this.addEdge(ddfe);
        //System.out.println("all edges ");
	//printEdges();
      }
        //System.out.println("after all adds ");
	//printEdges();
      
      /*for (Iterator it3 = instObj.listOfPredsDefnd.iterator(); it3.hasNext(); ) {
        Operand pred = (Operand)it3.next();
	for (Iterator it4 = iList.getInstSet().iterator(); it4.hasNext(); ) {
          InstructionList.InstructionObject childObj = 
	                          (InstructionList.InstructionObject)it4.next();
	  if(childObj.listOfPredsUsed.contains(pred)) {
	    DependenceFlowNode pdfn = _inst2NodeGenAndMapper.get(instObj.inst);
	    DependenceFlowNode cdfn = _inst2NodeGenAndMapper.get(childObj.inst);
            DependenceCntrlFlowEdge dcfe = new DependenceCntrlFlowEdge(pdfn, 
	                                                               cdfn,"");
            this.addEdge(dcfe);
	  
	  }
	}
      }*/
      BooleanEquation instPred = instObj.inst.getPredicate();
      if(instPred != null) {
        LinkedList BoolsTmp = instPred.listBooleanOperands();
       for(Iterator it3 = BoolsTmp.iterator(); it3.hasNext();) {
      //for (Iterator it3 = instObj.listOfPredsUsed.iterator(); it3.hasNext(); ) {
          Operand pred = (Operand)it3.next();
	  DefHash defs = bn.getDefHash();
	  Instruction parentInst = (Instruction)defs.get(pred);
	  if(parentInst == null) continue;
	  //for (Iterator it4 = iList.getInstSet().iterator(); it4.hasNext(); ) {
          //  InstructionList.InstructionObject childObj = 
	  //                          (InstructionList.InstructionObject)it4.next();
	  //  if(childObj.listOfPredsUsed.contains(pred)) {
	  //DependenceFlowNode pdfn = _inst2NodeGenAndMapper.get(instObj.inst);
	  DependenceFlowNode cdfn =
	               _inst2NodeGenAndMapper.get(iList.instToObjMap.get(parentInst));
          DependenceCntrlFlowEdge dcfe = new DependenceCntrlFlowEdge(cdfn, 
								     pdfn,"");
          this.addEdge(dcfe);
	  
	//  }
	//}
        }
      }
    }
  }
  
  public boolean setRecursiveEdges(boolean isLoop) {
    boolean isBlockRecursive = false;
    for (Iterator it5 = this.getAllEdges().iterator(); it5.hasNext();) {
      DependenceFlowEdge edge = (DependenceFlowEdge)it5.next();
      DependenceFlowNode pdfn = (DependenceFlowNode)edge.getSource();
      if(pdfn.getInstruction() == null) continue;
      InstructionList.InstructionObject pObj = pdfn.getInstructionObj();
      DependenceFlowNode cdfn = (DependenceFlowNode)edge.getSink();
      if(cdfn.getInstruction() == null) continue;
      //System.out.println("is conn between " + pdfn + " and " + cdfn + " recursive?");
      if(((pObj.isAnOutPrimal())||(isStoredToPrimal(pdfn)))&&
          (isSCCAndOneNotBackPointing((Node)pdfn, (Node)cdfn))) {
        edge.setBackWardsPointing();
        //dcfeTmp.setLabel("Control Dependency Flow Edge: backwards pointing");
         //System.out.println("makeing  " + pdfn + " to " + cdfn + " backpointing");
        if(isLoop)
          isBlockRecursive = true;
      }
      
    }
    return isBlockRecursive;
  }
  
  public void addCntrlEdges(BlockNode bn, HashSet iList) {
  
      BooleanEquation edgepred = null;
      Set out_edges = new HashSet();
      out_edges.addAll(bn.getOutEdges());
      BooleanEquation edgePredicate = null;
      for (Iterator it = out_edges.iterator(); it.hasNext();) {
        BlockEdge outEdge = (BlockEdge)it.next();
        if(outEdge.isBackwardEdge()) 
          edgepred = new BooleanEquation(outEdge.getPredicate());
      }
      
      //block is recursive and so control edges are interesting
      if(edgepred != null) {
        LinkedList BoolsTmp = edgepred.listBooleanOperands();
        DefHash defHash2 = bn.getDefHash();
        for(Iterator it1 = BoolsTmp.iterator(); it1.hasNext();) {
          Operand op_Operand = (Operand)it1.next();
          if(defHash2.containsKey(op_Operand)) {
            Instruction sourceInst = (Instruction)defHash2.get(op_Operand);
	    for(Iterator it3 = iList.iterator(); it3.hasNext();) {
              Instruction sinkInst = (Instruction)it3.next();
              DependenceCntrlFlowEdge dcfe = new DependenceCntrlFlowEdge(
	               (DependenceFlowNode)_inst2NodeMap.get(sourceInst),
		       (DependenceFlowNode)_inst2NodeMap.get(sinkInst), 
		       "");
	      this.addEdge(dcfe);
              dcfe.setBackWardsPointing();
         //System.out.println("adding backpointing cntrl edge between  " + sourceInst + " and " + sinkInst);
              //dcfe.setLabel("Control Dependency Edge: backwards pointing");
            }
          }
        }
        
      }
  
  
  }
   
   
  /** check if the output is stored to primal by the next instruction
   * 
   * @param node DependenceFlowNode to check
   * @return true if the output from this is stored to a primal in the next 
   *         instruction
   */
  public boolean isStoredToPrimal(DependenceFlowNode node) {
    if(doesLaterNodeDefPrimal(node, node, new ArrayList()))
      return false;
    for (Iterator nodeIt = node.getOutEdges().iterator(); 
    	 nodeIt.hasNext(); ) {
      DependenceFlowEdge edge = (DependenceFlowEdge)nodeIt.next();
      if(((DependenceFlowNode)edge.getSink()).getInstruction() == null) continue;
      
      
      if((((DependenceFlowNode)edge.getSink()).getInstruction().isStore())&&
         (((DependenceFlowNode)edge.getSink()).getInstruction()
	                                         .getOperand(0).isPrimal()))
        return true;
    }
    return false;
  }
   
   /** this method checks to make sure that there is not a pimal defined within this
   *   loop; it's better to make that primal def instruction the back edge.
   * 
   * @param startNode  
   * @param currentNode 
   * @return whether such a connection exists
   */
  private boolean doesLaterNodeDefPrimal(DependenceFlowNode startNode, 
                                        DependenceFlowNode currentNode,
                                        ArrayList alreadyVisited) {
    Set outEdges = currentNode.getOutEdges();
    boolean flag = false;
    if(outEdges.size()>0){
      for (Iterator it2 = outEdges.iterator(); it2.hasNext() && 
           flag == false;) {
        DependenceFlowEdge outEdge = (DependenceFlowEdge)it2.next();
        if(!startNode.isInSameSCC(outEdge.getSink()))
	  continue;
	if(isOutputPrimal((DependenceFlowNode)outEdge.getSink())) 
          return true;
        //if(outEdge.getSink()==startNode) 
        //  return false;
        
	//else 
	if(!alreadyVisited.contains(outEdge.getSink())){
          alreadyVisited.add(outEdge.getSink());
          flag = doesLaterNodeDefPrimal(startNode, (DependenceFlowNode)outEdge.getSink(), 
	                                alreadyVisited);
        }
      }
    }
    return flag;
     
  }

 /** check if at least one defined operand is primal
   * 
   * @param node_DependenceFlowNode 
   * @return true if at least one def is primal
   */
  public boolean isOutputPrimal(DependenceFlowNode node_DependenceFlowNode) {
    for(int n_int=0;n_int<node_DependenceFlowNode
                                 .getInstruction().getNumberOfDefs(); 
            n_int++) {
      if(node_DependenceFlowNode.getInstruction().getOperand(n_int).isPrimal()){
        return true;
	}
    }
    return false;
  }
   
   
   
  //copied from Graph.java with addition of checking to see if edge is backpointing
  /** this method was copied from Graph.java, but slightly edited.  It checks to 
   *  see if there is a connection between two nodes that does not pass through 
   *  a recursive edge.
   * 
   * @param startNode_Node 
   * @param currentNode 
   * @param alreadyVisited 
   * @return whether such a connection exists
   */
  //private HashSet _setOfSCCs = new HashSet();
  //private HashSet _alreadyFoundInSCC = new HashSet();
  private HashSet _notInSCC = new HashSet();
  private HashSet _alreadyCheckedIfNotInSCC = new HashSet();
  public boolean isSCCAndOneNotBackPointing(Node startNode, 
                                            Node currentNode) {
  
    //if this is true either they are in separate SCCs or if they are in the same
    //since it was already found, an edge must have been backwardspointing already
    //(this is only safe, because I first check if we're looking at the place where
    //I will set an edge to be backwardspointing.)
    //if((_alreadyFoundInSCC.contains(startNode))&&
    //   (_alreadyFoundInSCC.contains(currentNode)))			    
   //   return false;
    
    //if one of them is not in an scc no checking is necessary
    if((_notInSCC.contains(startNode))||
       (_notInSCC.contains(currentNode)))
      return false;
    
    if(!_alreadyCheckedIfNotInSCC.contains(startNode)) {
       _alreadyCheckedIfNotInSCC.add(startNode);
       if(!startNode.isSCC())
         _notInSCC.add(startNode);
    }
    boolean flag = isSCCAndOneNotBackPointingRec(startNode, currentNode, 
                                                 new ArrayList());
   // if(flag)
    //  	_alreadyFoundInSCC.add(startNode);			    
    return flag;
  }
  
  public boolean isSCCAndOneNotBackPointingRec(Node startNode, 
                                               Node currentNode,
                                               ArrayList alreadyVisited) {
    Set outEdges = currentNode.getOutEdges();
    boolean flag = false;
    if(outEdges.size()>0){
      for (Iterator it2 = outEdges.iterator(); it2.hasNext() && 
            flag == false;) {
        DependenceFlowEdge outEdge = (DependenceFlowEdge)it2.next();
        if(((DependenceFlowNode)outEdge.getSink()).getInstruction() == null) 
	  continue;
        if(outEdge.getisBackWardsPointing()) continue;
	if(_notInSCC.contains(currentNode)) continue;
	if(!_alreadyCheckedIfNotInSCC.contains(currentNode)) {
	   _alreadyCheckedIfNotInSCC.add(currentNode);
	   if(!currentNode.isSCC()) {
             _notInSCC.add(currentNode);
	     continue;
	   }
	}
	if(outEdge.getSink() == startNode) {
          return true;
        }
        else if(!alreadyVisited.contains(outEdge.getSink())){
          alreadyVisited.add(outEdge.getSink());
          flag = isSCCAndOneNotBackPointingRec(startNode, outEdge.getSink(), 
	                                       alreadyVisited);
          //if(flag)
	  //  _alreadyFoundInSCC.add(outEdge.getSink());
	}
      }
    }
    return flag;
     
  }
  
  
  
  /** 
   * Returns the nodes that have no outgoing edges.  If all the edges are
   * drawn to point down, these nodes are at the bottom of the graph.  NOTE:
   * This set is not just a copy.  If you alter it, you will alter the
   * graph itself.
   */
  public Set getBottomSinks() {
    return _bottomSinks;
  }
  
  /** 
   * Returns the name of <code>cls</code>, which is simple except for arrays
   * 
   * @param cls 
   */
  private String getName(Class cls) {
    if (cls == null) {
      return "null";
    } // end of if ()
    else if (cls.isArray()) {
      return getName(cls.getComponentType()) + "[]";
    } // end of else if ()
    else {
      return cls.getName();
    } // end of else
  }
  
  /** 
   * Instantiates a DependenceFlowNode
   */
  protected Node newNode() {
    return new DependenceFlowNode(null, null);
  }
  
  /** 
   * Instantiates a DependenceFlowEdge
   */
  protected Edge newEdge() {
    return new DependenceFlowEdge();
  }
  
  /** 
   * Also replaces <code>oldNode</code> in the set of top sources and bottom
   * sinks
   * 
   * @param oldNode 
   * @param newNode 
   */
  public void replaceNode(Node oldNode, Node newNode) {
    super.replaceNode(oldNode, newNode);
    Set topSources = getTopSources();
    if (topSources.contains(oldNode)) {
      topSources.remove(oldNode);
      topSources.add(newNode);
    } // end of if ()
    Set bottomSinks = getBottomSinks();
    if (bottomSinks.contains(oldNode)) {
      bottomSinks.remove(oldNode);
      bottomSinks.add(newNode);
    } // end of if ()
  }
  
   
  /** 
   * Appends the type of each node to its label before calling
   * <code>super.toDot()</code>
   */
  public String toDot() {
    Iterator iter = getAllNodes().iterator();
    StringBuffer sb = new StringBuffer();
    while (iter.hasNext()) {
      DependenceFlowNode node = (DependenceFlowNode) iter.next();
      sb.delete(0, sb.length());
      sb.append(node.getPrimaryDotLabel()).append('\n');
      Class type = node.getType();
      sb.append(getName(type));
      int line = node.getSourceLine();
      if (line != DependenceFlowNode.UNKNOWN_LINE) {
        sb.append('\n').append("line: ").append(line);
      } // end of if ()
      node.setDotAttribute("label", sb.toString());
    }
    String dotString = super.toDot();
    String dotStringMinSubStr = new String();
    String dotStringMaxSubStr = new String();
    dotStringMinSubStr = "{rank = minrank; ";
    dotStringMaxSubStr = "{rank = maxrank; ";
    int lastIndex = dotString.lastIndexOf("}");
    dotString = dotString.substring(0, lastIndex-1);
    dotString = dotString + "{rank = source; " + _sTART.getDotName() + "}\n";
    for (Iterator it = getAllNodes().iterator(); it.hasNext();) {
      DependenceFlowNode node = (DependenceFlowNode) it.next();
      if((node != _sTART)&&(node != _sTART)) {
    	dotStringMinSubStr = dotStringMinSubStr + node.getDotName() + "; ";
    	dotStringMaxSubStr = dotStringMaxSubStr + node.getDotName() + "; ";
      }
       
    }  
    dotStringMinSubStr = dotStringMinSubStr + "}\n";
    dotStringMaxSubStr = dotStringMaxSubStr + "}\n";
    dotString = dotString + dotStringMinSubStr;
    dotString = dotString + dotStringMaxSubStr;
    dotString = dotString + "{rank = source; " + _sTOP.getDotName() + "}\n";
    dotString = dotString + "}\n";
    return dotString;
  }
}

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


/** This class stores the list of instructions for use by the scheduling
 * routines.  It has some useful methods and a nested class for storing data for
 * an instruction.  I wrote this for modulo scheduling, but with the
 * hope that the other schedulers would be slowly migrated over to using this.
 *
 * The class extends hashmap and
 * keys = scheduled time of execution for instructions
 * values = HashSets of InstructionObjects to be executed at this time
 *
 * MSchedHash extends this class and contains modulo scheduling specific methods
 * and data 
 *
 * @author Kris Peterson
 */
public class InstructionList extends HashMap
{

  /** This nested class saves an instruction and some associated information,
   *  which my methods will use.  MSchedInstObject extends this class and
   *  contains modulo scheduling specific methods and data.
   */
  public class InstructionObject {
  
    /**
    * This nested class within MSchedInstObject is for finding and saving
    * the nets connecting different instructions
    */
    private class ConnectingOperands {
    
      /**
      key is a successor instruction
      value is HashSet of all nets connecting this instruction with its
            successor ignoring predicates
      */
      private HashMap _succConns = new HashMap();
      /**
      key is a predecessor instruction
      value is HashSet of all nets connecting this instruction with its
            predecessor ignoring predicates
      */
      private HashMap _predConns = new HashMap();
      /**
      key is a successor instruction
      value is HashSet of all nets connecting this instruction with its
            successor including predicates
      */
      private HashMap _succConnsAll = new HashMap();
      /**
      key is a predecessor instruction
      value is HashSet of all nets connecting this instruction with its
            predecessor including predicates
      */
      private HashMap _predConnsAll = new HashMap();
      
      /**
      constructor
      */
      public ConnectingOperands() {
      }
      
      /**
      find the connecting nets between two instructions, excluding
      predicates.  If they are 
      known, return that, but if not, find them.
      */
      public HashSet getPredConns(Instruction i) {
        //check if the connecting nets are saved
	if(!_predConns.containsKey(i)) {
	  //if not, then foreach input to this instruction, if parent
	  //instruction i defines that net, save it
	  HashSet operandList = new HashSet();
	  for (Iterator vIt = getInstIns().iterator(); vIt.hasNext();) {
            Operand op = (Operand) vIt.next();
	    InstructionObject pObj = (InstructionObject)instToObjMap.get(i);
	    if(pObj.getInstOuts().contains(op))
	      operandList.add(op);
	  }
	  //save and return the set of connecting nets
	  _predConns.put(i, operandList);
	  return operandList;
	}
	//if it was saved, return
        return (HashSet)_predConns.get(i);
      }
    
      /**
      find the connecting nets between two instructions, including
      predicates.  If they are 
      known, return that, but if not, find them.
      See comments within the method above for understand how this works
      */
      public HashSet getPredConnsAll(Instruction i) {
        if(!_predConns.containsKey(i)) {
	  HashSet operandList = new HashSet();
	  //notice getInstInsAll() is called instead of getInstIns()
	  //like above
	  for (Iterator vIt = getInstInsAll().iterator(); vIt.hasNext();) {
            Operand op = (Operand) vIt.next();
	    InstructionObject pObj = (InstructionObject)instToObjMap.get(i);
	    if(contains(pObj.getInstOuts(), op))
	      operandList.add(op);
	  }
	  _predConns.put(i, operandList);
	  return operandList;
	}
        return (HashSet)_predConns.get(i);
      }
      
      private boolean contains(HashSet savedOuts, Operand op) {
        for (Iterator vIt = savedOuts.iterator(); vIt.hasNext();) {
          Operand opTmp = (Operand) vIt.next();
          if(opTmp.toString().compareTo(op.toString())==0)
      	    return true;
        }
        return false;
      }
    
      /**
      find the connecting nets between an instruction and one of its
      successors, excluding
      predicates.  If they are 
      known, return that, but if not, find them.
      */
      public HashSet getSuccConns(Instruction i) {
        if(!_succConns.containsKey(i)) {
	  HashSet operandList = new HashSet();
	  //notice getInstOuts() is called this time
	  for (Iterator vIt = getInstOuts().iterator(); vIt.hasNext();) {
            Operand op = (Operand) vIt.next();
	    InstructionObject cObj = (InstructionObject)instToObjMap.get(i);
	    //here getInstIns() is called instead of getInstOuts() like
	    //above
	    if(cObj.getInstIns().contains(op))
	      operandList.add(op);
	  }
	  _succConns.put(i, operandList);
	  return operandList;
	}
        return (HashSet)_succConns.get(i);
      }
    
      /**
      find the connecting nets between an instruction and one of its
      successors, including predicates.  If they are 
      known, return that, but if not, find them.
      */
      public HashSet getSuccConnsAll(Instruction i) {
        if(!_succConns.containsKey(i)) {
	  HashSet operandList = new HashSet();
	  for (Iterator vIt = getInstOuts().iterator(); vIt.hasNext();) {
            Operand op = (Operand) vIt.next();
	    InstructionObject cObj = (InstructionObject)instToObjMap.get(i);
	    //notice getInstInsAll()
	    if(cObj.getInstInsAll().contains(op))
	      operandList.add(op);
	  }
	  _succConns.put(i, operandList);
	  return operandList;
	}
        return (HashSet)_succConns.get(i);
      }
    
    }//end nested class ConnectingOperands

    /**
    using the philosophy that using more memory to reduce calculations increases
    speed, the latency for an instruction is saved here.  That way, it only
    needs to be calculated once and all other times, this value will be
    returned.
    */
    private float _runlength = -1;
    /**
    the instruction associated with this object.
    */
    public Instruction inst;
    /**
    a list of operands used in predicates that are defined by this instruction.
    */
    public ArrayList listOfPredsDefnd = new ArrayList();
    /**
    a list of operands used in predicates that are used by this instruction.
    */
    public ArrayList listOfPredsUsed = new ArrayList();
    /**
    not used at this time, but may be used later if other schedulers use this.
    */
    public float execTimeTemp = -1;
    /**
    a set of uses by this instruction not including predicates unless this
    instruction is a store to a primal.
    */
    private HashSet _instIns = new HashSet();
    /**
    a set of all uses by this instruction including predicates 
    */
    private HashSet _instInsAll = new HashSet();
    /**
    a flag telling if this instruction has any inputs 
    */
    public boolean noIns = false;
    /**
    a set of definitions by this instruction
    */
    private HashSet _instOuts = new HashSet();
    /**
    a flag telling if this instruction has any outputs 
    */
    public boolean noOuts = false;
    /**
    a flag telling if this instruction has any primal outputs 
    */
    public Boolean _isAnOutPrimal = null;
    /**
    an arraylist of successor instructions (I can't remember why I used
    ArrayList instead of HashSet).
    */
    private ArrayList _listOfSuccs = new ArrayList();
    /**
    a boolean telling whether there are successors or not.  It is of type
    Boolean instead of boolean, because that gave me a third possible value
    (null), which I could use to know if it has already been determined if there
    are any successors.  If it is null, it will first try to create the list of
    successors before returning a value for this variable (the it I'm referring
    to is a method below which returns whether there are successors or not).
    */
    private Boolean _noSuccs = null;
    /**
    an arraylist of predecessor instructions
    */
    private ArrayList _listOfPreds = new ArrayList();
    /**
    a boolean telling whether there are successors or not.  
    */
    private Boolean _noPreds = null;
    
    private Operand _iterationPred = null;

    /**
    see nested class "ConnectingOperands".  This is an instantiation
    of that class and is used to save this instruction's connecting nets
    with other instructions
    */
    private ConnectingOperands _connOps = new ConnectingOperands();


    public InstructionObject() {
      _iterationPred = null;
    }
  
    public InstructionObject(Instruction i) {
      inst = i;
      updatePredLists();
      _iterationPred = null;
    }
    
    /**
    to make it easier to debug, I added this so that I can print these objects
    and know which object I'm refering to, without having to reference the
    instruction itself in the print statement (which may be difficult if I'm
    trying to print a collection of objects, since otherwise I'd have to put in
    a loop to go through each object and print its instruction)
    */
    public String toString() {
      return inst.toString();
    }
    

    /**
    get the list of connecting nets between this instruction and parent
    instruction i, excluding predicates.  
    */
    public HashSet getPredConns(Instruction i) {
      return _connOps.getPredConns(i);
    }
    
    /**
    get the list of connecting nets between this instruction and parent
    instruction i, including predicates.  
    */
    public HashSet getPredConnsAll(Instruction i) {
      return _connOps.getPredConnsAll(i);
    }
    
    /**
    get the list of connecting nets between this instruction and child
    instruction i, excluding predicates.  
    */
    public HashSet getSuccConns(Instruction i) {
      return _connOps.getSuccConns(i);
    }
    
    /**
    get the list of connecting nets between this instruction and child
    instruction i, including predicates.  
    */
    public HashSet getSuccConnsAll(Instruction i) {
      return _connOps.getSuccConnsAll(i);
    }

    //use carefully!
    public void addIn(Operand inputOp) {
      getInstIns();
      _instIns.add(inputOp);
      _iterationPred = inputOp;
    }
    
    public void addOut(Operand outputOp) {
      getInstOuts();
      _instOuts.add(outputOp);
      //_iterationSucc = outputOp;
    }
    
    public void changeItPred(Operand op1, Operand op2) {
      if((_iterationPred != null) && (_iterationPred == op1))
        _iterationPred = op2;
    }
    
    public BooleanOperand getItPred() { return (BooleanOperand)_iterationPred; }
    public void setItPred(Operand itPredOp) { _iterationPred = itPredOp; }
    
    /**
    this guy finds all the uses for an instruction
    */
    public HashSet getInstIns() {
      //if they've already been found or if there are none, than return the set
      //or null
      if(_instIns.size()>0) 
        return _instIns;
      else if(noIns)
        return null;
	
      if(AStore.conforms(inst)) {
        _instIns.add(AStore.getAddrDestination(inst));
        _instIns.add(AStore.getValue(inst));
      }
      else {
      
	//otherwise get them all and save them to the set
	Operand op;
	int numDefs = inst.getNumberOfDefs();
	int total = inst.getNumberOfOperands();
	for(int i = numDefs; i < total; i++) {
          op = inst.getOperand(i);
          if(op != null)
            _instIns.add(op);
	}

	//ignore predicates when scheduling unless, ignorePredsNicht is true or 
	//one of the defs is a primal
	/*if(isAnOutPrimal() || GlobalOptions.doNotIgnorePreds) {
          BooleanEquation predTmp = inst.getPredicate();
	  if(predTmp != null) {
            LinkedList boolsTmp = predTmp.listBooleanOperands();
            for (Iterator itin = ((LinkedList)boolsTmp.clone()).iterator(); 
        	 itin.hasNext(); ) {
              op = (Operand)itin.next();
              _instIns.add(op);
            }
          }
	}*/
      }
      //_instIns.addAll(_cntrlIns);
      //save the flag:
      if(_instIns.size()==0)
        noIns = true;
      return _instIns;
    }
    
    /**
    this guy adds the predicate uses to the other uses and saves it to
    instInstAll.  For normal scheduling instIns is all that is necessary, but
    for adding loads and stores instInsAll is necessary
    */
    public HashSet getInstInsAll() {
      _instInsAll.addAll(getInstIns());
      
      //if(!(isAnOutPrimal() || GlobalOptions.doNotIgnorePreds)) {
        BooleanEquation predTmp = inst.getPredicate();
	if(predTmp != null) {
          LinkedList boolsTmp = predTmp.listBooleanOperands();
          for (Iterator itin = ((LinkedList)boolsTmp.clone()).iterator(); 
               itin.hasNext(); ) {
            Operand op = (Operand)itin.next();
            _instInsAll.add(op);
          }
        }
      //}
      return _instInsAll;
    }
    
    /**
    finds the set of all instruction definitions and sets the flag.  It also
    checks if any outputs are primal and sets that flag as well
    */
    public HashSet getInstOuts() {
      if(_instOuts.size()>0) 
        return _instOuts;
      else if(noOuts)
        return null;
      
      if(AStore.conforms(inst)) {
        _instOuts.add(AStore.getPrimalDestination(inst));
	_isAnOutPrimal = new Boolean(true);
      }
      else {
	_isAnOutPrimal = new Boolean(false);
	Operand op;
	int numDefs = inst.getNumberOfDefs();
	for(int i = 0; i < numDefs; i++) {
          op = inst.getOperand(i);
          if(op != null) {
            _instOuts.add(op);
            if(op.isPrimal())
              _isAnOutPrimal = new Boolean(true);
          }
	}
      }
      if(_instOuts.size()==0)
        noOuts = true;
      return _instOuts;
    }
        
    /**
    return if any outputs are primal.  if the Boolean flag == null, meaning it
    was never determined, then run getInstOuts, who will check if any are
    */
    public boolean isAnOutPrimal() {
      if(_isAnOutPrimal == null)
        getInstOuts();
      return _isAnOutPrimal.booleanValue();
    }

    /**
      return whether or not an instruction has any successors
    */
    public boolean getNoSuccs() {
      if(_noSuccs == null)
        getListOfSuccs();
      return _noSuccs.booleanValue();
    }
    
    /**
      return whether or not an instruction has any predecessors
    */
    public boolean getNoPreds() {
      if(_noPreds == null)
        getListOfPreds();
      return _noPreds.booleanValue();
    }
    
    /**
    using the dependence flow graph create a list of predecessor instructions. 
    If this is used by other schedulers it might be useful or even necessary to
    look at uses and defs instead of the dependence flow graph if the dependence
    flow graph is undefined to determine this.
    */
    public ArrayList getListOfPreds() {
      if(_listOfPreds.size()>0) {
        return _listOfPreds;
      }
      else if((_noPreds!=null)&&(_noPreds.booleanValue()))
        return null;
      
      DependenceFlowNode childNode = _dfgI.findNode(inst);
      for (Iterator it1 = ((Set)(childNode.getInEdges())).iterator(); 
              it1.hasNext();) {
        DependenceFlowEdge pEdge = (DependenceFlowEdge)it1.next();
        DependenceFlowNode pNode = (DependenceFlowNode)pEdge.getSource();
        Instruction pInst = pNode.getInstruction();
	if(pInst == null)  continue;
	InstructionObject pObj = (InstructionObject)instToObjMap.get(pInst);
	//if(pEdge instanceof DependenceCntrlFlowEdge)
	//  _cntrlIns.addAll(pObj.getInstOuts());
	_listOfPreds.add(pObj); 
      }
      if(_listOfPreds.size()==0) _noPreds = new Boolean(true);
      else _noPreds = new Boolean(false);
      return _listOfPreds;
    }
  
    public void resetSuccsList() {
      _listOfSuccs = null;
      _noSuccs=null;
      _instIns=null;
      _instInsAll=null;
      noIns = false;
      _instOuts=null;
      noOuts = false;
      _isAnOutPrimal = null;
      _listOfPreds = null;
      _noPreds = null;
      
    }
    
    public ArrayList getSuccsForDFG(){
      ArrayList listOfSuccs = new ArrayList();
      for (Iterator it1 = getInstOuts().iterator(); it1.hasNext();) {
        Operand out = (Operand)it1.next();
        for (Iterator it2 = getInstSet().iterator(); it2.hasNext();) {
	  InstructionObject cObj = (InstructionObject)it2.next();
          if((!((Getelementptr.conforms(cObj.inst))&&
        	(AStore.conforms(inst))))) {

            boolean stop = false;
	    for (Iterator it3 = cObj.getInstIns().iterator(); 
	          it3.hasNext() && !stop;) {
              Operand op = (Operand)it3.next();
	      if(op.toString().compareTo(out.toString()) == 0) {
	        stop = true;
        	listOfSuccs.add(cObj);
              }
            }
          }
        }
      }
      return listOfSuccs;
    
    }
    /**
    using the dependence flow graph create a list of successor instructions. 
    */
    public ArrayList getListOfSuccs() {
      if(_listOfSuccs.size()>0)
        return _listOfSuccs;
      else if((_noSuccs!=null)&&(_noSuccs.booleanValue()))
        return null;
      
	DependenceFlowNode parentNode = _dfgI.findNode(inst);
	for (Iterator it1 = parentNode.getOutEdges().iterator(); 
        	it1.hasNext();) {
          DependenceFlowEdge cEdge = (DependenceFlowEdge)it1.next();
          DependenceFlowNode cNode = (DependenceFlowNode)cEdge.getSink();
          Instruction cInst = cNode.getInstruction();
	  if(cInst == null) continue;
	  InstructionObject cObj = (InstructionObject)instToObjMap.get(cInst);
	  _listOfSuccs.add(cObj); 
	}
	if(_listOfSuccs.size()==0) _noSuccs = new Boolean(true);
	else _noSuccs = new Boolean(false);
	return _listOfSuccs;
      /*}
      else {
	for (Iterator it1 = getInstOuts().iterator(); it1.hasNext();) {
          Operand out = (Operand)it1.next();
	  for (Iterator it2 = getInstSet().iterator(); it2.hasNext();) {
            InstructionObject cObj = (InstructionObject)it2.next();
	    if((!((Getelementptr.conforms(cObj.inst))&&
	          (AStore.conforms(inst))))) {

	      for (Iterator it3 = cObj.getInstIns().iterator(); it3.hasNext();) {
                Operand op = (Operand)it3.next();
	        if(op.toString().compareTo(out.toString()) == 0) {
	           _listOfSuccs.add(cObj);
	        }
	      }
	    }
	  }
        }
	if(_listOfSuccs.size()==0) _noSuccs = new Boolean(true);
	else _noSuccs = new Boolean(false);
	return _listOfSuccs;
      }*/
    }

    /**
    this is a fun little method, which is mostly useful for modulo scheduling,
    but might be useful for other schedulers or even anything else.  It looks
    for chains of loads and stores through a block and deletes them all and
    changes all instructions who use any block variables defined somewhere in
    the chain so that they use the source block variable.  I wrote this, because
    the unrolled loop bodies created after modulo scheduling and used to create
    the epilog and prolog, contain long chains of loads and stores.  Not only
    are they unnecessary and wasteful uses of space, they would even cause
    incorrect execution, because these loads could use values set in the kernal
    instead of within the epilog or it could mix up values from different
    iterations (and even in the prolog and epilog there are multiple
    iterations running concurrently). 
    */
    public boolean killLSChainsRecursive(HashSet extraBlocks, 
                                         HashSet extraInsts,
                                         HashSet alreadyVisited) {    
      //if(!getNoSuccs()) {
        boolean isNotLeaf = false;
	//foreach of an instructions successors
	//for (Iterator vIt = getSuccsForDFG().iterator(); 
        //     vIt.hasNext();) {
	
        for (Iterator it2 = getInstSet().iterator(); it2.hasNext();) {
          InstructionObject childObj = (InstructionObject)it2.next();
	  if(childObj == null) continue;
          if((!((Getelementptr.conforms(childObj.inst))&&
        	(AStore.conforms(inst))))) {

            //boolean stop = false;
	    boolean isChild = false;
	    for (Iterator it1 = getInstOuts().iterator(); 
                   it1.hasNext() && !isChild;) {
              Operand out = (Operand)it1.next();
	      for (Iterator it3 = childObj.getInstIns().iterator(); 
	    	    it3.hasNext() && !isChild;) {
                Operand op = (Operand)it3.next();

	        if(op.toString().compareTo(out.toString()) == 0) {
	          isChild = true;
                }
              }
	    }
	    //InstructionObject childObj = (InstructionObject) vIt.next();
	    //to prevent getting caught in an infinite loop, we must take care not
	    //to go into an instruction again:
	    if(isChild) {
	      if(alreadyVisited.contains(childObj)) continue;
	      alreadyVisited.add(childObj);
	      //if this is a load or store:
	      if((Store.conforms(childObj.inst))||
	         (Load.conforms(childObj.inst))) {
		Operand primal = null;
		Operand opToDie = null;
		if(Store.conforms(childObj.inst)) {
		  primal = Store.getDestination(childObj.inst);
		}
		if(Load.conforms(childObj.inst)) {
		  primal = Load.getSource(childObj.inst);
		  opToDie = Load.getResult(childObj.inst);
		}

		if(primal.getFullName().indexOf("modPrim")>=0) {
		  isNotLeaf = true;
		  //save the intermidiate block variables so they can be replaced
		  //later:
		  if(opToDie != null)
	            extraBlocks.add(opToDie);
		  //if the child is a not a leaf (which killLSChainsRecursive
		  //returns) or if the child is a load, save this instruction for
		  //deletion:
		  if((childObj.killLSChainsRecursive(extraBlocks, extraInsts,
	                                            alreadyVisited))||
		     (Load.conforms(childObj.inst)))
	            extraInsts.add(childObj.inst);
		}
	      }
	    }
          }
        }
	//}
	//an instruction is a leaf if it is the final load or store in the chain
	//we don't want to delete the final store (if the final node is a store
	//and not a load), because the prolog still needs to save these values
	//for priming the kernal (and the epilog will kill any extra modPrim
	//stores later)
	return isNotLeaf;
      //}
      //return false;
    
    }
    
    /**
    this method checks to see if any of its definitions happens to be used in a
    predicate, and saves that to its list of defined predicates
    */
    public void updatePredLists() {
    
      int numDefs = inst.getNumberOfDefs();
      for(int i = 0; i < numDefs; i++) {
        Operand op = inst.getOperand(i);
	if(_usedPredOperands.contains(op))
	  listOfPredsDefnd.add(op);
      }
    }
    
    /**
    the next two methods have been retired and the two after them are used
    instead.  The problem is, if you make a copy of an object from within one
    InstructionList and save that in a second, the objects use the global
    private variables from the original, source InstructionList object instead
    of the new one.
    */
    public InstructionObject copySaveOps() {
      
      //Instruction instOld = inst;
      Instruction instCpy = inst.copySaveOps();
      InstructionObject instObjCpy = new InstructionObject(instCpy);
      instObjCpy.listOfPredsDefnd = listOfPredsDefnd;
      instObjCpy.listOfPredsUsed = listOfPredsUsed;
      BooleanEquation instBoolEQCopy = inst.getPredicate();
      instCpy.setPredicate(instBoolEQCopy);
      instObjCpy.setItPred(_iterationPred);
      
      return instObjCpy;
    
    }
  
    /**
    retired, but left in case some other use pops up...
    */
    public InstructionObject copyNewOps() {
      
      
      InstructionObject instObjCpy = copySaveOps();
      instObjCpy.newOps();
      
      return instObjCpy;
    
    }
    
    
    /**
    This method is used to create a copy of "this" instruction object.  The copy
    has the same Operands (not only the same name, but the same objects)
    */
    public void copySaveOps(InstructionObject instObj) {
      
      //Instruction instOld = inst;
      //Instruction instCpy = instObj.inst.copySaveOps();
      
      //copy instruction
      inst = instObj.inst.copySaveOps();
      
      //InstructionObject instObjCpy = new InstructionObject(instCpy);
      
      //copy pred use and def lists:
      listOfPredsDefnd = instObj.listOfPredsDefnd;
      listOfPredsUsed = instObj.listOfPredsUsed;
      
      //copy instruction predicate:
      BooleanEquation instBoolEQCopy = instObj.inst.getPredicate();
      inst.setPredicate(instBoolEQCopy);
      
      //return instObjCpy;
      
      _iterationPred = instObj.getItPred();
    
    }
  
    /**
    copy "this" instruction object, but replace all the block operands with new
    ones.  This is used by the epilog because it has multiple iterations of the
    loop running it, all starting at different points (not starting at their
    beginning at different offsets of time, but starting at truly different 
    locations).  These different iterations need to use different block operands
    to prevent confusions and data transfer between interations that shouldn't
    occur.
    */
    public void copyNewOps(InstructionObject instObj) {
      
      
      copySaveOps(instObj);
      //System.err.println("b4 " + this);
      newOps();
      //System.err.println("this " + this);
      
      //return instObjCpy;
    
    }
    
    /**
    replaces operands with new ones.  But the new operands are saved so that
    when another instruction is found that uses the same operand, it will use
    the same newly created operand, instead of creating yet another and
    disconnecting them.
    */
    public void newOps() {
      
      Operand op;
      int total = inst.getNumberOfOperands();
      Operand newOp = null;
      for(int i = 0; i < total; i++) {
        op = inst.getOperand(i);
	
	//if an instruction has two operands of the same name they'll end up
	//getting replaced twice.  For example if we have the instruction:
	//%tmp_1 = aaa_mul %tmp_2 %tmp_2
	//during the first pass through it will see the first tmp_2 and make a
	//operand for it called tmp2_0, probably and then it will replace both
	//operands.  The instruction will now look like this:
	//%tmp_1 = aaa_mul %tmp_2_0 %tmp_2_0
	//but the loop will continue and it will look at the second tmp_2
	//variable, which now has the name tmp2_0.  It will think this is a new
	//operand and create yet another, probably called tmp_2_0_0 and replace
	//both again giving us this:
	//%tmp_1 = aaa_mul %tmp_2_0_0 %tmp_2_0_0
	//all other instructions using or defining tmp_2 will be using tmp_2_0
	//and this one will be unconnected.  To prevent this, I check if I've
	//already created this operand and if so skip to the next operand:
	if(_oldToNew.containsValue(op)) continue;
	newOp = null;
        /*if((op != null) && 
	  ((op.isBlock())||
	   ((op.isBoolean())&&(!_usedPredOperands.contains(op))))&&
	  (newOp != op)) {*/
        
	if(op != null) {
	  //if we've already created a new operand for this guy, use that
	  if(_oldToNew.containsKey(op))
	    newOp = (Operand)_oldToNew.get(op);
	  else {//otherwise create a new block or boolean as appropriate:
	    if(op.isBlock())
	      newOp = Operand.nextBlock(op.getFullName());
	    else if(op.isBoolean())
	      newOp = Operand.nextBoolean(op.getFullName());
	   _oldToNew.put(op, newOp);
	  }
	  if(newOp != null) //replace the operand (and since we only gave newOp
	                    //a value if op was a block or boolean, all other
			    //types of operands such as primals will not be 
			    //changed)
	    replaceOperand(op, newOp);
	}
      }
      //do the same in the predicate:
      newOp = null;
      BooleanEquation predTmp = inst.getPredicate();
      if(predTmp != null) {
        LinkedList boolsTmp = predTmp.listBooleanOperands();
        for (Iterator itin = ((LinkedList)boolsTmp.clone()).iterator(); 
             itin.hasNext(); ) {
          op = (Operand)itin.next();
          if(op != null) {
	    if(_oldToNew.containsKey(op))
	      newOp = (BooleanOperand)_oldToNew.get(op);
	    else {
	      newOp = Operand.nextBoolean(op.getFullName());
	     _oldToNew.put(op, newOp);
	    }
	    replaceOperand(op, newOp);
	  }
        }
      }
      if((_iterationPred != null)) {
	if(_oldToNew.containsKey(_iterationPred))
	  newOp = (Operand)_oldToNew.get(_iterationPred);
	else {//otherwise create a new block or boolean as appropriate:
	 newOp = Operand.nextBoolean(_iterationPred.getFullName());
	 _oldToNew.put(_iterationPred, newOp);
	}
        _iterationPred = newOp;
      }
    }
  
    /**
    replace an operand in an instruction, both within the instruction and its
    predicate.  This was written so that the listOfPredsUsed and defined could
    be edited.
    */
    public void replaceOperand(Operand oldOp, Operand newOp) {
      inst.replaceOperand(oldOp, newOp);
      if((oldOp.isBoolean())&&(newOp.isBoolean())) {
	BooleanEquation predTmp = (BooleanEquation)inst.getPredicate().clone();
	BooleanEquation neweq = new BooleanEquation(predTmp);
	neweq = predTmp.replaceBool((Bool)oldOp, (Bool)newOp);
	inst.setPredicate(neweq); 
	//_usedPredOperands.remove(oldOp);
	_usedPredOperands.add(newOp);
	listOfPredsUsed.remove(oldOp);
	listOfPredsUsed.add(newOp);
	changeItPred(oldOp, newOp);
      }
      updatePredLists();
      /*listOfPredsDefnd.remove(oldOp);
      listOfPredsUsed.remove(oldOp);
      listOfPredsDefnd.add(newOp);
      listOfPredsUsed.add(newOp);*/
      
    }

    /**
    return the latency of an instruction.  Once the latency has been found, it
    is saved to the private variable _runlength and in the future its value will
    be returned instead of recalculating the latency.  If the latency is an
    ALoad or AStore, the latency is found by checking in the chipdef hardware
    information class and otherwise it is found on the instruction itself
    */
    public float getRunLength() {
      if(_runlength != -1)
        return _runlength;
      
      float latency = 0;
      /*if((ALoad.conforms(inst))&&(_chipdef != null)) {
        Operand primSource = ALoad.getPrimalSource(inst);
        latency = _chipdef.getMemInitReadLat(primSource);
      }
      else if((AStore.conforms(inst))&&(_chipdef != null)) {
        Operand primDest = AStore.getPrimalDestination(inst);
        latency = _chipdef.getMemInitWriteLat(primDest);
      }
      else*/
        latency = inst.getRunLength();
	
      if(!GlobalOptions.packInstructions && latency < 1) latency=1;
      
      _runlength = latency;
      
      return latency;
      
      
    }
  
  }//end class InstructionObject
  
  /** Often I know the instruction I am interested in, but not its associated
  instruction object.  This nested class was written to help me find objects.  
  It extends HashMap where 
  keys = instruction
  values = instruction object
  */
  public class InstToObjMap extends HashMap {
  
    public InstToObjMap() {
      super();
    }
    
    /**
    Given an instruction i, this method returns the associated
    InstructionObject.  After searching through all InstructionObjects to find
    that one that is associated with i, it saves the relationship in THIS (since
    THIS is a hashmap) and in the future if the same instruction i is requested,
    the value in THIS is returned and another search is unnecessary.
    */
    public InstructionObject get(Instruction i) {
      //if instruction i is known, return its InstructionObject
      if(this.containsKey(i))
        return (InstructionObject)super.get(i);
      //else search through all Instruction objects for i, save i and return
      //that object
      for (Iterator vIt2 = _allInstsSet.iterator(); vIt2.hasNext();) {
        InstructionObject instObj = (InstructionObject) vIt2.next();
        Instruction inst = instObj.inst;
	this.put(inst, instObj);
	if(inst == i)
	  return instObj;
      }
      //hopefully it will never get to this statement:
      return (InstructionObject)super.get(i);
    }
    
  
  } //end class InstToObjMap
    
  /** This class was written, to deal with the problem with floating point
  numbers that 8.095 can become corrupted and become 8.0949999999.  Since I save
  my instructions in sets in a hashmap (InstructionList extends HashMap), where
  the key is the time and the value is the set of instructions in that time, if
  these corruptions are allowed we could have a instruction at 8.09499999, and
  one at 8.095, and one at 8.0950001, when we would want them all to be saved in
  the same time slot.  This method rounds all times to within 1/100th.
  */
  public class RoundTimeOff {
  
    
    public RoundTimeOff() {
    
    }
    
    public float roundTime(float t) {
      return ((float)Math.round(t*100))/100;
    }
  
  } //end RoundTimeOff
  
  /**
  This is an instantian of the instruction to InstructionObject mapper
  */
  public InstToObjMap instToObjMap = new InstToObjMap();
  /**
  To make it easier to access all InstructionObjects and to find individual
  objects, I created this private Set to contain copies of them all.  As I said,
  they are stored in InstructionList in sets associated with certain times, but
  to make it unnecessary to search through every set at every time, there is one
  set with them all.
  */
  private HashSet _allInstsSet = new HashSet();
  /**
  instatiation of the time rounding class
  */
  private RoundTimeOff _roundTimes = new RoundTimeOff();
  /**
  This is the lowest execution time of an insruction.  It is used by the 
  InstructionList copy functions
  */
  private float _minTime = 99999;
  /**
  This is the highest execution time of an insruction.  It is used by the 
  InstructionList copy functions
  */
  private float _maxTime = -99999;
  /**
  This set was created to help during creation of InstructionObjects to know
  what predicate operands are being used.  Knowing this list of all predicates
  used in the schedule, when one of these operands is defined in an
  InstructionObject, it can be noted that that definition is the definition of a
  predicate.
  */
  private HashSet _usedPredOperands = new HashSet();
  /**
  When using the InstructionObject "copyNewOps" method, to ensure that only one
  new operand is created for any existing operand and that all instances of the
  existing operand are all replaced with the same new operand, this hashmap was
  created to save which old operands are associated with which new operands.
  */
  static private HashMap _oldToNew = null;
  /**
  ASAP, ALAP, and modulo scheduling can get stuck in an infinite loop, trying to
  create longer and longer schedules, when in reality no schedule is possible. 
  To know when it is stuck in a loop, we just need to know the longest possible
  schedule, which would be when every instruction ran serially.  The time
  necessary for such a schedule is the sum of all latencies for all instructions
  in the list.  _maxRunTime saves this value after it is created when
  instructions are added to the list.
  */
  private float _maxRunTime = 0;

  /**
  this points the hardware description class instatiation
  */
  private ChipDef _chipdef = null;
  /**
  this points to a dependence flow graph for the hyperblock that is being
  scheduled
  */
  private DependenceFlowGraph _dfgI = null;
  /**
  this points to the hyperblock being scheduled
  */
  public BlockNode _node = null;
  //static private HashSet _usedPreds = new HashSet();
  
  public InstructionList() {
    super();
    _dfgI = null;
  }

  public InstructionList(ChipDef chipdef, float maxRunTime) {
    this();
    _chipdef = chipdef;
    _maxRunTime = maxRunTime;
  }

  public InstructionList(BlockNode bNode, ChipDef chipdef) {
    this();
    _chipdef = chipdef;
    _node = bNode;
    //read in instructions from a node:
    readInList(bNode);
  }
  
  public InstructionList(ChipDef chipdef, DependenceFlowGraph dfg) {
    this();
    _chipdef = chipdef;
    _dfgI = dfg;
  }
  
  /**
  if the dependence flow graph was unknown at the time when the InstructionList
  was instantiated, save it now
  */
  public void saveDFG(DependenceFlowGraph dfg) {
    _dfgI = dfg;
  
  }
  
  /**
  Instructions save their exection time.  If instructions have already been
  scheduled, this method can be called to place them in the correct time slots
  in InstructionList for their given execution times.
  */
  public void scheduleToExecTime() {
    ArrayList times = new ArrayList(this.keySet());
    sort(times);
    for (Iterator vIt = times.iterator(); vIt.hasNext();) {
      Float execTimeFl = (Float) vIt.next();
      float execTime = execTimeFl.floatValue();
      HashSet iList = getAllAtTime(execTime);
      for (Iterator vIt2 = ((HashSet)iList.clone()).iterator(); 
             vIt2.hasNext();) {
	InstructionObject instObj = (InstructionObject) vIt2.next();
	Instruction inst = instObj.inst;
	float newTime = inst.getExecTime();
	remove(execTime, instObj);
	put(newTime, instObj);
      }
    }
  }
  
  /**
  returns the certain maximum possible schedule length, which has been saved in
  _maxRunTime, after being calculated when the schedule was read in.
  */
  public float getMaxRunTime() { return _maxRunTime; }

  /**
  Since InstructionList extends HashMap, the easiest way to determine the number 
  of instructions in the InstructionList is to call "size" on the private
  hashset of all instructions
  */
  public int size() {
    return _allInstsSet.size();
  }
  
  /**
  this was written to adjust the min and max execution times given a certain
  execution time for an instruction
  */
  private void checkMinMaxTimes(float time) {
    _maxTime = Math.max(_maxTime, time); 
    _minTime = Math.min(_minTime, time); 
  }
  
   /**
  if the min or max time is unknown, go through the schedule and find it, save
  and return it; otherwise, just return the saved max value
  */
  public float getMaxTime() {
    if((_minTime == 99999)||(_maxTime == -99999)) {
      for (Iterator vIt = this.keySet().iterator(); vIt.hasNext();) {
        Float time = (Float) vIt.next();
        float timefl = time.floatValue();
        
	_maxTime = Math.max(_maxTime, timefl); 
	_minTime = Math.min(_minTime, timefl); 
      
      }
    
    }
    return _maxTime;
  }
  
  /**
  if the min or max time is unknown, go through the schedule and find it, save
  and return it; otherwise, just return the saved min value
  */
  public float getMinTime() {
    if((_minTime == 99999)||(_maxTime == -99999)) {
      for (Iterator vIt = this.keySet().iterator(); vIt.hasNext();) {
        Float time = (Float) vIt.next();
        float timefl = time.floatValue();
        
	_maxTime = Math.max(_maxTime, timefl); 
	_minTime = Math.min(_minTime, timefl); 
      
      }
    
    }
    return _minTime;
  }
  
  /**
  Since InstructionList extends HashMap but can be thought of as a list of
  instructions, I decided to support both "add" and "put" to use as I thought
  made most sense as I programmed.  They both schedule a given InstructionObject
  at some given time.
  */
  public void put(float time, InstructionObject element) {
    add(time, element);
  }
  
  public void add(float time, InstructionObject element) {
    
    //To correct corrupted floating point numbers, they are first rounded
    time = _roundTimes.roundTime(time);
    
    //a HashSet for the time is either found or created:
    Float timeFl = new Float(time);
    if(!this.containsKey(timeFl))
      super.put(timeFl, new HashSet());
    HashSet instsAtTime = (HashSet)super.get(timeFl);
    //if(!existsAlready(element.inst)) {
    
    //this object is added to the set of all objects
    _allInstsSet.add(element);
    
    //the object is added to the set for this time
    instsAtTime.add(element);
    
    //the min and max times are adjusted
    checkMinMaxTimes(time);
    //}
      
  }
  
  /**
  if there are multiple copies of the same instruction in the schedule, remove
  the duplicates 
  */
  public void removeDuplicateInsts() {
    for (Iterator vIt = ((HashSet)_allInstsSet.clone()).iterator(); 
         vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      //if an instruction exists more than once, remove it
      if(existsAlreadyCnt(instObj.inst) > 1)
        remove(instObj);
    }
  }
  
  /**
  count up how many copies of an instruction, i, exist
  */
  public int existsAlreadyCnt(Instruction i) {
    //HashSet instsAtTime = getAllAtTime(time);
    int cnt = 0;
    //foreach instruction in the InstructionList:
    for (Iterator vIt = _allInstsSet.iterator(); vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      Instruction inst = instObj.inst;
      //if the instructions are the same, increment a counter
      //(I used string compare, because often the instructions or their operands
      //may be different objects and a match may not be made)
      if(inst.toString().compareTo(i.toString())==0) {
	cnt++;
      }
    }
    return cnt;
  }
  
  /**
  This is unused, but it checks to see if instruction i is somewhere in the
  schedule.
  */
  public boolean existsAlready(Instruction i) {
    //HashSet instsAtTime = getAllAtTime(time);
    for (Iterator vIt = _allInstsSet.iterator(); vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      Instruction inst = instObj.inst;
      if(inst.toString().compareTo(i.toString())==0) {
	return true;
      }
    }
    return false;
  }

  /**
  remove all the instructionObjects associated with the instructions in
  Collection c.
  */
  public void removeInsts(Collection c) {
    for (Iterator i = c.iterator(); i.hasNext();) {
      Instruction inst = (Instruction)i.next();
      InstructionObject element = instToObjMap.get(inst);
      remove(element);
    }
  }
  
  public InstructionObject findInstObj(Instruction i) {
    return instToObjMap.get(i);
  }
  
  /**
  remove the instructionObject.  This is a little difficult, since the time is
  unknown, where this instruction might be, which means, all times must be
  attempted.
  */
  public void remove(InstructionObject element) {
    //foreach time attempt to remove element
    for (Iterator vIt = ((HashMap)this.clone()).keySet().iterator(); 
            vIt.hasNext();) {
      Float execTimeFl = (Float) vIt.next();
      float execTime = execTimeFl.floatValue();
      HashSet iList = getAllAtTime(execTime);
      iList.remove(element);
      if(iList.size()==0)
	super.remove(execTimeFl);
    }
    //remove element from the set of all instructionObjects
    _allInstsSet.remove(element);
  }
  
  /**
  remove the instructionObject from the given time
  */
  public void remove(float time, InstructionObject element) {
    //remove from the set of all instructions
    _allInstsSet.remove(element);
    
    //get the set for this time, and remove it from there
    time = _roundTimes.roundTime(time);
    Float timeFl = new Float(time);
    if(this.containsKey(timeFl)) {
      HashSet instsAtTime = (HashSet)super.get(timeFl);
      instsAtTime.remove(element);
      if(instsAtTime.size()==0)
	super.remove(timeFl);
    }
  }
  
  /**
  If you have a collection of instructions that you wish to add to
  InstructionList at the time saved in the Instruction than call this.  It calls
  addAllInstsToTime, with the special flag -9999 for the exection time, which
  tells addAllInstsToTime to schedule the instructions at their saved execution
  times.
  */
  public void addAllInsts(Collection c) {
    addAllInstsToTime(-9999, c);
  }
  
  /**
  import the instructions in collection c and place them in time slot "time",
  unless, time==-9999, in which case, add them to the time saved on the
  instruction.  Also, as each instruction is added
  */
  public void addAllInstsToTime(float time, Collection c) {
    //HashSet usedPredOperands = new HashSet();
    
    for (Iterator i = c.iterator(); i.hasNext();) {
      Instruction inst = (Instruction)i.next();
      //create a new instructionObject for this instruction
      InstructionObject instObj = newInstructionObject();
      instObj.inst = inst;
      
      //schedule to "time" or to the instruction's save execTime
      if(time == -9999) 
        this.add(inst.getExecTime(), instObj);
      else
        this.add(time, instObj);
      //save the operands used in a predicate by this instruction, both in the
      //object's used operand list, and in the private set of all predicate operands
      BooleanEquation pred = inst.getPredicate();
      ArrayList operandList = new ArrayList();
      if(pred != null) {
	operandList.addAll(pred.listBooleanOperands());
      }
      instObj.listOfPredsUsed.addAll(operandList);
      _usedPredOperands.addAll(operandList);

      //calculate the maximum possible latency for a schedule, which is the sum
      //of latencies for all objects:
      _maxRunTime += Math.ceil(instObj.getRunLength());
    }
    
    
    //knowing all the predicate operands, update which instructions define these
    //operands.
    for (Iterator it = c.iterator(); it.hasNext();) {
      Instruction inst = (Instruction)it.next();
      InstructionObject instObj = instToObjMap.get(inst);
      instObj.updatePredLists();
    }
    
  }
  
  public void addNewIns(Instruction i, Operand op) {
    InstructionObject instObj = instToObjMap.get(i);
    instObj.addIn(op);
  }
  
  public void addNewOuts(Instruction i, Operand op) {
    InstructionObject instObj = instToObjMap.get(i);
    instObj.addOut(op);
  }
  
  /**
  this is used to merge two InstructionLists.  Take each set of instructions for
  each time in iList's schedule and add it to the same times in THIS's schedule
  */
  public void addAll(InstructionList iList) {
    for (Iterator vIt = iList.keySet().iterator(); vIt.hasNext();) {
      Float time = (Float) vIt.next();
      float timefl = time.floatValue();
      addAllToTime(timefl, iList.getAllAtTime(timefl));
    }
  }
  
  /**
  add all the instruction objects in collection c to the given time
  */
  public void addAllToTime(float time, Collection c) {
    //_allInstsSet.addAll(c);
    time = _roundTimes.roundTime(time);
    //Float timeFl = new Float(time);
    //if(!this.containsKey(timeFl))
    //  super.put(timeFl, new HashSet());
    //HashSet instsAtTime = (HashSet)super.get(timeFl);
    for (Iterator vIt = c.iterator(); vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      add(time, instObj);
    }
    //instsAtTime.addAll(c);
    //checkMinMaxTimes(time);
  }
  
  /**
  get the set of instructions at a given time
  */
  public HashSet getAllAtTime(float time) {
    time = _roundTimes.roundTime(time);
    Float timeFl = new Float(time);
    if(!this.containsKey(timeFl))
      super.put(timeFl, new HashSet());
    HashSet instsAtTime = (HashSet)super.get(timeFl);
    return instsAtTime;
  }
  
  /**
  get the set of all InstructionObjects
  */
  public HashSet getInstSet() {
    return _allInstsSet;
  }
  
  public void resetSuccsList() {
    for (Iterator vIt = _allInstsSet.iterator(); vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      instObj.resetSuccsList();
    }
  }
  
  public void resetdfg() {
    _dfgI = null;
  }
  
  
  /**
  get a set of all instructions (instead of InstructionObjects like above) in 
  the List 
  */
  public HashSet getInstructions() {
    HashSet instSet = new HashSet();
    for (Iterator vIt = _allInstsSet.iterator(); vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      Instruction inst = instObj.inst;
      instSet.add(inst);
    }
    return instSet;
  }
  
  /**
  After the schedule has been determined, the instruction times must be saved
  onto each instruction and then the list of scheduled instructions must be
  saved onto the associated hyperblock node.  The old instructions are removed
  first, becuase during modulo scheduling new instructions have been created,
  which are not in the original list on the node.  Rather than find out which
  these are and only add those, I just use the node methed,
  removeAllInstructions to delete the old list, and addInstructions, to add the
  newly created list.
  */
  public void scheduleList(BlockNode node) {
    for (Iterator vIt = keySet().iterator(); vIt.hasNext();) {
      Float time = (Float) vIt.next();
      float timefl = time.floatValue();
      HashSet iList = getAllAtTime(timefl);
      for (Iterator vIt2 = iList.iterator(); vIt2.hasNext();) {
        InstructionObject instObj = (InstructionObject) vIt2.next();
        Instruction inst = instObj.inst;
        inst.setExecTime(timefl);
        inst.setExecClkCnt((int)timefl);
      }
    }
    node.removeAllInstructions();
    ArrayList schedList = new ArrayList(getInstructions());
    //sort(schedList);
    node.addInstructions(schedList);
    
  }
  
  /**
  Since MSchedInstObject extends InstructionObject, when such an object is
  created in InstructionList by one of its methods, its to know what kind of
  instance to create.  This method has been overwritten in MSchedHash, and so if
  called from InstructionList it will create an InstructionObject and if called
  from MSchedHash, a MSchedInstObject.
  */
  public InstructionObject newInstructionObject() {
    return new InstructionObject();
  }
  public InstructionObject newInstructionObject(Instruction i) {
    return new InstructionObject(i);
  }
  
  /**
  read in all instructions from a hyperblock node and put them in the
  appropriate schedule slot as saved already in each instruction
  */
  public void readInList(BlockNode bNode) {
    _node = bNode;
    addAllInsts(bNode.getInstructions());
  }
  
  /**
  Create a deep copy of THIS.  The copy is performed by partialListCopy, who
  copies THIS between two given times and so listCopy uses the saved min and max
  times from THIS to call partialListCopy
  */
  public InstructionList listCopy() {
    return partialListCopy(getMinTime(), getMaxTime());
  }
  
  /**
  make a copy of the list between start and finish 
  */
  public InstructionList partialListCopy(float start, float finish) {
  
    InstructionList copy = new InstructionList(_chipdef, _maxRunTime);
    
    for (Iterator vIt = this.keySet().iterator(); vIt.hasNext();) {
      Float time = (Float) vIt.next();
      float timefl = time.floatValue();
      
      //for all schedule times with the range from start to finish
      if((start <= timefl)&&(timefl <= finish)) {
        HashSet iList = getAllAtTime(timefl);
	for (Iterator vIt2 = iList.iterator(); vIt2.hasNext();) {
          InstructionObject instObj = (InstructionObject) vIt2.next();
	  //InstructionObject instObjCpy = new InstructionObject(instObj.copySaveOps());
	  
	  //copy the InstructionObject to the other list.  The other list has to
	  //do the job of actually creating the new InstructionObject, however
	  //because otherwise, the new object will still point to THIS's private
	  //variables instead of copy's
	  copy.copy(timefl, instObj);
	}
      }
    }
    
    return copy;
  
  }
  
  /**
  This does exactly the same as above, except that when copying
  InstructionObjects, the InstructionObject, function copyNewOps is used, to
  create copies of the instructions with new operands
  */
  public InstructionList partialListCopyNewBlocks(float start, float finish) {
  
    InstructionList copy = new InstructionList(_chipdef, _maxRunTime);
    _oldToNew = new HashMap();
    for (Iterator vIt = this.keySet().iterator(); vIt.hasNext();) {
      Float time = (Float) vIt.next();
      float timefl = time.floatValue();
      
      if((start <= timefl)&&(timefl <= finish)) {
        HashSet iList = getAllAtTime(timefl);
	for (Iterator vIt2 = iList.iterator(); vIt2.hasNext();) {
          InstructionObject instObj = (InstructionObject) vIt2.next();
	  //InstructionObject instObjCpy = instObj.copyNewOps();
	  copy.copyNewOps(timefl, instObj);
	}
      }
    }
    
    return copy;
  
  }
  
  /**
  create a new InstructionObject, and call copySaveOps to copy instObj to the
  newly created object
  */
  public void copy(float time, InstructionObject instObj) {
    InstructionObject instObjCpy = new InstructionObject();
    instObjCpy.copySaveOps(instObj);
    add(time, instObjCpy);
  }
  
  /**
  do the same as above except make the copy with new Operands 
  */
  public void copyNewOps(float time, InstructionObject instObj) {
    InstructionObject instObjCpy = new InstructionObject();
    instObjCpy.copyNewOps(instObj);
    add(time, instObjCpy);
  }
  
  /**
  if a string of instructions only result in the definition of a block variable, 
  they should be killed as they are useless since the block variable has no life 
  after this hyperblock.  This works, by first finding all instructions who
  define primals.  All the predecessors of these instructions must also be ok,
  so the inputs to these instructions are saved.  Then another search is
  performed for all instructions defining the inputs to the primal defining
  instructions.  This is repeated to find their predecessors until the ultimate
  predecessors of the primal defining circuits have been found.  Any instruction
  not in these circuits must define only block variables ultimately.
  */
  public void killBlockDefiningCircuits() {
    HashSet savedOuts = new HashSet();
    HashSet goodInsts = new HashSet();
    
    boolean change = false;
    do {
      change = false;
      //foreach instruction:
      for (Iterator vIt = _allInstsSet.iterator(); vIt.hasNext();) {
	InstructionObject instObj = (InstructionObject) vIt.next();
	Instruction inst = instObj.inst;
	Operand out = inst.getOperand(0);
	//if the output from this instruction is known to be ok because it is 
	//part of a circuit that defines a primal, or if the output is itself a 
	//primal, then ...
	if(((out.isPrimal())||(contains(savedOuts, out)))&&
	   (!goodInsts.contains(inst))) {
	  
	  //save the fact that this instruction is ok
	  goodInsts.add(inst);
	  
	  change = true;
	  
	  //save the inputs from this instruction, since this instruction's 
	  //predecessors must also be ok, if this guy is ok. 
	  Operand op;
	  int numDefs = inst.getNumberOfDefs();
	  int total = inst.getNumberOfOperands();
	  for(int i = numDefs; i < total; i++) {
            op = inst.getOperand(i);
            if(op != null)
              savedOuts.add(op);
	  }

          //save any predicate inputs
	  BooleanEquation predTmp = inst.getPredicate();
	  if(predTmp != null) {
            LinkedList BoolsTmp = predTmp.listBooleanOperands();
            for (Iterator itin = ((LinkedList)BoolsTmp.clone()).iterator(); 
          	 itin.hasNext(); ) {
              op = (Operand)itin.next();
              savedOuts.add(op);
            }
          }
	  
	}
      } 
    //continue until all primal defining circuits have been traced back to their
    //ultimate sources
    }while(change);
    
    //foreach instruction
    for (Iterator vIt = ((HashSet)_allInstsSet.clone()).iterator(); 
             vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      Instruction inst = instObj.inst;
      
      //if it's not one of the saved ones, kill it
      if(!goodInsts.contains(inst))
        remove(instObj);
    }
    
  }
  
  public boolean contains(HashSet savedOuts, Operand op) {
    for (Iterator vIt = savedOuts.iterator(); vIt.hasNext();) {
      Operand opTmp = (Operand) vIt.next();
      if(opTmp.toString().compareTo(op.toString())==0)
        return true;
    }
    return false;
  }
  
  /**
  This gets rid of aliasing in the form of block or boolean variables being
  loaded or stored into another block or boolean variable instead of a primal. 
  These load and store instructions can be deleted and the resultant operand can
  be replaced by the source operand whereever it is used.  But actually this
  replaces a special case created by modulo scheduling, where this aliased block
  is then used again in another load or store, which defines yet another block
  or boolean.  This looks for both loads and stores, kills them both and
  replaces all uses of their defined block or boolean operands with the source
  operand.  
  */
  public void killEmBlockToBlockCopies() {
    HashMap replacements = new HashMap();
    ArrayList times = new ArrayList(this.keySet());
    sort(times);
    //for all times
    for (Iterator vIt = times.iterator(); vIt.hasNext();) {
      Float time = (Float) vIt.next();
      float timefl = time.floatValue();
      HashSet list = getAllAtTime(timefl);
      //for each instruction at this time
      for (Iterator vIt2 = ((HashSet)list.clone()).iterator(); 
              vIt2.hasNext();) {
	InstructionObject instObj = (InstructionObject) vIt2.next();
	Instruction inst = instObj.inst;
	//if this instruction is a load or store
	if((Store.conforms(inst))||(Load.conforms(inst))){
	  Operand in = null;
	  Operand out1 = null;

	  if(Store.conforms(inst)) {
            in = Store.getValue(inst);
            out1 = Store.getDestination(inst);
	  }
	  if(Load.conforms(inst)) {
            in = Load.getSource(inst);
            out1 = Load.getResult(inst);
	  }
	  
	  
	  //if both this instruction's input and output are block or boolean 
	  //operands:
	  if(((in.isBlock())||(in.isBoolean()))&&
	     ((out1.isBlock())||(out1.isBoolean()))) {
	    boolean replaced = false;
	    //foreach instruction (place for speedup!!! now that getSuccs is  
	    //defined in here instead of MSchedHash, only the successors can be
	    //searched instead of all instructions)
	    for (Iterator it1 = ((HashSet)_allInstsSet.clone()).iterator(); 
        	      it1.hasNext();) {
	      InstructionObject cObj = (InstructionObject)it1.next();
	      Instruction cInst = cObj.inst;
	      //if the instruction is a child:
	      if((Store.conforms(cInst))||(Load.conforms(cInst))){
		Operand out2 = null;
		Operand in2 = null;
		if(Store.conforms(cInst)) {
	          in2 = Store.getValue(cInst);
        	  out2 = Store.getDestination(cInst);
		}
		if(Load.conforms(cInst)) {
	          in2 = Load.getSource(cInst);
        	  out2 = Load.getResult(cInst);
		}
		//if in2==out, which means this is a successor, and the out is a
		//block or boolean operand, delete the object and save that its
		//output needs to be replaced whereever it's used
		if((in2 == out1)&&((out2.isBlock())||(out2.isBoolean()))){
		  replacements.put(out2, in);
		  remove(cObj);
        	  replaced = true;
		}
	      }
	    }
	    //delete the first load or store, and save that its output needs to
	    //be replaced as well
	    if(replaced) {
	      replacements.put(out1, in);
	      remove(instObj);
	    }
	  }
	}

      }
    }
    
    //foreach instruction
    for (Iterator vIt = _allInstsSet.iterator(); 
         vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      //Instruction inst = instObj.inst;
      
      //for each key of the replacements hashMap, where:
      //key = alias, which should be removed
      //value = source operand, that should replace the alias
      for (Iterator vIte = replacements.keySet().iterator(); 
           vIte.hasNext();) {
        Operand op = (Operand) vIte.next();
        //get the source operand:
	Operand opTmp = (Operand) replacements.get(op);
	//replace the alias with the source:
	instObj.replaceOperand(op, opTmp);
      }
    }
  
  }

  /**
  I don't think this is used by modulo scheduling anymore, but it is still here
  in case I found later that it was useful.  It just replaces all operands in
  all instructions with new ones (but leaving wires connecting, i.e. replacing
  same operands with the same new operand)
  */
  public void newOpsForInsts() {
    _oldToNew = new HashMap();
    for (Iterator vIt = _allInstsSet.iterator(); 
         vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      instObj.newOps();
    }
  
  }
  
  /**
  if we have chains of loads and stores (that is where a value is stored into a
  primal, that primal is loaded (and perhaps used), and stored again, and
  perhaps loaded, all these instructions can be deleted, and all the extra
  operands can be replaced with the original source operand.  This was written
  specifically for modulo scheduling, but was placed in InstructionList, because
  it might be useful for other schedulers if they ever do use InstructionList. 
  Modulo scheduling creates chains of loads and stores to transfer data over the
  edges of the modulo scheduled block, but in the epilog and prolog, these
  chains cause confusion and are unnecessary extra space and slow down.
  */
  public void killLSChains() {
    HashSet allToDieSet = new HashSet();
    HashMap replacementMap = new HashMap();
    HashSet extraInsts = new HashSet();
    //foreach instruction
    for (Iterator vIt = ((HashSet)_allInstsSet.clone()).iterator(); 
         vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      //if it is a store to a modPrim primal operand
      if((Store.conforms(instObj.inst))&&
         (Store.getDestination(instObj.inst).getFullName().indexOf("modPrim")>=0)) {
      
        Operand in = Store.getValue(instObj.inst);
	if(allToDieSet.contains(in)) continue; //we don't need to consider
	                                       //instructions that killLSChainsRecursive
					       //has already determined should
                                               //die
	HashSet extraBlocks = new HashSet();
	//call killLSChainsRecursive to go down the chain, finding instructions
	//to die.  It returns whether the instruction was the end of the chain, 
	//which I want to keep alive, since the prolog needs this instruction to
	//transfer data to the loop kernal
	if(instObj.killLSChainsRecursive(extraBlocks, extraInsts, 
	                                 new HashSet()))
	  extraInsts.add(instObj.inst);
	//removeInsts(extraInsts);
	
	//save a set of operands to replace with the input of this instruction
	replacementMap.put(in, extraBlocks);
	allToDieSet.addAll(extraBlocks);
	//replaceOps(in, extraBlocks);
      
      }
    }
    //delete the instructions:
    removeInsts(extraInsts);
    //replace all the operands with the source:
    replaceOps(replacementMap, allToDieSet);
  
  }
  
  /**
  This was written to support killLSChains.  it replaces all the operands in a
  set with one operand, which killLSChains has determined is the source operand
  in a chain of loads and stores.
  */
  public void replaceOps(HashMap replacementMap, HashSet allToDieSet) {
    //for replacementMap
    //key = source operand
    //value = set of alias that should be replaced by key

    
    for (Iterator vIt = replacementMap.keySet().iterator(); 
         vIt.hasNext();) {
      Operand keeper = (Operand) vIt.next();
      //because of the way killLSChains goes through the list, it will sometimes
      //start in the middle of a chain of loads and stores and will not know
      //that the source it found when starting here is not the ultimate source.
      //That means that it will want to do extra replacements, where it will try
      //to replace operands with the operand used in the middle.  allToDieSet 
      //knows all the operands that will be replaced, however, and so by
      //checking if it contains an operand, we can know if this operand was from
      //the middle of the chain and not bother with the replacement.
      if(allToDieSet.contains(keeper)) continue;
      
      //foreach instruction, and foreach operand in the set of operands to 
      //replace, replace the operand with the source operand
      for (Iterator vIt2 = _allInstsSet.iterator(); 
           vIt2.hasNext();) {
	InstructionObject instObj = (InstructionObject) vIt2.next();
	for (Iterator vIt3 = ((HashSet)replacementMap.get(keeper)).iterator(); 
            vIt3.hasNext();) {
          Operand looser = (Operand) vIt3.next();
	  instObj.replaceOperand(looser, keeper);
	}
      }
    }
  }
  
  /**
  This method is no longer used.  It was written to do the same job as
  killLSChains, but didn't do it as well.  It also tried to kill the loads and
  stores to modPrim primal operands used in modulo scheduling.  I think it
  doesn't even work anymore at all, because from looking at it, it looks like I
  may have been in the middle of some change to it, when I had the idea to write
  killLSChains and didn't finish whatever that change was.  Anyway, if you feel
  inspired to play with it, here it is:
  */
  public void removeUnwantedPs() {
  
    HashMap replacements = new HashMap();
    HashMap killEm = new HashMap();
    HashMap killEmTimes = new HashMap();
    HashMap primToBlock = new HashMap();
    ArrayList times = new ArrayList(this.keySet());
    sort(times);
    for (Iterator vIt = times.iterator(); vIt.hasNext();) {
      Float execTimeFl = (Float) vIt.next();
      float execTime = execTimeFl.floatValue();
      HashSet iList = getAllAtTime(execTime);
      for (Iterator vIt2 = ((HashSet)iList.clone()).iterator(); 
             vIt2.hasNext();) {
	InstructionObject instObj = (InstructionObject) vIt2.next();
	Instruction inst = instObj.inst;
	if((Store.conforms(inst))||(Load.conforms(inst))) {
	
	  Operand in = null;
	  Operand out = null;

	  if(Store.conforms(inst)) {
            in = Store.getValue(inst);
            out = Store.getDestination(inst);
	  }
	  if(Load.conforms(inst)) {
            in = Load.getSource(inst);
            out = Load.getResult(inst);
	  }
	  if(((in.isBlock())||(in.isBoolean()))&&
	     (out.isPrimal())&&(out.getFullName().indexOf("modPrim")>=0)) {
	    killEm.put(out, instObj);
	    killEmTimes.put(out, new Float(execTime));
	    primToBlock.put(out, in);
	  }
	  else if(((out.isBlock())||(out.isBoolean()))&&
	           (in.isPrimal())&&(primToBlock.keySet().contains(in))) {
	    replacements.put(out, primToBlock.get(in));
	    remove(execTime, instObj);
	    remove(((Float)killEmTimes.get(in)).floatValue(), 
	           (InstructionObject)killEm.get(in));
	  }
	
	}
      }
    }
    /*HashSet allPs = new HashSet();
    HashSet defs = new HashSet();
    HashMap defTimes = new HashMap();
    HashSet uses = new HashSet();
    HashMap useTimes = new HashMap();
    
    for (Iterator vIt = this.keySet().iterator(); vIt.hasNext();) {
      Float execTimeFl = (Float) vIt.next();
      float execTime = execTimeFl.floatValue();
      HashSet iList = getAllAtTime(execTime);
      for (Iterator vIt2 = iList.iterator(); vIt2.hasNext();) {
	InstructionObject instObj = (InstructionObject) vIt2.next();
	Instruction inst = instObj.inst;
	Operand op;
	int numDefs = inst.getNumberOfDefs();
	for(int i = 0; i < numDefs; i++) {
          op = inst.getOperand(i);
          if(op != null) {
            if(op.isPrimal()) {
	      if((defTimes.containsKey(op))&&(execTime>=0))
		defTimes.put(op, new Float(Math.max(((Float)defTimes.get(op)).floatValue(),
	                	 execTime)));
	      else
		defTimes.put(op, new Float(execTime));
              defs.add(op);
              allPs.add(op);
            }
	  }
	}

	int total = inst.getNumberOfOperands();
	for(int i = numDefs; i < total; i++) {
          op = inst.getOperand(i);
          if(op != null)
            if(op.isPrimal()) {
	      if(useTimes.containsKey(op)) 
		useTimes.put(op, new Float(Math.min(((Float)useTimes.get(op)).floatValue(),
	                                     execTime)));
	      else
		useTimes.put(op, new Float(execTime));
              uses.add(op);
              allPs.add(op);
            }
	}

      }
    }
    
    HashMap replacements = new HashMap();
    for (Iterator vIt = allPs.iterator(); 
         vIt.hasNext();) {
      Operand op = (Operand) vIt.next();
      if((uses.contains(op)) && (defs.contains(op)) && 
       (((Float)useTimes.get(op)).floatValue() > ((Float)defTimes.get(op)).floatValue())) {
        BlockOperand epiTmp = Operand.nextBlock("pro_epiTmp");
	replacements.put(op, epiTmp);
      }
    }*/
    
    for (Iterator vIt = _allInstsSet.iterator(); 
         vIt.hasNext();) {
      InstructionObject instObj = (InstructionObject) vIt.next();
      //Instruction inst = instObj.inst;
      for (Iterator vIte = replacements.keySet().iterator(); 
         vIte.hasNext();) {
        Operand op = (Operand) vIte.next();
        Operand opTmp = (Operand) replacements.get(op);
	instObj.replaceOperand(op, opTmp);
      }
    }
  
  }
  
  /**
  * This is for debugging.  It prints the schedule to the screen
  */
  public void printSchedule() {
  
    ArrayList times = new ArrayList(this.keySet());
    sort(times);
    for (Iterator vIt = times.iterator(); vIt.hasNext();) {
      Float time = (Float) vIt.next();
      float timefl = time.floatValue();
      HashSet list = getAllAtTime(timefl);
      System.out.println("At time " + timefl + " there are these inst" +
                         "ructions: ");
      for (Iterator vIt2 = list.iterator(); vIt2.hasNext();) {
        InstructionObject instObj = (InstructionObject) vIt2.next();
        Instruction inst = instObj.inst;
	BooleanEquation pred = inst.getPredicate();
	System.out.println(inst + "   |   " + pred);
      }
      
    }
  
  }
  
  /**
  * ummmm, like duh.  Do I really need to explain this one?  This one "sorts"
  * a list.  If you don't know the word "sort", try a dictionairy--and while
  * you're at it, you can tell me if I spelled dictionairy correctly.
  */
  public void sort(ArrayList o_list) {
    class DoubleCompare implements Comparator {      
      /** compare function used by sort.  sorts hashmaps based on their size 
       *  and instructions in a list depending on the size of their execution 
       *  windows.  
       * 
       * @param o1 input 1
       * @param o2 input 2
       * @return -1, 0, or 1 depending on if o1 or o2 is greater or if they are 
       *     equal
       */
      public int compare(Object o1, Object o2) {	
  	if (o1 instanceof Float 	    
  	&& o2 instanceof Float) {	   
  	  Float p1 = (Float)o1; 	 
  	  Float p2 = (Float)o2; 	 
  	  if( p1.floatValue() < p2.floatValue()) {	      
  	    return -1;  	
  	    } else if( p1.floatValue() > p2.floatValue() ){	       
  	    return 1;	      
  	    } else {		
  	    return 0;	       
  	  }	   
  	} if (o1 instanceof Instruction             
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
  	  throw new ClassCastException("Not Float");	    
  	}      
      }    
    }	     
    Collections.sort(o_list, new DoubleCompare());  
  }
}  //end class InstructionList

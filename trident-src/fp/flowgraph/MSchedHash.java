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


/** This class contains most of the guts of the modulo scheduler.  The 
 *  comments in here are mostly on the parts that are designed by me to do
 *  edits or something.  To learn more about modulo scheduling itself,
 *  please read Rau's papers.  This guy works with ModSched.java and 
 *  extends InstructionList.  InstructionList extends HashMap and contains
 *  many methods useful for editing a scheduled list of instructions.  It
 *  also saves the schedule itself, in that the key is the schedule time
 *  and the values are HashSets of all the instructions at that time.  
 *  MSchedHash extends InstructionList and contains Modulo Scheduling 
 *  specific methods.  InstructionList contains a nested class called
 *  InstructionObject, which saves instructions, some stuff for them, and
 *  useful methods for dealing with individual instructions.  MSchedHash
 *  uses MSchedInstObjects, which extend InstructionObjects, and contain
 *  Modulo Scheduling specific instruction editing methods as well as some
 *  implementations for part of the modulo scheduling algorithm.
 * 
 * @author Kris Peterson
 */
public class MSchedHash extends InstructionList
{

  /**
  * This nested class saves instruction data, and modulo scheduling
  * methods for editing individual instructions
  */
  public class MSchedInstObject extends InstructionObject {
  
    
    //public Instruction inst;
    /**
    schedule time
    */
    public float startTime = -1;
    /**
    previous schedule time
    */
    public float oldStartTime = -1;      
    //private ArrayList _listOfSuccs = new ArrayList();
    //private Boolean _noSuccs = null;
    //private ArrayList _listOfPreds = new ArrayList();
    //private Boolean _noPreds = null;
    /**
    number of iterations separating two instructions (0 means they are in
    the same iteration and 1 means that one is executed one iteration later)
    */
    private HashMap _distances = new HashMap();
    /**
    the earliest legal start time for an instruction as determined based on
    the scheduled times for its immediate scheduled predecessors
    */
    private float _estart = -1;
    /**
    the execution time if the loop were unrolled
    */
    public float unrollSt = -1;
    /**
    the earliest possible number of iterations of the modulo scheduled block
    before the loop would reach this instruction (this corresponds to estart
    and not the actual schedule time)
    */
    private int _eIICnt = -1;
    /**
    the number iterations of the modulo scheduled block
    before the loop would reach this instruction
    */
    private int _iiCnt = -1;
    //public float pStTime = 999;
    //private float _unrollEstart = -1;
    //private HashSet _cntrlIns = new HashSet();
    //public HashSet badParents = new HashSet();
    /**
    when a legal schedule slot has not been found, choose one randomly, but
    not in places that have already been tried.  This HashSet saves those
    who have been tried
    */
    private HashSet _triedTimes = new HashSet();
    
    /**
    constructor--this one is never used
    */
    public MSchedInstObject() {
      super();
    }
    
    /**
    constructor--saves the instructions associated with this object.  
    */
    public MSchedInstObject(Instruction i) {
      //save instruction to an InstructionObject variable
      super(i);
    }
    
    /*public boolean getNoSuccs() {
      if(_noSuccs == null)
        getListOfSuccs();
      return _noSuccs.booleanValue();
    }
    
    public boolean getNoPreds() {
      if(_noPreds == null)
        getListOfPreds();
      return _noPreds.booleanValue();
    }*/
    
    
    
    /*public ArrayList getListOfPreds() {
      if(_listOfPreds.size()>0)
        return _listOfPreds;
      else if((_noPreds!=null)&&(_noPreds.booleanValue()))
        return null;
      
      DependenceFlowNode childNode = _dfg.findNode(inst);
      for (Iterator it1 = ((Set)(childNode.getInEdges())).iterator(); 
              it1.hasNext();) {
        DependenceFlowEdge pEdge = (DependenceFlowEdge)it1.next();
        DependenceFlowNode pNode = (DependenceFlowNode)pEdge.getSource();
        Instruction pInst = pNode.getInstruction();
	if(pInst == null)  continue;
	MSchedInstObject pObj = (MSchedInstObject)instToObjMap.get(pInst);
	//if(pEdge instanceof DependenceCntrlFlowEdge)
	//  _cntrlIns.addAll(pObj.getInstOuts());
	_listOfPreds.add(pObj); 
      }
      if(_listOfPreds.size()==0) _noPreds = new Boolean(true);
      else _noPreds = new Boolean(false);
      return _listOfPreds;
    }
  
    public ArrayList getListOfSuccs() {
      if(_listOfSuccs.size()>0)
        return _listOfSuccs;
      else if((_noSuccs!=null)&&(_noSuccs.booleanValue()))
        return null;
      
      DependenceFlowNode parentNode = _dfg.findNode(inst);
      for (Iterator it1 = parentNode.getOutEdges().iterator(); 
              it1.hasNext();) {
        DependenceFlowEdge cEdge = (DependenceFlowEdge)it1.next();
        DependenceFlowNode cNode = (DependenceFlowNode)cEdge.getSink();
        Instruction cInst = cNode.getInstruction();
	MSchedInstObject cObj = (MSchedInstObject)instToObjMap.get(cInst);
	if(cInst == null) continue;
	//if(!((cEdge.getisBackWardsPointing())&&
	//     (cEdge instanceof DependenceCntrlFlowEdge)))
	if(!cEdge.getisBackWardsPointing())
	  _distances.put(cInst, new Integer(0));
	else if((_distances.get(cInst) == null)||
	       (((Integer)_distances.get(cInst)).intValue() != 0)) 
	  _distances.put(cInst, new Integer(1));
	_listOfSuccs.add(cObj); 
      }
      if(_listOfSuccs.size()==0) _noSuccs = new Boolean(true);
      else _noSuccs = new Boolean(false);
      return _listOfSuccs;
    }*/
    
    /**
    this method determines the "distance" or number of iterations separating
    this instruction and its child instruction.  
    */
    public int getDistance(Instruction childInst) {
    
      //check if it has already been determined
      if(!_distances.containsKey(childInst)) {
	//if not, find both instructions in the dependence flow graph
	DependenceFlowNode parentNode = _dfg.findNode(inst);
	DependenceFlowNode childNode = _dfg.findNode(childInst);
	//for each out edge from the parent:
	for (Iterator it1 = parentNode.getOutEdges().iterator(); 
        	it1.hasNext();) {
          DependenceFlowEdge cEdge = (DependenceFlowEdge)it1.next();
          DependenceFlowNode cNode = (DependenceFlowNode)cEdge.getSink();
	  if(childNode != cNode) continue;
          //if the edge connects the two instructions
	  Instruction cInst = cNode.getInstruction();
	  if(cInst == null) continue;
	  //if at least one connecting edge is not recursive set the 
	  //distance to 0, and otherwise to 1 and save in the hashmap
	  //"_disntances"
	  if(!cEdge.getisBackWardsPointing())
	    _distances.put(cInst, new Integer(0));
	  else if((_distances.get(cInst) == null)||
		 (((Integer)_distances.get(cInst)).intValue() != 0)) 
	    _distances.put(cInst, new Integer(1));
	}
      }
      //return the distance
      return  ((Integer)_distances.get(childInst)).intValue();
    
    }
    
    /**
    see Rau's papers for more.  The effective delay is equal to the
    real delay (i.e. the latency of an instruction) - the distance between
    parent and child times ii.  
    */
    public float getEffectiveDelay(Instruction childInst, int ii) {
      float effDelay = getRunLength() - getDistance(childInst) * ii;
      return effDelay;
    }
    
    /**
    Please read Rau's paper for more on this.  This is one of the main
    methods from modulo scheduling.  Its purpose is to determine the
    earliest possible start time for an instruction based on the finish
    times of all its immidiate, scheduled predecessors.  This is legal
    because the immidiate predecessors should have been scheduled correctly
    based on their predecessors, because problems with successors will be
    handled later, and because if any problems occur later with other non
    scheduled predecessors, this instruction will be unscheduled and
    rescheduled at the time that predecessor is scheduled (that is, when I
    say "the time..." I don't mean the cycle that the other instruction was
    scheduled at, but rather the moment in Trident's runtime that it
    considers the other instruction it will also see this guy and unschedule
    him).  
    */
    public void estart(int ii) {

      float estartTmp = -9999;
      float unrollEstartTmp = -9999;
      //int unrollEII = -9999;
      //for each immidiate predecessor:
      if(getListOfPreds()==null) _eIICnt=0;
      else {
	for (Iterator it1 = getListOfPreds().iterator(); 
        	  it1.hasNext();) {
	  MSchedInstObject pObj = (MSchedInstObject)it1.next();

	  float execTime = 0;
	  float unrollExecTime = 0;
	  //float unrollII = 0;

	  //if the parent is scheduled, find the effective end time for that
	  //instruction, which is its start time plus its effective delay:
	  if(pObj.startTime >= 0)
	    execTime = Math.max(0, pObj.startTime +
	                           pObj.getEffectiveDelay(this.inst, ii));
	  //save the max from all parents:
	  estartTmp = Math.max(estartTmp, execTime);
	  //get the unroll start time, similarly, except use the parent's
	  //unrolled start time plus its full latency if the distance is 0
	  //and 0 if the distance is 1 (if they are in different iterations
	  //we don't care where they are with respect to each other in the 
	  //unrolled schedule since the parent should be near the end of the 
	  //unrolled loop body and the child near the beginning
	  if(pObj.unrollSt >= 0) {
	    if(pObj.getDistance(inst)==0)
	      unrollExecTime = Math.max(0, pObj.getRunLength() + pObj.unrollSt);
	    else {
	      unrollExecTime = 0;
	      //pStTime = Math.min(pStTime, pObj.startTime);
	      //badParents.add(pObj);
	    }
          }
	  //save the max unroll time:
	  unrollEstartTmp = Math.max(unrollEstartTmp, unrollExecTime);
	}

	//save the max parent stop time, to this instruction's _estart private
	//variable
	_estart = estartTmp;
	//set the earliest number of iterations of the modulo scheduled block
	//before this executes to the unrolled time divided by ii-1 
	//(remember, the modulo scheduled block contains different parts of
	//the unrolled loop body and for data to travel through from the start
	//to this instruction, it must pass through the number of iterations
	//of the modulo scheduled body that would execute in the time it 
	//would take for the data to get to this instruction in the unrolled 
	//loop):
	_eIICnt = (int)(unrollEstartTmp/(ii));
      }
    }

    /**
    Please also refer ro Rau's paper for more on this. Once an estart has 
    been found, a schedule time must be found.  This time depends on several
    factors.  The factor Rau mentions is hardware conflichts.  But in
    Trident there are a few other factors that must be considered when
    finding a legal schedule time.  One is that, pipelined instructions must
    always start on even clock edges, and non-piplined instructions cannot
    run over a clock boundary.  The second Trident-specific issue is that
    for data to pass between different iterations of the modulo scheduled
    block, it must be saved and read from a register, and time for these
    loads and stores must be inserted into the block.
    */
    public float findTimeSlot(int ii) {
      //this class is for correcting start times for instructions so that
      //pipelined instructions start on even clock edges and non-pipelined
      //instructions don't run over a boundary:
      CorrectStartTimes adjustTimes = new CorrectStartTimes(inst, _chipdef,
							    !GlobalOptions.packInstructions);
    
      //Loads and store instructions must be handled differently.  The
      //problem is that if a store to a register occurs in the same cycle
      //as a read from that same register, the synthesis part of Trident
      //will try to optimize things by connecting instructions using the
      //load to the same wire that was used in the store and will remove 
      //the load instruction. This causes timing problems because
      //instructions will use values, which would have been stored 
      //in the register and read one iteration later, too early--one 
      //iteration  the modulo scheduled block too early.  This means that, 
      //for example, if you had the c code:
      //for(int i=0; i<10 ; i++)
      //  x++;
      //that i could be incremented faster (or slower) than x, and the loop
      //might stop before (or after) x has been incremented 10 times.  
      //To prevent timing problems, I force all loads to time 0, and all
      //stores to time ii-1.  
      //(note, the loads and stores I am talking about here are ones
      //originally in the code and not those, which I insert myself)
      if((Load.conforms(inst))&&(Load.getSource(inst).isPrimal())) {
	if(_estart > _loadStore.getLoadTime())
	  _iiCnt = _eIICnt + 1;
	else
	  _iiCnt = _eIICnt;
        return (float)0.0;
      }
    
      //see the comments above
      if((Store.conforms(inst))&&(Store.getDestination(inst).isPrimal())) {
        
	//_iiCnt = _eIICnt + (int)((_estart)/(ii-1));
	_iiCnt = _eIICnt + (int)((_estart)/(ii));
	float corrctedEstart = adjustTimes.getCorrectedStartTime(_estart);
	if(corrctedEstart >= 1)
	  return corrctedEstart;
	else
	  return (float)1;
      }
    
    
      float currTime = _estart;
      float scheduleTime = -1;

      //first look for a time_int between min and max, where the instruction can 
      //be placed without causing resource conflicts--and if a instruction is 
      //placed later than one of its child instrutions needs it to be, the 
      //child will be unscheduled and later rescheduled

//make two while loops and add in stuff for load and store on loop edges
//and to make sure that pipelined instructions start on even cycles and 
//the others don't spill over cycle boundaries!!!!!
      
      while((scheduleTime == -1)&&(currTime <= ii)) {
	if((!_chipdef.analyzeHardwareUse(_bNode, inst, (int)currTime))||
	   (!_loadStore.testObsGenugZeitGibt(this, ii, currTime))||
	   (Math.abs(adjustTimes.getCorrectedStartTime(currTime)-currTime) 
	        > 0.001))//||
	   //((int)pStTime <= (int)currTime)) 
          currTime += _schedTimePrecision;
	else
          scheduleTime = currTime;
      }
      //keep track of the unroll time for calculating the ii cnt (number of
      //mod sched block itertions before execution of this instruction):
      float unrollOffset = currTime - _estart;
      currTime = 0;
      
      //if no time was found, wrap over and consider from 0 to estart (
      //this is legal, even though it is later than estart, because it
      //is one iteration of hte mod sched block later):
      while((scheduleTime == -1) && (currTime <= _estart)) {
	if((!_chipdef.analyzeHardwareUse(_bNode, inst, (int)currTime))||
	   (!_loadStore.testObsGenugZeitGibt(this, ii, currTime))||
	   (Math.abs(adjustTimes.getCorrectedStartTime(currTime)-currTime) 
	        > 0.001))//||
	   //((int)pStTime <= (int)currTime)) 
          currTime += _schedTimePrecision;
	else 
          scheduleTime = currTime;
      }
      //still keep track of the un roll offset:
      unrollOffset += currTime;
      
      
      //if no place was found, then just place it at the first spot (almost--
      //but I'll get back to that) and later the scheduler will unschedule
      //all other instructions in conflict.  This algorithm could possibly
      //get stuck in an infinite loop with instructions conintiuously 
      //pushing each other out of place, and so, the spot where the instruction
      //is to be placed is slowly moved forward in time to hopefully allow the
      //two instructions to miss each other.
      if(scheduleTime < 0) {
	//if estart is legal and later than the last starting time, 
	//schedule to estart:
	if(((oldStartTime < 0) || (_estart > oldStartTime))&&
	   (_loadStore.testObsGenugZeitGibt(this, ii, _estart))) { //&&
	   //((int)pStTime > (int)_estart)) {
          scheduleTime = adjustTimes.getCorrectedStartTime(_estart);
	}
	else {
	
	  //I search randomly for a time slot to place the instruction in
	  //this is different from Rau, who simply increments the last 
	  //schedule time.  This way, I hope to get greater variety between
	  //different attempts to schedule so that hopefully there is a
	  //greater probability that one will work
	  float roundingFactor = 1/_schedTimePrecision;
	  if(getRunLength() > 1)
            roundingFactor = 1;
	  float stoppingPoint = ii * roundingFactor -
	                        _triedTimes.size();
	
	  HashSet alreadyTried = new HashSet();
          boolean stop = false;
	  boolean reachedEnd = false;
	  do {
	    
            do {
	      //randomly choose a time from 0 to ii, and round off this 
	      //number to _schedTimePrecision-ths of a cycle
	      oldStartTime = (float)Math.random() * ii;
              oldStartTime = ((float)Math.round(oldStartTime * roundingFactor))/
	                                           roundingFactor;
	    //make sure this one hasn't been attempted already, either 
	    //this time (in alreadyTried) or ever (_triedTimes)
	    } while((alreadyTried.contains(new Float((float)oldStartTime)))||
	            (_triedTimes.contains(new Float((float)oldStartTime))));
	    
	    alreadyTried.add(new Float((float)oldStartTime));
	    //to prevent getting stuck in infinite loops, we need to notice
	    //when all legal slots have been tried and exit the loop
	    if(alreadyTried.size()>=stoppingPoint) {
	      stop = true;
	    }
	    oldStartTime = adjustTimes.getCorrectedStartTime(oldStartTime);
	  //make sure that this time allows enough space for loads and
	  //stores
	  }while((!_loadStore.testObsGenugZeitGibt(this, ii, oldStartTime))&&
	          (!stop));
	         //((int)pStTime <= (int)oldStartTime))&&(!stop));
	  if(stop)
	    oldStartTime = -1; //no valid time was found; do not schedule
	  else
	    _triedTimes.add(new Float((float)oldStartTime));
	  scheduleTime = oldStartTime;
	}
	oldStartTime = scheduleTime;
	
	//save unrolled schedule
	if(scheduleTime < _estart) {
	  unrollOffset = ii-_estart;
	  unrollOffset += scheduleTime;
	}
	else {
	  unrollOffset = scheduleTime;
	}
      }
      
      //set the number of iterations of the mod sched block (which is the 
      //estart ii cnt + the time in this iteration until starting (which is
      //the sum of the estart (which is the time from the beginning of this
      //iteration until exection) + the unroll offset, which is the time
      //after estart when scheduling is set to) divided by ii -1):
      //_iiCnt = _eIICnt + (int)((unrollOffset+_estart)/(ii-1));
      _iiCnt = _eIICnt + (int)((unrollOffset+_estart)/(ii));
      return scheduleTime;

    }
    
    /**
    Once again, it is useful to refer to Rau's paper.  Modulo scheduling is
    a type of list scheduling, where the order that instructions are
    considered for scheduling is based on a priority.  The priority used
    here is their "height", which is the distance from an instruction to the
    end of the schedule.  See the nested class min distance matrix defined
    below, for more.
    */
    public float getHeight() {
      return _minDistMat.getHeight(inst);
    }
  }//end nested class MSchedInstObject
  
  /**
  * This nested class is for inserting loads and stores into the schedule.  
  * If an unrolled loop is 12 cycles long, and modulo scheduling decides on 
  * an II of 4, the mod sched block of instructions will be 4 cycles long
  * and the instructions from cycle 0 in the unrolled loop will run in
  * parallel with those from cycle 4 and 8, and those from cycle 1 will run
  * in parallel with those from 5 and 9, and so on (this is not totally true
  * but is a nice simplification for understanding).  When the mod sched
  * block is iterated over, three iterations of the main loop run in
  * parallel executing different thirds.  But whereas the original loop
  * could communicate between cycles 3 and 4 and between 7 and 8 using
  * wires, since these cycles sit in different iterations of the mod sched
  * loop body, they need to have their values saved to and read from
  * registers.  During scheduling, I ensure that there is space for these
  * loads and stores, but after scheduling, the loads and stores must be 
  * inserted.  This class was created to do that.  It does it in two main
  * steps.  First, MSchedHash uses one of its own methods to search for 
  * places where a load and store is necessary and it calls the method 
  * "saveLS" from SaveStoresNLoads to save the location of a load and store
  * as well as the net that is being transferred, and the iteration of the
  * modulo scheduled block that it must be sent to. For example, in the 
  * schedule described above, if a value needs to be transfered from 3 to 4
  * it will save that there is a load and store necessary from iteration 0
  * to iteration 1, and if there is a value that needs to be transfered from
  * 3 to 7, it will save that a value needs to be saved from iteration 0 to
  * 1 and again from 1 to 2 (there needs to be 2 loads and stores or there
  * will be timing problems because it will try to use values from the next
  * iteration of the main, unrolled loop!).  The second major method in this
  * class is called "addLS".  After the times and nets have been saved for
  * all loads and stores, they need to be created, and scheduled into the 
  * block.
  */
  private class SaveStoresNLoads extends HashSet {
    
    //private HashMap _op2PrimMap = new HashMap();
    //private HashMap _op2LoadBlockVarMap = new HashMap();
    //private HashMap _op2StoreBlockVarMap = new HashMap();
  
    /**
    *  This nested class is used to save all the necessary information for 
    *  any load or store
    */
    private class LSObject {
    
      /**
      iteration where parent stops
      */
      public int startII=-1;
      /**
      iteration where child starts - iteration where parent stops
      */
      public int iiDiff = -1;
      /**
      time where parent stops exection (its schedule time plus effective
      delay)
      */
      public float parentStopTime = (float)-1;
      /**
      when creating the load and store instructions we need to know the
      necessary "Type" 
      */
      public Type type = null;
      /**
      the source wire that is being saved
      */
      public Operand op = null;
      /**
      the name of this wire at the end (it needs to have a different name to
      ensure it is really a different wire and that iteration 0 and 2 can't
      talk directly to each other)
      */
      public Operand newOp = null;
      
      /**
      constructor allowing all values to be set at once
      */
      public LSObject (int sII, int iiD, float pST, Type t, Operand o, 
                       Operand n) {
        startII=sII;
        iiDiff=iiD;
        parentStopTime=pST;
        type=t;
        op=o;
        newOp=n;
      }
    
    }//end class LSObject
    
    /**
    *  This nested class generates ModPrim variables which are primal
    *  variables (registers) for transfering data between blocks.  It 
    *  creates specific variables for different operands and for different
    *  iterations
    */
    private class ModPrimCreator extends HashMap {
      
      /**
      *  This nested class saves the modPrim variables for different times
      *  and the count associated with a specific operand to be transfered
      */
      private class ModPrimTable {
        public HashMap modprimsForTimes = null;
	public int primCreateCnt = -1;
	public ModPrimTable(HashMap mpTble, int primCnt) {
	  modprimsForTimes = mpTble;
	  primCreateCnt = primCnt;
	}
      }//end class ModPrimTable
      
      public ModPrimCreator() {
        super();
      }
      
      /**
      this method takes an Operand and the iteration where it was transfered
      from and creates a specific modPrim primal
      */
      public PrimalOperand getPrim(Operand op, int iiCnt) {
      
        HashMap modprimsForTimes = null;
        int primCreateCnt = -1;
        if(!containsKey(op)) { //if this operand does not have a modPrim yet
	  //increment the counter
	  _primCreateCnt++;
	  //save this counter for this operand
	  primCreateCnt = _primCreateCnt;
	  //make a table for different primals at different times
	  modprimsForTimes = new HashMap();
	  ModPrimTable modPrimTble = new ModPrimTable(modprimsForTimes, 
	                                              _primCreateCnt);
	  put(op, modPrimTble);
	}
	else {
	//if it does exist get it:
	  modprimsForTimes = ((ModPrimTable)get(op)).modprimsForTimes;
	  primCreateCnt = ((ModPrimTable)get(op)).primCreateCnt;
        }
	PrimalOperand modPrim = null;
	if(!modprimsForTimes.containsKey(new Integer(iiCnt))) {
	  //if a primal doesn't exist yet, create it
	  modPrim = Operand.newPrimal("modPrim" + primCreateCnt + "_" +
	                              iiCnt);
	  //save the new primal			      
	  modprimsForTimes.put(new Integer(iiCnt), modPrim);
	}
	else //otherwise get the saved one
	  modPrim = (PrimalOperand)modprimsForTimes.get(new Integer(iiCnt));
	//return the modprim
	return modPrim;
      }
      
    }//end class ModPrimCreator
    
    /**
    *  This nested class is similar to ModPrimCreator except that it creates
    *  new block variables to be used in the future iterations of the mod 
    *  sched block
    */
    private class NewBlockCreator extends HashMap {
      
      //becuase of the existence of the "nextBlock" method, saving a count
      //for each block is unnecessary
      
      public NewBlockCreator() {
        super();
      }
      
      public Operand getNewBlock(Operand op, int iiCnt) {
      
        HashMap newBlocksForTimes = null;
	if(!containsKey(op)) {
	  //if this operand is new, make a new HashMap to save times
	  newBlocksForTimes = new HashMap();
	  put(op, newBlocksForTimes);
	}
	else //otherwise get the saved hashmap
	  newBlocksForTimes = (HashMap)get(op);
	Operand newBlock = null;
	if((!newBlocksForTimes.containsKey(new Integer(iiCnt)))||
	   (newBlocksForTimes.get(new Integer(iiCnt)) == null)) {
	  //make a new block or boolean operand for this operand at this 
	  //iteration
	  if(op.isBlock())
            newBlock = Operand.nextBlock(op.getFullName() + "_" + iiCnt);
	  else if(op.isBoolean())
            newBlock = Operand.nextBoolean(op.getFullName() + "_" + iiCnt);
	  else if(op.isAddr()) {
            newBlock = Operand.nextAddr(op.getFullName() + "_" + iiCnt);
	    //HashMap ptr2IndicesMap = _chipdef.getptr2IndicesMap();
	    //ptr2IndicesMap.put(newBlock.toString(), ptr2IndicesMap.get(op.toString()));
	  }
	  newBlocksForTimes.put(new Integer(iiCnt), newBlock);
	}
	else
	  newBlock = (Operand)newBlocksForTimes.get(new Integer(iiCnt));
	return newBlock;
      }
      
    }//end NewBlockCreator
    
    /**
    instantiate the operand creating classes:
    */
    private ModPrimCreator _modPrimCreator = null;
    private NewBlockCreator _newBlockCreator = null;
    
    /**
    For a more thorough explanation of what this is for, refer to
    MuxTableGenerator.java.  However the predicates used in primal stores
    in the loop kernal are saved and used in the epilog.  The instructions
    to do this are not added to the schedule until it has been unrolled and
    is ready for creating the epilog, becuase they will not be used in the
    kernal or prolog.  But since the loads and stores are added to the
    original kernal, the operand names used in these instructions must be
    changed separately so that when they are added to the epilog they
    already have the correct names.  This hashmap is used to save those
    instructions.
    keys: primals that were stored to in the kernal
    values: HashSets of instructions needed to evaluate and load the
            predicates used when storing to these primals
    */
    private HashMap _savedPredSaveStoreInsts = new HashMap();
    /**
    this contains all the instructions from all the hashsets in the above
    hashmap
    */
    //private HashSet _allLogic = new HashSet();
    /**
    Because I don't want to add multiple loads and stores for each operand
    for each time it is used, I only want to save it once.  But I need to
    make sure enough loads and stores exist to carry that operand to its
    last use.  Therefore, as I save new loads and stores, I only save a new
    version of a load and store on the same operand, if it goes further than
    previous ones, and if one is saved, the old needs to be erased.  These
    hashes are used to help with this.
    key: operand being transfered 
    value: (deepestLSObject) the actual load-store object that is the
                             deepest--if another deeper one is found, 
			     this value will be used, to
                             delete the old one from SaveStoresNLoads 
			     (which extends HashSet)
           (deepestLSObjectDepth) the latest iteration where this operand is
	                          used.  I need to save this to compare with
				  other uses of this operand to know if I
				  need to save that use instead.
    */
    private HashMap _deepestLSObject = new HashMap();
    private HashMap _deepestLSObjectDepth = new HashMap();
    //private HashSet _alreadyAdded = new HashSet();
    
    public SaveStoresNLoads() {
      super();
      _modPrimCreator = new ModPrimCreator();
      _newBlockCreator = new NewBlockCreator();
    }
    
    /**
    As SaveStoresNLoads extends hashset, we can save load store objects in
    it.  This was written to handle our adds in a special way.  It checks to
    see what the deepest use of an operand is, and only adds a new LSObject
    if the previous LSObject for this operand was less deep or nonexistent. 
    If there is a LSObject for this operand already and it is less deep, the
    older one must be deleted.
    */
    public boolean add(LSObject newLS) {
      //depth of the new LSObject:
      int hisDepth = newLS.startII + newLS.iiDiff;
      //operand in question:
      Operand sourceOp = newLS.op;
      //current deepest use of this operand (if it is used at all):
      int deepestDepth = -999;
      if(_deepestLSObjectDepth.containsKey(sourceOp))
        deepestDepth = ((Integer)_deepestLSObjectDepth.get(sourceOp)).intValue();
      
      //if the new LSObject is deeper, remove the old one and add the new
      //one and save it and its depth to the two deepestLSObject hashmaps
      if(hisDepth > deepestDepth) {
        LSObject deepestLSObj = (LSObject)_deepestLSObject.get(sourceOp);
	super.remove(deepestLSObj);
        super.add(newLS);
	_deepestLSObjectDepth.put(sourceOp, new Integer(hisDepth));
	_deepestLSObject.put(sourceOp, newLS);
	return true;
      }
      return false;//return true only if an LSObject was added
    }
    
    /**
    this method is called before saving or adding the loads and stores, to
    save the kernal primal store predicate instructions
    */
    /*public void savePredLogicInsts(HashMap predLogc) {
      _savedPredSaveStoreInsts = predLogc; //copy the hashmap to the private
                                           //var
      //copy the instructions from all the hashsets into the alllogic
      //hashset
      for (Iterator it1 = predLogc.values().iterator(); it1.hasNext();) {
	HashSet logicSet = (HashSet)it1.next();
        _allLogic.addAll(logicSet);
      }
    }
    public void changePredLogicInsts(Operand oldOp, Operand newOp) {
      for (Iterator it1 = _allLogic.iterator(); it1.hasNext();) {
	Instruction predLogicInst = (Instruction)it1.next();
	if(Store.conforms(predLogicInst))
	  predLogicInst.replaceOperand(oldOp, newOp);
      }
    }*/
    
    /**
    This is one of the two main methods for this class, and its purpose is
    to save all necessary information for all loads and stores
    */
    public void saveLS(int iiCnt, int iiDiff, HashSet connVars, 
                       MSchedInstObject cObj, float pST) {
      //if the child instruction is one of the saved predicate instructions
      //do nothing, because we want to edit these instructions only in 
      //reference to the primal in which they are associated.  To understand
      //what this is all about, please refer to the comments on
      //"getKernalPreds" in MuxTableGenerator.java.  But anyway, it is
      //important to note here, that the predicate logic instructions are in
      //the schedule, and if they were edited like normally, it would seem
      //they are simply successors of the predicate calculation instruction
      //(which would be something like %tmp_1 = setlt %tmp_2 %const1.0)
      //and would add loads and stores and change variable names accordingly
      //when what we want is to store the actual predicate used when storing
      //a primal, which could be and probably is some version of %tmp_1 from
      //a much later iteration.  Therefore, when we see these instructions
      //alone, we need to ignore them, but when we see the primal that they 
      //are associated with, then they need to be edited to use the primal's
      //new version of %tmp_1
      //if(!_allLogic.contains(cObj.inst)) {
	for (Iterator it1 = connVars.iterator(); it1.hasNext();) {
	  Operand op = (Operand)it1.next();
	  if(op.isPrimal()) continue;  //if an operand is primal it does not
	                               //need to be loaded and stored
	  //make a new Block operand for this operand at this time
	  Operand newOp = getNewLoadBlock(op, iiCnt + iiDiff);
	  //create a new LSObject for it:
	  LSObject newLS = new LSObject(iiCnt, iiDiff, pST, cObj.inst.type(),
	                        	op, newOp);
	  //change the child instruction so that it uses the new operand
	  //from the later iteration:
	  cObj.replaceOperand(op, newOp);
	  cObj.changeItPred(op, newOp);
	  //changePredLogicInsts(op, newOp);
	  
	  //if the child instruction is a save to a primal, then the 
	  //predicate logic instructions associated with its store must be
	  //edited to use the new operand instead of the old one
	  /*if((Store.conforms(cObj.inst))&&
	     (Store.getDestination(cObj.inst).isPrimal())) {
	    //foreach inst in logic
	    for (Iterator it2 = ((HashSet)_savedPredSaveStoreInsts.get(Store.getDestination(cObj.inst))).iterator(); 
	              it2.hasNext();) {
	      Instruction instTmp = (Instruction)it2.next();
	      //replace op with newOp
	      instTmp.replaceOperand(op, newOp);
	    }
	  }*/
	  add(newLS);
	}
      //}
    }
    
    /**
    This is the second of the two main methods for this class.  Its purpose
    is to create and add the loads and stores.  It will add enough loads and
    stores to carry a non primal operand from the iteration where its value
    was defined up until the latest use.  If the latest use is 3 iterations
    later, then three loads and stores must be created.
    */
    public void addLS(int ii) {
      //foreach saved LSObject in this:
      for (Iterator it1 = this.iterator(); it1.hasNext();) {
	LSObject lsObj = (LSObject)it1.next();
	//get the operand, type, and the new operand (this is the new one at
	//the end and not any in the middle):
	Operand op = lsObj.op;
        //if(!_alreadyAdded.contains(op)) {
        Type type = lsObj.type;
	Operand newOp = lsObj.newOp;
	//make sure that if the operand is boolean, that the created loads
	//and stores are type bool:
	if(op.isBoolean())
	  type = Type.Bool;
	int iiCnt = lsObj.startII;
	int iiDiff = lsObj.iiDiff;
	//get a modPrim variable:
	PrimalOperand modPrim = getPrimal(op, iiCnt);
	float predStopTime = lsObj.parentStopTime;
	//create the first store:
	BooleanEquation trueBoolEq = new BooleanEquation(true);
	Instruction storeInst = Store.create(Operators.STORE, type,
        				     modPrim, op, trueBoolEq);
	_opSel.select(storeInst);
	MSchedInstObject storeInstObj = new MSchedInstObject(storeInst);
	//schedule the store to the end of the modulo scheduled block (ii-1)
	//It must be at the end, to ensure that the loads and stores occur 
	//in different cycles and that the synthesizer will not delete the
	//load and use the wire going to the store instead--which would make
	//the instruction using the loaded value happen one iteration too
	//early.
	float storeStartTime = ii-1-storeInstObj.getRunLength();
	CorrectStartTimes cPredStopTimeS = new CorrectStartTimes(storeInst,
        							 _chipdef,
	        						 !GlobalOptions.packInstructions);
	float cPredStopTime = cPredStopTimeS.getCorrectedStartTime(storeStartTime);
	storeInstObj._iiCnt= iiCnt + 
			    (int)((cPredStopTime-predStopTime)/ii);
	scheduleInst(storeInstObj, cPredStopTime, ii);
	
	//if the operand needs to be transfered over more than one
	//iteration, then add all the necessary loads and stores
	for(int n = iiCnt + 1; n < iiCnt + iiDiff; n++){
	
	  //create new block operand (because of the way the operands are
	  //created, if the operand is used between its definition and its
	  //latest use, this will make a specific block variable for those
	  //intermidiate uses):
	  Operand transferOp = getNewLoadBlock(op, n);
	  //create new load:
	  Instruction loadInstT = Load.create(Operators.LOAD, type,
        				     transferOp, modPrim, trueBoolEq);
	  _opSel.select(loadInstT);
	  MSchedInstObject loadInstObjT = new MSchedInstObject(loadInstT);
	  //schedule to time 0, iteration n:
	  loadInstObjT._iiCnt=n;
	  scheduleInst(loadInstObjT, 0, ii);
	  
	  //make new modprim:
	  modPrim = getPrimal(op, n);
	  //make new store:
	  storeInst = Store.create(Operators.STORE, type, modPrim, transferOp, 
	        		   trueBoolEq);
	  _opSel.select(storeInst);
	  storeInstObj = new MSchedInstObject(storeInst);
	  //cPredStopTimeS = new CorrectStartTimes(storeInst, _chipdef, 
	  //					   !GlobalOptions.packInstructions);
	  //cPredStopTime = cPredStopTimeS.getCorrectedStartTime(getLoadTime());
	  
	  //schedule to ii-1 and iteration n:
	  storeInstObj._iiCnt= n + 
			      (int)((cPredStopTime-predStopTime)/ii);
	  scheduleInst(storeInstObj, cPredStopTime, ii);
	  
	}
	
	//create final load:
	Instruction loadInst = Load.create(Operators.LOAD, type,
        				   newOp, modPrim, trueBoolEq);
	_opSel.select(loadInst);
	MSchedInstObject loadInstObj = new MSchedInstObject(loadInst);
	//schedule to time 0, and the iteration of the deepest child:
	loadInstObj._iiCnt= iiCnt + iiDiff;
	scheduleInst(loadInstObj, 0, ii);
	//_alreadyAdded.add(op);
	//}
	
      }
    }
    
    /*public void changeBlockNames(HashSet inVars, MSchedInstObject cObj){
      for (Iterator it1 = inVars.iterator(); it1.hasNext();) {
	Operand op = (Operand)it1.next();
	if((op.isBlock())||(op.isBoolean())) {
	  Operand newOp = getNewStoreBlock(op);
	  cObj.replaceOperand(op, newOp);
	}
      }
    }*/
    /**
    use the modprimcreator object to make a new primal
    */
    public PrimalOperand getPrimal(Operand op, int iiCnt) {
      
      return _modPrimCreator.getPrim(op, iiCnt);
      
      /*if(_op2PrimMap.containsKey(op))
        return (PrimalOperand)_op2PrimMap.get(op);
      PrimalOperand newOp = Operand.newPrimal("modPrim" + _primCreateCnt++);
      _op2PrimMap.put(op, newOp);
      return newOp;*/
    }
    
    /**
    use the modprimcreator object to make a new block (or Boolean) operand
    */
    public Operand getNewLoadBlock(Operand op, int iiCnt) {
      
      return _newBlockCreator.getNewBlock(op, iiCnt);
      
      /*if(_op2LoadBlockVarMap.containsKey(op))
        return (Operand)_op2LoadBlockVarMap.get(op);
      Operand newOp = null;
      if(op.isBlock())
        newOp = Operand.nextBlock(op.getFullName());
      else if(op.isBoolean())
        newOp = Operand.nextBoolean(op.getFullName());
      _op2LoadBlockVarMap.put(op, newOp);
      return newOp;*/
    }
    
    /*public Operand getNewStoreBlock(Operand op) {
      if(_op2StoreBlockVarMap.containsKey(op))
        return (Operand)_op2StoreBlockVarMap.get(op);
      Operand newOp = null;
      if(op.isBlock())
        newOp = Operand.nextBlock(op.getFullName());
      else if(op.isBoolean())
        newOp = Operand.nextBoolean(op.getFullName());
      _op2StoreBlockVarMap.put(op, newOp);
      return newOp;
    }*/
    
  } //end class SaveStoresNLoads

  /**
  *  This nested class was created to perform analysis of whether an
  *  instruction was scheduled so that there is space for loads and stores.
  */
  private class LoadStoreObject {
    
    
    /*private class SaveLoads extends HashMap {
    
      public SaveLoads() { super(); }
      
      public int get(Operand op) {
        if(!containsKey(op))
	  return 999;
	return ((Integer)super.get(op)).intValue();
      }
    
      public void put(Operand op, int iiCnt) {
        super.put(op, new Integer(iiCnt));
      }
      
      
      public void putLoad(Operand op, int iiCnt) {
        put(op, Math.min(iiCnt, get(op)));
      }
    
      public void putLoads(Operand op, int iiCnt) {
        HashSet needTimes = new HashSet();
	if(containsKey(op))
	  needTimes = (HashSet)super.get(op);
	else
	  super.put(op, needTimes);
	needTimes.add(new Integer(iiCnt));
      }
    
      public HashSet getLoads(Operand op) {
        return (HashSet)super.get(op);
      }
    
    }
    
    private class SaveStores extends HashMap {
    
      public SaveStores() { super(); }
      
      public int get(Operand op) {
        if(!containsKey(op))
	  return -999;
        return ((Integer)super.get(op)).intValue();
      }
    
      public void put(Operand op, int iiCnt) {
        super.put(op, new Integer(iiCnt));
      }
      
      
      public void putStore(Operand op, int iiCnt) {
        put(op, Math.max(iiCnt, get(op)));
      }
    
    }*/
    
    /**
    to no the time necessary for a load and store, I create one of each
    */
    private Instruction _loadInst;
    private Instruction _storeInst;
    /*private HashSet _alreadyAddedL = new HashSet();
    private HashSet _alreadyAddedS = new HashSet();
    private HashSet _alreadyAddedLUR = new HashSet();
    private HashSet _alreadyAddedSUR = new HashSet();
    private SaveLoads _loadsToAdd = new SaveLoads();
    private SaveLoads _loadsToAddUR = new SaveLoads();
    private SaveStores _storesToAdd = new SaveStores();
    private HashMap _types = new HashMap();
    private HashMap _stoptimes = new HashMap();*/
    
    public LoadStoreObject() {
    
      //create dummy load and store instructions:
      Operand vartmp = Operand.newBlock("tmp1");
      Operand vartmp2 = Operand.newBlock("tmp2");
      _storeInst = Store.create(Operators.STORE, null, vartmp, vartmp2);
      //need to be able to see the selected library...
      _opSel.select(_storeInst);
      _loadInst = Load.create(Operators.LOAD, null, vartmp, vartmp2);
      _opSel.select(_loadInst);
      
    
    }
    
    /*private HashMap saveLsAndSsMap = new HashMap();
    public void saveLoads(int iiCnt, HashSet inVars, MSchedInstObject cObj){
      for (Iterator it1 = inVars.iterator(); it1.hasNext();) {
	Operand op = (Operand)it1.next();
	_loadsToAdd.putLoad(op, iiCnt);
	_loadsToAddUR.putLoads(op, iiCnt);
	System.out.println("op " + op);
	if(saveLsAndSs)
	  saveLsAndSsMap.put(op, new Boolean(true));
	else
	  saveLsAndSsMap.put(op, new Boolean(false));
	Operand newOp = getNewLoadBlock(op);
	System.out.println("newOp " + newOp);
	cObj.replaceOperand(op, newOp);
      
	if(op.isBoolean()) {
	  for (Iterator it = _node.getOutEdges().iterator(); it.hasNext();) {
            BlockEdge outEdge = (BlockEdge)it.next();
	    BooleanEquation eq = (BooleanEquation)outEdge.getPredicate().clone();
	    BooleanEquation neweq = eq.replaceBool((Bool)op, 
	    					   (Bool)newOp);
	    outEdge.setPredicate(neweq);

	  }
	}
      
      }
    }
    
    public void saveStores(int iiCnt, HashSet inVars, Type type, 
                           float stopTime, MSchedInstObject pObj){
      for (Iterator it1 = inVars.iterator(); it1.hasNext();) {
	Operand op = (Operand)it1.next();
	_storesToAdd.putStore(op, iiCnt);
	_types.put(op, type);
	_stoptimes.put(op, new Float(stopTime));
	if(saveLsAndSs)
	  saveLsAndSsMap.put(op, new Boolean(true));
	else
	  saveLsAndSsMap.put(op, new Boolean(false));
	//Operand newOp = getNewStoreBlock(op);
	//pObj.replaceOperand(op, newOp);
	
      }
    }*/
    
    /*public void addLoads(int ii) {
      
      for (Iterator it1 = _loadsToAdd.keySet().iterator(); it1.hasNext();) {
	Operand op = (Operand)it1.next();
        if(!op.isPrimal()&&(!_alreadyAddedL.contains(op))) {
          Type type = (Type)_types.get(op);
	  if(op.isBoolean())
	    type = Type.Bool;
	  PrimalOperand input = getPrimal(op);
	  Operand output = getNewLoadBlock(op);
	  BooleanEquation trueBoolEq = new BooleanEquation(true);
	  Instruction loadInst = Load.create(Operators.LOAD, type,
          				     output, input, trueBoolEq);
	  _opSel.select(loadInst);
	  MSchedInstObject loadInstObj = new MSchedInstObject(loadInst);
	  //add((float)0, loadInstObj);
	  loadInstObj._iiCnt=_loadsToAdd.get(op);
	  scheduleInst(loadInstObj, 0, ii);
	  System.out.println("scheduling load " + loadInst);
	  if(((Boolean)saveLsAndSsMap.get(op)).booleanValue())
	    _kernalPredLsNSs.add(loadInst);
	  _alreadyAddedL.add(op);
	}
      }
    }
    
    public void addStores(int ii) {
      
      for (Iterator it1 = _storesToAdd.keySet().iterator(); it1.hasNext();) {
        Operand op = (Operand)it1.next();
        if(!op.isPrimal()&&(!_alreadyAddedS.contains(op))) {
          Type type = (Type)_types.get(op);
	  if(op.isBoolean())
	    type = Type.Bool;
          float predStopTime = ((Float)_stoptimes.get(op)).floatValue();
          PrimalOperand output = getPrimal(op);
	  //Operand input = getNewStoreBlock(op);
	  BooleanEquation trueBoolEq = new BooleanEquation(true);
	  Instruction storeInst = Store.create(Operators.STORE, type,
          				       output, op, trueBoolEq);
          				       //output, input, trueBoolEq);
	  _opSel.select(storeInst);
	  MSchedInstObject storeInstObj = new MSchedInstObject(storeInst);
	  CorrectStartTimes cPredStopTimeS = new CorrectStartTimes(_storeInst,
                                                                   _chipdef,
							           !GlobalOptions.packInstructions);
	  float cPredStopTime = cPredStopTimeS.getCorrectedStartTime(predStopTime);
	  //add((float)cPredStopTime, storeInstObj);
	  storeInstObj._iiCnt=_storesToAdd.get(op) + 
	                      (int)((cPredStopTime-predStopTime)/ii);
	  scheduleInst(storeInstObj, cPredStopTime, ii);
	  if(((Boolean)saveLsAndSsMap.get(op)).booleanValue())
	    _kernalPredLsNSs.add(storeInst);
	  _alreadyAddedS.add(op);
	}
      }
    }
    
    public void addUnrollLoads(int ii, float max, MSchedHash unrolledSched) {
      for (Iterator it1 = _loadsToAddUR.keySet().iterator(); it1.hasNext();) {
	Operand op = (Operand)it1.next();
        if(!op.isPrimal()&&(!_alreadyAddedLUR.contains(op))) {
	  HashSet times = _loadsToAddUR.getLoads(op);
	  for (Iterator it = times.iterator(); it.hasNext();) {
	    Integer time = (Integer)it.next();
	    float timefl = time.floatValue();
	    if(timefl == _loadsToAdd.get(op)) continue;
	    Type type = (Type)_types.get(op);
	    if(op.isBoolean())
	      type = Type.Bool;
	    PrimalOperand input = getPrimal(op);
	    Operand output = getNewLoadBlock(op);
	    BooleanEquation trueBoolEq = new BooleanEquation(true);
	    Instruction loadInst = Load.create(Operators.LOAD, type,
          				       output, input, trueBoolEq);
	    _opSel.select(loadInst);
	    MSchedInstObject loadInstObj = new MSchedInstObject(loadInst);
	    loadInstObj._iiCnt=0;
	    unrolledSched.scheduleInst(loadInstObj, timefl*ii, ii);
	    
	  }
	  _alreadyAddedLUR.add(op);
	}
      }
    }
    
    public void addUnrollStores(int ii, float max, MSchedHash unrolledSched) {
      
      for (Iterator it1 = _storesToAdd.keySet().iterator(); it1.hasNext();) {
        Operand op = (Operand)it1.next();
        if(!op.isPrimal()&&(!_alreadyAddedSUR.contains(op))) {
          for(float f= _storesToAdd.get(op)*ii+ii; f<max; f+=ii) {
            Type type = (Type)_types.get(op);
            float predStopTime = ((Float)_stoptimes.get(op)).floatValue();
            PrimalOperand output = getPrimal(op);
	    BooleanEquation trueBoolEq = new BooleanEquation(true);
	    Instruction storeInst = Store.create(Operators.STORE, type,
          					 output, op, trueBoolEq);
	    _opSel.select(storeInst);
	    MSchedInstObject storeInstObj = new MSchedInstObject(storeInst);
	    CorrectStartTimes cPredStopTimeS = new CorrectStartTimes(_storeInst,
                                                                     _chipdef,
							             !GlobalOptions.packInstructions);
	    float cPredStopTime = cPredStopTimeS.getCorrectedStartTime(predStopTime);
	    //add((float)cPredStopTime, storeInstObj);
	    storeInstObj._iiCnt=0;
	    //float start = (int)((cPredStopTime-predStopTime)/ii);
	    //if(start + f > max) continue;
	    unrolledSched.scheduleInst(storeInstObj, cPredStopTime + f, ii);

	  }
	  _alreadyAddedSUR.add(op);
	}
      }
    }*/
    /**
    these functions are used to find out how long a load or store using the 
    choosen library would take
    */
    public float getLoadTime() {
      return _loadInst.getRunLength();
    }
  
    public float getStoreTime() {
      return _storeInst.getRunLength();
    }
    
    /**
    check if the scheduled instruction was legaled legal spot
    */
    public boolean testObsGenugZeitGibt(MSchedInstObject instObj, int ii) {
    
      return testObsGenugZeitGibt(instObj, ii, instObj.startTime);
      
    }
  
    /**
    check if potential schedule time for an instruction is legal
    */
    public boolean testObsGenugZeitGibt(MSchedInstObject instObj, int ii,
                                        float startTime) {
    
      float storeStTime = startTime + instObj.getRunLength();
      CorrectStartTimes corrStoreStTime = 
	new CorrectStartTimes(_storeInst, _chipdef, !GlobalOptions.packInstructions);

      float corrStStTime = corrStoreStTime.getCorrectedStartTime(storeStTime);
      //if there is not enough time after the instruction is finished, for
      //a store to fit return false (though if the instruction has no
      //successors this test is unnecessary)
      if((corrStStTime + getStoreTime() > ii - 1)&&(!instObj.getNoSuccs())) {
	return false;
      }
      //if there is not enough time before the instruction for a load return
      //false (though if the instruction has no predecessors this test is 
      //unnecessary)
      if((getLoadTime() > startTime)&&(!instObj.getNoPreds()))
        return false;
	
      //otherwise, alles klar!  Kein problem!  Nun, nach Hause mit dir!
      //that is, everything's cool.  schedule it here, for all I care
      return true;
      
    }
  
    /**
    this method was written to test early on if a given ii will allow legal
    schedules.  It doesn't use a start time for the instruction.  It simply 
    sees if it possible to fit, a load before it (if it has no parents) and
    a store after it (if it has no children).  If the answer is no, we
    already know, this ii is too small, and stop trying to mod schedule the
    loop with this ii.

    FIX THIS?

    */
    public boolean testIfSchedPossible(MSchedInstObject instObj, int ii) {
    
      float startTime = getLoadTime();
      if(instObj.getNoPreds())
        startTime = 0;

      CorrectStartTimes corrInstStTime = 
	new CorrectStartTimes(instObj.inst, _chipdef, !GlobalOptions.packInstructions);

      float corrstartTime = corrInstStTime.getCorrectedStartTime(startTime);
      float storeStTime = corrstartTime + instObj.getRunLength();

      CorrectStartTimes corrStoreStTime 
	= new CorrectStartTimes(_storeInst, _chipdef, !GlobalOptions.packInstructions);

      float corrStStTime = corrStoreStTime.getCorrectedStartTime(storeStTime);
      float storeEndTime = corrStStTime + getStoreTime();

      if(instObj.getNoSuccs())
        storeEndTime = corrStStTime;
      if(storeEndTime > ii - 1)
        return false;
      
      return true;
      
    }
  
  } //end class LoadStoreObject
  
  /**
  *  This nested class is just a special version of HashSet, which is used
  *  by the main modulo scheduling method, "iterativeSchedule" to save all
  *  the instructions which need to be scheduled.  That method, decides when
  *  to stop its main loop, when this set becomes empty.
  */
  private class InstHashSet extends HashSet{
  
    /**
    We always want to schedule the instruction with the greatest "height"
    (see above for definition of height).  To reduce the amount of searching
    through the set for the highest instruction, this instruction is saved,
    and only needs to be changed when add, addAll, or remove is called--and
    in those cases usually, with minimum effort and not a full search of the
    entire set.
    */
    public MSchedInstObject tallestInst = null;
    
    public InstHashSet() { super(); }
    
    /**
    to initially create the hashset from a collection of MSchedInstObjects
    */
    public boolean addAll(Collection c) {
      if(c==null)
        return false;
      for (Iterator it1 = c.iterator(); it1.hasNext();) {
        MSchedInstObject instObj = (MSchedInstObject)it1.next();
	this.add(instObj);
      }
      return true;
    }
    
    /**
    add an object to the set, and if this object is "higher" than the saved
    "highest" then save it to the tallestInst private variable
    */
    public void add(MSchedInstObject instObj) {
      if((tallestInst == null) ||
         (instObj.getHeight() > tallestInst.getHeight()))
        tallestInst = instObj;
      super.add(instObj);
    }
  
    /**
    when combining two InstHashSets, the tallestInsts of the two need to be
    compared (assuming both are known) and the tallest of the two saved, and
    the rest of the instructions can be added without looking at their
    heights
    */
    public void addAll(InstHashSet c) {
      if(c.tallestInst == null) {
        this.addAll((Collection)c);
	return;
      }
      if((tallestInst == null) ||
         (c.tallestInst.getHeight() > tallestInst.getHeight()))
        tallestInst = c.tallestInst;
      super.addAll(c);
    }
    
    /**
    removes an instruction object from the set.  This is the one time where
    it might be necessary to search through the whole set for the the
    tallestInst--but only if the object that is removed happens to be the
    tallest object.
    */
    public void remove(MSchedInstObject instObjR) {
      super.remove(instObjR);
      if(tallestInst == instObjR) {
        tallestInst = null;
	for (Iterator it1 = this.iterator(); it1.hasNext();) {
          MSchedInstObject instObj = (MSchedInstObject)it1.next();
	  if((tallestInst == null) ||
             (instObj.getHeight() > tallestInst.getHeight()))
            tallestInst = instObj;
	}
      
      }
      
    }
  
  } //end class InstHashSet

  /**
  *  This nested class implements stuff specific to Rau's algorithm and more
  *  information can be obtained by reading his papers.  He generates a
  *  matrix where the x and y axis are all the instructions and the values
  *  in the matrix are the distances between these instructions.  The
  *  distances are the effective delays (see getDistance method above, but 
  *  this equals Delay-distance*II).  Rau inserts two pseudo nodes into the
  *  dependence flow graph, one called start and the other stop.  The
  *  min-distance matrix is used for two things.  First of all, as a
  *  priority assigning tool for use when scheduling instructions, their 
  *  height is calculated based on their min-distance to the stop pseudo
  *  node.  The second use for the min-distance matrix is to determine a
  *  minimum II (mII, which is greater than or equal to the max of the
  *  mResII--the minimum II due to resource constraints--and the mRecII--the
  *  minimum II due to dependencies) to start the modulo scheduling 
  *  algorithm on.  This is done by taking advantage of the fact that the
  *  min-dist matrix varies based on the ii, since the effective delay
  *  changes (see equation above) and that in the matrix, the distance 
  *  between an instruction and itself cannot be positive--which would mean
  *  that the second occurance of that instruction happens later than the
  *  first one in the loop body.  Therefore, to search for this mII, the
  *  min-dist can be continuously recalculated for greater and greater IIs
  *  until a legal min-dist matrix is found. This is the min II, but there 
  *  is no gaurrantee that the scheduler will find a legal schedule at this 
  *  II.  I know this is badly explained, but this is only a paraphrase of 
  *  what I remember from reading Rau's paper. For more help understanding,
  *  refer to him.
  *
  *  the matrix is saved in a hashMap of hashmaps:
  *  key: nodes from dependence flow graph, including start and stop pseudo
  *       nodes
  *  value: HashMap where key: nodes from dependence flow graph
  *                       values: distances
  */
  private class MinDistMatrix extends HashMap {
  
    /**
    This helps me to find a node associated with an instruction
    key: instruction
    value: node in the dependence flow graph
    */
    private HashMap _inst2NodeMap = new HashMap();
    
    
    public MinDistMatrix() {
      super();
    }
    
   /**
   this method was written so that the DependenceFlowGraph method "findNode"
   would not need to be called every time a node needs to be found, since
   that would slow the program down a lot.  Each time a node is found, it is
   saved in the private hashmap _inst2NodeMap, and everytime after that,
   that the node is needed, it is gotten from the hashmap instead of from
   searching through the graph.
   */
    private DependenceFlowNode findNode(Instruction i) {
      if(_inst2NodeMap.containsKey(i))
        return (DependenceFlowNode)_inst2NodeMap.get(i);
      DependenceFlowNode tmpNode = _dfg.findNode(i);
      _inst2NodeMap.put(i, tmpNode);
      return tmpNode;
    }
    
    /**
    calculate the "height" by looking for the distance between a node
    associated with instruction i and the stop pseudo node
    */
    public float getHeight(Instruction i) {
      DependenceFlowNode n = findNode(i);
      if(n == null)
        return (float)0;
      return get(n, _dfg.sTOP());
    }
  
    /**
    get the distance between two instructions
    */
    public float get(Instruction i0, Instruction i1) {
      DependenceFlowNode n0 = findNode(i0);
      DependenceFlowNode n1 = findNode(i1);
      return get(n0, n1);
    }
  
    /**
    get the distance between two nodes
    */
    public float get(DependenceFlowNode n0, DependenceFlowNode n1) {
      HashMap row = (HashMap)this.get(n0);
      return ((Float)row.get(n1)).floatValue();
    }
  
    /**
    save the distance between two instructions
    */
    public void put(Instruction i0, Instruction i1, float v) {
      DependenceFlowNode n0 = findNode(i0);
      DependenceFlowNode n1 = findNode(i1);
      put(n0, n1, v);
    }
    
    /**
    save the distance between two nodes
    */
    public void put(DependenceFlowNode n0, DependenceFlowNode n1, float v) {
      if(!this.containsKey(n0))
        this.put(n0, new HashMap());
      HashMap row = (HashMap)this.get(n0);
      row.put(n1, new Float(v));
    }
    
    /**
    see if a distance exists for the location referenced by instructions i0
    and i1
    */
    public boolean containsKey(Instruction i0, Instruction i1) {
      DependenceFlowNode n0 = findNode(i0);
      DependenceFlowNode n1 = findNode(i1);
      return containsKey(n0, n1);
    }
  
    /**
    see if a distance exists for the location referenced by nodes n0 and n1
    */
    public boolean containsKey(DependenceFlowNode n0, DependenceFlowNode n1) {
      if(!this.containsKey(n0))
        return false;
      HashMap row = (HashMap)this.get(n0);
      return row.containsKey(n1);
    }
  
    /**
    this was copied from Rau's paper.  For each two connecting nodes, save
    the effective delay between them and then save a distance of 0 between
    each node and the stop pseudo node.  Return true or false based on
    whether this matrix is legal
    */
    public boolean compMinDist(int ii) {
    
      HashSet nList = (HashSet)_dfg.getAllNodes();
      for (Iterator it1 = nList.iterator(); 
        	it1.hasNext();) {
	DependenceFlowNode cNode = (DependenceFlowNode)it1.next();
	Instruction cInst = cNode.getInstruction();
	if(cInst == null) continue;
	MSchedInstObject cObj = (MSchedInstObject)instToObjMap.get(cInst);
	if(!cObj.getNoPreds()) {
          for (Iterator it2 = cObj.getListOfPreds().iterator(); 
        	  it2.hasNext();) {
	    MSchedInstObject pObj = (MSchedInstObject)it2.next();
	    DependenceFlowNode pNode = findNode(pObj.inst);
	    float effDelay = pObj.getEffectiveDelay(cInst, ii);
	    put(cNode, pNode, effDelay);
          }
	}
      }
    
      for (Iterator it1 = nList.iterator(); 
        	it1.hasNext();) {
	DependenceFlowNode cNode = (DependenceFlowNode)it1.next();
	put(cNode, _dfg.sTOP(), (float)0);
      }
    
      return (createMinDistMat(ii));
    
    }
    
    /**
    this was also copied from Rau's paper.  For any two nodes who are
    connected through a third node, save the greatest distance (whether that
    be the direct connection or the sum of the two connections)--why a
    MIN-distance matrix wants the max, I'll never understand, but this is
    exactly what Rau does.  If the delay between any node and itself is
    positive, this matrix is illegal and the method will return true.
    */
    public boolean createMinDistMat(int ii) {
    
      HashSet nList = (HashSet)_dfg.getAllNodes();
      for (Iterator m = nList.iterator(); m.hasNext();) {
	DependenceFlowNode nodeM = (DependenceFlowNode)m.next();
	for (Iterator i = nList.iterator(); i.hasNext();) {
          DependenceFlowNode nodeI = (DependenceFlowNode)i.next();
	  //check that there is a connection from nodeI to nodeM
          if(containsKey(nodeI, nodeM)) {
            for (Iterator j = nList.iterator(); j.hasNext();) {
              DependenceFlowNode nodeJ = (DependenceFlowNode)j.next();
	      //check if there is a connection from nodeM to nodeJ
              if(containsKey(nodeM, nodeJ)) {
        	float delay = get(nodeI, nodeM) + get(nodeM, nodeJ);
        	if((!containsKey(nodeI, nodeJ))||(delay < get(nodeI, nodeJ))) {
		      //set distance from nodeI to nodeJ equal to 
		      //the distance from nodeI to nodeM +
		      //the distance from nodeM to nodeJ
		      //longer connections are ignored, but this is how 
		      //this algorithm is described in Rau's paper, and 
		      //implemented in Trimaran
                  put(nodeI, nodeJ, delay);
                  if((nodeI == nodeJ) && (delay > 0)) {
                    //if diagonal is positive, invalid matrix
		    return true;
                  }
        	}
              }
            }
          }
	}
      }
      return false; 
    
    }
  
  /** This method calculates the minimum Inititiation interval due to recursion 
   *  (interiteration data dependencies).  It does this by using the method 
   *  compMinDist, which creates a minimum distance matrix, which is a matrix 
   *  describing the distance between all nodes in the dependence graph.  Each 
   *  row and column corresponds to a node, and the intersection point between 
   *  them has the distance between these two nodes saved.  The diagonal from 
   *  the top left to the bottom right, describes distances between nodes and 
   *  themselves.  If any of these values are positive, we know we must increase 
   *  II, as it is impossible to have any distance between a node and itself.  
   *  CalculateRecMII calls compMinDist repeatedly doubling II, until it fails 
   *  to create a valid minimum distance matrix.  Then it searches between a 
   *  minimum and maximum value of II, slowly cutting the distance between the 
   *  two by half to try and get as close to the correct II as possible.  Please 
   *  read Rau's articles for a better and more full description of this 
   *  algorithm.
   * 
   * @param dfg dependence flow graph
   * @param chipInfo1 chip information
   * @param ii iniitiation interval to use as seed for starting search
   */
    public int calculateRecMII(int ii) { 
    //um, this was not copy pasted from trimaran
    // but it was inspired by them :) 
      int lowerlimit = 1;
      int upperlimit = ii;
      //binary doubling to find a failure
      while(compMinDist(upperlimit)) {
	lowerlimit = upperlimit;
	upperlimit *= 2;
	if(upperlimit == 0)
	{
          throw new ScheduleException("Failed to find an II which could create a"+
	                              " valid min dist matrix, before the ii got"+
				      " so big it rolled back over to 0");
	}
      }
      //bin search for RecMII
      float recmii = ((float)( upperlimit + lowerlimit ))/2;
      while(Math.abs(upperlimit - lowerlimit) >= 0.001) {
	if(compMinDist((int)recmii))
          lowerlimit = (int)recmii+1;
	else
          upperlimit = (int)recmii;
	recmii = ((float)( upperlimit + lowerlimit ))/2;
      }
      //_mRecII = (int)recmii;
      return (int)recmii;
    }
  } //end class MinDistMatrix
  
  //
  /** the size of a window, in which the scheduler attempts to find slots 
  *   for an instruction
  */
  private float _schedTimePrecision = (float)0.05;
  /** a copy of the dependence flow graph
  */
  private DependenceFlowGraph _dfg = null;
  //private HashMap instToObjMap = new HashMap();
  /** a pointer to the choosen operation selection method associated with
  *   the choosen hardware library
  */
  private OperationSelection _opSel;
  /** instantiation of the LoadStoreObject class
  */
  private LoadStoreObject _loadStore;
  /** instantiation of the MinDistMatrix class
  */
  private MinDistMatrix _minDistMat;
  /** a pointer to the hardware info class
  */
  private ChipDef _chipdef = null;
  /** this is a counter used by SaveStoresNLoads to help create unique
  modPrim variables for each operand.  It is static so that if there are
  multiple loops in the design, each loop will have unique modPrim variables
  and no collisions of data will occur
  */
  static private int _primCreateCnt = 0;
  /** instantiation of the SaveStoresNLoads class
  */
  private SaveStoresNLoads _saveStoresNLoads;
  /**
  Inside a loop, there can be multiple mini loops of different lengths.  If
  they are left at the different lengths, they may execute at different
  speeds and incorrect results may occur.  To correct this, the shorter mini
  loops are stretched out to be as tall as the tallest.  I will discuss this
  issue in more detail below, defining mini-loop and talking about where and
  why this occurs, but this private variable, saves the height of
  the tallest mini loop.
  */
  private int _maxII = -999;
  
  private BlockNode _bNode;
  
  public MSchedHash() {
    super();
    _loadStore = new LoadStoreObject();
    _minDistMat = new MinDistMatrix();
  }
  
  public InstructionObject newInstructionObject() {
    return new MSchedInstObject();
  }
  public InstructionObject newInstructionObject(Instruction i) {
    return new MSchedInstObject(i);
  }

  public MSchedHash(ChipDef chipdef, DependenceFlowGraph dfg, 
                    OperationSelection opSel, BlockNode bNode) {
    //InstructionList also needs the chipdef and dfg:
    super(chipdef, dfg);
    _chipdef = chipdef;
    _opSel = opSel;
    _dfg = dfg;
    _bNode = bNode;
    _loadStore = new LoadStoreObject();
    _minDistMat = new MinDistMatrix();
    _saveStoresNLoads = new SaveStoresNLoads();
  }
    
  /**
  after the modulo scheduled block has been created, the epilog and prolog
  are created.  Using the example discussed above, where a loop is 12 cycles
  long and the modulo scheduled block is 4 cycles long, there are three
  iterations of the loop running at once.  One is just starting at cycle 0. 
  One is starting its second third at cycle 4.  And the last is just
  starting cycle 8.  When the modulo scheduled block first starts, it needs
  initial data for the second and third iteration who are already at cycle 4
  and 8.  This data is calculated in the prolog.  In addition, when the
  modulo scheduled block exits one iteration is just finishing, one
  iteration has only just finished cycle 7 and the last has just finished
  cycle 3.  Those two iterations must be completed.  That occurs in the
  epilog. I will discuss this in more detail in ModSched.java, where these
  blocks are created, but the first step is to create an unrolled version
  of the loop. This is actually very easy, because while scheduling, I
  calculated the unroll schedule times for each instruction.  
  */
  public InstructionList getUnrolledSched(ModSched.NumberPasser nums, int ii) {
    float schdlngh = -999;
    float excTime = 0;
    float unrltimeOfFstInst = 999;
    float timeOfFstInst = 0;
    //create a new schedule to hold the unrolled schedule:
    MSchedHash unrolledSched = new MSchedHash(_chipdef, null, _opSel, _bNode);
    //for each instruction
    for (Iterator it1 = ((HashSet)getInstSet().clone()).iterator(); it1.hasNext();) {
      MSchedInstObject instObj = (MSchedInstObject)it1.next();
      //copy it to the unrolled schedule and schedule it to the unrolled
      //schedule time saved on the instruction
      unrolledSched.copy(instObj.unrollSt, instObj);
      //this data is used when creating the prolog and epilog to decide
      //which and how many instructions to copy.  I'll talk about that more
      //in ModSched.java
      if(instObj.unrollSt > schdlngh) {
        schdlngh = instObj.unrollSt;
	excTime = instObj.startTime;
      }
      if(instObj.unrollSt < unrltimeOfFstInst) {
        unrltimeOfFstInst = instObj.unrollSt;
	timeOfFstInst = instObj.startTime;
      }
    }
    nums.schedLength = schdlngh;
    nums.lastInstExecTime = excTime;
    nums.fstStartTime = timeOfFstInst;
    //addUnrollLsNSs(ii, schdlngh, unrolledSched);
    return unrolledSched;
  }
   
  /*public void addUnrollLsNSs(int ii, ModSched.NumberPasser nums, 
                             MSchedHash unrolledSched) {
    _loadStore.addUnrollLoads(ii, nums.schedLength, unrolledSched);
    //_loadStore.addUnrollStores(ii, nums.schedLength, unrolledSched);
  }*/
  
  //private HashSet _kernalPredInsts;
  //private HashSet _kernalPredLsNSs = new HashSet();
  //private boolean saveLsAndSs = false;
  
  /*public void saveKernalPredInsts(Collection c) {
    _kernalPredInsts = new HashSet(c);
  }*/
  
  /*public HashSet getKernalPredLsAndSs() {
    return _kernalPredLsNSs;
  }*/
  
  /**
  these next two methods are for finding the latencies for a load or store
  instruction
  */
  public float getLoadTime() {
    return _loadStore.getLoadTime();
  }
  
  public float getStoreTime() {
    return _loadStore.getStoreTime();
  }
  
  
  /**
  the load and store class discussed above was written to create the loads
  and stores, but this method is what actually decides where to place them
  and when it is necessary.  They are necessary, whenever a parent is
  scheduled at an earlier iteration than the child (or if by some error they
  are in the same iteration, but the parent is scheduled to start at a later
  time).
  */
  public void addLoadsAndStores(int ii) {
    //foreach instruction 
    for (Iterator it1 = ((HashSet)getInstSet().clone()).iterator(); 
             it1.hasNext();) {
      MSchedInstObject cObj = (MSchedInstObject)it1.next();
      //foreach predecessor of that instruction
      if(cObj.getListOfPreds() != null)
	for (Iterator it2 = cObj.getListOfPreds().iterator(); 
      		it2.hasNext();) {
          MSchedInstObject pObj = (MSchedInstObject)it2.next();
	  //if the parent is a store to a primal, no more loads and stores are
	  //necessary as it will already carry the data over the loop edge
	  if((Store.conforms(pObj.inst))&&
	     (Store.getDestination(pObj.inst).isPrimal()))
	    continue;
	  //find out when the parent is finished executing
	  float pStopTime = pObj.startTime + pObj.getRunLength();
	  if(pStopTime > ii)
	    pStopTime -= ii;

	  //if the parent is scheduled at an earlier iteration of the modulo
	  //scheduled loop block than its child, then loads and stores will be
	  //necessary to carry its data to the child
	  if((pObj._iiCnt < cObj._iiCnt)||
	     ((pObj._iiCnt == cObj._iiCnt)&&
	      (pObj.startTime > cObj.startTime)&&
	      (pObj.getDistance(cObj.inst)==0))) {
	    //int iiCnt = (int)((pObj.unrollSt + pObj.getRunLength())/ii);
	    //Type type = pObj.inst.type();

	    //save the load and store, the iteration, the iteration
	    //difference between parent and child, the connecting operands,
	    //the child object, and the time when the parent is finished 
	    //executing

	    if((pObj._iiCnt == cObj._iiCnt)&&
	       (pObj.startTime > cObj.startTime)&&
	       (pObj.getDistance(cObj.inst)==0)) {
	      cObj._iiCnt++;
	      cObj.unrollSt += ii;
	    }

	    _saveStoresNLoads.saveLS(pObj._iiCnt, cObj._iiCnt - pObj._iiCnt, 
	                             cObj.getPredConnsAll(pObj.inst), cObj, 
				     pStopTime);


	    //_loadStore.saveLoads(cObj._iiCnt, cObj.getPredConnsAll(pObj.inst), 
	    //                     cObj);
	    //_loadStore.saveStores(iiCnt, pObj.getSuccConnsAll(cObj.inst),
	    //                      type, pStopTime, pObj);
	  }
	  //else
	  //  _loadStore.changeBlockNames(cObj.getPredConns(pObj.inst), cObj);

	}
    }
    //_loadStore.addLoads(ii);
    //_loadStore.addStores(ii);
    
    //tell the save stores and loads class to create all loads and stores
    _saveStoresNLoads.addLS(ii);
  }
  
  /**
  This method is called just prior to addLoadsAndStores.  It saves the
  information about primal predicates from the kernal (see notes above in
  save stores and loads class definition and in MuxTableGenerator.java)
  */
  /*public void savePredLogicInsts(HashMap predLogc) {
    _saveStoresNLoads.savePredLogicInsts(predLogc);
  }*/
  
  /**
  this method creates the min-dist matrix.
  */
  public boolean calcMinDistMat(int ii) {
    return _minDistMat.compMinDist(ii);
  }
  
  /** This method determines MII.  It first calls calculateRecMII, which 
   *  performs a binary search using MResII as a starting point, to find MRecII.  
   *  Then finally, calc_MII, sets MII to the max of MResII and MRecII (and 
   *  MRecII can end up less than MResII).  If the seed given to calculateRecMII
   *  is 1, the binary search will not work, and therefore if MResII is 1, a 
   *  seed of four is used.  It is difficult to say what the ideal starting 
   *  point is.  The higher the starting point, the less the first loop in 
   *  calculateRecMII does and the more the second must do to find MRecII, 
   *  whereas too small a seed, and the more the first loop in calculateRecMII 
   *  must do.  Either way, the search will be performed less optimally, but I 
   *  do not know what the seed should best be.  To understand more about the 
   *  binary search and what the first and second loops do, please refer to the 
   *  comments in calculateRecMII.
   * 
   * @param node_BlockNode Hyperblock node currently under consideration
   * @param dfg dependence flow graph
   * @param chipInfo1 information about the target chip
   * @return the minimum initiation interval
   */
  public int calc_MII(int mResII) {
    //_mResII = node_BlockNode.getII();
    int mRecII = -1;
    if(mResII <= 1)
      mRecII = _minDistMat.calculateRecMII(2);
    else
      mRecII = _minDistMat.calculateRecMII(mResII);
    int mII = Math.max(mResII, mRecII);
    return mII;
  }

  /**
  this is a fun little method.  It is possible that timing problems can be
  created in the modulo scheduled loop block.  Let's say we have the
  following c code:
  
  for(int i=0; i<10; i ++) {
    a=b+c;
    x*=a;
  }
  
  The resultant set of instructions in the loop will contain two miniloops. 
  One of those loops will be i slowly increasing until it reaches 10.  The
  other will be x increasing by being multiplied by a.  It could be, and
  very often is the case that one of these mini loops will be shorter than
  the other.  It could be that in cycle 0, x and i are initialized to the
  values from the previous iteration of the loop and that their data flows
  through the circuit and for our example, perhaps the final value of i is
  saved in cycle 7, whereas the final value of x is saved in cycle 11.  If
  the loop has an ii of 4, that means that 0, 4, and 8 will run in parallel,
  as will 1, 5, and 9 and so will 2, 6, and 10 and also 3, 7, and 11.  Now
  when the modulo scheduled block first starts and the data starts in cycle
  0 and propogates through to 3, is transfered over as the block restarts to
  cycle 4 and so on, at the end of the second iteration of the modulo
  scheduled block, i will be saved, but x not yet.  (and after the 1st
  run of the modulo scheduled block was finished a second iteration of the 
  main loop
  will start while the 1st is just starting cycle 4).  When the modulo
  scheduled block starts again, iteration 3 of the main loop will be
  starting, while iteration 2 will be starting its cycle 4, and iteration 1
  will be starting its cycle 8.  The problem is that because i has already
  been saved, iteration 3 will use this recently calculated version of i,
  and when x is saved at the end of cycle 11 and the block begins again, it
  will be used by iteration 4!  This means that i will be incrementing
  faster than x and the loop will exit before x has been multiplied by "a" 
  10 times!  To correct this, I need to stretch out i's mini loop so that 
  it finishes in cycle 11 with x's miniloop.  To do this, I force i (and all
  ends of mini loops) to the last section of the modulo scheduled block. 
  This would mean that a new load and store would be added to transfer the
  final calculated value for i from cycle 7 to 8 and that i would be saved
  in cycle 11 just like x.
  .
  */
  public void stretchOutLoops(int ii) {
    //foreach instruction
    for (Iterator it1 = ((HashSet)getInstSet().clone()).iterator(); it1.hasNext();) {
      MSchedInstObject pObj = (MSchedInstObject)it1.next();
      //if this instruction is a store and has successors
      if((Store.conforms(pObj.inst))&&(!pObj.getNoSuccs())) {
	//foreach of his successors:
	for (Iterator it2 = pObj.getListOfSuccs().iterator(); 
    		it2.hasNext();) {
    	  MSchedInstObject cObj = (MSchedInstObject)it2.next();
	  //if the distance between parent and child is 1 (meaning one is at
	  //the end of a miniloop and the other at the beginning) and if the
	  //parent is a store and the child is a load (ruling out recursive
	  //control edges) then move the parent to the last iteration of the
	  //modulo scheduled block (or section, however you want to think of
	  //it, but the time when cycle 11 would happen) and move the child 
	  //to the beginning
	  if((Load.conforms(cObj.inst))&&
	     (pObj.getDistance(cObj.inst) == 1)&&
	     (areAllPredsFromLastIteration(cObj))) {
	    pObj._iiCnt = _maxII;
	    scheduleInst(pObj, pObj.startTime, ii);
	    cObj._iiCnt = 0;
	    scheduleInst(cObj, cObj.startTime, ii);
	  }
	}
      }
    }  
  }

  /**
  this guy is not really necessary.  I just wanted to rule out non recursive
  connections, but checking the distance and checking that we have a load
  and store between parent and child is actually enough
  */
  public boolean areAllPredsFromLastIteration(MSchedInstObject cObj) {
    boolean predsAreFromLastIt = true;
    for (Iterator it2 = cObj.getListOfPreds().iterator(); 
    	    it2.hasNext();) {
      MSchedInstObject pObj = (MSchedInstObject)it2.next();
      if(pObj.getDistance(cObj.inst) == 0)
        predsAreFromLastIt = false;
    }  
    return predsAreFromLastIt;
  }
  
  /**
  given a collection of instruction objects, unschedule them all.  This is
  useful, because there maybe lots of hardware or dependency conflicting
  instructions that need to be rescheduled
  */
  public void unscheduleInstSet(Collection c) {
    
    for (Iterator it1 = c.iterator(); 
              it1.hasNext();) {
      MSchedInstObject instObj = (MSchedInstObject)it1.next();
      unscheduleInst(instObj);
    }
    
  }
  
  /**
  unschedule and instruction.  Reset all values to -1 (the flag for
  unscheduled, move it from it's location in the InstructionList hashmap to
  the time -1, and unsave any hardware usage due to this instruction
  */
  public void unscheduleInst(MSchedInstObject instObj) {
    float execTime = instObj.startTime;
    //HashSet list = this.getAllAtTime(execTime);
    //remove from the old time in the hashmap:
    this.remove(execTime, (InstructionObject)instObj);
    //put in -1
    this.add((float)-1, (InstructionObject)instObj);
    
    int iiCntTmp = instObj._iiCnt;
    //tell chipdef that he's no longer using these hardware resources at
    //this time:
    _chipdef.removeFromTime(_bNode, instObj.inst, (int)instObj.startTime);
    instObj.startTime = -1;
    instObj.unrollSt = -1;
    instObj._iiCnt = -1;
    instObj._eIICnt = -1;
    
    //if this happened to be the end of the longest miniloop, find the new
    //longest miniloop and its end:
    if(iiCntTmp == _maxII) {
      _maxII = -999;
      for (Iterator it1 = ((HashSet)getInstSet().clone()).iterator(); it1.hasNext();) {
	MSchedInstObject instObjTmp = (MSchedInstObject)it1.next();
	_maxII = Math.max(_maxII, instObjTmp._iiCnt);
      }  
    }
  }
  
  /**
  does the exact opposite of unscheduleInst
  */
  private void scheduleInst(MSchedInstObject instObj, float execTime, int ii) {
    //HashSet list = (HashSet)this.get(new Float((float)execTime));
    //list.add(instObj);
    //remove from its previous location (probably -1):
    this.remove(instObj.startTime, (InstructionObject)instObj);
    //put in new location
    this.add(execTime, (InstructionObject)instObj);
    
    //set execution and unroll start times
    instObj.startTime = execTime;
    instObj.unrollSt = instObj._iiCnt*ii + execTime;
    //save the time of the end of the longest miniloop
    _maxII = Math.max(_maxII, instObj._iiCnt);
    //save hardware usage at this time due to this instruction:
    _chipdef.saveNewHardwareUsage(_bNode, instObj.inst, (int)execTime);
  }
  
  /**
  This is another method copied and adapted from Rau.  Once a time has been
  choosen for an instruction, it needs to be scheduled there, and any
  conflicting instructions (either due to hardware or dependencies) must be
  unscheduled.
  */
  public InstHashSet scheduleAtTime(MSchedInstObject instObj, 
                                    float execTime, int ii) {
  
    InstHashSet unscheduledInstSet = new InstHashSet();
    
    //see who needs to be unscheduled due to hardware conflicts:
    unscheduledInstSet.addAll(_chipdef.killConflictingInstrucs(_bNode, 
                                                               instObj.inst, 
                                                               (int)execTime,
                                                               this));
    
    //see which SUCCESSORS to be unscheduled due to dependency problems:
    //if(minStartTime > ii ) minStartTime -= ii;
    if(!instObj.getNoSuccs()) {
      for (Iterator it1 = instObj.getListOfSuccs().iterator(); 
    		it1.hasNext();) {
	MSchedInstObject cObj = (MSchedInstObject)it1.next();
	if(cObj.startTime == -1) continue;
	float minStartTimeUR = 0;
	
	//check to see that the child is scheduled to start after the
	//parent's effective delay and that the unroll start for the child
	//is after the parent's unroll start + latency
	
	if(instObj.getDistance(cObj.inst) == 0)
	  minStartTimeUR = instObj._iiCnt*ii + execTime + instObj.getRunLength();
	float minStartTime = execTime + 
	                     instObj.getEffectiveDelay(cObj.inst, ii);
	if((cObj.unrollSt < minStartTimeUR)||
	   (cObj.startTime < minStartTime)||
	   ((instObj._iiCnt == cObj._iiCnt)&&
	    (instObj.startTime + instObj.getEffectiveDelay(cObj.inst, ii)
	                                   > cObj.startTime)&&
	    (instObj.getDistance(cObj.inst)==0))) { //||
	//   ((instObj._iiCnt >= cObj._iiCnt)&&
	//    (execTime <= cObj.startTime))) {
	//if((instObj._iiCnt >= cObj._iiCnt)&&
	//   (execTime <= cObj.startTime)) {
	//if(cObj.startTime < minStartTime) {
	  unscheduleInst(cObj);
	  unscheduledInstSet.add(cObj);
	}

      }
    }
    /*if(!instObj.getNoPreds()) {
      for (Iterator it1 = instObj.getListOfPreds().iterator(); 
    		it1.hasNext();) {
	MSchedInstObject pObj = (MSchedInstObject)it1.next();
	if(pObj.startTime == -1) continue;
	/*float minStartTime = pObj.startTime + 
	                     pObj.getEffectiveDelay(instObj.inst, ii);
	System.out.println("unsched p " + pObj.inst);
	System.out.println("unsched c " + instObj.inst);
	System.out.println("pObj.getEffectiveDelay(instObj.inst, ii) " + pObj.getEffectiveDelay(instObj.inst, ii));
	System.out.println("execTime " + execTime);
	System.out.println("minStartTime " + minStartTime);
	System.out.println("pObj.startTime " + pObj.startTime);
	//if((cObj.unrollSt <= minStartTimeUR)||
	//   (cObj.startTime < minStartTime)) {
	//if(execTime < minStartTime) {
	if((pObj._iiCnt >= instObj._iiCnt)&&
	   (pObj.startTime <= execTime)) {
          unscheduleInst(pObj);
	  unscheduledInstSet.add(pObj);
	}

      }
    }*/
      
    //schedule the instruction:
    scheduleInst(instObj, execTime, ii);
    //return all the unscheduled instructions so the main method knows to 
    //try again on them:
    return unscheduledInstSet;
  }
  
  /**
  This too was copied from Rau.  Given an II and a number of times to
  attempt scheduling, go through the set of unscheduled instructions
  starting from the "tallest" and:
  
  1) check if it is possible to schedule this instruction with loads and
  stores around it
  2) find an earliest possible start time for this instruction based on its
  immidiate, scheduled predecessors
  3) choose an execution time for this instruction from estart to estart+ii
  (which is in the next iteration of the mod sched block)
  4) schedule the instruction and unschedule all conflicting instructions
  5) remove this instruction from the set of unscheduled instructions
  6) decrement budget
  7) repeat with the next tallest instruction until either the list of
  unscheduled instructions gets to 0 or until the maximum number of tries
  have been attempted (i.e. budget==0).
  
  finally return whether scheduling was successful (i.e. if the list of
  unscheduled instructions contains anything still)
  
  */
  public boolean iterativeSchedule(int ii, int budget) {
  
    InstHashSet unscheduledInstSet = new InstHashSet();
    unscheduledInstSet.addAll(this.getInstSet());
    
    while((unscheduledInstSet.size()>0) && (budget>0)) {
    
      MSchedInstObject instObj = unscheduledInstSet.tallestInst;
      if(!_loadStore.testIfSchedPossible(instObj, ii)) 
	return false;
      instObj.estart(ii);
      float runTime = instObj.findTimeSlot(ii);
      if(runTime>=0) {
        unscheduledInstSet.addAll(scheduleAtTime(instObj, runTime, ii));
        unscheduledInstSet.remove(instObj);
     }
      //else
        //unscheduleInstSet(instObj.badParents);
      budget--;
    }
    
    return (unscheduledInstSet.size()==0);
  
  }
}

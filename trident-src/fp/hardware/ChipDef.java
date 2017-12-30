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


package fp.hardware;

import java.util.*;

import fp.flowgraph.*;
import fp.hwdesc.Memory;
import fp.hwdesc.memClasses.IndexMatch;

/** This class saves information about the target FPGA and has several methods 
 *  for helping me to analyze hardware usage and limitations.
 * 
 * @author Kris Peterson
 */
public class ChipDef
{
  
  private class ArrayToMemMap extends HashMap {
  
    public ArrayToMemMap() {super();}
    
    public void saveArray(ArrayToArrayInfoMap.ArrayInfo array, Memory block) {
      super.put(array, block);
    }
    public void removeArray(ArrayToArrayInfoMap.ArrayInfo array) {
      super.remove(array);
    }
    public Memory get(ArrayToArrayInfoMap.ArrayInfo array) {
      return (Memory)super.get(array);
    }
    
    public int cost(BlockNode bNode) {
      int cost = -9999;
      for (Iterator memIt = values().iterator(); 
    	   memIt.hasNext(); ) {
	Memory block = (Memory)memIt.next();
    	cost = Math.max(cost, block.cost(bNode, _arrToArrInfoMap));
      }
      return cost;
    }
  
    public int trueCost(BlockNode bNode) {
      int cost = -9999;
      for (Iterator memIt = values().iterator(); 
    	   memIt.hasNext(); ) {
	Memory block = (Memory)memIt.next();
    	cost = Math.max(cost, block.trueCost(bNode, _arrToArrInfoMap));
      }
      return cost;
    }
  
  }
  
  //logic parameters
  /** amount of space on board for logic implementation
   */
  private int _sliceCnt;
  /** percentage of board that can be used for logic instead of wiring, muxes, 
   *  or other
   */
  private float _percentUsage;
   
  //memory block parameters:
  /** list of data for all the memories on or in the chip or board
   */
  private ArrayList _memoryBlocks;
   
  //logic availability info:
  /** used to save the number of an operator that is available to be used (e.g. 
   *  there may be 4 multipliers)
   */
  private HashMap _opCntsAvailable;
  /** the number of a given operator needed by the design (e.g. the design may 
   *  have 5 multiply operations and therefore needs 5 multipliers).
   */
  private HashMap _opCntsNeeded;
  /** a list of all the operators used in the design
   */
  private ArrayList _opList;
   
  private boolean _isDummyBoard;
   
   
  /** these two variables are used to save usage of modules
   */
  private HashMap _opUseCntHM;
  private HashMap _opUseListsHM;
  private BlockNode _node;
   
  /** default chip with 0 size?  how big should it be by default?
   */
  public ChipDef() {
    this(0,0, null);
    _isDummyBoard = false;
  }
   
  
  /** the Hardware analysis routine needs to schedule the board ignoring 
   *  hardware issues.  
   */
  public ChipDef(boolean isDummyBoard) {
    this(0,0, null);
    _isDummyBoard = isDummyBoard;
  }
  public ChipDef(int sliceCnt, float percentUsage, boolean isDummyBoard,
                 ArrayList memBlocks) {
    this(sliceCnt, percentUsage, null);
    _isDummyBoard = isDummyBoard;
    _memoryBlocks = memBlocks;

  }
  /** langweilig
   * 
   * @param sliceCnt measure of space on board available for logic
   * @param percentUsage perecentage of sliceCnt that can be used for    
   *      logic instead of wiring or other
   */
  //private static IndexMatch Memory.matchTester; // = new IndexMatch();
  public ChipDef(int sliceCnt, float percentUsage, ArrayList memBlocks) {
    _isDummyBoard = false;
    _sliceCnt = sliceCnt;
    _percentUsage = percentUsage;
    _opUseCntHM = new HashMap();
    _opUseListsHM = new HashMap();
    _memoryBlocks = memBlocks;
    _arrToArrInfoMap = new ArrayToArrayInfoMap();
    //Memory.matchTester = new IndexMatch();
  }

  public boolean isDummyBoard(){return _isDummyBoard;}
  public void makeDummyBoard(){_isDummyBoard = true;}
  public void unDummyBoard(){_isDummyBoard = false;}
  
  /** the get and set methods are all self explanatory
   * 
   * @return the sliceCnt
   */
  public int getSliceCnt() { return _sliceCnt;}
  public float getPercentUsage() { return _percentUsage;}
  public void setSliceCnt(int sliceCnt) { _sliceCnt = sliceCnt;}
  public void setPercentUsage(float percentUsage) { _percentUsage=percentUsage;}
  
  public ArrayList getMemoryBlockList() {
    return _memoryBlocks;
  } 
  
  private ArrayToArrayInfoMap _arrToArrInfoMap;
  private ArrayToMemMap _arrayToBlockMap = new ArrayToMemMap();
  private ArrayToMemMap _bestArrayToBlockMap = null;
  private int _bestArrayAllocCost = -999;
  
  /**
  
  */
  public Operator chooseStoreOp(Instruction inst) {
    Operand array = AStore.getPrimalDestination(inst);
    Memory mBlock = findMemoryBlock(array);
    if(mBlock==null) return null;
    return mBlock.getAStoreOp();
  }
  public Operator chooseLoadOp(Instruction inst) {
    Operand array = ALoad.getPrimalSource(inst);
    Memory mBlock = findMemoryBlock(array);
    if(mBlock==null) return null;
    return mBlock.getALoadOp();
  }
  
  /**
  this method attempts to allocate the given array to the given memory.
  */
  public boolean allocate(Memory block, 
                          ArrayToArrayInfoMap.ArrayInfo array) {
    if(_arrToArrInfoMap.arrayDoesntFit(block, array)) return false;
    if(block.allocateArray(array, _arrToArrInfoMap)) {
      _arrayToBlockMap.saveArray(array, block);
      return true;
    }
    else
      return false;
  }
  /**
  This method calls the MemoryBlock method loadIndexes which uses the information 
  from the GEP and ALoads and AStores which will be used to determine if arrays 
  can be packed together
  */
  public void loadIndexes(BlockGraph designBGraph) {
    Memory.matchTester.readIndex(designBGraph);
  }
  
  public void loadDesign(BlockGraph bGraph) {
    _arrToArrInfoMap.loadDesign(bGraph);
  }
  
  public void loadSchedule(BlockNode bNode) {
    _arrToArrInfoMap.loadSchedule(bNode);
  }
  
  public void deAllocateArray(Memory block, 
                              ArrayToArrayInfoMap.ArrayInfo array) {
    _arrayToBlockMap.removeArray(array);
    _arrToArrInfoMap.addArrayBack(array.getVar());
    block.deAllocateArray(array);
  }
  
  public void resetMemSpaceLeft() {
    for (Iterator itsMem = _memoryBlocks.iterator(); itsMem.hasNext(); ) {
      Memory memBlock = (Memory)itsMem.next();
      memBlock.setMemSizeLeft();
    }
  }
  public boolean arrayAllocate(BlockGraph bGraph) {
    resetMemSpaceLeft();
    //boolean hasntFailed = true;
    while((_arrToArrInfoMap.size()>0)/* && (hasntFailed)*/) {
      //hasntFailed = true;
      ArrayList arrays = new ArrayList(_arrToArrInfoMap.returnRemainingArrs());
      Collections.shuffle(arrays);
      _arrToArrInfoMap.printRemainingArrays();
      
      //for (Iterator arrs = _arrToArrInfoMap.returnRemainingArrs().iterator(); 
      for (Iterator arrs = arrays.iterator(); 
            arrs.hasNext(); ) {
	ArrayToArrayInfoMap.ArrayInfo arrInfo = 
	                            (ArrayToArrayInfoMap.ArrayInfo)arrs.next();
        
        if(!checkIfEnoughRoom(arrInfo)) return false;
	if(!memAllocate(arrInfo, bGraph)) 
          arrInfo.tryCnt++;
      }
    }
    return (_arrToArrInfoMap.returnRemainingArrs().size()<=0);
  }
  
  public boolean checkIfEnoughRoom(ArrayToArrayInfoMap.ArrayInfo array) {
    for (Iterator itsMem = _memoryBlocks.iterator(); itsMem.hasNext(); ) {
      Memory memBlock = (Memory)itsMem.next();
      if(!_arrToArrInfoMap.arrayDoesntFit(memBlock, array)) return true;
    }
    return false;
  }
  
  public void resetRemainingArrays() {
    _arrToArrInfoMap.resetRemainingArrays();
  }

  public void printRemainingArrays() {
    _arrToArrInfoMap.printRemainingArrays();
  }

  /**
  this method is for debugging.  It lets you see the contents of memory
  */
  public void displayMemsArrs() {
    for (Iterator itsMem = _memoryBlocks.iterator(); itsMem.hasNext(); ) {
      Memory memBlock = (Memory)itsMem.next();
      System.out.println("memBlock " + memBlock.getChipName());
      memBlock.displayMemContents(_arrToArrInfoMap);
    }
  }

  
  public boolean memAllocate(ArrayToArrayInfoMap.ArrayInfo array, 
                             BlockGraph bGraph) {
    ArrayList mems = new ArrayList(_memoryBlocks);
    Collections.shuffle(mems);
    boolean locFound = false;
    for (Iterator itsMem = mems.iterator(); itsMem.hasNext()&&
                                            !locFound; ) {
    //for (Iterator itsMem = _memoryBlocks.iterator(); itsMem.hasNext(); ) {
      Memory memBlock = (Memory)itsMem.next();
      boolean didAllocate = allocate(memBlock, array);
      int cost = cost(bGraph);
      if(cost==9999) cost = _memoryBlocks.size();
      if((didAllocate) && (cost-array.tryCnt<=0)) {
        memBlock.subSpace(array.arraySize);
	_arrToArrInfoMap.removeArray(array.var);
	locFound = true;
      }
      else {
        deAllocateArray(memBlock, array);
	locFound = false;
      }
    }
    return locFound;
  }
  
  
  /**
  This method is used by AnalyzeHardware to decide, given a predefined
  schedule, the "cost" of a certain array to memory aljlocation.  The cost
  depends on the number of cycles necessary to do all memory accesses on these
  arrays and considering the latencies of the instructions.
  */
  public int cost(BlockGraph bGraph) {
  
    int cost = -9999;
    for (Iterator vIt = new HashSet(bGraph.getAllNodes()).iterator(); 
           vIt.hasNext();) {
      BlockNode bNode = (BlockNode) vIt.next();
      cost = Math.max(cost, _arrayToBlockMap.cost(bNode));
    }
    return cost;
  
  }
  
  public int trueCost(BlockGraph bGraph) {
  
    int cost = -9999;
    for (Iterator vIt = new HashSet(bGraph.getAllNodes()).iterator(); 
           vIt.hasNext();) {
      BlockNode bNode = (BlockNode) vIt.next();
      cost = Math.max(cost, _arrayToBlockMap.trueCost(bNode));
    }
    return cost;
  
  }
  
  public void saveBestArrayAlloc(BlockGraph bGraph) {
    if(_bestArrayToBlockMap == null) {
      _bestArrayToBlockMap = new ArrayToMemMap();
      _bestArrayToBlockMap.putAll((HashMap)_arrayToBlockMap.clone());
      _bestArrayAllocCost = trueCost(bGraph);
    }
    else {
      int currentAllocCost = trueCost(bGraph);
      if(currentAllocCost < _bestArrayAllocCost) {
        _bestArrayToBlockMap = new ArrayToMemMap();
        _bestArrayToBlockMap.putAll((HashMap)_arrayToBlockMap.clone());
        //_bestArrayToBlockMap = (ArrayToMemMap)_arrayToBlockMap.clone();
	_bestArrayAllocCost = currentAllocCost;
      }
    }
  }
  
  public void changeToBestArrayAlloc() {
    for (Iterator arrs = ((ArrayToMemMap)_arrayToBlockMap.clone()).keySet().iterator(); 
    	  arrs.hasNext(); ) {
      ArrayToArrayInfoMap.ArrayInfo array = 
        			  (ArrayToArrayInfoMap.ArrayInfo)arrs.next();
      Memory mBlock = _arrayToBlockMap.get(array);
      deAllocateArray(mBlock, array);
      mBlock.addSpace(array.arraySize);
    }
    for (Iterator arrs = _bestArrayToBlockMap.keySet().iterator(); 
    	  arrs.hasNext(); ) {
      ArrayToArrayInfoMap.ArrayInfo array = 
        			  (ArrayToArrayInfoMap.ArrayInfo)arrs.next();
      Memory mBlock = _bestArrayToBlockMap.get(array);
      allocate(mBlock, array);
      mBlock.subSpace(array.arraySize);
    }
  }
  
  public void deallocateAll() {
    for (Iterator arrs = ((ArrayToMemMap)_arrayToBlockMap.clone()).keySet().iterator(); 
    	  arrs.hasNext(); ) {
      ArrayToArrayInfoMap.ArrayInfo array = 
        			  (ArrayToArrayInfoMap.ArrayInfo)arrs.next();
      Memory mBlock = _arrayToBlockMap.get(array);
      deAllocateArray(mBlock, array);
    }
    resetArrayToBlockMap();
  }
  
  public void resetArrToArrInfo() {
    _arrToArrInfoMap = new ArrayToArrayInfoMap();
  }
  
  public void resetArrayToBlockMap() {
    _arrayToBlockMap = new ArrayToMemMap();
  }
  
  public void resetPorts() {
    //if(_isDummyBoard) return;
    for (Iterator itsMem = _memoryBlocks.iterator(); itsMem.hasNext(); ) {
      Memory memBlock = (Memory)itsMem.next();
      memBlock.resetPorts();
    }
  }
  
  /**
  for testing an array to memory allocation assuming all aloads and astores
  are scheduled at the same time
  */
  public int calcLoad(BlockNode bNode, ArrayList instList) {
  
    HashMap memBlockLoadCost = new HashMap();
    HashMap memBlockStoreCost = new HashMap();
    int maxCost = 0;
    for (Iterator it1 = instList.iterator(); it1.hasNext();) {
      Instruction inst = (Instruction)it1.next();
      if(AStore.conforms(inst)) {
	Operand array = AStore.getPrimalDestination(inst);
	Memory mBlock = findMemoryBlock(array);
	int cost = mBlock.addStoreCnt(bNode, 0, array);
	maxCost = Math.max(maxCost, cost);
      }
      if(ALoad.conforms(inst)) {
	Operand array = ALoad.getPrimalSource(inst);
	Memory mBlock = findMemoryBlock(array);
	int cost = mBlock.addLoadCnt(bNode, 0, array);
	maxCost = Math.max(maxCost, cost);
      }
    }
    return maxCost;
  }
  
  /**
  This method looks for the slowest memory, so that its aload and astore
  instructions can be used by the preliminary schedule.
  */
  private class MemBlockPairStore {
    public Memory slowestReadMBlock = null;
    public Memory slowestWriteMBlock = null;
  }
  public MemBlockPairStore findSlowestMem() { 
    MemBlockPairStore memBlockPairStore = new MemBlockPairStore();
    Memory slowestReadMBlock = null;
    int slowesReadtLat = -9999; //slowest mem has the largest latency
    Memory slowestWriteMBlock = null;
    int slowesWritetLat = -9999; //slowest mem has the largest latency
    for (Iterator itsMem = _memoryBlocks.iterator(); itsMem.hasNext(); ) {
      Memory memBlock = (Memory)itsMem.next();
      int memReadLat = memBlock.findSlowestReadLat();
      if(memReadLat > slowesReadtLat) {
        slowesReadtLat = memReadLat;
	slowestReadMBlock = memBlock;
      }
      int memWriteLat = memBlock.findSlowestWriteLat();
      if(memWriteLat > slowesWritetLat) {
        slowesWritetLat = memWriteLat;
	slowestWriteMBlock = memBlock;
      }
    }
    memBlockPairStore.slowestReadMBlock = slowestReadMBlock;
    memBlockPairStore.slowestWriteMBlock = slowestWriteMBlock;
    return memBlockPairStore;
  }
  public void setInstructions(ArrayList instList) {
    MemBlockPairStore memBlockPairStore = findSlowestMem();
    for (Iterator it1 = instList.iterator(); it1.hasNext();) {
      Instruction inst = (Instruction)it1.next();
      if(AStore.conforms(inst)) {
        inst.operator = memBlockPairStore.slowestWriteMBlock.getAStoreOp();
        Operand array = AStore.getPrimalDestination(inst);
        ArrayToArrayInfoMap.ArrayInfo arr = _arrToArrInfoMap.get(array);
        //_arrayToBlockMap.saveArray(arr, memBlockPairStore.slowestWriteMBlock);
	//memBlockPairStore.slowestWriteMBlock.allocateArray(arr, 
	 //                                                 _arrToArrInfoMap);
	allocate(memBlockPairStore.slowestWriteMBlock, arr);
      }
      if(ALoad.conforms(inst)) {
        inst.operator = memBlockPairStore.slowestReadMBlock.getALoadOp();
        Operand array = ALoad.getPrimalSource(inst);
        ArrayToArrayInfoMap.ArrayInfo arr = _arrToArrInfoMap.get(array);
        //_arrayToBlockMap.saveArray(arr, memBlockPairStore.slowestReadMBlock);
	//memBlockPairStore.slowestReadMBlock.allocateArray(arr, 
	        //                                          _arrToArrInfoMap);
	allocate(memBlockPairStore.slowestReadMBlock, arr);
      }
    }
    
  }
  
  //old stuff:
  
  /** "@param cntTmp
   * 
   * @param cntTmp 
   */
  public void setOpCntsAvailable(HashMap cntTmp) {
    _opCntsAvailable = (HashMap)cntTmp.clone();
  } 
  public HashMap getOpCntsAvailable() {
    return _opCntsAvailable;
  } 
  /** "@param cntTmp
   */
  public void setOpCntsNeeded(HashMap cntTmp) {
    _opCntsNeeded = (HashMap)cntTmp.clone();
  } 
  public void setOpList(ArrayList listTmp) {
    _opList = (ArrayList)listTmp.clone();
  } 
  public ArrayList getOpList() { return _opList; } 
   
  public int getNumNeededForOp(Operator op_Operator) { 
    return ((Integer)(_opCntsNeeded.get(op_Operator))).intValue();
  }
  /** "@param op_Operator
   */
  public int getNumAvailableForOp(Operator op_Operator){ 
    return ((Integer)(_opCntsAvailable.get(op_Operator))).intValue();
  }
  
  /** searches through memories to see if a given primal operand has been 
   *  allocated to them.  When they are found, they are saved to 
   *  _arrayVarsMemBlock so that they can be accessed quicker. This method 
   *  returns the memory block if it was found. If it wasn't found, then 
   *  null is returned.
   * 
   * @param p array name
   * @return Returns the mem blk if it is in memory and null otherwise.
   */
   public Memory findMemoryBlock(Operand p) {
   
     ArrayToArrayInfoMap.ArrayInfo array = _arrToArrInfoMap.get(p);
     return _arrayToBlockMap.get(array);
   
   }

  /** checks if there would be hardware conflicts if this instruction were to be
   *  scheduled at this time.  
   * 
   * @param instr instruction to schedule
   * @param cycle desired time to schedule it
   * @return true=no prob; false = please, try again later
   */
  public boolean analyzeHardwareUse(BlockNode bNode, Instruction instr, 
                                    int cycle) {

    Operator operator = instr.operator();
    
    //check for too many writes:
    if(AStore.conforms(instr)) {
      Operand array = AStore.getPrimalDestination(instr);
      Memory mBlock = findMemoryBlock(array);
      /*System.out.println("looking at store conflicts");
      //displayMemsArrs();
      System.out.println("instr " + instr);
      System.out.println("cycle " + cycle);
      System.out.println("array " + array);
      System.out.println("bNode " + bNode);
      System.out.println("mBlock " + mBlock.getChipName());
      System.out.println("mBlock.addStoreTestCnt(bNode, cycle, array) " + mBlock.addStoreTestCnt(bNode, cycle, array));*/
      if(mBlock.addStoreTestCnt(bNode, cycle, array) > 1) return false;
    }
    
    //check for too many reads:
    if(ALoad.conforms(instr)) {
      Operand array = ALoad.getPrimalSource(instr);
      Memory mBlock = findMemoryBlock(array);
      /*System.out.println("looking at load conflicts");
      //displayMemsArrs();
      System.out.println("instr " + instr);
      System.out.println("cycle " + cycle);
      System.out.println("array " + array);
      System.out.println("bNode " + bNode);
      System.out.println("mBlock " + mBlock.getChipName());
      System.out.println("mBlock.addLoadTestCnt(bNode, cycle, array) " + mBlock.addLoadTestCnt(bNode, cycle, array));*/
     if(mBlock.addLoadTestCnt(bNode, cycle, array) > 1) return false;
    }
    
    if(_isDummyBoard) return true;
    
    //check for too many uses of an operator:
    //initialize if necessary:
    if(!(((HashMap)_opUseCntHM.get(_node)).containsKey(new Integer(cycle))))
      ((HashMap)_opUseCntHM.get(_node)).put(new Integer(cycle), new HashMap());
    if(!(((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new Integer(cycle)))).containsKey(operator)))
      ((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new Integer(cycle)))).put(operator, 
                                                          new Integer(0));
    
    //find old usage:
    int opUseCnt = ((Integer)(((HashMap)(((HashMap)_opUseCntHM.get(_node))
                                             .get(new Integer(cycle))))
        	                                  .get(operator))).intValue();
    HashMap operatorCntsAtTime = ((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new
                                                              Integer(cycle))));
    if(opUseCnt+1 > getNumAvailableForOp(operator)) {
      return false;
      
    }
    //if all the tests passed, then there are no hardware probs
    return true;
  }
  
  public HashSet killConflictingInstrucs(BlockNode bNode, Instruction instr, 
                                         int cycle, MSchedHash schedule) {

    HashSet unscheduled = new HashSet();
    Operator operator = instr.operator();
    
    //check for too many writes:
    if(AStore.conforms(instr)) {
      Operand array = AStore.getPrimalDestination(instr);
      Memory mBlock = findMemoryBlock(array);
      if(mBlock.addStoreTestCnt(bNode, cycle, array) > 1) {
	HashSet list = schedule.getAllAtTime((float)cycle);
	for (Iterator it1 = ((HashSet)list.clone()).iterator(); it1.hasNext();) {
          MSchedHash.MSchedInstObject instObj = 
        				(MSchedHash.MSchedInstObject)it1.next();
          if(AStore.conforms(instObj.inst)) {
            Operand prim = AStore.getPrimalDestination(instr);
            Memory otherMem = findMemoryBlock(prim);
            if(mBlock == otherMem) {
              schedule.unscheduleInst(instObj);
              unscheduled.add(instObj);
            }
          }
	}
      }
    }
    
    //check for too many reads:
    if(ALoad.conforms(instr)) {
      Operand array = ALoad.getPrimalSource(instr);
      Memory mBlock = findMemoryBlock(array);
      if(mBlock.addLoadTestCnt(bNode, cycle, array) > 1) {
        HashSet list = schedule.getAllAtTime((float)cycle);
	for (Iterator it1 = ((HashSet)list.clone()).iterator(); it1.hasNext();) {
	  MSchedHash.MSchedInstObject instObj = 
	                                (MSchedHash.MSchedInstObject)it1.next();
	  if(ALoad.conforms(instObj.inst)) {
	    Operand prim = ALoad.getPrimalSource(instr);
	    Memory otherMem = findMemoryBlock(prim);
	    if(mBlock == otherMem) {
	      schedule.unscheduleInst(instObj);
	      unscheduled.add(instObj);
	    }
	  }
	}
      }
    }

    if(_isDummyBoard) return unscheduled;
    
    //check for too many uses of an operator:
    //initialize if necessary:
    if(!(((HashMap)_opUseCntHM.get(_node)).containsKey(new Integer(cycle))))
      ((HashMap)_opUseCntHM.get(_node)).put(new Integer(cycle), new HashMap());
    if(!(((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new Integer(cycle)))).containsKey(operator)))
      ((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new Integer(cycle)))).put(operator, 
                                                          new Integer(0));
    
    //find old usage:
    HashMap opCntMap = ((HashMap)(((HashMap)_opUseCntHM.get(_node))
                                             .get(new Integer(cycle))));
    int opUseCnt = ((Integer)opCntMap.get(operator)).intValue();
    
    if(opUseCnt+1 > getNumAvailableForOp(operator)) {
        HashSet list = schedule.getAllAtTime((float)cycle);
	for (Iterator it1 = ((HashSet)list.clone()).iterator(); it1.hasNext();) {
        
	  MSchedHash.MSchedInstObject instObj = 
	                                (MSchedHash.MSchedInstObject)it1.next();
	  if(instObj.inst.operator() == instr.operator()) {
	    //opCntMap.put(operator, new Integer(--opUseCnt));
	    schedule.unscheduleInst(instObj);
	    unscheduled.add(instObj);
	  }
	
	}
    }
    
    return unscheduled;
          
  }
  
  public void removeFromTime(BlockNode bNode, Instruction instr, int cycle) {
  
    if(cycle >= 0) {
      if(AStore.conforms(instr)) {
        Operand array = AStore.getPrimalDestination(instr);
        Memory mBlock = findMemoryBlock(array);
        mBlock.subStoreCnt(bNode, cycle, array);
      }
      if(ALoad.conforms(instr)) {
        Operand array = ALoad.getPrimalSource(instr);
        Memory mBlock = findMemoryBlock(array);
        mBlock.subLoadCnt(bNode, cycle, array);
      }
    }
    
    if(_isDummyBoard) return;

    Operator operator = instr.operator();
    if((((HashMap)_opUseCntHM.get(_node)).containsKey(new Integer(cycle)))&&
    	(((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new Integer(cycle))))
        			   .containsKey(operator))) {
      int opUseCntOld = ((Integer)(((HashMap)(((HashMap)_opUseCntHM.get(_node))
        		   .get(new Integer(cycle))))
        				.get(operator)))
        				      .intValue();
      if(opUseCntOld >= 0)
    	((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new Integer(cycle))))
        	.put(operator, new Integer(opUseCntOld - 1));
    }
  
  }
  
  /** saves when the instruction is being executed and which module is doing it.  
   * 
   * @param instr instruction to schedule
   * @param cycle desired time to schedule it
   */
  public void saveNewHardwareUsage(BlockNode bNode, Instruction instr, 
                                   int cycle) {
    int maxRanktmp = (int)instr.getExecTime();
    Operator operator = instr.operator();
    if(!(_isDummyBoard)) { 
      if((((HashMap)_opUseCntHM.get(_node)).containsKey(new Integer(maxRanktmp)))&&
    	  (((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new Integer(maxRanktmp))))
        			     .containsKey(operator))) {
	int opUseCntOld = ((Integer)(((HashMap)(((HashMap)_opUseCntHM.get(_node))
        		     .get(new Integer(maxRanktmp))))
        				  .get(operator)))
        					.intValue();
	if(opUseCntOld >= 0)
    	  ((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new Integer(maxRanktmp))))
        	  .put(operator, new Integer(opUseCntOld - 1));
      }
    }
    if(maxRanktmp != -1) {
      if(AStore.conforms(instr)) {
        Operand array = AStore.getPrimalDestination(instr);
        Memory mBlock = findMemoryBlock(array);
        mBlock.subStoreCnt(bNode, maxRanktmp, array);
      }
      if(ALoad.conforms(instr)) {
        Operand array = ALoad.getPrimalSource(instr);
        Memory mBlock = findMemoryBlock(array);
        mBlock.subLoadCnt(bNode, maxRanktmp, array);
      }
    }
    
    if(!(_isDummyBoard)) { 
      HashMap operatorCntsAtTime = ((HashMap)(((HashMap)_opUseCntHM.get(_node)).get(new
                                                        	Integer(cycle))));

      if(operatorCntsAtTime == null) {
	operatorCntsAtTime = new HashMap();
	((HashMap)_opUseCntHM.get(_node)).put(new Integer(cycle), operatorCntsAtTime);
      }


      int opUseCnt = 0;
      if(operatorCntsAtTime.get(operator) != null)
	opUseCnt = ((Integer)operatorCntsAtTime.get(operator)).intValue();
      operatorCntsAtTime.put(operator, new Integer(opUseCnt+1));
    }
      
    if(AStore.conforms(instr)) {
      Operand array = AStore.getPrimalDestination(instr);
      Memory mBlock = findMemoryBlock(array);
      mBlock.addStoreCnt(bNode, cycle, array);
    }
    
    if(ALoad.conforms(instr)) {
      Operand array = ALoad.getPrimalSource(instr);
      Memory mBlock = findMemoryBlock(array);
      mBlock.addLoadCnt(bNode, cycle, array);
    }
    
    //save this instruction to the individual instantiation of the operator:
    if(!(_isDummyBoard)) { 
      int cntr = -1;
      for(int j = 0; j<((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(operator))).size();j++) {
	if(((HashSet)(((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(operator))).get(j)))
                                                            .contains(instr))
    	  ((HashSet)(((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(operator)))
        					.get(j))).remove(instr);
      }
      sort(((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(instr.operator()))));	 
      do{
	cntr++;
	while(((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(operator))).size() <= cntr)
    	  ((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(operator))).add(new HashSet());

      //assign to an operator that is unused at this clock tick
      }while(!noDuplicateTimes(((HashSet)(((ArrayList)(((HashMap)_opUseListsHM.get(_node))
    				   .get(operator))).get(cntr))), 
        		       cycle));

      ((HashSet)(((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(operator)))
    						.get(cntr))).add(instr);
    }
  }
  /** initialize the hashmaps.  
   * 
   */
  public void completeInitialize() {
    _opUseCntHM = new HashMap();
    _opUseListsHM = new HashMap();
  }
  /** initialize the hashmaps.  
   * 
   */
  public void initializeForOneNode(BlockNode node) {
    _opUseListsHM.put(node, new HashMap());
    _opUseCntHM.put(node, new HashMap());
  }
  /** initialize the hashmap.  
   * 
   */
  public void initOpUseLists(Operator op) {
    if(!((HashMap)_opUseListsHM.get(_node)).containsKey(op))
      ((HashMap)_opUseListsHM.get(_node)).put(op, new ArrayList());
  }
  /** groups instructions together that are sharing an operator and saves to 
   *  the instruction objects.  
   * 
   */
  public void saveNode(BlockNode node) {
    _node = node;
    if(!_opUseListsHM.containsKey(_node))
      _opUseListsHM.put(_node, new HashMap());
    if(!_opUseCntHM.containsKey(_node))
      _opUseCntHM.put(_node, new HashMap());
  }
  
  /** groups instructions together that are sharing an operator and saves to 
   *  the instruction objects.  
   * 
   */
  public void saveSharedOps() {
    if(_isDummyBoard) return;
    for (Iterator itsPrint = ((Set)(((HashMap)_opUseListsHM.get(_node)).keySet())).iterator(); 
    	  itsPrint.hasNext(); ) {
      Operator op = (Operator)itsPrint.next();
      for(int n_int=0; n_int<((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(op)))
        						   .size(); n_int++) {
    	if(((HashSet)(((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(op)))
        					   .get(n_int))).size() > 1) {
    	  for (Iterator its2 = ((HashSet)(((ArrayList)(((HashMap)_opUseListsHM.get(_node))
        			   .get(op))).get(n_int))).iterator();
        		       its2.hasNext(); ) {
    	    Instruction inst = (Instruction)its2.next();
    	    inst.setIsShared(true);
    	    inst.setShareSet(((HashSet)(((ArrayList)(((HashMap)_opUseListsHM.get(_node)).get(op)))
	                                                         .get(n_int))));
    	  }
    	}
      }
    }
  }
  
  /** this method has something to do with the resource limitations checking, 
   *  but unfortunately, I can't remember exactly what.
   * 
   * @param instrList list of instructions from the hyperblock
   * @param maxRank time to search for matches with in the    
   *	 instruction list
   * @return true if there are no times equal to maxRank
   */
  private boolean noDuplicateTimes(HashSet instrList, float maxRank) {
    for (Iterator its = ((HashSet)instrList.clone()).iterator(); 
      its.hasNext(); ) {
      Instruction inst = (Instruction)its.next();
      if(inst.getExecTime() == maxRank)
  	return false;
  	      
    }
    return true;
     
  }
  /** sort lists of instructions or hashsets...
   * 
   * @param o_list a list which needs sorting
   */
  private void sort(ArrayList o_list) {
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
  	if (o1 instanceof HashSet	      
  	&& o2 instanceof HashSet) {	     
  	  HashSet p1 = (HashSet)o1;	     
  	  HashSet p2 = (HashSet)o2;	     
  	  if( p1.size() < p2.size()) {  	  
  	    return -1;  	
  	    } else if( p1.size() > p2.size() ){ 	   
  	    return 1;	      
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


  public String toString() {
    String retval =  "ChipDef: \n";
    retval = retval + " _sliceCnt: " + _sliceCnt + "\n"; 
    retval = retval + " _percentUsage: " + _percentUsage + "\n"; 
    retval = retval + " _memoryBlocks: " + " \n"; 

    for (Iterator it = _memoryBlocks.iterator(); it.hasNext(); ) {
      Memory memBlock = (Memory)it.next();
      retval = retval + "    " + memBlock.toString() + "\n";
    }
    return retval;
  }

}

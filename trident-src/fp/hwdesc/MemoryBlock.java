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


package fp.hwdesc;

import java.util.*;

import fp.flowgraph.*;
import fp.hardware.ChipDef;
import fp.hardware.ArrayToArrayInfoMap;
import fp.hardware.AllocateArrays;
import fp.hwdesc.memClasses.IndexMatch;
import fp.GlobalOptions;

/** This class saves information about memories on the target FPGA including
 * their sizes and which arrays have been allocated to them.
 * 
 * @author Kris Peterson
 */
public class MemoryBlock extends Chip implements Memory {

  /**
  
  class PseudoStopAddresses
  
  -This class is nested in MemoryBlock, also because it was written only for
  MemoryBlock's use.
  
  This class was written to help me analyze whether arrays will fit in memory.
  It and ArrayToArrayInfoMap.ArrayInfo (but not AllocateArrays.ArrayInfo) use fake addresses.
  The AllocateArrays pass does the actual allocating of arrays to specific
  addresses in memory.  This class was written to support 
  AnalyzeHardwareConstraints, which allocates arrays to blocks of memory, 
  but not to addresses in those memories.  The reason I use pseudo addresses
  instead of just determining if there is space by adding up the sizes of the
  arrays in memory, and seeing if this is smaller than the size of the memory
  is that this fit the algorithm I designed to solve the problem (and of course
  there may be a better algoritm).  But my algorithm allocates in this way:
  
  foreach array
    foreach memory block
      (1)search for an open space in the block were an array could possibly fit
      (2)check if this would slow the execution of the program down (by considering
         where ALoads and AStores were scheduled in the preliminary schedule and
         if this allocation would require some of those instructions, which might
         have been run in parallel to be spread apart)
    (3)repeat until either the program will not be slown down, or a minium
       amount of slow down has been found
  
  That is, for example, we have the arrays A, B, C, and D and memory blocks 1 
  and 2, and arrays C and D can be packed together.  And further more, let's 
  assume in the preliminary schedule all three arrays were read simultaneously, 
  and each memory has only one read port.
  
  The algorithm at (1), would start by trying to put array A in memory 1 at 
  address 0.  Next it will check if it can put array B in memory 1.  First,
  it will try to put it in memory 1 at address 0.  But since A and B cannot
  be packed together, it will then place B after the last address of A.  Doing
  this, however will slow down execution, because two reads will be necessary.
  So next it will put B in memory 2, and this will not slow down execution, so 
  B will stay in memory 2.  Next it will try to allocate C to memory 1 at 
  address 0.  Again, it cannot be packed.  So it will try at the end of A.  
  Again, this will slow down execution.  So it will try memory 2 at address 0,
  but it cannot be packed with B either.  Then it will try after B ends, but
  this will slow down execution.  So it will start again with memory 1, but with
  a higher pain threshold for slowing down the execution of the design.  Again
  it will start at address 0, but be unable to pack with A.  Then it will place
  it after A, and decide this is good enough.  Then the algorithm will try to 
  place D.  It will start also in memory 1 and address 0.  But it cannot be 
  packed with A, so it will go on to the hole after A.  D can be packed with,
  C however, so it will be placed there (after trying to place it in memory 2
  and finding that execution was slowed down).  After raising the pain 
  threshhold for D, packing it with C will give the optimal solution.  
  
  I will repeat this in other places probably, but I wanted anyone who didn't
  understand why I did it this way to know, without having to search for the
  explanation somewhere else.
  
  */
  private class PseudoStopAddresses extends HashMap {
    //key = stop address
    //value = set of ArrInfos at this pseudoaddress
    
    private HashMap _startAddys = new HashMap();
    
    public PseudoStopAddresses() {
      super();
      _startAddys.put(new Long(0), new HashSet());
    }
    
    public HashSet get(long stopAddy) {
      return (HashSet)super.get(new Long(stopAddy));
    }
    
    public boolean containsKey(long stopAddy) {
      return super.containsKey(new Long(stopAddy));
    }
    
    public void remove(long stopAddy) {
      super.remove(new Long(stopAddy));
    }
    
    public void put(long stopAddy, HashSet setOfArrInfos) {
      super.put(new Long(stopAddy), setOfArrInfos);
    }
    
    public void saveStop(ArrayToArrayInfoMap.ArrayInfo array) {
    
      long stop = array.getStop();
      long start = array.getStart();
      HashSet arrAtAddyStop = null;
      if(containsKey(stop)) 
        arrAtAddyStop = get(stop);
      else {
        arrAtAddyStop = new HashSet();
	put(stop, arrAtAddyStop);
      }
      arrAtAddyStop.add(array);
      
      HashSet arrAtAddyStart = null;
      if(_startAddys.containsKey(new Long(start))) 
        arrAtAddyStart = (HashSet)_startAddys.get(new Long(start));
      else {
        arrAtAddyStart = new HashSet();
	_startAddys.put(new Long(start), arrAtAddyStart);
      }
      arrAtAddyStart.add(array);
    }
    
    public void forgetStop(ArrayToArrayInfoMap.ArrayInfo array) {
    
      long stop = array.getStop();
      long start = array.getStart();
      if(containsKey(stop)) {
        HashSet arrAtAddyStop = get(stop);
	if((arrAtAddyStop.size()==1)&&(arrAtAddyStop.contains(array))) {
	  remove(stop);
	  arrAtAddyStop.remove(array);
	}
	else
	  arrAtAddyStop.remove(array);
      }
      
      if(_startAddys.containsKey(new Long(start))) {
        HashSet arrAtAddyStart = (HashSet)_startAddys.get(new Long(start));
	if((arrAtAddyStart.size()==1)&&
	   (arrAtAddyStart.contains(array))) {
	  _startAddys.remove(new Long(start));
	  arrAtAddyStart.remove(array);
	}
	else
	  arrAtAddyStart.remove(array);
      }
    }
    
    public ArrayList getStopAddys() {
      ArrayList stopList = new ArrayList(super.keySet());
      Collections.sort(stopList);
      return stopList;
    }
    
    public boolean isThereSpaceInWordToPack(ArrayToArrayInfoMap.ArrayInfo array, 
                                            MemoryBlock mBlock) {
      long start = array.getStart();
      HashSet startAddys = (HashSet)_startAddys.get(new Long(start));
      long used = 0;
      if(startAddys != null) {
	for (Iterator start1It = startAddys.iterator(); 
             start1It.hasNext(); ) {
          ArrayToArrayInfoMap.ArrayInfo arr1= (ArrayToArrayInfoMap.ArrayInfo)start1It.next();
	  long arr1Size = array.getWordSize();
	  used += arr1Size;
        }
      }
      if(used + array.getWordSize() > mBlock.width) 
        return false;
      else
        return true;
    }
  } //end PseudoStopAddresses
  
  private HashMap _infoOnArrays;
  
  private long _totMemSizeLeft;
   
  private Operator _aload;
  private Operator _astore;
  
  /** for saving array packing information
   */
  private HashSet _listOfArrayVarGroupings;
  private HashSet _setOfArraysInMem;

  private String _name;
  private long _addr;
  
  private PortUsageSplitter _portUseSplit;
  private PseudoStopAddresses _pseudoAddys;
  
  private HashMap _saveArrLoadTimes;
  private HashMap _saveArrStoreTimes;
  
  public MemoryBlock() {
    _totMemSizeLeft = depth;
    //Memory.matchTester = new IndexMatch();
    _pseudoAddys = new PseudoStopAddresses();
    _pseudoAddys.put(0, new HashSet());
    _portUseSplit =  new PortUsageSplitter(port);
    _listOfArrayVarGroupings =  new HashSet();
    _setOfArraysInMem =  new HashSet();
    _infoOnArrays = new HashMap();
    _saveArrLoadTimes = new HashMap();
    _saveArrStoreTimes = new HashMap();
  }
  
  /**
  =====================================================================
  get and set section...
  */
  public void addArrayInfo(AllocateArrays.ArrayInfo a) {
    if(a != null)
      _infoOnArrays.put(a.name, a);
  }

  public void removeArrayInfo(AllocateArrays.ArrayInfo a) {
    if(a != null) 
      _infoOnArrays.remove(a.name);
  }

  public AllocateArrays.ArrayInfo getArrayInfo(String aName) {
    return (AllocateArrays.ArrayInfo) _infoOnArrays.get(aName);
  }

  public HashMap getArrayInfos() { return _infoOnArrays; }

  public String getName() { return _name; }
  public void setName(String name) { _name = name; }
  public String getChipName() { return name; }
  public void setChipName(String cName) { name = cName; }
  public int getWidth() { return width; }
  public void setWidth(int cWidth) { width = cWidth; }
  public int getDepth() { return depth; }
  public void setDepth(int cDepth) { depth = cDepth; }

  public void setAddressOffset(long addr) { _addr = addr; }
  public long getAddressOffset() { return _addr; }
  
   
  //_listOfArrayVarGroupings methods:
  public int getVarGroupingsSize() { return _listOfArrayVarGroupings.size();}
  public HashSet getVarGroupingsPtr() { return _listOfArrayVarGroupings;}
  public void setVarGroupingsPtr(HashSet varGroups) { 
    _listOfArrayVarGroupings=varGroups;
  }
  public void addToVarGroupingsPtr(HashSet arrSet) { 
    _listOfArrayVarGroupings.add(arrSet);
  }
   
  public void saveALoadOp(Operator aload){
    _aload = aload;
  }
  public Operator getALoadOp() {
    return _aload;
  }
   
  public void saveAStoreOp(Operator astore){
    _astore = astore;
  }
  public Operator getAStoreOp() {
    return _astore;
  }
  public long getMemSizeLeft() {return _totMemSizeLeft;}
  public void setMemSizeLeft() {_totMemSizeLeft = depth;}
  public void subSpace(long diff) {_totMemSizeLeft-=diff;}
  public void addSpace(long diff) {_totMemSizeLeft+=diff;}
  /**
  =====================================================================
  end get and set section...
  */
  
  //private static IndexMatch Memory.matchTester; // = new IndexMatch();
  
  /**
  I added these three back, because there were a few classes who wanted this 
  info
  */
  public int getNumOfWriteBus() {
    int writePortCnt = 0;
    for (Iterator portIt = port.iterator(); portIt.hasNext(); ) {
      Port portTmp = (Port)portIt.next();
      if((portTmp.typeCode == Port.DATA_WRITE_TYPE)||
         (portTmp.typeCode == Port.DATA_RW_TYPE))
    	writePortCnt++;
    }
    return writePortCnt;
  }
  
  public int getNumOfReadBus() {
    int readPortCnt = 0;
    for (Iterator portIt = port.iterator(); portIt.hasNext(); ) {
      Port portTmp = (Port)portIt.next();
      if((portTmp.typeCode == Port.DATA_READ_TYPE)||
         (portTmp.typeCode == Port.DATA_RW_TYPE))
    	readPortCnt++;
    }
    return readPortCnt;
  }
  
  public boolean getonlyOneAddy() {
    int addyPortCnt = 0;
    for (Iterator portIt = port.iterator(); portIt.hasNext(); ) {
      Port portTmp = (Port)portIt.next();
      if((portTmp.typeCode == Port.ADDRESS_READ_TYPE)||
         (portTmp.typeCode == Port.ADDRESS_WRITE_TYPE)||
         (portTmp.typeCode == Port.ADDRESS_RW_TYPE))
    	addyPortCnt++;
    }
    return (addyPortCnt == 1);
  }
  
  /**
  This method is used for debugging.  It allows me to check the contents of 
  memory
  */
  public void displayMemContents(ArrayToArrayInfoMap arrToArrInf) {
  
   if(getVarGroupingsPtr().size() == 0) {
      System.out.println("memory block empty");
      return;
    }  
    System.out.println("Sets of packed arrays:");
    for(Iterator packIt = getVarGroupingsPtr().iterator(); 
    	 packIt.hasNext(); ) {
      HashSet pack = (HashSet)packIt.next();
      
      //if(!pack.iterator().hasNext()) continue;
      System.out.println("arrays in pack ");
      for(Iterator arrIt = pack.iterator(); 
    	 arrIt.hasNext(); ) {
	Operand array= (Operand)arrIt.next();
	System.out.println("array " + array);
	ArrayToArrayInfoMap.ArrayInfo arr1 = arrToArrInf.get(array);
	long arr1Stop = arr1.getStop();
	long arr1start = arr1.getStart();
	System.out.println("at address " + arr1start);
	System.out.println("to address " + arr1Stop);
      }
    }
  
  }
  
  /**
  This method is for implementing Maya's idea for finding the slowest read and
  write ports for a memory so that the slowest memory will be used for making the
  preliminary schedule.
  */
  public int findSlowestReadLat() {
    int slowestPortLat = -9999;
    for (Iterator portIt = port.iterator(); portIt.hasNext(); ) {
      Port portTmp = (Port)portIt.next();
      if((portTmp.typeCode == Port.DATA_READ_TYPE)||
         (portTmp.typeCode == Port.DATA_RW_TYPE))
       slowestPortLat = Math.max(slowestPortLat, portTmp.read_latency);
    }
    return slowestPortLat;
  }
  
  public int findSlowestWriteLat() {
    int slowestPortLat = -9999;
    for (Iterator portIt = port.iterator(); portIt.hasNext(); ) {
      Port portTmp = (Port)portIt.next();
      if((portTmp.typeCode == Port.DATA_READ_TYPE)||
         (portTmp.typeCode == Port.DATA_RW_TYPE))
       slowestPortLat = Math.max(slowestPortLat, portTmp.write_latency);
    }
    return slowestPortLat;
  }
  
  /**
  This method tries to pack an array into the same address location as others
  at that location.  Returns true if successful and false if not.
  */
  public boolean saveToPackedArrCollect(ArrayToArrayInfoMap.ArrayInfo array, 
                                        ArrayToArrayInfoMap arrToArrInf) {
 
    for (Iterator packIt = getVarGroupingsPtr().iterator(); 
    	 packIt.hasNext(); ) {
      HashSet pack = (HashSet)packIt.next();
      if(!pack.iterator().hasNext()) continue;
      Operand arr1Op= (Operand)(pack.iterator().next());
      ArrayToArrayInfoMap.ArrayInfo arr1 = arrToArrInf.get(arr1Op);
      long arr1Stop = arr1.getStop();
      long arr1start = arr1.getStart();
      
      if((arr1Stop == array.getStop())&&
         (arr1start == array.getStart())&&
         (Memory.matchTester.matches(array.getVar(), arr1.getVar()))) {
        pack.add(array.getVar());
	return true;
      }
    }
    return false;
  
  }
  
  public boolean isInSamePack(Operand arr1, Operand arr2) {
    for (Iterator packIt = getVarGroupingsPtr().iterator(); 
    	 packIt.hasNext(); ) {
      HashSet pack = (HashSet)packIt.next();
      if(!pack.iterator().hasNext()) continue;
      if(pack.contains(arr1)&&pack.contains(arr2))
        return true;
    }
    return false;
  }
  
  public boolean isInSamePack(Operand arr1, HashSet otherArrays) {
    for (Iterator packIt = otherArrays.iterator(); 
    	 packIt.hasNext(); ) {
      Operand arr2 = (Operand)packIt.next();
      if(arr2==null) continue;
      if(arr1==arr2) continue;
      if(isInSamePack(arr1, arr2))
        return true;
    }
    return false;
  }
  
  /**
  This method tests if there are no arrays at this location and it is thus
  ok to place an array here
  */
  public boolean noPackdArrAtLoc(ArrayToArrayInfoMap.ArrayInfo array, 
                                 ArrayToArrayInfoMap arrToArrInf) {
  
    for (Iterator packIt = getVarGroupingsPtr().iterator(); 
    	 packIt.hasNext(); ) {
      HashSet pack = (HashSet)packIt.next();
      if(!pack.iterator().hasNext()) continue;
      Operand arr1Op= (Operand)(pack.iterator().next());
      ArrayToArrayInfoMap.ArrayInfo arr1 = arrToArrInf.get(arr1Op);
      long arr1Stop = arr1.getStop();
      long arr1start = arr1.getStart();
      
      if(((arr1Stop == array.getStop())&&
          (arr1start == array.getStart()))||
	 ((arr1start<=array.getStart())&&(arr1Stop>=array.getStart()))||
	 ((arr1start<=array.getStop())&&(arr1Stop>=array.getStop()))) {
       return false;
      }
    }
    HashSet pack2 = new HashSet();
    pack2.add(array.getVar());
    if(pack2.size() > 0)
      _listOfArrayVarGroupings.add(pack2);
    return true;
  }
  
  public void resetPorts() {
    for(Iterator portIt = port.iterator(); 
    	 portIt.hasNext(); ) {
      Port portTmp = (Port)portIt.next();
      portTmp.resetPortUseCnter();
    }
  }
  
  /**
  This method attempts to allocate an array to a given address location
  (which is saved in array, which is of type ArrayToArrayInfoMap.ArrayInfo, which holds the 
  location information)
  */
  public boolean allocateArray(ArrayToArrayInfoMap.ArrayInfo array, 
                               ArrayToArrayInfoMap arrToArrInf) {
    boolean locFound = false;
    _setOfArraysInMem.add(array);
    //if(GlobalOptions.slowestMem) return true;
    for (Iterator stopIt = _pseudoAddys.getStopAddys().iterator(); 
    	 stopIt.hasNext() && !locFound; ) {
      long time = ((Long)stopIt.next()).longValue();
      array.setStart(time+1);
      if(_pseudoAddys.isThereSpaceInWordToPack(array, this) 
	 && (( GlobalOptions.packArrays && saveToPackedArrCollect(array, arrToArrInf) )
	     || noPackdArrAtLoc(array, arrToArrInf) )) {
	_pseudoAddys.saveStop(array);
	locFound = true;
      }
    }
    return locFound;
  }
  
  public void deAllocateArray(ArrayToArrayInfoMap.ArrayInfo array) {
    _pseudoAddys.forgetStop(array);
    array.deAlloc();
    _setOfArraysInMem.remove(array);
    HashSet varGroupings = (HashSet)getVarGroupingsPtr().clone();
    for (Iterator packIt = varGroupings.iterator(); 
    	 packIt.hasNext(); ) {
      HashSet pack = (HashSet)packIt.next();
      if(((pack.size()<=1)&&(pack.contains(array.getVar())))/*||
         (pack.size()<=1)*/) {
	getVarGroupingsPtr().remove(pack);
      }
      pack.remove(array.getVar());
    }
    HashSet varGroupings2 = new HashSet(getVarGroupingsPtr());
    for (Iterator packIt2 = getVarGroupingsPtr().iterator(); 
    	 packIt2.hasNext(); ) {
      HashSet pack = (HashSet)packIt2.next();
      if(pack.size()==0) {
	varGroupings2.remove(pack);
     }
    }
    setVarGroupingsPtr(varGroupings2);
  }
  
  /**
  This method uses the information from the GEP and ALoads and AStores which 
  will be used to determine if arrays can be packed together
  */
  public void loadIndexes(BlockGraph designBGraph) {
  
    Memory.matchTester.readIndex(designBGraph);
  
  }
  
  public int trueCost(BlockNode bNode, ArrayToArrayInfoMap arrToArrInf) {
    return costGeneric(bNode, arrToArrInf, PortUsageSplitter.TRUECOST);
  }
  public int cost(BlockNode bNode, ArrayToArrayInfoMap arrToArrInf) {
    return costGeneric(bNode, arrToArrInf, PortUsageSplitter.NORMCOST);
  }
  private int costGeneric(BlockNode bNode, ArrayToArrayInfoMap arrToArrInf, 
                         boolean trueCost) {
    resetPorts();
    int lCost = 0;
    int sCost = 0;
    //int lCost = -1;
    //int sCost = -1;
    for (Iterator packIt = getVarGroupingsPtr().iterator(); 
    	 packIt.hasNext(); ) {
      HashSet pack = (HashSet)packIt.next();
      if(pack.size()==0) continue;
      if(!pack.iterator().hasNext()) continue;
      Operand arrp= (Operand)(pack.iterator().next());
      ArrayToArrayInfoMap.ArrayInfo array = arrToArrInf.get(arrp);
      HashMap loadSched = array.getLoadCntSched(bNode);
      
      for(Iterator loadsIt = loadSched.keySet().iterator(); 
    	   loadsIt.hasNext(); ) {
	   
	Integer time = (Integer)loadsIt.next();
	Integer cnt = (Integer)loadSched.get(time);
	
	//if(trueCost)
	lCost += _portUseSplit.addLoads(bNode, time.intValue(), cnt.intValue(),
	                                trueCost);
	//else
	//  lCost += _portUseSplit.addLoadsCnt(time.intValue(), cnt.intValue());
      }
      HashMap storeSched = array.getStoreCntSched(bNode);
      for(Iterator storesIt = storeSched.keySet().iterator(); 
    	   storesIt.hasNext(); ) {
	Integer time = (Integer)storesIt.next();
	Integer cnt = (Integer)storeSched.get(time);
	//if(trueCost)
	sCost += _portUseSplit.addStores(bNode, time.intValue(), cnt.intValue(),
	                                 trueCost);
	//else
	//  sCost += _portUseSplit.addStoresCnt(time.intValue(), cnt.intValue());
      }
    }
    //System.out.println("lCost " + lCost);
    //System.out.println("sCost " + sCost);
    return Math.max(lCost, sCost);
  }
  
  public int addLoadTest(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrLoadTimes.get(new Integer(time));
    if((previousArrays == null)||(!(isInSamePack(array, previousArrays)))) {
      return _portUseSplit.addLoadTest(bNode, time, PortUsageSplitter.TRUECOST);
    }
    else 
      //return _portUseSplit.addLoadTest(time);
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.LOAD);
  }
  
  public int addLoadTestCnt(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrLoadTimes.get(new Integer(time));
    if((previousArrays == null)||
    (!(isInSamePack(array, previousArrays)))) {
      return _portUseSplit.addLoadTest(bNode, time, PortUsageSplitter.NORMCOST);
    }
    else {
      //return _portUseSplit.addLoadTestCnt(time);
/*System.out.println("isInSamePack(array, previousArrays) " + isInSamePack(array, previousArrays));
System.out.println("array " + array);
System.out.println("previousArrays " + previousArrays);
System.out.println("matches");
System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&");*/
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.LOAD);
    }
  }
  
  public int addLoad(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrLoadTimes.get(new Integer(time));
    if(previousArrays == null) {
      previousArrays = new HashSet();
      previousArrays.add(array);
      _saveArrLoadTimes.put(new Integer(time), previousArrays);
      return _portUseSplit.addLoad(bNode, time, PortUsageSplitter.TRUECOST);
    }
    else if(isInSamePack(array, previousArrays))
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.LOAD);
    else {
      previousArrays.add(array);
      return _portUseSplit.addLoad(bNode, time, PortUsageSplitter.TRUECOST);
    }
  }
  
  public int addLoadCnt(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrLoadTimes.get(new Integer(time));
    if(previousArrays == null) {
      previousArrays = new HashSet();
      previousArrays.add(array);
      _saveArrLoadTimes.put(new Integer(time), previousArrays);
      return _portUseSplit.addLoad(bNode, time, PortUsageSplitter.NORMCOST);
    }
    else if(isInSamePack(array, previousArrays))
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.LOAD);
    else {
      previousArrays.add(array);
      return _portUseSplit.addLoad(bNode, time, PortUsageSplitter.NORMCOST);
    }
  }
  
  public int subLoad(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrLoadTimes.get(new Integer(time));
    if((previousArrays == null)||(isInSamePack(array, previousArrays))) {
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.LOAD);
    }
    else {
      if((previousArrays.size()==1)&&(previousArrays.contains(array)))
        _saveArrLoadTimes.remove(new Integer(time));
      previousArrays.remove(array);
      return _portUseSplit.subLoad(bNode, time, PortUsageSplitter.TRUECOST);
    }
  }
  
  public int subLoadCnt(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrLoadTimes.get(new Integer(time));
    if((previousArrays == null)||(isInSamePack(array, previousArrays))) {
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.LOAD);
    }
    else {
      if((previousArrays.size()==1)&&(previousArrays.contains(array)))
        _saveArrLoadTimes.remove(new Integer(time));
      previousArrays.remove(array);
      return _portUseSplit.subLoad(bNode, time, PortUsageSplitter.NORMCOST);
    }
  }
  
  public int addStoreTest(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrStoreTimes.get(new Integer(time));
    if((previousArrays == null)||(!(isInSamePack(array, previousArrays)))) {
      return _portUseSplit.addStoreTest(bNode, time, PortUsageSplitter.TRUECOST);
    }
    else 
      //return _portUseSplit.addStoreTest(time);
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.STORE);
  }
  
  public int addStoreTestCnt(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrStoreTimes.get(new Integer(time));
    if((previousArrays == null)||(!(isInSamePack(array, previousArrays)))) {
      return _portUseSplit.addStoreTest(bNode, time, PortUsageSplitter.NORMCOST);
    }
    else {
      //return _portUseSplit.addStoreTestCnt(time);
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.STORE);
    }
  }
  
  public int addStore(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrStoreTimes.get(new Integer(time));
    if(previousArrays == null) {
      previousArrays = new HashSet();
      previousArrays.add(array);
      _saveArrStoreTimes.put(new Integer(time), previousArrays);
      return _portUseSplit.addStore(bNode, time, PortUsageSplitter.TRUECOST);
    }
    else if(isInSamePack(array, previousArrays))
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.STORE);
    else {
      previousArrays.add(array);
      return _portUseSplit.addStore(bNode, time, PortUsageSplitter.TRUECOST);
    }
  }
  public int addStoreCnt(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrStoreTimes.get(new Integer(time));
    if(previousArrays == null) {
      previousArrays = new HashSet();
      previousArrays.add(array);
      _saveArrStoreTimes.put(new Integer(time), previousArrays);
      return _portUseSplit.addStore(bNode, time, PortUsageSplitter.NORMCOST);
    }
    else if(isInSamePack(array, previousArrays))
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.STORE);
    else {
      previousArrays.add(array);
      return _portUseSplit.addStore(bNode, time, PortUsageSplitter.NORMCOST);
    }
  }
  
  public int subStore(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrStoreTimes.get(new Integer(time));
    if((previousArrays == null)||(isInSamePack(array, previousArrays))) {
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.STORE);
    }
    else {
      if((previousArrays.size()==1)&&(previousArrays.contains(array)))
        _saveArrStoreTimes.remove(new Integer(time));
      previousArrays.remove(array);
      return _portUseSplit.subStore(bNode, time, PortUsageSplitter.TRUECOST);
    }
  }
  
  public int subStoreCnt(BlockNode bNode, int time, Operand array) {
    HashSet previousArrays = (HashSet)_saveArrStoreTimes.get(new Integer(time));
    if((previousArrays == null)||(isInSamePack(array, previousArrays))) {
      return _portUseSplit.getDataPortUseCnt(bNode, time, PortUsageSplitter.STORE);
    }
    else {
      if((previousArrays.size()==1)&&(previousArrays.contains(array)))
        _saveArrStoreTimes.remove(new Integer(time));
      previousArrays.remove(array);
      return _portUseSplit.subStore(bNode, time, PortUsageSplitter.NORMCOST);
    }
  }
  
 //I don't know if this should be used, but I commented it out, so I could use
 //Chip's definition of "toString."
 /* public String toString() {
    String retval = "MemoryBlock: \n";
    retval = retval + " _name: " + _name + "\n";
    /*retval = retval + " _isROM: " + _isROM + "\n";
    retval = retval + " _onlyOneAddy: " + _onlyOneAddy + "\n";
    retval = retval + " _wordSize: " + _wordSize + "\n";
    retval = retval + " _initReadLatency: " + _initReadLatency + "\n";
    retval = retval + " _normalReadLatency: " + _normalReadLatency + "\n";
    retval = retval + " _initWriteLatency: " + _initWriteLatency + "\n";
    retval = retval + " _normalWriteLatency: " + _normalWriteLatency + "\n";
    retval = retval + " _memSize: " + _memSize + "\n";
    retval = retval + " _numOfReadBus: " + _numOfReadBus + "\n";
    retval = retval + " _numOfWriteBus: " + _numOfWriteBus + "\n";
    retval = retval + " _addr: 0x"+Long.toHexString(_addr)+"\n";
    return retval;
  }*/

}

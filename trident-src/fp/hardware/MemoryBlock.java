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

/** This class saves information about memories on the target FPGA including
 * their sizes and which arrays have been allocated to them.
 * 
 * @author Kris Peterson
 */
public class MemoryBlock
{

  private HashMap _infoOnArrays;
  
  /** ROM flag
   */
  private boolean _isROM;
  /** only one address flag
   */
  private boolean _onlyOneAddy;
  /** size of memory words in bits
   */
  private int _wordSize;
  /** read latency of memory
   */
  private float _initReadLatency;
  /** time between pipelined reads
   */
  private float _normalReadLatency;
  /** write latency of memory
   */
  private float _initWriteLatency;
  /** time between pipelined writes
   */
  private float _normalWriteLatency;
  /** total size of memory bits
   */
  private int _memSize;
  private int _totMemSizeLeft;
   
  /** number of read data ports
   */
  private int _numOfReadBus;
  /** number of write data ports
   */
  private int _numOfWriteBus;
  
  /** variable used by bus count methods, see below
   */
  private int _index;
  /** for saving number of reads
   */
  private ArrayList _readBusUseCnts;
  /** for saving number of writes
   */
  private ArrayList _writeBusUseCnts;
   
  /** the following are variables used for keeping track of memory reads and
   * writes at specific times.
   */
  private HashMap _writeIndexAtTime;
  private HashMap _readIndexAtTime;
  private HashMap _readBusUseCntAtTime;
  private HashMap _writeBusUseCntAtTime;
  
  
  /** for saving arrays and their locations
   */
  //private ArrayList _varSpaces;
  /** for saving space left in different memory words
   */
  //private ArrayList _memLeftInSpace;
  
  /** for saving array packing information
   */
  private ArrayList _listOfArrayVarGroupings;

  private String _name;
  private long _addr;
  
  private HashSet _arraysInThisMem;
  private float _highestStopAddr;
  private int _stopListIndex;
  private ArrayList _stopList;
  
  public MemoryBlock() {
    this(false, false, 0, 0, 0, 0, 0, 0, 0, 0);
  }
  
  /** 
   * The four simplest types of memories (handled by this code) can fit in these categories:
   * 
   * true dual port, where there is a read port, a write port, a read address port, and a write address port.
   * partial dual port, where there is a read port, a write port, but only one address port for both data ports.
   * 1 port RAM, where there is one data port for both reads and writes
   * 1 port ROM, where there is one data port, which can only be read.
   * 
   * These special cases need to be handled differently than other memory types.  Other memory types,
   * just have as many read and write buses as defined by the vars, numOfReadBus and numOfWriteBus.
   * 
   * To recognize one of these special memory types, I use the boolean variables, isROM and onlyOneAddy.
   * A memory with 1 read bus, 1 write bus, but where onlyOneAddy is true is partial dual port.
   * If the sum of the number of read busses and write busses is 1 and isROM is false, then this is a
   * 1 port RAM.  If the number of read busses is 1 and isROM is true, this is a 1 port ROM.
   */
  public MemoryBlock(boolean isROM, boolean onlyOneAddy, int wordSize, 
                     float initReadLatency, float normalReadLatency, 
		     float initWriteLatency, float normalWriteLatency, 
		     int memSize, int numOfReadBus, int numOfWriteBus) {
    _infoOnArrays = new HashMap();
    _isROM = isROM;
    _onlyOneAddy = onlyOneAddy;
    _wordSize = wordSize;
    _initReadLatency = initReadLatency;
    _normalReadLatency = normalReadLatency;
    _initWriteLatency = initWriteLatency;
    _normalWriteLatency = normalWriteLatency;
    _memSize = memSize;
    _totMemSizeLeft  = memSize;
    
    //new memory stuff
    _arraysInThisMem = new HashSet();
    _highestStopAddr = 0;
    _stopListIndex = 0;
    _stopList = new ArrayList();
    _stopList.add(new Float(0.0));
    
    _numOfReadBus = numOfReadBus;
    _numOfWriteBus = numOfWriteBus;
    
    //_memLeftInSpace = new ArrayList();
    //_varSpaces = new ArrayList();
    //for(int i=0; i < memSize/wordSize; i++) {
    //  _memLeftInSpace.add(new Integer(wordSize));
    //  _varSpaces.add(new ArrayList());
    //}
    _listOfArrayVarGroupings = new ArrayList();
    _index = 0;
    _readBusUseCnts = new ArrayList();
    _writeBusUseCnts = new ArrayList();
    if((_onlyOneAddy && (_numOfReadBus == 1) && (_numOfWriteBus == 1)) 
         || (_numOfReadBus + _numOfWriteBus == 1)) {
      _readBusUseCnts.add(new Integer(0));
    }
    else {
      for(int i=0;i<numOfReadBus;i++)
        _readBusUseCnts.add(new Integer(0));
      for(int i=0;i<numOfWriteBus;i++)
        _writeBusUseCnts.add(new Integer(0));
    }
     
    _writeIndexAtTime = new HashMap();
    _readIndexAtTime = new HashMap();
    _readBusUseCntAtTime = new HashMap();
    _writeBusUseCntAtTime = new HashMap();
  }
   
   
  public boolean gibtEsGenugPlatzHier(float start, float stop, int wordSize) {
  
    int totUsedWordSize = 0;
//System.out.println("start " + start + " stop " + stop + " wordSize " + wordSize);
    for (Iterator its = ((HashSet)_arraysInThisMem).iterator(); 
    	  its.hasNext(); ) {
      ArrayInfo arr = (ArrayInfo)its.next();
      //System.out.println("arr " + arr.getVar());
      //System.out.println("arr getStart " + arr.getStart());
      //System.out.println("arr getStop " + arr.getStop());
      if(((arr.getStart() <= start) && (start <= arr.getStop()))||
         ((arr.getStart() <= stop) && (stop <= arr.getStop())))
        totUsedWordSize += arr.getWordSize();
      
    }
    
//System.out.println("totUsedWordSize " + totUsedWordSize + " _wordSize " + _wordSize);
    if(totUsedWordSize + wordSize > _wordSize)
      return false;
    else
      return true;
  
  } 
  
  public float getHighestStopAddr() {return _highestStopAddr;}
  public void resetStopListIndex() {_stopListIndex=0;}
  public float nextStopAddr() {
    //if(_stopListIndex >= _stopList.size())
    //  return 0;
    Float currentStopAddr = ((Float)_stopList.get(_stopListIndex));
    while(_stopList.get(_stopListIndex) == currentStopAddr)
      _stopListIndex++;
    
    return ((Float)_stopList.get(_stopListIndex)).floatValue();
  }
   
  /** Using this method, all the memory parameters can be set simultaneously.
   * I've never actually used it though.  I always just use the constructor to
   * do this.
   * 
   * @param isROM is the memory a ROM?
   * @param onlyOneAddy Does it only have 1 address bus?
   * @param wordSize what is the word size of the memory?
   * @param initReadLatency what is the initial read latency?
   * @param normalReadLatency can reads be pipelined and if so, what is  
   *      the minimum allowed time between reads?
   * @param initWriteLatency what is the initial write latency?
   * @param normalWriteLatency can writes be pipelined and if so, what is
   *      the minimum allowed time between writes?
   * @param memSize size of memory bits
   * @param numOfReadBus number of data read ports
   * @param numOfWriteBus number of data write ports
   */
  public void setAllValues(boolean isROM, boolean onlyOneAddy, int wordSize, 
                           float initReadLatency, float normalReadLatency, 
                           float initWriteLatency, float normalWriteLatency, 
                           int memSize, int numOfReadBus, int numOfWriteBus) {
    _isROM = isROM;
    _onlyOneAddy = onlyOneAddy;
    _wordSize = wordSize;
    _initReadLatency = initReadLatency;
    _normalReadLatency = normalReadLatency;
    _initWriteLatency = initWriteLatency;
    _normalWriteLatency = normalWriteLatency;
    _memSize = memSize;
    _numOfReadBus = numOfReadBus;
    _numOfWriteBus = numOfWriteBus;
  }
   
  /** I think, the get and set methods should all be self explanatory...
   */
  public boolean getisROM() { return _isROM;}
  public boolean getonlyOneAddy() { return _onlyOneAddy;}
  public int getWordSize() { return _wordSize;}
  public float getInitReadLatency() { return _initReadLatency;}
  public float getNormalReadLatency() { return _normalReadLatency;}
  public float getInitWriteLatency() { return _initWriteLatency;}
  public float getNormalWriteLatency() { return _normalWriteLatency;}
  public int getMemSize() { return _memSize;}
  public int getTotMemSizeLeft() { return _totMemSizeLeft;}
  
  public int getNumOfReadBus() { return _numOfReadBus;}
  public int getNumOfWriteBus() { return _numOfWriteBus;}
  
  /** "@param i
   */
  //public int getMemSizeLeft(int i) { 
  //  return (int)(((Integer)(_memLeftInSpace.get(i))).floatValue());
  //}
  //public ArrayList getVarsAtLoc(int i) { return (ArrayList)(_varSpaces.get(i));}
   
  public void setisROM(boolean isROM) { _isROM = isROM;}
  public void setonlyOneAddy(boolean onlyOneAddy) { _onlyOneAddy = onlyOneAddy;}
  public void setWordSize(int wordSize) { _wordSize = wordSize;}
  public void setInitReadLatency(float initLatency) { 
    _initReadLatency = initLatency;
  }
  public void setNormalReadLatency(float normalLatency) { 
    _normalReadLatency = normalLatency;
  }
  /** "@param initLatency
   */
  public void setInitWriteLatency(float initLatency) { 
    _initWriteLatency = initLatency;
  }
  /** "@param normalLatency
   */
  public void setNormalWriteLatency(float normalLatency) { 
    _normalWriteLatency = normalLatency;
  }
  /** "@param memSize
   */
  public void setMemSize(int memSize) { _memSize = memSize;}
  public void setTotMemSizeLeft(int memSize) { _totMemSizeLeft = memSize;}
   
  /** "@param numOfReadBus
   */
  public void setNumOfReadBus(int numOfReadBus) { _numOfReadBus = numOfReadBus;}
  public void setNumOfWriteBus(int numOfWriteBus) { 
    _numOfWriteBus = numOfWriteBus;
  }
  
  //public void setMemSizeLeft(int i, int NewMemSize) { 
  //  _memLeftInSpace.set(i, new Integer(NewMemSize));
  //}
  /** "@param i
   * 
   * @param i 
   * @param var_Operand 
   */
  public HashSet getVarsInMem() { return _arraysInThisMem; }
  public void saveVarLoc(float i, ArrayInfo var) { 
    //((ArrayList)(_varSpaces.get(i))).add(var_Operand);
    _arraysInThisMem.add(var);
    var.setStart(i);
    _stopList.add(new Float(var.getStop()));
    Collections.sort(_stopList);
    _highestStopAddr = Math.max(_highestStopAddr, var.getStop());
    _stopListIndex = 0;
  }

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

  public void setAddressOffset(long addr) { _addr = addr; }
  public long getAddressOffset() { return _addr; }
  
   
  //_listOfArrayVarGroupings methods:
  public int getVarGroupingsSize() { return _listOfArrayVarGroupings.size();}
  public ArrayList getVarGroupingsPtr() { return _listOfArrayVarGroupings;}
  /** "@param L
   */
  public void saveVarGroupings(ArrayList l) { 
    _listOfArrayVarGroupings.addAll(l);
  }
   
  /** this function resents all the bus usage count information
   */
  public void resetBusUseCnts() {
    if(_onlyOneAddy || (_numOfReadBus + _numOfWriteBus == 1)) {
      _readBusUseCnts.add(new Integer(0));
    }
    else {
      for(int i=0;i<_numOfReadBus;i++)
        _readBusUseCnts.add(new Integer(0));
      for(int i=0;i<_numOfWriteBus;i++)
        _writeBusUseCnts.add(new Integer(0));
    }
     
     
  }
  /** This method is for counting uses of the write bus independent of time.  
   *  The count is equal to the number of clock ticks it would take to perform 
   *  all requested writes on this memory.  It takes into account both the 
   *  number of AStore operations and the number of data write ports available 
   *  to carry out the task.
   */
  public void incWriteBusUseCnt() {
    if((_onlyOneAddy && (_numOfReadBus == 1) && (_numOfWriteBus == 1)) 
         || (_numOfReadBus + _numOfWriteBus == 1)) {
      //for 1 port memories just increment the read bus count
      int writeCntTmp = ((Integer)(_readBusUseCnts.get(0))).intValue();
      _readBusUseCnts.set(0, new Integer(++writeCntTmp));
    }
    else {
    //for multiple port memories, save a write count for each port.  For each
    //write, save it to a different port, so that they are spread out.  The 
    //number of clock ticks needed for all writes is equal to the largest of
    //these values
      int writeCntTmp = ((Integer)(_writeBusUseCnts.get(_index))).intValue();
      _writeBusUseCnts.set(_index, new Integer(++writeCntTmp));
      _index++;
      if(_index >= _numOfWriteBus)
        _index = 0;
    }
  }
  /** This method is for counting uses of the read bus independent of time.  The 
   *  count is equal to the number of clock ticks it would take to perform all 
   *  requested reads on this memory.  It takes into account both the number of 
   *  ALoad operations and the number of data read ports available to carry out 
   *  the task.
   */
  public void incReadBusUseCnt() {
    //see comments in incWriteBusUseCnt
    if((_onlyOneAddy && (_numOfReadBus == 1) && (_numOfWriteBus == 1)) 
            || (_numOfReadBus + _numOfWriteBus == 1)) {
      int readCntTmp = ((Integer)(_readBusUseCnts.get(0))).intValue();
      _readBusUseCnts.set(0, new Integer(++readCntTmp));
    }
    else {
      int readCntTmp = ((Integer)(_readBusUseCnts.get(_index))).intValue();
      _readBusUseCnts.set(_index, new Integer(++readCntTmp));
      _index++;
      if(_index >= _numOfReadBus)
        _index = 0;
    }
  }
   
  /** This method is for getting the write bus count
   * 
   * @return number of clock ticks to handle all memory writes
   */
  public int getWriteBusUseCnt() {
    //return the count from the bus with the highest number.  This is equal to 
    //the number of clock ticks necessary to perform all the writes
    int cnt = 0;
    if((_onlyOneAddy && (_numOfReadBus == 1) && (_numOfWriteBus == 1)) 
          || (_numOfReadBus + _numOfWriteBus == 1)) {
      for (Iterator itsPrint = ((ArrayList)(_readBusUseCnts)).iterator(); 
                itsPrint.hasNext(); ) {
        int cntTmp_int = ((Integer)itsPrint.next()).intValue();
        if( cntTmp_int > cnt )
          cnt = cntTmp_int;
      }
    }
    else {
      for (Iterator itsPrint = ((ArrayList)(_writeBusUseCnts)).iterator(); 
                itsPrint.hasNext(); ) {
        int cntTmp_int = ((Integer)itsPrint.next()).intValue();
        if( cntTmp_int > cnt )
          cnt = cntTmp_int;
      }
    }
    return cnt;
  }
  /** This method is for getting the read bus count
   * 
   * @return number of clock ticks to handle all memory reads
   */
  public int getReadBusUseCnt() {
  
    //once again, please see the write comments
    int cnt = 0;
    for (Iterator itsPrint = ((ArrayList)(_readBusUseCnts)).iterator(); 
              itsPrint.hasNext(); ) {
      int cntTmp_int = ((Integer)itsPrint.next()).intValue();
      if( cntTmp_int > cnt )
        cnt = cntTmp_int;
    }
    return cnt;
  }
  /** the next several methods are for measuring memory access over time.  This
   *  method initializes the storage hashmaps used by all those methods.
   */
  public void resetBusCntsAtAllTimes() {
    _writeIndexAtTime = new HashMap();
    _readIndexAtTime = new HashMap();
    _readBusUseCntAtTime = new HashMap();
    _writeBusUseCntAtTime = new HashMap();
  }
  /** This method is for counting uses of the write bus at a given clock tick.  
   *  The count is equal to the number of clock ticks it would take to perform 
   *  all requested writes on this memory.  It takes into account both the 
   *  number of AStore operations and the number of data write ports available 
   *  to carry out the task.
   * 
   * @param i clock tick to count a write at
   */
  public void incWriteBusUseCntAtTime(int i) {
    //initialize hashmaps
    if(!_readBusUseCntAtTime.containsKey(new Integer(i)))
      _readBusUseCntAtTime.put(new Integer(i), new ArrayList());
    if(!_writeBusUseCntAtTime.containsKey(new Integer(i)))
      _writeBusUseCntAtTime.put(new Integer(i), new ArrayList());
    
    //for one port memories, use the read bus count data bases:
    //but see the comments in the else statement to see what I'm doing here
    if((_onlyOneAddy && (_numOfReadBus == 1) && (_numOfWriteBus == 1)) 
         || (_numOfReadBus + _numOfWriteBus == 1)) {
      if(((ArrayList)(_readBusUseCntAtTime.get(new Integer(i)))).size() <= 0)
        ((ArrayList)(_readBusUseCntAtTime
	                             .get(new Integer(i)))).add(new Integer(0));
      int writeCntTmp = ((Integer)(((ArrayList)(_readBusUseCntAtTime
                                     .get(new Integer(i)))).get(0))).intValue();
      ((ArrayList)(_readBusUseCntAtTime
                      .get(new Integer(i)))).set(0, new Integer(++writeCntTmp));
    }
    else {
      //more initialize stuff
      if(!_writeIndexAtTime.containsKey(new Integer(i)))
        _writeIndexAtTime.put(new Integer(i), new Integer(0));
	
      //find out the next port to write too at this time. This will
      //alternate to spread out the writes.
      int IndexTmp = ((Integer)(_writeIndexAtTime
                                              .get(new Integer(i)))).intValue();
      //more initializing:
      while(((ArrayList)(_writeBusUseCntAtTime
                                      .get(new Integer(i)))).size() <= IndexTmp)
        ((ArrayList)(_writeBusUseCntAtTime
	                             .get(new Integer(i)))).add(new Integer(0));
      //increment the bus count for this bus at this time:
      int writeCntTmp = ((Integer)(((ArrayList)(_writeBusUseCntAtTime
                              .get(new Integer(i)))).get(IndexTmp))).intValue();
      ((ArrayList)(_writeBusUseCntAtTime
               .get(new Integer(i)))).set(IndexTmp, new Integer(++writeCntTmp));
      //go on to the next bus, and save this, so that the next write at this 
      //time will happen on the next bus:
      IndexTmp++;
      if(IndexTmp >= _numOfWriteBus)
        IndexTmp = 0;
      _writeIndexAtTime.put(new Integer(i), new Integer(IndexTmp));
    }
  }
  /** This method decrements the write count at a given clock tick
   * 
   * @param i clock tick in which the write count should be decremented
   */
  public void decWriteBusUseCntAtTime(int i) {
    //hopefully there won't be any decrements until there are increments, and so initializing the arrays is unnecessary
    //once again, one port memories, use the read bus count for all usage 
    //calulations:
    if((_onlyOneAddy && (_numOfReadBus == 1) && (_numOfWriteBus == 1)) 
          || (_numOfReadBus + _numOfWriteBus == 1)) {
      int writeCntTmp = ((Integer)(((ArrayList)(_readBusUseCntAtTime
                                     .get(new Integer(i)))).get(0))).intValue();
      ((ArrayList)(_readBusUseCntAtTime
                      .get(new Integer(i)))).set(0, new Integer(--writeCntTmp));
    }
    else {
      //find which bus was last written to
      int IndexTmp = ((Integer)(_writeIndexAtTime
                                              .get(new Integer(i)))).intValue();
      //go to the bus before
      IndexTmp--;
      if(IndexTmp < 0)
        IndexTmp = _numOfWriteBus-1;
      //decrement the write count for this bus at this time
      int writeCntTmp = ((Integer)(((ArrayList)(_writeBusUseCntAtTime
                              .get(new Integer(i)))).get(IndexTmp))).intValue();
      ((ArrayList)(_writeBusUseCntAtTime
               .get(new Integer(i)))).set(IndexTmp, new Integer(--writeCntTmp));
      //save the new bus
      _writeIndexAtTime.put(new Integer(i), new Integer(IndexTmp));
    }
  }
  /** This method is for counting uses of the read bus at a given clock tick.  
   *  The count is equal to the number of clock ticks it would take to perform 
   *  all requested reads on this memory.  It takes into account both the number
   *  of ALoad operations and the number of data read ports available to carry 
   *  out the task.
   * 
   * @param i clock tick to count a read at
   */
  public void incReadBusUseCntAtTime(int i) {
    //please refer to the comments in the write bus count methods!
    if(!_readBusUseCntAtTime.containsKey(new Integer(i)))
      _readBusUseCntAtTime.put(new Integer(i), new ArrayList());
    if((_onlyOneAddy && (_numOfReadBus == 1) && (_numOfWriteBus == 1)) 
          || (_numOfReadBus + _numOfWriteBus == 1)) {
      if(((ArrayList)(_readBusUseCntAtTime.get(new Integer(i)))).size() <= 0)
        ((ArrayList)(_readBusUseCntAtTime
	                             .get(new Integer(i)))).add(new Integer(0));
      int readCntTmp = ((Integer)(((ArrayList)(_readBusUseCntAtTime
                                     .get(new Integer(i)))).get(0))).intValue();
      ((ArrayList)(_readBusUseCntAtTime
                       .get(new Integer(i)))).set(0, new Integer(++readCntTmp));
    }
    else {
      if(!_readIndexAtTime.containsKey(new Integer(i)))
        _readIndexAtTime.put(new Integer(i), new Integer(0));
      int IndexTmp = ((Integer)(_readIndexAtTime.get(new Integer(i))))
                                                                    .intValue();
      while(((ArrayList)(_readBusUseCntAtTime.get(new Integer(i)))).size() 
                       <= IndexTmp)
        ((ArrayList)(_readBusUseCntAtTime.get(new Integer(i))))
	                                                   .add(new Integer(0));
      int readCntTmp = ((Integer)(((ArrayList)(_readBusUseCntAtTime
                              .get(new Integer(i)))).get(IndexTmp))).intValue();
      ((ArrayList)(_readBusUseCntAtTime.get(new Integer(i))))
                                      .set(IndexTmp, new Integer(++readCntTmp));
      IndexTmp++;
      if(IndexTmp >= _numOfReadBus)
        IndexTmp = 0;
      _readIndexAtTime.put(new Integer(i), new Integer(IndexTmp));
    }
  }
  /** This method decrements the read count at a given clock tick
   * 
   * @param i clock tick in which the read count should be decremented
   */
  public void decReadBusUseCntAtTime(int i) {
    if((_onlyOneAddy && (_numOfReadBus == 1) && (_numOfWriteBus == 1)) 
         || (_numOfReadBus + _numOfWriteBus == 1)) {
      int readCntTmp = ((Integer)(((ArrayList)(_readBusUseCntAtTime
                                     .get(new Integer(i)))).get(0))).intValue();
      ((ArrayList)(_readBusUseCntAtTime
                       .get(new Integer(i)))).set(0, new Integer(--readCntTmp));
    }
    else {
      int IndexTmp = ((Integer)(_readIndexAtTime
                                              .get(new Integer(i)))).intValue();
      IndexTmp--;
      if(IndexTmp < 0)
        IndexTmp = _numOfReadBus-1;
      int readCntTmp = ((Integer)(((ArrayList)(_readBusUseCntAtTime
                              .get(new Integer(i)))).get(IndexTmp))).intValue();
      ((ArrayList)(_readBusUseCntAtTime.get(new Integer(i)))).set(IndexTmp, 
                                                     new Integer(--readCntTmp));
      _readIndexAtTime.put(new Integer(i), new Integer(IndexTmp));
    }
  }
   
  /** This method is for getting the write bus count at a given time
   * 
   * @param i clock tick in which the write count should be returned from
   * @return number of clock ticks to handle all memory writes
   */
  public int getWriteBusUseCntAtTime(int i) {
    int cnt = 0;
    if((_onlyOneAddy && (_numOfReadBus == 1) && (_numOfWriteBus == 1)) 
      || (_numOfReadBus + _numOfWriteBus == 1)) {
      if(!_readBusUseCntAtTime.containsKey(new Integer(i)))
        return 0;
      for (Iterator itsPrint = ((ArrayList)(_readBusUseCntAtTime
                                              .get(new Integer(i)))).iterator(); 
              itsPrint.hasNext(); ) {
        int cntTmp_int = ((Integer)itsPrint.next()).intValue();
        if( cntTmp_int > cnt )
          cnt = cntTmp_int;
      }
    }
    else {
      if(!_writeBusUseCntAtTime.containsKey(new Integer(i)))
        return 0;
      for (Iterator itsPrint = ((ArrayList)(_writeBusUseCntAtTime
                                              .get(new Integer(i)))).iterator(); 
              itsPrint.hasNext(); ) {
        int cntTmp_int = ((Integer)itsPrint.next()).intValue();
        if( cntTmp_int > cnt )
          cnt = cntTmp_int;
      }
    }
    return cnt;
  }
  /** This method is for getting the read bus count at a given time
   * 
   * @param i clock tick in which the read count should be returned from
   * @return number of clock ticks to handle all memory reads
   */
  public int getReadBusUseCntAtTime(int i) {
    int cnt = 0;
    if(!_readBusUseCntAtTime.containsKey(new Integer(i)))
      return 0;
    for (Iterator itsPrint = ((ArrayList)(_readBusUseCntAtTime
                                              .get(new Integer(i)))).iterator(); 
              itsPrint.hasNext(); ) {
      int cntTmp_int = ((Integer)itsPrint.next()).intValue();
      if( cntTmp_int > cnt )
        cnt = cntTmp_int;
    }
    return cnt;
  }
  
  public String toString() {
    String retval = "MemoryBlock: \n";
    retval = retval + " _name: " + _name + "\n";
    retval = retval + " _isROM: " + _isROM + "\n";
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
  }

}

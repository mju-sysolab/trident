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
import fp.hwdesc.Chip;

/** This class saves information about arrays from the c program.
 * 
 * @author Kris Peterson
 */
public class ArrayToArrayInfoMap extends HashMap {
  
    //private HashSet _arraySet;
  public class ArrayInfo {
    public String name;
    public Operand var;
    public long startAddress;
    public long stopAddress;
    public int wordSize;
    public long arraySize; //ok, this is no longer in bits
    public boolean someStores;
    public int tryCnt = 0;
    private HashMap _storeTimes;
    private HashMap _loadTimes;

    public ArrayInfo(Operand varTmp, long arraySizeTmp, int wordSizeTmp) {

      var = varTmp;
      name = varTmp.toString();
      wordSize = wordSizeTmp;
      arraySize = arraySizeTmp;
      someStores = false;
      _storeTimes = new HashMap();
      _loadTimes = new HashMap();
      
    }

    public Operand getVar() {return var;}
    public int getWordSize() {return wordSize;}
    public long getArraySize() {return arraySize;}
    public long getStart() {return startAddress;}
    public void deAlloc() {
     startAddress = 0;
     stopAddress = 0;
    }
    public void setStart(long stAddy) {

      startAddress = stAddy;
      stopAddress = stAddy + arraySize;

    }
    public long getStop() {return stopAddress;}
    public void setCantUseROM() {someStores = true;}
    public boolean getCantUseROM() {return someStores;}
    public void setNodeLoadTime(BlockNode bNode, int cycle) {
      HashMap loadCounts = new HashMap();
      if(_loadTimes.containsKey(bNode))
        loadCounts = (HashMap)_loadTimes.get(bNode);
      else
        _loadTimes.put(bNode, loadCounts);
      Integer loadCntAtTime = new Integer(1);
      if(loadCounts.containsKey(new Integer(cycle)))
        loadCntAtTime = (Integer)loadCounts.get(new Integer(cycle));
      else
        loadCounts.put(new Integer(cycle), loadCntAtTime);
      loadCntAtTime = new Integer(loadCntAtTime.intValue()+1);
    }
    public void setNodeStoreTime(BlockNode bNode, int cycle) {
      HashMap storeCounts = new HashMap();
      if(_storeTimes.containsKey(bNode))
        storeCounts = (HashMap)_storeTimes.get(bNode);
      else
        _storeTimes.put(bNode, storeCounts);
      Integer storeCntAtTime = new Integer(1);
      if(storeCounts.containsKey(new Integer(cycle)))
        storeCntAtTime = (Integer)storeCounts.get(new Integer(cycle));
      else
        storeCounts.put(new Integer(cycle), storeCntAtTime);
      storeCntAtTime = new Integer(storeCntAtTime.intValue()+1);
    }
    
    public int getLoadCnt(BlockNode bNode, int cycle) {
      HashMap loadCounts = new HashMap();
      if(_loadTimes.containsKey(bNode))
        loadCounts = (HashMap)_loadTimes.get(bNode);
      else
        return 0;
      Integer loadCntAtTime = new Integer(0);
      if(loadCounts.containsKey(new Integer(cycle)))
        loadCntAtTime = (Integer)loadCounts.get(new Integer(cycle));
      else
        return 0;
      return loadCntAtTime.intValue();
    }
    public int getStoreCnt(BlockNode bNode, int cycle) {
      HashMap storeCounts = new HashMap();
      if(_storeTimes.containsKey(bNode))
        storeCounts = (HashMap)_storeTimes.get(bNode);
      else
        return 0;
      Integer storeCntAtTime = new Integer(0);
      if(storeCounts.containsKey(new Integer(cycle)))
        storeCntAtTime = (Integer)storeCounts.get(new Integer(cycle));
      else
        return 0;
      return storeCntAtTime.intValue();
    }
    
    public HashMap getLoadCntSched(BlockNode bNode) {
      if(_loadTimes.containsKey(bNode))
        return (HashMap)_loadTimes.get(bNode);
      else
        return new HashMap();
    }
    public HashMap getStoreCntSched(BlockNode bNode) {
      if(_storeTimes.containsKey(bNode))
        return (HashMap)_storeTimes.get(bNode);
      else
        return new HashMap();
    }
    public String toString() {
      if(var==null) return "nothing";
      return var.toString();
    }
  }
  
  
  private HashSet _remainingVars = new HashSet();
  public ArrayToArrayInfoMap(){super();}
  public ArrayInfo get(Operand array) {
    return (ArrayInfo)super.get(array);
  }
  public ArrayInfo get(Instruction inst, Operand array) {
    if(containsKey(array))
      return (ArrayInfo)super.get(array);
    else
      return makeArrayInfo(inst, array);
  }
  /*public ArrayInfo get(Instruction inst, Operand array) {
    if(containsKey(array))
      return (ArrayInfo)super.get(array);
    else {
      long width = 0;
      if(inst.type() instanceof PointerType) {
        PointerType ptype  = (PointerType)inst.type();
        Type typeTmp = ptype.getType();
        width = typeTmp.getWidth();
      }
      else {
        width = inst.type().getWidth();
      }
      System.out.println("array " + array);
      System.out.println("array.getType() " + array.getType());
      ArrayInfo arrInf = new ArrayInfo(array, width,
        			      (int)array.getType().getWidth());
      
      _remainingVars.add(arrInf);
      put(array, arrInf);
      return arrInf;
    }
  }*/
  public void resetRemainingArrays(){
    _remainingVars = new HashSet();
  }
  public void printRemainingArrays(){
    System.out.println("remaining arrays " + _remainingVars);
  }
  public void put(Operand array, ArrayInfo arrInfo) {
    super.put(array, arrInfo);
  }
  public ArrayInfo makeArrayInfo(Instruction inst, Operand array) { //, boolean someStores) {
    //ArrayInfo arrInfo = get(inst, array);
    if(containsKey(array)) { /*get(array).tryCnt=1;*/ return get(array);}
    //=================================================================
    //the code below was copied with modifications from AllocateArrays:
    int arrayBitSize = 0;
    Type type = inst.type();
    while(true) {
      if(type == null) {
        //I know I'm lazy not to throw an exception, but this works for now
        System.err.println("null type found");
	System.exit(1);
      } else if(type instanceof PointerType) {
	type = ((PointerType)type).getType();
	arrayBitSize = ((ArrayType)type).getWidth();
      } else if(type instanceof ArrayType) {
	type = ((ArrayType)type).getType();
      } else {
	break;
      }
    }
    int width = type.getWidth();
    //=================================================================
    ArrayInfo arrInfo = new ArrayInfo(array, arrayBitSize/width, width);
    //if(someStores) arrInfo.setCantUseROM();
    _remainingVars.add(arrInfo);
    put(array, arrInfo);
    return arrInfo;
  }
  public void removeArray(Operand array) {
    _remainingVars.remove(get(array));
  }
  public void addArrayBack(Operand array) {
    _remainingVars.add(get(array));
  }
  public ArrayList returnRemainingArrs() {
    ArrayList remainVarsList = new ArrayList(_remainingVars);
    sort(remainVarsList);
    return remainVarsList;
  }
  public int size() {
    return returnRemainingArrs().size();
  }
  public boolean arrayDoesntFit(Memory block, ArrayInfo array) {
    if(array.getArraySize() > block.getMemSizeLeft())
      return true;
    if((array.getCantUseROM())&&
        ((block instanceof Chip)&&(((Chip)block).typeCode == Chip.ROM_TYPE)))
      return true;
    return false;
  }

  /**
  This method saves all the loads and stores used by the design to be used
  to make an initial guess on where to allocate arrays.  It does it by assuming
  that all loads and stores can occur simultaneously (which is of course, 
  usually not possible, but the later preliminary scheduling step will find 
  that.)
  */
  public void loadDesign(BlockGraph bGraph) {
  
    for (Iterator vIt = new HashSet(bGraph.getAllNodes()).iterator(); 
         vIt.hasNext();) {
      BlockNode bNode = (BlockNode) vIt.next();
      ArrayList instructions = bNode.getInstructions();
      for (Iterator instIt = instructions.iterator(); 
    	   instIt.hasNext(); ) {
	Instruction inst = (Instruction)instIt.next();
	if(Getelementptr.conforms(inst)) {
          Operand array = Getelementptr.getArrayVar(inst);
          ArrayInfo arrInfo = makeArrayInfo(inst, array);
	}
      }
    }
    
    for (Iterator vIt = new HashSet(bGraph.getAllNodes()).iterator(); 
         vIt.hasNext();) {
      BlockNode bNode = (BlockNode) vIt.next();
      ArrayList instructions = bNode.getInstructions();
      for (Iterator instIt = instructions.iterator(); 
    	   instIt.hasNext(); ) {
	Instruction inst = (Instruction)instIt.next();
	if(ALoad.conforms(inst)) {
          Operand array = ALoad.getPrimalSource(inst);
          ArrayInfo arrInfo = get(inst, array);
	  arrInfo.setNodeLoadTime(bNode, 0);
	}
	else if(AStore.conforms(inst)) {
          Operand array = AStore.getPrimalDestination(inst);
          ArrayInfo arrInfo = get(inst, array);
          arrInfo.setCantUseROM();
	  arrInfo.setNodeStoreTime(bNode, 0);
	}
      }
    }
  }
  /**
  this method is used by AnalyzeHardware and CalculateII, because they already
  have a schedule, and either need to decide where best to place the arrays.
  CalculateII determines an ideal Initiation interval (II), assuming all instructions
  can be run in simultaneously (which is of course never possible) to use as a 
  seed II for the modulo scheduler.  
  */
  public void loadSchedule(BlockNode bNode) {
  
    ArrayList instructions = bNode.getInstructions();
    for (Iterator instIt = instructions.iterator(); 
    	 instIt.hasNext(); ) {
      Instruction inst = (Instruction)instIt.next();
      if(Getelementptr.conforms(inst)) {
        Operand array = Getelementptr.getArrayVar(inst);
        ArrayInfo arrInfo = makeArrayInfo(inst, array);
      }
    }
    for (Iterator instIt = instructions.iterator(); 
    	 instIt.hasNext(); ) {
      Instruction inst = (Instruction)instIt.next();
      if(ALoad.conforms(inst)) {
        Operand array = ALoad.getPrimalSource(inst);
        ArrayInfo arrInfo = get(inst, array);
	arrInfo.setNodeLoadTime(bNode, inst.getExecClkCnt());
      }
      else if(AStore.conforms(inst)) {
        Operand array = AStore.getPrimalDestination(inst);
        ArrayInfo arrInfo = get(inst, array);
        arrInfo.setCantUseROM();
	arrInfo.setNodeStoreTime(bNode, inst.getExecClkCnt());
      }
    }
  }
  public int getLoadCnt(HashSet arraySet, BlockNode bNode, int time) {
  
    int loadCnt = 0;
    for (Iterator arrIt = arraySet.iterator(); 
    	 arrIt.hasNext(); ) {
      ArrayInfo array = (ArrayInfo)arrIt.next();
      loadCnt += array.getLoadCnt(bNode, time);
    }    
    return loadCnt;
  }
  public int getStoreCnt(HashSet arraySet, BlockNode bNode, int time) {
  
    int storeCnt = 0;
    for (Iterator arrIt = arraySet.iterator(); 
    	 arrIt.hasNext(); ) {
      ArrayInfo array = (ArrayInfo)arrIt.next();
      storeCnt += array.getStoreCnt(bNode, time);
    }    
    return storeCnt;
  }
  /*public HashMap getLoadCntSched(BlockNode bNode) {
  
    return array.getLoadCntSched(bNode);
  }
  public HashMap getStoreCntSched(BlockNode bNode) {
  
    return array.getStoreCntSched(bNode);
  }*/
  /** sort operands based on their widths, operators based on their latencies or 
   *  size, memory blocks based on the number of data ports, and ArrayLists 
   *  based on their size.
   * 
   * @param o_list list to be sorted
   */
  private void sort(ArrayList o_list) {
    class DoubleCompare implements Comparator {      
      /** fancy compare for sort
       * 
       * @param o1 1st item
       * @param o2 2nd item
       * @return bigger, smaller, same
       */
      public int compare(Object o1, Object o2) {        
        if (o1 instanceof ArrayInfo             
         && o2 instanceof ArrayInfo) {          
          ArrayInfo p1 = (ArrayInfo)o1;          
          ArrayInfo p2 = (ArrayInfo)o2;          
       //System.out.println("p1 " + p1);
      //System.out.println("p2 " + p2);
         if (p1.arraySize > p2.arraySize) {
            return -1;          
          } else if (p1.arraySize < p2.arraySize) {
            return 1;         
          } else {            
            return 0;          
          }        
        } 
	else {          
          throw new ClassCastException("Not ArrayInfo");        
        }      
      }    
    }        
    Collections.sort(o_list, new DoubleCompare());  
  }


} //end ArrayToArrayInfoMap

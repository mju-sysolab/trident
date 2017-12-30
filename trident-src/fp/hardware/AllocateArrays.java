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
import fp.hwdesc.MemoryBlock;
import fp.GlobalOptions;

/**
 * This class allocates each used array memory space. Information is used 
 * from the analyzeHardwareConstraints class (?) about which arrays belong 
 * to which memories. This class proceeds by mapping the most frequently 
 * used arrays to lowest spaces in memory, which might reduce the amount of 
 * processing required for the most used arrays.
 */
public class AllocateArrays {

  /*
    Array information is stored in the memory blocks as 
    AllocateArray.ArrayInfos.

  */


  public static final int LOOP_USE_FACTOR = 50;

  private HashSet memoryBlocks;
  private HashMap useMap;

  public AllocateArrays() {
    memoryBlocks = new HashSet();
    useMap = new HashMap();
  }

  // Find each GEP instruction and allocate the associated array...
  public boolean allocateArrays(BlockGraph graph) {
    ChipDef chipInfo = GlobalOptions.chipDef;
    HashMap arrayInfos = new HashMap();

    System.out.println("====================================================");
    // For each block...
    for(Iterator bIt = graph.getAllNodes().iterator(); bIt.hasNext(); ) {
      BlockNode bn = (BlockNode) bIt.next();
      // For each instruction...
      for(Iterator iIt = bn.getInstructions().iterator(); iIt.hasNext(); ) {
	Instruction inst = (Instruction) iIt.next();
	if(Getelementptr.conforms(inst)) {
	  String arrayName = Getelementptr.getArrayVar(inst).getFullName();
	  updateArrayUsage(bn, arrayName);
	  arrayInfos.put(arrayName, getArrayInfo(bn, inst, chipInfo));
	}
      }
    }
    placeArraysInMemory(arrayInfos);

    System.out.println("====================================================");
    return true;
  }

  /**
   * This method gathers information about each array, which will later be 
   * used for placing the arrays in the memory space.
   */
  private ArrayInfo getArrayInfo(BlockNode bn, Instruction inst, 
				 ChipDef chipInfo) {

    // Check for unhandled "struct" case of getelementptr...
    if(((IntConstantOperand)Getelementptr.getValOperand(inst,0)).getValue()!=0)
      throw new AllocateArraysException("Unhandled struct case of GEP inst");

    String arrayName = Getelementptr.getArrayVar(inst).getFullName();
    // Get the type of the array elements.
    int arrayBitSize = 0;
    LinkedList dimInfo = new LinkedList();
    Type type = inst.type();
    while(true) {
      if(type == null) {
	throw new AllocateArraysException("Converting GEP: null type found.");
      } else if(type instanceof PointerType) {
	type = ((PointerType)type).getType();
	arrayBitSize = ((ArrayType)type).getWidth();
      } else if(type instanceof ArrayType) {
	dimInfo.addLast(new Integer(((ArrayType)type).getWidth()));
	type = ((ArrayType)type).getType();
      } else {
	break;
      }
    }

    // If there's no size associated with this array, then throw exception...
    if(arrayBitSize == 0) 
      throw new AllocateArraysException("No size associated with array!");
    
    // Get the bus and memory info for the array.
    Memory mb = chipInfo.findMemoryBlock(Getelementptr.getArrayVar(inst));
    memoryBlocks.add(mb);

    // Get the type of an atomic array element.
    Type aType = type;

    int prev = aType.getWidth();
    for(ListIterator aIt = dimInfo.listIterator(dimInfo.size()); 
	aIt.hasPrevious(); ) {
      int dim = (int)((Integer)aIt.previous()).intValue();
      int newDim = (int)(dim/prev);
      prev = dim;
      aIt.set(new Integer(newDim));
    }

    // Set the ArrayInfo for this array; the address and usage will be set 
    // later...in placeArraysInMemory().


    System.out.println(" new array info "+arrayName+" arrayBitSize "+(arrayBitSize)+"/ aType.getWidth() "+aType.getWidth());
    System.out.println(" count ... "+(arrayBitSize/aType.getWidth()));

    //int addressable_size = mb.getAddressableSize();
    // addressable size is an external problem...
    //int addressable_size = 8;

     ArrayInfo ai = new ArrayInfo(arrayName, dimInfo, 
				  arrayBitSize/aType.getWidth(), 
				  aType);
     return ai;
  }

  /**
   * This method allocates memory space to the specified array.
   */
  private void placeArraysInMemory(HashMap arrayInfos) {
    // for each memory...
    // add all arrayInfos that belong to this memory to an arraylist
    // sort the arraylist based on amount of usage
    // put the most used arrayinfos closest to address 0
    // remember to check for "packed" words via getVarGroupingsPtr()
    // use saveVarLoc() in MemoryBlock???

    // For each MemoryBlock...
    for(Iterator mIt = memoryBlocks.iterator(); mIt.hasNext(); ) {
      Memory mb = (Memory) mIt.next();
      HashSet groups = mb.getVarGroupingsPtr();
      ArrayList gList = new ArrayList();

      // For each group of arrays in the memory block...
 System.out.println("groups " + groups);
     for(Iterator gIt1 = groups.iterator(); gIt1.hasNext(); ) {
	HashSet group = (HashSet) gIt1.next();
	ArrayList aList = new ArrayList();
	gList.add(aList);

	// For each array in the group...
	for(Iterator aIt = group.iterator(); aIt.hasNext(); ) {
	  Operand array = (Operand) aIt.next();
	  String aName = array.getFullName();
	  ArrayInfo aInfo = (ArrayInfo) arrayInfos.get(aName);

	  // Get an estimate of the frequency of use of the array.
	  int usage = getArrayUsage(aName);
	  aInfo.useCnt = usage;
System.out.println("array " + array);
System.out.println("aInfo " + aInfo);
	  aList.add(aInfo);	
System.out.println("aList " + aList);
	}
      }
      sort(gList);  // Sort from largest to smallest use frequencies of arrays.
      int address = 0;
      // For each group (in decreasing frequency of use)...
      for(Iterator gIt2 = gList.iterator(); gIt2.hasNext(); ) {
	// Allocate this group the lowest next available addr space in memory.
	ArrayList aList = (ArrayList) gIt2.next();
	System.out.println("address="+address+",  "+aList);

	// CHANGE: should this utilize MemoryBlock's saveVarLoc()??
	int groupArraySize = 0;
	// For each arrayinfo in this group..
	for(Iterator aIt = aList.iterator(); aIt.hasNext(); ) {
	  // The address space for this array is the same as all in this group
	  ArrayInfo ai = (ArrayInfo) aIt.next();
	  ai.addr = address;
	  mb.addArrayInfo(ai);
	  if(ai.totalWords > groupArraySize)
	    groupArraySize = ai.totalWords;
	}
	address += groupArraySize;
      }
      System.out.println();
    }
  }

  // CHANGE: how much analysis should be done here????
  /**
   * This method tries to estimate how often an array is accessed.  Obviously, 
   * if the array is accessed in a loop, then it has a much better chance of 
   * having a greater frequency of use than an array that isn't accessed 
   * inside a loop.  
   */
  private int getArrayUsage(String arrayName) {
    UsePair pair = (UsePair) useMap.get(arrayName);
    int useFrequency = pair.staticUse + (pair.loopUse*LOOP_USE_FACTOR);
    System.err.println("useFrequency " + useFrequency);
    return useFrequency;
  }

  /**
   * This method updates the useMap private variable in order to estimate 
   * the frequency of array usage...
   */
  private void updateArrayUsage(BlockNode bn, String arrayName) {
    UsePair pair = null;
    if(useMap.containsKey(arrayName)) 
      pair = (UsePair) useMap.get(arrayName);
    else
      pair = new UsePair();
    System.err.println("arrayName " + arrayName);
    pair.staticUse++;
    System.err.println("pair.staticUse " + pair.staticUse);
    if(isLoopBlock(bn)) // Is this loop test good enough???
      pair.loopUse++;
    useMap.put(arrayName, pair);
  }

  /**
   * This method checks to see if this blocknode has a backedge to itself.
   */
  public boolean isLoopBlock(BlockNode bn) {
    Set inEdges = bn.getInEdges();
    Set outEdges = bn.getOutEdges();
    for (Iterator its = ((Set)inEdges).iterator(); 
      its.hasNext(); ) {
      BlockEdge edge = (BlockEdge)its.next();
      if(edge != null) {
        if(outEdges.contains(edge))
          return true;
      }
    }
    return false;
  }
  

  public String name() {
    return "AllocateArrays"; 
  }

  private class AllocateArraysException extends RuntimeException {
    public AllocateArraysException() {
      super();
    }
    public AllocateArraysException(String message) {
      super("\n"+message);
    }
  }

  /**
   * This is a basic class that holds info about the uses of each 
   * array. StaticUses are the number of uses found in the CFG, while 
   * loopUses are only those uses that are contained inside a loop.
   */
  public class UsePair {
    UsePair() {
      staticUse = 0;
      loopUse = 0;
    }
    protected int staticUse;
    protected int loopUse;
    public String toString() {
      return "[ "+staticUse+" , "+loopUse+" ]";
    }
  }


  /**
   * This class stores information about arrays that were found in the 
   * GEP instructions in the BlockGraph.
   */
  public class ArrayInfo {
    ArrayInfo(String aName, LinkedList dimInformation, int nWords, 
	      Type aType) {
      name = aName;
      dimInfo = (LinkedList) dimInformation.clone();
      totalWords = nWords;
      type = aType;
    }

    public String name;
    public int addr;
    public LinkedList dimInfo;
    public int totalWords;
    public Type type;
    public int useCnt;

    public String toString() { return name + "["+totalWords+"]"; }
    public String getDefinition() { return name+dimInfo; }
  }

  /**
   * Sort a list of lists based on the maximum "use count" of the elements 
   * in each sublist.
   */
  private void sort(ArrayList a) {
    class ArrayInfoListCompare implements Comparator {
      public int compare(Object o1, Object o2) {
	if(o1 instanceof ArrayList &&
	   o2 instanceof ArrayList) {
	      System.err.println("o1 " + o1);
	      System.err.println("o2 " + o2);
	  ArrayInfo ai1 = max((ArrayList) o1);
	  ArrayInfo ai2 = max((ArrayList) o2);
	  if(ai1.useCnt > ai2.useCnt) 
	    return -1;
	  else if(ai1.useCnt < ai2.useCnt) 
	    return 1;
	  else
	    return 0;
	} else {
	  System.out.println("ERROR");
	  System.out.println("o1: "+o1);
	  System.out.println("o2: "+o2);
	  throw new ClassCastException("Not a ArrayList!");
	}
      }

      /**
       * Find the max value of the arrayInfo "use counts" in the arrayinfo.
       */
      private ArrayInfo max(ArrayList l) {
	class ArrayInfoCompare implements Comparator {
	  public int compare(Object o1, Object o2) {
	    if(o1 instanceof ArrayInfo &&
	       o2 instanceof ArrayInfo) {
	      ArrayInfo ai1 = (ArrayInfo)o1, ai2 = (ArrayInfo)o2;
	      System.err.println("ai1 " + ai1);
	      System.err.println("ai2 " + ai2);
	      System.err.println("ai1.useCnt " + ai1.useCnt);
	      System.err.println("ai2.useCnt " + ai2.useCnt);
	      if(ai1.useCnt > ai2.useCnt) 
		return 1;
	      else if(ai1.useCnt < ai2.useCnt)
		return -1;
	      else return 0;
	    } else {
	      throw new ClassCastException("Not a ArrayInfo!");
	    }
	  }
	} // end of class ArrayInfoCompare
	return (ArrayInfo)Collections.max(l, new ArrayInfoCompare());
      } // end of max()
    } // end of class ArrayInfoListCompare
    Collections.sort(a, new ArrayInfoListCompare());
  } // end of sort()
}

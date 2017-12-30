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


package fp.hwdesc.memClasses;

import fp.flowgraph.Operand;
import fp.flowgraph.BlockGraph;
import fp.flowgraph.BlockNode;
import fp.flowgraph.Instruction;
import fp.flowgraph.Getelementptr;
import fp.flowgraph.ALoad;
import fp.flowgraph.AStore;
import java.util.*;

/**

class IndexMatch

The purpose of IndexMatch is to test if two arrays can be packed together
in the same address space in a memory.  This is possible if for every load
or store to one memory, there is an operation of the same kind to the other
array at the same address offset.  For example, these arrays could be packed:

z = A[i];
y = B[i];

(two Aloads at the same address offset, i)

but these two arrays can not be packed:

A[i] = B[i]

(The address offset is identical, but one instruction is an ALoad and the
other an AStore)

Also these two arrays can not be packed:

x = A[i]
y = B[i+1]

(There are two ALoads, but to two different address offsets.)

This is tested, by comparing the memory address offsets used by the GEP
instructions and ensuring that they are exactly identical including the order.
Also, if two instructions have matching address offsets, the address 
calculated by GEP must be compared with the ALoads or AStores that these
addresses correspond to, to ensure that the two arrays are being accessed 
using the same instruction.  This test is actually performed first since it
is quicker.  

*/
public class IndexMatch extends HashMap {

  /**
  class IndexList
  
  -this class was placed in IndexMatch because it is only used by IndexMatch
  
  This class stores the list of operands used in the GEP instruction.
  
  It also has a method for comparing two lists of index operands used by the
  GEP instruction and deciding if they match.
  
  -key = array operand
  -value = IndexSet object, which the list of address offsets and if an
	   instructions is an AStore orAload
  
  */
  private class IndexList extends ArrayList {
  
    /**
    This variable saves whether this address offset for this array
    was used for an ALoad or AStore
    */
    public boolean isALoad;
    
    public IndexList(){
      super();
    }
    public IndexList(boolean isAld){
      super();
      isALoad = isAld;
    }
    
    public void setIsALoad(boolean isAld){
      isALoad = isAld;
    }
    
   public void add(ArrayList offsetList){
      super.addAll(offsetList);
    }
    
    //an array can be accessed several times, but this checks that for every
    //read or write to an array, there is an identical read or write to the
    //second.
    public boolean match(IndexList arr2Set) {
    
      //check that the list of operands given to GEP is the same length
      //for both arrays.  If they aren't, then these arrays cannot have the
      //same access address offset
      if(size() != arr2Set.size())
        return false;
      //check that both accesses to the two different arrays are the same 
      //instruction (i.e. that both ALoads or both AStores, but not one of 
      //each)  
      if(isALoad != arr2Set.isALoad)
        return false;
        
      //go through the list of operands used by each array's GEP instruction
      //to make sure they are calculating the exact same address offset
      for(int n=0; n<size(); n++) {
        //if((((Operand)super.get(n)).toString().compareTo(((Operand)arr2Set.get(n)).toString()))!=0)
        //System.out.println("super.get(n) " + super.get(n));
        //System.out.println("arr2Set.get(n) " + arr2Set.get(n));
	if(super.get(n) != arr2Set.get(n))
          return false;
      }
        //System.out.println("indexl list match");
      return true;
    
    }
	
  }// end IndexList
  
  /**
  class MatchSaver
  
  -this class was placed in IndexMatch because it is only used by IndexMatch
  
  This class saves the results of calculations of whether two arrays are 
  packable so that the same calculation does not need to be performed twice.
  
  */
  private class MatchSaver extends HashMap {
  
    //key= 1st array
    //value = hashMap
	   //for this HashMap:
               //key = 2nd array
               //value = boolean of if there is a match
    public MatchSaver() {
      super();
    }
    
    
    public Boolean isKnownMatch(Operand arr1, Operand arr2) {
    
      HashMap matchMap = (HashMap)super.get(arr1);
      if(matchMap == null) return null;
      //I use Booleans, instead of boolean, because that gives 3 possible
      //outputs, true, false, and null--if null is outputed, it means a 
      //calculation for this pair of arrays must still be calculated
      Boolean isMatch = (Boolean)matchMap.get(arr2);
      return isMatch;
    
    }
  
    public void saveIsMatch(Operand arr1, Operand arr2, boolean isMatch) {
    
      HashMap arr1ToArr2ToBool;
      if(super.containsKey(arr1)) {
        arr1ToArr2ToBool = (HashMap)super.get(arr1);  
      }
      else {
        arr1ToArr2ToBool = new HashMap();
        super.put(arr1, arr1ToArr2ToBool);
      }

      arr1ToArr2ToBool.put(arr2, new Boolean(isMatch));
    
    }
  
  } //end MatchSaver
  
  private static HashMap _ptrToIndexSetMap = new HashMap();
  private static MatchSaver _saveCalctdmatches;
  
  public IndexMatch(){
    super();
    _saveCalctdmatches = new MatchSaver();
  }
  /**
  this method examines the GEP, ALoad, and AStore instructions saving
  what kind of instructions (ALoad or AStore) as well as the array offset
  addresses
  */
  public void readIndex(BlockGraph graph) {
    for (Iterator vIt = new HashSet(graph.getAllNodes()).iterator(); 
	 vIt.hasNext();) {
      BlockNode node = (BlockNode) vIt.next();
      for (Iterator it = node.getInstructions().iterator(); 
	   it.hasNext(); ) {
	Instruction instr = (Instruction)it.next();
        if(Getelementptr.conforms(instr)) {
          Operand arr = Getelementptr.getArrayVar(instr);
          Operand ptr = Getelementptr.getResult(instr);
          ArrayList paramList = new ArrayList();
	  int total = instr.getNumberOfOperands();
          for(int i = 0; i <= total/2 - 2; i++) {
	    Operand ind = (Operand)Getelementptr.getValOperand(instr, i);
            paramList.add(ind);
          }
          save(arr, paramList, ptr);
        }
        else if(ALoad.conforms(instr)) {
          Operand arr = ALoad.getPrimalSource(instr);
          Operand ptr = ALoad.getAddrSource(instr);
          save(arr, ptr, true);
        }
        else if(AStore.conforms(instr)) {
          Operand arr = AStore.getPrimalDestination(instr);
          Operand ptr = AStore.getAddrDestination(instr);
          save(arr, ptr, false);
        }
      }   
    }
  }  
  public void save(Operand arr, ArrayList paramList, Operand ptr) {
    IndexList saveIndexLists = null;
    HashSet indexListSet = null;
    if(super.containsKey(arr))
      indexListSet =  (HashSet)super.get(arr);
    else {
      indexListSet =  new HashSet();	    
      super.put(arr, indexListSet);
    }


    if(_ptrToIndexSetMap.containsKey(ptr)) {
      saveIndexLists =  (IndexList)_ptrToIndexSetMap.get(ptr);      
    }
    else {
      saveIndexLists =  new IndexList();    
    }
    saveIndexLists.addAll(paramList);
    indexListSet.add(saveIndexLists);
    _ptrToIndexSetMap.put(ptr, saveIndexLists);
  }

  public void save(Operand arr, Operand ptr, boolean isALoad) {
    IndexList saveIndexLists = null;
    if(_ptrToIndexSetMap.containsKey(ptr)) {
      saveIndexLists =  (IndexList)_ptrToIndexSetMap.get(ptr);
      saveIndexLists.setIsALoad(isALoad);
    }
    else {
      saveIndexLists =  new IndexList(isALoad);
      _ptrToIndexSetMap.put(ptr, saveIndexLists);
      HashSet indexListSet = null;
      if(super.containsKey(arr))
	indexListSet =  (HashSet)super.get(arr);
      else {
	indexListSet =  new HashSet();      
      }
      indexListSet.add(saveIndexLists);
      super.put(arr, indexListSet);
    }
  }

  /**
  this method tests if two arrays can be packed together
  */
  public boolean matches(Operand arr1, Operand arr2) {
    //so we don't have to calculate if the indexes match, we save
    //the previous calculation
    Boolean isMatch = _saveCalctdmatches.isKnownMatch(arr1, arr2);
    if(isMatch != null) return isMatch.booleanValue();

    HashSet arr1IndexSets = (HashSet)super.get(arr1);
    HashSet arr2IndexSets = (HashSet)super.get(arr2);
    
/*System.out.println("##############################################");
System.out.println("match test between:");
System.out.println("arr1 " + arr1);
System.out.println("arr2 " + arr2);*/
    for (Iterator arr1It = arr1IndexSets.iterator(); 
	 arr1It.hasNext(); ) {
      IndexList arr1IndexList = (IndexList)arr1It.next();
      boolean matchFound = false;
      for (Iterator arr2It = arr2IndexSets.iterator(); 
	   arr2It.hasNext() && matchFound == false; ) {
	IndexList arr2IndexList = (IndexList)arr2It.next();
	if(arr1IndexList.match(arr2IndexList))
	  matchFound = true;
      }
      if(!matchFound) {
	_saveCalctdmatches.saveIsMatch(arr1, arr2, false);
	return false;
      }
    }
    _saveCalctdmatches.saveIsMatch(arr1, arr2, true);
/*System.out.println("match found");
System.out.println("##############################################");*/
    return true;
  }
  
  public boolean matches(Operand arr1, HashSet otherArrays) {
    //if(otherArrays.contains(arr1)) return false;
System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&");
    for (Iterator arrIt = otherArrays.iterator(); 
	 arrIt.hasNext(); ) {
      Operand array = (Operand)arrIt.next();
System.out.println("arr1 " + arr1);
System.out.println("array " + array);
      //if(arr1 == array) continue;
      if(matches(arr1, array))
        return true;
    }
    return false;
  }
}  //end IndexMatch

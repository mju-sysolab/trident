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
import fp.hardware.AllocateArrays;
import fp.hardware.ArrayToArrayInfoMap;
import fp.flowgraph.Operator;
import fp.flowgraph.Operand;
import fp.flowgraph.BlockGraph;
import fp.flowgraph.BlockNode;
import fp.hwdesc.memClasses.IndexMatch;

public interface Memory {

  public static IndexMatch matchTester = new IndexMatch();

  public void addArrayInfo(AllocateArrays.ArrayInfo a);
  public void removeArrayInfo(AllocateArrays.ArrayInfo a);
  public AllocateArrays.ArrayInfo getArrayInfo(String aName);
  public HashMap getArrayInfos();
  public String getName();
  public void setName(String name);
  public String getChipName();
  public void setChipName(String cName);
  public int getWidth();
  public void setWidth(int cWidth);
  public int getDepth();
  public void setDepth(int cDepth);
  public void setAddressOffset(long addr);
  public long getAddressOffset();
  public int getVarGroupingsSize();
  public HashSet getVarGroupingsPtr(); 
  public void addToVarGroupingsPtr(HashSet arrSet); 
  public void saveALoadOp(Operator aload);
  public Operator getALoadOp();
  public void saveAStoreOp(Operator astore);
  public Operator getAStoreOp();
  public long getMemSizeLeft();
  public void setMemSizeLeft();
  public void subSpace(long diff);
  public void addSpace(long diff);
  public int getNumOfWriteBus();
  public int getNumOfReadBus();
  public boolean getonlyOneAddy();
  public void displayMemContents(ArrayToArrayInfoMap arrToArrInf);
  public int findSlowestReadLat();
  public int findSlowestWriteLat();
  public boolean saveToPackedArrCollect(ArrayToArrayInfoMap.ArrayInfo array,
                                        ArrayToArrayInfoMap arrToArrInf); 
  public boolean noPackdArrAtLoc(ArrayToArrayInfoMap.ArrayInfo array, 
                                 ArrayToArrayInfoMap arrToArrInf); 
  public void resetPorts();
  public boolean allocateArray(ArrayToArrayInfoMap.ArrayInfo array, 
                               ArrayToArrayInfoMap arrToArrInf); 
  public void deAllocateArray(ArrayToArrayInfoMap.ArrayInfo array);
  public void loadIndexes(BlockGraph designBGraph);
  public int trueCost(BlockNode bNode, ArrayToArrayInfoMap arrToArrInf);
  public int cost(BlockNode bNode, ArrayToArrayInfoMap arrToArrInf);
  public int addLoadTest(BlockNode bNode, int time, Operand array);
  public int addLoadTestCnt(BlockNode bNode, int time, Operand array);
  public int addLoad(BlockNode bNode, int time, Operand array);
  public int addLoadCnt(BlockNode bNode, int time, Operand array);
  public int subLoad(BlockNode bNode, int time, Operand array);
  public int subLoadCnt(BlockNode bNode, int time, Operand array);
  public int addStoreTest(BlockNode bNode, int time, Operand array);
  public int addStoreTestCnt(BlockNode bNode, int time, Operand array);
  public int addStore(BlockNode bNode, int time, Operand array);
  public int addStoreCnt(BlockNode bNode, int time, Operand array);
  public int subStore(BlockNode bNode, int time, Operand array);
  public int subStoreCnt(BlockNode bNode, int time, Operand array);
}

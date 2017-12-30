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
import fp.flowgraph.BlockNode;

public class PortUseCnt extends HashMap {
  /*private class Time {
    private int _time;
    public Time(int time) {_time=time;}
    public int get() {return _time;}
  }
  private class Count {
    private int _cnt;
    public Count(int cnt) {_cnt=cnt;}
    public int get() {return _cnt;}
  }*/
  
  
  //key=time
  //value=count
  
  public PortUseCnt(){super();}
  
  public void put(BlockNode node, int time, int cnt) {
    Integer tm = new Integer(time);
    Integer cn = new Integer(cnt);
    if(containsKey(node)) {
      HashMap node2Time = ((HashMap)super.get(node));
      node2Time.put(tm, cn);
    }
    else {
      HashMap node2Time = new HashMap();
      node2Time.put(tm, cn);
      super.put(node, node2Time);
    }
  }
  
  public int get(BlockNode node, int time) {
    if(containsKey(node)) {
      HashMap node2Time = ((HashMap)super.get(node));
      Integer tm = new Integer(time);
      if(node2Time.containsKey(tm))
	return ((Integer)node2Time.get(tm)).intValue();
      else
	return 0;
      
    }
    else
      return 0;
  }
  
  /*public int get(Integer time) {
    return ((Integer)super.get(time)).intValue();
  }*/
  
  public void addUses(BlockNode node, int cnt) {
    addUses(node, 0, cnt);
  }
  
  public void addUse(BlockNode node, int time) {
    addUses(node, time, 1);
  }
  
  public void addUses(BlockNode node, int time, int cnt) {
    int oldCnt = get(node, time);
    /*System.out.println("=============================================");
    System.out.println("node " + node);
    System.out.println("time " + time);
    System.out.println("cnt " + cnt);
    System.out.println("oldCnt " + oldCnt);*/
    put(node, time, oldCnt + cnt);
    //System.out.println("get(node, time) " + get(node, time));
    //System.out.println("=============================================");
  }
  
  public void subUses(BlockNode node, int cnt) {
    subUses(node, 0, cnt);
  }
  
  public void subUse(BlockNode node, int time) {
    subUses(node, time, 1);
  }
  
  public void subUses(BlockNode node, int time, int cnt) {
    int oldCnt = get(node, time);
    /*System.out.println("----------------------------------------------");
    System.out.println("node " + node);
    System.out.println("time " + time);
    System.out.println("cnt " + cnt);
    System.out.println("oldCnt " + oldCnt);*/
    //if(oldCnt>0)
      put(node, time, oldCnt - cnt);
    //System.out.println("get(node, time) " + get(node, time));
    //System.out.println("----------------------------------------------");
  }
  
  public int testPortUseCntGen(BlockNode node, int cnt) {
    return testPortUseCnt(node, 0, cnt);
  }
  
  public int testPortUse(BlockNode node, int time) {
    return testPortUseCnt(node, time, 1);
  }

  public int testPortUseCnt(BlockNode node, int time, int cnt) {
    addUses(node, time, cnt);
    int cost = getLoad(node, time);
    subUses(node, time, cnt);
    return cost;
  }

  public int getLoad(BlockNode node, int time) {
  //    if(this.isEmpty()) return 9999;
    return get(node, time);
  }

  public int getMaxPortLoad() {
    int fullCost = 0;
    for (Iterator nIt = values().iterator(); 
    	 nIt.hasNext(); ) {
      HashMap node2Time = (HashMap)nIt.next();
      for (Iterator costIt = node2Time.keySet().iterator(); 
    	   costIt.hasNext(); ) {
	Integer time = (Integer)costIt.next();
	fullCost = Math.max(((Integer)node2Time.get(time)).intValue(), fullCost);
      }
    }
    return fullCost;
  }

}

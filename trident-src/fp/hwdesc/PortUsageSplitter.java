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

public class PortUsageSplitter {
  private ArrayList _port;
  public static final HashSet DATA_READ = new HashSet();
  public static final HashSet DATA_WRITE = new HashSet();
  public static final HashSet ADDRESS_READ = new HashSet();
  public static final HashSet ADDRESS_WRITE = new HashSet();
  public static final HashSet RWPORTS = new HashSet();
  public static final HashSet NONRWPORTS = new HashSet();
  public static final boolean BEST = true;
  public static final boolean WORST = false;
  public static final boolean TRUECOST = true;
  public static final boolean NORMCOST = false;
  public static final boolean LOAD = true;
  public static final boolean STORE = false;
  
  public PortUsageSplitter(ArrayList portList) {
    _port = portList;
    DATA_READ.add(new Integer(Port.DATA_READ_TYPE));
    DATA_READ.add(new Integer(Port.DATA_RW_TYPE));
    DATA_WRITE.add(new Integer(Port.DATA_WRITE_TYPE));
    DATA_WRITE.add(new Integer(Port.DATA_RW_TYPE));
    ADDRESS_READ.add(new Integer(Port.ADDRESS_READ_TYPE));
    ADDRESS_READ.add(new Integer(Port.ADDRESS_RW_TYPE));
    ADDRESS_WRITE.add(new Integer(Port.ADDRESS_WRITE_TYPE));
    ADDRESS_WRITE.add(new Integer(Port.ADDRESS_RW_TYPE));
    RWPORTS.add(new Integer(Port.DATA_RW_TYPE));
    RWPORTS.add(new Integer(Port.ADDRESS_RW_TYPE));
    NONRWPORTS.add(new Integer(Port.DATA_READ_TYPE));
    NONRWPORTS.add(new Integer(Port.DATA_WRITE_TYPE));
    NONRWPORTS.add(new Integer(Port.ADDRESS_READ_TYPE));
    NONRWPORTS.add(new Integer(Port.ADDRESS_WRITE_TYPE));
  }
  
  /*public Port findBestAddyLoadPort(int time) {
    return findLoadAddyPort(time, true);
  }
  
  public Port findBestDataLoadPort(int time) {
    return findLoadDataPort(time, true);
  }
  
  public Port findWorstAddyLoadPort(int time) {
    return findLoadAddyPort(time, false);
  }
  
  public Port findWorstDataLoadPort(int time) {
    return findLoadDataPort(time, false);
  }
  
  
  public Port findLoadAddyPort(int time, boolean findBest) {
    Port bestPort = null;
    Port port = null;
    int cost = 0;
    for (Iterator portIt = _port.iterator(); 
         portIt.hasNext(); ) {
      port = (Port)portIt.next();
      if(bestPort == null) bestPort = port;
      if((port.typeCode == Port.ADDRESS_READ_TYPE)||
    	 (port.typeCode == Port.ADDRESS_RW_TYPE)) { 
        int portCost = port.getPortUseCnter().testPortUse(time);
        if((((cost==0)||(cost > portCost) || 
    	    ((cost == portCost)&&(port.typeCode == Port.ADDRESS_READ_TYPE)))
	       && findBest) || //find best port
	   (((cost==0)||(cost < portCost) || 
    	    ((cost == portCost)&&(port.typeCode == Port.ADDRESS_RW_TYPE)))
	       && !findBest)) {  //find worse port

    	  cost = portCost;
          bestPort = port;

        }
      }
    }
    return bestPort;
  }
  
  public Port findLoadDataPort(int time, boolean findBest) {
    Port bestPort = null;
    Port port = null;
    int cost = 0;
    for (Iterator portIt = _port.iterator(); 
         portIt.hasNext(); ) {
      port = (Port)portIt.next();
      if(bestPort == null) bestPort = port;
      if((port.typeCode == Port.DATA_READ_TYPE)||
    	 (port.typeCode == Port.DATA_RW_TYPE)) { 
        int portCost = port.getPortUseCnter().testPortUse(time);
        if((((cost==0)||(cost > portCost) || 
    	    ((cost == portCost)&&(port.typeCode == Port.DATA_READ_TYPE)))
	       && findBest) || //find best port
	   (((cost==0)||(cost < portCost) || 
    	    ((cost == portCost)&&(port.typeCode == Port.DATA_RW_TYPE)))
	       && !findBest)) {  //find worse port

    	  cost = portCost;
          bestPort = port;

        }
      }
    }
    return bestPort;
  }*/
  
  public Port findPort(BlockNode bNode, int time, HashSet portType, boolean findBest) {
    Port bestPort = null;
    Port port = null;
    int cost = -9999;
    for (Iterator portIt = _port.iterator(); 
         portIt.hasNext(); ) {
      port = (Port)portIt.next();
      if(bestPort == null) {bestPort = port; continue;}
      if(portType.contains(new Integer(port.typeCode))) { 
        int portCost = port.getPortUseCnter().testPortUse(bNode, time);
        HashSet rwPorts = null;
	if(!findBest) {portCost = -portCost; rwPorts = RWPORTS;}
	else {rwPorts = NONRWPORTS;}
	if((cost > portCost) || 
    	   ((cost == portCost)&&
	    (rwPorts.contains(new Integer(port.typeCode))))) {  

    	  cost = portCost;
          bestPort = port;

        }
      }
    }
    return bestPort;
  }
  
      /**
      for true cost I want to take into account the read
      latency with the cost.  That is, if there are two reads at the same
      time on a port, they will be staggered by the scheduler--and I'm 
      assuming that the staggering will be only 1 cycle)--and then the
      cost is number of reads minus one plus the read latency.  That is
      graphically, if we have these read attempts at the same cycle:
      
       |_____________|
       |_____________|
       |_____________|
      
      assuming the scheduler staggers each of these reads one cycle we'll 
      get:
      
       |_____________|
        |_____________|
         |_____________|
      
      and then the cost is the number of reads (3) minus 1 plus the latency.
      Even in this method, where we are subtracting a load, this holds true
      because the number of loads has just been reduced by one and now 
      the cost is (2-1) + read_latency
      */
  
  public int addLoad(BlockNode bNode, int time, boolean trueCost) {
    Port bestDataPort = findPort(bNode, time, DATA_READ, BEST);
    Port bestAddyPort = findPort(bNode, time, ADDRESS_READ, BEST);
    
    if((bestDataPort == null)||(bestAddyPort == null)) 
      return 0;
    bestDataPort.getPortUseCnter().addUse(bNode, time);
    bestAddyPort.getPortUseCnter().addUse(bNode, time);
    int cost = Math.max(bestDataPort.getPortUseCnter().getLoad(bNode, time),
                        bestAddyPort.getPortUseCnter().getLoad(bNode, time));
    if(trueCost)
      cost += (bestDataPort.read_latency - 1);
    return cost;
    
  }
  
  public int subLoad(BlockNode bNode, int time, boolean trueCost) {
    Port worstDataPort = findPort(bNode, time, DATA_READ, WORST);
    Port worstAddyPort = findPort(bNode, time, ADDRESS_READ, WORST);
    
    if((worstDataPort == null)||(worstAddyPort == null)) 
      return 0;
    worstDataPort.getPortUseCnter().subUse(bNode, time);
    worstAddyPort.getPortUseCnter().subUse(bNode, time);
    int cost = Math.max(worstDataPort.getPortUseCnter().getLoad(bNode, time),
                        worstAddyPort.getPortUseCnter().getLoad(bNode, time));
    if(trueCost)
      cost += (worstDataPort.read_latency - 1);
    return cost;
    
  }
  public int addStore(BlockNode bNode, int time, boolean trueCost) {
    Port bestDataPort = findPort(bNode, time, DATA_WRITE, BEST);
    Port bestAddyPort = findPort(bNode, time, ADDRESS_WRITE, BEST);
    
    if((bestDataPort == null)||(bestAddyPort == null)) 
      return 0;
    bestDataPort.getPortUseCnter().addUse(bNode, time);
    bestAddyPort.getPortUseCnter().addUse(bNode, time);
    int cost = Math.max(bestDataPort.getPortUseCnter().getLoad(bNode, time),
                        bestAddyPort.getPortUseCnter().getLoad(bNode, time));
    //System.out.println("cost " + cost);
    if(trueCost)
      cost += (bestDataPort.read_latency - 1);
    return cost;
    
  }
  
  public int subStore(BlockNode bNode, int time, boolean trueCost) {
    Port worstDataPort = findPort(bNode, time, DATA_WRITE, WORST);
    Port worstAddyPort = findPort(bNode, time, ADDRESS_WRITE, WORST);
    
    if((worstDataPort == null)||(worstAddyPort == null)) 
      return 0;
    worstDataPort.getPortUseCnter().subUse(bNode, time);
    worstAddyPort.getPortUseCnter().subUse(bNode, time);
    int cost = Math.max(worstDataPort.getPortUseCnter().getLoad(bNode, time),
                        worstAddyPort.getPortUseCnter().getLoad(bNode, time));
    if(trueCost)
      cost += (worstDataPort.write_latency - 1);
    return cost;
    
  }
  
 /* public int subLoad(int time) {
  
    Port worstAddyPort = findWorstAddyLoadPort(time);
    Port worstDataPort = findWorstDataLoadPort(time);
    if((worstAddyPort == null)||(worstDataPort == null)) {
      return 0;
    }
    else {
      worstAddyPort.getPortUseCnter().subUse(time);
      worstDataPort.getPortUseCnter().subUse(time);
      /**
      the reason I do this is because I want to take into account the read
      latency with the cost.  That is, if there are two reads at the same
      time on a port, they will be staggered by the scheduler--and I'm 
      assuming that the staggering will be only 1 cycle)--and then the
      cost is number of reads minus one plus the read latency.  That is
      graphically, if we have these read attempts at the same cycle:
      
       |_____________|
       |_____________|
       |_____________|
      
      assuming the scheduler staggers each of these reads one cycle we'll 
      get:
      
       |_____________|
        |_____________|
         |_____________|
      
      and then the cost is the number of reads (3) minus 1 plus the latency.
      Even in this method, where we are subtracting a load, this holds true
      because the number of loads has just been reduced by one and now 
      the cost is (2-1) + read_latency
      
      return (worstDataPort.getPortUseCnter().getLoad(time)-1) + worstDataPort.read_latency;
    }
    
  }

  public int subLoadCnt(int time) {
  
    Port worstAddyPort = findWorstAddyLoadPort(time);
    Port worstDataPort = findWorstDataLoadPort(time);
    if((worstAddyPort == null)||(worstDataPort == null)) {
      return 0;
    }
    else {
      worstAddyPort.getPortUseCnter().subUse(time);
      worstDataPort.getPortUseCnter().subUse(time);
      return worstDataPort.getPortUseCnter().getLoad(time);
    }
    
  }

  public int addLoad(int time) {
  
    Port bestAddyPort = findBestAddyLoadPort(time);
    Port bestDataPort = findBestDataLoadPort(time);
    if((bestAddyPort == null)||(bestDataPort == null)) {
      return 9999;
    }
    else {
      bestAddyPort.getPortUseCnter().addUse(time);
      bestDataPort.getPortUseCnter().addUse(time);
      return (bestDataPort.getPortUseCnter().getLoad(time)-1) + bestDataPort.read_latency;
    }
    
  }

  public int addLoadCnt(int time) {
  
    Port bestAddyPort = findBestAddyLoadPort(time);
    Port bestDataPort = findBestDataLoadPort(time);
    if((bestAddyPort == null)||(bestDataPort == null)) {
      return 9999;
    }
    else {
      bestAddyPort.getPortUseCnter().addUse(time);
      bestDataPort.getPortUseCnter().addUse(time);
      return bestDataPort.getPortUseCnter().getLoad(time);
    }
    
  }*/

  public int addLoadTest(BlockNode bNode, int time, boolean trueCost) {
    Port bestDataPort = findPort(bNode, time, DATA_READ, BEST);
    Port bestAddyPort = findPort(bNode, time, ADDRESS_READ, BEST);
    
    if((bestDataPort == null)||(bestAddyPort == null)) 
      return 9999;
    //bestDataPort.getPortUseCnter().addUse(time);
    //bestAddyPort.getPortUseCnter().addUse(time);
    int cost = Math.max(bestDataPort.getPortUseCnter().testPortUse(bNode, time),
                        bestAddyPort.getPortUseCnter().testPortUse(bNode, time));
  	  /*System.out.println("+++++++++++++++++++++++++++++++");
  	  System.out.println("anfang");
  	  System.out.println("cost " + cost);
  	  System.out.println("bestDataPort.getPortUseCnter().testPortUse(bNode, time) " + bestDataPort.getPortUseCnter().testPortUse(bNode, time));
  	  System.out.println("bestAddyPort.getPortUseCnter().testPortUse(bNode, time) " + bestAddyPort.getPortUseCnter().testPortUse(bNode, time));
  	  System.out.println("+++++++++++++++++++++++++++++++");*/
    if(trueCost)
      cost += (bestDataPort.read_latency - 1);
    return cost;
    
  }
  public int addStoreTest(BlockNode bNode, int time, boolean trueCost) {
    Port bestDataPort = findPort(bNode, time, DATA_WRITE, BEST);
    Port bestAddyPort = findPort(bNode, time, ADDRESS_WRITE, BEST);
    
    if((bestDataPort == null)||(bestAddyPort == null)) 
      return 9999;
    //bestDataPort.getPortUseCnter().addUse(time);
    //bestAddyPort.getPortUseCnter().addUse(time);
    int cost = Math.max(bestDataPort.getPortUseCnter().testPortUse(bNode, time),
                        bestAddyPort.getPortUseCnter().testPortUse(bNode, time));
    if(trueCost)
      cost += (bestDataPort.write_latency - 1);
    return cost;
    
  }
  
  /*public int addLoadTest(int time) {
  
    Port bestAddyPort = findBestAddyLoadPort(time);
    Port bestDataPort = findBestDataLoadPort(time);
    if((bestAddyPort == null)||(bestDataPort == null)) {
      return 9999;
    }
    else {
      return (bestDataPort.getPortUseCnter().testPortUse(time)-1) + bestDataPort.read_latency;
    }
    
  }

  public int addLoadTestCnt(int time) {
  
    Port bestAddyPort = findBestAddyLoadPort(time);
    Port bestDataPort = findBestDataLoadPort(time);
    if((bestAddyPort == null)||(bestDataPort == null)) {
      return 9999;
    }
    else {
      return bestDataPort.getPortUseCnter().testPortUse(time);
    }
    
  }*/

  public int getDataPortUseCnt(BlockNode bNode, int time, boolean load) {
  
    HashSet dataPorts = null;
    if(load)
      dataPorts = DATA_READ;
    else 
      dataPorts = DATA_WRITE;
    Port bestDataPort = findPort(bNode, time, dataPorts, BEST);
  	  /*System.out.println("_______________________________");
  	  System.out.println("anfang");
  	  System.out.println("time " + time);
  	  System.out.println("bestDataPort.getPortUseCnter().getLoad(bNode, time) " + bestDataPort.getPortUseCnter().getLoad(bNode, time));
  	  System.out.println("_______________________________");*/
    if(bestDataPort == null) {
      return 9999;
    }
    else {
      return bestDataPort.getPortUseCnter().getLoad(bNode, time);
    }
    
  }

  /*public int getLoadDataPortUseCnt(int time) {
  
    Port bestDataPort = findBestDataLoadPort(time);
    if(bestDataPort == null) {
      return 9999;
    }
    else {
      return bestDataPort.getPortUseCnter().getLoad(time);
    }
    
  }*/

  public int addLoads(BlockNode bNode, int time, int cnt, boolean trueCost) {
    Port bestDataPort = null;
    if(trueCost)
      bestDataPort = findPort(bNode, time, DATA_READ, BEST);
    int totalCost = 0;
    for(int i=0; i<cnt; i++) {
      totalCost += addLoad(bNode, time, trueCost);
    }
    if(trueCost)
      totalCost -= (cnt-1)*bestDataPort.read_latency;
    return totalCost;
    
  }
  
  public int addStores(BlockNode bNode, int time, int cnt, boolean trueCost) {
    Port bestDataPort = null;
    if(trueCost)
      bestDataPort = findPort(bNode, time, DATA_WRITE, BEST);
    int totalCost = 0;
    for(int i=0; i<cnt; i++) {
      totalCost += addStore(bNode, time, trueCost);
    }
    //System.out.println("totalCost " + totalCost);
    if(trueCost)
      totalCost -= (cnt-1)*bestDataPort.write_latency;
    return totalCost;
    
  }
  
  /*public int addLoads(int time, int cnt) {
  
    Port bestDataPort = findBestDataLoadPort(time);
    int totalCost = 0;
      System.out.println("time " + time);
    for(int i=0; i<cnt; i++) {
      totalCost += addLoad(time);
    }
    //we need only add the read_latency once:
    if(bestDataPort == null) {
      return 9999;
    }
    else {
      return totalCost - (cnt-1)*bestDataPort.read_latency;
    }
  }

  public int addLoadsCnt(int time, int cnt) {
  
    int totalCost = 0;
    for(int i=0; i<cnt; i++) {
      totalCost += addLoadCnt(time);
    }
    //we need only add the read_latency once:
    return totalCost;
  }*/

  /*public Port findBestAddyStorePort(int time) {
    return findStoreAddyPort(time, true);
  }
  
  public Port findBestDataStorePort(int time) {
    return findStoreDataPort(time, true);
  }
  
  public Port findWorstAddyStorePort(int time) {
    return findStoreAddyPort(time, false);
  }
  
  public Port findWorstDataStorePort(int time) {
    return findStoreDataPort(time, false);
  }
  
  public Port findStoreAddyPort(int time, boolean findBest) {
    Port bestPort = null;
    Port port = null;
    int cost = 0;
    for (Iterator portIt = _port.iterator(); 
         portIt.hasNext(); ) {
      port = (Port)portIt.next();
      if(bestPort == null) bestPort = port;
      //int cost = port.getPortUseCnter().getLoad(time);
      if((port.typeCode == Port.ADDRESS_WRITE_TYPE)||
    	 (port.typeCode == Port.ADDRESS_RW_TYPE)) { 
        int portCost = port.getPortUseCnter().testPortUse(time);
        if((((cost==0)||(cost > portCost) || 
    	    ((cost == portCost)&&(port.typeCode == Port.ADDRESS_WRITE_TYPE)))
	           && findBest) ||  //find best port
	   (((cost==0)||(cost < portCost) || 
    	    ((cost == portCost)&&(port.typeCode == Port.ADDRESS_RW_TYPE)))
	           && !findBest)){   //find worst port

    	  cost = portCost;
          bestPort = port;

        }
      }
    }
    return bestPort;
  }
  
  public Port findStoreDataPort(int time, boolean findBest) {
    Port bestPort = null;
    Port port = null;
    int cost = 0;
    for (Iterator portIt = _port.iterator(); 
         portIt.hasNext(); ) {
      port = (Port)portIt.next();
      if(bestPort == null) bestPort = port;
      //int cost = port.getPortUseCnter().getLoad(time);
      if((port.typeCode == Port.DATA_WRITE_TYPE)||
    	 (port.typeCode == Port.DATA_RW_TYPE)) { 
        int portCost = port.getPortUseCnter().testPortUse(time);
        if((((cost==0)||(cost > portCost) || 
    	    ((cost == portCost)&&(port.typeCode == Port.DATA_WRITE_TYPE)))
	           && findBest) ||  //find best port
	   (((cost==0)||(cost < portCost) || 
    	    ((cost == portCost)&&(port.typeCode == Port.DATA_RW_TYPE)))
	           && !findBest)){   //find worst port

    	  cost = portCost;
          bestPort = port;

        }
      }
    }
    return bestPort;
  }*/
  
  /*public int addStore(int time) {
    Port bestAddyPort = findBestAddyStorePort(time);
    Port bestDataPort = findBestDataStorePort(time);
    if((bestAddyPort == null)||(bestDataPort == null)) {
      return 9999;
    }
    else {
      bestAddyPort.getPortUseCnter().addUse(time);
      bestDataPort.getPortUseCnter().addUse(time);
      return (bestDataPort.getPortUseCnter().getLoad(time)-1) + bestDataPort.write_latency;
    }
  }
  
  public int addStoreCnt(int time) {
    Port bestAddyPort = findBestAddyStorePort(time);
    Port bestDataPort = findBestDataStorePort(time);
    if((bestAddyPort == null)||(bestDataPort == null)) {
      return 9999;
    }
    else {
      bestAddyPort.getPortUseCnter().addUse(time);
      bestDataPort.getPortUseCnter().addUse(time);
      return bestDataPort.getPortUseCnter().getLoad(time);
    }
  }
  
  public int subStore(int time) {
    Port worstAddyPort = findWorstAddyStorePort(time);
    Port worstDataPort = findWorstDataStorePort(time);
    if((worstAddyPort == null)||(worstDataPort == null)) {
      return 0;
    }
    else {
      worstAddyPort.getPortUseCnter().subUse(time);
      worstDataPort.getPortUseCnter().subUse(time);
      return (worstDataPort.getPortUseCnter().getLoad(time)-1) + worstDataPort.write_latency;
    }
  }
  
  public int subStoreCnt(int time) {
    Port worstAddyPort = findWorstAddyStorePort(time);
    Port worstDataPort = findWorstDataStorePort(time);
    if((worstAddyPort == null)||(worstDataPort == null)) {
      return 0;
    }
    else {
      worstAddyPort.getPortUseCnter().subUse(time);
      worstDataPort.getPortUseCnter().subUse(time);
      return worstDataPort.getPortUseCnter().getLoad(time);
    }
  }*/
  
  /*public int addStoreTest(int time) {
    Port bestAddyPort = findBestAddyStorePort(time);
    Port bestDataPort = findBestDataStorePort(time);
    if((bestAddyPort == null)||(bestDataPort == null)) {
      return 9999;
    }
    else {
      return (bestDataPort.getPortUseCnter().testPortUse(time)-1) + bestDataPort.write_latency;
    }
  }
  
  public int addStoreTestCnt(int time) {
    Port bestAddyPort = findBestAddyStorePort(time);
    Port bestDataPort = findBestDataStorePort(time);
    if((bestAddyPort == null)||(bestDataPort == null)) {
      return 9999;
    }
    else {
      return bestDataPort.getPortUseCnter().testPortUse(time);
    }
  }*/
  
  /*public int addStores(int time, int cnt) {
  
    Port bestDataPort = findBestDataStorePort(time);
    int totalCost = 0;
    for(int i=0; i<cnt; i++) {
      totalCost += addStore(time);
    }
    if(bestDataPort == null) {
      return 9999;
    }
    else {
      return totalCost - (cnt-1)*bestDataPort.write_latency;
    }
  }
  
  public int addStoresCnt(int time, int cnt) {
  
    int totalCost = 0;
    for(int i=0; i<cnt; i++) {
      totalCost += addStoreCnt(time);
    }
    return totalCost;
  }
  
  public int getStoreDataPortUseCnt(int time) {
  
    Port bestDataPort = findBestDataStorePort(time);
    if(bestDataPort == null) {
      return 9999;
    }
    else {
      return bestDataPort.getPortUseCnter().getLoad(time);
    }
    
  }*/
  

}

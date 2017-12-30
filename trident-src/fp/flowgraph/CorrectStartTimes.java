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


/** This class was written to correct the start times for instructions due to 
 *  how they fit in a cycle.  A pipelined instruction must always start at the
 *  beginning of a cycle, and a non-piplined instruction cannot start in one
 *  cycle and finish in another.  This class checks for violations of these 
 *  rules and adjusts the start times for an instruction accordingly.
 * 
 * @author Kris Peterson
 */
public class CorrectStartTimes 
{

  private Instruction _inst;
  private ChipDef _chipInfo;
  private boolean _packNicht;
  private float _startTime;
  private float _runlength = -1;
  
  public CorrectStartTimes(){
  
  }

  public CorrectStartTimes(Instruction inst, ChipDef chipInfo, 
                           boolean packNicht){
    _inst = inst;
    _chipInfo = chipInfo;
    _packNicht = packNicht;
    
  }
  
  private float getRunLength() {
    if(_runlength != -1)
      return _runlength;
    
    float latency = 0;
    /*if(ALoad.conforms(_inst)) {
      Operand primSource = ALoad.getPrimalSource(_inst);
      latency = _chipInfo.getMemInitReadLat(primSource);
    }
    else if(AStore.conforms(_inst)) {
      Operand primDest = AStore.getPrimalDestination(_inst);
      latency = _chipInfo.getMemInitWriteLat(primDest);
    }
    else*/
      latency = _inst.getRunLength();

    if(_packNicht && latency<1)
      latency=1;
    
    _runlength = latency;
    
    return latency;
    
    
  }
  
  public float getCorrectedStartTime(Instruction inst, float startTime) {
    _inst = inst;
    return getCorrectedStartTime(startTime);
  }

  public float getCorrectedStartTime(float startTime) {
    return getCorrectedStartTime(startTime, 1); 
  }
  public float getCorrectedStartTime(float startTime, int upOrDwn) {
    if(getRunLength() > 1) { //if pipelined
      if(Math.abs(startTime % 1) < 0.001) {//if not starting on an even cycle
        if(upOrDwn >= 0)
	  return (float)Math.ceil((double)startTime); //round up
	else 
	  return (float)Math.floor((double)startTime); //round down
      }
      else
        return startTime;
    }
    else { //not pipelined
      float startTimeDn = (float)Math.floor((double)startTime);
      float startTimeUp = (float)Math.ceil((double)startTime);
      float finishTimeDn = (float)Math.floor((double)(startTime + 
                                                      getRunLength()));
      if(startTimeDn != finishTimeDn) {//does it start and stop in same cycle?
        if(upOrDwn >= 0)
          return startTimeUp; //if not, start in next cycle
	else 
          return startTimeDn; //if not, start in previous cycle
      }
      else
        return startTime;  //if so, no problem with this start time
    }
  }
}

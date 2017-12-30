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

import fp.hardware.*;

/** This class stores the windows used by the force directed scheduler as well
 *  as methods for editing them.
 * 
 * @author Kris Peterson
 */
public class FDWindows extends HashMap
{
  
  private float _absMax;
  
  public FDWindows() {
    super();
  }

  private class Window {
  
    public Window() {
    
    }
    
    public float max;
    public float min;
  
  }  
  
  public float getWinSize(Object key) {
  
    Window win = (Window)this.get(key);
                 //it shouldn't be necessary if the max and min really are max
		 //and min to take the absolute value
    //float size = Math.abs(win.max - win.min);
    float size = win.max - win.min;
    return size;
  }
  public float getMax(Object key) {
  
    Window win = (Window)this.get(key);
    float max = win.max;
    return max;
  }
  public float getMin(Object key) {
  
    Window win = (Window)this.get(key);
    float min = win.min;
    return min;
  }
  public void putMax(Object key, float max) {
  
    Window win;
    if(this.containsKey(key)) 
      win = (Window)this.get(key);
    else
      win = new Window();
    max = ((float)((int)((max+0.0005)*1000)))/1000; 
    win.max = max;
    this.put(key, win);
  }
  public void putMin(Object key, float min) {
  
    Window win;
    if(this.containsKey(key)) 
      win = (Window)this.get(key);
    else
      win = new Window();
    min = ((float)((int)((min+0.0005)*1000)))/1000; 
    win.min = min;
    this.put(key, win);
  }
  public void putWin(Object key, float max, float min) {
  
    Window win;
    if(this.containsKey(key)) 
      win = (Window)this.get(key);
    else
      win = new Window();
    max = ((float)((int)((max+0.0005)*1000)))/1000; 
    win.max = max;
    min = ((float)((int)((min+0.0005)*1000)))/1000; 
    win.min = min;
    this.put(key, win);
  }
  public FDWindows copy() {
  
    FDWindows clone = new FDWindows();
    for (Iterator it = this.keySet().iterator(); it.hasNext(); ) {
      Instruction win = (Instruction)it.next();
      float max = this.getMax(win);
      float min = this.getMin(win);
      clone.putWin(win, max, min);
    }
    clone._absMax = _absMax;
    return clone;
  
  }

  public Object clone() {
    FDWindows clone = new FDWindows();
    for (Iterator it = this.keySet().iterator(); it.hasNext(); ) {
      Instruction inst = (Instruction)it.next();
      Window winOld = (Window)this.get(inst);
      Window winNew = new Window();
      winNew.max = winOld.max;
      winNew.min = winOld.min;
      //float max = this.getMax(win);
      //float min = this.getMin(win);
      clone.put(inst, winNew);
    }
    clone._absMax = _absMax;
    return clone;
  
  }

  private HashMap inst2ASAPTime = new HashMap();
  public void getASAPTimes(Instruction instr, ArrayList aSAPlist) {
    int ASAPi = aSAPlist.indexOf(instr);
    Instruction asapCopy = ((Instruction)(aSAPlist.get(ASAPi)));
    float ASAPexectime = asapCopy.getExecTime();
    inst2ASAPTime.put(instr, new Float(ASAPexectime));
  }
  
  private HashMap inst2ALAPTime = new HashMap();
  public void getALAPTimes(Instruction instr, ArrayList aLAPlist) {
    int ALAPi = aLAPlist.indexOf(instr);
    Instruction alapCopy = ((Instruction)(aLAPlist.get(ALAPi)));
    float ALAPexectime = alapCopy.getExecTime();
    inst2ALAPTime.put(instr, new Float(ALAPexectime));
  }
  
  public void setMinMax(Instruction instr) {
    float minTime, maxTime;
    float ASAPexectime = ((Float)inst2ASAPTime.get(instr)).floatValue();
    float ALAPexectime = ((Float)inst2ALAPTime.get(instr)).floatValue();
    minTime = Math.min(ASAPexectime, ALAPexectime);
    maxTime = Math.max(ASAPexectime, ALAPexectime);
    _absMax = Math.max(_absMax, maxTime);
    putWin(instr, maxTime, minTime);
  }
  
  public void loadWinsFromALAPnASAPScheds(Instruction instr, 
                                           ArrayList aSAPlist,
					   ArrayList aLAPlist) {
    float absMaxTime = -9999;
    //foreach instruction
    float minTime, maxTime;
    //find the ASAP and ALAP execution times
    int ASAPi = aSAPlist.indexOf(instr);
    /*int ASAPi = -1;
    for(int i = 0; i < aSAPlist.size();i++) {
      Instruction asapCopyOfInst = ((Instruction)(aSAPlist.get(i)));
      String asapCopyAsString = asapCopyOfInst.toString();
      String originalAsString = instr.toString();
      //I have to use string compare, because the copy created two truly different
      //objects for each instruction
      if(asapCopyAsString.compareTo(originalAsString) == 0) {
    	//save location of this instruction in asap list:
        ASAPi = i;
    	continue;
      }
       
    }*/
    int ALAPi = aLAPlist.indexOf(instr);
    /*int ALAPi = -1;
    for(int i = 0; i < aLAPlist.size();i++) {
      Instruction alapCopyOfInst = ((Instruction)(aLAPlist.get(i)));
      String alapCopyAsString = alapCopyOfInst.toString();
      String originalAsString = instr.toString();
      if(alapCopyAsString.compareTo(originalAsString) == 0) {
    	ALAPi = i;
    	continue;
      }
       
    }*/
    Instruction asapCopy = ((Instruction)(aSAPlist.get(ASAPi)));
    Instruction alapCopy = ((Instruction)(aLAPlist.get(ALAPi)));
    float ASAPexectime = asapCopy.getExecTime();
    float ALAPexectime = alapCopy.getExecTime();
    minTime = Math.min(ASAPexectime, ALAPexectime);
    maxTime = Math.max(ASAPexectime, ALAPexectime);
    
    _absMax = Math.max(_absMax, maxTime);
     
    putWin(instr, maxTime, minTime);
  }
  
  public int getabsMax() {return ((int)(_absMax+0.999))+1;}
  
  public Instruction findInstWithMinWin(ArrayList instrList) { 
  
    Instruction instr = null;	 
    float minWin = 999;
    //find the instruction with the smallest window, and find a time slot
    //to schedule this one in first
    for (Iterator itsTmp = instrList.iterator(); 
        itsTmp.hasNext(); ) {
      Instruction instTmp2 = (Instruction)itsTmp.next();
      if(instr==null) {instr = instTmp2; continue;}
      boolean instrIsAInst = ALoad.conforms(instr)||AStore.conforms(instr);
      boolean instTmp2IsAInst = ALoad.conforms(instTmp2)||
                                AStore.conforms(instTmp2);
      if((instTmp2IsAInst)&&(!instrIsAInst)) {
    	//minWin = getWinSize(instTmp2);
    	instr = instTmp2;
      }
      else if(getWinSize(instTmp2) < minWin) {
    	minWin = getWinSize(instTmp2);
    	instr = instTmp2;
      }

    }
    
    return instr;
  
  }
}

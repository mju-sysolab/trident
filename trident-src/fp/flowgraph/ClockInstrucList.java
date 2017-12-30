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

//this class is used to store the database used in Schedule.java.  Schedule.java
//has a private ArrayList, which stores elements from this class.  Each of these elements
//contains a list of instructions, and an integer for the clock tick, in which these
//instructions should be executed


public class ClockInstrucList 
{

  private int ExecClkCnt;
  private ArrayList _InstList;

  public ClockInstrucList()
  {
    ExecClkCnt = 0;
    _InstList = new ArrayList();
  }

  public ClockInstrucList(int clk)
  {
    ExecClkCnt = clk;
    _InstList = new ArrayList();
  }
  
  public ClockInstrucList(int clk, Instruction inst)
  {
    ExecClkCnt = clk;
    _InstList = new ArrayList();
    _InstList.add(inst);
  }

  public ClockInstrucList(int clk, ArrayList instList)
  {
    ExecClkCnt = clk;
    _InstList = new ArrayList(instList);
  }

  public void addInst(Instruction inst)
  {
    _InstList.add(inst);
  }  

  public ArrayList getInstList()
  {
    return (_InstList);
  }  

  public void removeInst(Instruction inst)
  {
    _InstList.remove(inst);
  }  

  public int getClk()
  {
    return ExecClkCnt;
  }
  
  public void setClk(int clk)
  {
    ExecClkCnt = clk;
  }
  

}

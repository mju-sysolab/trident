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


package fp;

import fp.hwdesc.*;
import fp.hardware.*;
import fp.util.FileTree;
import java.io.File;

public class GlobalOptions {

  public static FileTree ft = new FileTree();

  public static String programFileName = null;
  public static String functionName = null;
  public static String hardwareFileName = "xd1_hw.dat";
  public static String target = "dot";

  public static boolean makeTestBench = false;
  public static String  testBenchFile = null;

  public static boolean pipeline = false;
  public static boolean unrollLoops = false;
  public static boolean mergeHyperblocks = true;

  public static Hardware hardware = null;

  public static String hardwareName = "xd1";
  public static Platform platform = null;

  //scheduler options
  public static final int ASAPSchedSelect = 1;
  public static final int ALAPSchedSelect = 2;
  public static final int FDSchedSelect = 3;
  public static int scheduleSelect = FDSchedSelect;
  public static boolean modSched = true;
  public static boolean ignorePreds = true;
  public static boolean packInstructions = true;

  // array options
  public static boolean packArrays = false;

  public static int maxAttemptsOnFDSched = 80;
  //number of tries for mod sched:
  public static float budgetRatio = (float)30.0;
  public static float cycleLength = (float)1.0;

  //hardware analysis options:
  public static boolean conserveArea = false;
  
  //hardware description:
  public static ChipDef chipDef = null;
  
  /**
  memory allocation options
  */
  //pain threshhold for running multiple attempts at prescheduling
  //for array allocation (in msec)
  public static long painThreshHold = 3000;
  public static boolean onePreAlloc = true;
  public static boolean slowestMem = false;
  /**
  ===========================================
  */
  
  //floating point library selection:
  public static String libSelect = "quixilica";
  public static Library library = null;

  // Synthesis options:
  public static boolean buildTop = false;
  
  
}


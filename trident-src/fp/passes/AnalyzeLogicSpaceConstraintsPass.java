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


package fp.passes;

import java.util.*;

import fp.*;
import fp.flowgraph.*;
import fp.hardware.*;


/** call the analyze hardware methods to decide on hardware sharing and allocate
 *  memory.
 * 
 * @author Kris Peterson
 */
public class AnalyzeLogicSpaceConstraintsPass extends Pass implements GraphPass {
   
  private boolean _conserveArea;
   
  /** "@param pm
   */
  public AnalyzeLogicSpaceConstraintsPass(PassManager pm) {
    this(pm, GlobalOptions.conserveArea);
  }
  public AnalyzeLogicSpaceConstraintsPass(PassManager pm, 
                                          boolean conserveArea) {
    super(pm);
    _conserveArea = conserveArea;
  }
  
  /** call analyzeHardware to allocate memory and decide if and which logic 
   *  devices need to be shared on the FPGA.
   * 
   * @param graph 
   */
  public boolean optimize(BlockGraph graph) {
    //AnalyzeHardareConstraints iICalc = new AnalyzeHardareConstraints();
     
    AnalyzeHrdWrConstraintsPass.iICalc.calcLogicSpaceReq(graph, 
                                                         GlobalOptions.chipDef, 
						         _conserveArea);
    return true;
  }
  
   
  public String name() { 
    return "AnalyzeLogicSpaceConstraintsPass";
  }
   
}


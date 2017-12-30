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
package fp.circuit;

import java.util.*;

import fp.util.BooleanEquation;
import fp.util.BooleanOp;
import fp.flowgraph.*;

public class ModuloStateMachine extends StateMachine {
  
  public ModuloStateMachine(int states, BlockNode bn) {
    super(states);
    System.out.println("Generating a ModuloStateMachine w/ II="+bn.getII());
    BooleanEquation backedge = getBackEdge(bn);
    if(backedge != null)
      addLoopConditionsToTable(backedge);
    collectInputs();    // this must be done after the edges are setup
    //System.out.println("State Machine: \n"+this);
  }

  /**
   * This method changes the last cycle in the FSM so that it goes (loops) 
   * back to the first cycle if the exit condition is not met.
   */
  private void addLoopConditionsToTable(BooleanEquation backedge) {
    _table[_states-2][0] = backedge;
    _table[_states-2][_states-1] = ((BooleanEquation)backedge.clone()).not();
  }

}

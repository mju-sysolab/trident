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

public abstract class FSM extends Node {

  // these are portinfos -- inputs and outputs are limited to states.
  //LinkedList inputs;
  //LinkedList outputs;
  private StateMachine _transitions;
  
  public FSM(Circuit parent, String name, StateMachine transitions) {
    super(parent, name);
    _transitions = transitions;
    
    /*
    int sm_states = _transitions.getStates();
    BooleanEquation[][] table = _transitions.getTable();
    int tb_states = table.length;

    System.out.println("FSM states:");
    System.out.println(" declared  "+_states);
    System.out.println(" table     "+tb_states);
    System.out.println(" sm_object "+sm_states);

    if (sm_states != _states) {
      System.err.println("State Machine states != declared states");
    }
    if (tb_states != _states) {
      System.err.println("State Machine table != declared states");
    }
    if (tb_states != sm_states) {
      System.err.println("State Machine table != State Machine states");
    }
    */


  }

  public int getStates() { return _transitions.getStates(); }
  protected StateMachine getTransitions() { return _transitions; }


}

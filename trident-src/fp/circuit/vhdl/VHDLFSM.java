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
package fp.circuit.vhdl;

import java.util.*;

import fp.flowgraph.Operand;

import fp.util.BooleanEquation;
import fp.util.BooleanOp;
import fp.util.Bool;

import fp.circuit.Circuit;
import fp.circuit.Operator;
import fp.circuit.PortTag;
import fp.circuit.FSM;
import fp.circuit.StateMachine;

import fp.util.vhdl.generator.*;

public class VHDLFSM extends FSM {

  // why cache a local copy?
  private StateMachine _machine;
  private Circuit _fsm;
  
  final static Waveform one = new Waveform(Char.ONE);
  final static Waveform zero = new Waveform(Char.ZERO);

  

  VHDLFSM(Circuit parent, String name, StateMachine transitions) {
    super(parent, name, transitions);
    _machine = transitions;
  }


  public void build(String name, Object[] arg_o) {
    //System.out.println("Building VHDLFSM "+getRefName());
    
    VHDLCircuit parent = (VHDLCircuit)getParent();

    Circuit my_circuit = makeVHDLCircuit(parent);
    _fsm = my_circuit;
    // add some nets ??
    my_circuit.build(name, arg_o);

    DesignUnit du = ((VHDLCircuit)my_circuit).getVHDLParent();
    LibraryUnit lu = du.getLibraryUnit();
    
    Architecture a = lu.getArchitecture();

    TypeDeclaration states = getStates(getStates());

    a.addItem(states);

    // need to be able to declear wierd types.
    // signal and types need to be revamped ...
    SimpleName state = new SimpleName("state");
    SimpleName next_state = new SimpleName("next_state");

    // find clk and reset ... should there be a check?

    SimpleName clk = new SimpleName("clk");
    SimpleName reset = new SimpleName("reset");

    // what about start ???
    // this is still no handling start correctly.

    SimpleName start = new SimpleName("start");

    a.addItem(new Signal(state, states.getType()));
    a.addItem(new Signal(next_state, states.getType()));

    StateMachine machine = getTransitions();

    //System.out.println(" state count "+getStates());
    //System.out.println(" My state machine "+machine);

    ProcessStatement state_next = 
      generateNextStateControl(clk, reset, state, next_state);

    a.addStatement(state_next);

    //System.out.println("AddStatemachine");
    ProcessStatement state_machine = 
      generateMachineControl(state, next_state, machine);

    a.addStatement(state_machine);

    //System.out.println("OutStates");
    ProcessStatement out_states = generateOutStates(state);
    a.addStatement(out_states);
    //System.out.println("Done");
    

  }

  
  ProcessStatement generateNextStateControl(Name clk, Name reset, Name state, 
					    Name next_state) {
    int start_state = _machine.getStartState();
    SimpleName init_state = new SimpleName("state_"+start_state);

    ProcessStatement state_next = 
      new ProcessStatement(new SimpleName("state_next"));

    // must go together
    state_next.addSignal(clk);
    state_next.addSignal(reset);


    // if (reset = '1') then 
    IfStatement if_reset = new IfStatement();
    state_next.addStatement(if_reset);

    // state <= start_state
    LinkedList if_reset_body = 
      if_reset.addElseIf(new Expression(new Eq(reset, Char.ONE)));

    SequentialStatement sa_default = new SignalAssignment(state, init_state);
    if_reset_body.add(sa_default);

    // if (clk'event and clk='1') then

    Expression if_clk_event = new And(new AttributeName(clk, 
							AttributeName.EVENT), 
				      new Eq(clk, Char.ONE));
    
    
    LinkedList if_clk_body = if_reset.addElseIf(if_clk_event);

    // state <= next_state
    SequentialStatement sa_init = new SignalAssignment(state, next_state);
    if_clk_body.add(sa_init);

    return state_next;
  }


  ProcessStatement generateMachineControl(Name state, Name next_state, 
					  StateMachine machine) {
    ProcessStatement statemachine = 
      new ProcessStatement( new SimpleName("machine"));

    int states = getStates();
    int start_state = machine.getStartState();
    SimpleName init_state = new SimpleName("state_"+start_state);
    
    statemachine.addSignal(state);

    HashMap start_signals = machine.getInputs();
    System.out.println("inputs "+start_signals);
    for(Iterator iter=start_signals.keySet().iterator(); iter.hasNext(); ) {
      String start = (String)iter.next();
      //Operand operand = (Operand)start_signals.get(start);
      VHDLNet net = ((VHDLCircuit)_fsm).findNet(start);

      statemachine.addSignal(net.getVHDLName());
    }
    
    CaseStatement c_state = new CaseStatement(new Expression(state));
    statemachine.addStatement(c_state);

    BooleanEquation[][] array = machine.getTable();
    for(int i=0; i<states; i++) {
      
      Identifier id = new Identifier("state_"+i);
      SequentialStatementList current_list = c_state.addAlternative(id);

      for(int j=0; j<states; j++) {
	// This is tricky, I am not sure what will happen with constants 
	// and non-pure boolean, stuff.  I think it will look like:
	//   if ((a = B"00001") and (start = '1')) or ((a != B"000001") 
	// and ( ... )
        //
	//    next_state <= something ...
	BooleanEquation current_eq = array[i][j];
	if (current_eq.isFalse()) continue;
	
	SimpleName target = new SimpleName("state_"+j);
	SequentialStatement sa_target = 
	  new SignalAssignment(next_state, target);

	if (current_eq.isTrue()) {
	  current_list.add(sa_target);
	} else {
	  IfStatement if_exp = new IfStatement();
	  current_list.add(if_exp);
	  SequentialStatementList if_body = 
	    if_exp.addElseIf(makeExpression(array[i][j]));

	  if_body.add(sa_target);
	}
	
      }
    }
    
    SequentialStatementList default_list = c_state.addDefault();
    default_list.add(new SignalAssignment(next_state, init_state));

    return statemachine;
  }


  /*
    This is going to be tricky.  The question is, how do I line up the 
    values that certain wide variables will need to be.  Or do I know if
    I will have things like:
    ((a = B'00001' or a = B'00010' or ...  Since, I made it so the boolean
    class wraps whatever is inside, It may be possible or necessary to include
    this info or to put the info somewhere else.

    What does B | ~B mean, if b has two different values...  Ugh.
  */
  Expression makeExpression(BooleanEquation eq /* , HashMap test_values */) {
    System.out.println(" I am currently assuming single bit wires, I hope that is OKAY?");
    LinkedList two_level = eq.getTwoLevel();
    Expression expression = null;
    int outer_size = two_level.size();
    if (outer_size > 1) {
      expression = new Expression(Expression.OR);
    } // else do nothing til later???
    
    for(ListIterator outer = two_level.listIterator(); outer.hasNext(); ) {
      LinkedList in_list = (LinkedList)outer.next();

      Expression in_expression;

      int inner_size = in_list.size();

      if (inner_size == 1) {
	BooleanOp op = (BooleanOp)in_list.getFirst();
	Expression op_exp = generateBooleanExpression(op);
	in_expression = op_exp;
      }	else {
	in_expression = new Expression(Expression.AND);
	for(ListIterator inner = in_list.listIterator(); inner.hasNext(); ) {
	  BooleanOp op = (BooleanOp)inner.next();
	  in_expression.and(new Paren(generateBooleanExpression(op)));
	}
	in_expression = new Paren(in_expression);
      }
      if (outer_size > 1) {
	expression.or(in_expression);
      } else {
	expression = in_expression;
      }
    }
    return expression;
  }

  // this will need to be more complicated as it needs to take into account
  // different values (like one OR zero or wide values or their inversion).

  Expression generateBooleanExpression(BooleanOp op) {
    Operand operand = (Operand)op.getOp();
    VHDLNet net = ((VHDLCircuit)_fsm).findNet(operand.getFullName());
    Name net_name = net.getVHDLName();
    
    int width = net.getWidth();
    if (width != 1) {
      System.out.println(" I DO NOT DEAL WITH NON-SINGLE BIT WIRES, HELP ME!!!");
      System.out.println(" Wire "+net+"  "+width);
    }
    Expression op_exp;
    if(op.getSense()) {
      op_exp = new Expression(new Eq(net_name, Char.ONE));
    } else {
      op_exp = new Expression(new Ne(net_name, Char.ONE));
    }
    return op_exp;
  }

				  
  ProcessStatement generateOutStates(Name state) {
    int states = getStates();
    
    ProcessStatement out_states = new ProcessStatement( new SimpleName("out_states"));
    out_states.addSignal(state);
    CaseStatement c_state = new CaseStatement(new Expression(state));

    // set one for each state.
    for (int i=0; i<states; i++) {
      SequentialStatementList list = c_state.addAlternative(new Identifier("state_"+i));
      
      for(int j=0; j<states; j++) {
	Name o_state = new SimpleName("\\o_s%"+j+"\\");
	SequentialStatement case_out;
	if (j == i) {
	  case_out = new SignalAssignment(o_state, one);
	} else {
	  case_out = new SignalAssignment(o_state, zero);
	}
	list.add(case_out);
      }
    }

    // in the default set no states.
    SequentialStatementList default_list = c_state.addDefault();
    for(int j=0; j<states; j++) {
      Name o_state = new SimpleName("\\o_s%"+j+"\\");
      default_list.add( new SignalAssignment(o_state, zero));
    } 
    
    out_states.addStatement(c_state);
    return out_states;
  }
    


  VHDLCircuit makeVHDLCircuit(Circuit parent) {
    VHDLCircuit circuit = new VHDLCircuit(parent, null, getRefName());
    
    for(Iterator iter=getInPorts().iterator(); iter.hasNext(); ) {
      PortTag in = (PortTag)iter.next();
      String port_name = in.getName();
      int size = in.getWidth();
      String in_net = in.getNet().getName();
      circuit.insertInPort(in_net,port_name,port_name,size);
    }

    for(Iterator iter=getOutPorts().iterator(); iter.hasNext(); ) {
      PortTag out = (PortTag)iter.next();
      String port_name = out.getName();
      int size = out.getWidth();
      String out_net = out.getNet().getName();
      circuit.insertOutPort(port_name,port_name,out_net,size);
    }

    // wires inside this circuit do not have sizes ...

    return circuit;
  }



  TypeDeclaration getStates(int count) {
    EnumerationType states = new EnumerationType();
    for (int i=0; i<count; i++) {
      states.addEnum(new Identifier("state_"+i));
    }
    return new TypeDeclaration(new Identifier("states"), states);
  }

  

}

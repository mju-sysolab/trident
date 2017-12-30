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

//import fp.util.BooleanEquation;
import fp.util.*;

public class Instruction implements Operators {
  
  // data members
  public Operator operator;
  private Operand[] _operands;
  private Type _operand_type;
   
  private static final int MARK1        = 0x00000001;
  private static final int MARK2        = 0x00000002;
  
  private int _flags;
   
  //added by Kris:
  private static final int NOTSCHEDULED = -1;
  private int _execClkCnt;
  private float _execTime;
  
  private boolean _isShared;
  private HashSet _shareSet;
  
  
  // maybe we should not have equations here.
  // maybe we should just have equations temporarily
  // and use another use to accomplish this ...
  private BooleanEquation _predicate;
  
  private Schedule _schedule;
   
  
  private Object _mark_object;
   
  public int scratch;
  public Object scratch_object;
  
  final static int DEFAULT_OP_COUNT = 5;
   
  
  // this constructor may not be the one to use.  The others should be more 
  //convenient.
  public Instruction(Operator op, Type type, int size, 
                     BooleanEquation predicate) {
    operator = op;
    _operands = new Operand[size];
    _operand_type = type;
    _predicate = predicate;
     
    //added by Kris:
    _execClkCnt = NOTSCHEDULED;
    _execTime = (float)NOTSCHEDULED;
  }
  
  public Instruction(Operator op, Type type) {
    this(op, type, DEFAULT_OP_COUNT, null);
  }
  
  public Instruction(Operator op, Type type, BooleanEquation predicate) {
    this(op, type, DEFAULT_OP_COUNT, predicate);
  }
   
  public Instruction(char op_code, Type type) {
    this(Operator.OperatorArray[op_code], type, null);
  }
  
  public Instruction(char op_code, Type type, BooleanEquation predicate) {
    this(Operator.OperatorArray[op_code], type, predicate);
  }
  
  // is this required ?? -- Yes, fat instructions like Phi, Switch, ... 
  // need to have the option to specify more operands...
  public Instruction(char op_code, Type type, int size) {
    this(Operator.OperatorArray[op_code], type, size, null);
  }
  
  public Instruction(char op_code, Type type, int size, 
                     BooleanEquation predicate) {
    this(Operator.OperatorArray[op_code], type, size, predicate);
  }
  
  //added by Kris:  
  public void setExecClkCnt(int clk) {
    _execClkCnt = clk;
  }
  public int getExecClkCnt() {
    return _execClkCnt;
  }
  public void setExecTime(float time) {
    _execTime = time;
  }
  public float getExecTime() {
    return _execTime;
  }
  public void unSchedule() {
    setExecTime(-1);
    setExecClkCnt(-1);
    setIsShared(false);
    setShareSet(null);
  }
  public boolean getIsShared() {
    return _isShared;
  }
  public void setIsShared(boolean isShared) {
    _isShared = isShared;
  }
  public HashSet getShareSet() {
    return _shareSet;
  }
  public void setShareSet(HashSet sharedSet) {
    _shareSet = sharedSet;
  }
  
  public Operator operator() { return operator; }
  
  public Type type() { return _operand_type; }
  public void setType(Type t) { _operand_type = t; }
   
  public char getOpcode() { return operator.opcode; }
   
  public Operand getOperand(int i) { 
    if (i >= _operands.length && i < 0) 
      return (Operand)null;
    else
      return _operands[i]; 
  }
  
  public Operand putOperand(int i, Operand op) {
    if (i >= _operands.length) {
      // dangerous ?
      resizeNumberOfOperands(2*i);
    }
    if (op == null) {
      _operands[i] = null;
      } else {
      _operands[i] = op;
    }
    return op;
  }
   
  
  void resizeNumberOfOperands(int new_size) {
    int old_size = _operands.length;
    if (old_size != new_size) {
      Operand[] new_ops = new Operand[new_size];
      int min = old_size;
      if (new_size < old_size) min = new_size;
      for (int i = 0; i < min; i++) {
        new_ops[i] = _operands[i];
      }
      _operands = new_ops;
    }
  }
  
  public void replaceOperand(Operand old_op, Operand new_op) {
    for (int i = 0; i < _operands.length; i++) {
      if (getOperand(i) == old_op) {
        putOperand(i, new_op);
      }
    }
  }
  
  public final boolean hasMemoryOperand() {
    for(int i = 0; i < _operands.length; i++) {
      Operand op_Operand = getOperand(i);
      if (op_Operand instanceof MemoryOperand) 
        return true;
    }
    return false;
  }
   
  public int getNumberOfOperands() {
    // this probably does not work for var defs or var uses.
    return getNumberOfDefs() + getNumberOfUses();
  }
  
  public int getNumberOfDefs() {
    return operator.getNumberOfDefs();
  }
  
  public int getNumberOfUses() {
    if (operator.isVariableUses()) {
      // calculate the number of uses here.
      int total = _operands.length;
       
      int numDefs = getNumberOfDefs();
      int count = 0;
      for(int i = numDefs; i < total; i++) 
        if (getOperand(i) != null) count++;
          return count;
      
      } else {
      return operator.getNumberOfUses();
    }
  }
  
  public boolean isMove() { return operator.isMove(); }
   
  public boolean isTerminator() { return operator.isTerminator(); } 
   
  public boolean isReturn() { return operator.isReturn(); }
   
  public boolean isConditionalBranch() { 
    return operator.isConditionalBranch(); 
  }
   
  public boolean isUnconditionalBranch() {
    return operator.isUnconditionalBranch();
  }
  
  public boolean isVariableUses() {
    return operator.isVariableUses();
  }
  
  public boolean isLoad() { return operator.isLoad(); }
  public boolean isStore() { return operator.isStore(); }
   
  public boolean isCompare() { return operator.isCompare(); }
  public boolean isAlloc() { return operator.isAlloc(); }
   
  public boolean isNOP() { return (operator.opcode == Operators.NOP_opcode);}
  public boolean isCommutative() { return operator.isCommutative(); }
  
  
  // -- Instruction ...
  
  // invalidate
  
  public void invalidate() {
    operator = Operators.NOP;
    // do I need to remove anything from the Hashes ???
    // can't see them from here -- don't want to.
  }
  
  public BooleanEquation getPredicate() { return _predicate; }
  public void setPredicate(BooleanEquation bool) { _predicate = bool; }
  
  boolean isMarked() { return (MARK1 & _flags) != 0; }
  void setMark() { _flags |= MARK1; }
  
  Object getMarkObject() { return _mark_object; }
  void setMarkObject(Object o) { _mark_object = o; }
  
  // this is dangerous ...
  public int getFlags() { return _flags; }
  public void setFlags(int flag_int) { _flags = flag_int; }
  
   
  /**
  get operands used by this instruction  (This is different than
  the use and def hashes in that, it returns a set of operands used
  by an instruction rather than a list of Instructions in a block that
  use a given Operand.)
  */
  public HashSet getUses() {
  
    HashSet uses = new HashSet();
    if (Branch.conforms(this)) { 
      uses.add(Branch.getBoolean(this));
      uses.add(Branch.getTarget1(this));
      uses.add(Branch.getTarget2(this));
    }
    else if (Goto.conforms(this)) { 
      //no uses?
    }
    else if(operator.isCall()) {
      uses.add(Call.getFunction(this));
      int i = 0;
      while(Call.hasArg(this, i)) {
        uses.add(Call.getArgVal(this, i));
	i++;
      }
    }
    else if(Load.conforms(this)) {
      uses.add(Load.getSource(this));
    }
    else if(ALoad.conforms(this)) {
      uses.add(ALoad.getAddrSource(this));
      uses.add(ALoad.getPrimalSource(this));
    }
    else if(Store.conforms(this)) {
      uses.add(Store.getValue(this));
    }
    else if(AStore.conforms(this)) {
      uses.add(AStore.getAddrDestination(this));
      uses.add(AStore.getValue(this));
    }
    else if(operator.isReturn()){ 
      uses.add(Return.getVal(this));
    }
    else if(Phi.conforms(this)) {
      int i = 0;
      while(Phi.hasVal(this, i)) {
        uses.add(Phi.getValOperand(this, i));
        uses.add(Phi.getValLabel(this, i));
	i++;
      }
    }
    else if(Getelementptr.conforms(this)) {
      uses.add(Getelementptr.getArrayVar(this));
      int i = 0;
      while(Getelementptr.hasVal(this, i)) {
        uses.add(Getelementptr.getValOperand(this, i));
	i++;
      }
    }
    else if(Select.conforms(this)) {
      uses.add(Select.getCondition(this));
      uses.add(Select.getVal1(this));
      uses.add(Select.getVal2(this));
    }
    else if(Cast.conforms(this)) { 
      uses.add(Cast.getType(this));
      uses.add(Cast.getValue(this));
    }
    else if(Binary.conforms(this)) {
      uses.add(Binary.getVal1(this));
      uses.add(Binary.getVal2(this));
    }
    else if(Unary.conforms(this)) {
      uses.add(Unary.getVal(this));
    }
    else if(Test.conforms(this)) {
      uses.add(this.getOperand(1));
      uses.add(this.getOperand(2));
    }
    else if(Switch.conforms(this)) { //what is this operand?
      if(Switch.hasDefault(this))
        uses.add(Switch.getDefault(this)); //is this a use or an def?
      if(Switch.hasTest(this))
        uses.add(Switch.getTest(this)); //is this a use or an def?
      int i = 0;
      while(Switch.hasCase(this, i)) {
        uses.add(Switch.getCaseValue(this, i));
	i++;
      }
    }
    else if(Shift.conforms(this)) { //what is this operand?
      //no uses?
    }
    return uses;
    
  }
  
  
  /**
  get operands defined by this instruction
  */
  public HashSet getDefs() {
  
    HashSet defs = new HashSet();
    if (Branch.conforms(this)) { 
      //no defs?
    }
    else if (Goto.conforms(this)) { 
      defs.add(Goto.getTarget(this));
    }
    else if(operator.isCall()) {
      defs.add(Call.getResult(this));
    }
    else if(Load.conforms(this)) {
      defs.add(Load.getResult(this));
    }
    else if(ALoad.conforms(this)) {
      defs.add(ALoad.getResult(this));
    }
    else if(Store.conforms(this)) {
      defs.add(Store.getDestination(this));
    }
    else if(AStore.conforms(this)) {
      defs.add(AStore.getPrimalDestination(this));
    }
    else if(operator.isReturn()){ 
      //no defs
    }
    else if(Phi.conforms(this)) {
      defs.add(Phi.getResult(this));
    }
    else if(Getelementptr.conforms(this)) {
      defs.add(Getelementptr.getResult(this));
    }
    else if(Select.conforms(this)) {
      defs.add(Select.getResult(this));
    }
    else if(Cast.conforms(this)) { 
      defs.add(Cast.getResult(this));
    }
    else if(Binary.conforms(this)) {
      defs.add(Binary.getResult(this));
    }
    else if(Unary.conforms(this)) {
      defs.add(Unary.getResult(this));
    }
    else if(Test.conforms(this)) {
      defs.add(Test.getResult(this));
    }
    else if(Switch.conforms(this)) { //what is this operand?
      //no defs?
    }
    else if(Shift.conforms(this)) { //what is this operand?
      //no uses?
    }
    return defs;
    
  }
  
  // scheduling stuff
  // schedule
  // start cycle/start value
  // stop cycle/stop value
  // _run_length, _pipe_states, _prop_delay
  // register output
  
  Schedule getSchedule() { return _schedule; }
  void setSchedule(Schedule s_Schedule) { _schedule = s_Schedule; }
  
   
  public boolean isPipelined() { return operator.isPipelined(); }
  public float getRunLength() { return operator.getRunLength(); }
  public int getPipeStages() { return operator.getPipeStages(); }
  public float getArea() { return operator.getArea(); }
  
  public Instruction copy() { 
    Instruction copy = new Instruction(operator, _operand_type, 
    _operands.length, _predicate);
    for(int i = 0; i <_operands.length; i++) {
      if (_operands[i] != null) {
        copy.putOperand(i,_operands[i].copy());
      }
    }
    copy.setFlags(_flags);
     
    // what about scheduling stuff...
    //copy.setExecClkCnt(_execClkCnt);
    //copy.setExecTime(_execTime);
    return copy;
  }
  
  public Instruction deepCopy() { 
    Instruction copy = new Instruction(operator, _operand_type, 
    _operands.length, _predicate);
    for(int i = 0; i <_operands.length; i++) {
      if ((_operands[i] != null)&&(!_operands[i].isPrimal())) {
        copy.putOperand(i,_operands[i].copy().getNextNonPrime());
      }
    }
    copy.setFlags(_flags);
     
    // what about scheduling stuff...
    //copy.setExecClkCnt(_execClkCnt);
    //copy.setExecTime(_execTime);
    return copy;
  }
  
  //most of you probably don't want to use this...
  public Instruction copySaveOps() { 
    Instruction copy = new Instruction(operator, _operand_type, 
    _operands.length, _predicate);
    for(int i = 0; i <_operands.length; i++) {
      if (_operands[i] != null) {
        copy.putOperand(i,_operands[i]);
      }
    }
    copy.setFlags(_flags);
     
    // what about scheduling stuff...
    //copy.setExecClkCnt(_execClkCnt);
    //copy.setExecTime(_execTime);
    return copy;
  }
  
  
  public String toString() {
    StringBuffer result = new StringBuffer();
    
    Operand op;
    // this cannot work for variable size instructions.
    //int total = getNumberOfOperands();
    int total = _operands.length;
    
    int numDefs = getNumberOfDefs();
    int defsPrinted = 0;
    for(int i = 0; i < numDefs; i++) {
      op = getOperand(i);
      if (op != null) {
        if (defsPrinted > 0) result.append(", ");
          result.append(op);
        defsPrinted++;
      }
    }
    
    if (defsPrinted > 0) result.append(" = ");
      
    result.append(operator);
    result.append(" ");
    if (_operand_type != null) {
      result.append(_operand_type);
      result.append(" ");
    }
     
    int usesPrinted=0;
    for(int i = numDefs; i < total; i++) {
      op = getOperand(i);
      if (op != null) {
        if (usesPrinted > 0) result.append(", ");
          usesPrinted++;
        if (op != null) {
          result.append(op);
          } else {
          result.append("<unused>");
        }
      }
    }
    return result.toString();
  }
  
  
  public String toDotLabel() {
    // this will be fancier as there is more to see.
    StringBuffer sbuf = new StringBuffer();
    if(_execClkCnt!=NOTSCHEDULED) {
      sbuf.append(_execClkCnt);
      sbuf.append('|');
      sbuf.append(_execTime);
      sbuf.append('|');
       
    }
    sbuf.append(toString());
    if (_predicate != null) {
      sbuf.append('|');
      sbuf.append(_predicate.toDot());
    }
    return sbuf.toString();
  }
  
  
  
}

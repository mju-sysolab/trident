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
import fp.util.UniqueName;

public abstract class Operand {
  // I don't think this can be useful.
  //public Instruction instruction;

  protected String _name;
  
  // is type required here ??? LLVM does not use type 
  // on a per operand basis...
  // uh -- operands know where they are used so they can
  // ask the instruction what type they are.
  // I guess that might be okwy.
  //
  // this is for type operands not operands with types.
  // that may not totally make sense, but some operations
  // have a type associated with individual operands -- 
  // different than binary or binary similar instructions
  // which have mostly one type. 
  protected Type _type;

  // Type should know how to generate value.
  protected int _assignment;
  
  private final static HashMap _operands = new HashMap();
  private final static UniqueName _names = new UniqueName();


  // coold be expanded into flags.
  private boolean _mark;
  private Object _marker;
  
  // widths ... 
  // do we do width analysis, where is the info stored.
  // on the operands?

  // primals ... hmmm.
  // how are primal distinguished, how important are they.

  public static int NOT_ASSIGNED = -1;

  // ?? keep this approach ...
  static final String SYNTHETIC_NAME_PREFIX = "%";
  static final String PREDICATE_NAME = SYNTHETIC_NAME_PREFIX + "B";
  static final char SPACE = '_';

  public String getFullName() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(_name);
    if (_assignment != NOT_ASSIGNED) {
      sbuf.append(SPACE);
      sbuf.append(_assignment);
    }
    return sbuf.toString();
  }

  public String getName() { return _name; }
  public Type getType() { return _type; }
  public void setType(Type type) { _type = type; }
  public int getAssignment() { return _assignment; }

  public void setMarker(Object o) { _marker = o; }
  public Object getMarker() { return _marker; }

  public void setMark(boolean value) { _mark = value; }
  public boolean getMark() { return _mark; }


  public abstract Operand copy();

  public abstract Operand getNext();

  // This can only be called after the parsing pass and all
  // names in the program have been seen.
  public Operand getNextNonPrime() {
    if (this instanceof PrimalOperand) 
      return (Operand)nextBlock(_name);
    else 
      return getNext();
  }

  /*
  public final Operand clear() {
    //instruction = null;
    return this;
  }
  */

  final private String getKey() {
    return _name + SPACE + _assignment;
  }

  static public Operand getOperand(String name, int assign) {
    String key = name + SPACE + assign;
    return (Operand)_operands.get(key);
  }

  // assumeUniqueNames() is used after putting a new Operand
  // into the _operands HashMap.  It updates the asignment value
  // for that name in the _names UniqueNamer, so that when
  // _names.getNextIndex() is called it gets the next highest
  // index over all the other indices that have been put so far.
  static private void assureUniqueNames(String name, int assign) {
    Integer value = (Integer)_names.get(name);
    if (value == null) {
      value = new Integer(assign + 1);
      _names.put(name, value);
    }
    int int_value = value.intValue();
    if (int_value <= assign) {
      _names.put(name, new Integer(assign + 1));
    }
  }

  static public BlockOperand newBlock(String name, int assign) throws IllegalOperand{
    Operand result = getOperand(name, assign);
    if (result == null) {
      result = new BlockOperand(name, assign);
      _operands.put(result.getKey(), result);
      assureUniqueNames(name, assign);
    } else if (!result.isBlock()) {
      throw new IllegalOperand("Block operand name: " + name + " Assignment: " + assign);
    }
    return (BlockOperand)result;
  }

  static public BlockOperand newBlock(String name) {
    return newBlock(name, NOT_ASSIGNED);
  }

  // This can only be called after the parsing pass and all
  // names in the program have been seen.
  static public BlockOperand nextBlock(String name) {
    int index = _names.getNextIndex(name);
    return newBlock(name, index);
  }

  static public BooleanOperand newBoolean(String name, int assign) throws IllegalOperand{
    Operand result = getOperand(name, assign);
    if (result == null) {
      result = new BooleanOperand(name, assign);
      _operands.put(result.getKey(), result);
      assureUniqueNames(name, assign);
    } else if (!result.isBoolean()) {
      throw new IllegalOperand("Boolean operand name: " + name + " Assignment: " + assign);
    }
    return (BooleanOperand)result;
  }

  static public BooleanOperand newBoolean(String name) {
    return newBoolean(name, NOT_ASSIGNED);
  }

  // This can only be called after the parsing pass and all
  // names in the program have been seen.
  static public BooleanOperand nextBoolean(String name) {
    int index = _names.getNextIndex(name);
    return newBoolean(name, index);
  }

  static public AddrOperand newAddr(String name, int assign) throws IllegalOperand{
    Operand result = getOperand(name, assign);
    if (result == null) {
      result = new AddrOperand(name, assign);
      _operands.put(result.getKey(), result);
      assureUniqueNames(name, assign);
    } else if (!result.isAddr()) {
      throw new IllegalOperand("Addr operand name: " + name + " Assignment: " + assign);
    }
    return (AddrOperand)result;
  }

  static public AddrOperand newAddr(String name) {
    return newAddr(name, NOT_ASSIGNED);
  }

  static public UnknownOperand newUnknown(String origname, String name, int assign) throws IllegalOperand {
    Operand result = new UnknownOperand(origname, name, assign);
    return (UnknownOperand)result;
  }

  static public UnknownOperand newUnknown(String name) throws IllegalOperand {
    return newUnknown(name, name, NOT_ASSIGNED);
  }

  // This can only be called after the parsing pass and all
  // names in the program have been seen.
  static public AddrOperand nextAddr(String name) {
    int index = _names.getNextIndex(name);
    return newAddr(name, index);
  }

  static public DoubleConstantOperand newDoubleConstant(double dp) {
    return new DoubleConstantOperand(dp);
  }

  static public FloatConstantOperand newFloatConstant(float fp) {
    return new FloatConstantOperand(fp);
  }

  static public IntConstantOperand newIntConstant(int value) {
    // In SC Constants were not unique, because that made 
    // weird hardware.  (Everything tied together...)
    return new IntConstantOperand(value);
  }
  
  static public LongConstantOperand newLongConstant(long value) {
    return new LongConstantOperand(value);
  }
 
  static public LabelOperand newLabel(String name, int assign) throws IllegalOperand {
    Operand result = getOperand(name, assign);
    if (result == null) {
      result = new LabelOperand(name, assign);
      _operands.put(result.getKey(), result);
      assureUniqueNames(name, assign);
    } else if (!result.isLabel()) { 
      throw new IllegalOperand("Label Operand name: " + name);
    }
    return (LabelOperand)result;
  }
   
  static public LabelOperand newLabel(String name) {
    return newLabel(name, NOT_ASSIGNED);
  }

  // This can only be called after the parsing pass and all
  // names in the program have been seen.
  static public LabelOperand nextLabel(String name) {
    int index = _names.getNextIndex(name);
    return newLabel(name, index);
  }
 
  static public MemoryOperand newMemory(String name, int size) throws IllegalOperand{
    Operand result = getOperand(name, NOT_ASSIGNED);
    if (result == null) {
      result = new MemoryOperand(name, size);
      _operands.put(result.getKey(), result);
      assureUniqueNames(name, NOT_ASSIGNED);
    } else if (!result.isMemory()) {
      throw new IllegalOperand("Memory Operand name: " + name);
    }
    return (MemoryOperand)result;
  }

  // does this make sense ??
  static public MemoryOperand nextMemory(String name) {
    return (MemoryOperand) null;
  }

  static public PrimalOperand newPrimal(String name) throws IllegalOperand {
    Operand result = getOperand(name, NOT_ASSIGNED);
    if (result == null) {
      result = new PrimalOperand(name);
      _operands.put(result.getKey(), result);
      assureUniqueNames(name, NOT_ASSIGNED);
    } else if (!result.isPrimal()) {
      throw new IllegalOperand("Primal Operand name: " + name);
    }
    return (PrimalOperand)result;
  }

  //Do not use this method unless you are prepared to change all instructions,
  //def and use hashes that use this operand, after calling this method!!!!!!!!!!
  static public PrimalOperand changeTmp2Primal(String name)  {
   Operand result = getOperand(name, NOT_ASSIGNED);
   if (result != null) {
      _operands.remove(name);
   }
   
   result = new PrimalOperand(name);
   _operands.put(result.getKey(), result);
   assureUniqueNames(name, NOT_ASSIGNED);
   return (PrimalOperand)result;
  }

  static public PrimalOperand nextPrimal(String name) {
    return (PrimalOperand) null;
  }

  static public TypeOperand newType(Type type) {
    return new TypeOperand(type);
  }

  // this could be an exception ...
  static public TypeOperand nextType(Type type) {
    return (TypeOperand) null;
  }

  public final boolean isConstant() { 
    return (this instanceof ConstantOperand); 
  }

  public final boolean isBooleanConstant() {
    return ((this == BooleanOperand.TRUE) || (this == BooleanOperand.FALSE));
  }

  public final boolean isFloatConstant() { 
    return this instanceof FloatConstantOperand;
  }

  public final boolean isDoubleConstant() {
    return this instanceof DoubleConstantOperand;
  }

  public final boolean isIntConstant() {
    return this instanceof IntConstantOperand;
  }
  
  public final boolean isLongConstant() {
    return this instanceof LongConstantOperand;
  }
  /*
  public final boolean isNullConstant() {
    return this instanceof NullConstantOperand;
  }

  // ?? what is the difference between this and label?
  public final boolean isBranch() {
    return this instanceof BranchOperand;
  }

  */
  public final boolean isBlock() {
    return this instanceof BlockOperand;
  }

  public final boolean isBoolean() {
    return this instanceof BooleanOperand;
  }

  public final boolean isMemory() {
    return this instanceof MemoryOperand;
  }

  public final boolean isPrimal() {
    //System.out.println("isPrimal of " + getName() + " is: " + (this instanceof PrimalOperand));
    return this instanceof PrimalOperand;
  }

  public final boolean isAddr() {
    return this instanceof AddrOperand;
  }

  public final boolean isUnknown() {
    return this instanceof UnknownOperand;
  }

  public final boolean isLabel() {
    return this instanceof LabelOperand;
  }

  public final boolean isType() {
    return this instanceof TypeOperand;
  }

  // auto casts into the different types
  // asConstant() ...


  public String toString() {
    String op = null;
    if (isPrimal()) {
      op = "PRIMAL";
    } else if (isBlock()) {
      op = "BLOCK";
    } else if (isBoolean()) {
      op = "BOOLEAN";
    } else if (isAddr()) {
      op = "ADDR";
    } else if (isUnknown()) {
      op = "UNKNOWN";
    } else if (isType()) {
      op = "TYPE";
    } else if (isLabel()) {
      op = "LABEL";
    } else if (isFloatConstant()) {
      op = "FLOAT";
    } else if (isDoubleConstant()) {
      op = "DOUBLE";
    } else if (isIntConstant()) {
      op = "INT";
    } else if (isLongConstant()) {
      op = "LONG";
    } else if (isMemory()) {
      op = "MEMORY";
    } else op = "NO HECK IDEA!";

    StringBuffer sbuf = new StringBuffer("(" + op + ") " + SYNTHETIC_NAME_PREFIX);
    sbuf.append(_name);
    if (_assignment != NOT_ASSIGNED) {
      sbuf.append(SPACE);
      sbuf.append(_assignment);
    }
    return sbuf.toString();
  }
    
  public boolean isIntegerOperand() {
    return false;
  }

  public boolean isFloatingPointOperand() {
    return false;
  }

  public boolean isIntegralOperand() {
    return (isIntegerOperand() || isBoolean());
  }

  public boolean isFirstClassOperand() {
    return (isIntegerOperand() || isFloatingPointOperand() || isBoolean());
  }

}

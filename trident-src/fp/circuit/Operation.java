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

import java.util.HashMap;

public class Operation {
  private static class UndefOp extends Operation {
    UndefOp() { super("undef", "???", false); }
  }
  public static final Operation UNDEF = new UndefOp();
 
  private static class AddOp extends Operation {
    AddOp() { super("add", "+", true); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseAdd(); }
  }
  public static final Operation ADD = new AddOp();
   
  private static class AndOp extends Operation {
    AndOp() { super("and", "&", true); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseAnd(); }
  }
  public static final Operation AND = new AndOp();

  private static class BufOp extends Operation {
    BufOp() { super("buf", "=", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseBuf(); }
  }
  public static final Operation BUF = new BufOp();

  private static class MultOp extends Operation {
    MultOp() { super("mult", "*", true); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseMult(); }
  }
  public static final Operation MULT = new MultOp();
 
  private static class NotOp extends Operation {
    NotOp() { super("not", "!", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseNot(); }
  }
  public static final Operation NOT = new NotOp();

  private static class OrOp extends Operation {
    OrOp() { super("or", "|", true); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseOr(); }
  }
  public static final Operation OR = new OrOp();

  private static class ShlOp extends Operation {
    ShlOp() { super("shl", "<<", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseShl(); }
  }
  public static final Operation SHL = new ShlOp();
 
  private static class ShrOp extends Operation {
    ShrOp() { super("shr", ">>", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseShr(); }
  }
  public static final Operation SHR = new ShrOp();
 
  private static class SubOp extends Operation {
    SubOp() { super("sub", "-", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseSub(); }
  }
  public static final Operation SUB = new SubOp();
 
  private static class UshrOp extends Operation {
    UshrOp() { super("ushr", ">>>", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseUshr(); }
  }
  public static final Operation USHR = new UshrOp();
 
  private static class XorOp extends Operation {
    XorOp() { super("xor", "^", true); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseXor(); }
  }
  public static final Operation XOR = new XorOp();
 
  private static class NegOp extends Operation {
    NegOp() { super("neg", "-", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseNeg(); }
  }
  public static final Operation NEG = new NegOp();

  private static class MuxOp extends Operation {
    MuxOp() { super("mux", "?:", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseMux(); }
  }
  public static final Operation MUX = new MuxOp();

  private static class ExpandOp extends Operation {
    ExpandOp() { super("expand","#", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseExpand(); }
  }
  public static final Operation EXPAND = new ExpandOp();

  private static class SliceOp extends Operation {
    SliceOp() { super("slice", "slice", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseSlice(); }
  }
  public static final Operation SLICE = new SliceOp();
  

  private static class ConcatOp extends Operation {
    ConcatOp() { super("concat", "^", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseConcat(); }
  }
  public static final Operation CONCAT = new ConcatOp();

  private static class DivOp extends Operation {
    DivOp() { super("div", "/", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseDiv(); }
  }
  public static final Operation DIV = new DivOp();
  
  private static class SqrtOp extends Operation {
    SqrtOp() { super("sqrt", "sqrt", false); }
    public void apply(OperationSwitch opSwitch) { opSwitch.caseSqrt(); }
  }
  public static final Operation SQRT = new SqrtOp();

  private final String _name;
  private final String _symbol;
  private final boolean _isCommutative;
  private final boolean _isPipelined;

  private static HashMap _op_hash = new HashMap();

  /**
   * Constructs a new <code>Operation</code>
   *
   * @param name the name of the operation, such as "add"
   * @param symbol the symbol for the operation, such as "+"
   * @param isIf whether the operation corresponds to an if statement
   * @param isCommutative whether the order of the operands can be changed
   * without affecting the result
   */
  protected Operation(String name, String symbol, boolean isCommutative) {
    this(name, symbol, isCommutative, false);
  }

  protected Operation(String name, String symbol,boolean isCommutative,
		      boolean isPipelined) {
    _name = name;
    _symbol = symbol;
    _isCommutative = isCommutative;
    _isPipelined = isPipelined;
  }


  static public void add(String name, String symbol, boolean commutative, 
			 boolean pipelined) {
    _op_hash.put(name, new Operation(name, symbol, commutative, pipelined));
  }

  static public Operation get(String name) {
    return (Operation)_op_hash.get(name);
  }

  /**
   * 
   */
  public void apply(OperationSwitch opSwitch) {
    opSwitch.defaultCase(this);
  }

    // Subclasses should not override this method
  public final boolean equals(Object o) {
    return super.equals(o);
  }

  /**
   * Returns the name of this operation
   */
  public String getName() {
    return _name;
  }


  /**
   * Returns the symbol for this operation
   */
  public String getSymbol() {
    return _symbol;
  }

  // Subclasses should not override this method
  public final int hashCode() {
    return super.hashCode();
  }

  /**
   * Returns <code>true</code> if the order of the operands can be changed
   * without affecting the result
   */
  public boolean isCommutative() {
    return _isCommutative;
  }

  public boolean isPipelined() {
    return _isPipelined;
  }
 
  /**
   *
   */
  public String toString() {
    return _name;
  }


}


  

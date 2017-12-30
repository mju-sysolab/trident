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


/**
 * Implemented by Objects in order to perfrom a multiway branch or
 * switch, on an <code>Operation</code>/ It corresponds to the Visitor
 * in the the Visitor pattern (see <i>Design Patterns</i>, 1995).
 *
 * @see Operation
 *
 * @author Justin L. Tripp
 */



public interface OperationSwitch {

  public void caseAdd();
  public void caseAnd();
  public void caseBuf();
  public void caseConcat();
  public void caseExpand();
  public void caseMult();
  public void caseMux();
  public void caseNeg();
  public void caseNot();
  public void caseOr();
  public void caseShl();
  public void caseShr();
  public void caseSub();
  public void caseUshr();
  public void caseXor();
  public void caseDiv();
  public void caseSqrt();
  public void caseSlice();

  public void defaultCase(Operation op);

}




  

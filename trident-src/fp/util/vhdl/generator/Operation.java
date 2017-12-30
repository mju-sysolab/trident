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


package fp.util.vhdl.generator;

public interface Operation {

  /*
    If these were objects it could be nice to know if they are 
    unary or binary operators and to allow them to act differently
    for right and left operands.

  */

  
  public static final String EMPTY  = "";
  public static final String NOP    = "";

  public static final String ABS    = "abs";
  public static final String ADD    = "+";
  public static final String AND    = "and";
  public static final String CONCAT = "&";
  public static final String DIV    = "/";
  public static final String GE     = ">=";
  public static final String GT     = ">";
  public static final String EQ     = "=";
  public static final String LE     = "<=";
  public static final String LT     = "<";
  public static final String MINUS  = "-";
  public static final String MOD    = "mod"; 
  public static final String MULT   = "*";
  public static final String NAND   = "nand";
  public static final String NE     = "/=";
  public static final String NOR    = "nor";
  public static final String NOT    = "not";
  public static final String OR     = "or";
  public static final String POW    = "**";
  public static final String PLUS   = "+";
  public static final String REM    = "rem";
  public static final String ROL    = "rol";
  public static final String ROR    = "ror";
  public static final String SLL    = "sll";
  public static final String SLA    = "sla";
  public static final String SRA    = "sra";
  public static final String SRL    = "srl";
  public static final String SUB    = "-";
  public static final String XNOR   = "xnor";
  public static final String XOR    = "xor";








}  

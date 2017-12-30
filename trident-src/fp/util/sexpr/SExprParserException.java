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


/*
 *  SExprParserException.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id$
 */

package fp.util.sexpr;

/**
 * An exception class for syntax errors detected during s-expression parsing.
 */
public class SExprParserException extends Exception {

  /**
   * Initialize the exception with an explanatory message.
   */
  public SExprParserException(String explanation)
  {
    super(explanation);
  }

  /**
   * Initialize the exception with a message about an illegal character.
   */
  public SExprParserException(char illegalChar)
  {
    super("Invalid character '" + illegalChar + "'");
  }

}

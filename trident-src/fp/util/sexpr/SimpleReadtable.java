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
 *  SimpleReadtable.java
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
 * Basic implementation of the Readtable interface, a dispatch table.
 */
public class SimpleReadtable implements Readtable {

  private SExprParser parsers[];

  /**
   * Initializes an empty dispatch table (no associations).
   */
  public SimpleReadtable() {
    parsers = new SExprParser[256];
  }

  /**
   * Copy constructor.
   */
  public SimpleReadtable(SimpleReadtable table) {
    parsers = new SExprParser[256];
    for (int i = 0; i < 256; i++)
      parsers[i] = table.parsers[i];
  }

  /*
    jt

    This interface seems strange.  I don't understand why you
    would want to split on chars.  Somethings like strings might
    be okay or the vectors might work, but in general you may want
    to parse whole delimited chunks -- atleast, I would think so.

  */


  public SExprParser getParser(char key) {
    return parsers[(int)key];
  }

  public SExprParser addParser(char key, SExprParser parser) {
    parsers[(int)key] = parser;
    return parser;
  }

}

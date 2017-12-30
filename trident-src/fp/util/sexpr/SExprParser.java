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
 *  SExprParser.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id$
 */

package fp.util.sexpr;

import java.io.IOException;

/**
 * An interface for all dispatched "sub-parsers."
 */
public interface SExprParser {

  /**
   * Dispatched on character <i>first</i>, parse an object from the stream.
   *
   * @exception SExprParserException on syntax error
   * @exception IOException on I/O related problems (e.g. end of file)
   */
  public Object parse(char first, SExprStream stream)
    throws SExprParserException, IOException;

}

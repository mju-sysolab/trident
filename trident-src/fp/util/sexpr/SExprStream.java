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
 *  SExprStream.java
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
import java.util.Map;

/**
 * An interface for a full s-expression parser.
 */
public interface SExprStream extends SExprParser {

  /**
   * Parse a single object from the stream.
   * @exception SExpParseException if the input stream cannot be parsed.
   * @exception IOException if the input stream cannot be opened or read.
   */
  public Object parse() throws SExprParserException, IOException;

  /**
   * Access the symbol table of the parser.
   */
  public Map getSymbols();

  /**
   * Assign the symbol table of the parser.
   */
  public Map setSymbols(Map symbols);

  /**
   * Access the dispatch table of the parser.
   */
  public Readtable getReadtable();

  /**
   * Assign the dispatch table of the parser.
   */
  public Readtable setReadtable(Readtable readtable);

  /**
   * Associate an input character with a "sub-parser."
   */
  public SExprParser addParser(char key, SExprParser parser);

  /**
   * Checks whether lists are to be parsed as Vectors or Cons cells.
   */
  public boolean getListsAsVectors();

  /**
   * Controls whether parsed lists are Vectors or Cons cells.
   */
  public boolean setListsAsVectors(boolean listsAsVectors);

  /**
   * Accesses an empty string buffer available temporary storage.
   */
  public StringBuffer getScratchBuffer();

  /**
   * Reads from the stream, skipping whitespace.
   */
  public char readSkipWhite() throws IOException;

  /**
   * Read a single character from the stream.
   * This method is here because there is no InputStream interface in the
   * java.io package (JavaSoft please take notice!).
   */
  public int read() throws IOException;

}

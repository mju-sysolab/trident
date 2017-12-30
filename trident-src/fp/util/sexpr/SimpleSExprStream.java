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
 *  SimpleSExprStream.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id$
 */

package fp.util.sexpr;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;

import java.net.URL;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Vector;


/**
 * Basic implementation of the SExprStream parser interface.
 */
public class SimpleSExprStream extends PushbackInputStream
                               implements SExprStream {

  private StringBuffer buffer;
  private Map symbols;
  private boolean noSymbols;
  private Readtable readtable;
  private boolean listsAsVectors;

  // this does not handle 1.235e10 or that kind of FP
  static final Pattern FP = Pattern.compile("-?[0-9]*\\.[0-9]+");
  static final Pattern HEX = Pattern.compile("0x[A-Fa-f_0-9]+");
  static final Pattern NUM = Pattern.compile("\\d+");


  /**
   * Initializes the parser with no read table and no symbol table assigned.
   * Parsed lists will be represented as Cons cells.
   */
  public SimpleSExprStream(InputStream input) {
    super(input);

    buffer = new StringBuffer();
    symbols = null;
    noSymbols = false;
    readtable = null;
    listsAsVectors = false;
  }

  /**
   * Accesses the symbol table of the parser.
   * If no symbol table has been assigned, creates an empty table.
   */
  public Map getSymbols() {
    if (!noSymbols && symbols == null)
      symbols = new HashMap();
    return symbols;
  }

  /**
   * Assigns a symbol table to the parser.
   * Assigning <tt>null</tt> will prevent an empty symbol table to be created
   * in the future.
   */
  public Map setSymbols(Map d) {
    if (d == null)
      noSymbols = true;
    return symbols = d;
  }

  /**
   * Accesses the read table of the parser.
   * If no read table has been assigned, creates an empty table.
   */
  public Readtable getReadtable() {
    if (readtable == null)
      readtable = new SimpleReadtable();
    return readtable;
  }

  /**
   * Assigns a new read table to the parser.
   */
  public Readtable setReadtable(Readtable _readtable) {
    return readtable = _readtable;
  }

  /**
   * Checks whether lists should be parsed as Vectors or Cons cells.
   */
  public boolean getListsAsVectors() {
    return listsAsVectors;
  }

  /**
   * Controls whether lists are represented as Vectors or Cons cells.
   */
  public boolean setListsAsVectors(boolean _listsAsVectors) {
    return listsAsVectors = _listsAsVectors;
  }

  /**
   * Accesses an empty string buffer available temporary storage.
   * This buffer can be used by sub-parsers as a scratch area. Please note
   * that the buffer is not guarded in any way, so multithreaded and reentrant
   * programs must worry about this themselves.
   */
  public StringBuffer getScratchBuffer() {
    buffer.setLength(0);
    return buffer;
  }

  /**
   * Parses a single object from the underlying input stream.
   *
   * @exception SExprParserException if syntax error was detected
   * @exception IOException if any other I/O-related problem occurred
   */
  public Object parse()
    throws SExprParserException, IOException {
    return parse(readSkipWhite(), this);
  }

  /**
   * Parses a single object started by the character <i>c</i>.
   * Implements the SExprParser interface.
   *
   * @exception SExprParserException if syntax error was detected
   * @exception IOException if any other I/O-related problem occurred
   */
  public Object parse(char c, SExprStream stream)
    throws SExprParserException, IOException {
    SExprParser parser = getReadtable().getParser(c);
    // this parser per char is wierd.  It might be nice to
    // have a Pattern/Parser combo, but otherwise, I am not
    // sure how useful this is.
    //
    //System.out.println(" Parser for "+c+" "+parser);
    if (parser != null)
      return parser.parse(c, this);
    else if (c == '(') {
      if (getListsAsVectors())
        return parseVector(new Vector(), ')');
      else
        return parseList();
    }
    else if (c == '"')
      return parseString();
    else if (isAtomChar(c, true))
      return parseAtom(c);
    else
      throw new SExprParserException(c);
  }

  /**
   * Parses a list (as Cons cells) sans first character.
   *
   * @exception SExprParserException if syntax error was detected
   * @exception IOException if any other I/O-related problem occurred
   */
  protected Cons parseList()
    throws SExprParserException, IOException {
    char c = readSkipWhite();
    if (c == ')')
      return null;
    else {
      unread(c);
      return new Cons(parse(), parseList());
    }
  }

  /**
   * Parses a list (as a Vector) sans first character.
   * In order to parse list-like structures delimited by other characters
   * than parentheses, the delimiting (ending) character has to be provided.
   *
   * @exception SExprParserException if syntax error was detected
   * @exception IOException if any other I/O-related problem occurred
   */
  protected Vector parseVector(Vector vector, char delimiter)
    throws SExprParserException, IOException {
    char c = readSkipWhite();
    if (c == delimiter)
      return vector;
    else {
      unread(c);
      vector.addElement(parse());
      return parseVector(vector, delimiter);
    }
  }

  /**
   * Parses an atom (a number or a symbol).
   * Since anything that is not a number is a symbol, syntax errors are not
   * possible.
   *
   * @exception SExprParserException not signalled but useful for the protocol
   * @exception IOException if an I/O problem occurred (e.g. end of file)
   */
  protected Object parseAtom(char c)
    throws SExprParserException, IOException {
    StringBuffer b = getScratchBuffer();
    do {
      b.append(c);
    } while (isAtomChar(c = (char)read(), false));
    unread(c);
    String s = b.toString();
    try {
      return makeNumber(s);
    }
    catch (NumberFormatException e) {
      return Symbol.makeSymbol(s, getSymbols());
    }
  }

  /**
   * Parses a double-quote -delimited string (sans the first character).
   * Please note: no escape-character interpretation is performed. Override
   * this method for any escape character handling.
   *
   * @exception SExprParserException not signalled but useful for the protocol
   * @exception IOException any I/O problem (including end of file)
   */
  public String parseString()
    throws SExprParserException, IOException {
    int code;
    StringBuffer b = getScratchBuffer();
    while (true) {
      switch (code = read()) {
        case (int)'"':
          return new String(b);
        case -1:
          throw new EOFException();
        default:
          b.append((char)code);
          break;
      }
    }
  }

  /**
   * Predicate function for checking if a character can belong to an atom.
   *
   * @param first if true means that c is the first character of the atom
   */
  protected boolean isAtomChar(char c, boolean first) {
    return !(Character.isWhitespace(c)
             || c == '(' || c == ')' || c == '"' || c == '}' || c == '{');
  }

  /**
   * Reads from the stream, skipping whitespace and comments.
   *
   * @exception IOException if an I/O problem occurred (including end of file)
   */
  public char readSkipWhite() 
    throws IOException {
    char c;
    do {
      c = (char)read();
      if (c == ';') // skip comments
        do {} while ((c = (char)read()) != '\n' && c != '\r');
      if (c == -1)
        throw new EOFException();
    } while (Character.isWhitespace(c));
    return c;
  }

  /**
   * Attempts to parse a number from the string.
   *
   * @exception NumberFormatException the string does not represent a number
   */
  protected Number makeNumber(String s)
    throws NumberFormatException {

    if (HEX.matcher(s).matches()) {
      // remove prefix and underscores
      s = s.replaceAll("0x","").replaceAll("_","");
      
      try {
	return Integer.valueOf(s, 16);
      } catch (NumberFormatException e) {
	return Long.valueOf(s, 16);
      }
    } else {

      try {
	return Integer.valueOf(s);
      } catch (NumberFormatException e) {
	try {
	  return Long.valueOf(s);
	} catch (NumberFormatException f) {
	  return Double.valueOf(s);
	}
      }
    }
  }

  /**
   * Associates a dispatch character with a parser in the read table.
   */
  public SExprParser addParser(char key, SExprParser parser) {
    return getReadtable().addParser(key, parser);
  }

  /**
   * Produces a printed representation of an s-expression.
   */
  public static void printExpr(Object expr, PrintStream out) {
    if (expr == null)
      out.print("nil");
    else if (expr instanceof Number)
      out.print(expr);
    else if (expr instanceof String) {
      out.print('"');
      out.print(expr);
      out.print('"');
    } else if (expr instanceof Vector) {
      out.print("(");
      for (int i = 0; i < ((Vector)expr).size(); i++) {
        if (i != 0)
          out.print(" ");
        printExpr(((Vector)expr).elementAt(i), out);
      }
      out.print(")");
    }
    else if (expr instanceof SExpr)
      ((SExpr)expr).printExpr(out);
    else
      out.print("#<unknown " + expr + ">");
  }


  /**
   * Produces a string representation of an s-expression.
   */
  public static String toString(Object expr) {
    if (expr == null)
      return "";
    if (expr instanceof Number)
      return expr.toString();
    if (expr instanceof String)
      return "\"" + (String)expr + "\"";
    if (expr instanceof URL)
      return "\"" + ((URL)expr).toString() + "\"";
    if (expr instanceof Date)
      return "\"" + ((Date)expr).toString() + "\"";
    if (expr instanceof Vector) {
      String s = "(";
      for (int i=0; i < ((Vector)expr).size(); i++) {
	if (i != 0)  s += " ";
	s += SimpleSExprStream.toString(((Vector)expr).elementAt(i));
      }
      return s += ")";
    }
    if (expr instanceof SExpr)
      return ((SExpr)expr).toString();
    // error
    return "(#<unknown " + expr.toString() + ">)";
  }
	
  /**
   * Produces a normative form of the string representation of 
   * an s-expression.
   */
  public String toNormativeForm(Object v) {
    return toString(v);
  }
	
  /**
   * Produces a normative form of the string representation of 
   * an s-expression.
   */
  public String prettyPrint(Object v) {
    return toString(v);
  }
	



  public static void main(String args[])
    throws SExprParserException, IOException {
    SExprStream p = new SimpleSExprStream(System.in);
    p.setListsAsVectors(true);
    HashMap map = new HashMap();
    map.put("bob", new Symbol("eddie"));
    p.setSymbols(map);


    Object e = p.parse();
    SimpleSExprStream.printExpr(e, System.out);
    System.out.println();

    System.out.println(" Symbols "+p.getSymbols());
    
    ForVisitor fv = new ForVisitor();
    fv.visit(e);


    System.out.println(SimpleSExprStream.toString(e));
    System.out.println();
  }

}

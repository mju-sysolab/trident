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
 *  Cons.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  THIS IS VERY BROKEN RIGHT NOW.  NOT SURE IF IT IS WORTH IT TO FIX IT.
 * 
 *  Author: Ora Lassila
 *
 *  $Id$
 */

package fp.util.sexpr;

import java.io.PrintStream;
import java.util.Enumeration;

/**
 * Basic class for implementing linked lists a la Lisp.
 */
public class Cons implements SExpr {

  private Object car;
  private Object cdr;

  /**
   * Initializes a Cons cell with the left and right "subtrees".
   */
  public Cons(Object left, Object right)
  {
    this.car = left;
    this.cdr = right;
  }

  /**
   * Initializes a Cons cell with a left subtree only.
   * Right subtree will be set to <tt>null</tt>.
   */
  public Cons(Object left)
  {
    this.car = left;
    this.cdr = null;
  }

  /**
   * Returns the left subtree (i.e. the head) of a cons cell.
   */
  public Object left()
  {
    return car;
  }

  /**
   * Returns the right subtree (i.e. the tail) of a cons cell.
   */
  public Object right()
  {
    return cdr;
  }

  /**
   * Returns the tail of a cons cell if it is a list.
   * Signals an error otherwise (no dotted pairs allowed).
   *
   * @exception SExprParserException if the tail is not a Cons or <tt>null<tt>
   */
  public Cons rest()
    throws SExprParserException
  {
    Object r = right();
    if (r == null)
      return null;
    else if (r instanceof Cons)
      return (Cons)r;
    else
      throw new SExprParserException("No dotted pairs allowed");
  }

  /*
   * Returns an enumeration of the elements of the list.
   */
  public Enumeration elements()
  {
    return new ConsEnumeration(this);
  }

  public void printExpr(PrintStream stream)
  {
    printList(stream, true);
  }

  private void printList(PrintStream out, boolean first)
  {
    out.print(first ? "(" : " ");
    SimpleSExprStream.printExpr(left(), out);
    Object r = right();
    if (r == null)
      out.print(")");
    else if (r instanceof Cons)
      ((Cons)r).printList(out, false);
    else {
      out.print(". ");
      SimpleSExprStream.printExpr(r, out);
      out.print(")");
    }
  }

  public String toString()
  {
    return toString(true);
  }

  private String toString(boolean first)
  {
    String s = new String(first ? "(" : " ");
    s += SimpleSExprStream.toString(left());
    Object r = right();
    if (r == null)
      s += ")";
    else if (r instanceof Cons)
      s += ((Cons)r).toString(false);
    else {
      s += ". ";
      SimpleSExprStream.toString(r);
      s += ")";
    }
    return s;
  }

}

class ConsEnumeration implements Enumeration {

  private Cons current;

  public ConsEnumeration(Cons head)
  {
    this.current = head;
  }

  public boolean hasMoreElements()
  {
    return current != null;
  }

  public Object nextElement()
  {
    Object element = null;
    try {
      element = current.left();
      current = current.rest();
    }
    catch (SExprParserException e) {
      // current is a dotted pair
      current = null;
    }
    return element;
  }

}

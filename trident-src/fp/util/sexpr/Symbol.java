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
 *  Symbol.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Andreas Sterbenz: added hashCode() and equals()
 *
 *  Author: Ora Lassila
 *
 *  $Id$
 */

package fp.util.sexpr;

import java.io.PrintStream;
import java.util.Map;

/**
 * Base class for lisp-like symbols.
 */
public class Symbol implements SExpr {

  private String name;

  /**
   * Creates a symbol and potentially interns it in a symbol table.
   */
  public static Symbol makeSymbol(String name, Map symbols)
  {
    if (symbols == null)
      return new Symbol(name);
    else {
      String key = name.toLowerCase();
      Symbol s = (Symbol)symbols.get(key);
      if (s == null) {
        s = new Symbol(name);
        symbols.put(key, s);
      }
      return s;
    }
  }

  public Object clone()
  {
    return new Symbol(name);
  }


  protected Symbol(String name)
  {
    this.name = name;
  }

  public String toString()
  {
    return name;
  }

  public void printExpr(PrintStream out)
  {
    out.print(toString());
  }

  public int hashCode()
  {
    return name.hashCode();
  }

  public boolean equals(Object obj)
  {
    if ((obj != null) && (obj instanceof Symbol)) {
      String othername = ((Symbol)obj).toString();
      return this.name.equals(othername);
    }
    return false;
  }
}

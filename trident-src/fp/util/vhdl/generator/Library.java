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

import java.util.*;

class Library {
  private HashSet _libraries;
  private HashSet _uses;

  Library(String lib) {
    _libraries = new HashSet();
    if (lib != null) {
      addLibrary(lib);
    }
    _uses = new HashSet();
  }

  public void addLibrary(String lib) {
    _libraries.add(lib);
  }

  public void addUse(String use) {
    _uses.add(use);
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer("library ");
    int lib_count = _libraries.size();
    for(Iterator iter = _libraries.iterator(); iter.hasNext(); ) {
      String lib = (String)iter.next();
      if (lib_count > 1) {
	sbuf.append(lib);
	sbuf.append(", ");
      } else {
	sbuf.append(lib);
	sbuf.append(";\n");
      }
      lib_count--;
    }
    
    for(Iterator iter = _uses.iterator(); iter.hasNext(); ) {
      String use = (String)iter.next();
      sbuf.append("use ");
      sbuf.append(use);
      sbuf.append(".all");
      sbuf.append(";\n");
    }
    
    return sbuf.toString();
  }

}


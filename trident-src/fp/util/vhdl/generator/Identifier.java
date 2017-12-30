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

public class Identifier implements VHDLout, Choice {


  public static final Identifier OTHERS = new Identifier("others");

  // This should check to see if it is legal.
  // this can be letter{"_",letter,digit}
  String _ident;

  public Identifier(String ident) {
    // we check and if it is illegal we throw an exception?
    _ident = ident;
  }

  String getId() { return _ident; }


  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre).append(_ident);
    return s;
  }
  
  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }


   public static void main(String args[]) {
     checkId("Bob");
     checkId("B_56ABab");
     // illegal
     checkId("5_abcde");
     checkId("abcde%");
     checkId("abcde.");
  }

  public static void checkId(String name) {
    System.out.println("Identifier "+(new Identifier(name)));
  }

   
}

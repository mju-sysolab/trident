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

public class BitStringLiteral extends Literal {
  public static final String B = "B";
  public static final String O = "O";
  public static final String X = "X";

  private String _base;
  private String _value;

  public BitStringLiteral(String base, String value) {
    super();
    // this can be B, O or X
    _base = base;

    // this 0-9A-Fa-f atleast
    _value = value;

    _string = _base + "\"" +_value +"\"";

  }


  
  
  public static void main(String args[]) {
    Literal l = new BitStringLiteral(B,"1010101");
    System.out.println("BitString "+l);
    l = new Char('1');
    System.out.println("EnumLiteral "+l);
    l = new Char('0');
    System.out.println("EnumLiteral "+l);
  }



}

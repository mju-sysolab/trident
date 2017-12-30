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

public class EnumerationType implements VHDLout, TypeDefinition {

  LinkedList _literals;


  public EnumerationType() {
    _literals = new LinkedList();
  }

  public EnumerationLiteral getEnum(Identifier id) {
    for(ListIterator list_iter = _literals.listIterator(); 
	list_iter.hasNext(); ) {
      EnumerationLiteral literal = (EnumerationLiteral)list_iter.next();
      if (id == literal.getId()) {
	return literal;
      }
    }
    return null;
  }

  public void addEnums(Identifier[] id) {
    for(int i=0;i < id.length; i++) {
      _literals.add(new EnumerationLiteral(id[i]));
    }
  }

  public void addEnum(Identifier id) {
    _literals.add(new EnumerationLiteral(id));
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    // label top.

    s.append(pre);
    int count = 0;
    for(ListIterator list_iter = _literals.listIterator(); list_iter.hasNext(); ) {
      Literal literal = (Literal)list_iter.next();
      
      if (count == 0) 
	s.append("( ");
       else 
	s.append(", ");
      literal.toVHDL(s,"");
      count++;
    }
    s.append(" )"); 
    
    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }



}  

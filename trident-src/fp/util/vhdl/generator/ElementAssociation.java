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


public class ElementAssociation implements VHDLout {

  public static final ElementAssociation OTHERS_IS_ZERO = 
    new ElementAssociation(Identifier.OTHERS, Expression.ZERO);
  public static final ElementAssociation OTHERS_IS_ONE = 
    new ElementAssociation(Identifier.OTHERS, Expression.ONE);
  
  private ChoiceList _choices;
  private Expression _expression;

  public ElementAssociation(ChoiceList choices, Expression expression) {
    if (choices == null) 
      _choices = new ChoiceList();
    else 
      _choices = choices;
    if (expression == null) 
      throw new VHDLException(" ElementAssociation with null expression "+null);
    _expression = expression;
  }

  public ElementAssociation(Object choice, Expression expression) {
    this(null, expression);
    _choices.add(choice);
  }


  public ElementAssociation(Expression expression) {
    this(null, expression);
  }


  public void addChoice(Object choice) {
    _choices.add(choice);
  }

  
  public StringBuffer toVHDL(StringBuffer s, String pre) {

    if (_choices.size() > 0) {
      _choices.toVHDL(s,"");
      s.append(" => ");
    }

    _expression.toVHDL(s, "");
    return s;
  }


  public String toString() {
    return toVHDL(new StringBuffer(), "").toString();
  }

}  



  

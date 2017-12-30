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


public class Architecture extends Block {
  
  // block_declaritive_item
  // Concurrent Statements

  // Contains BlockItem objects (no duplicates)
  private HashSet _itemSet = new HashSet();

  // Contains names of identifiers (changed to lower case) from BlockItem objects (no duplicates)
  private HashSet _nameSet = new HashSet();

  private LinkedList _statements = new LinkedList();

  // this part could be fixed.  It is the proper way to do it.
  Identifier _identifier;
  Name _entity;


  private String _real_name;

  public Architecture(String name) {
    super(name+"_arch","architecture");
    _real_name = name;
    _itemSet = new HashSet();
    _nameSet = new HashSet();
    _statements = new LinkedList();
  }

  // Add an item to the HashMap if one isn't there already.
  // Make strings all lower case to make search possible.
  public void addItem(BlockItem i) { 
    LinkedList itemNames = i.getNames();
   
    // check for any duplicate names first 
    for (ListIterator iter = itemNames.listIterator(); iter.hasNext(); ) {
      String itemName = (String)iter.next();
      if (_nameSet.contains(itemName.toLowerCase())) {
	// jt - we don't need this warning since it does not tell us much
	// these duplications will happen.
	//System.out.println("CODEGEN:  Found a duplicate name:" + itemName);
	return;
      } 
    }

    // go ahead and add names
    for (ListIterator iter = itemNames.listIterator(); iter.hasNext(); ) {
      String itemName = (String)iter.next();
      _nameSet.add(itemName.toLowerCase());
    } 

    // make sure BlockItem is added
    _itemSet.add(i);
  }

  HashSet getItems() { return _itemSet; }

  public void addStatement(Statement s) { _statements.add(s); }
  public void addStatementComment(String c) { _statements.add(new Comment(c)); }
  LinkedList getStatements() { return _statements; }

  protected void appendName(StringBuffer sbuf, String pre) {
    sbuf.append(pre).append(getType()).append(" ");
    sbuf.append(getName()).append(" of ").append(_real_name);
    sbuf.append(" is\n");
  }

  protected void appendBody(StringBuffer s, String pre) {
    // group them in this order: component, constant, type and signal

    for (Iterator iter = _itemSet.iterator(); iter.hasNext(); ) {
      BlockItem bItem = (BlockItem)iter.next();
      if (bItem instanceof Component) {
	((VHDLout)bItem).toVHDL(s, pre+TAB);
      }
    }
    
    for (Iterator iter = _itemSet.iterator(); iter.hasNext(); ) {
      BlockItem bItem = (BlockItem)iter.next();
      if (bItem instanceof ConstantItem) {
	((VHDLout)bItem).toVHDL(s, pre+TAB);
      }
    }
    
    for (Iterator iter = _itemSet.iterator(); iter.hasNext(); ) {
      BlockItem bItem = (BlockItem)iter.next();
      if (bItem instanceof TypeDeclaration) {
	((VHDLout)bItem).toVHDL(s, pre+TAB);
      }
    }
    
    for (Iterator iter = _itemSet.iterator(); iter.hasNext(); ) {
      BlockItem bItem = (BlockItem)iter.next();
      if (bItem instanceof Signal) {
	((VHDLout)bItem).toVHDL(s, pre+TAB);
      }
    }
    
    s.append(pre).append("-- begin architecture for ").append(_real_name).append("\n");
    s.append(pre).append("begin\n");
    for (ListIterator iter = _statements.listIterator(); iter.hasNext(); ) {
      ((VHDLout)iter.next()).toVHDL(s, pre+TAB);
    }

  }

}

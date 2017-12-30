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


package fp.passes;

import fp.flowgraph.*;

import java.util.*;

public class CheckBlockNames extends Pass implements GraphPass {
  /**
   * This class checks all of the block names and fixes those that are 
   * VHDL reserved words or contain a '.'.
   */

  // Array of VHDL reserved words.
  private static String[] reservedArray = { 
    "abs", "access", "after", "alias", "all", 
    "and", "architecture", "array", "assert", 
    "attribute", "begin", "block", "body", 
    "buffer", "bus", "case", "component", 
    "configuration", "constant", "disconnect", 
    "downto", "else", "elsif", "end", "entity", 
    "exit", "file", "for", "function", 
    "generate", "generic", "group", "guarded", 
    "if", "impure", "in", "inertial", "inout", 
    "is", "label", "library", "linkage", 
    "literal", "loop", "map", "mod", "nand", 
    "new", "next", "nor", "not", "null", "of", 
    "on", "open", "or", "others", "out", 
    "package", "port", "postponed", "procedure", 
    "process", "pure", "range", "record", 
    "register", "reject", "rem", "report", 
    "return", "rol", "ror", "select", 
    "severity", "shared", "signal", "sla", 
    "sll", "sra", "srl", "subtype", "then", 
    "to", "transport", "type", "unaffected", 
    "units", "until", "use", "variable", "wait", 
    "when", "while", "with", "xnor", "xor"
  };

  private static HashSet reservedHash = 
    new HashSet(Arrays.asList(reservedArray));

  public CheckBlockNames(PassManager pm) {
    super(pm);
  }

  public boolean optimize(BlockGraph graph) {
    // Gather block names...
    HashSet blkNames = gatherBlockNames(graph);

    // For each block node...
    for(Iterator bIt = graph.getAllNodes().iterator(); bIt.hasNext(); ) {
      // If the block node's name has any periods, then rename it.
      BlockNode bn = (BlockNode) bIt.next();
      String oldName = bn.getLabel().getFullName();
      boolean error = true;

      // If this block name is a VHDL reserved word...
      if(isNameReserved(oldName)) {
	// Get a new name for this block!
        String newName = oldName;
	for(int i = 0; i < 1000000; i++)
	  if(!blkNames.contains(oldName+i)) {
	    newName = oldName+i;
	    error = false;
	    break;
	  }

	if(error)
	  throw new CheckBlockNamesException("New block name not found!");

	bn.setName(newName);  // Replace with new name.
      }

      if(oldName.indexOf('.') != -1) { // If there's an occurance of '.'
	String newName = oldName.replace('.', '_');
	bn.setName(newName);           // Then rename it.
      }
    }
    return true;
  }

  /**
   * Return a set of all block names in the block graph.
   */
  private HashSet gatherBlockNames(BlockGraph graph) {
    HashSet set = new HashSet();
    for(Iterator bIt = graph.getAllNodes().iterator(); bIt.hasNext(); ) {
      BlockNode bn = (BlockNode) bIt.next();
      set.add(bn.getLabel().getFullName());
    }
    return set;
  }

  /**
   * Return true if the specified name is a VHDL reserved word.
   */
  public static boolean isNameReserved(String name) {
    return reservedHash.contains(name);
  }

  public String name() { return "CheckBlockNames"; }


  private class CheckBlockNamesException extends RuntimeException {
    public CheckBlockNamesException() {
      super();
    }
    public CheckBlockNamesException(String message) {
      super("\n"+message);
    }
  }

}

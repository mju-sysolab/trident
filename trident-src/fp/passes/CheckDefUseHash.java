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
import fp.util.UseHash;
import fp.util.DefHash;
import java.util.*;
 
public class CheckDefUseHash extends Pass implements BlockPass {
 
  /*
    The purpose of this pass is to check that the Def Use
    Hash is consistent.
  */
   
 
  public CheckDefUseHash(PassManager pm) {
    super(pm);
  }

public class HashComparator implements Comparator{

    public HashComparator(){};

    public int compare (Object o1, Object o2) {
      // can't let it think they're equal or it won't add an object
      // with the same weight as one that is already in the tree
      int comparison = (((Operand)o1).toString().compareTo(((Operand)o2).toString()));
      if (comparison == 0) {
	return 1;
      } else
	return (comparison);
    }

    public boolean equals (Object o1, Object o2) {
      int comparison = (((Operand)o1).toString().compareTo(((Operand)o2).toString()));
      return (comparison == 0);
    }
  }

  private void printDefHash(HashMap hm, String title) {
    Set keySet = hm.keySet();
    SortedSet sortedKeySet = new TreeSet(new HashComparator());
    sortedKeySet.addAll(keySet);

    System.out.println(title);

    for(Iterator iter = sortedKeySet.iterator(); iter.hasNext(); ) {
      Operand o = (Operand) iter.next(); 
      Instruction inst = (Instruction)(hm.get(o));
      System.out.println("DEF OPERAND: " + o.toString() + " DEF INSTRUCTION: " + inst.toString());
    }
  }

  private void printUseHash(HashMap hm, String title) {
    Set keySet = hm.keySet();
    SortedSet sortedKeySet = new TreeSet(new HashComparator());
    sortedKeySet.addAll(keySet);

    System.out.println(title);

    for(Iterator iter = sortedKeySet.iterator(); iter.hasNext(); ) {
      Operand o = (Operand) iter.next(); 
      System.out.println("USE OPERAND: " + o.toString() );
      ArrayList list =  (ArrayList)hm.get(o);

      for (Iterator iter2 = list.iterator(); iter2.hasNext();) {
	Instruction inst = (Instruction)iter2.next();
	System.out.println("   USE INSTRUCTION: " +  inst.toString());
      }
    }
  }

 
  public boolean optimize(BlockNode node) {
    DefHash oldDefHash = node.getDefHash();
    UseHash oldUseHash = node.getUseHash();
    DefHash newDefHash = new DefHash();  
    UseHash newUseHash = new UseHash();  

    ArrayList list = node.getInstructions();

    for(Iterator iter = list.iterator(); iter.hasNext(); ) {
      Instruction inst = (Instruction) iter.next(); 

      // Hash the instruction into the newDefHash and newUseHash
      newDefHash.add(inst);
      newUseHash.add(inst);
    }


    //printDefHash(oldDefHash, "CURRENT DEFS");
    //printDefHash(newDefHash, "GENERATED DEFS");
    if (oldDefHash.size() != newDefHash.size()) {
      System.out.println("HASH WARNING:  DEF HASH IS NOT CORRECT");
    }
    //printUseHash(oldUseHash, "CURRENT USES");
    //printUseHash(newUseHash, "GENERATED USES");
    if (oldUseHash.size() != newUseHash.size()) {
      System.out.println("HASH WARNING:  USE HASH IS NOT CORRECT");
    }

    return true;
  }
 
  public String name() {
    return "CheckDefUseHash";
  }
 
 
}


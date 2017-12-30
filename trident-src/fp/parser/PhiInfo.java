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


package fp.parser;
import fp.flowgraph.Operand;
import fp.flowgraph.IllegalOperand;
import fp.flowgraph.Type;
import fp.flowgraph.Instruction;
import fp.flowgraph.Phi;
import fp.parser.Varid;
import java.util.*;

class PhiInfo {
  // list of pairs (phi instruction, list of vals to patch)
  public final static LinkedList patchList = new LinkedList();

  public int index;
  public String basename, origname;
  public int version;
  
  public PhiInfo(int ind, String oname, String bname, int vers ) {
	index = ind;
	origname = oname;
	basename = bname;
	version  = vers;
  }

  public String toString() {
    return ("PhiInfo[" + index + ", " + origname + ", " + basename + ", " + version + "]");
  }

  public static void patch() throws IllegalOperand {
    for(ListIterator iter = patchList.listIterator(); iter.hasNext(); ) {
      Instruction phiI = (Instruction)iter.next();
      //System.out.println("Phi inst patched:" + phiI.toString());
      LinkedList phiInfoList = null;
      if (iter.hasNext()) {
        phiInfoList = (LinkedList)iter.next();
      } else continue;

      for (ListIterator iter2 = phiInfoList.listIterator(); iter2.hasNext(); ) {
	PhiInfo pi = (PhiInfo)iter2.next();
        //System.out.println(pi.toString());
	// I don't know if it's a primal or not, so don't know how to do the lookup!
	// We try for a nonprimal first, then primal (based on the original name)
	Operand o = null;
	o = Operand.getOperand(pi.basename, pi.version);
	if (o == null) {
	  o = Operand.getOperand(pi.origname, Operand.NOT_ASSIGNED);
	}
	if (o == null) {
	 //throw IllegalOperand exception 
	 System.err.println("Operand for phi operation not set anywhere: " + pi.basename + pi.version + "...  In phi instruction: " + phiI.toString());
	 //throw new IllegalOperand("Operand for phi operation not set anywhere: " + pi.basename + pi.version + "...  In phi instruction: " + phiI.toString());
	} else {
	  Phi.setVal(phiI, pi.index, o, Phi.getValLabel(phiI, pi.index));
	}
      }
    }
  }

}


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

import java.util.*;

import fp.flowgraph.*;
import fp.util.BooleanEquation;
import fp.util.UseHash;
import fp.util.Bool;




public class CSERemoval extends Pass implements BlockPass {
  
  /*
    This is fairly limited CSE analysis -- it only looks for a pair of 
    binary operations that match.  It does not build anything more fancy and
    look for larger matches.

    For example:
    
    c = a + b;        -->   tmp = a + b;
    d = a + b;                c = tmp;
                              d = tmp;
    However,
    
    t1 = a + b;
    c = t1 + f;
    t2 = a + f;
    d = t2 + b;
    
    does not change at all.
			      
  */


  public CSERemoval(PassManager pm) {
    super(pm);
  }

  public String name() { return "CSERemoval"; }

  public boolean optimize(BlockNode node) {
    
    /* sorry, mate, but I needed to use this so made a separate class
     * to hold the code
     */

    CSERemovalCode theCSErminator = new CSERemovalCode();
    return theCSErminator.removeEmCSEs(node);
  }
  

}


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

import fp.*;
import fp.flowgraph.*;
import fp.util.*;

//This pass looks for temp operands that are used in multiple blocks, and converts them to primals


/** This is pass probably needs to be changed, but it looks for nonprimals that 
 *  are used in one block and defined in another and then it promotes these 
 *  nonprimals to primal, unless the two blocks in which they exist would later 
 *  be merged together.
 * 
 * @author Kris Peterson
 */
public class ConvInterBlockNonPrimToPrimal extends Pass implements GraphPass {
   
   
  /** ignore if there are interiteration loop dependencies and schedule using 
   *  force directed anyway?
   */
  //private boolean _ignoreInterItDataDep;
  private HyperBlockList _hyperBlocks;
  
  public ConvInterBlockNonPrimToPrimal(PassManager pm, HyperBlockList hyperBlocks) {
    super(pm);
    _hyperBlocks = hyperBlocks;
  }

  public boolean optimize(BlockGraph graph) {
     PrimalPromotion promotPrims = new PrimalPromotion(graph);
     
     promotPrims.primalPromotion(_hyperBlocks);
     return true;
  }
  
  public String name() { 
    return "ConvInterBlockNonPrimToPrimal";
  }
   
}


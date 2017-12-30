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
import fp.hardware.*;

  /**
   * This class locates all getelementptr instructions and converts them to 
   * instructions that calculate the address to be accessed.
   */
public class ConvertGepInst extends Pass implements GraphPass {
  private OperationSelection _opSel;

  public ConvertGepInst(PassManager pm, OperationSelection opSel) {
    super(pm);
    _opSel = opSel;
  }

  // Find each GEP instruction and convert it...
  public boolean optimize(BlockGraph graph) {
    ConvertGepInstructions convert = new ConvertGepInstructions(_opSel);
    convert.gepConversion(graph);
    return true;
  }

  public String name() {
    return "ConvertGepInst";
  }
}

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

import java.io.*;

import fp.flowgraph.BlockGraph;

/**
  * The Wait pass is a debugging tool to wait until the user presses 
  * the return key before continuing execution.  This should not 
  * normally be committed as a required pass.
  * <p>
  * Unfortunately this doesn't work under ant test because System.in 
  * doesn't seem to be attached to the keyboard.
  * @author Neil Steiner
  */
public class Wait extends Pass implements GraphPass {

  /**
    * Constructor.
    * @param pm the PassManager object.
    */
  public Wait(PassManager pm) { super(pm); }

  /** Returns the name of this optimizer. */
  public String name() { return "Wait"; }

  public boolean optimize(BlockGraph graph) {

    // some of these objects may throw exceptions
  	try { new BufferedReader(new InputStreamReader(System.in)).readLine(); }

    // if that happens, we'll just ignore them
	catch(IOException ioe) {}

    // tell the PassManager that all is well
    return true;
  }

}



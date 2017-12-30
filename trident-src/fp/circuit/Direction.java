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
package fp.circuit;

public interface Direction {
  public static final int IN    = 1;
  public static final int OUT   = 2;
  public static final int INOUT = 3;

  public static final int _TOP = 1;
  public static final int _CELL = 2;
  public static final int _MODULE_GROUP = 3;
  public static final int _DATA_PATH = 4;

  public static final int _TOP_INPUT = 1;
  public static final int _LOCAL_INPUT = 2;
  
}

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


package fp.hwdesc;

public class MemAccessInst extends Base {
  public int latency = 0;
  public float area = 0;
  public int sliceCnt = 0;
  
  // instruction types
  public static final int ALOAD = 0;
  public static final int ASTORE = 1;


  public MemAccessInst() { 
    typeName = new String[] {
      "aload",
      "astore"
    };
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer("(memAccessInst ");
    sbuf.append(type).append("\n");
    
    sbuf.append("(name ").append(name).append(" )\n");
    sbuf.append("(latency ").append(latency).append(" )\n");
    sbuf.append("(area ").append(area).append(" )\n");
    sbuf.append("(sliceCnt ").append(sliceCnt).append(" )\n");

    sbuf.append(")\n");
    
    return sbuf.toString();
  }
    
}

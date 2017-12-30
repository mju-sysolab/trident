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

public class Resource extends Base {
  public int dimension1 = 0;
  public int dimension2 = 0;
  public int address = 0;

  // resource types

  public static final int NONE = 0;
  public static final int BLOCKRAM_TYPE = 1;
  public static final int MULTIPLIER_TYPE = 2;
  public static final int PROCESSOR_TYPE = 3;
  public static final int DSPBLOCK_TYPE = 4;
  public static final int LUTRAM_TYPE = 5;
  public static final int SRL16_TYPE = 6;
  public static final int REGISTER_TYPE = 7;

  public Resource() {
    typeName = new String[] {
      "none",
      "blockram",
      "multiplier",
      "processor",
      "dspblock",
      "lutram",
      "srl16",
      "register",
    };
  }
 

  public String toString() {
    StringBuffer sbuf = new StringBuffer("(resource ");
    sbuf.append(type).append("\n");
    sbuf.append("(count ").append(count).append(" )\n");
    sbuf.append("(name \"").append(name).append("\" )\n");

    sbuf.append("(address 0x").append(Integer.toHexString(address)).append(" )\n");
    sbuf.append("(size ").append(size).append(" )\n");
    sbuf.append("(width ").append(width).append(" )\n");
    sbuf.append("(depth ").append(depth).append(" )\n");
    sbuf.append("# dimensions is not really implemented\n");
    sbuf.append("(dimensions ").append(dimension1).append(" )\n");

    sbuf.append(")\n");
    
    return sbuf.toString();
  }
    
 
}

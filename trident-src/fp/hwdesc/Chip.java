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

import java.util.*;

public class Chip extends Base {
  // these apply to fpga type chips
  public float area_max = 0;
  public float area_min = 0;
  public int area = -1;
  public int address = -1;
  public int addressable_size = -1;

  public ArrayList resource;  	// list of Resource objects if chip is FPGA
  public Class resourceClass = Resource.class;

  // these apply to ram or rom type chips
  public ArrayList port;    	// or list of Port objects if chip is RAM or ROM
  public Class portClass = Port.class;

  // these apply to ram or rom type chips and are the ALoad and AStore instructions
  public ArrayList memAccessInst;    	// or list of MemAccessInst objects if chip is RAM or ROM
  public Class memAccessInstClass = MemAccessInst.class;

  // chip types
  public static final int  NONE = 0;
  public static final int  FPGA_TYPE = 1;
  public static final int  RAM_TYPE = 2;
  public static final int  ROM_TYPE = 3;

  
  //this is used for calculating port usage:
  //private PortUsageSplitter _portUseSplit;
  
  public Chip() {
    resource = new ArrayList();
    port = new ArrayList();
    memAccessInst = new ArrayList();
    typeName = new String[] {
      "none",
      "fpga",
      "ram",
      "rom"
    };
    //_portUseSplit =  new PortUsageSplitter();
  }
 
 /* public boolean addLoad(int time) {
    return _portUseSplit.addLoad(time);
  }

  public boolean addStore(int time) {
    return _portUseSplit.addStore(time);
  }*/

  public String toString() {
    StringBuffer sbuf = new StringBuffer("(chip ");
    sbuf.append(type).append("\n");
    sbuf.append("(count ").append(count).append(" )\n");
    sbuf.append("(name \"").append(name).append("\" )\n");

    if (typeCode == RAM_TYPE || typeCode == ROM_TYPE) {
      sbuf.append("(size ").append(size).append(" )\n");
      sbuf.append("(width ").append(width).append(" )\n");
      sbuf.append("(depth ").append(depth).append(" )\n");
      sbuf.append("(address 0x").append(Integer.toHexString(address)).append(" )\n");
    }

    if (typeCode == FPGA_TYPE) {
      sbuf.append("(area_min ").append(area_min).append(" )\n");
      sbuf.append("(area_max ").append(area_max).append(" )\n");
      sbuf.append("(area ").append(area).append(" )\n");
    }

    for(Iterator iter=port.iterator(); iter.hasNext(); ) {
      sbuf.append("\n");
      sbuf.append(((Port)iter.next()).toString());
    }

    for(Iterator iter=resource.iterator(); iter.hasNext(); ) {
      sbuf.append("\n");
      sbuf.append(((Resource)iter.next()).toString());
    }

    sbuf.append(")\n");
    
    return sbuf.toString();
  }
    
  
}



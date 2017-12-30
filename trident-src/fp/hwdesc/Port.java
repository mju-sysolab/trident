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

public class Port extends Base {
  public int read_latency = 0;
  public int write_latency = 0;
  public int clock = 0;
  
  private PortUseCnt _portUseCnter;
  
  // port types
  public static final int NONE = 0;
  public static final int DATA_READ_TYPE = 1;
  public static final int DATA_WRITE_TYPE = 2;
  public static final int DATA_RW_TYPE = 3;
  public static final int ADDRESS_READ_TYPE = 4;
  public static final int ADDRESS_WRITE_TYPE = 5;
  public static final int ADDRESS_RW_TYPE = 6;


  public Port() { 
    typeName = new String[] {
      "none",
      "data_read",
      "data_write",
      "data_rw",
      "address_read",
      "address_write",
      "address_rw"
    };
    _portUseCnter = new PortUseCnt();
  }

  public void resetPortUseCnter() {
    _portUseCnter = new PortUseCnt(); 
    _portUseCnter.clear();
    //System.out.println("_portUseCnter.size() " + _portUseCnter.size());
  }
  public PortUseCnt getPortUseCnter() {return _portUseCnter;}
  
  public String toString() {
    StringBuffer sbuf = new StringBuffer("(port ");
    sbuf.append(type).append("\n");
    sbuf.append("(count ").append(count).append(" )\n");
    sbuf.append("(name \"").append(name).append("\" )\n");

    sbuf.append("(clock ").append(clock).append(" )\n");
    sbuf.append("(read_latency ").append(read_latency).append(" )\n");
    sbuf.append("(write_latency ").append(write_latency).append(" )\n");

    sbuf.append(")\n");
    
    return sbuf.toString();
  }
    



}

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

class PassStat {

  long millis = 0;
  String name = "";
  
  PassStat(String n, long i) {
    name = n;
    millis = i;
  }

  public String toString(long total) {
    int result = (int)((millis*10000) / total);
    StringBuffer buf = millisPercent(result);

    StringBuffer sbuf = pad(name,30);

    return "  "+sbuf+"  "+buf+"%";
  }
   
  static StringBuffer millisPercent(int result) {
    int result_h = result / 100;
    int result_f = result % 100;
    StringBuffer buf = new StringBuffer();
    if (result_h < 10) buf.append("0");
    buf.append(result_h).append(".");
    if (result_f < 10) buf.append("0");
    buf.append(result_f);
    return buf;
  }

  static StringBuffer pad(String input, int size) {
    StringBuffer result = new StringBuffer(input);
    while(result.length() < size) {
      result.append(" ");
    }
    return result;
  }


}


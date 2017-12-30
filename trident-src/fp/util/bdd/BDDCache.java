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

package fp.util.bdd;

public class BDDCache {

  int tablesize;
  BDDCacheData[] table;

  BDDCache(int size) {
    int n;

    table = new BDDCacheData[size];
    
    for(n=0; n < size; n++) {
      table[n] = new BDDCacheData();
      table[n].a = null;
    }
    tablesize = size;
  }

  void reset() {
    for(int n=(tablesize-1); n>=0; n--) table[n].a = null;
  }
  
  
  BDDCacheData lookup(int hash) {
    return table[hash % tablesize];
  }
  

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    for(int n = 0; n < tablesize - 1; n++) {
      sbuf.append("[").append(n).append("]").append(table[n]).append("\n");
    }

    return sbuf.toString();
  }
  
}







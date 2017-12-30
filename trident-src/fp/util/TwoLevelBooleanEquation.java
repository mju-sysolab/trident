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

package fp.util;

import java.util.LinkedList;
import java.util.ListIterator;


public class TwoLevelBooleanEquation extends LinkedList {
  


  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    
    LinkedList[] lists = (LinkedList[])this.toArray( new LinkedList[this.size()] );
    
    for( int i = 0; i < lists.length; i++ ) {
      BooleanOp[] terms = (BooleanOp[])lists[i].toArray( new BooleanOp[lists[i].size()] );
      if( terms.length == 0 ) {
	sbuf.append("empty ");
      } else {
	for( int j = 0; j < terms.length; j++ ) {
	  sbuf.append(terms[j].toString());
	  sbuf.append(" ");
	}
      }
      if( i != lists.length-1 ) {
	sbuf.append( "OR " );
        }
    }
    return sbuf.toString();
  }


}

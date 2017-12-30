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



extern int a, b, c, d;

void run() {
  int local_a = 0;

  if (a == 0) {
    local_a = 1;
    if (b == 0) {
      b = 2;
      if (c == 0) {
	c = 3;
	if (d == 0) {
	  d = 4;
	} else {
	  d = 5;
	}
      } else {
	c = 6;
      }
    } else {
      b = 7;
    }
  } else {
    a = 8;
  }

  if (local_a == 0) {
    a = 1; 
    if( b == 0 ) { 
      b = 2; 
      if( c == 0 ) { 
	c = 3; 
	if( d == 0 ) { 
	  d = 4; 
	} else { 
	  d = 5; 
	} 
      } else { 
	c = 6; 
      } 
    } else { 
      b = 7; 
    } 
  } else { 
    a = 8;
  } 
}  

  

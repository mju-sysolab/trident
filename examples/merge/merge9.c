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


extern float a,b,i;

void run() {
  i = 1.0;
  
  if (a < 0.0) {
    i = 2.0;
    if (b < 1.0) 
      i = 3.0;
    else
      i = 4.0;
  } else {
    i = 5.0;
    if (b > 1.0) 
      i = 6.0;
    else
      i = 7.0;
  }
  i = 8.0;

}


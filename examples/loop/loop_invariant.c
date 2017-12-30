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


extern int a, b, c, d, e;

void run() {
  a = 6;
  b = 5; 
  int i;
  for(i=0; i<10; i++) {
    d = i;
    a = c + a;
  }

  for(i=0; i < 10; i++) {
    e = 5;
    if (c < d) 
      d = a + b + 5;
    else
      c = a + 2 + b;
  }
  d++;
  e++;
} 


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


float N=1;
float P = 0;
float K[100], L[100], M[100], O[100];
void run() {
  int i;
  int I=6 ;
  for (i = 0; i < I; i++) {
    N=  (K[i]*L[i] + M[i]*N)*O[i];
    P = N + P;
  }
}

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


extern float N, P;
extern int i;
extern float K[3][4][5]; 
extern float L[3][4][5];
extern float M[3][4][5];
extern float O[3][4][5];
void run() {
  N=  (K[1][2][i]*L[i][2][3] + M[1][i][2]*N)*O[i][0][2];
  P = N + P;
}

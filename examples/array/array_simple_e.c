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


extern int i, h;
extern int N, P, M, Q, J, R, S, T;
extern  int K[5]; 
extern  int L[5];
void run() {
  K[i] = N;
  L[i] = P;
  M = K[i];
  Q = L[i];
  K[h] = J;
  L[h] = R;
  S = K[h];
  T = L[h];
}

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


//extern float a[5], b, c;
extern float a[5], b[5], c;

void run() {
  int i;
  
  for(i=0;i<c;i++)
  {
    //should not have an edge from 
    //astore to getelementptr:
    //a[i]= b + 5;
    //should see connection between 
    //aload and astore of array a:
    //a[i]= a[i] + 5;
    //should not see connection between 
    //aload and astore of array a:
    //a[i]= a[((int)(i+b))] + 5;
    //should see connection between 
    //aload and astore of array a:
    //(not yet implemented)
    a[i]= a[i-1] + 5;
  }
}

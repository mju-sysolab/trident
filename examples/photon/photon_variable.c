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


extern int l, li, le;
extern float delxl, delyl, rhse, rhsl, x1l, y1l, x2l, y2l, sqlnl, ex, ey, ssq;

void run() {
  const epsdet0 = 1.0000000133514319600181e-10;
  //const epsdet0 = 0.00000000010000000133514319600181;
  //const epsdet0 = 0x2EDBE6FF;
  float absdt, det, dtinv, xi, yi;
  if(l != le)
  {
    /*
       delxl   = readonly->delx   [l];
       delyl   = readonly->dely   [l];
       rhsl    = readonly->rhs    [l];
     */
    /*c*/
    /*c  compute intersection points*/
    /*c*/
    det  = ex*delyl - ey*delxl;
    absdt= fabs(det);
    if(absdt <= epsdet0)
      det=  epsdet0;
    dtinv= 1.0/det;
    xi     = dtinv * (delxl*rhse - ex*rhsl);
    yi     = dtinv * (delyl*rhse - ey*rhsl);
    /*c*/
    /*c  test for intersection between surface endpoints*/
    /*c*/
    /*
       x1l     = readonly->x1     [l];
       y1l     = readonly->y1     [l];
       x2l     = readonly->x2     [l];
       y2l     = readonly->y2     [l];
       sqlnl   = readonly->sqln   [l];
     */
    ssq  = (xi - x1l)*(xi - x1l) + (xi - x2l)*(xi - x2l)
      + (yi - y1l)*(yi - y1l) + (yi - y2l)*(yi - y2l);
    if(ssq <= sqlnl)
    {
      li= l;
      /*      break; */
    }
  }
}

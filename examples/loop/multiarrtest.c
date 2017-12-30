
extern int A[260], b[262], c[174], d[174];
//extern int e[300], f[200], g[300], h[200];
extern int i, x;//, x0, x1, x2, x3, x4;

void run() {

 for(i=0;i<x;i++) {
  A[i+1]=b[i];
  c[i+2]=d[i];
 }
 
 /*for(i=0;i<x;i++) {
  x0=b[i];
  x1=d[i+4];
  x2=A[i+1];
  x3=c[i+2];
  x4=e[i+3];
 }*/
 
 /*for(i=0;i<x;i++) {
  A[i+1]=b[i] * e[i+20] + f[i-10];
  c[i+2]=d[i] / (g[i+3] - h[i+4]);
 }*/
 
 /*for(i=0;i<x;i++) {
  A[i+1]=b[i] * e[i+20] + f[i-10]/c[i+2];
  f[i-10] = A[i+1] + c[i+2] - h[i+4];
  c[i+2]=(d[i]*A[i+1]*A[i+1]*A[i+1]) / 
         (g[i+3] - h[i+4] + f[i-9] * A[i+1] * A[i+1]);
  h[i+4]= -A[i+1] - c[i+2] + f[i-10];
 }*/
 
  //A[5]=b[5];
  //c[6]=d[5];
 

}

//This example was made to demonstrate how the allocation algorithm handles 
//large numbers of small arrays whose total size is equal to what can fit
//in memory, but only if the arrays shared memory words, which in this example
//is not possible.

extern int a[500000];
extern int b[500000];
extern int c[500000];
extern int d[500000];
extern int e[500000];
extern int f[500000];
extern int g[500000];
extern int h[500000];
extern int i[500000];
extern int j[500000];

int run() {

  int n;
  for(n=0; n<10; n++) {
    a[n]++;
    b[n+1]++;
    c[n+2]++;
    d[n+3]++;
    e[n+4]++;
    f[n+5]++;
    g[n+6]++;
    h[n+7]++;
    i[n+8]++;
    j[n+9]++;
  }

}

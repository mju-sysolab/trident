//This example was made to demonstrate how the allocation algorithm can save 
//space by packing arrays in the same word in memory.

extern double a[500000];
extern double b[500000];
extern double c[500000];
extern double d[500000];
extern double e[500000];
extern double f[500000];
extern double g[500000];
extern double h[500000];
extern double i[500000];
extern double j[500000];

int run() {

  int n;
  for(n=0; n<10; n++) {
    a[n]++;
    b[n]++;
    c[n]++;
    d[n]++;
    e[n]++;
    f[n]++;
    g[n]++;
    h[n]++;
    i[n]++;
    j[n]++;
  }

}

//This example was made to demonstrate what happens if the preschedule is
//impossible because enough simultaneous writes are not possible.

extern double a[500000];
extern double b[500000];
extern double c[500000];
extern double d[500000];
extern double e[500000];
extern double f[500000];

int run() {

  int i;
  for(i=0; i<10; i++) {
    a[i]++;
    b[i+1]++;
    c[i+2]++;
    d[i+3]++;
    e[i+4]++;
    f[i+5]++;
  }

}

//This example was made to demonstrate the difference between choosing the 
//slowest memory before prescheduling or extimating which would be best.

extern double a[500000];
extern double b[500000];
extern double c[500000];
extern double d[500000];
extern double e[500000];

int run() {

  int i;
  for(i=0; i<10; i++) {
    a[i]++;
    b[i+1]++;
    c[i+2]++;
    d[i+3]++;
    e[i+4]++;
  }

}

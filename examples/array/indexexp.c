extern int i;
extern int stop;
extern  float x[30]; 
void run() {
  for(i=0;i<stop;i++) {
    x[i] = x[i+1];
  }
}

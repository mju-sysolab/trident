//This example was made to demonstrate what happens if the size of the arrays
//is such that they need to be packed in a special way for all to fit.
//for example, if you have 10 memories of size 4, and 10 arrays of size 3 and
//10 of size 1, you have to pack one 1-sized array and one 3-sized array in 
//each memory in order for them all to fit.  No other allocation is possible.

extern double smalla[1];
extern double smallb[1];
extern double smallc[1];
extern double smalld[1];
extern double smalle[1];
extern double smallf[1];
extern double smallg[1];
extern double smallh[1];
extern double smalli[1];
extern double smallj[1];

extern double biga[3];
extern double bigb[3];
extern double bigc[3];
extern double bigd[3];
extern double bige[3];
extern double bigf[3];
extern double bigg[3];
extern double bigh[3];
extern double bigi[3];
extern double bigj[3];

int run() {

  int i;
  for(i=0; i<10; i++) {
    smalla[i]++;
    smallb[i]++;
    smallc[i]++;
    smalld[i]++;
    smalle[i]++;
    smallf[i]++;
    smallg[i]++;
    smallh[i]++;
    smalli[i]++;
    smallj[i]++;

    biga[i]++;
    bigb[i]++;
    bigc[i]++;
    bigd[i]++;
    bige[i]++;
    bigf[i]++;
    bigg[i]++;
    bigh[i]++;
    bigi[i]++;
    bigj[i]++;
  }

}

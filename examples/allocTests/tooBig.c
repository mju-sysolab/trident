//This example was made to show how the allocation algorithm handles arrays
//that are too large to fit in one memory, even though it would have fit in 
//the two memories used on the board.

extern double a[988888];

int run() {

  int i;
  for(i=0; i<10; i++) {
    a[i]++;
  }

}

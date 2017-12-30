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

package fp.util.bdd;

class BDDPairs {

  int pairs_id;
  BDDPair pairs;
  BDD bdd;

  BDDPairs(BDD my_bdd) {
    pairs_id = 0;
    pairs = null;
    bdd = my_bdd;
  }

  int updatePairsId() {
    pairs_id++;
    
    if (pairs_id == (Integer.MAX_VALUE >> 2)) {
      BDDPair p;
      pairs_id = 0;
      for (p=pairs ; p!=null; p=p.getNext()) 
	p.setId(pairs_id++);
      // bdd_operator_reset !!
      bdd.bdd_operator_reset();

    }
    return pairs_id;
  }

  void registerPair(BDDPair p) {
    p.setNext(pairs);
    pairs = p;
  }

  void resize(int varnum) {
    BDDPair p;
    
    for(p=pairs; p!=null; p=p.getNext()) {
      p.setResults(new BDDNode[varnum]);
    }
  }

}

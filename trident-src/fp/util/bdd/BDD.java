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

import java.util.HashMap;
import java.util.Stack;
import java.util.Iterator;

public class BDD {
  static final int MAXVAR = 0x1FFFFF;

  BDDNode bdd_nodes[];

  BDDNode ZERO, ONE;
  
  int bddnodesize;
  int bddmaxnodesize;

  int bddvarnum;
  BDDNode bddfreepos;
  int bddfreenum;

  BDDNode bddvarset[];
  BDDNode bddlevel2var[];
  BDDNode bddvar2level[];
  
  Stack bddrefstack;

  BDDCache applycache;
  //BDDCache itecache;
  //BDDCache quantcache;
  //BDDCache appexcache;
  //BDDCache replacecache;
  BDDCache misccache;

  static final int CACHEID_RESTRICT = 0x1;

  int miscid;
  int cachesize;
  int maxnodeincrease;
  int bddproduced = 0;

  boolean bdd_try_reordering = false;
  int bdd_reorder_method = 0;

  static int gbcollectnum;
  static int minfreenodes = 20;

  static int DEFAULTMAXNODEINC = 50000;
  
  BDDPairs bdd_pairs;

  public static final int AND  = 0;
  public static final int XOR  = 1;
  public static final int OR   = 2;
  public static final int NAND = 3;

  public static final int NOR  = 4;
  public static final int IMPL = 5;
  public static final int BIML = 6;
  public static final int DIFF = 7;

  public static final int LESS = 8;
  public static final int IIMP = 9;
  public static final int NOT  = 10;
  public static final int SIMPLIFY = 11;

  static String op_name[] = {
    "AND",  "XOR",  "OR",   "NAND", 
    "NOR", "IMPL",  "BIML", "DIFF", 
    "LESS", "IIMP", "NOT" };

  static Integer op_integer[] = {
    new Integer(AND),  new Integer(XOR),  new Integer(OR), 
    new Integer(NAND), new Integer(NOR),  new Integer(IMPL),
    new Integer(BIML), new Integer(DIFF), new Integer(LESS),
    new Integer(IIMP), new Integer(NOT), new Integer(SIMPLIFY),
  };


  static int oprres[][] = {
    {0,0,0,1},  /* and                       ( & )         */
    {0,1,1,0},  /* xor                       ( ^ )         */
    {0,1,1,1},  /* or                        ( | )         */
    {1,1,1,0},  /* nand                                    */
    {1,0,0,0},  /* nor                                     */
    {1,1,0,1},  /* implication               ( >> )        */
    {1,0,0,1},  /* bi-implication                          */
    {0,0,1,0},  /* difference /greater than  ( - ) ( > )   */
    {0,1,0,0},  /* less than                 ( < )         */
    {1,0,1,1},  /* inverse implication       ( << )        */
    {1,1,0,0}   /* not                       ( ! )         */
  };

  public BDD(int nodesize, int cs) {
    //hash = new Hashmap(nodesize); 
    
    bddnodesize = nodesize;

    bdd_nodes = new BDDNode[nodesize];

    BDDNode next_node = new BDDNode(2);
    for (int i = 2; i < nodesize - 1; i++) {
      bdd_nodes[i] = next_node;
      next_node = new BDDNode(i+1);
      bdd_nodes[i].setNext(next_node);
    }
    bdd_nodes[nodesize-1] = next_node;
    bdd_nodes[nodesize-1].setNext(null);
    
    ZERO = bdd_nodes[0] = BDDNode.ZERO;
    ONE = bdd_nodes[1] = BDDNode.ONE;
    
    ZERO.setRoot(0); ONE.setRoot(1);
    ZERO.setMaxRefCount();
    ONE.setMaxRefCount();
    ZERO.setLow(ZERO); ZERO.setHigh(ZERO);
    ONE.setLow(ONE); ONE.setHigh(ONE);
    ONE.setNext(bdd_nodes[2]);
    ZERO.setNext(ONE);

    bdd_operator_init(cs);

    // bdd operator cache init

    bddfreepos = bdd_nodes[2];
    bddfreenum = nodesize - 2;
    bddvarnum = 0;
    gbcollectnum = 0;
    cachesize = cs;
    
    bdd_reorder_method = 0;
    bdd_try_reordering = false;
    maxnodeincrease = DEFAULTMAXNODEINC;

    // Other Init
    //bdd_fddinit();
    bdd_clrvarblocks();

    // bdd pairs init
    bdd_pairs = new BDDPairs(this);
    //bdd_reorder_init();

  }

  static int PAIR(int a, int b) {
    return ((a + b) * (a + b + 1)/2 + a);
  }

  static long TRIPLE(int a, int b, int c) {
    long p = PAIR(a,b);
    long result = ((p + c) * (p + c + 1) / 2 + p);
    return result;
  }

  int nodeHash(int lvl, BDDNode low, BDDNode high) {
    /*
    int a = lvl;
    int b = low.getRoot();
    int c = high.getRoot();
    System.err.println("a "+a+" b "+b+" c "+c);
    System.err.println("PAIR(a,b) "+PAIR(a,b));
    System.err.println("PAIR(PAIR(a,b),c) "+PAIR(PAIR(a,b),c));
    System.err.println("TRIPLE(a,b,c) % nodesize "+(TRIPLE(a,b,c) % bddnodesize));
    */
    return (int)(TRIPLE(lvl,low.getRoot(),high.getRoot()) % bddnodesize);
  }

  int applyHash(BDDNode l, BDDNode r, int op) {
    /*
    int a = l.getRoot();
    int b = r.getRoot();
    int c = op;
    System.err.println("a "+a+" b "+b+" c "+c);
    System.err.println("PAIR(a,b) "+PAIR(a,b));
    System.err.println("PAIR(PAIR(a,b),c) "+PAIR(PAIR(a,b),c));
    System.err.println("TRIPLE(a,b,c) "+(TRIPLE(a,b,c)));
    System.err.println("TRIPLE(a,b,c) % nodesize "+(TRIPLE(a,b,c) % 0x80000000));
    */
    return (int)(Math.abs(TRIPLE(l.getRoot(), r.getRoot(), op)) % 0x80000000);
  }

  int notHash(BDDNode r) {
    return (r.getRoot());
  }

  int restrHash(BDDNode r, int misc) {
    return (PAIR(r.getRoot(),misc));
  }

  void bdd_operator_init(int cachesize) {
    //System.out.println("Cache Inited!");
    applycache = new BDDCache(cachesize);
    //itecache = new BDDCache(cachesize);
    //quantcache = new BDDCache(cachesize);
    //appexcache = new BDDCache(cachesize);
    //replacecache = new BDDCache(cachesize);
    misccache = new BDDCache(cachesize);
  }



  void bdd_gbc() {
    System.out.println("Garbage Collecting! "+gbcollectnum);

    Iterator stack_iter = bddrefstack.iterator();
    // Iterate on refstack
    while(stack_iter.hasNext()) {
      BDDNode node = (BDDNode) stack_iter.next();
      node.setMark();
    }

    for (int n=0; n<bddnodesize; n++) {
      if (bdd_nodes[n].getRefCount() > 0) 
	bdd_nodes[n].setMark();

      // Should this be ZERO or null
      bdd_nodes[n].setHash(null);
    }
    
    bddfreepos = null;
    bddfreenum = 0;
    
    for (int n=2; n < bddnodesize; n++) {
      BDDNode node = bdd_nodes[n];
      
      if ((node.getMark()) && (node.getLow() != null) ) {
	int hash;
	
	node.unmark();
	hash = nodeHash(node.getLevel(), node.getLow(), node.getHigh());
	node.setNext(bdd_nodes[hash].getHash());
	bdd_nodes[hash].setHash(bdd_nodes[n]);
      } else {
	node.setLow(null);
	node.setNext(bddfreepos);
	bddfreepos = bdd_nodes[n];
	bddfreenum++;
      }
    }

    bdd_operator_reset();
    
    gbcollectnum++;
  }


  BDDNode makeNode(int level, BDDNode low, BDDNode high) {
    BDDNode node;
    int hash;
    BDDNode res;
    
    //System.err.println("Makenode!");

    //System.err.println(" lvl: "+level+" l: "+low.getRoot()+" h: "+high.getRoot());
    //System.err.println(" equal "+(low == high));
    // Check whether children are equal
    if (low == high) 
      return low;
  
    // find existing node
    // This is a problem ...
    hash = nodeHash(level, low, high);
    //System.err.println(" Hash returned "+hash+" nodesize "+bddnodesize);
    res = bdd_nodes[hash].getHash();

    while (res != null) {
      if (res.getLevel() == level
	  && res.getLow() == low
	  && res.getHigh() == high)
	return res;
      res = res.getNext();
    }

    if (bddfreepos == null) {

      //System.err.println(" freepos == null -- will garbage collect!");

      bdd_gbc();

      if ((bddfreenum*100)/bddnodesize <= minfreenodes) {
	// Must always run rehash after noderesize()
	bdd_noderesize();
	bdd_gbc();
	bdd_try_reordering = true;
	hash = nodeHash(level, low, high);
      }

      if (bddfreepos == null) {
	System.err.println("Cannot find free nodes");
	System.exit(-1);
      }
    }
    
    res = bddfreepos;
    bddfreepos = bddfreepos.getNext();
    /*
    if (bddfreepos != null) 
      System.err.println("FreePos now is "+bddfreepos.getRoot());
    else 
      System.err.println("FreePos now is null");
    */
    bddfreenum--;
    bddproduced++;
    
    node = res;
    node.setLevel(level);
    node.setLow(low);
    node.setHigh(high);

    node.setNext(bdd_nodes[hash].getHash());
    bdd_nodes[hash].setHash(res);

    return res;
  }

  public void setVarNum(int num) {
    if ((num < 1) || ( num > MAXVAR)) {
      System.err.println("Number of BDD Vars out of range");
      System.exit(-1);
    }

    if (num < bddvarnum) {
      System.err.println("Number of variables can only be increased.");
      System.exit(-1);
    }

    // nothing to do -- do nothing.
    if (num == bddvarnum) return;

    if (bddvarset == null) {
      // needs real nodes ??
      bddvarset = new BDDNode[num*2];
      bddlevel2var = new BDDNode[num];
      bddvar2level = new BDDNode[num];
    } else {

      // build bigger arrays.
      BDDNode[] new_bddvarset = new BDDNode[num*2];
      BDDNode[] new_bddlevel2var = new BDDNode[num];
      BDDNode[] new_bddvar2level = new BDDNode[num];
      
      // copy old references
      for(int n=0; n<bddvarnum; n++) {
	new_bddvarset[n] = bddvarset[n];
	new_bddvarset[n+bddvarnum] = bddvarset[n+bddvarnum];
	new_bddlevel2var[n] = bddlevel2var[n];
	new_bddvar2level[n] = bddvar2level[n];
      }

      // move new arrays into place
      bddvarset = new_bddvarset;
      bddlevel2var = new_bddlevel2var;
      bddvar2level = new_bddvar2level;
    }

    if (bddrefstack == null) {
      bddrefstack = new Stack();
    }
	
    for( ;bddvarnum < num; bddvarnum++) {
      bddvarset[bddvarnum*2] = 
	(BDDNode)bddrefstack.push(makeNode(bddvarnum,ZERO,ONE));
      bddvarset[bddvarnum*2 + 1] = makeNode(bddvarnum,ONE,ZERO);
      bddrefstack.pop();
      
      bddvarset[bddvarnum*2].setMaxRefCount();
      bddvarset[bddvarnum*2+1].setMaxRefCount();
      bddlevel2var[bddvarnum] = bdd_nodes[bddvarnum];
      bddvar2level[bddvarnum] = bdd_nodes[bddvarnum];
      
    }

    ZERO.setLevel(num);
    ONE.setLevel(num);

    bdd_pairs.resize(bddvarnum);

  }
   
  
  public BDDNode bdd_ithvar(int var) {
    if (var < 0 || var >= bddvarnum) {
      System.err.println("Illegal varnum "+var);
      System.exit(-1);
    }
    return bddvarset[var*2];
  }

  void checkreorder(BDDNode node) {
    node.incRefCount();
    if (bdd_try_reordering)
      bdd_reorder_auto();
    bdd_try_reordering = false;
    
    node.decRefCount();
  }

  public BDDNode bdd_not(BDDNode r) {
    BDDNode res;

    if (r == null || r.getLow() == null) {
      new Exception().printStackTrace();
      System.out.println("Illegal BDD "+r);
      System.exit(-1);
    }
    
    // reset refstack ???
    
    res = not_rec(r);
    checkreorder(res);
    return res;
  }
   
  BDDNode not_rec(BDDNode r) {
    BDDCacheData entry;
    BDDNode res;
    
    if (r.isZero())
      return ONE;
    if (r.isOne())
      return ZERO;
    
    entry = applycache.lookup(notHash(r));

    if (entry.a == r && ((Integer)entry.c).intValue() == NOT) {
      return (BDDNode)entry.res;
    }

    bddrefstack.push(not_rec(r.getLow()));
    bddrefstack.push(not_rec(r.getHigh()));
	
    res = makeNode(r.getLevel(), 
		   (BDDNode)bddrefstack.elementAt(bddrefstack.size() - 2),
		   (BDDNode)bddrefstack.peek());
    
    bddrefstack.pop();
    bddrefstack.pop();

    entry.a = r;
    entry.c = op_integer[NOT];
    entry.res = res;
    
    return res;
  }



  public BDDNode bdd_apply(BDDNode l, BDDNode r, int op) {
    BDDNode res;
    
    if (r == null || r.getLow() == null) {
      new Exception().printStackTrace();
      System.out.println("Illegal BDD "+r);
      System.exit(-1);
    }

    if (l == null || l.getLow() == null) {
      new Exception().printStackTrace();
      System.out.println("Illegal BDD "+l);
      System.exit(-1);
    }
    
    // check op number ??
    
    //reset refstack -- uh, how do I have a stack that 
    // I am not pointing to the top?
    
    res = apply_rec(l,r,op);
    checkreorder(res);

    return res;
  }
   
  BDDNode apply_rec(BDDNode l, BDDNode r, int op) {
    BDDCacheData entry;
    BDDNode res;

    //System.out.println(" apply_rec "+l.getRoot()+" "+op_name[op]+" "+r.getRoot());
  
    switch(op) {
    case AND:
      if (l == r) 
	return l;
      if (l.isZero() || r.isZero()) 
	return ZERO;
      if (l.isOne())
	return r;
      if (r.isOne())
	return l;
      break;
    case OR:
      if (l == r) 
	return l;
      if (l.isOne() || r.isOne()) 
	return ONE;
      if (l.isZero())
	return r;
      if (r.isZero()) 
	return l;
      break;
    case XOR:
      if (l == r) 
	return ZERO;
      if (l.isZero()) 
	return r;
      if (r.isZero())
	return l;
      break;
    case NAND:
      if (l.isZero() || r.isZero()) 
	return ONE;
      break;
    case NOR:
      if (l.isOne() || r.isOne()) 
	return ZERO;
      break;
    case IMPL:
      if (l.isZero()) 
	return ONE;
      if (l.isOne()) 
	return r;
      if (r.isOne()) 
	return l;
      break;
    }

    if (l.isConst() && r.isConst()) {
      res = (oprres[op][l.getRoot()<<1 | r.getRoot()] == 1 ? ONE : ZERO);
    } else {

      //int q_hash = applyHash(l,r,op);
      //System.err.println(" apply hash "+q_hash);
      entry = applycache.lookup(applyHash(l,r,op));

      //System.err.println(" Found "+entry.toApplyCache());
      
      if (entry.a == l && entry.b == r 
	  && ((Integer)entry.c).intValue() == op) {
	return (BDDNode)entry.res;
      }


      if (l.getLevel() == r.getLevel()) {
	//System.err.println("Level equal");
	bddrefstack.push(apply_rec(l.getLow(),r.getLow(),op));
	bddrefstack.push(apply_rec(l.getHigh(),r.getHigh(),op));
	
	res = makeNode(l.getLevel(), 
		       (BDDNode)bddrefstack.elementAt(bddrefstack.size() - 2),
		       (BDDNode)bddrefstack.peek());
      } else if (l.getLevel() < r.getLevel()) {
	//System.err.println("Level l less than r");
	bddrefstack.push(apply_rec(l.getLow(), r, op));
	bddrefstack.push(apply_rec(l.getHigh(), r, op));
	
	/*
	      System.err.println("Stack");
      Iterator stack_iter = bddrefstack.iterator();
    // Iterate on refstack
      while(stack_iter.hasNext()) {
	BDDNode node = (BDDNode) stack_iter.next();
	System.err.println("\t "+node);
      }
	*/
	//System.err.println(" peek()  "+ (BDDNode)bddrefstack.peek());
	//System.err.println(" trick() "+ (BDDNode)bddrefstack.elementAt(bddrefstack.size() - 2));
      


	res = makeNode(l.getLevel(), 
		       (BDDNode)bddrefstack.elementAt(bddrefstack.size() - 2),
		       (BDDNode)bddrefstack.peek());
      } else {
	//System.err.println("Level l bigger than r");
	bddrefstack.push(apply_rec(l, r.getLow(), op));
	bddrefstack.push(apply_rec(l, r.getHigh(), op));
	
	res = makeNode(r.getLevel(), 
		       (BDDNode)bddrefstack.elementAt(bddrefstack.size() - 2),
		       (BDDNode)bddrefstack.peek());
      }
      bddrefstack.pop();
      bddrefstack.pop();

      entry.a = l;
      entry.b = r;
      entry.c = op_integer[op];
      entry.res = res;
    }
    //System.out.println(" apply rec made "+res);

    return res;
  }

  public BDDNode bdd_restrict(BDDNode r, BDDNode var) {
    BDDNode res;
    int[] varset;

    if (r == null || r.getLow() == null) {
      new Exception().printStackTrace();
      System.out.println("Illegal BDD "+r);
      System.exit(-1);
    }

    if (var == null || var.getLow() == null) {
      new Exception().printStackTrace();
      System.out.println("Illegal BDD "+var);
      System.exit(-1);
    }
    
    // check op number ??

    if (var.getRoot() < 2) {
      return r;
    }
    
    varset = set2sintp(var);
    /*
    for (int i=0; i < varset.length; i++) {
      System.out.println(" varset ["+i+"] "+varset[i]);
    }
    */
    // size can change...

    //reset refstack -- uh, how do I have a stack that 
    // I am not pointing to the top?

    miscid = (var.getRoot() << 2) | CACHEID_RESTRICT;

    res = restrict_rec(r, varset, 0, varset.length);
    checkreorder(res);

    return res;
  }

  

  BDDNode restrict_rec(BDDNode r, int[] lvl, int current, int num) {
    BDDCacheData entry;
    BDDNode res;
    int level;
    
    if (r.getRoot() < 2 || num == 0) 
      return r;
    
    entry = misccache.lookup(restrHash(r,miscid));
    if (entry.a == r && ((Integer)entry.c).intValue() == miscid) 
      return (BDDNode)entry.res;

    level = Math.abs(lvl[current]) - 1;
    
    if (r.getLevel() == level) {
      if (lvl[current] > 0) 
	res = restrict_rec(r.getHigh(), lvl, current + 1, num - 1);
      else
	res = restrict_rec(r.getLow(), lvl, current + 1, num - 1);
    } else if (r.getLevel() < level) {
      bddrefstack.push(restrict_rec(r.getLow(), lvl, current, num));
      bddrefstack.push(restrict_rec(r.getHigh(), lvl, current, num));
      
      res = makeNode(r.getLevel(), 
		       (BDDNode)bddrefstack.elementAt(bddrefstack.size() - 2),
		       (BDDNode)bddrefstack.peek());
      bddrefstack.pop();
      bddrefstack.pop();
    } else {
      res = restrict_rec(r, lvl, current + 1, num - 1);
    }

    
    entry.a = r;
    entry.c = new Integer(miscid);
    entry.res = res;
      
    return res;
  }
   

  public BDDNode bdd_simplify(BDDNode f, BDDNode d) {
    BDDNode res;

   if (f == null || f.getLow() == null) {
      new Exception().printStackTrace();
      System.out.println("Illegal BDD "+f);
      System.exit(-1);
    }

    if (d == null || d.getLow() == null) {
      new Exception().printStackTrace();
      System.out.println("Illegal BDD "+d);
      System.exit(-1);
    }
    
    //reset refstack -- uh, how do I have a stack that 
    // I am not pointing to the top?
    
    res = simplify_rec(f,d);
    checkreorder(res);
    
    return res;
  }

  
  BDDNode simplify_rec(BDDNode f, BDDNode d) {
    BDDCacheData entry;
    BDDNode res;

    if (d.isZero()) 
      return ZERO;
    if (d.isOne())
      return f;
    if (f.isZero() || f.isOne())
      return f;

    entry = applycache.lookup(applyHash(f,d,SIMPLIFY));

    //System.err.println(" Found "+entry.toApplyCache());
    
    if (entry.a == f && entry.b == d 
	&& ((Integer)entry.c).intValue() == SIMPLIFY) {
      return (BDDNode)entry.res;
    }

    if (f.getLevel() == d.getLevel()) 
      if (d.getLow().isZero())
	res = simplify_rec(f.getHigh(), d.getHigh());
      else if (d.getHigh().isZero())
	res = simplify_rec(f.getLow(), d.getLow());
      else {
	bddrefstack.push(simplify_rec(f.getLow(), d.getLow()));
	bddrefstack.push(simplify_rec(f.getHigh(), f.getHigh()));

	res = makeNode(f.getLevel(), 
		       (BDDNode)bddrefstack.elementAt(bddrefstack.size() - 2),
		       (BDDNode)bddrefstack.peek());
	bddrefstack.pop();
	bddrefstack.pop();
      }
    else if (f.getLevel() < d.getLevel()) {
      bddrefstack.push(simplify_rec(f.getLow(), d ));
      bddrefstack.push(simplify_rec(f.getHigh(), d));
      
      res = makeNode(f.getLevel(), 
		     (BDDNode)bddrefstack.elementAt(bddrefstack.size() - 2),
		     (BDDNode)bddrefstack.peek());
      bddrefstack.pop();
      bddrefstack.pop();
    } else {
      bddrefstack.push(simplify_rec(f, d.getLow()));
      bddrefstack.push(simplify_rec(f, d.getHigh()));
      
      res = makeNode(d.getLevel(), 
		     (BDDNode)bddrefstack.elementAt(bddrefstack.size() - 2),
		     (BDDNode)bddrefstack.peek());
      bddrefstack.pop();
      bddrefstack.pop();
    }
      
    entry.a = f;
    entry.b = d;
    entry.c = op_integer[SIMPLIFY];
    entry.res = res;
    
    return res;
  }


  int[] set2sintp(BDDNode r) {
    BDDNode n;
    int num;
    int result[];

    if (r.getRoot() < 2) {
      System.out.println(" This is a error in set2sintp -- what??");
      System.exit(-1);
    }

    n = r;
    num = 0;

    while (n.getRoot() > 1) {
      if (n.getHigh().getRoot() >= 1) {
	n = n.getHigh();
      } else {
	n = n.getLow();
      }
      num++;
    }

    result = new int[num];
    n = r;
    num = 0;
    
    while (n.getRoot() > 1) {
      if (n.getHigh().getRoot() >= 1) {
	result[num++] = n.getLevel() + 1;
	n = n.getHigh();
      } else {
	result[num++] = -(n.getLevel() + 1);
	n = n.getLow();
      }
    }

    return result;
  }
	     
   
    
  void bdd_operator_reset() {
    applycache.reset();
    //itecache.reset();
    //quantcache.reset();
    //appexcache.reset();
    //replacecache.reset();
    misccache.reset();
  }

  void bdd_reorder_auto() {
    System.err.println(" NO auto reording ... ");
  }
						

  void bdd_noderesize() {
    BDDNode newnodes[];
    BDDNode next_node;
    int oldsize = bddnodesize;
    int n;

    if (bddnodesize >= bddmaxnodesize && bddmaxnodesize > 0) {
      System.err.println("No more nodes can be created ...??");
      System.exit(-1);
    }

    bddnodesize = bddnodesize << 1;
    
    if (bddnodesize > oldsize + maxnodeincrease)
      bddnodesize = oldsize + maxnodeincrease;
   
    if (bddnodesize > bddmaxnodesize  &&  bddmaxnodesize > 0)
      bddnodesize = bddmaxnodesize;

    /*
    if (resize_handler != NULL)
      resize_handler(oldsize, bddnodesize);
    */

    newnodes = new BDDNode[bddnodesize];

    // copy mess over 
    for (n = 0 ; n < oldsize ; n++) {
      bdd_nodes[n].setHash(null); // Nulls now?
      newnodes[n] = bdd_nodes[n];
    }
    
    bdd_nodes = newnodes;
    next_node = new BDDNode(oldsize);
    for (n = oldsize; n < bddnodesize - 1; n++)   {
      bdd_nodes[n] = next_node;
      next_node = new BDDNode(n+1);
      bdd_nodes[n].setNext(next_node);
    }
    bdd_nodes[bddnodesize-1] = next_node;
    bdd_nodes[bddnodesize-1].setNext(bddfreepos);

    bddfreepos = bdd_nodes[oldsize];
  }


  void bdd_clrvarblocks() {
    // System.err.println(" No bdd_clrvarblocks ");
  }

  public int getVar(BDDNode node) {  
    //System.out.println(" size  "+bddlevel2var.length+" level "+node.getLevel());
    if (node.getLevel() > 0) 
      return bddlevel2var[node.getLevel()].getRoot();  
    else 
      return -1;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();

    for (int i = 0; i < bddnodesize; i++) {
      sbuf.append(bdd_nodes[i]);
      sbuf.append("\n");
    }    
    // print caches
    // print mapping tables?

    return sbuf.toString();
  }

  public String toDot(BDDNode r) {
    StringBuffer sbuf = new StringBuffer();
    
    sbuf.append("digraph G {\n");
    if (r.isZero() || !r.isConst()) {
      sbuf.append("0 [shape=box, label=\"0\", ");
      sbuf.append("style=filled, shape=box, height=0.3, width=0.3];\n");
    }
    if (r.isOne() || !r.isConst()) {
      sbuf.append("1 [shape=box, label=\"1\", ");
      sbuf.append("style=filled, shape=box, height=0.3, width=0.3];\n");
    }

    sbuf.append(r.toDot(bddlevel2var));
    sbuf.append("}\n");
    r.clearMark();
    
    return sbuf.toString();
  }
   

    
  

  public static void main( String[] args ) {
    int i;
    BDD my_bdd = new BDD(10,100);
    //BDD my_bdd = new BDD(30000,100000);

    my_bdd.setVarNum(100);

    //System.out.println("my_bdd\n"+my_bdd);

    //BDDNode r[] = new BDDNode[10000];
    //BDDNode a[] = new BDDNode[10000];

    BDDNode b = my_bdd.bdd_ithvar(0);
    BDDNode c = my_bdd.bdd_ithvar(1);
    BDDNode a = my_bdd.bdd_ithvar(2);
    BDDNode d = my_bdd.bdd_ithvar(3);
    BDDNode e = my_bdd.bdd_ithvar(4);
    BDDNode f = my_bdd.bdd_ithvar(5);
    
    /*
    for(i=0; i<10000; i++) {
      //my_bdd.setVarNum(4+i);
      a[i] = my_bdd.bdd_ithvar(i);
    }
    */

    // add varblock ??

    BDDNode tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;

    tmp1 = my_bdd.bdd_not(b).incRefCount();
    tmp2 = my_bdd.bdd_not(b).incRefCount();
    tmp3 = my_bdd.bdd_not(tmp2).incRefCount();
    tmp2.decRefCount();
    tmp4 = my_bdd.bdd_apply(a,tmp1,AND).incRefCount();
    tmp1.decRefCount();
    tmp5 = my_bdd.bdd_apply(a,tmp3,AND).incRefCount();
    tmp3.decRefCount();
    tmp1 = my_bdd.bdd_apply(tmp4,c,OR).incRefCount();
    tmp4.decRefCount();
    tmp4 = my_bdd.bdd_apply(tmp1,c,OR).incRefCount();
    tmp1.decRefCount();
    tmp1 = my_bdd.bdd_apply(tmp4,c,OR).incRefCount();
    tmp4.decRefCount();

    d = my_bdd.bdd_apply(tmp1,tmp5,OR).incRefCount();
    tmp1.decRefCount();
    tmp5.decRefCount();

    tmp1 = my_bdd.bdd_apply(a,b,XOR).incRefCount();
    e = my_bdd.bdd_apply(tmp1,c,XOR).incRefCount();
    tmp1.decRefCount();

    tmp1 = my_bdd.bdd_apply(a,b,AND).incRefCount();
    tmp2 = my_bdd.bdd_apply(b,c,AND).incRefCount();
    f = my_bdd.bdd_apply(tmp1,tmp2,OR).incRefCount();
    tmp1.decRefCount();
    tmp2.decRefCount();
    tmp1 = my_bdd.bdd_apply(a,c,AND).incRefCount();
    f = my_bdd.bdd_apply(f,tmp1,OR).incRefCount();
    tmp1.decRefCount();

    System.out.println("sum a b c\n"+my_bdd.toDot(f));
    System.out.println("carry a b c\n"+my_bdd.toDot(e));

      /*
      r[i] = my_bdd.bdd_apply(my_bdd.bdd_apply(my_bdd.bdd_apply(my_bdd.bdd_apply(my_bdd.bdd_apply(a[i],my_bdd.bdd_not(b),AND),c,OR),c,OR),c,OR),my_bdd.bdd_apply(a[i],my_bdd.bdd_not(my_bdd.bdd_not(b)),AND),OR).incRefCount();
      */
      //System.out.println(" r["+i+"] "+r[i].getRoot());
    //r = my_bdd.bdd_apply(a,b,AND);

    
    System.out.println("my_bdd\n"+my_bdd.toDot(d));
    
    d = my_bdd.bdd_restrict(d,e);
    System.out.println("my_bdd\n"+my_bdd.toDot(d));
    
    d = a;
    d = my_bdd.bdd_restrict(d,e);
    System.out.println("my_bdd\n"+my_bdd.toDot(d));
  

  }

}

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


package fp.passes;

import java.util.*;

import fp.flowgraph.BlockGraph;

public class PassManager {

  public static int VERBOSE_QUIET = 0;
  public static int VERBOSE_L1 = 1;
  public static int VERBOSE_L2 = 2;
  public static int VERBOSE_L3 = 3;
  public static int VERBOSE_L4 = 4;
  
  private LinkedList _passes;
  private LinkedList _pass_schedule;
  private LinkedList _pass_stats;

  private int _verbose = VERBOSE_QUIET;

  private boolean _debug = false;

  public PassManager() {
    this(false);
  }

  public PassManager(boolean debug) { 
    _passes = new LinkedList();
    _pass_schedule = new LinkedList();
    _debug = debug;
  }

  public void setVerbose(int v) { _verbose = v; }
  public int getVerbose() { return _verbose; }

  LinkedList getStats() { return _pass_stats; }

  // besides user stuff being added, we could also make it 
  // easy to pick your passes if some where optional or for testing ...
  void register(Pass pass) {
    _passes.add(pass);
  }
  

  public void add(Pass pass) {
    // check dependencies 
    // see if CFG is consistent 
    // and if this requires it to be.
    // add appropriate passes.

    if (pass instanceof GraphPass) {
      _pass_schedule.add(pass);
    } else if (pass instanceof UtilPass) { 
      // is there any reason to wait -- probably.
      // But then we need a separate list for utils before 
      // and utils after.  I don't think it makes sense for 
      // utils during...
      ((UtilPass)pass).optimize();
      //_pass_schedule.add(pass);
    } else if (pass instanceof BlockPass) {
      Pass last_pass = (Pass)_pass_schedule.getLast();
      // this may need to be more complicated if this blockpass does
      // not get along with others -- it may also have its own set of 
      // dependencies or may require that the CFG is consistent...
      if (last_pass == null || ! (last_pass instanceof GroupGraph) ) {
	last_pass = new GroupGraph(this);
	_pass_schedule.add(last_pass);
      }
      
      ((GroupGraph)last_pass).add(pass);

    } else {
      new Exception("Cannot add Pass").printStackTrace();
      System.exit(-1);
    }
  }

  public void addPrintGraph() {
    addPrintGraph(null);
  }

  public void addPrintGraph(LinkedList passes) {
    for (ListIterator iter = _pass_schedule.listIterator(); iter.hasNext(); ) {
      Pass pass = (Pass)iter.next();
      // skip the PrintGraphs ...
      if (pass instanceof PrintGraph) continue;
      boolean add = false;
      if (passes == null) {
	add = true;
      } else {
	for (Iterator pass_iter = passes.iterator(); pass_iter.hasNext(); ) {
	  String pass_name = (String)pass_iter.next();
	  if (pass_name.equals(pass.name())) {
	    add = true;
	    break;
	  }
	}
      }
      if (add==true) 
	iter.add(new PrintGraph(this, pass.name()+"_"));
    }
  }


  public void addDebug(LinkedList passes) {
    for (ListIterator iter = _pass_schedule.listIterator(); iter.hasNext(); ) {
      Pass pass = (Pass)iter.next();
      // skip the PrintGraphs ...
      if (pass instanceof PrintGraph) continue;
      boolean add = false;
      if (passes == null) {
	add = true;
      } else {
	for (Iterator pass_iter = passes.iterator(); pass_iter.hasNext(); ) {
	  String pass_name = (String)pass_iter.next();
	  if (pass_name.equals(pass.name())) {
	    add = true;
	    break;
	  }
	}
      }
      if (add==true) {
	//iter.add(new PrintGraph(this, pass.name()+"_"));
	// here we would add passes that would do sanity checks.
	// some could fail at certain points, but others may pass
	// all the time.
	System.err.println("Unimplemented ... ");
	System.exit(-1);
      }
    }
  }
		 

  

  // probably should move the verbosity to separate 
  // methods as this will need to become more complicated with
  // how to traverse different kinds of passes.

  public void run(BlockGraph graph) {
    if (_debug) {
      // here we should add the debug;
      // addDebug(null);
      _debug = false;
    }

    long total = 0;
    _pass_stats = new LinkedList();
    if (_verbose >= VERBOSE_L1) {
      total = System.currentTimeMillis();
    }
      
    for(Iterator iter = _pass_schedule.iterator(); iter.hasNext(); ) {
      Pass pass = (Pass)iter.next();
      // eventually there should be other types
      if (pass instanceof GraphPass) {
	long time = 0;
	
	PassStat stat = new PassStat(pass.name(), 0);
	_pass_stats.add(stat);
	if (_verbose >= VERBOSE_L1) {
	  time = System.currentTimeMillis();
	  print(VERBOSE_L2, "GraphPass : "+pass.name() + " start ");
	  print(VERBOSE_L3, new Date().toString());
	  println(VERBOSE_L2, "");
	}

	((GraphPass)pass).optimize(graph);

	if (_verbose >= VERBOSE_L1 ) { 
	  time = System.currentTimeMillis() - time;
	  String pass_name = "GraphPass : "+pass.name();
	  println(VERBOSE_L3, pass_name + " run tim "+time+" ms");
	  print(VERBOSE_L2, pass_name + " stop ");
	  print(VERBOSE_L3, new Date().toString());
	  println(VERBOSE_L2, "");

	  // fix stat with proper time.
	  stat.millis = time;
	}
      }	else {
	new Exception("Unknown Pass").printStackTrace();
	System.exit(-1);
      }
    }

    if (_verbose >= VERBOSE_L1) {
      total = System.currentTimeMillis() - total;
      System.out.println("------------------------------------------");
      System.out.println("Total Passes Run Time : "+total+" ms");
      System.out.println("  Passes                        Percentage ");
      System.out.println("------------------------------------------");
      long overhead = 0;
      for(Iterator iter=_pass_stats.iterator(); iter.hasNext(); ) {
	PassStat s = (PassStat)iter.next();
	overhead += s.millis;
	System.out.println(s.toString(total));
      }

      int total_overhead = (int)(((total - overhead)*10000) / total);
      if (total_overhead < 0) total_overhead = 0;
      StringBuffer mbuf = PassStat.millisPercent(total_overhead);
      StringBuffer obuf = PassStat.pad("Overhead Total", 30);
      System.out.println("  "+obuf+"  "+mbuf+"%");
      System.out.println("------------------------------------------");
    }
  }

  final void print(int level, String out) {
    if (_verbose >= level)  System.out.print(out); 
  }

  final void println(int level, String out) { 
    if (_verbose >= level)  System.out.println(out); 
  }



}
    

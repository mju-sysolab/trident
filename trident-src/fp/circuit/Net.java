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
package fp.circuit;

import java.util.HashSet;
import java.util.Iterator;

import fp.util.Nameable;
import fp.util.Bool;

abstract public class Net extends Nameable implements Bool {

  protected HashSet sources;
  protected HashSet sinks;

  protected int width;

  protected boolean pullup = false;
  protected boolean pulldown = false;

  private Circuit _parent;

  public Net(Circuit graph, String name) {
    super(name);
    _parent = graph;
    sources = new HashSet();
    sinks = new HashSet();
    
    // checking for nullness, is not normally and possibly should be removed
    // in order to force the net to be in a graph -- and not graphless.
    if (graph != null)
      // is this the way to go?
      graph.addNet(this);
  }


  abstract public void build(String name, Object[] objs);

  public Circuit getParent() {
    return _parent;
  }

  // add friends
  public Net addSink(PortTag sink) {
    if (!sinks.contains(sink)) {
      sinks.add(sink);
      sink.setNet(this);
    }
    return this;
  }

  public Net addSource(PortTag src) {
    if (!sources.contains(src)) {
      sources.add(src);
      src.setNet(this);
    }
    return this;
  }

  // width stuff.
  public int getWidth() { return width; };
  public void setWidth(int w) { width = w; }

  // This method has two purposes: 1) to resolve the widths of the 
  // wires and 2) to do connectivity tests of the wires.
  public boolean resolveWidth() {
    int sourceWidth = 0;
    boolean isErrorFree = true;

    // Check the number of sources for this wire...
    if (sources.size() == 1) { 
      // Just get the only PortTag's width.
      sourceWidth = ((PortTag)((HashSet)sources).toArray()[0]).getWidth();
      
      //System.out.println("source: " + (PortTag)((HashSet) sources).toArray()[0]);
    } else if (sources.size() == 0) {
      // CHANGE: this is a hack and should eventually be changed!
      if(sinks.size() > 0) {
	sourceWidth = ((PortTag)((HashSet)sinks).toArray()[0]).getWidth();
      } else {
	sourceWidth = -1;
      }
      System.out.println("WARNING: resolveWidth() -- wire with no source!");
      isErrorFree = false;
    } else {
      sourceWidth = -1;
      System.out.println("ERROR: resolveWidth() -- wire with mult sources!");
      isErrorFree = false;
      //System.exit(-1);
      //new Exception().printStackTrace();
    }

    if (sinks.size() == 0) {
      System.out.println("WARNING: resolveWidth() -- wire with no sinks!");
      isErrorFree = false;
    }

    // Check that the source and sinks have the same width.
    for(Iterator iter = sinks.iterator(); iter.hasNext(); ) {
      PortTag iSink = (PortTag)iter.next();
      //System.out.println("sink: " + iSink);
      int iSinkWidth = iSink.getWidth();
      if ((sourceWidth != iSinkWidth) && (sourceWidth != -1)) {
	System.out.print("WARNING: resolveWidth() -- ");
	System.out.println("source and sink widths don't match!");
	System.out.println("source:"+sourceWidth+", sink:"+iSinkWidth);
	isErrorFree = false;
      }
    }

    // Set the wire's width. If the wire doesn't have a source, then 
    // the width is set to -1.  Is this correct?
    setWidth(sourceWidth);

    return isErrorFree;
  }

  // pull-up stuff... ??

  // get sets
  public HashSet getSources() { return sources; }
  public HashSet getSinks() { return sinks; }

  // is this correct?
  public String getBoolName() { return getName(); }

  public String toString() {
    return getName()+"("+getWidth()+")";
  }

}  
    

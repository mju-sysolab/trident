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

import java.util.*;

import fp.util.Nameable;
import fp.util.UniqueName;

/**
 * Node is the base class for Leaf circuit elements.  It will be used
 * to define all elements that can be defined in the generic circuit
 * object.  It is also used to define the ciruit block boxes that may
 * be inserted as is necessary.
 * 
 * Nodes are not well equipped to handle In/Out ports ore really odd
 * stuff like that.
 *
 * @author Justin L. Tripp
 * @version $revision$
 */
public abstract class Node extends Nameable {

  /**
   * My circuit parent.
   */
  protected Circuit _parent;
  
  // ??
  /**
   * My non-uniquified name.  Useful for pretty printing things.
   */
  protected String _ref_name; 

  /**
   * A hash of all of the ports in the object.
   */
  protected HashSet _inports;
  protected HashSet _outports;
  
  /**
   * IF we have this unique namer for node objects -- what is the
   * extra one in Circuit.  Odd, very odd.
   */
  private static UniqueName uname = new UniqueName(".");
  
  /**
   * Special object name.
   */
  private String _object_name;
  /**
   * Any random parameters -- upto 2 Billion!
   */
  private Object[] _parameters;
  /**
   * The hash of the ports that are to be associated with the black
   * box element.
   */
  private HashMap _ports;


  /**
   * The main constructor used to build the generic node.  It records
   * the name and resets the various hashes.  Also of note is that it
   * adds itself to the node list in the parent.  Very tricky.
   * 
   * @param parent Circuit parent
   * @param name name
   */
  public Node(Circuit parent, String name) {
    super( uname.getUniqueName(parent.getName()+"."+name));
    _ref_name= name;
    _inports = new HashSet();
    _outports = new HashSet();

    _parent = parent;
    parent.addNode(this); // ??

  }

  /**
   * This is the random logic constructor. It has different parameters
   * for defining the information that the build sequence will use to
   * create the randome logic.
   * 
   * @param parent Circuit parent
   * @param name my name
   * @param ports hash of ports
   * @param object_name name of 
   *     external object
   * @param parameters help 
   *     parameters
   */
  public Node(Circuit parent, String name, HashMap ports, String object_name, 
       Object[] parameters) {
    this(parent, name);
    _ports = ports;
    _object_name = object_name;
    _parameters = parameters;
  }

  


  // abstract methods
  /**
   * Ahh the abstract build process.  This is the secret sauce.
   * Should it also be an interface so that we do not even need to
   * know what kind of thing were are building.  Hmmm...
   * 
   * @param name Name of process
   * @param arg_o secret args.
   */
  public abstract void build(String name, Object[] arg_o);

  // Accessor Methods
  /**
   * This accessor is for getting the name of the object before it has
   * been made large with heirachy or unique.  It may only be useful
   * in special circumstances.
   * 
   * @return Returns non-unique name of 
   *     object.
   */
  public String getRefName() { return _ref_name; }
  /**
   * This is not something to be used lightly.  Generally it is for
   * internal use only.
   * 
   * @param n New anme
   */
  void setRefname(String n) { _ref_name = n; }

  protected String getObjectName() { return _object_name; }


  /**
   * Simple accessor.
   * 
   * @return parent circuit object.
   */
  public Circuit getParent() { return _parent; }

  /**
   * Simple accessor.
   * 
   * @return get inputs to Node.
   */
  public HashSet getInPorts() { return _inports; }
  /**
   * Simple accessor.
   * 
   * @return get out ports of Node.
   */
  public HashSet getOutPorts() { return _outports; }
  

  // this is only for Nodes that wrap an inserted object
  protected HashMap getPortMap() { return _ports; }
  
  protected Object[] getParameters() { return _parameters; }


  // Port Methods

  /**
   * add a new portInfo to the node.
   * 
   * @param name PortTag name
   * @param type direction of 
   *     PortTag
   * @param width size of portInfo
   * @return new PortTag.
   */
  public PortTag addPort(String name, int type, int width) {
    /*
    if (name == "=") {
      System.err.println("Can't have a port with name \"=\"");
      name = "eq";
    }
    */
    PortTag p = new PortTag(this, name, type, width);
    if (type == PortTag.IN)
      addInPort(p);
    else if (type == PortTag.OUT)
      addOutPort(p);
    else if (type == PortTag.INOUT) {
      addInPort(p);
      addOutPort(p);
    }
    return p;
  }

  /**
   * Suppose you already have the portInfo for some reason.  You can
   * add it without making a new one.
   * 
   * @param pi The portinfo.
   * @return A convenience copy
   */
  public PortTag addPortTag(PortTag pi) {
    pi.setParent(this);
    int type = pi.getType();
    
    if (type == PortTag.IN)
      addInPort(pi);
    else if (type == PortTag.OUT)
      addOutPort(pi);
    else if (type == PortTag.INOUT) {
      addInPort(pi);
      addOutPort(pi);
    }
    return pi;
  }


  /**
   * Save you one extra paramter -- now you pick a method for the
   * direction.
   * 
   * @param name PortTag name
   * @param width PortTag size
   * @return new PortTag.
   */
  public PortTag addInPort(String name, int width) {
    return addPort(name, PortTag.IN, width);
  }
  
  /**
   * @param name 
   * @param width 
   */
  public PortTag addOutPort(String name, int width) {
    return addPort(name, PortTag.OUT, width);
  }
  
  /**
   * Add a portinfo to the local hash.
   * 
   * @param p The portinfo
   */
  protected void addInPort(PortTag p) {
    _inports.add(p);
  }

  /**
   * @param p 
   */
  public void addOutPort(PortTag p) {
    _outports.add(p);
  }


  public PortTag findInPort(String name) {
    for(Iterator iter=_inports.iterator(); iter.hasNext(); ) {
      PortTag info = (PortTag) iter.next();
      String p_name = info.getName();
      if (p_name.equals(name)) {
	return info;
      }
    }
    return (PortTag)null;
  }

  public PortTag findOutPort(String name) {
    for(Iterator iter=_outports.iterator(); iter.hasNext(); ) {
      PortTag info = (PortTag) iter.next();
      String p_name = info.getName();
      if (p_name.equals(name)) {
	return info;
      }
    }
    return (PortTag)null;
  }


  public String toString() {
    StringBuffer sbuf = new StringBuffer("Node:\n");
    sbuf.append("\t name = "+getName()+" unique_name = "+getUniqueName()+"\n");
    sbuf.append("\t ref_name = "+_ref_name+" object_name = "+_object_name+"\n");
    sbuf.append("\t parent = "+_parent+" parent_name = "+_parent.getName()+"\n");
    sbuf.append("\t inports:\n");
    for(Iterator iter=_inports.iterator(); iter.hasNext(); ) {
      PortTag info = (PortTag) iter.next();
      sbuf.append("\t\t "+info.toString()+"\n");
    }
    sbuf.append("\t outports:\n");
    for(Iterator iter=_outports.iterator(); iter.hasNext(); ) {
      PortTag info = (PortTag) iter.next();
      sbuf.append("\t\t "+info.toString()+"\n");
    }
    return sbuf.toString();
  }

  
  
}

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
package fp.util;

/**
 * This is a convenience class for naming things.  It is not too
 * fancy, but many structures in circuit synthesis are easier to deal
 * with if they have names.
 * 
 * @author Justin L. Tripp
 * @version $version$
 */
public class Nameable {
  /**
   * The name.
   */
  String _name;
  
  String _default_name;
  String _instance_name;

  public Nameable(String name, String instance) {
    _default_name = name;
    _instance_name = instance;
    _name = name;
  }


  /**
   * The constructor to extend, so that you have naming capability.
   * 
   * @param name The name
   */
  public Nameable(String name) {
    this(name, null);
  }

  /**
   * simple accessor.
   * 
   * @return Get the name.
   */
  public String getName() { return _name; }
  public String getUniqueName() { return _instance_name; }
  public String getDefaultName() { return _default_name; }

  protected void setName(String n) { _name = n; }
  protected void setUniqueName(String n) { _instance_name = n; }
  protected void setDefaultName(String n) { _default_name = n; }

  /**
   * Print the name.
   * 
   * @return String with output
   */
  public String toString() {
    return _name;
  }
}

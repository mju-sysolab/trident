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


package fp.graph;

/**
 * Abstract superclass for Nodes and Edges
 *
 * @author Nathan Kitchen
 */
public abstract class AbstractGraphComponent extends DotAttributes {
  private Object _tag;

  /**
   * Constructs a new AbstractGraphComponent with a null tag
   */
  public AbstractGraphComponent() {
    this(null);
  }
  
  /**
   * Constructs a new AbstractGraphComponent with the given tag
   */
  public AbstractGraphComponent(Object tag) {
    setTag(tag);
  }
  
  /**
   * Returns the tag object
   */
  public Object getTag() {
    return _tag;
  }

  /**
   * Sets the value of the dot attribute "label" to the return value of
   * <code>toString</code>
   */
  public void setDotLabelFromToString() {
    setDotAttribute("label", toString());
  }
  
  /**
   * Replaces the tag object
   */
  public void setTag(Object tag) {
    _tag = tag;
  }

  /**
   * Returns a String representation of this component, which is derived from
   the dot attribute &quot;label&quot;, if possible, or the tag
   */
  public String toString() {
    String label = getDotAttribute("label");
    if (label == null) {
      Object tag = getTag();
      if (tag == null) {
	return super.toString();
      } // end of if ()
      else {
	return tag.toString();
      } // end of else
    }
    else {
      return label;
    } // end of else
  }
}

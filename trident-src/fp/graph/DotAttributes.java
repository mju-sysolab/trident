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

import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map;

/**
 * Abstract superclass for objects that can have attributes defined to be
 * used when producing input files for AT&T's graph drawing program "dot."
 *
 * @author Nathan Kitchen
 */
public abstract class DotAttributes {
  /**
   * Contains the attributes for this Node to be used when producing
   * input files for the dot graph drawing tool from AT&T
   */
  private Properties _dotAttributes;
  
  public DotAttributes() {
    _dotAttributes = new Properties();
  }

  /**
   * Removes all dot attributes from this object
   */
  public void clearDotAttributes() {
    _dotAttributes.clear();
  }

  /**
   * Returns a String containing all the attributes of this object in this
   * format: <code>["key1"="value1","key2"="value2"]</code>.  All special
   * characters, like newlines and quote characters, are converted to
   * escape sequences.
   */
  public String getDotAttributeList() {
    if (_dotAttributes.size() == 0)
      return "";
    
    StringBuffer strBuf = new StringBuffer();
    strBuf.append('[');
    Iterator iter = _dotAttributes.entrySet().iterator();
    boolean hasNext = iter.hasNext();
    while (hasNext) {
      Map.Entry entry = (Map.Entry) iter.next();
      strBuf.append('\"')
	.append(convertSpecialsToEscapes((String) entry.getKey()))
	.append("\"=\"")
	.append(convertSpecialsToEscapes((String) entry.getValue()))
	.append('\"');
      hasNext = iter.hasNext();
      if (hasNext)
	strBuf.append(',');
    }
    strBuf.append(']');
    return strBuf.toString();
  }
	
  /**
   * Returns the value of the dot attribute with the given name, or null if
   * attribute has not been defined for this Node
   */
  public String getDotAttribute(String name) {
    return _dotAttributes.getProperty(name);
  }

  /**
   * Returns an Iterator over all the names of dot attributes which have
   * been defined for this Node
   */
  public Iterator getDotAttributeNames() {
    return Collections.unmodifiableSet(_dotAttributes.keySet()).iterator();
  }

  /**
   * Returns the number of attributes that have been set
   */ 
  public int getNumDotAttributes() {
    return _dotAttributes.size();
  }

  /**
   * Removes the dot attribute with the given name
   */
  public void removeDotAttribute(String name) {
    _dotAttributes.remove(name);
  }
  
  /**
   * Sets the value of the dot attribute with the given name.  The format
   * of the value should be the same as you want it displayed.  For
   * example, use this:
   * <p><code>setDotAttribute("label", "\"abc\"\nString");</code>
   * <p>instead of this:
   * <p><code>setDotAttribute("label", "\\\"abc\\\"\\nString");</code>
   */
  public void setDotAttribute(String name, String value) {
    _dotAttributes.setProperty(name, value);
  }

  /**
   * Converts all the special characters in <code>str</code> (like newlines
   * and quotes) to escape sequences (like \n)
   */
  public static String convertSpecialsToEscapes(String str) {
    StringBuffer strBuf = new StringBuffer();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      switch (c) {
      case '\n':
	strBuf.append("\\n");
	break;
      case '\t':
	strBuf.append("\\t");
	break;
      case '\r':
	strBuf.append("\\r");
	break;
      case '\"':
	strBuf.append("\\\"");
	break;
      case '\'':
	strBuf.append("\\\'");
	break;
      case '\b':
	strBuf.append("\\b");
	break;
      case '\f':
	strBuf.append("\\f");
	break;
      case '\\':
	strBuf.append("\\\\");
	break;
      default:
	strBuf.append(c);
      }
    }
    return strBuf.toString();
  }
}  

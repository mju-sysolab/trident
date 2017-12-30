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


package fp.flowgraph;

import fp.graph.*;
import java.util.*;

/**
 * A node in a data-flow graph, which represents a typed value.  The type
 * may be void.
 *
 * @author Nathan Kitchen
 */
public class DataFlowNode extends Node {
  // This class depends on the Soot J*va Optimization Framework by the
  // Sable research group, even though it should be language-independent.
  // We made this decision because it was convenient.
  
  /**
   * The type of this node
   */
  private Class _type;

  /**
   * The label for this node in dot language will be this field with the type
   * appended
   */
  private String _primaryDotLabel;

  /**
   * The name of the source file for this node
   */
  private String _sourceFile;
  
  /**
   * The line of source code 
   */
  private int _sourceLine;


  private boolean _isSource;
  private boolean _isSink;
    
  /**
   * Constructs a new data-flow node with the given type and no tag
   */
  public DataFlowNode(Class type, boolean srcFlag, boolean sinkFlag){
    this(type, null, srcFlag, sinkFlag );
  }

  /**
   * Constructs a new data-flow node with the given type and tag
   */
  public DataFlowNode(Class type, Object tag, boolean sourceFlag, boolean sinkFlag) {
    super(tag);
    setType(type);
    setSourceLine(UNKNOWN_LINE);
    setIsSource(sourceFlag);
    setIsSink(sinkFlag);
  }

  /**
   * Returns the label for this node that will be used when producing dot
   * language output.  The type of this node is only appended at that time.
   */
  public String getPrimaryDotLabel() {
    return _primaryDotLabel;
  }

  /**
   * The value used for the source line when it is unknown
   *
   * @see #getSourceLine
   * @see #setSourceLine
   */
  public static final int UNKNOWN_LINE = -1;
  
  /**
   * Returns the name of the file that contains the source code for this graph
   * (may be null)
   */
  public String getSourceFile() {
    return _sourceFile;
  }

  /**
   * Returns the line in the source code for this node
   *
   * @see #UNKNOWN_LINE
   */
  public int getSourceLine() {
    return _sourceLine;
  }
  
  /**
   * Returns the type of this node
   */
  public Class getType() {
    return _type;
  }

  /**
   * Returns a set that only accepts DataFlowEdges
   */
  protected Set newEdgeSet() {
    return new HashSet() {
	public boolean add(Object o) {
	  if (! (o instanceof DataFlowEdge)) {
	    throw new DataFlowException("Not a DataFlowEdge: " + o + " (" +
					o.getClass() + ")");
	  } // end of if ()
	  return super.add(o);
	}
      };
  }

  /**
   * Assigns the label for this node that will be used when producing dot
   * language output.  The type of this node is only appended at that time.
   */
  public void setPrimaryDotLabel(String label) {
    _primaryDotLabel = label;
  }

  /**
   * Assigns the name of the file that contains the source code for this graph
   */
  public void setSourceFile(String sourceFile) {
    _sourceFile = sourceFile;
  }


  /**
   * Assigns the line in the source code for this node
   *
   * @see #UNKNOWN_LINE
   */
  public void setSourceLine(int line) {
    _sourceLine = line;
  }

  /**
   * Sets the type of this node
   */
  public void setType(Class type) {
    _type = type;
  }

  public void setIsSource(boolean flag){
    _isSource = flag;
  }

  public void setIsSink(boolean flag){
    _isSink = flag;
  }

  public boolean isSource() {
    return _isSource;
  }
  
  public boolean isSink() {
    return _isSink;
  }

  public boolean isSourceOrSink() {
    return (_isSource || _isSink);
  }

  /**
   * Uses <code>getPrimaryDotLabel</code>
   */
  public String toString() {
    String primary = getPrimaryDotLabel();
    if (primary == null) {
      return super.toString();
    } // end of if ()
    else {
      return primary;
    } // end of else
  }
}

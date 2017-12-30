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

import java.util.Set;

/**
* A directed edge in a graph.  Each edge may be tagged with an
* arbitrary Object.
*
* Each edge may be assigned attributes which will be used when producing
* input files for AT&T's graph drawing program "dot."
  *
* @author Nathan Kitchen
* @author based on a package by Mike Wirthlin and Carl Worth
*/
public class Edge extends AbstractGraphComponent {
  /**
  * In dot format, the port that this edge leaves
    */
  private String _dotSourcePort;
  
  /**
  * In dot format, the port that this edge enters
    */
  private String _dotSinkPort;
   
  /**
  * The Node that this edge is entering
  */
  private Node _sink;
  
  /**
  * The node that this edge is leaving
  */
  private Node _source;
  
  /**
  * Constructs a new edge with no tag
  */
  public Edge() {
    this(null);
  }
   
  /**
  * Constructs a new edge with the given tag
  */
  public Edge(Object tag) {
    super(tag);
  }
  
  /**
  * Constructs a new edge with no tag from <code>source</code> to
  * <code>sink</code>, FOR POWER USERS ONLY!!!  This method alters the
  * data structures of <code>source</code> and <code>sink</code>.
  */
  public Edge(Node source, Node sink) {
    this(source, sink, null);
  }
  
  /**
  * Constructs a new edge with the given tag from <code>source</code> to
  * <code>sink</code>, FOR POWER USERS ONLY!!!  This method alters the
  * data structures of <code>source</code> and <code>sink</code>.
  */
  public Edge(Node source, Node sink, Object tag) {
    super(tag);
    setSource(source);
    setSink(sink);
    if (source != null)
      source.getOutEdges().add(this);
    if (sink != null)
      sink.getInEdges().add(this);
  }
  
  /**
  * Returns the port that this edge enters in dot format
    */
  public String getDotSinkPort() {
    return _dotSinkPort;
  }
  
  /**
  * Returns the port that this edge leaves in dot format
    */
  public String getDotSourcePort() {
    return _dotSourcePort;
  }
  
  /**
  * Returns the node that this edge is entering
  */
  public Node getSink() {
    return _sink;
  }
  
  /**
  * Returns the node that this edge is leaving
  */
  public Node getSource() {
    return _source;
  }
  
  /**
  * Returns the port that this edge enters in dot format.
    */
  public void setDotSinkPort(String port) {
    if (port != null && port.length() > 0 && port.charAt(0) == ':') {
      port = port.substring(1);
    } // end of if ()
    _dotSinkPort = port;
  }
  
  /**
  * Returns the port that this edge leaves in dot format
    */
  public void setDotSourcePort(String port) {
    if (port != null && port.length() > 0 && port.charAt(0) == ':') {
      port = port.substring(1);
    } // end of if ()
    _dotSourcePort = port;
  }
  
  /**
  * Redirects this edge to enter <code>sink</code>, FOR POWER USERS
  * ONLY!!!
  * This method will alter <code>sink</code>'s data structures.
  */
  public void setSink(Node sink) {
    if (_sink != null) {
      _sink.getInEdges().remove(this);
    }
    _sink = sink;
    if (sink != null) 
      sink.getInEdges().add(this);
  }
  
  /**
  * Redirects this edge to leave <code>source</code>, FOR POWER USERS
  * ONLY!!!
  * This method will alter <code>source</code>'s data structures.
  */
  public void setSource(Node source) {
    if (_source != null) {
      _source.getOutEdges().remove(this);
    }
    _source = source;
    if (source != null) 
      source.getOutEdges().add(this);
  }
  
  /**
  * Returns a representation of this edge in the language used by AT&T's
  * graph drawing program "dot"
  */
  public String toDot() {
    Node src = getSource();
    Node snk = getSink();
    StringBuffer buf = new StringBuffer(80);
    
    String srcPort, snkPort;
    srcPort = getDotSourcePort();
    snkPort = getDotSinkPort();
     
    // The reason that I null the ports here is because current versions
    // of dot have a bug that crashes when using ports and a loopback edge
    
    if (snk == src) {
      //String tmp_String = srcPort;
      //srcPort = snkPort;
      //snkPort = tmp_String;
      srcPort = null;
      snkPort = null;
    }
     
    buf.append(src.getDotName());
    if (srcPort != null) {
      buf.append(':').append(srcPort);
    } // end of if ()
    buf.append(" -> ").append(snk.getDotName());
    if (snkPort != null) {
      buf.append(':').append(snkPort);
    } // end of if ()
    if (getNumDotAttributes() > 0) {
      buf.append(" ").append(getDotAttributeList());
    } // end of if ()
    
    return buf.toString();
  }
}

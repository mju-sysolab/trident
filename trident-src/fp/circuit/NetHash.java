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

import java.util.HashMap;
import java.util.Iterator;

public class NetHash {
  
  private HashMap _hash;
  private Circuit _parent;
  
  NetHash(Circuit p) {
    _hash = new HashMap();
    _parent = p;
  }
  
  void addSink( String net, PortTag sink ) {
    if( _hash.containsKey( net ) ) {
      ((Net)_hash.get( net )).addSink( sink );
    } else {
      Net n = _parent.newNet(_parent,net).addSink(sink);
      _hash.put( net, n);
    }
  }
  
  void addSource( String net, PortTag source ) {
    if( _hash.containsKey( net ) ) {
      ((Net)_hash.get( net )).addSource( source );
    }  else {
      Net n = _parent.newNet(_parent,net).addSource(source);
      _hash.put( net, n);
    }
  }

  /*
  void setNetPullup(String net) {
    if (_hash.containsKey(net)) {
      ((Net)_hash.get(net)).setPullup(true);
    } else {
      System.out.println("Net " + net + " not yet created. Cannot create pulldown");
    }
  }

  void setNetPulldown(String net) {
    if (_hash.containsKey(net)) {
      ((Net)_hash.get(net)).setPulldown(true);
    } else {
      System.out.println("Net " + net + " not yet created. Cannot create pulldown.");
    }
  }
  */  

  public Net getNet( String name ) {
    return (Net)_hash.get( name );
  }
  
  void checkNets() {
    for(Iterator iter = _hash.keySet().iterator(); iter.hasNext(); ) {
      String name = (String)iter.next();
      Net n = (Net)_hash.get( name );
      System.out.println( "name: " + name );
    }
  }
}

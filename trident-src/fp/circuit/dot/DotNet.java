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
package fp.circuit.dot;

import java.util.Iterator;
import fp.circuit.*;



public class DotNet extends Net implements Dot {

  public DotNet(Circuit parent, String name) {
    super(parent, name);
  }


  public void build(String name, Object[] arg_o) {
    System.out.println("Building DotNet");
  }
    

  public String toDot() {
    StringBuffer s = new StringBuffer();
    for (Iterator in = sources.iterator(); in.hasNext(); ) {
      PortTag i = (PortTag)in.next();
      for (Iterator out = sinks.iterator(); out.hasNext(); ) {
        s.append("\""+i.getParent().getName()+"\":\""+i.getName()+"\"");
        s.append("->");
        PortTag p = (PortTag)out.next();
        s.append("\""+p.getParent().getName()+"\":\""+p.getName()+"\"");
                                                                                     
        String name = getName();
        if( name != null ) {
          s.append( " [ label = \"" + name + "("+width+")\" ]" );
        }
        s.append(";\n");
      }
    }
    return s.toString();
  }

}

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

package fp.util.vhdl.generator;

import java.util.HashSet;
import java.util.Iterator;


public class LibraryUnit extends VHDLBase implements VHDLout {

  // according to my revised parse stuff, this is goofy.
    
  // Entity
  // Architectures
  // Package
  // Configuration

  private Entity _entity;
  private Architecture _arch;

  private SimpleName _simple_name;

  public LibraryUnit(String name) {
    super(name);
    _simple_name = new SimpleName(name);
    _entity = new Entity(_simple_name);
    _arch = new Architecture(name);
  }
  
  public Entity getEntity() { return _entity; }
  void setEntity(Entity e) { _entity = e; }

  public Architecture getArchitecture() { return _arch; }
  void setArchitecture(Architecture a) { _arch = a; }


  protected void appendBody(StringBuffer s, String pre) {
    _entity.toVHDL(s,pre);
    _arch.toVHDL(s, pre);
  }

  
}

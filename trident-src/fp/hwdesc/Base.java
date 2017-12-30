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


package fp.hwdesc;

public abstract class Base {
  public String name = null;
  public int count = 0;
  public int size = 0;
  public int width = 0;
  public int depth = 0;
  public String type = "none";
  public int typeCode = 0;
  public String[] typeName = {};

  public Base() {}

  public void setTypeCode() {
    for (int i = 0; i < typeName.length; i++) {
      if (type.compareTo(typeName[i]) == 0) {
	typeCode = i;
	return;
      }
    }
    System.out.println("Description for " + name + " contains invalid type:  " + type);
  }
}

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

import java.util.*;

public class Platform extends BaseConfig {
  String hw_file_name;
  String interface_file_name;

  public Platform() { }

  void setHWFileName(String s) { hw_file_name = s; }
  public String getHWFileName() { return hw_file_name; }

  void setInterfaceFileName(String s) { interface_file_name = s; }
  public String getInterfaceFileName() { return interface_file_name; }

  public String toString() {
    StringBuffer sbuf = new StringBuffer("(platform ");
    sbuf.append(name).append("\n");
    if (isDefault()) {
      sbuf.append("\t(default yes)\n");
    }
    sbuf.append("\t(name ").append(name).append(" )\n");
    sbuf.append("\t(class ").append(class_name).append(" )\n");
    sbuf.append("\t(hardware ").append(hw_file_name).append(" )\n");
    sbuf.append("\t(interface ").append(interface_file_name).append(" )\n");
    sbuf.append(")\n");
    return sbuf.toString();
  }
  
}

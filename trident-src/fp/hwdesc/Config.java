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

public class Config {
  String name;
  String default_config;
  String default_target;
  Library default_library;
  Platform default_platform;

  HashMap library;
  HashMap platform;
  
  public Config() { 
    library = new HashMap();
    platform = new HashMap();
  }

  void setDefaultConfig(String s) {
    default_config = s;
  }

  public String getDefaultConfig() { return default_config; }
  public String getDefaultTarget() { return default_target; }

  void addLibrary(String s, Library l) { 
    if (l.isDefault()) {
      default_library = l;
    }
    library.put(s,l); 
  }
  public Library getLibrary(String s) { return (Library)library.get(s); }
  public Library getDefaultLibrary() { return default_library; }
  public HashMap getLibraries() { return library; }

  void addPlatform(String s, Platform p) { 
    if (p.isDefault()) { 
      default_platform = p;
    }
    platform.put(s, p); 
  }
  public Platform getPlatform(String s) { return (Platform)platform.get(s); }
  public Platform getDefaultPlatform() { return default_platform; }
  public HashMap getPlatforms() { return platform; }

  public String toString() {
    StringBuffer sbuf = new StringBuffer("(compiler ");
    sbuf.append(name).append("\n");
    
    if (default_config != null) {
      sbuf.append("\t(default_config ").append(default_config);
      sbuf.append(" )\n");
    }

    if (default_target != null) {
      sbuf.append("\t(default_target ").append(default_target);
      sbuf.append(" )\n");
    }

    for (Iterator iter = library.values().iterator(); iter.hasNext(); ) {
      sbuf.append(((Library)iter.next()).toString());
    }

    for (Iterator iter = platform.values().iterator(); iter.hasNext(); ) {
      sbuf.append(((Platform)iter.next()).toString());
    }

    sbuf.append(")\n");
    return sbuf.toString();
  }


}

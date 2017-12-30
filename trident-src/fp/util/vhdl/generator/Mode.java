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

public class Mode implements VHDLout {

  private static class UndefMode extends Mode {
    UndefMode() { super("undef"); }
  }
  public static final Mode UNDEF = new UndefMode(); 

  private static class In extends Mode {
    In() { super("in"); }
  }
  public static final Mode IN = new In();

  private static class Out extends Mode {
    Out() { super("out"); }
  }
  public static final Mode OUT = new Out();

  private static class Buffer extends Mode {
    Buffer() { super("buffer"); }
  }
  public static final Mode BUFFER = new Buffer();

  private static class InOut extends Mode {
    InOut() { super("inout"); }
  }
  public static final Mode INOUT = new InOut();

  private static class Linkage extends Mode {
    Linkage() { super("linkage"); }
  }
  public static final Mode LINKAGE = new Linkage();


  private final String _name;

  Mode(String n) {
    _name = n;
  }

  public StringBuffer toVHDL(StringBuffer s, String pre) {
    s.append(pre).append(_name);
    return s;
  }

  public String toString() {
    StringBuffer sbuf = new StringBuffer();
    return toVHDL(sbuf,"").toString();
  }
                                                                                
}

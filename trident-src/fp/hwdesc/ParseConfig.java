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

import java.io.*;
import java.util.*;

import fp.util.sexpr.SExprStream;
import fp.util.sexpr.SExprParserException;
import fp.util.sexpr.SimpleSExprStream;

import fp.util.FileTree;

public class ParseConfig {
  private Config _config;

  public ParseConfig(String filename) {
    //System.out.println("Parsing file "+filename);

    SExprStream p = null;
   
    InputStream is = fp.GlobalOptions.ft.getStream(filename);
    p = new SimpleSExprStream(is);

    p.setListsAsVectors(true);
    Object o = null;
    try { 
      o = p.parse();
      ParseConfigVisitor phv = new ParseConfigVisitor();
      phv.visit(o);
      _config = phv.getConfig();
    } catch (IOException e) {
      System.err.println("ParseConfig: IOException: Could not parse "
			 +filename);
      System.exit(-1);
    } catch (SExprParserException e) {
      System.err.println("ParseConfig: ParserException: Could not parse "
			 +filename);
      System.exit(-1);
    }

    //System.out.println("ParseConfig::_compiler "+_compiler);

  }

  public Config getConfig() { return _config; }

  public static void main(String args[]) {
    if (args.length == 1) {
      ParseConfig ph = new ParseConfig(args[0]);
      Config c = ph.getConfig();
      System.out.println("Config "+c);
    }
  }

}

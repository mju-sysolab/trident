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


package fp.circuit.vhdl;

import java.io.*;
import java.util.*;

import fp.util.sexpr.ForVisitor;
import fp.util.sexpr.SExprStream;
import fp.util.sexpr.SExprParserException;
import fp.util.sexpr.SimpleSExprStream;

public class ParseModule {
  
 // actual variables
  ParseModuleVisitor _pmv;

  public ParseModule(String filename) {
    SExprStream p = null;

    InputStream is = fp.GlobalOptions.ft.getStream(filename);
    p = new SimpleSExprStream(is);

    /*
    try {
      InputStream is = new FileInputStream(filename);
      p = new SimpleSExprStream(is);

    } catch (FileNotFoundException e) {
      System.err.println("Could not open "+filename);
      System.exit(-1);
    }
    */
    p.setListsAsVectors(true);
    Object o = null;
    try { 
      o = p.parse();
      
      // resolve for statments...
      ForVisitor fv = new ForVisitor();
      fv.visit(o);
      // parse my stuff ...
      _pmv = new ParseModuleVisitor();
      _pmv.visit(o);

    } catch (IOException e) {
      System.err.println("Could not parse "+filename);
      System.exit(-1);
    } catch (SExprParserException e) {
      System.err.println("Could not parse "+filename);
      System.exit(-1);
    }
  }

  public String getLibrary() { return _pmv.getLibrary(); }
  public HashMap getModules() { return _pmv.getModules(); }
  public Library getLib() { return _pmv.getLib(); }

  public static void main(String args[]) {
    if (args.length == 1) {
      ParseModule pm = new ParseModule(args[0]);
      // could be fancier.
      HashMap modules = pm.getModules();
      System.out.println("Modules "+modules);
    }
  }



}

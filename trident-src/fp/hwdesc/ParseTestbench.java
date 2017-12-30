package fp.hwdesc;

import java.io.*;
import java.util.*;

import fp.util.sexpr.SExprStream;
import fp.util.sexpr.SExprParserException;
import fp.util.sexpr.SimpleSExprStream;

import fp.util.FileTree;

public class ParseTestbench {
  private FabIn _fabin;

  public ParseTestbench(String filename) {
    //System.out.println("Parsing file "+filename);

    SExprStream p = null;
   
    InputStream is = fp.GlobalOptions.ft.getStream(filename);
    p = new SimpleSExprStream(is);

    /*
    try {
      //InputStream is = new FileInputStream(filename);
      InputStream is = FileTree.getStream(filename);
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
      ParseTestbenchVisitor ptv = new ParseTestbenchVisitor();
      ptv.visit(o);
      _fabin = ptv.getFabIn();
    } catch (IOException e) {
      System.err.println("ParseTestbench: IOException: Could not parse "
			 +filename);
      System.exit(-1);
    } catch (SExprParserException e) {
      System.err.println("ParseTestbench: ParserException: Could not parse "
			 +filename);
      System.exit(-1);
    }

    //System.out.println("ParseHardware::_hardware "+_hardware);

  }

  public FabIn getFabIn() { return _fabin; }


  public static void main(String args[]) {
    if (args.length == 1) {
      ParseTestbench ph = new ParseTestbench(args[0]);
      FabIn f = ph.getFabIn();
      System.out.println(f);
    }
  }

}

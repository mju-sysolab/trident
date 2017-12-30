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

import java.util.*;
import java.util.regex.Pattern;

import fp.util.vhdl.generator.Add;
import fp.util.vhdl.generator.Component;
import fp.util.vhdl.generator.Div;
import fp.util.vhdl.generator.Expression;
import fp.util.vhdl.generator.IndexConstraint;
//import fp.util.vhdl.generator.InterfaceConstant;
//import fp.util.vhdl.generator.InterfaceSignal;
import fp.util.vhdl.generator.Mult;
import fp.util.vhdl.generator.NumericLiteral;
import fp.util.vhdl.generator.Operation;
import fp.util.vhdl.generator.Primary;
import fp.util.vhdl.generator.Range;
import fp.util.vhdl.generator.SimpleName;
import fp.util.vhdl.generator.Sub;
import fp.util.vhdl.generator.SubType;

import fp.util.sexpr.StackVisitor;
import fp.util.sexpr.Symbol;

class ParseModuleVisitor extends StackVisitor {
  // this was the file name.
  private String _library;
  private HashMap _modules;
  private Library _lib;

  static final String tokens[] = {
    "library",
    "libname",
    "libinclude",
    "libobject",
    "generic",
    "port",
    "map",
    "gmap",
    "integer",
    "boolean",
    "in",
    "out",
    "inout",
    "name",
    "type",
    "size",
    "for",
    "tag",
    "id",
    "open",
    "low",
    "high",
  };

  static final int LIBRARY = 0;
  static final int LIBNAME = 1;
  static final int LIBINCLUDE = 2;
  static final int LIBOBJECT = 3;
  static final int GENERIC = 4;
  static final int PORT = 5;
  static final int MAP = 6;
  static final int GMAP = 7;
  static final int INTEGER = 8;
  static final int BOOLEAN = 9;
  static final int IN = 10;
  static final int OUT = 11;
  static final int INOUT = 12;
  static final int NAME = 13;
  static final int TYPE = 14;
  static final int SIZE = 15;
  static final int FOR = 16;
  static final int TAG = 17;
  static final int ID = 18;
  static final int OPEN = 19;
  static final int LOW = 20;
  static final int HIGH = 21;

  static final Pattern SIMPLENAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");
  static final Pattern OPERATORS = Pattern.compile("[\\*\\+-/]");
  
  HashMap getModules() { return _modules; }
  String getLibrary() { return _library; } // this is poorly named now.
  Library getLib() { return _lib; }

   //public void forSymbol(Symbol that) { }

  public void forVector(Vector that) { 
    int size = that.size();

    if (size > 0) {
      String s = getLabel(that);

      //System.out.println(" Vector "+that);
      //System.out.println(" Stack "+stack);
      
      // skip non descriptive ??
      if (s == null) return;

      // this feels dumb
      if (tokens[LIBRARY].equals(s)) { 
        stack.push(parseLibrary(that)); }
      else if (tokens[LIBNAME].equals(s)) { parseLibName(that); }
      else if (tokens[LIBINCLUDE].equals(s)) { parseLibInclude(that); }
      else if (tokens[LIBOBJECT].equals(s)) { 
        stack.push(parseLibObject(that) ); }
      else if (tokens[GENERIC].equals(s)) {  
        stack.push(parseGeneric(that)); }
      else if (tokens[PORT].equals(s)) {  
        stack.push(parsePort(that)); }
      else if (tokens[MAP].equals(s)) { parseMap(that); }
      else if (tokens[GMAP].equals(s)) { parseGMap(that); }
      else if (tokens[INTEGER].equals(s)) { parseInteger(that); }
      else if (tokens[BOOLEAN].equals(s)) { parseBoolean(that); }
      else if (tokens[NAME].equals(s)) { parseName(that); }
      else if (tokens[TYPE].equals(s)) { parseType(that); }
      else if (tokens[SIZE].equals(s)) { parseSize(that); }
      else if (tokens[TAG].equals(s)) { parseTag(that); }
      else if (tokens[ID].equals(s)) { parseID(that); }
      else if (tokens[OPEN].equals(s)) { parseOpen(that); }
      else if (OPERATORS.matcher(that.lastElement().toString()).matches()) { 
	parseEq(that); }
      else { 
        System.err.println("Unknown Token "+s);
        System.exit(-1);
      }
    }
  } 

  //public void forString(String that) { }
  //public void forNumber(Number that) { }
  //public void forUnknown(Object that) { }
  
  protected Object pop(Vector v) {
    String s = getLabel(v);
    if (tokens[LIBRARY].equals(s)) { 
      return super.pop(v); }
    else if (tokens[LIBOBJECT].equals(s)) { 
      return super.pop(v); }
    else if (tokens[GENERIC].equals(s)) {
      // when we pop, we have to collect the created objects.
      Generic gen = (Generic)super.pop(v);
      LibObject lo = (LibObject)stack.peek();
      lo.addGeneric(gen);
      return gen; }
    else if (tokens[PORT].equals(s)) {
      ParsePort port = (ParsePort)super.pop(v);
      LibObject lo = (LibObject)stack.peek();
      lo.addPort(port);
      return port; }
    else if (tokens[SIZE].equals(s)) {
      // I don't think I will do it this way...
      return null;
    } else
      return null;
  }
  

  Object parseLibrary(Vector that) {
    _modules = new HashMap();
    Library lib = new Library(that.elementAt(1).toString());
    _lib = lib;
    return lib;
  }
    
  void parseLibName(Vector that) {
    Library l = (Library)stack.peek();
    l.libname = that.elementAt(1).toString();
  }

   void parseLibInclude(Vector that) {
    Library l = (Library)stack.peek();
    l.include = that.elementAt(1).toString();
  }

  Object parseLibObject(Vector that) {
    Library l = (Library)stack.peek();
    String libobject = that.elementAt(1).toString();
    String objectname = that.elementAt(2).toString();

    LibObject lo = new LibObject(libobject, objectname, l);
    l.addLibObject(objectname, lo);

    _modules.put(objectname, lo.getVHDLModule());

    return lo;
  }

  Object parseGeneric(Vector that) {
    Generic gen = new Generic(that.elementAt(1).toString());
    return gen;
  }

  Object parsePort(Vector that) {
    // elementAt(1) is not the name, it is the direction (in,out,inout ...)
    ParsePort port = new ParsePort(that.elementAt(1).toString());
    return port;
  }

  void parseMap(Vector that) {
    String first = that.elementAt(1).toString();
    Object second = that.elementAt(2);
    if (!(second instanceof Number)) {
      second = second.toString();
    }
    //VHDLModule vm = (VHDLModule)stack.peek();
    //vm.getPortMap().put(first, second);
    LibObject lo = (LibObject)stack.peek();
    lo.addPortMap(first, second);
  }

  void parseGMap(Vector that) {
    String first = that.elementAt(1).toString();
    Object second = that.elementAt(2);
    if (!(second instanceof Number)) {
      second = second.toString();
    }
    //VHDLModule vm = (VHDLModule)stack.peek();
    //vm.getGenericMap().put(first, second);
    LibObject lo = (LibObject)stack.peek();
    lo.addGenericMap(first, second);
  }
  
  void parseInteger(Vector that) {
    Generic gen = (Generic)stack.peek();
    gen.type = SubType.INTEGER;
    gen.value = new NumericLiteral(((Number)that.elementAt(1)).intValue());
  }

  void parseBoolean(Vector that) {
    Generic gen = (Generic)stack.peek();
    gen.type = SubType.BOOLEAN;
    gen.value = new SimpleName(that.elementAt(1).toString());
  }
	
  void parseName(Vector that) {
    // this is just for ports ...
    ParsePort p = (ParsePort)stack.peek();
    p.setName(that.elementAt(1).toString());
  }
  
  void parseType(Vector that) {
    ParsePort p = (ParsePort)stack.peek();
    String type = that.elementAt(1).toString();
    if ("std_logic".equals(type)) {
      p.type = SubType.STD_LOGIC;
    } else {
      System.err.println("parseType(): unknown type "+type);
      System.exit(-1);
    }
  }
  
  void parseSize(Vector that) {
    ParsePort p = (ParsePort)stack.peek();
    if (that.elementAt(1) instanceof Vector) {
      // do nothing ...???
    } else if (that.elementAt(1) instanceof Number) {
      int size = ((Number)that.elementAt(1)).intValue();
      p.width = size;
      if (size == 1) {
	p.size = null;
      } else {
	p.size = new IndexConstraint(new Range(new NumericLiteral(size - 1),
					       "downto", NumericLiteral.ZERO));
      }
    }
  }

  void parseTag(Vector that) {
    ParsePort p = (ParsePort)stack.peek();
    p.tag = that.elementAt(1).toString();
  }

  void parseID(Vector that) {
    ParsePort p = (ParsePort)stack.peek();
    String id = that.elementAt(1).toString();
    // nothing uses this.
  }

  void parseOpen(Vector that) {
    ParsePort p = (ParsePort)stack.peek();
    p.open = that.elementAt(1).toString();
  }

  void parseEq(Vector that) {
    //System.out.println("parseEq() "+that);
    ParsePort p = (ParsePort)stack.peek();
    Stack s = new Stack();

    for (Iterator iter=that.iterator(); iter.hasNext();) {
      Object object = iter.next();
      if (object instanceof Number)
	s.push(new NumericLiteral(((Number)object).intValue()));
      else {
	String string = object.toString();
	if (SIMPLENAME.matcher(string).matches()) {
	  // this could be slightly wasteful
	  // could hash them to see if we can reuse.
	  s.push(new SimpleName(string));
	} else if (OPERATORS.matcher(string).matches()) {
	  Object operand_a = s.pop();
	  Object operand_b = s.pop();

	  if ("+".equals(string)) {
	    // math in VHDLgen sucks
	    Operation o = new Add(operand_a, operand_b);
	    s.push(o);
	  } else if ("-".equals(string)) {
	    Operation o = new Sub(operand_a, operand_b);
	    s.push(o);
	  } else if ("*".equals(string)) {
	    Operation o = new Mult(operand_a, operand_b);
	    s.push(o);
	  } else if ("/".equals(string)) {
	    Operation o = new Div(operand_a, operand_b);
	    s.push(o);
	  } else {
	    System.err.println("Unknown operand "+string);
	    System.exit(-1);
	  }
	} else {
	  System.err.println("Unknown input "+string);
	  System.exit(-1);
	}
      }
    }

    // result should be on the stack ...

    Object result = s.pop();
    // subtract one 
    result = new Sub(result, NumericLiteral.ONE);
    p.size = new IndexConstraint(new Range(result, "downto", 
					   NumericLiteral.ZERO));
    //System.out.println("parseEq() "+that);
  }
      
  

} 
  
  
 
  

  
    

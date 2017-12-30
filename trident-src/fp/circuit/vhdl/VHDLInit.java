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
import java.io.*;

import fp.util.vhdl.generator.*;

public class VHDLInit {
  
  /* data that needs to be known:
   what library
   what ports map to what ports
   outs -> VHDL outs -- I don't think direction is important
   ins -> VHDL ins
   what generics map to what -- ??
   needs clock ?
   what is the library name
   what is the VHDL name

   I need one more thing -- the pre-declaration of the VHDL ...
   I could :
     1) put in a big chunk of text that is "correct"
     2) record all the necessary bits of data and build it (ugh).
       vhdl name,
       port
         direction
	 name
	 size
       generic
         name
	 type
	 value
   */
      
  static final String START = "(";
  static final String END = ")";

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

  static final Pattern FP = Pattern.compile("-?[0-9]*\\.[0-9]+");
  static final Pattern HEX = Pattern.compile("0x[A-Fa-f_0-9]+");
  static final Pattern NUM = Pattern.compile("\\d+");
  // could allow white space between numbers ...
  static final Pattern SPAN = Pattern.compile("\\d+\\.\\.\\d+");

  static final Pattern SIMPLENAME = Pattern.compile("[a-z][a-z0-9_]*");


  // actual variables
  private String _library;
  private HashMap _modules;

  VHDLInit(String library) {
    _library = library;
    _modules = new HashMap(); 

    parseFile(library);
  }

  HashMap getModules() { return _modules; }
  String getLibrary() { return _library; }

  // having this separate from the constructor allows for 
  // multiple library files ....
  void parseFile(String library) {
    StreamTokenizer st = null;
    Reader r = null;
    try {
      InputStream is = new FileInputStream(library);
      r = new BufferedReader(new InputStreamReader(is));

    } catch(FileNotFoundException e) {
      System.err.println("Could not open "+library);
      System.exit(-1);
    }

    st = new StreamTokenizer(r);
    st.ordinaryChars('0', '9');
    st.ordinaryChar('-');
    st.ordinaryChar('.');
    st.ordinaryChar('?');
    st.eolIsSignificant(false);
    st.lowerCaseMode(true);

    // this is for hex strings...
    st.wordChars('_','_');
    // this allows floating point/negative numbers
    st.wordChars('0','9');
    st.wordChars('-','-');
    st.wordChars('.','.');
    st.wordChars('?','?');
    // this allows "long quoted strings"
    st.quoteChar('"');
    // this is consistent with python
    st.commentChar('#');
    
    parseLibrary(st);
  }

  String token;

  void parseLibrary(StreamTokenizer st) {

    //String type = null;
    String libname = null;
    String libinclude = null;
    String objectname = null;
    String libobject = null;

    if (getStart(st)) {
      token = getNextToken(st);
      if (tokens[LIBRARY].equals(token)) {
	String library = getNextToken(st);
	//System.out.println("lib "+library);
      
	while(getStart(st)) {
	  token = getNextToken(st);
	  if (tokens[LIBNAME].equals(token)) {
	    libname = getNextToken(st);
	    getEnd(st);
	    //System.out.println(" name "+libname);
	  } else if (tokens[LIBINCLUDE].equals(token)) {
	    libinclude = getNextToken(st);
	    getEnd(st);
	    //System.out.println(" inc "+libinclude);
	  } else if (tokens[LIBOBJECT].equals(token)) {
	    libobject = getNextToken(st);
	    objectname = getNextToken(st);
	    //System.out.println(" obj "+objectname+" "+libobject);
	    SimpleName name = new SimpleName(libobject);
	    // do I need a unique name for a component?
	    Component comp = new Component(name);
	    HashMap map = new HashMap();
	    HashMap gmap = new HashMap();
	    HashMap open = new HashMap();
	    HashMap tags = new HashMap();
	    parseObject(st, comp, map, gmap, open, tags, null);
	    getEnd(st);
	    //System.out.println("comp "+comp);
	    //System.out.println("map "+map);
	    VHDLModule vm = new VHDLModule(libobject, objectname,
					   library, libinclude, comp);
	    // temporary fix
	    vm.setPortMap(map);
	    vm.setGenericMap(gmap);

	    _modules.put(objectname, vm);
	    
	    //System.out.println(" VHDLModule "+objectname+"\n"+vm);

	  } else {
	    System.err.println("Unknown token "+token+" line no "+st.lineno());
	    System.exit(-1);
	  }
	}
      } else {
	System.err.println("Unknown token "+token+" line no "+st.lineno());
      }
      getEnd(st);
      


      // this has to be done due to the fact that things can appear out of 
      // order.
      for(Iterator iter=_modules.values().iterator(); iter.hasNext();) {
	VHDLModule mod = (VHDLModule)iter.next();
	mod.setInclude(libinclude);
      }
    } else {
      System.err.println("Cannot find start token ("+token
			 +") line no "+st.lineno());
      System.exit(-1);
    }
  }


  void parseObject(StreamTokenizer st, Component comp,
		   HashMap map, HashMap gmap, HashMap open,
		   HashMap tags, Integer index) {
    //System.out.println("parseObject");
    while(getStart(st)) {
      token = getNextToken(st);

      if (tokens[GENERIC].equals(token)) {
	InterfaceConstant ic = parseGeneric(st, index);
	// add to component
	comp.addGeneric(ic);
	getEnd(st);
      } else if (tokens[PORT].equals(token)) {
	InterfaceSignal is = parsePort(st, open, tags, index);
	comp.addPort(is);
	getEnd(st);
      } else if (tokens[MAP].equals(token)) {
	String first = getNextToken(st);
	String second = getNextToken(st);
	
	map.put(updateIndex(first, index),updateIndex(second, index));
	getEnd(st);
      } else if (tokens[GMAP].equals(token)) {
	String first = getNextToken(st);
	String second = getNextToken(st);
	gmap.put(updateIndex(first, index), updateIndex(second, index));
	getEnd(st);
      } else if (tokens[FOR].equals(token)) {
	// I think this will be a string since I added . to the okay list.
	// ugh -- recursion could work ... but there is no way to combine
	// indices of differing levels ...
	String span = getNextToken(st);
	if (SPAN.matcher(span).matches()) {
	  String[] indices = span.split("\\.\\.");

	  int start = Integer.parseInt(indices[0]);
	  // could be null or dangerous ... no not anymore, I can split
	  // on regexp!!! cool beans.
	  int stop = Integer.parseInt(indices[1]);

	  // what about steps ??
	  for(int i=start; i<=stop; i++) {
	    System.out.println(" parseObject "+ i);
	    parseObject(st, comp, map, gmap, open, tags, new Integer(i));
	  }
	  getEnd(st);
	} else {
	  System.err.println("Improper for loop: "+span);
	  System.exit(-1);
	}
      } else {
	System.err.println("Unknown token "+token);
	System.exit(-1);
      }
    }
  }
    

  InterfaceConstant parseGeneric(StreamTokenizer st, Integer index) {
    //System.out.println("parseGeneric");
    token = getNextToken(st);
    SimpleName name = new SimpleName(updateIndex(token, index));
    SubType type = null;
    Primary value = null;

    while(getStart(st)) {
      token = getNextToken(st);
      if (tokens[INTEGER].equals(token)) {
	type = SubType.INTEGER;
	if (!getEnd(st)) {
	  token = getNextToken(st);
	  value = new NumericLiteral(Integer.parseInt(token));
	  getEnd(st);
	}
      } else if (tokens[BOOLEAN].equals(token)) {
	type = SubType.BOOLEAN;
	if (!getEnd(st)) {
	  token = getNextToken(st);
	  // true or false
	  value = new SimpleName(token);
	  getEnd(st);
	}
      } else {
	System.err.println("Illegal type");
	System.exit(-1);
      }
    }
    getEnd(st);

    return new InterfaceConstant(name, type, new Expression(value));
  }

  InterfaceSignal parsePort(StreamTokenizer st, HashMap open, HashMap tags,
			    Integer index) { 
    //System.out.println("parsePort");
    Mode mode = parseMode(st);
    SimpleName name = null;
    String _name = null;
    IndexConstraint size = null;
    String tag = null;
    String open_choice = null;

    String type_s = null;
    SubType type = null;

    while(getStart(st)) {
      token = getNextToken(st);
      if (tokens[NAME].equals(token)) {
	token = getNextToken(st);
	_name = updateIndex(token, index);
	name = new SimpleName(_name);
	getEnd(st);
      } else if (tokens[TAG].equals(token)) {
	token = getNextToken(st);
	// add tag to hash.
	tag = updateIndex(token,index);
	getEnd(st);
      } else if (tokens[TYPE].equals(token)) {
	type_s = getNextToken(st);
	getEnd(st);
      } else if (tokens[SIZE].equals(token)) {
	size = parseSize(st);
	getEnd(st);
      } else if (tokens[OPEN].equals(token)) {
	open_choice = getNextToken(st);
	getEnd(st);
      } else {
	System.err.println("Illegal token "+token);
	System.exit(-1);
      }
    }
    
    if (tag != null) {
      tags.put(_name, tag);
    }

    if (open_choice != null) {
      open.put(_name, open_choice);
    }


    // this is lame.
    if (size != null) {
      if ("std_logic".equals(type_s)) {
	type = new SubType(new SimpleName("std_logic_vector"),
			   size);
      } else {
	System.err.println("parsePort(): unknown type "+type_s);
	System.exit(-1);
      }
    } else {
      // ugh
      if ("std_logic".equals(type_s)) {
	type = SubType.STD_LOGIC;
      } else {
	System.err.println("parsePort(): unknown type "+type_s);
	System.exit(-1);
      }
    }

    InterfaceSignal signal = new InterfaceSignal(name, mode, type);
    return signal;
  }

  IndexConstraint parseSize(StreamTokenizer st) {
    IndexConstraint result = null;
    SimpleExpression size_s = null;
    int size_i = 0;

    if (getStart(st)) {
      String term_s = getNextToken(st);
      Primary term = null;
      // parse first operand
      if (SIMPLENAME.matcher(term_s).matches()) 
	term = new SimpleName(term_s);
      else 
	term = new NumericLiteral(Integer.parseInt(term_s));

      size_s = new SimpleExpression(term);

      // parse remaining operands
      while(!getEnd(st)) {
	String op = getNextToken(st);
	term_s = getNextToken(st);
	      
	if (SIMPLENAME.matcher(term_s).matches()) 
	  term = new SimpleName(term_s);
	else 
	  term = new NumericLiteral(Integer.parseInt(term_s));

	if ("+".equals(op)) {
	  size_s.add(term);
	} else if ("-".equals(op)) {
	  size_s.sub(term);
	} else {
	  System.err.println("unknown operator");
	  System.exit(-1);
	}
      }
    } else {
      String num = getNextToken(st);
      size_i = Integer.parseInt(num);
    }
    
    if (size_s == null) {
      if (size_i == 1) 
	result = null; // ?? means that the size is one...
       else
         result = new IndexConstraint(new Range(new NumericLiteral(size_i - 1),
						"downto",  
						NumericLiteral.ZERO));
    } else {
      size_s.sub(NumericLiteral.ONE);  
      //System.out.println(" expression "+size_s);
      // very limiting...
      result = new IndexConstraint(new Range(size_s, "downto", 
					     NumericLiteral.ZERO));
    }
    return result;
  }


 

  Mode parseMode(StreamTokenizer st) {
    token = getNextToken(st);
    getEnd(st);
    if (tokens[IN].equals(token)) 
      return Mode.IN;
    else if (tokens[OUT].equals(token))
      return Mode.OUT;
    else if (tokens[INOUT].equals(token))
      return Mode.INOUT;

    return (Mode)null;
  }


  static final String updateIndex(String s, Integer i) {
    if (i != null) 
      return s.replaceAll("\\?",i.toString());
    else 
      return s;
  }


  boolean getStart(StreamTokenizer st) {
    String s = getNextToken(st);
    if (START.equals(s)) 
      return true;
    else
      st.pushBack();
    return false;
  }

  boolean getEnd(StreamTokenizer st) {
    String s = getNextToken(st);
    if (END.equals(s)) 
      return true;
    else
      st.pushBack();
    return false;
  }


  String getNextToken(StreamTokenizer st) {
     String s = null;
     try {
       if (st.nextToken() != StreamTokenizer.TT_EOF) {
	 switch(st.ttype) {
	 case StreamTokenizer.TT_EOL:
	   break;
	 case StreamTokenizer.TT_NUMBER:
	   //s = Double.toString(st.nval);
	   s = st.sval;
	   break;
	 case StreamTokenizer.TT_WORD:
	   s = st.sval; // Already a String
	   break;
	 case '"':
	   s = st.sval; // already a string
	   break;
	 default: // single character in ttype
	   s = String.valueOf((char)st.ttype);
	 }
       }
     } catch(IOException e) {
       System.out.println("st.nextToken() unsuccessful");
     }

     //System.out.println(" tok "+s);
     return s;
  }


  String getNextToken(StreamTokenizer st, int count) {
    String s = null;
    for (int i=0; i<count; i++) 
      s = s + getNextToken(st);
    return s;
  }

  // this only works with terminal groups    
  String getGroup(StreamTokenizer st) {
    String s = null;
    s = getNextToken(st);
    if (START.equals(s)) {
      s = "";
      String next = null;
      // this might be dangerous
      while(!END.equals(next)) {
	s = s + next;
	next = getNextToken(st);
	if (START.equals(next)) {
	  System.err.println("Oops extra paren?");
	  System.exit(-1);
	}
      }
    }
    return s;
  }



  public static void main(String args[]) {
    if (args.length == 1) {
      VHDLInit vi = new VHDLInit(args[0]);
    }

  }

}


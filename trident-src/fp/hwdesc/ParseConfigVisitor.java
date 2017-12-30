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

import fp.util.sexpr.StackVisitor;
import fp.util.sexpr.Symbol;

class ParseConfigVisitor extends StackVisitor {
  private Config _config;

  static final String tokens[] = {
    "compiler",
    "config_default",
    "target_default",
    "library",
    "default",
    "name",
    "short_name",
    "file",
    "require",
    "platform",
    "hardware",
    "interface",
    "class",
  };

  static final int COMPILER = 0;
  static final int CONFIG_DEFAULT = 1;
  static final int TARGET_DEFAULT = 2;
  static final int LIBRARY = 3;
  static final int DEFAULT = 4;
  static final int NAME = 5;
  static final int SHORT_NAME = 6;
  static final int FILE = 7;
  static final int REQUIRE = 8;
  static final int PLATFORM = 9;
  static final int HARDWARE = 10;
  static final int INTERFACE =  11;
  static final int CLASS =  12;

  
  //public void forSymbol(Symbol that) { }

  public void forVector(Vector that) { 
    int size = that.size();

    if (size > 0) {
      String s = getLabel(that);

      //System.out.println(" Vector "+that);
      //System.out.println(" Stack "+stack);
      
      // this feels dumb
      if (tokens[COMPILER].equals(s)) { 
	stack.push(parseConfig(that)); }
      else if (tokens[CONFIG_DEFAULT].equals(s)) { parseConfigDefault(that); }
      else if (tokens[TARGET_DEFAULT].equals(s)) { parseTargetDefault(that); }
      else if (tokens[LIBRARY].equals(s)) { 
	stack.push(parseLibrary(that)); }
      else if (tokens[DEFAULT].equals(s)) { parseDefault(that); }
      else if (tokens[NAME].equals(s)) {  parseName(that); }
      else if (tokens[SHORT_NAME].equals(s)) {  parseShortName(that); }
      else if (tokens[FILE].equals(s)) {  parseFile(that); }
      else if (tokens[REQUIRE].equals(s)) {  parseRequire(that); }
      else if (tokens[PLATFORM].equals(s)) {  
	stack.push(parsePlatform(that)); }
      else if (tokens[HARDWARE].equals(s)) {  parseHardware(that); }
      else if (tokens[INTERFACE].equals(s)) {  parseInterface(that); }
      else if (tokens[CLASS].equals(s)) {  parseClass(that); }
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
    if (tokens[COMPILER].equals(s)) { 
      return super.pop(v); }
    else if (tokens[LIBRARY].equals(s)) { 
      Library lib = (Library)super.pop(v);
      ((Config)stack.peek()).addLibrary(lib.getName(), lib);
      return lib; }
    else if (tokens[PLATFORM].equals(s)) {
      Platform p = (Platform)super.pop(v);
      ((Config)stack.peek()).addPlatform(p.getName(), p);
      return p; }
    else
      return null;
  }
    


  Object parseConfig(Vector v) {
    _config = new Config();
    // I think this is the name
    _config.name = v.elementAt(1).toString();
    return _config;
  }

  void parseConfigDefault(Vector v) {
    Config c = (Config)stack.peek();
    c.default_config = v.elementAt(1).toString();
  }

  void parseTargetDefault(Vector v) {
    Config c = (Config)stack.peek();
    c.default_target = v.elementAt(1).toString();
  }
 
  Object parseLibrary(Vector v) {
    Library _lib = new Library();
    return _lib;
  } 

  void parseDefault(Vector v) {
    BaseConfig _base = (BaseConfig)stack.peek();
    String is_default = v.elementAt(1).toString();
    if ("yes".equals(is_default) || "true".equals(is_default)) {
      _base.setDefault(true);
    }
  }

  void parseName(Vector v) {
    BaseConfig _base = (BaseConfig)stack.peek();
    _base.name = v.elementAt(1).toString();
  }

  void parseShortName(Vector v) {
    Library _lib = (Library)stack.peek();
    _lib.short_name = v.elementAt(1).toString();
  }

  void parseFile(Vector v) {
     Library _lib = (Library)stack.peek();
    _lib.file_name = v.elementAt(1).toString();
  }

  void parseRequire(Vector v) {
    Library _lib = (Library)stack.peek();
    _lib.addRequiredFile(v.elementAt(1).toString());
  }

  Object parsePlatform(Vector v) {
    Platform _p = new Platform();
    return _p;
  } 

  void parseHardware(Vector v) {
    Platform _p = (Platform)stack.peek();
    _p.hw_file_name = v.elementAt(1).toString();
  }

  void parseInterface(Vector v) {
    Platform _p = (Platform)stack.peek();
    _p.interface_file_name = v.elementAt(1).toString();
  }

  void parseClass(Vector v) {
    BaseConfig _base = (BaseConfig)stack.peek();
    _base.class_name = v.elementAt(1).toString();
  }


  Config getConfig() { return _config; }
  
}

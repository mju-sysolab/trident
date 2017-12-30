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

import java.util.HashMap;
import java.util.Iterator;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

public class DesignUnit extends VHDLBase implements VHDLout {
    
  // converted these to hash maps, so I can add more than one
  // without worrying about the consequences.
  private HashMap _libraries;
  private HashMap _use_clauses;
  private LibraryUnit _lib_unit;

  private String _external_file;

  public DesignUnit(String name) {
    super(name);
    _libraries = new HashMap();
    _use_clauses = new HashMap();
    _lib_unit = new LibraryUnit(name);
  }

  public DesignUnit(String name, String external_file) {
    super(name);
    _external_file = external_file;

  }
  
  HashMap getLibraries() { return _libraries; }
  public void addLibrary(String lib) { 
    _libraries.put(lib.toLowerCase(), lib);
  }

  HashMap getUses() { return _use_clauses; }
  public void addUse(Use u) { 
    String use = u.getText().toLowerCase();
    _use_clauses.put(use,u); 
  }

  public LibraryUnit getLibraryUnit() { return _lib_unit; }
  void setLibraryUnit(LibraryUnit lu) { 
    _lib_unit = lu; 
  }


  protected void appendHeader(StringBuffer s, String pre) {
    super.appendHeader(s, pre);
    appendLibraries(s, pre);
    appendUses(s, pre);
    // then append the library and uses statements ??
  }

  void appendLibraries(StringBuffer s, String pre) {
    int count = _libraries != null ? _libraries.size() : 0;
    if (count > 0) {
      s.append(pre).append("library ");
      for(Iterator iter = _libraries.values().iterator(); iter.hasNext(); ) {
	s.append(((String)iter.next()));
	count--;
	if (count > 0) s.append(", ");
      }
      s.append(";\n\n");
    }
  }

  void appendUses(StringBuffer s, String pre) {
    int count = _use_clauses != null ? _use_clauses.size() : 0;
    if (count > 0) {
      for(Iterator iter = _use_clauses.values().iterator(); iter.hasNext(); ) {
	((Use)iter.next()).toVHDL(s, pre);
      }
      s.append("\n");
    }
  }
	
    

  protected void appendBody(StringBuffer s, String pre) {
    if (_lib_unit != null)
      _lib_unit.toVHDL(s, pre);
    if (_external_file != null) {
      try {
	BufferedReader read = new BufferedReader(new FileReader(_external_file));
	String line;
	s.append(new Comment(" -----------------------------------------" ));
	s.append(new Comment(" Begin inserted file "+_external_file));
	s.append(new Comment(" -----------------------------------------" ));
	while( (line=read.readLine())!=null ) 
	  s.append(line).append("\n");
	s.append(new Comment(" -----------------------------------------" ));
	s.append(new Comment(" End inserted file "+_external_file));
	s.append(new Comment(" -----------------------------------------" ));
	read.close();
      } catch (IOException e) {
	System.out.println(" I had trouble reading "+_external_file);
	e.printStackTrace();
	System.exit(-1);
	
      }

    }
  }


  
}

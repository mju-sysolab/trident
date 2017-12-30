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

public class Library {
  String name;
  String libname;
  String include;
  
  private HashMap _lib_objects;
  //private HashMap _name_objects;

  public Library(String s) {
    name = s;
    _lib_objects = new HashMap();
  }
  
  void addLibObject(String lib, LibObject lo) {
    _lib_objects.put(lib, lo);
  }

  /*
  void addNameObjects(String name, LibObject lo) {
    _name_objects.put(name, lo);
  }
  */

  public LibObject getLibObject(String lib) {
    return (LibObject)_lib_objects.get(lib);
  }

  /*
  LibObject getNameObject(String name) {
    return (LibObject)_name_objects.get(name);
    } */
  
  

}

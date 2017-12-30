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


package fp.hardware;

import java.util.*;

import fp.flowgraph.*;

/** This class saves information about arrays from the c program.
 * 
 * @author Kris Peterson
 */
public class ArrayInfo
{
  private String _name;
  private Operand _var;
  private float _startAddress;
  private float _stopAddress;
  private int _wordSize;
  private float _arraySize;
  
  public ArrayInfo(Operand var, float arraySize, int wordSize) {
  
    _var = var;
    _name = var.toString();
    _wordSize = wordSize;
    _arraySize = arraySize;
  
  }
  
  public Operand getVar() {return _var;}
  public int getWordSize() {return _wordSize;}
  public float getArraySize() {return _arraySize;}
  public float getStart() {return _startAddress;}
  public void setStart(float stAddy) {
  
    _startAddress = stAddy;
    _stopAddress = stAddy + _arraySize;
  
  }
  public float getStop() {return _stopAddress;}

}

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

class ParseHardwareVisitor extends StackVisitor {
  private Hardware _hardware = new Hardware();

  static final String tokens[] = {
    "hardware",
    "chip",
    "count",
    "name",
    "area_max",
    "area_min",
    "area",
    "resource",
    "address",
    "size",
    "dimensions",
    "width",
    "depth",
    "clock",
    "port",
    "read_latency",
    "write_latency",
    "memAccessInst",
    "latency",
    "sliceCnt",
    "addressable_size",
  };

  static final int HARDWARE = 0;
  static final int CHIP = 1;
  static final int COUNT = 2;
  static final int NAME = 3;
  static final int AREA_MAX = 4;
  static final int AREA_MIN = 5;
  static final int AREA = 6;
  static final int RESOURCE = 7;
  static final int ADDRESS = 8;
  static final int SIZE = 9;
  static final int DIMENSIONS = 10;
  static final int WIDTH = 11;
  static final int DEPTH = 12;
  static final int CLOCK = 13;
  static final int PORT = 14;
  static final int READ_LATENCY = 15;
  static final int WRITE_LATENCY = 16;
  static final int MEMACCESSINST = 17;
  static final int LATENCY = 18;
  static final int SLICECNT = 19;
  static final int ADDRESSABLE_SIZE = 20;

  /*
    // these are unnecessary due to Number ! 
  static final Pattern FP = Pattern.compile("-?[0-9]*\\.[0-9]+");
  static final Pattern HEX = Pattern.compile("0x[A-Fa-f_0-9]+");
  static final Pattern NUM = Pattern.compile("\\d+");
  */

  //public void forSymbol(Symbol that) { }

  public void forVector(Vector that) { 
    int size = that.size();

    if (size > 0) {
      String s = getLabel(that);

      //System.out.println(" Vector "+that);
      //System.out.println(" Stack "+stack);
      
      if (s == null) return;
      
      // this feels dumb
      if (tokens[HARDWARE].equals(s)) { 
	stack.push(parseHardware(that)); }
      else if (tokens[CHIP].equals(s)) { 
	stack.push(parseChip(that)); }
      else if (tokens[COUNT].equals(s)) { parseCount(that); }
      else if (tokens[NAME].equals(s)) {  parseName(that); }
      else if (tokens[AREA_MAX].equals(s)) {  parseAreaMax(that); }
      else if (tokens[AREA_MIN].equals(s)) {  parseAreaMin(that); }
      else if (tokens[AREA].equals(s)) {  parseArea(that); }
      else if (tokens[RESOURCE].equals(s)) {  
	stack.push(parseResource(that)); }
      else if (tokens[ADDRESS].equals(s)) {  parseAddress(that); }
      else if (tokens[SIZE].equals(s)) {  parseSize(that); }
      else if (tokens[DIMENSIONS].equals(s)) {  parseDimensions(that); }
      else if (tokens[WIDTH].equals(s)) {  parseWidth(that); }
      else if (tokens[DEPTH].equals(s)) {  parseDepth(that); }
      else if (tokens[CLOCK].equals(s)) {  parseClock(that); }
      else if (tokens[PORT].equals(s)) {  
	stack.push(parsePort(that)); }
      else if (tokens[READ_LATENCY].equals(s)) {  parseReadLatency(that); }
      else if (tokens[WRITE_LATENCY].equals(s)) {  parseWriteLatency(that); }
      else if (tokens[MEMACCESSINST].equals(s)) {  
        stack.push(parseMemAccessInst(that)); }
      else if (tokens[LATENCY].equals(s)) {  parseLatency(that); }
      else if (tokens[SLICECNT].equals(s)) {  parseSliceCnt(that); }
      else if (tokens[ADDRESSABLE_SIZE].equals(s)) {  
	parseAddressableSize(that); }
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
    if (tokens[HARDWARE].equals(s)) { 
      return super.pop(v); }
    else if (tokens[CHIP].equals(s)) { 
      return super.pop(v); }
    else if (tokens[PORT].equals(s)) {
      return super.pop(v); }
    else if (tokens[RESOURCE].equals(s)) {
      return super.pop(v); }
    else if (tokens[MEMACCESSINST].equals(s)) {
      return super.pop(v); }
    else
      return null;
  }
    


  Object parseHardware(Vector v) {
    _hardware = new Hardware();
    // I think this is the name
    _hardware.name = v.elementAt(1).toString();
    _hardware.type = "none";
    _hardware.setTypeCode();
    return _hardware;
  }

  Object parseChip(Vector v) {
    Chip _chip = new Chip();
    _chip.type = v.elementAt(1).toString();
    _chip.setTypeCode();
    if((_chip.typeCode==Chip.RAM_TYPE)||
       (_chip.typeCode==Chip.ROM_TYPE)) {
      MemoryBlock _mChip = new MemoryBlock();

      _mChip.type = v.elementAt(1).toString();
      _mChip.setTypeCode();
      ((Hardware)stack.peek()).chip.add(_mChip);
      return _mChip;
    } else {
      ((Hardware)stack.peek()).chip.add(_chip);
      return _chip;
    }
  }

  void parseCount(Vector v) {
    Base _base = (Base)stack.peek();
    _base.count = ((Number)v.elementAt(1)).intValue();
  }

  void parseName(Vector v) {
    Base _base = (Base)stack.peek();
    _base.name = v.elementAt(1).toString();
  }

  void parseAreaMax(Vector v) {
    Chip _chip = (Chip)stack.peek();
    _chip.area_max = ((Number)v.elementAt(1)).floatValue();
  }

  void parseAreaMin(Vector v) {
    Chip _chip = (Chip)stack.peek();
    _chip.area_min = ((Number)v.elementAt(1)).floatValue();
  }

  void parseArea(Vector v) {
    if (stack.peek() instanceof Chip) {
      Chip _chip = (Chip)stack.peek();
      _chip.area = ((Number)v.elementAt(1)).intValue();
    }
    else if (stack.peek() instanceof MemAccessInst) {
      MemAccessInst _memAccessInst = (MemAccessInst)stack.peek();
      _memAccessInst.area = ((Number)v.elementAt(1)).floatValue();
    }
    else {
      System.err.println("Unknown area attribute on "+stack.peek());
      System.exit(-1);
    }
  }

 
  Object parseResource(Vector v) {
    Resource _res = new Resource();
    _res.type = v.elementAt(1).toString();
    ((Chip)stack.peek()).resource.add(_res);
    _res.setTypeCode();
    return _res;
  } 

  void parseAddress(Vector v) {
    // this may need to be put into the Base class
    int addr = ((Number)v.elementAt(1)).intValue();

    if (stack.peek() instanceof Resource) {
      Resource _res = (Resource)stack.peek();
      _res.address = addr;
    } else if (stack.peek() instanceof Chip) {
      Chip _chip = (Chip)stack.peek();
      _chip.address = addr;
    } else {
      System.err.println("Unknown address attribute on "+stack.peek());
      System.exit(-1);
    }
  }

  void parseSize(Vector v) {
    Base _base = (Base)stack.peek();
    _base.size = ((Number)v.elementAt(1)).intValue();
  }

  void parseDimensions(Vector v) {
    Resource _res = (Resource)stack.peek();
    _res.dimension1 = ((Number)v.elementAt(1)).intValue();
    _res.dimension2 = ((Number)v.elementAt(2)).intValue();
  }

  void parseWidth(Vector v) {
    Base _base = (Base)stack.peek();
    _base.width = ((Number)v.elementAt(1)).intValue();
  }

  void parseDepth(Vector v) {
    Base _base = (Base)stack.peek();
    _base.depth = ((Number)v.elementAt(1)).intValue();
  }

   void parseClock(Vector v) {
     Port _port = (Port)stack.peek();
    _port.clock = ((Number)v.elementAt(1)).intValue();
  }

  Object parsePort(Vector v) {
    Port _port = new Port();
    _port.type = v.elementAt(1).toString();
    _port.setTypeCode();
    ((Chip)stack.peek()).port.add(_port);

    return _port;
  }

  void parseReadLatency(Vector v) {
    Port _port = (Port)stack.peek();
    _port.read_latency = ((Number)v.elementAt(1)).intValue();
  }

  void parseWriteLatency(Vector v) {
    Port _port = (Port)stack.peek();
    _port.write_latency = ((Number)v.elementAt(1)).intValue();
  }
  
  Object parseMemAccessInst(Vector v) {
    MemAccessInst _memAccessInst = new MemAccessInst();
    _memAccessInst.type = v.elementAt(1).toString();
    _memAccessInst.setTypeCode();
    ((Chip)stack.peek()).memAccessInst.add(_memAccessInst);

    return _memAccessInst;
  }

  void parseLatency(Vector v) {
    MemAccessInst _memAccessInst = (MemAccessInst)stack.peek();
    _memAccessInst.latency = ((Number)v.elementAt(1)).intValue();
  }

  void parseSliceCnt(Vector v) {
    MemAccessInst _memAccessInst = (MemAccessInst)stack.peek();
    _memAccessInst.sliceCnt = ((Number)v.elementAt(1)).intValue();
  }


  void parseAddressableSize(Vector v) {
     Chip _chip = (Chip)stack.peek();
    _chip.addressable_size = ((Number)v.elementAt(1)).intValue();
  }


  Hardware getHardware() { return _hardware; }

  
}

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


package fp.synthesis;

import fp.circuit.*;
import fp.circuit.dot.DotCircuit;
import fp.circuit.vhdl.VHDLCircuit;
import fp.hardware.*;
import fp.hardware.AllocateArrays.ArrayInfo;
import fp.GlobalOptions;
import fp.hwdesc.Memory;

import java.util.*;
import java.util.Map.Entry;
import java.math.BigInteger;

/**
 * This class implements the memory interface of the system. It also contains 
 * data structures for handling the connections betw/ this interface and the 
 * datapath.
 */
public final class MemoryInterfaceGenerator extends InterfaceGenerator {

  /**
   * This data structure has the following hierarchy:
   *   HashMap memories(name, HashSet accesses([block, cycle, wirename]))
   */
  static protected HashMap _memoryAccesses;
  static protected HashMap _memoryInterface;

  static protected HashSet _inPorts;   // Saves which ports have been added.
  static protected HashSet _outPorts;
  static protected boolean _singlePorts;
  static protected HashSet _desWePorts;

  protected HashMap _mbMap;     // Stores the memory blocks for quick access.
  private MemInfoList _mem_info_list;

  // this need to be fixed, the first two are probably okay, but 
  // the last four should not
  // be final static, unless this is just for XD1 ...
  protected static final int LOAD  = 0;
  protected static final int STORE = 1;

  protected static long ADDRESS_OFFSET = 0;
  protected static int ADDR_WIDTH = 20;
  protected static int DATA_WIDTH = 72;

  // this can be static and protected, but it probablyu should not have
  // a default.
  protected static int N_MEMORIES = 4;
  // what if this is not the same ...
  protected static int MEM_READ_DELAY = 8;
  

  MemoryInterfaceGenerator(Circuit parent, MemInfoList mi) {
    _mem_info_list = mi;
    _circuit = parent;
  }

  /**
   * This method allocates data structures that will be used as memory 
   * accesses are recorded during datapath generation.
   */
  public void generate(ChipDef chipInfo) {
    // Allocate a new HashMap with size of the number of memory blocks.
    ArrayList mList = chipInfo.getMemoryBlockList();

    int _numberOfMemories = mList.size();
    N_MEMORIES = _numberOfMemories;

    _memoryAccesses = new HashMap(_numberOfMemories);
    _memoryInterface = new HashMap(_numberOfMemories);

    // Allocate the data structures.
    _mbMap = new HashMap(_numberOfMemories);
    _inPorts = new HashSet();
    _outPorts = new HashSet();
    _desWePorts = new HashSet();
    _singlePorts = true;
    
    // For each memoryBlock...
    // name the memories ...
    int mi = 0;
    for(ListIterator it = mList.listIterator(); it.hasNext(); mi++) {
      Memory mb = (Memory)it.next();
      
      // Update the data structure with the bus and its accesses.
      //_memoryAccesses.put(memName, new HashSet());
      String memName = "mem" + mi;
      mb.setName(memName);
      long addr_offset = ADDRESS_OFFSET + ((mb.getDepth()*mb.getWidth()) / 8  * mi);
      mb.setAddressOffset(addr_offset); 

      
      System.out.println("Memory Block "+mb);
      
      HashMap map = mb.getArrayInfos();
      for(Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
	Map.Entry me = (Map.Entry)iter.next();
	String array_name = (String)me.getKey();
	AllocateArrays.ArrayInfo ai = (AllocateArrays.ArrayInfo)me.getValue();

	System.out.println("Array "+array_name+" mem_offset "+ai.addr+" addr "+(Long.toHexString(addr_offset+ai.addr)));
	System.out.println(" width "+ai.type.getWidth()+" depth "+ai.totalWords);
	// need to put this in my mem.info file.
	MemInfo mem_info = new MemInfo(array_name, ai.type.getWidth(), ai.totalWords);
	
	// this is where we move to byte addressable ... ???
	//int addressable_size = mb.getAddressableSize();
	// addressable size is an external problem...
	int addressable_size = 8;
	mem_info.setAddr(addr_offset+(addressable_size*ai.addr));
	// this is correct.
	mem_info.setAddrSimple(ai.addr);
	_mem_info_list.add(mem_info);

      }


      _mbMap.put(memName, mb);

      _singlePorts &= ((mb.getNumOfReadBus()==1) && 
		       (mb.getNumOfWriteBus()==1));
    }



    if(_buildTop) {
      // Make an address expander for the top 2 bits of i_addr (once).
      // The top two are for mem_sel...
      Circuit parent = _circuit.getParent();
      // is there a better way to do this?
      int high = ADDR_BUS_WIDTH - 1 -1;  // -1 due to line up mem sel bits.
      int low = high+1-AddressDecodeGenerator.getDecoderWidth(_numberOfMemories);
      insertExpand(parent, "expand_addr"+parent.getUniqueName(), 
		   I_ADDR_BUS, low, high, "addr", ADDR_BUS_WIDTH);
      // Get the mem_en bit from i_status(1).
      insertExpand(parent, "expand_status"+parent.getUniqueName(), 
		   I_STAT_BUS, 1, 1, "mem_en", STAT_BUS_WIDTH);
      insertExpand(parent, "expand_status"+parent.getUniqueName(), 
		   I_STAT_BUS, 2, 2, "stat_mem_re", STAT_BUS_WIDTH);
    }
  }

  public static HashMap getMemoryInterface() {
    return _memoryInterface;
  }
  public HashSet getInPorts() {
    return _inPorts;
  }
  public HashSet getOutPorts() {
    return _outPorts;
  }


  public static boolean memNotUsed(String memName) {
    return !_memoryInterface.keySet().contains(memName);
  }


  /**
   * This method makes logic to get the data coming out of the dr ports.
   */
  static String makeDataReadLogic(Circuit parent, String name, String memRe) {
    String out = name+parent.getUniqueName();

    // Make the control logic (the memory read enable delayed by 8 cycles).
    // Add one more cycle for the XD1 host memory read latency.
    System.out.println("MEM_READ_DELAY "+MEM_READ_DELAY);
    int delay = MEM_READ_DELAY + 1;
    for(int i = 1; i <= delay; i++)
      parent.insertRegister("reg_"+memRe+"_"+i, 
			    (i == 1) ? memRe : memRe+"_"+(i-1), 
			    null, memRe+"_"+i, 1, 0);
    String sel = memRe+"_"+delay;

    // Make a guarding mux.
    String zeroOut = parent.getUniqueWireName();
    parent.insertConstant("c_0", 0, zeroOut, DATA_WIDTH);
    insertMux(parent, "mux_"+name, zeroOut, name+"_dr", sel, out, 
	      DATA_WIDTH);

    return (String) out;
  }


  /**
   * This method adds host access to the memories.  More specifically, it 
   * adds the following logic and related ports: address read, address write, 
   * data read, and data write.  The data read output wire name is returned.
   */
  static String makeMemoryAccesses(Circuit parent, String memName, 
				   int memNum) {
    HashMap map = new HashMap();

    // Make a memory decoder that looks at the top 2 bits of i_addr.
    String decOut = makeMemoryDecoder(parent, memName, memNum, N_MEMORIES);
    map.put(decOut, new PortTag(null, "in0", PortTag.IN, 1));

    // And the memi_re with the memory decoder.
    String memRe = memName+"_re";
    parent.insertOperator(Operation.AND, "and_"+memRe, "stat_mem_re", 
			  decOut, memRe, 1);

    // Get the mem_en bit from i_addr.
    /*
    int bitNum = ADDR_BUS_WIDTH-
      AddressDecodeGenerator.getDecoderWidth(N_MEMORIES)-1;
    String mem_en = "addr_"+bitNum;
    */
    String mem_en = "mem_en";
    map.put(mem_en, new PortTag(null, "in1", PortTag.IN, 1));

    // AND the done signal and mem_en with the decoder output.
    String andOut = "and_"+memName+"_en";
    map.put("done", new PortTag(null, "in2", PortTag.IN, 1));
    map.put(andOut, new PortTag(null, "out", PortTag.OUT, 1));
    parent.insertOperator(andOut, Operation.AND, map);
    
    String andOut_r = andOut+"_r";
    parent.insertRegister("enable_delay", andOut, (String)null, andOut_r, 1, 0);

    // Or the design's we with the host's we.
    String orOut = "or_"+memName+"_we";
    String desWe = "o_"+memName+"_aw_we";
    if(_desWePorts.contains(desWe))
      parent.insertOperator(Operation.OR, orOut, andOut_r, desWe, orOut, 1);
    else
      parent.insertOperator(Operation.OR, orOut, andOut_r, orOut, 1);

    // Address Write.
    String aw = memName+"_aw";
    addAccess(parent, aw, andOut, I_ADDR_BUS, ADDR_BUS_WIDTH, ADDR_WIDTH, 
	      _outPorts.contains(aw));

    // Data Write.
    String dw = memName+"_dw";
    addAccess(parent, dw, andOut, REG_IN, DATA_BUS_WIDTH, DATA_WIDTH, 
	      _outPorts.contains(dw));

    // Address Read.
    String ar = memName+"_ar";
    addAccess(parent, ar, memRe, I_ADDR_BUS, ADDR_BUS_WIDTH, ADDR_WIDTH, 
	      _outPorts.contains(ar));
    /*
    addAccess(parent, ar, andOut, I_ADDR_BUS, ADDR_BUS_WIDTH, ADDR_WIDTH, 
	      _outPorts.contains(ar));
    */

    // Data Read.
    String drMuxOut = makeDataReadLogic(parent, memName, memRe);

    return drMuxOut;
  }

  /** 
   * This method simply creates a guarding mux and OR operator for the inputs 
   * to the SRAM memories.
   */
  static void addAccess(Circuit parent, String name, String en, 
			String muxIn, int muxWidth, int widthOut, 
			boolean designInput) {
    // Make a mux with the output of the AND as the control.
    String muxOut = parent.getUniqueWireName();
    String zeroOut = parent.getUniqueWireName();
    parent.insertConstant("c_0", BigInteger.ZERO, zeroOut, muxWidth);
    insertMux(parent, "mux_"+name, zeroOut, muxIn, en,
	      muxOut, muxWidth);

    // Fix the width of the mux output.
    String sOut = null, zOut = null;
    if(muxWidth > widthOut) {
      int width = widthOut;
      sOut = parent.getUniqueWireName();

      // If this is an address rd/wt, then chop off the MSB and zero extend...
      // This is because the MSB is used for the mem_en signal.
      if((name.lastIndexOf("_ar") != -1) || (name.lastIndexOf("_aw") != -1)) {
	width = widthOut-1;
	zOut = parent.getUniqueWireName();
	insertZeroExtend(parent, "extend_"+name, sOut, zOut, width, widthOut);
      }
      insertSlice(parent, "slice_"+name, muxOut, 0, width-1, sOut, muxWidth,
		  width);
    } else if(muxWidth < widthOut) {
      sOut = parent.getUniqueWireName();
      insertZeroExtend(parent, "extend_"+name, muxOut, sOut, muxWidth, 
		       widthOut);
    }

    // OR the mux output with the design's uses of this port.
    String in = (sOut != null) ? ((zOut != null) ? zOut : sOut) : muxOut;

    if(designInput) {
      String desIn = "o_"+name;
      // Delay the address for the XD1 host memory read delay.
      parent.insertRegister("reg_"+name, in, null, in+"_r", widthOut, 0);
      parent.insertOperator(Operation.OR, "or_"+name, desIn, in+"_r", 
			    "or_"+name, widthOut);
      /*
      parent.insertOperator(Operation.OR, "or_"+name, desIn, in, 
			    "or_"+name, widthOut);
      */
    } else {
      String desIn = "o_"+name;
      parent.insertRegister("reg_"+name, in, null, in+"_r", widthOut, 0);
      // single input OR ??
      parent.insertOperator(Operation.OR, "or_"+name, in+"_r",
			    "or_"+name, widthOut);
    }
  }


  /**
   * This method maps memory accesses to corresponding memory ports.
   */
  void makeConnections() {
    // For each memory...
    for(Iterator mIt = _memoryAccesses.keySet().iterator(); mIt.hasNext(); ) {
      // For each access...
      String key = (String) mIt.next();
      List accesses = (List) _memoryAccesses.get(key);
      sort(accesses);  // Sort by blockname, then cycle

      int lIndex = 0, sIndex = 0;
      for(Iterator aIt = accesses.iterator(); aIt.hasNext(); ) {
	MemoryAccessInfo aInfo = (MemoryAccessInfo) aIt.next();	

	if(aInfo.accessType == LOAD)
	  lIndex = mapToPorts(key, aInfo, aInfo.accessType, lIndex);
	else
	  sIndex = mapToPorts(key, aInfo, aInfo.accessType, sIndex);
      }
    }

    // Connect the block wires to the ports out to memory.
    // For each memory...
    for(Iterator mIt = _memoryInterface.keySet().iterator(); mIt.hasNext(); ) {
      String memory = (String) mIt.next();

      // For each port...
      HashMap ports = (HashMap)_memoryInterface.get(memory);
      for(Iterator pIt = ports.keySet().iterator(); pIt.hasNext(); ) {
	String port = (String) pIt.next();
	if(_inPorts.contains(port))  // If this is a "dr" port, leave it alone.
	  continue;

	boolean noReadEn = false, noWrtEn = false;
	if(port.matches(".*_dw[0-9]*") || port.matches(".*_aw[0-9]*"))
	  noReadEn = true;
	if(port.matches(".*_dw[0-9]*") || port.matches(".*_ar[0-9]*"))
	  noWrtEn = true;
	  
	HashMap dOrMap = new HashMap();  // Allocate a map for the OR operator.
	HashMap enOrMap = new HashMap(); // For the we signals.
	HashMap weOrMap = new HashMap();

	// For each access designated to this port..
	int i = 0, widest = 0;
	HashSet accesses = (HashSet) ports.get(port);
	for(Iterator aIt = accesses.iterator(); aIt.hasNext(); ) {
	  MemoryAccessInfo info = (MemoryAccessInfo) aIt.next();

	  System.out.println("Using memory info "+info);
	  System.out.println("Using memory info datawire "+info.dataWire);

	  int width = 0;
	  if(port.matches(".*_dw[0-9]*")) {
	    width = info.dataWidth;
	    dOrMap.put(info.dataWire, new PortTag(null, "in"+i, PortTag.IN, 
						 width));
	    //enOrMap.put(info.dataWire+"_we", new PortTag(null, "in"+i, PortTag.IN, 1));
	  } else {
	    width = info.addrWidth;
	    dOrMap.put(info.addrWire, new PortTag(null, "in"+i, PortTag.IN, 
						  width));
	    if(!noReadEn)
	      enOrMap.put(info.addrWire+"_we", new PortTag(null, "in"+i, 
							   PortTag.IN, 1));
	    if(!noWrtEn) {
	      System.out.println("we wire : "+info.addrWire);
	      weOrMap.put(info.dataWire+"_we", new PortTag(null, "in"+i, 
							   PortTag.IN, 1));
	    }
	  }

	  if(width > widest) widest = width;
	  i++;
	}
	// OR all memory access wires that access this port.
	dOrMap.put(port, new PortTag(null, "out", PortTag.OUT, widest));
	_circuit.insertOperator("op_or"+_circuit.getUniqueName(), 
				Operation.OR, dOrMap);
	if(!noReadEn) {
	  enOrMap.put(port+"_re", new PortTag(null, "out", PortTag.OUT, 1));
	  _circuit.insertOperator("op_or"+_circuit.getUniqueName(), 
				  Operation.OR, enOrMap);
	}
	if(!noWrtEn) {
	  weOrMap.put(port+"_we", new PortTag(null, "out", PortTag.OUT, 1));
	  _circuit.insertOperator("op_or"+_circuit.getUniqueName(),
				  Operation.OR, weOrMap);
	}
      }
    }
  }

  /**
   * This method maps a memory access to a port of the specified memory. It 
   * returns the next possible port number.
   */
  int mapToPorts(String memory, MemoryAccessInfo info, int type, int index) {
    HashMap ports = (HashMap)_memoryInterface.get(memory);
    String addrType = (type == LOAD) ? "_ar" : "_aw";
    String dataType = (type == LOAD) ? "_dr" : "_dw";
    String dIndex = "";

    // Deal with the address port...
    // If the accessed memory has a single address read-write port.
    String arwName = memory+"_arw";
    if(ports.containsKey(arwName)) {
      HashSet port = (HashSet)ports.get(arwName);
      port.add(info);
    } else {  // Otherwise there are separate read and write address ports.
      String aName = (_singlePorts) ? memory+addrType : memory+addrType+index;
      if(ports.containsKey(aName)) {
	HashSet port = (HashSet) ports.get(aName);
	port.add(info);
      } else {
	index = 0;
	aName = memory+addrType+index;
	if(ports.containsKey(aName)) {
	  HashSet port = (HashSet) ports.get(aName);
	  port.add(info);
	}
      }
    }

    // Deal with the data port...
    dIndex = new Integer(index).toString();
    String dName = (_singlePorts) ? memory+dataType : memory+dataType+dIndex;
    if(ports.containsKey(dName)) {
      HashSet port = (HashSet) ports.get(dName);
      port.add(info);
    } else {
      dIndex = new Integer(0).toString();
      dName = memory+dataType+dIndex;
      if(ports.containsKey(dName)) {
	HashSet port = (HashSet) ports.get(dName);
	port.add(info);
      }
    }
    // If this is a load, then add a buffer for getting input from "_dr" port.
    if(type == LOAD)
      insertBuf(_circuit, "buf_"+info.dataWire, dName, info.dataWire, 
		info.dataWidth);
    return index+1;
  }

  /**
   * Port methods -- 
   *  These methods add ports based on its type (addr (out) and 
   *  data (in, out)). 
   */
  boolean addInPort(String name, HashMap ports, int width) {
    if(!_inPorts.contains(name)) {
      String portOut = name+"_s";
      if(GlobalOptions.buildTop)
	_circuit.insertInPort(name, "i_"+name, portOut, width);
      else
	_circuit.insertInPort("i_"+name, portOut, width);
      _inPorts.add(name);
      ports.put(name, new HashSet());
      return true;
    }
    return false;
  }
  boolean addOutPort(String name, HashMap ports, int width) {
    if(!_outPorts.contains(name)) {
      if(GlobalOptions.buildTop) {
	_circuit.insertOutPort(name, "o_"+name, "o_"+name, width);
	if(name.matches("mem[0-9]*_ar")) // if this is a read port...
	  _circuit.insertOutPort(name+"_re","o_"+name+"_re","o_"+name+"_re",1);
	else if(name.matches("mem[0-9]*_aw")) { //if this is a write port...
	  _circuit.insertOutPort(name+"_we","o_"+name+"_we","o_"+name+"_we",1);
	  _desWePorts.add("o_"+name+"_we");
	}
      } else {
	_circuit.insertOutPort(name, "o_"+name, width);	
	if(name.matches("mem[0-9]*_ar")) // if this is a read port...
	  _circuit.insertOutPort(name+"_re", "o_"+name+"_re",1);
	else if(name.matches("mem[0-9]*_aw")) {//if this is a write port...
	  _circuit.insertOutPort(name+"_we","o_"+name+"_we",1);
	  _desWePorts.add("o_"+name+"_we");
	}
      }
      _outPorts.add(name);
      ports.put(name, new HashSet());
      return true;
    }
    return false;
  }
  void addAddrPort(Memory mb, int type, HashMap ports, int width) {
    String name = mb.getName();
    if(mb.getonlyOneAddy())
      addOutPort(name+"_arw", ports, width);
    else {
      int nPorts = (type == LOAD) ? mb.getNumOfReadBus() : 
	                            mb.getNumOfWriteBus();
      for(int i = 0; i < nPorts; i++) {
	String t = (type == LOAD) ? "_ar" : "_aw";
	String portName = (nPorts==1) ? name+t : name+t+i;
	boolean result = addOutPort(portName, ports, width);
	if(result == true)
	  break;
      }
    }
  }
  void addDataPort(Memory mb, int type, HashMap ports, int width) {
    int nPorts = (type == LOAD) ? mb.getNumOfReadBus() : mb.getNumOfWriteBus();
    String name = mb.getName();
    boolean resultL = false, resultS = false;
    // Try to add ports until a port is actually added...
    for(int i = 0; i < nPorts; i++) {
      String pName = null;
      if(type==LOAD) {
	pName = (nPorts == 1) ? name+"_dr" : name+"_dr"+i;
	resultL = addInPort(pName, ports, DATA_WIDTH);
      } else {
	pName = (nPorts == 1) ? name+"_dw" : name+"_dw"+i;
	resultS = addOutPort(pName, ports, width);
      }
      if(resultL) {
	// If this is a Load, then get the right size of internal wires.
	int r0 = 0, r1 = r0 + width - 1;
	if(DATA_WIDTH > width)
	  insertSlice(_circuit, "slice_"+pName, pName+"_s", r0, r1, 
		      pName, DATA_WIDTH, width);
	else if(DATA_WIDTH == width)
	  insertBuf(_circuit, "buf_"+pName, pName+"_s", pName, width);
	else
	  throw new SynthesisException("DATA_WIDTH < width of circuitry");
	break;
      } else if(resultS)
	break;
    }
  }

  /**
   * This method updates the memory access data structure.
   */
  void updateMemoryAccesses(String memory, MemoryAccessInfo access) {
    List accesses = (List)_memoryAccesses.get(memory);

    // If this memory hasn't been added yet to the access hashmap, add it...
    if(accesses == null) {
      accesses = new LinkedList();
      _memoryAccesses.put(memory, accesses);
    }
    accesses.add(access);

    System.out.println("Memory: "+memory+", Array: "+access.getArrayName());
  }

  /**
   * This method adds a HashMap to the data structure if the memory hasn't 
   * been "seen" yet. The hashmap is returned. The hashmap will be used to 
   * store all of the ports associated with this memory. Then, each port 
   * entry will have a hashset containing all of the memory accesses that 
   * are mapped to this port.
   */
  HashMap updateMemoryInterface(String memory) {
    // Update the memory interface data structure...
    HashMap ports = (HashMap)_memoryInterface.get(memory);
    if(ports == null) {
      ports = new HashMap();
      _memoryInterface.put(memory, ports);
    }
    return ports;
  }

  /**
   * This method records a memory access of the specified memory. Also, it 
   * creates ports based on the type of memory access.
   */
  void addMemoryAccess(String memory, String block, String addrwire, 
		       String datawire, int addrwidth, int datawidth, 
		       int cycle, int type) {
    Memory mb = (Memory) _mbMap.get(memory);
    MemoryAccessInfo access = new MemoryAccessInfo(block, addrwire, datawire, 
						   addrwidth, datawidth, 
						   cycle, type);
    // Update the data structures.
    updateMemoryAccesses(memory, access);
    HashMap ports = updateMemoryInterface(memory);
    
    // Add ports if they do not already exist...
    addAddrPort(mb, type, ports, addrwidth);
    addDataPort(mb, type, ports, datawidth);
  }

  /**
   * This nested class records information about memory accesses. The 
   * datapath generator uses methods to create them whenever an ALoad or 
   * AStore is come across in the CFG.
   */
  private class MemoryAccessInfo {
    MemoryAccessInfo(String block, String addrwire, String datawire, 
		     int addrwidth, int datawidth, int cycle, int type) {
      blockName = block;
      addrWire = addrwire; 
      dataWire = datawire;
      addrWidth = addrwidth;
      dataWidth = datawidth;
      cycle = cycle;
      accessType = type;
    }
    public String blockName;
    public String addrWire;
    public int addrWidth;
    public String dataWire;
    public int dataWidth;
    public int cycle;
    public int accessType;
    public String toString() {
      return "[ "+blockName+", "+addrWire+", "+dataWire+" ]";
    }
    public String getArrayName() {
      return dataWire;
    }
  }

  /**
   * This method sorts lists of MemoryAccessInfos first by their blockNames 
   * and then in case of ties by cycles.
   */ 
  private void sort(List a) {
    class MemoryAccessInfoCompare implements Comparator {
      public int compare(Object o1, Object o2) {
	if((o1 instanceof MemoryAccessInfo) && 
	   (o2 instanceof MemoryAccessInfo)) {
 	  int comp = ((MemoryAccessInfo)o1).blockName.compareTo(((MemoryAccessInfo)o2).blockName);
	  if(comp == 0)
	    return ((MemoryAccessInfo)o1).cycle - ((MemoryAccessInfo)o2).cycle;
	  else
	    return comp;
	} else
	  throw new ClassCastException("Not a MemoryAccessInfo!");
      }
    }
    Collections.sort(a, new MemoryAccessInfoCompare());
  }

}

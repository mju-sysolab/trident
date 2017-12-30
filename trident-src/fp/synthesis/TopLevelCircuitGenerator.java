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
import fp.circuit.vhdl.LibObject;
import fp.circuit.vhdl.ParseModule;
import fp.circuit.vhdl.ParsePort;


import fp.flowgraph.BlockGraph;
import fp.GlobalOptions;
import fp.hwdesc.*;
import fp.util.FileTree;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class TopLevelCircuitGenerator extends GenericCircuitGenerator implements CircuitSwitch {

  public TopLevelCircuitGenerator(String target, String arch) {
    super(target, "xd1_top", null);
    
    // we have a Circuit object _circuit
    // this should not exactly be hardcoded this way.

    Platform platform = GlobalOptions.platform;
    // despite this being loaded this way, it should probably come as 
    // part of the constructor and the whole class should be chosen
    // dynamically ... 
    ParseModule pm = new ParseModule(platform.getInterfaceFileName());

    fp.circuit.vhdl.Library lib = pm.getLib();
    LibObject lo = lib.getLibObject("xd1_top");
    HashMap ports = lo.getPorts();

    for (Iterator iter = ports.values().iterator(); iter.hasNext(); ) {
      ParsePort p = (ParsePort)iter.next();
      int width = p.getWidth();
      String name = p.getName();
      int direction = p.getDirection();
      if (direction == PortTag.IN) {
	_circuit.insertInPort(name, name, width);
	//System.out.println(" Insert in port "+name+" ("+width+")");
      } else if (direction == PortTag.OUT) {
	_circuit.insertOutPort(name, name, width);
	//System.out.println(" Insert out port "+name+" ("+width+")");
      }
    }


  }
  
  public void generate(BlockGraph graph) {
    
    Hardware hw = GlobalOptions.hardware;

    long reg_offset = 0L;
    long mem_offset = 0L;
    int memory_count = 0;
    int mem_width = 0;
    int mem_addr_width = 0;
    int mem_read_delay = -1;

    // this code assumes one fpga and one block of memory ... it should be more flexible.
    // I should really put things into hash maps that can be recalled by name like
    // {'mem0' : addr 0x0000, 'mem1': addr 0x40000 ...
    for(Iterator iter = hw.chip.listIterator(); iter.hasNext(); ) {
      Chip chip = (Chip)iter.next();
      if (chip.typeCode == Chip.FPGA_TYPE) {
	for(Iterator r_iter = chip.resource.listIterator(); r_iter.hasNext(); ) {
	  Resource res = (Resource)r_iter.next();
	  if (res.typeCode == Resource.REGISTER_TYPE) {
	    reg_offset = (long)res.address;
	  }
	}
      }
      
      if (chip.typeCode == Chip.RAM_TYPE) {
	mem_offset = (long)chip.address;
	memory_count = memory_count + chip.count;
	mem_width = chip.width;

	for(Iterator p_iter = chip.port.iterator(); p_iter.hasNext(); ) {
	  fp.hwdesc.Port p = (fp.hwdesc.Port)p_iter.next();
	  if (p.typeCode == fp.hwdesc.Port.ADDRESS_READ_TYPE) {
	    mem_addr_width = p.width;
	  }
	  if (p.typeCode == fp.hwdesc.Port.DATA_READ_TYPE) {
	    mem_read_delay = p.read_latency;
	  }
	  
	}


      }
    }

    // is this the way to communicate this info ??
    MemoryInterfaceGenerator.ADDRESS_OFFSET = mem_offset;
    MemoryInterfaceGenerator.N_MEMORIES = memory_count;
    MemoryInterfaceGenerator.ADDR_WIDTH = mem_addr_width;
    MemoryInterfaceGenerator.DATA_WIDTH = mem_width;
    MemoryInterfaceGenerator.MEM_READ_DELAY = mem_read_delay;

    /*
    System.out.println("ADDRESS_OFFSET "+mem_offset);
    System.out.println("N_MEMORIES "+memory_count);
    System.out.println("ADDR_WIDTH "+mem_addr_width);
    System.out.println("DATA_WIDTH "+mem_width);
    System.out.println("MEM_READ_DELAY "+mem_read_delay);
    */

    // what about write?

    
    RegInfoList reg_list = new RegInfoList(reg_offset,1,8);
    MemInfoList mem_list = new MemInfoList();

    
    _circuit.insertOperator(Operation.NOT, "inv_reset_n", "reset_n",
			    "reset", 1);

    // here we join the normal path.
    DesignCircuitGenerator design =  
      new DesignCircuitGenerator(_target, "design_run", _circuit, reg_list, mem_list);
    design.generate(graph);

    // Generate register bus connections/logic.
    // should need this now.
    //ArrayList extRegs = design.getExtRegInfos();
    
    // will need to add start to the end ...
    System.out.println("externals:  "+reg_list);

    
    RegisterInterfaceGenerator regBus = 
      new RegisterInterfaceGenerator(_circuit, reg_list);
    regBus.generate();


    // Make the logic for host memory accesses.
    // the data_bus_width should be extracted from the Hardware var.
    
    String memOut = makeHostMemoryAccesses(design);

    // Make the memory ports. -- don't need to do this, but
    // will will need to handle un-used memory ports.
    //makeMemoryPorts(design);
    connectMemoryPorts();
    
    // this is a kludge --
    fixUnusedMemoryPorts();

    // Give all the wires their widths, and check wire connectivity.
    resolveWidths();

    // Build the circuit in the underlying representation.
    _circuit.build(graph.getName(), _objs);

    // arrays will need to be added to this.
    new MemoryInfoFile("mem.info",reg_list, mem_list);
  }


  /* This is TEMPORARY -- it will need to be written better, when I 
   * have time.  There is some support for describing what should be
   * done with port that is unused -- however, nothing currently passes
   * that info along to the right spots so it easily recognizable, what
   * should be done.
   */

  void fixUnusedMemoryPorts() {
    HashSet ports = _circuit.getPorts();
    for (Iterator iter = ports.iterator(); iter.hasNext();) {
      fp.circuit.Port p = (fp.circuit.Port)iter.next();

      String name = p.getName();
      //System.out.print(" Port : "+name);

      if (p.getDirection() == PortTag.OUT) {
	// only check out ports ...
	//System.out.print(" out ");
	if (name.indexOf("mem") > 0) {
	  //System.out.print(" mem ");	  
	  boolean neg = (name.indexOf("_n") > 0);
	  PortTag pt = p.findInPort("in");
	  Net net = pt.getNet();
	  if (net.getSources().size() == 0) {
	    if (neg) {
	      _circuit.insertConstant("tidy", 0xFFFFFFFF, net.getName(), 
				      pt.getWidth());
	    } else {
	      _circuit.insertConstant("tidy", 0, net.getName(), pt.getWidth());
	    }
	  }
	}
      }
      //System.out.println("");
    }
  }


  /**
   * This method simply creates ports based on whatever was created in the 
   * design circuit.
   */
  void makeMemoryPorts(DesignCircuitGenerator design) {
    int width = 0;
    HashSet inPorts = design.getBusInPorts();
    HashSet outPorts = design.getBusOutPorts();

    for(int i = 0; i < MemoryInterfaceGenerator.N_MEMORIES; i++) {
      String memName = "mem"+i;
      if(MemoryInterfaceGenerator.memNotUsed(memName))
	continue;  // Don't add ports for memories that aren't used...
      outPorts.add(memName+"_dw");
      outPorts.add(memName+"_aw");
      outPorts.add(memName+"_ar");
      inPorts.add(memName+"_dr");
    }

    // For each of the memory ports, create a top-level inport.
    for(Iterator it = inPorts.iterator(); it.hasNext(); ) {
      String port = (String) it.next();
      width = MemoryInterfaceGenerator.DATA_WIDTH;
      /*
      _circuit.insertInPort(port, "i_"+port, width);
      _circuit.insertRegister("reg_"+port, "i_"+port, null, port, width, 0);
      */
      _circuit.insertInPort(port, port, width);
    }
    for(Iterator it = outPorts.iterator(); it.hasNext(); ) {
      String port = (String) it.next();
      if((port.indexOf("ar") != -1) || (port.indexOf("aw") != -1) || 
	 (port.indexOf("arw") != -1))
	width = MemoryInterfaceGenerator.ADDR_WIDTH;
      else
	width = MemoryInterfaceGenerator.DATA_WIDTH;
      /*
      if(port.indexOf("ar") != -1) {  
	// Delay the ar signal for the XD1 host read delay of 10 cycles.
	_circuit.insertRegister("reg_"+port, "or_"+port, null, port+"_r", width, 0);
	_circuit.insertOutPort(port+"_r", port, width);
      } else 
      */
	_circuit.insertOutPort("or_"+port, port, width);
    }
  }

  void connectMemoryPorts() {
    // this should be hash of strings or something ...
    HashSet set = new HashSet();
    set.add("_dw");
    set.add("_aw");
    set.add("_ar");
    for(int i = 0; i < MemoryInterfaceGenerator.N_MEMORIES; i++) {
      String memName = "mem"+i;
      if(MemoryInterfaceGenerator.memNotUsed(memName))
	continue; 
      for (Iterator iter = set.iterator(); iter.hasNext(); ) {
	String p_type = (String)iter.next();
	String port = memName + p_type;
	int width = 0;
	if (p_type.equals("_dw")) {
	  width = MemoryInterfaceGenerator.DATA_WIDTH;
	} else {
	  width = MemoryInterfaceGenerator.ADDR_WIDTH;
	}
	_circuit.insertOperator(Operation.BUF, "or_"+port, "or_"+port,
				port, width);
      }
    }
  }
      


  /**
   * Make host access to memories; create logic for the following 
   * ports: address read, address write, data read, and data write.
   */
  String makeHostMemoryAccesses(DesignCircuitGenerator design) {
    HashMap map = new HashMap();
    HashSet inPorts = design.getBusInPorts();
    HashSet outPorts = design.getBusOutPorts();

    for(int i = 0; i < MemoryInterfaceGenerator.N_MEMORIES; i++) {
      String memName = "mem"+i;

      if(MemoryInterfaceGenerator.memNotUsed(memName))
	continue;  // Don't add access to memories that aren't used...

      System.out.println("adding host access to: "+memName);

      // add write enable.
      String memEn = memName+"_we", memEnN = memEn+"_n";
      String notIn = "or_"+memEn;
      _circuit.insertOperator(Operation.NOT, "inv_"+memEn, notIn, memName
			      +"_we_n", 1);       
      //String memEn = memName+"_en", memEnN = memEn+"_n";
      //_circuit.insertOperator(Operation.NOT, "inv_"+memEn, "and_"+memEn, memEnN, 1); 

      // add read enable
      String memRe = "o_"+memName+"_ar_re", memReN = memRe+"_n";
      String hostMemRe = memName+"_re";
      // Delay the re signal for the XD1 host read delay.
      _circuit.insertRegister("reg_"+memReN, hostMemRe, null, hostMemRe+"_r", 
			      1, 0);
      String orOut = "or_"+hostMemRe;
      if(!outPorts.contains(memName+"_ar"))
	orOut = hostMemRe+"_r";
      else
	_circuit.insertOperator(Operation.OR, "or_"+hostMemRe, hostMemRe+"_r", 
				memRe, orOut, 1);
      /*
      String orOut = "or_"+hostMemRe;
      _circuit.insertOperator(Operation.OR, "or_"+hostMemRe, hostMemRe, 
			      memRe, orOut, 1);
      */
      _circuit.insertOperator(Operation.NOT, "inv_"+memRe, orOut, 
			      memName+"_re_n", 1);
      
      // what is this ?  A special mux ....
      String muxOut = MemoryInterfaceGenerator.makeMemoryAccesses(_circuit, 
								  memName, i);
      map.put(muxOut, new PortTag(null, "in"+i, PortTag.IN, 
				  MemoryInterfaceGenerator.DATA_WIDTH));
    }

    // OR all of the guarded memories' outputs.
    String orOut = "or_mem";
    map.put(orOut, new PortTag(null, "out", PortTag.OUT, 
			       MemoryInterfaceGenerator.DATA_WIDTH));

    // jlt -- This is temporary as we need a fix so that circuits
    // can be compiled if they don't use any memory.
    //
    if (map.size() > 1) {
      _circuit.insertOperator("or_"+orOut, Operation.OR, map);
    } else {
      // this just gives us a value so that we don't error out.
      _circuit.insertConstant("or_"+orOut, 0, orOut, 
			      MemoryInterfaceGenerator.DATA_WIDTH);
    }

    String sliceOut = "slice_mem";
    if (MemoryInterfaceGenerator.DATA_BUS_WIDTH == 
	MemoryInterfaceGenerator.DATA_WIDTH) {
         _circuit.insertOperator(Operation.BUF, "slice_o_mem", orOut, 
				 sliceOut, 	
				 MemoryInterfaceGenerator.DATA_WIDTH);
    } else {
      insertSlice(_circuit, "slice_o_mem", orOut, 0, 
		  MemoryInterfaceGenerator.DATA_BUS_WIDTH-1, sliceOut, 
		  MemoryInterfaceGenerator.DATA_WIDTH, 
		  MemoryInterfaceGenerator.DATA_BUS_WIDTH);
    }
    // Add register to make timing for 10 cycles for host memory read.
    String out = "o_mem";
    _circuit.insertRegister("reg_o_mem", "slice_mem", null, "o_mem", 
			    MemoryInterfaceGenerator.DATA_BUS_WIDTH, 0);
    return out;
  }

  
}

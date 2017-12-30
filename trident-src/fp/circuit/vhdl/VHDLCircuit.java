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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import java.math.BigInteger;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;

import fp.GlobalOptions;
import fp.util.FileTree;

import fp.circuit.*;
import fp.circuit.dot.DotCircuit;

import fp.hwdesc.Read;
import fp.hwdesc.Write;
import fp.hwdesc.Wait;
import fp.hwdesc.FabIn;
import fp.hwdesc.Run;
import fp.hwdesc.Command;
import fp.hwdesc.ParseTestbench;

// these are spelled out to avoid name clash with
// circuit.Operator and generator.Operator.
import fp.util.vhdl.generator.Architecture;
import fp.util.vhdl.generator.AssertionStatement;
import fp.util.vhdl.generator.Char;
import fp.util.vhdl.generator.Component;
import fp.util.vhdl.generator.Concat;
import fp.util.vhdl.generator.ConditionalSignalAssignment;
import fp.util.vhdl.generator.ConstantItem;
import fp.util.vhdl.generator.Entity;
import fp.util.vhdl.generator.Expression;
import fp.util.vhdl.generator.Eq;
import fp.util.vhdl.generator.FunctionCall;
import fp.util.vhdl.generator.DesignFile;
import fp.util.vhdl.generator.DesignUnit;
import fp.util.vhdl.generator.Div;
import fp.util.vhdl.generator.Instance;
import fp.util.vhdl.generator.LibraryUnit;
import fp.util.vhdl.generator.Literal;
import fp.util.vhdl.generator.Mult;
import fp.util.vhdl.generator.Not;
import fp.util.vhdl.generator.NumericLiteral;
import fp.util.vhdl.generator.ProcessStatement;
import fp.util.vhdl.generator.Signal;
import fp.util.vhdl.generator.SignalAssignment;
import fp.util.vhdl.generator.SimpleName;
import fp.util.vhdl.generator.StringLiteral;
import fp.util.vhdl.generator.SubType;
import fp.util.vhdl.generator.Use;
import fp.util.vhdl.generator.Waveform;
import fp.util.vhdl.generator.WaitStatement;


public class VHDLCircuit extends Circuit {

  private Object _tech_object;

  //'just one ??
  private static DesignFile _design_file;
  //private static VHDLInit _init;
  private static HashMap _vhdl_libs;

  private DesignUnit _design_unit;

  private SimpleName _name;

  private HashMap inputs;
  private HashMap outputs;
  private HashMap widths;

  boolean built = false;

  public VHDLCircuit(Circuit parent, Object tech_object, String name) {
    super(parent, name);

    inputs = new HashMap();
    inputs.put(".reset", new SimpleName("w_reset"));
    inputs.put(".start", new SimpleName("w_start"));
    outputs = new HashMap();
    outputs.put(".done", new SimpleName("w_o_done"));
    widths = new HashMap();

    _tech_object = tech_object;

    if (parent == null) {
      // some init.
      HashSet lib_names = new HashSet();
      _vhdl_libs = new HashMap();
      
      fp.hwdesc.Library lib_conf = GlobalOptions.library;

      lib_names.add(lib_conf.getFileName());
      for (Iterator iter=lib_conf.getRequiredFiles().iterator(); 
	   iter.hasNext(); ) {
	String required_file = (String)iter.next();
	// I don't know what will happen for name collisions here, but
	// it looks like fun!!
	lib_names.add(required_file);
      }
      
      for(Iterator iter=lib_names.iterator(); iter.hasNext(); ) {
	String file_name = (String)iter.next();
	_vhdl_libs.put(file_name, new ParseModule(file_name));

      }
    }

  }

  public SimpleName getVHDLName() {
    if (_name == null) {
      _name = new SimpleName(getRefName());
    }
    return _name;
  }

  DesignUnit getVHDLParent() { return _design_unit; }

  DesignFile getDesignFile() { return _design_file; }


  public boolean setWidth() {
    // What is this for???
    return true;
  }

  /*
    
  So the 40,000 dollar question of the day is, do I merge the modules
  here or do I wait?  I think I merge them all together.

  */

  HashMap getModules() { 
    HashMap result = new HashMap();
    for(Iterator iter=_vhdl_libs.values().iterator(); iter.hasNext(); ) {
      //VHDLInit init = (VHDLInit)iter.next();
      ParseModule init = (ParseModule)iter.next();

      HashMap map = init.getModules();
      // sanity check
      for(Iterator map_iter=map.keySet().iterator(); map_iter.hasNext(); ) {
	String name = (String)map_iter.next();
	if (result.containsKey(name)) {
	  System.out.println("Collision in the mapping of VHDL modules");
	  System.out.println(" colliding name "+name);
	  System.out.println(" I will still merge, but you may not get what "
			     + "is desired.");
	  
	}
      }
      result.putAll(map);
    }    

    return result;
  }

  protected Circuit newCircuit(Circuit graph, String name) {
    return new VHDLCircuit(graph, _tech_object, name);
  }

  protected Constant newConstant(Circuit graph, String name,
				 String value, int width, int type) {
    return new VHDLConstant(graph, name, value, width, type);
  }

  protected FSM newFSM(Circuit graph, String name, StateMachine transitions) {
    return new VHDLFSM(graph, name, transitions);
  }

  protected Memory newMemory(Circuit graph, String name, 
			     int width, int a_width, 
			     int[] contents) {
    return new VHDLMemory(graph, name, width, a_width, contents);
  }

  protected Net newNet(Circuit graph, String name) {
    return new VHDLNet(graph, name);
  }
  
  protected Node newNode(Circuit graph, String name, HashMap ports, 
			 String object, Object[] objects) {
    return new VHDLNode(graph, name, ports, object, objects);
  }
  
  protected Operator newOperator(Circuit graph, String name, 
				 Operation type) {
    return new VHDLOperator(graph, name, type);
  }

  protected Port newPort(Circuit graph, String name, int width, 
			 int direction) {
    return new VHDLPort(graph, name, width, direction);
  }

  protected Register newRegister(Circuit graph, String name, int width, 
				 String contents) {
    return new VHDLRegister(graph, name, width, contents);
  }


  void buildInstance(Circuit c) {
    DesignUnit du = ((VHDLCircuit)c.getParent()).getVHDLParent();
    LibraryUnit lu = du.getLibraryUnit();
      //Entity e = lu.getEntity();

    Architecture a = lu.getArchitecture();
    
    // get name ?? -- this is supposed to be a "class" name 
    Component comp = addComponent(a, c);
    
    Instance inst = new Instance(new SimpleName(c.getName()), comp);
    for (Iterator iter=c.getPorts().iterator(); iter.hasNext();) {
      Port p = (Port)iter.next();
      
      // maps "port" -> "signal"
      HashSet infos = null;
      if (p.getDirection() == PortTag.IN) {
	infos = p.getInPorts();
      } else if (p.getDirection() == PortTag.OUT) {
	infos = p.getOutPorts();
      } else {
	System.out.println(" VHDLCircuit: buildInstance(): This is complicated  -- fix me.");
	System.exit(-1);
      }
      
      // This is tricky VHDL does not allow multiple wires with different names.  They
      // must be explicity the same name or merged at some point.
      Iterator info_iter = infos.iterator(); 
      // okay we will test ...
      if (info_iter.hasNext()) {
	PortTag first = (PortTag)info_iter.next();

	inst.addPortMap(new SimpleName(((VHDLPort)p).getIdentifier()),
			((VHDLNet)first.getNet()).getVHDLName());
      }
    }
    a.addStatement(inst);
    
  }

  
  void addLibraries(DesignUnit du) {
    // this should really check to see or somehow
    // libs need to be added for modules.
    du.addLibrary("ieee");
    du.addUse(new Use("ieee.std_logic_1164.all"));
    du.addUse(new Use("ieee.std_logic_unsigned.all"));
    du.addUse(new Use("ieee.std_logic_arith.all"));
  }


  Component addComponent(Architecture a, Circuit c) {
    Component comp = new Component(((VHDLCircuit)c).getVHDLName());
    for (Iterator iter=c.getPorts().iterator(); iter.hasNext();) {
      Port p = (Port)iter.next();
      comp.addPort(((VHDLPort)p).getVHDLPort());
    }
    a.addItem(comp);
    return comp;
  }


  public void build(String name, Object[] objs) {
    // if top insert top stuff and libraries
    // add a design thinger -- the design unit <--> Level of heirachy.
    //   -- for the design unit add ports to the entity
    //   -- add the architecture
    //       -- add signals
    //       -- add everything else (in progress.)
    
    //System.out.println("TODO build() " + getRefName());
    // crazy
    if (built) return;
    
    if (getParent() == null) {
      _design_file = new DesignFile();
      _design_unit = new DesignUnit(getName());

      DesignUnit du = _design_unit;
      _design_file.addDesignUnit(du);

      addLibraries(du);

    } else {
      /*
	If I am not the top, I think I have two jobs.  -- No I do not
	think that is right.  The circuit that wants the instatiations
	should build them itself.  A circuit is responsible for the
	leaves and appearence those things in it.
      */

      buildInstance(this);

      _design_unit = new DesignUnit(getRefName());
      
      DesignUnit du = _design_unit;
      // how do I get in the file ???
      getDesignFile().addDesignUnit(du);

      addLibraries(du);
      
    }   

    // signals ??
    //System.out.println("Build Nets");
    no_net:
    for(Iterator iter = getNets().iterator(); iter.hasNext(); ) {
      VHDLNet net = (VHDLNet)iter.next();
      // need to see if net exists in portmap

      /*
	Just checking sources is not sufficent -- this can happen
	to Port->Sink Source->Port and also to FSM and probably
	black boxes (which are nodes -- upgraded to Circuits (maybe)).

	OKay -- maybe I should change the port hashset to a hashmap
	so I can quickly find the names....  This should work for everything
	now, but it could be costly.

      */

      for(Iterator port_iter = getPorts().iterator(); port_iter.hasNext(); ) {
	Port port = (Port)port_iter.next();
	
	if (port.getRefName().equals(net.getName())) {
	  // skip building this net ??
	  //System.out.println(" Not adding signal "+net);
	  continue no_net;
	}
      }
      net.build(name, objs);
    }

    //System.out.println("Build Nodes");
    // ports -- but does the order matter ... no, not for concurrent statements.
    for(Iterator iter = getNodes().iterator(); iter.hasNext(); ) {
      Node node = (Node)iter.next();
      node.build(name,objs);
    }

    //System.out.println("Build SubCircuits");
    for(Iterator iter = getCircuits().iterator(); iter.hasNext(); ) {
      Circuit c = (Circuit)iter.next();

      c.build(name,objs);
    }


    if (getParent() == null) {
      // we need to get just the file basename and path from it
      int index = name.lastIndexOf(System.getProperty("file.separator"));
      int last_dot = name.lastIndexOf(".");
      String basename = name.substring(index+1,last_dot);
      String path = name.substring(0, index+1);

      // now create the changed file name
      String file_name = path + "" + basename + ".vhd";

      DesignFile.write(_design_file, file_name);

      // we should hack in a test bench generator to ease
      // making testbenches for our stuff.
      
      if (GlobalOptions.makeTestBench)
	makeTestBench(name, this);
    }

    built = true;
  }



  VHDLNet findNet(String name) {
    return (VHDLNet)getNetHash().getNet(name);
  }

  Architecture makeTestBenchTop(DesignUnit du, Circuit c) {
    addLibraries(du);

    // need some more libraries!
    du.addLibrary("std_developerskit"); // this won't work in general because the library is not available everywhere. :P
    du.addUse(new Use("std_developerskit.std_iopak.all"));

    LibraryUnit lu = du.getLibraryUnit();
    //Entity is empty ...
    //Entity e = lu.getEntity();
    
    Architecture a = lu.getArchitecture();

    // build component
    Component comp = addComponent(a, c);

    // make instance (and build signals)
    Instance inst = new Instance(new SimpleName("dut"), comp);
    for (Iterator iter=c.getPorts().iterator(); iter.hasNext();) {
      Port p = (Port)iter.next();

      String ref_name = p.getRefName();
      int width = p.getWidth();

      // maps "port" -> "signal"
      HashSet infos = null;
      if (p.getDirection() == PortTag.IN) {
	infos = p.getInPorts();
      } else if (p.getDirection() == PortTag.OUT) {
	infos = p.getOutPorts();
      } else {
	System.out.println(" VHDLCircuit: makeTestBench(): This is complicated  -- fix me.");
	System.exit(-1);
      }


      // build test signals
      SimpleName vhdl_port = new SimpleName(((VHDLPort)p).getIdentifier());
      SimpleName vhdl_net = new SimpleName("w_"+ref_name);

      // kind of a kludge
      if (ref_name.equals("clk")) {

	a.addItem(new Signal(vhdl_net, SubType.STD_LOGIC, 
			     new Expression(Char.ZERO) ));
      } 
      else {



	if (width == 1) { 
	    a.addItem(new Signal(vhdl_net, SubType.STD_LOGIC));
	} else {
	  a.addItem(new Signal(vhdl_net, 
			       SubType.STD_LOGIC_VECTOR(width - 1, 0)));
	}
	addVarToMap(p,ref_name,vhdl_net,width);
      }
      
      inst.addPortMap(vhdl_port, vhdl_net);
    }

    SimpleName period = new SimpleName("PERIOD");
    a.addItem(new ConstantItem(period, SubType.TIME, 
	    new Expression(new NumericLiteral(10, new SimpleName("ns")))));
    a.addStatement(inst);

    return a;
  }

  public void addVarToMap(Port p, String ref_name, SimpleName vhdl_net, int width) {
    
    String short_name="";
    if(ref_name.startsWith("i_") || ref_name.startsWith("o_"))
      short_name = ref_name.substring(2);
    else
      short_name="."+ref_name;
    
    widths.put(short_name, new Integer(width));
    if (p.getDirection() == PortTag.IN) {
      if(!inputs.containsKey(short_name))
	inputs.put(short_name, vhdl_net);
    }
    else
      if(!outputs.containsKey(short_name))
	outputs.put(short_name, vhdl_net);
  }

  
//   public void addVarToMap(Port p, String ref_name, String short_name) {
// 	if (p.getDirection() == PortTag.IN) {
// 	  //if it is an input port
// 	  if(ref_name.startsWith("i_")) {
	  
// 	    //update the array if one exists
// 	    if(inputs.containsKey(short_name))
// 	      if(ref_name.endsWith("_we"))
// 		inputs.get(short_name)[1]=vhdl_net;
// 	      else
// 		inputs.get(short_name)[0]=vhdl_net;

// 	    // else create a new key,value pair
// 	    else {
// 	      String s[2];
// 	      if(ref_name.endsWith("_we"))
// 		s[1]=vhdl_net;
// 	      else
// 		s[0]=vhdl_net;
// 	      inputs.put(short_name,s);  
// 	    }	
// 	  }
// 	  //it must be a special named input port eg 'reset'
// 	  else
// 	    if(inputs.containsKey(short_name))
// 	      break;
// 	    else
// 	      inputs.put(short_name, vhdl_net);
// 	}
// 	//it must be an output port
// 	else
// 	  if(outputs.containsKey(short_name))
// 	    break;
// 	  else
// 	    outputs.put(short_name, vhdl_net);
//   }


  // this does not handle illegal top level port names
  void makeTestBench(String name, Circuit c) {

    // we need to get just the file basename and path from it
    int index = name.lastIndexOf(System.getProperty("file.separator"));
    int last_dot = name.lastIndexOf(".");
    String basename = name.substring(index+1,last_dot);
    String path = name.substring(0, index+1);
    
    // now create the changed file name
    String file_name = path + "tb_" + basename + ".vhd";
    // prevent tb blotto ...
    index = 0;
    while (new File(file_name).exists()) {
      file_name = path + "tb_" + basename + "_"+ index + ".vhd";
      index++;
    }
    
    FabIn fab=null;
    ParseTestbench ptb;
    if(GlobalOptions.testBenchFile != null) {
      ptb = new ParseTestbench(GlobalOptions.testBenchFile);
      fab = ptb.getFabIn();
    }
      
    DesignFile design_file = new DesignFile();
    DesignUnit design_unit = new DesignUnit("tb_"+getName());
    
    DesignUnit du = design_unit;
    design_file.addDesignUnit(du);

    Architecture a = makeTestBenchTop(du, c);
 
    // duplicated from above
    SimpleName period = new SimpleName("PERIOD");
    /*
    a.addItem(new ConstantItem(period, SubType.TIME, 
	    new Expression(new NumericLiteral(10, new SimpleName("ns")))));
    a.addStatement(inst);
    */
    // add clk
    SimpleName w_clk = new SimpleName("w_clk");
    ConditionalSignalAssignment csa = 
      new ConditionalSignalAssignment(w_clk);
    a.addStatement(csa);
    csa.addCondition(new Waveform(new Not(w_clk), 
				  new Div(period, new NumericLiteral(2))), 
		     null);

    // add process
    ProcessStatement p = new ProcessStatement(new SimpleName("STIMULI"));
    // add it early, does not matter much.
    a.addStatement(p);

    //Initialize everything to zero.
    for(Iterator i=inputs.keySet().iterator(); i.hasNext();){
      String port_name=(String)i.next();
      p.addStatement(new SignalAssignment((SimpleName)inputs.get(port_name), 
					  new Waveform(VHDLConstant.genConstant(BigInteger.ZERO,
										((Integer)widths.get(port_name)).intValue()))));
    }

    a.addFooterComment("\b- @ asim -lib work tb_"+getName());   

    //if no command list is present, construct a default list
    if(fab == null) {
      addDummyCommands(p);
      a.addFooterComment("\b- @ run 8000ns");
    }
    //else construct the testbench
    else {
      addCommands(p, fab, file_name);
      if (fab.run != null) 
	a.addFooterComment("\b- @ run "+fab.run.time+fab.run.unit);
      else 
	// sometimes there is no run command ...
	a.addFooterComment("\b- @ run 8000ns");
    }

    a.addFooterComment("\b- @ exit");

    DesignFile.write(design_file, file_name);
    
  }

  void addDummyCommands(ProcessStatement p) {
    SimpleName period = new SimpleName("PERIOD");
    SimpleName w_reset = new SimpleName("w_reset");
    SimpleName w_start = new SimpleName("w_start");
      
    p.addStatement(new SignalAssignment(w_reset, new Waveform(Char.ZERO)));
    p.addStatement(new SignalAssignment(w_start, new Waveform(Char.ZERO)));
      
    p.addStatement(new WaitStatement((Expression)null, 
				     new Expression(period)));

    // the end
    p.addStatement(new WaitStatement());
  }

  void addCommands(ProcessStatement p, FabIn fab, String name) {

    Command prev_c = null;
    for(ListIterator i=fab.commands.listIterator(); i.hasNext();){
      Command c = (Command)i.next();
      if(c instanceof Write)
	addWrite(p,(Write)c);
      else if(c instanceof Read) {
	if (prev_c instanceof Write){
	  System.err.println("WARNING: "+name.substring(0, name.length()-3)+"in contains a read directly after a write.");
	  System.err.println("This will most likely cause the read to report failure during execution."); 
	  System.err.println("Consider changing it so there is a wait between the two.");
	  System.err.println("A wait has been automatically placed for you.\n");
	  addWait(p, null);
	}
	addRead(p, (Read)c, name);
      }
      else if(c instanceof Wait)
	addWait(p, (Wait)c);
      else
	System.err.println("Unknown Command type in "+name.substring(0, name.length()-3)+"in.");
      prev_c = c;
    }

    p.addStatement(new AssertionStatement(new Expression(new SimpleName("false")), 
					  new Expression(new StringLiteral("Done signal found.")), 
					  AssertionStatement.NOTE));
    p.addStatement(new WaitStatement());
  }
  
  void addWrite(ProcessStatement p, Write w) {
    SimpleName period = new SimpleName("PERIOD");
    String port_name = w.sector;

    //This currently only takes the first element and 
    // ignores any others that might be there.
    //Right now, writing to arrays is not supported
    
    String wv = w.value[0].toString();
    BigInteger value;
    if(wv.endsWith("d")) {
      long l = Double.doubleToRawLongBits(Double.parseDouble(wv));
      value = new BigInteger(""+l);
    }
    else if(wv.endsWith("x")) {
      value = new BigInteger(wv.substring(wv.length()-1), 16);
    }
    else if(wv.endsWith("f") || wv.indexOf(".")>0) {
      int i = Float.floatToRawIntBits(Float.parseFloat(wv));
      value = new BigInteger(""+i);
    }
    else{
      value = new BigInteger(wv);
    }

    int width=((Integer)widths.get(port_name)).intValue();
    SimpleName port=(SimpleName)inputs.get(port_name);

    if(port_name.equals(".reset") || port_name.equals(".start")) {
      p.addStatement(new SignalAssignment(port, new Waveform(VHDLConstant.genConstant(value, width))));
      p.addStatement(new WaitStatement((Expression)null,new Expression(period)));
      p.addStatement(new SignalAssignment(port, new Waveform(VHDLConstant.genConstant(BigInteger.ZERO, width))));
    }
    else{
      int width_we=((Integer)widths.get(port_name+"_we")).intValue();
      SimpleName port_we=(SimpleName)inputs.get(port_name+"_we");
      p.addStatement(new SignalAssignment(port, new Waveform(VHDLConstant.genConstant(value, width))));
      p.addStatement(new SignalAssignment(port_we, new Waveform(VHDLConstant.genConstant(BigInteger.ONE,width_we))));
      p.addStatement(new WaitStatement((Expression)null,new Expression(period)));
      p.addStatement(new SignalAssignment(port, new Waveform(VHDLConstant.genConstant(BigInteger.ZERO,width))));
      p.addStatement(new SignalAssignment(port_we, new Waveform(VHDLConstant.genConstant(BigInteger.ZERO,width_we))));
    }
  }

  void addRead(ProcessStatement p, Read r, String name) {
    String port_name = r.sector;
    String wr = r.value[0].toString();
    BigInteger value;
    if(wr.endsWith("d")) {
      long l = Double.doubleToRawLongBits(Double.parseDouble(wr));
      value = new BigInteger(""+l);
    }
    else if(wr.endsWith("x")) {
      value = new BigInteger(wr.substring(wr.length()-1), 16);
    }
    else if(wr.endsWith("f") || wr.indexOf(".")>0) {
      int i = Float.floatToRawIntBits(Float.parseFloat(wr));
      value = new BigInteger(""+i);
    }
    else{
      value = new BigInteger(wr);
    }

    Literal first = new StringLiteral("N wasn't set correctly to "+r.value[0]+" (N=");
    FunctionCall to_string = new FunctionCall(new SimpleName("to_string"));
    to_string.add((SimpleName)outputs.get(port_name));
    Literal second = new StringLiteral(") "+name);

    int width=((Integer)widths.get(port_name)).intValue();
    SimpleName port=(SimpleName)outputs.get(port_name);

    //Have to disable anysize zeros since the compiler doesn't like them in asserts
    p.addStatement(new AssertionStatement(new Expression(new Eq(port, VHDLConstant.genConstant(value, width, false))),
					  new Expression(new Concat(first,to_string,second)),
					  AssertionStatement.ERROR));
  }

  void addWait(ProcessStatement p, Wait w) {
    //This appears in so many places that it makes sense to make it a constant somewhere someday
    SimpleName period = new SimpleName("PERIOD");
    
    if(w == null) {
      p.addStatement(new WaitStatement((Expression)null, new Expression(period)));
      return;
    } 

    String port_name = w.sector;
    if(port_name == null) {
      p.addStatement(new WaitStatement((Expression)null, new Expression(period)));
    } 
    else if(port_name.equals(".done")) {
      p.addStatement(new WaitStatement(new Expression(new Eq((SimpleName)outputs.get(port_name), Char.ONE))));
      p.addStatement(new WaitStatement((Expression)null,new Expression(period)));
    }
    else if(port_name.equals(".reset")) {
      p.addStatement(new WaitStatement((Expression)null, new Expression(new Mult(period,new NumericLiteral(8)))));
    }

  }
    
  public static void main(String args[]) {
    Circuit dc = new VHDLCircuit(null, null, "Bob");
    //dc.setClustering(true);

    dc.insertInPort("a","data_a",1);
    dc.insertInPort("b","data_b",1);
    dc.insertOperator(Operation.AND, "data_a", "data_b", "and_ab", 1);
    dc.insertRegister("my_reg", "and_ab", null, "result", 1, 0);
    dc.insertOutPort("result","c",1);

    Circuit child = dc.insertCircuit("my_child");
    child.insertInPort("data_a","a","my_a",1);
    child.insertInPort("data_b","b","my_b",1);
    child.insertOutPort("my_c","c","result_2",1);
    child.insertOperator(Operation.OR, "my_a", "my_b", "my_c", 1);
    child.insertOperator(Operation.MUX, "my_a", "my_b", "my_c", "s", 1);

    dc.insertOutPort("result_2","d",1);

    dc.build("test.dot",null);
  }


  

}

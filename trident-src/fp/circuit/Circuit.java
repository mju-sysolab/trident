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
package fp.circuit;


import fp.util.Nameable;
import fp.util.UniqueName;

import java.util.*;
import java.math.BigInteger;


/**
 * This is a generic circuit object for generated other circuit
 * formats.  It was originally developed for use with JHDL for the Sea
 * Cucumber synthesis tool.  This is a simplified version that
 * hopefully removed some of the more tedious elements of the first
 * version.
 * 
 * The Circuit object contains other _circuits, _nets, _nodes, and
 * _ports.  there are some naming objects and a reference to its
 * parent.  There are also several abstract methods that are to be
 * implemented by a circuit object of a specific technology.  This
 * allows the circuits to be built using the generic abstract class
 * and then "build" the circuit in the underlying technology.  This
 * allows several different backends to be targeted from the same
 * generic circuit object.
 * 
 * @author Justin L. Tripp
 * @version $revision$
 */
public abstract class Circuit extends Nameable {

  int WIDTH = -1;
  int CONTROL = 1;


  /**
   * Subcircuits contained within this level of hierarchy.  They may
   * also have subcircuits as well.
   */
  private HashSet _circuits;
  /**
   * The wires contained within this circuit level.
   */
  private HashSet _nets;
  /**
   * These are all of the leaf-nodes found at this level.  These are
   * generally registers, operations, memories and the objects that
   * will have a definition outside of this circuit.
   */
  private HashSet _nodes;
  /**
   * This hash is for easy differetiation between ports and other
   * nodes.  All of the ports are contained within the node hash.
   */
  private HashSet _ports;


  /**
   * This is a special hash for associating wires.
   */
  private NetHash _nethash;

  /**
   * The parent of this level of heirarchy.  If it is
   * <code>null</code>, then this is the highest level of heirarchy.
   */
  private Circuit _parent;
  /**
   * This is the non-unique version of the name of level of the
   * circuit.
   */
  private String _ref_name;

  // and if we are a leaf
  /**
   * This and parameters may be cruft from the idea that leaves in a
   * circuit, which are externally defined objects should be Circuits
   * and not Nodes.  I am not so convinced now.
   */
  private String _object;
  private Object[] _parameters;

  /** 
   * For naming different elements in the circuit with unique names.
   */
  private UniqueName _unique_names;

  /**
   * Wire name generation for this level of heirarchy.
   */
  private UniqueName _wire_names;

  // hmmm ...
  /**
   * This is for naming nodes.  Wires and nodes can have name
   * collisions, which is probably fine.
   */
  private static UniqueName uname = new UniqueName("_");

  /**
   * The main constructor for building Circuit heirachies.  It creates
   * all of the datamembers that are necessary.
   * 
   * @param parent CIrcuit's parent 
   * @param name Name of this 
   *     level.
   */
  public Circuit(Circuit parent, String name) {
    super(parent == null ? name : uname.getUniqueName(name));
    _ref_name = name;
    _parent = parent;

    if (_parent != null) 
      _parent.addCircuit(this);

    _circuits = new HashSet();
    _nets = new HashSet();
    _nodes = new HashSet();
    _ports = new HashSet();

    _nethash = new NetHash(this);
    _wire_names = new UniqueName();
    _unique_names = new UniqueName();
  }

  /**
   * This is the leaf version of the Circuit.  The additional fields
   * in the constructor set the name of the object and parameters to
   * be passed to that object.  They could also be extended to be used
   * however one feels necessary.
   * 
   * @param parent Parent circuit
   * @param name Name
   * @param object Object that 
   *     defines behavior
   * @param parameters Parameters 
   *     to that behavior 
   */
  public Circuit(Circuit parent, String name, String object,
		      Object[] parameters) {
    this(parent, name);
    _object = object;
    _parameters = parameters;

    // ??? -- we do not need them, but do we care ???
    _nets = null;
    // ports -- still up in the air about ports.
    // may be the isLeaf() call should be _circuit == null ????
  }    

  /**
   * Simple accessor
   * 
   * @return parent Circuit object
   */
  public Circuit getParent() { return _parent; }
  /**
   * This tests to see if there are any subcircuits at this level of
   * heirachy.  There still may be leaves of this circuit.
   * 
   * @return true if there are no 
   *     subcircuits.
   */
  public boolean isParent() { return _circuits.size() > 0; }
  /**
   * This is a test to see if there no subcircuits.  It does not say
   * that there are no leaves (nodes).
   * 
   * @return if size of the circuits is 
   *     zero
   */
  public boolean isLeaf() { return _circuits.size() == 0; }
  

  // Nodes accessors and Methods
  /**
   * Add a node to the node hash.
   * 
   * @param node A circuit Node.
   */
  void addNode(Node node) { _nodes.add(node); }

  // this is not exclusive -- it may include subCircuits ...
  /**
   * Simple accessor.
   * 
   * @return returns a reference to the 
   *     _nodes Hash.
   */
  protected HashSet getNodes() { return _nodes; }

  /**
   * Simple accessor.
   * 
   * @return Returns non-unique name.
   */
  public String getRefName() { return _ref_name; }
  
  // Net accessors and methods

  /**
   * Adds a net to the net hash ... not to be confused with the
   * NetHash. :)
   * 
   * @param net net to be added.
   */
  void addNet(Net net) { _nets.add(net); }

  

  /**
   * Simple accessor.
   * 
   * @return return simple hash of nets.
   */
  protected HashSet getNets() { return _nets; }

  /**
   * Query the unique wire namer for a unique wire name.  It is
   * important to choose a useful seed.
   * 
   * @param seed Base name for wire.
   * @return unique ware name.
   */
  public String getUniqueWireName(String seed) {
    return _wire_names.getUniqueName(seed);
  }
  
  /**
   * This is the lazy unique wire namer.  It does not provide
   * insightful wire names.
   * 
   * @return Unique wire name.
   */
  public String getUniqueWireName() {
    return getUniqueWireName("net");
  }

  /**
   * Gives access to unique names for elements in each circuit.
   */  
  public String getUniqueName() {
    return _unique_names.getUniqueName("");
  }


  // ports
  /**
   * Simple accessor.
   * 
   * @return port hash.
   */
  public HashSet getPorts() { return _ports; }
  /**
   * Adds a port to the port hash.  Since ports are also nodes, this
   * is only used for quick classification.
   * 
   * @param p A port.
   */
  void addPort(Port p) { _ports.add(p); }


  public Port getPortByName(String name) { 
    for(Iterator iter = _ports.iterator(); iter.hasNext(); ) {
      Port p = (Port)iter.next();
      //      System.out.println(" Port :"+p+" matches ? "+name);

      if (p.getName().equals(name)) {
	return p;
      }
    }
    return (Port)null;
  }



  // sub graph accessors and methods

  /**
   * Adde a new sub circuit to this circuit.
   * 
   * @param graph Circuit to be a child 
   *     of this circuit.
   */
  public void addCircuit(Circuit graph) {
    _circuits.add( graph );
  }

  /**
   * Simple accessor.
   * 
   * @return This method returns a 
   *     reference to the circuit hash.
   */
  protected HashSet getCircuits() { return _circuits; }

  
  /**
   * Simple accessor.
   * 
   * @return the NetHash.
   */
  protected NetHash getNetHash() { return _nethash; }

  


  /*
    Insert Object into circuit
    
  */

  /**
   * This inserts a new circuit object with name.  Since newCircuit is
   * abstract it will create the underlying circuit of the current
   * implementation technology.
   * 
   * @param name New name.
   * @return the new Circuit.
   */
  public Circuit insertCircuit(String name) {
    //new Exception("Building "+name).printStackTrace();
    Circuit c = newCircuit(this, name);
    addCircuit(c);
    return c;
  }


  private String extendFpValue(String val, int width) {
    String extVal = new String(val);
    int hexLength = width/4;
    for(int i = 0; i < (hexLength - val.length()); i++)
      extVal = "0"+extVal;
    return extVal;
  }
  
  private Constant insertConstant(String name, String val, String out_name, 
				  int width, int type) {
    if (width <= 0) 
      new CircuitException("insertConstant: Constant width too small <= 0");

    Constant c = newConstant(this, name, val, width, type);

    PortTag out = c.addPort("out", PortTag.OUT, width);
    
    _nethash.addSource( out_name, out);
    // addConstant(c);
    return c;
  }

  public Constant insertConstant(String name, BigInteger val, 
				 String out_name, int width) {
    return insertConstant(name, val.toString(16), out_name, width, 
			  Constant.INT);
  }

  public Constant insertConstant(String name, int val, 
				 String out_name, int width) {
    BigInteger big = BigInteger.valueOf(val);
    return insertConstant(name, big, out_name, width);
  }

  public Constant insertConstant(String name, float val, 
				 String out_name, int width) {
    String constVal = Integer.toHexString(Float.floatToRawIntBits(val));
    String extVal = (val < 0) ? constVal : extendFpValue(constVal, width);
    return insertConstant(name, extVal, out_name, width, Constant.FLOAT);
  }

  public Constant insertConstant(String name, double val, 
				 String out_name, int width) {
    String constVal = Long.toHexString(Double.doubleToRawLongBits(val));
    String extVal = (val < 0) ? constVal : extendFpValue(constVal, width);
    return insertConstant(name, extVal, out_name, width, Constant.DOUBLE);
  }



  public FSM insertFSM(String name, LinkedList inputs, 
		       StateMachine transitions) {
    FSM f = newFSM(this, name, transitions);

    for(ListIterator list_iter = inputs.listIterator(); 
	list_iter.hasNext(); ) {
      String input = (String)list_iter.next();
      PortTag in = f.addInPort(input, CONTROL);
      _nethash.addSink(input, in);
    }

    for(int i=0; i< transitions.getStates(); i++) {
      String external = "s%"+i;
      String output = StateMachine.FSM_OUT + i;

      PortTag out = f.addOutPort(output, CONTROL);
      _nethash.addSource(external, out);
    }
    return f;
  }
    



  /**
   * This is a new Port insert method.  It allows the wires and the
   * port to be declared all in one place.  Wires in Circuit are soft,
   * so the wires are just names that one hopes actually meet up.
   * They will only meet up if the same name is used by both sources
   * and sinks.  This is a double edged sword as it is easy, but it is
   * also easy to make a mistake.
   * 
   * @param parent_net The net that 
   *     exists in the parent
   * @param name The name of the 
   *     portInfo in the Port.
   * @param child_net The net that 
   *     will exist in the child
   * @param width The size of the 
   *     portinfo in the Port
   * @return The new Port.
   */
  public Port insertInPort(String parent_net, String name, 
			   String child_net, int width) {
    Port port = newPort(this, name, width, PortTag.IN);
    
    if (parent_net != null) {
      PortTag in = port.addPort("in", PortTag.IN, width);
      _parent.getNetHash().addSink(parent_net, in);
    }

    PortTag out = port.addPort("out", PortTag.OUT, width);
    _nethash.addSource(child_net, out);
    _ports.add(port);
    return port;
  }


  /**
   * This is the old way of inserting a port.  It is tedious because
   * someone has to find the port and attach another wire to the other
   * side (in this case on the outside).  The other method will allow
   * faster wiring of levels of heirachy.
   * 
   * @param in_name The name of the new 
   *     Port
   * @param out_name The name of 
   *     the wire inside of the port
   * @param width size of the new 
   *     Port
   * @return The new port.
   */
  public Port insertInPort(String in_name, String out_name, int width) {
    return insertInPort(null, in_name, out_name, width);
  }

  
  void insertParentPort(Node node, String name, 
			int direction, int width) {

    PortTag port = node.addPort(name, direction, width);

    // hmm ?

    if (direction == PortTag.IN) {
      _nethash.addSink(name, port);
      insertInPort(name, name, name, width);
    } else {
      _nethash.addSource(name, port);
      insertOutPort(name, name, name, width);
    }

  }

  public Memory insertMemory(String name, String data, int width,
			     String address, int address_width,
			     String we, String output, int[] contents) {

    Memory memory = newMemory(this, name, width, address_width, contents);

    PortTag address_port = memory.addPort("addr",PortTag.IN, address_width);
    _nethash.addSink( address, address_port);

    PortTag clk = memory.addPort("clk", PortTag.IN, CONTROL);
    _nethash.addSink("clk",clk);

    // add a port for the processor.
    // hmmm ... if we knew if there was more than one memory
    // and if there were the same size (which they must be same size or 
    // smaller, then -- we could add a high bits for addressing multiple
    // rams ...
    insertParentPort(memory, "proc_addr", PortTag.IN, address_width);

    if (data != null) {
      // these go together.
      PortTag in = memory.addPort("data", PortTag.IN, width);
      _nethash.addSink( data, in);
      PortTag we_port = memory.addPort("we", PortTag.IN, CONTROL);
      _nethash.addSink( we, we_port);
    }
    
    insertParentPort(memory, "proc_data", PortTag.IN, width);
    insertParentPort(memory, "proc_we", PortTag.IN, CONTROL);

    if (output != null) {
      PortTag out = memory.addPort("d_out", PortTag.OUT, width);
      _nethash.addSink( output, out);
    }

    insertParentPort(memory, "proc_out", PortTag.OUT, width);

    return memory;
  }
			 


  public Node insertNode(String name, HashMap ports, String object_name, 
			 Object[] parameters) {
    Node node = newNode(this, name, ports, object_name, parameters);
    
    for (Iterator set_itr = ports.entrySet().iterator(); set_itr.hasNext();) {
      Map.Entry entry = (Map.Entry)set_itr.next();
      String net = (String)entry.getKey();

      // my port_infos do not have nets.
      PortTag port_info = (PortTag)entry.getValue();
             
      // ?? not necessary, done when added.
      // port_info.setParent(node);

      node.addPortTag(port_info);
      if (port_info.getType() == PortTag.IN) {
        _nethash.addSink(net, port_info);
      } else if (port_info.getType() == PortTag.OUT) {
        _nethash.addSource(net, port_info);
      } else {
        System.err.println("Circuit: insertNode: Unhandled port direction"
                           +port_info.getType());
        System.exit(-1);
      }
    }

    return node;
  }
			 

    
  /**
   * Create a new Port and associate wires with both sides of the new
   * port.
   * 
   * @param child_net Name of the 
   *     insider wire
   * @param name Name of the port
   * @param parent_net Name of the 
   *     outside wire
   * @param width size of the port
   * @return the newly created Port.
   */
  public Port insertOutPort(String child_net, String name, 
			    String parent_net, int width) {
    Port port = newPort(this, name, width, PortTag.OUT);
    
    PortTag in = port.addPort("in", PortTag.IN, width);
    _nethash.addSink(child_net, in);

    if (parent_net != null) {
      PortTag out = port.addPort("out", PortTag.OUT, width);
      _parent.getNetHash().addSource(parent_net, out);
    }

    _ports.add(port);
    return port;
  }
  

  
  /**
   * Inserts a new Out port into the current Circuit.
   * 
   * @param in_name Inside wire name
   * @param out_name The name of 
   *     the new Port
   * @param width size of the new 
   *     Port
   * @return the newly created Port.
   */
  public Port insertOutPort(String in_name, String out_name, int width) {
    return insertOutPort(in_name, out_name, null, width);
  }

  
  /**
   * This is the generic operator insertation method.  It allows
   * operators of any number of inputs and outputs and sizes to be
   * inserted.  The input/outputs of this operator are defined by a
   * HashMap made up of PortTags.  The Hash should be "String"
   * "PortTag" where the string represents the name of the wire that
   * should connect to this port.  This probably should be a special
   * subclass of HashMap to assure that it is built correctly.
   * 
   * @param type the kind of operation.
   * @param map The map of inputs 
   *     and outputs and their wires..
   * @return A newly minted Operator.
   */
  public Operator insertOperator(String name, Operation type, HashMap map) {
    Operator op = newOperator(this, name, type);

    // If this is pipelined...then a clk input should be added.
    if(type.isPipelined()) { 
      PortTag clk_info = new PortTag(op, "clk", PortTag.IN, 1);
      op.addPortTag(clk_info);
      _nethash.addSink("clk", clk_info);
    }

    for (Iterator set_itr = map.entrySet().iterator(); set_itr.hasNext();) {
      Map.Entry entry = (Map.Entry)set_itr.next();
      String net = (String)entry.getKey();
      
      PortTag port_info = (PortTag)entry.getValue();
             
      op.addPortTag(port_info);
      if (port_info.getType() == PortTag.IN) {
        _nethash.addSink(net, port_info);
      } else if (port_info.getType() == PortTag.OUT) {
        _nethash.addSource(net, port_info);
      } else {
        System.err.println("Circuit: insertOpertor: Unhandled port direction"
                           +port_info.getType());
        System.exit(-1);
      }
    }
    
    return op;
  }




  /**
   * This is a more traditionaly insertOperator for a fixed number of
   * inputs All of the inputs and outputs must be the same size.  This
   * is true of logic functions (sometimes) and some operators.
   * 
   * @param type The kind of operator.
   * @param in0 Input wire 1
   * @param in1 Input wire 2
   * @param in2 Input wire 3
   * @param out Output wire
   * @param width 
   * @return New Operator.
   */
  public Operator insertOperator(Operation type, String name, 
				 String in0, String in1, String in2,
				 String out, int width) {
    Operator op = newOperator(this, name, type);

    // If this is pipelined...then a clk input should be added.
    if(type.isPipelined()) { 
      PortTag clk_info = new PortTag(op, "clk", PortTag.IN, 1);
      op.addPortTag(clk_info);
      _nethash.addSink("clk", clk_info);
    }

    PortTag port0 = op.addPort("in0", PortTag.IN, width);
    _nethash.addSink(in0, port0);

    if (in1 != null) {
      PortTag port1 = op.addPort("in1", PortTag.IN, width);
      _nethash.addSink(in1, port1);
    }

    if (in2 != null) { 
      PortTag port2 = op.addPort("in2", PortTag.IN, width);
      _nethash.addSink(in2, port2);
    }

    PortTag q = op.addPort("out", PortTag.OUT, width);
    _nethash.addSource(out, q);
    return op;
  }


  /**
   * @param type 
   * @param in0 
   * @param in1 
   * @param out 
   * @param width 
   */
  public Operator insertOperator(Operation type, String name, 
				 String in0, String in1, String out,
				  int width) {
    return insertOperator(type, name, in0, in1, null, out, width);
  }

  /**
   * @param type 
   * @param in0 
   * @param out 
   * @param width 
   */
  public Operator insertOperator(Operation type, String name, 
				 String in0, String out, int width) {
    return insertOperator(type, name, in0, null, null, out, width);
  }


  /**
   * @param in_name 
   * @param out_name 
   * @param width 
   * @param contents 
   */
  /*
  public Register insertRegister(String name, String in_name, String out_name, 
				 int width, int contents) {
    return insertRegister(name, in_name, null, out_name, width, contents);
  }
  */

  /**
   * Creates s new register with a write enable and a preset value.
   * The preset value and write enable are optional.  This method uses 
   * a hex string to hold integer/float/double types.
   * 
   * @param in_name in wire name
   * @param we_name write enable wire (optional)
   * @param out_name output name
   * @param width size of register
   * @param contents pre-set value
   * @return The new register.
   */
  private Register insertRegister(String name, String in_name, String we_name, 
				 String out_name, int width, String contents) {
    Register reg = newRegister(this, name, width, contents);
    
    PortTag in = reg.addPort("in", PortTag.IN, width);
    PortTag out = reg.addPort("out", PortTag.OUT, width);

    /*    
    PortTag clk = reg.addPort("clk", PortTag.IN, CONTROL);
    _nethash.addSink("clk",clk);
    */

    PortTag we = null;
    if (we_name != null) {
      we = reg.addPort("we", PortTag.IN, CONTROL);
      _nethash.addSink(we_name, we);
    }

    _nethash.addSink( in_name, in);
    _nethash.addSource( out_name, out);
    
    return reg;
  }


  /** 
   * Creates a new register with a integer preset value.
   */
  public Register insertRegister(String name, String in_name, String we_name, 
				 String out_name, int width, int contents) {
    BigInteger big = BigInteger.valueOf(contents);
    return insertRegister(name, in_name, we_name, out_name, width, 
			  big);
  }

  /** 
   * Creates a new register with a integer preset value.
   */
  public Register insertRegister(String name, String in_name, String we_name, 
				 String out_name, int width, 
				 BigInteger contents) {
    return insertRegister(name, in_name, we_name, out_name, width, 
			  contents.toString(16));
  }

  /**
   * Creates a new register with a float preset value.
   */
  public Register insertRegister(String name, String in_name, String we_name, 
				 String out_name, int width, float contents) {
    
    return insertRegister(name, in_name, we_name, out_name, width, 
			  Integer.toHexString(Float.floatToRawIntBits(contents)));
  }

  /**
   * Creates a new register with a double preset value.
   */
  public Register insertRegister(String name, String in_name, String we_name, 
				 String out_name, int width, double contents) {
    return insertRegister(name, in_name, we_name, out_name, width, 
			  Long.toHexString(Double.doubleToRawLongBits(contents)));
  }

  /**
   * Creates a hashset containing all of the nets of the circuit. 
   */
  public HashSet collectNets(HashSet net_collection) {
    // Add the nets at this level of hierarchy.
    net_collection.addAll(getNets());

    // Recursively go through sub-circuits...
    Iterator it = getCircuits().iterator();
    while(it.hasNext()) {
      Circuit c = (Circuit) it.next();
      c.collectNets(net_collection);
    }

    return net_collection;
  }
  
  /*
    abstract CircuitCode 
  */
  
  /**
   * This is what is called to actuall build the circuit.  The generic
   * objects allow for everything to be inserted and associated but
   * the build process actually forms the circuit.  Prior to the build
   * process is a good point at which to capture the generic circuit
   * object.  This can be reformed and built at a later point if
   * necessary.  The arg_o is really optional, but I like to leave a
   * way to pass some random bits down to the implementation layers.
   * 
   * @param name Name of thing to be built
   * @param arg_o Special args
   */
  public abstract void build(String name, Object[] arg_o);

  /**
   * Not sure.  The old version had it -- it may need to be dropped.
   * The only widths to be concerned about are the wires, since I do
   * not think they need declared widths.
   * 
   * @return true if the width of this object was set.
   */
  public abstract boolean setWidth();

  /**
   * These are really the "magic" of the Circuit object.  They allow
   * the object to be used without knowing what the underlying
   * technology is.  All off the elements of the object are abstracted
   * away and must be called using these creation methods.
   * 
   * @param graph parent Circuit
   * @param name Name of the new circuit.
   * @return a New Circuit
   */
  protected abstract Circuit newCircuit(Circuit graph, String name);

  /**
   * Produce a constant value.
   * 
   * @param graph parent
   * @param val constant value
   * @param width requested width of the constant value.
   * @return new constant node.
   */
  protected abstract Constant newConstant(Circuit graph, String name, 
					  String val, int width, int type);


  protected abstract FSM newFSM(Circuit graph, String name, 
				StateMachine transitions);


  /**
   * Build a new memory.
   * 
   * @param graph parent
   * @param width data bus size
   * @param address_width address bus size
   * @param contents optional array defining the content
   * @return A new Memory.
   */
  protected abstract Memory newMemory(Circuit graph, String name, 
				      int width, int address_width, 
				      int[] contents);

  /**
   * Insert a new Wire.
   * 
   * @param graph parent
   * @param name name
   * @return the shiney new wire
   */
  protected abstract Net newNet(Circuit graph, String name);

  /**
   * This inserts a new "Leaf" Node object that will not be defined in this
   * framework.  This allows the insertion of random black boxes which
   * will be built using whatever tricks the underly technology wants
   * to use.  For example with JHDL, it was possible to build a
   * circuit modules that the synthesizer did not specifically know
   * about.
   * 
   * @param graph Circut parent
   * @param name Name of the object
   * @param ports Hashmap defining the ports and wires?
   * @param object Name of the magic object
   * @param objects special paramters or whatever the underlying guys
   * want.
   * @return A new circuit node.
   */
  protected abstract Node newNode(Circuit graph, String name, 
				  HashMap ports, String object, 
				  Object[] objects);
  

  /**
   * create a new Operator.
   * 
   * @param graph parent
   * @param type type of operator.
   * @return The new operator.
   */
  protected abstract Operator newOperator(Circuit graph, String name, 
					  Operation type);

  /**
   * Create a new port.
   * 
   * @param graph parent.
   * @param name name of new port.
   * @param width size of the port.
   * @param direction Direction in PortTag.CONSTANT.
   * @return The new port.
   */
  protected abstract Port newPort(Circuit graph, String name, int width, 
				  int direction);

  /**
   * Create a new register.
   * 
   * @param graph parent
   * @param width size
   * @param contents contents
   * @return the new register
   */
  protected abstract Register newRegister(Circuit graph, String name,
					  int width, String contents);

    

}

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
package fp.circuit.dot;

import java.util.HashMap;
import java.util.Iterator;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;

import fp.circuit.*;

public class DotCircuit extends Circuit implements Dot {

  private Object _tech_object;
  private static boolean _cluster = false;
  protected static String cluster = "";

  public DotCircuit(Circuit parent, Object tech_object, String name) {
    super(parent, name);

    _tech_object = tech_object;

  }

  public void setClustering(boolean b) { _cluster = b; }

  protected Circuit newCircuit(Circuit graph, String name) {
    return new DotCircuit(graph, _tech_object, name);
  }

  protected Constant newConstant(Circuit graph, String name, 
				 String value, int width, int type) {
    return new DotConstant(graph, name, value, width, type);
  }

  protected FSM newFSM(Circuit graph, String name, StateMachine transitions) {
    return new DotFSM(graph, name, transitions);
  }

  protected Memory newMemory(Circuit graph, String name, int width, 
			     int a_width, 
			     int[] contents) {
    return new DotMemory(graph, name, width, a_width, contents);
  }

  protected Net newNet(Circuit graph, String name) {
    return new DotNet(graph, name);
  }

  protected Node newNode(Circuit graph, String name, HashMap ports, 
			 String object, Object[] objects) {
    return new DotNode(graph, name, ports, object, objects);
  }

  protected Operator newOperator(Circuit graph, String name, Operation type) {
    return new DotOperator(graph, name, type);
  }

  protected Port newPort(Circuit graph, String name, int width, int direction) {
    return new DotPort(graph, name, width, direction);
  }

  protected Register newRegister(Circuit graph, String name, int width, String contents) {
    return new DotRegister(graph, name, width, contents);
  }



  public String toDot() {
    StringBuffer s = new StringBuffer();
    // to cluster or not to cluster -- that is the question?
    //System.out.println("Cluster is "+_cluster);
    if (_cluster) 
      cluster = "cluster_";
    else
      cluster = "";
    return toDot(s).toString();
  }

  public StringBuffer toDot(StringBuffer s) {
    if( isParent() || getNets().size() > 0 ) {
      appendHeader( s );
      appendVertices( s );
      appendEdges( s );
      appendSubgraphs( s );
      appendFooter( s );
    }
    return s;
  }	
  
  protected void appendHeader( StringBuffer s ) {
    if( getParent() == null ) {
      s.append("// Dotfile created by toDot()\n");
      s.append("\ndigraph \"" + getName() + "\" {\n");
    } else {
      s.append("\nsubgraph \"").append(cluster).append(getName());
      s.append("\" {\n  label = \"").append(getName()).append("\"\n");
    }
    if( getNodes().size() > 0 ) {
      s.append("\n\t// Vertices\n");
      s.append("node [shape=record];\n");
    }
  }
  
  protected void appendVertices( StringBuffer s ) {
    // all nodes are part of _nodes and so are 
    // ports.

    Iterator node_iter = getNodes().iterator();
    while (node_iter.hasNext()) {
      s.append("\t");
      /*
	this cast is a problem -- Dot* classes have no common
	ancestor -- so should we develop a Dot interface or something
	so that they can all implement the toDot() call.
      */
      Node n = (Node)node_iter.next();
      //System.out.println(" Node : "+n);

      s.append(((Dot)n).toDot());
    }
  }
  
  protected void appendEdges( StringBuffer s ) {
    s.append("\n\t// Edges\n");
    for (Iterator net_iter = getNets().iterator(); net_iter.hasNext(); ) {
      Net e = (Net)net_iter.next();
      s.append("\t");
      s.append(((Dot)e).toDot());
    }
  }
  
  protected void appendSubgraphs( StringBuffer s ) {
    for (Iterator iter = getCircuits().iterator(); iter.hasNext(); ) {
      s.append( ((Dot)iter.next()).toDot() );
    }
  }
  
  protected void appendFooter( StringBuffer s ) {
    s.append("}\n");
  }
  
  public void printGraph(String outputName) {
    printGraph(this, outputName);
  }
  
  /**
   * static method that could used from outside to print a Graph
   **/
  public static void printGraph(DotCircuit g) {
    printGraph(g, g.getName()+".dot");
  }

  /**
   * a filename can be specified where the output shoud go
   */
  public static void printGraph(DotCircuit g, String fname) {
    PrintWriter out = getPrintWriter(fname);
    System.out.println("Writing graph "+g.getName()+" in file "+fname);
    out.println(g.toDot());
    out.close();
  }

  public static PrintWriter getPrintWriter(String fileName) {
    Writer ostream = null ;
    try {
      ostream = new java.io.FileWriter(new File(fileName) ) ;
    }
    catch (java.io.IOException e) {
      e.printStackTrace();
      return null ;
    }
    return new PrintWriter(ostream);
  }


  /*
    I do not think I need this 

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  */

  
  public void build(String name, Object[] objs) {

    // we need to get just the file basename and path from it
    int index = name.lastIndexOf(System.getProperty("file.separator"));
    int last_dot = name.lastIndexOf(".");
    String basename = name.substring(index+1,last_dot);
    String path = name.substring(0, index+1);

    // now create the changed file name
    String file_name = path + "syn_" + basename + ".dot";

    printGraph(file_name);
  }

  public boolean setWidth() {
    // What is this for???
    return true;
  }


  public static void main(String args[]) {
    /*
    DotCircuit dc = new DotCircuit(null, null, "Bob");
    //    dc.setClustering(true);

    dc.insertInPort("a","data_a",1);
    dc.insertInPort("b","data_b",1);
    dc.insertOperator(Operation.AND, "data_a", "data_b", "and_ab", 1);
    dc.insertRegister("and_ab", null, "result", 1, 0);
    dc.insertOutPort("result","c",1);

    Circuit child = dc.insertCircuit("my_child");
    child.insertInPort("data_a","a","my_a",1);
    child.insertInPort("data_b","b","my_b",1);
    child.insertOutPort("my_c","c","result_2",1);
    child.insertOperator(Operation.OR, "my_a", "my_b", "my_c", 1);
    child.insertOperator(Operation.MUX, "my_a", "my_b", "my_c", "s", 1);

    dc.insertOutPort("result_2","d",1);

    dc.build("test.dot",null);
    */
  }


  

}

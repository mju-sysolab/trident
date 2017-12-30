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


package fp.flowgraph;

import java.util.*;

import fp.graph.Node;
import fp.util.*;

public class BlockNode extends ControlFlowNode {

  private String _name;
  private LabelOperand _label;

  private ArrayList _instructions;
  private DefHash _def_hash;
  private UseHash _use_hash;
 
  //added by Kris, 
  private DependenceFlowGraph _dfg;
  private int _ii;
  private boolean _isModScheduled;
  private boolean _isRecursive;
  private float _aveOpsPCycle;
  private float _maxOpsPCycle;
  private float _minOpsPCycle;
  private int _totOps;
  private int _cycleCount;
  private HashMap _operatorCounts;
  private int _mark;
  
  public Object scratch_object;
  public int scratch;

  private BlockNode(ArrayList instructions, String name, LabelOperand label,
                    DefHash defHash, UseHash useHash, int mark) {
    super(null); 
    _instructions = instructions;
    _name = name;
    _label = label;
    _def_hash = defHash;
    _use_hash = useHash;
    _mark = mark;
    setDotAttribute("shape", "record");
    setDotAttribute("fontName", "Courier");

  }
  
  
  public BlockNode() {
    super(null); 
    _instructions = new ArrayList();
    // should we add an begin and end ?
    _def_hash = new DefHash();
    _use_hash = new UseHash();
    _mark = 0;
    
    _dfg = null;
    _ii = 0;
    _isModScheduled = false;
    _isRecursive = false;
    _aveOpsPCycle = 0;
    _maxOpsPCycle = 0;
    _minOpsPCycle = 0;
    _totOps = 0;
    _cycleCount = 0;
    _operatorCounts = new HashMap(); 
      
    setDotAttribute("shape", "record");
    setDotAttribute("fontName", "Courier");

  }

  public void setName(String name) {
    setName(name, Operand.NOT_ASSIGNED);
  }

  public void setName(String name, int assign) { 
    _label = Operand.newLabel(name, assign);
    _name = _label.toString();
    setDotAttribute("label", _name);
  }

  public String getName() { return _name; }

  /*
    Returns the label for this block.
  */
  public LabelOperand getLabel() { return _label; }


  public int getDepth() {
    return getPreOrder() + 1;
  }

  /*
    Earlier this cared about "channel blocks" and would not
    merge if they 

  */
  public boolean isMergeable() {
    String name = getLabel().getFullName();
    // lets not merge the entry and exit blocks
    if ("GlobalEntry".equals(name) || "GlobalExit".equals(name)) 
      return false;
    return (getInDegree() == 1);
  }
  

  public boolean isMarked() { return _mark != 0; }
  public void setMark() { _mark++; }
  public void clearMark() { _mark = 0; }

  public DefHash getDefHash() { return _def_hash; }
  public UseHash getUseHash() { return _use_hash; }

  //added by kris:
  public void saveDepFlowGraph(DependenceFlowGraph dfg){ _dfg = dfg;}
  public DependenceFlowGraph getDepFlowGraph(){ return _dfg;}
  public void setII(int ii) { _ii = ii; }
  public int getII() { return _ii; }
  public void setIsModuloScheduled(boolean b) { _isModScheduled = b; }
  public boolean getIsModuloScheduled() { return _isModScheduled; }
  public void setIsRecursive(boolean b) { _isRecursive = b; }
  public boolean getIsRecursive() { return _isRecursive; }
  public void setAveOpsPCycle(float i) { _aveOpsPCycle = i; }
  public float getAveOpsPCycle() { return _aveOpsPCycle; }
  public void setMaxOpsPCycle(float i) { _maxOpsPCycle = i; }
  public float getMaxOpsPCycle() { return _maxOpsPCycle; }
  public void setMinOpsPCycle(float i) { _minOpsPCycle = i; }
  public float getMinOpsPCycle() { return _minOpsPCycle; }
  public void setTotOps(int i) { _totOps = i; }
  public int getTotOps() { return _totOps; }
  public void setCycleCount(int i) { _cycleCount = i; }
  public int getCycleCount() { return _cycleCount; }
  public void setOperatorCounts(HashMap i) { _operatorCounts = i; }
  public HashMap getOperatorCounts() { return _operatorCounts; }

  public ArrayList getInstructions() { return _instructions; }

  /*
    For the terminally lazy.

  */

  public void addInstructions(List list, int rank) {
    for(Iterator lIt = list.iterator(); lIt.hasNext(); ) 
      addInstruction((Instruction)lIt.next(), rank);
  }
 
  public void addInstructions(List list) {
    for(Iterator lIt = list.iterator(); lIt.hasNext(); ) 
      addInstruction((Instruction)lIt.next());
  }
 
  public void removeInstructions(List list) {
    for(Iterator lIt = list.iterator(); lIt.hasNext(); )
      removeInstruction((Instruction)lIt.next());
  }

  public void removeAllInstructions() {
    _instructions.clear();
    _def_hash.clear();
    _use_hash.clear();
  }

  public void updateHashes(Instruction inst) {
    _def_hash.add(inst);
    _use_hash.add(inst);
  }

  public void removeFromHashes(Instruction inst) {
    _def_hash.remove(inst);
    _use_hash.remove(inst);
  }

  // this places the instruction at a specific index in the
  //instruction ArrayList.  I added this so that in Phi lowering
  //the new loads/stores would be placed in the location where
  //the phi was to keep execution order of the intructions.
  public void addInstruction(Instruction inst, int rank) {
    _instructions.add(rank, inst);
    updateHashes(inst);
  }
  
  // here is where use and def are maintained.
  public void addInstruction(Instruction inst) {
    _instructions.add(inst);
    updateHashes(inst);
  }

  public void removeInstruction(Instruction inst) {
   if ( _instructions.remove(inst) ) {
      removeFromHashes(inst);
    }
  }
  
  public BlockNode copy() {
    return new BlockNode(_instructions, _name, _label, _def_hash, _use_hash,
                         _mark);
  }
  
  public void cloneInstructions() {
    for(Iterator it = _instructions.iterator(); it.hasNext(); ) {
      Instruction oldInst = (Instruction)it.next();
      Instruction newInst = oldInst.deepCopy();
      removeInstruction(oldInst);
      addInstruction(newInst);
    }
  }

  // should this be written a better way?
  // this is better now.  If getOutDegree is greater than 1, 
  // the block must have a conditional branch.
  public boolean containsConditionalBranch() {
    return getOutDegree() > 1;
  }

  public boolean containsUnconditionalBranch() {
    return (getOutDegree() == 1 && (getDecision() != null));
  }

  public boolean containsBranch() {
    return (getOutDegree() == 1);
  }

  // only finds the first (or last)
  // this probably do not make sense for hyperblocks.
  public Instruction getBranch() {
    int size = _instructions.size();
    if (size < 1) return null;
    // this is the cheap test -- perhaps by chance the last instruction
    // is a decision
    Instruction decision = (Instruction)_instructions.get(size - 1);
    if (Branch.conforms(decision)) 
      return decision;
    // if not, go the long way.
    for(Iterator it = _instructions.iterator(); it.hasNext(); ) {
      decision = (Instruction)it.next();
      if (Branch.conforms(decision)) 
	return decision;
    }
    return null;
  }

  // only finds the first (or last)
  // this probably does not make sense for hyperblocks.
  public Instruction getDecision() {
    int size = _instructions.size();
    if (size < 1) return null;
    Instruction decision = (Instruction)_instructions.get(size - 1);
    if (Test.conforms(decision)) 
      return decision;
    for(Iterator it = _instructions.iterator(); it.hasNext(); ) {
      decision = (Instruction)it.next();
      if (Test.conforms(decision)) 
	return decision;
    }
    return null;
  }

  // I guess enough other code uses this.
  public void setPredicates(BooleanEquation eq) {
    for(ListIterator iter = _instructions.listIterator(); iter.hasNext(); ) {
      Instruction inst = (Instruction)iter.next();
      inst.setPredicate(eq);
    }
  }



  public String toString() {
    StringBuffer result = new StringBuffer();
    int size = _instructions.size();

    result.append("\n");
    result.append(_name);
    /*if (size > 0) {
      Instruction[] inst_array = 
	(Instruction [])_instructions.toArray(new Instruction[size]);

      result.append(inst_array[0]);
      for (int i=1; i<size; i++) {
	result.append("\n");
	result.append(inst_array[i]);
      }
    }*/
    
    return result.toString();
  }

  public String toDot() {
    String old_color = getDotAttribute("color");
    if (isMarked()) {
      setDotAttribute("color","red");
    }
    
    // list -- list_label
    String list_label = toDotLabel();
    
    // use_hash
    // def_hash
    
    StringBuffer label_buf = new StringBuffer(list_label);
    int i = list_label.indexOf("{");
    label_buf.insert(list_label.indexOf("{", i) + 1,
		      "{ " + getName() + "}|"+
		      //"{" + " depth : "+getDepth()+"}|"+
		      //"{"+" pre:"+getPreOrder()+" post:"+getPostOrder()+"}|"+
		      "");
    setDotAttribute("label", label_buf.toString());

    String to_dot = super.toDot();

    if (old_color != null) {
      setDotAttribute("color", old_color);
    }
    
    return to_dot;
  }

  String toDotLabel() {
    StringBuffer sbuf = new StringBuffer();
     int size = _instructions.size();
     
     sbuf.append('{');
     if (size > 0) {
       Instruction[] inst_array = 
	 (Instruction [])_instructions.toArray(new Instruction[size]);
       for (int i=0; i< size - 1; i++) {
	 sbuf.append('{');
	 sbuf.append(inst_array[i].toDotLabel());
	 sbuf.append("}|");
       }
     
       sbuf.append('{');
       sbuf.append(inst_array[size - 1].toDotLabel());
       sbuf.append('}');
     }
     sbuf.append( '}');
     return sbuf.toString();
  }
       
		   

}


    
  

  

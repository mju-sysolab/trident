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
import fp.util.*;
import java.io.*;

import fp.hardware.*;

/** This class is used to generate the table of muxes that store the final
 *  output from a modulo scheduled loop.  The epilog block created by the modulo 
    scheduler contains the end of several iterations of a loop.  Each of these
    iterations should be trying to save values to some register for use by the
    rest of the circuit later or as output of the circuit.  However each of
    these iterations will be attempting to write to the same registers and
    multiple writes to a register is not allowed, and even if it were, there is
    nothing to gaurantee that the correct value was stored.  Also when the
    kernal exists it might start iterations in the epilog that are after what
    was supposed to be the final iteration.  We need a way to make sure only the
    correct iteration's output is stored in the registers.  This is performed
    with a tree of muxes, the inputs of which are the outputs from the different
    iterations and the select signals are made up of logic using the store
    instructions' predicates from each iteration.  
    
    It is easiest to explain how the mux tree and its control is built, using an
    example.  Let's say we have the following c code:
    
    for(int i=0; i<5; i++)
      x+=5;
      
    When the loop exits it will save a value to a register representing x.  Now
    let us assume that after translating the c code to byte code we have
    a schedule that looks like this:
    
     15
     14
     13
     12
     11
     10
     9
     8
     7
     6
     5
     4
     3
     2
     1
     0
    
     where each of these numbers represents a cycle in a schedule of the byte
     code and for each cycle there is some set of instructions.  Now let's
     assume after modulo scheduling we find an ii of 4 and can pipeline the loop
     like this:
     
     15
     14
     13
     12 
     11 15
     10 14
     9  13
     8  12
     7  11   15
     6  10   14
     5   9   13
     4   8   12
     3   7   11   15
     2   6   10   14
     1   5    9   13
     0   4    8   12
         3    7   11   15
	 2    6   10   14
	 1    5    9   13
	 0    4    8   12
	      3    7   11
	      2    6   10
	      1    5    9
	      0    4    8
	           3    7
		   2    6
		   1    5
		   0    4
                        3
			2
			1
			0
     
     iteration
     5   4    3    2    1         
     
     For this, the kernal is this:
     
     3   7   11   15
     2   6   10   14
     1   5    9   13
     0   4    8   12
     
     and the epilog is this:
     
     15
     14
     13
     12 
     11 15
     10 14
     9  13
     8  12
     7  11   15
     6  10   14
     5   9   13
     4   8   12
     
     There are three iterations running concurrently in the epilog and all three
     will try to save x.  However only the fifth iteration, which in this case
     happens to be the last iteration of the epilog, should be written to x. 
     What we need is a table of muxes to decide which iteration's output to
     write to the register.  My mux trees are as flat as possible, to try and
     reduce the number of muxes.  If there were 8 iterations in the epilog the
     mux tree would look like this:
     
                   x
               /        \
           mux5          mux6
         /     \       /     \
      mux1    mux2   mux3   mux4
      /\      /\     /\     /\
     1  2    3  4   5  6   7  8
     
     Our three iteration example above would have two muxes, the first of which
     would choose between the two earliest iterations (the two shortest), and
     the second mux would choose between the first mux's output and third
     iteration and would store the result to the x register.  
     
     My algorithm starts with a list of the outputs from each iteration.  It
     takes the first two (if there are more than 1), creates a mux and saves the
     output from the mux to a new list.  It then takes the next two outputs and
     does the same.  It continues until there is only one or zero outputs in the
     list.  If there is one, it copies this output directly to the new list and
     if there's zero, it has completed the first row of muxes.  Then it repeats
     this process on the new list of mux outputs.  it continues to repeat until
     there is only one mux output.  The final step is to create a store
     instruction to store the last mux's output to the register.  This process
     is performed on every primal operand in the loop that must be saved (i.e.
     all but the modPrims).  
     
     In addition to creating the muxes we need logic to control the select
     signals on the muxes.  We need to be able to choose the correct output to
     save to the register.  We can know this by looking at the predicates on the
     store instructions for each iteration.  In the above c-code example, there
     would be an instruction like this:
     
     x = aaa_store %tmp_1  | ~%tmp_2
     
     where "~%tmp_2" (or when %tmp_2 is 0) is the predicate.  Also, somewhere in 
     the schedule there should be an instruction to calculate %tmp_2, which 
     should look something like this:
     
     %tmp_2 = aaa_setlt %inc_1 %const5
     
     Which means that if %inc_1 is less than 5 return true (actually 1) and 
     otherwise return false (0).  Each iteration will calculate the predicate
     separately, where in iteration 1, inc_1 will be 1 (at the end), %tmp_2 will 
     be 1, and
     ~%tmp_2 will be 0, and the store will not occur.  In iteration 2, inc_1
     will be 2, and ~tmp_2 still 0, and in iteration 3, inc_1 will be 3, and
     ~tmp_2 again 0, up until iteration 5, when inc_1 will be 5 and ~tmp_2 will
     finally be 1 and the store will occur (and the loop will exit).  But
     actually, if iteration 6 is allowed to occur (which in this case could
     happen) %inc_1 will be 6, and ~tmp_2 will still be 1 and it could be stored
     too.  We don't want that value, however.  What we need is the first true. 
     So if we have conditions like the following:
     
     iteration:         1   2   3   4   5   6   7   8
     predicate value:   F   F   F   F   T   T   T   T
     
     we want to write on the first iteration that was true (6).  However, in
     while loops with strange conditions, it may be true in one iteration and
     false again in the next one.  We still, however, want the first true
     iteration. The reason, more than five iterations may have started is that
     the location that the exit condition for the kernal can be in different
     places.  Reffering back to the kernal from above:
     
     3   7   11   15
     2   6   10   14
     1   5    9   13
     0   4    8   12
    
     If the exit condition is calculated somewhere between cycles 12 and 15,
     three later cycles will have already started, and if it was calculated
     between 8 and 11, two will later cycles will have started, and if was
     calculated between 4 and 7, 1 later cycle.  In all of those conditions not
     only will the 5th iteration that we want be finishing in the epilog
     (actually if the exit condition is calculated between 12 and 15, then
     iteration 5 will be finished before the epilog, but I'll discuss that
     later), but also some later iterations will be finishing in the epilog and
     they will all want to store their values in the register and all their
     predicates will be true.  To choose the correct iteration we can take
     advantage of the mutual exclusitivity of the even when one iteration should
     be stored as opposed to another.  This means, if we have to choose between
     two different iterations with one mux like this:
     
       x
      /\
     1  2
     
     the control is iteration 1's predicate.  If it's false, the mux will select
     2 and if it's true it will select 1, even if both predicates are true, and
     iteration 1 (the earlier iteration) was choosen simply due to its location
     on the input pins.  When another layer is added, the select is only
     slightly more complicated.
     
            x
         /    \
      mux1    mux2
      /\      /\
     1  2    3  4 
     
     The control for mux 1 is iteration 1's predicate and the control for
     mux 2 is iteration 3's predicate.  The control for the output mux is
     iteration 1's predicate OR iteration 2's.  If iteration 4 is the only one
     that is true it will be selected, because 3's predicate will evaluate to
     false, and since both 1 and 2 are false the output mux will choose the 
     right input coming from mux 2.  If 3 was correct it would be choosed by
     mux 2 similar to above even if 4 was also correct, and choosen in the
     output mux because 1 and 2 were both false. If 1 or 2 was true the output
     mux would choose them even if iteration 3 and/or 4 was also correct.  When
     creating each mux, I also OR the two predicates from its input and save the
     result to be the control for the mux above and to be ORed with that mux's
     other input to create the control for the mux above that.  
     
     However there are a few special cases that must be handled slightly
     differently.  The first case is when the correct iteration actually
     finished in the kernal.  In the above example kernal:
     
     3   7   11   15
     2   6   10   14
     1   5    9   13
     0   4    8   12
     
     if the correct iteration evaluated its loop exit condition somewhere
     between cycles 12 and 15, when the kernal reached its end and exited, this
     iteration will have completely finished and the result will have been saved 
     to register x.  To handle this, I store the predicate used in the store in 
     the kernal, and load this in the epilog.  Then I use the inverse of this 
     predicate as the final epilog store to x's predicate.  That is, if the
     predicate for the kernal's store was true, the epilog store's predicate
     needs to be false.  This, unfortunately however, involves a few steps. The
     first step is to evaluate the logic used in the kernal store's predicate. 
     If it was ~%tmp_2, we must add the instruction:
     
     %tmp_3 = aaa_not %tmp_2
     
     because, unfortunately, we cannot simply store ~%tmp_2.  (And if the
     predicate were some kind of complicated logic equation it would have to
     translated into the appropriate instructions and added to the schedule.) 
     It is %tmp_3 that I store, for use in the epilog.  So I also add a store
     instruction to the loop.  However, these extra instructions cannot simply
     be inserted anywhere into the kernal, nor can they be added after
     scheduling.  They too can cause hardware conflicts and care must taken when
     scheduling them, and therefore, they must be added to the kernal, before
     modulo scheduling is performed.  However, these instructions shouldn't be
     in the prolog or epilog, so we need to know what was added so that after
     the kernal is scheduled andan unrolled copy created (which, in turn, is used
     to create the epilog and prolog), they need to be deleted from the unrolled 
     copy so they won't end up in the prolog or epilog.  Additionally, an extra
     load statement must be added to the epilog to load the kernal's predicate
     and another instruction must be added to take the inverse of this and use
     it as the epilog's final store's predicate.  (And we can't ignore this not
     and assume that the operand used in the kernal's predicate is itself the 
     inverse of the kernal's store, because the compiler doesn't know what the
     kernal predicate will be and if it will actually be ~tmp_2 like in this
     case and not simply %tmp_2 or some complicated logic equation.)
     
     And there is one final issue regarding the kernal predicates.  A %tmp_2
     will be calculated during every iteration of the kernal, but which
     iteration of the original loop this corresponds to depends on where it
     falls in the kernal.  In the above kernal, if %tmp_2 was calculated
     somewhere between cycles 8 and 11, but the store was between 12 and 15,
     then there will be a load and store between 11 and 12 transfering %tmp_2's
     value, and a new %tmp_2 copy will be created to contain its value between
     12 and 15.  If the original %tmp_2 were used, it would correspond to a
     %tmp_2 from a later iteration of the main loop.  When we store the kernal
     store predicate, we need to make sure to store the correct predicate from
     the correct iteration.  A few methods in here help with that, but also
     refer to MSchedHash and ModSched for more on this.
     
     The final mux table issue is the possibility of collapsing the tree.  Often
     many iterations have already calculated the value that will be stored in x
     before the epilog begins, and have transfered it to the epilog using a
     modprim operand.  All of these iterations will probably be reading from the
     same modPrim.  Additionally, these iterations should be next to each other.
     This means that not only do we not need several loads for each iteration to
     read the modPrim value, but we can also eliminate several muxes, since all
     their inputs should be the same.  
     
     This is performed in three steps.  The first step is to search the
     epilog for cases when a value is loaded from a modPrim and used as output
     to store in the register.  MuxTableGenerator takes note of which
     iteration's outputs are from which modPrims when they come from a modPrim
     and then it deletes those load instructions so that there won't be one for
     each iteration where this is the case.  When it knows it needs to read a
     value from a modPrim, it creates its own block operand which it will
     later use to hold the value in the modPrim when it creates its own load
     statement.  Step two is to replace any uses of all the previous loads of
     that modPrim from each iteration with a use of this MuxTableGenerator
     created block operand.  Step 3 occurs during creation of the mux tree. 
     Whenever two inputs to a mux are from the same modPrim, it doesn't create a
     mux and instead saves that this mux is instead a usage of the modPrim. 
     When it finds a mux, where only one input is from a modPrim, it creates
     that mux, but uses as one input the block operand it created to hold the
     value in the modPrim.  Finally it creates a load instruction to load the
     modPrim into its block operand.  
 * 
 * @author Kris Peterson
 */
public class MuxTableGenerator extends HashMap
{

  /**
  The select input to the muxes needs an operand, but I'm using each iteration's
  store's predicate for the select.  Even if a predicate is only one operand, it
  is of type BooleanEquation.  To get a single operand to use on the select, I
  generate all the logic instructions necessary to calculate the predicate
  (usually just a NOT) and use the output from these logic instructions as the
  select input to a mux.  Creating these equations happens during the method add
  but they are added at the end of the method genMuxTable.  This ArrayList
  stores those instructions until they can be added.
  */
  private ArrayList _logicHandleInsts = new ArrayList();
  /**
  This ArrayList stores all the kernal predicate store instructions so that they
  can be added separately after scheduling to put them at the end of the kernal.
  Actually I'm not sure why I did this.  Perhaps because I wanted to add them
  after unrolling.  I add the kernal predicate evaluation instructions before
  scheduling, but I wrote that in after writing this part, so maybe I just
  forgot to just put these with the logic instructions and add them all at the
  top.  
  */
  private ArrayList _predStores = new ArrayList();
  /**
  This guy saves the primal operand that the kernal predicate was saved to so
  that a load can be added to the epilog to retrieve it.
  key = primal being stored
  value = primal where the primal store's predicate was saved
  */ 
  private HashMap _primToPred = new HashMap();
  private HashMap _primToOldPred = new HashMap();
  /**
  This saves the kernal store predicate logic evaluation instructions so that
  they can be deleted from the unrolled copy of the scheduled loop.
  key = primal being stored to
  value = set of instructions used to evaluate the store's predicate
  */
  private HashMap _savedPredSaveStoreInsts = new HashMap();
  
  public MuxTableGenerator() {
    super();
  }

  /**
  This nested class is used to help with the creation of the mux tree.  It
  contains the block operand that is the input to the mux, this operand's
  iteration's predicate, and a modPrim primal operand that this block operand
  was loaded from if one exists.  As the mux tree is created new elements are
  created where the operand is the output from the mux in the layer below and
  the predicate is the OR of the predicates of that mux's input, and where
  modPrim is equal to a modPrim operand, if one was used by both inputs to the
  mux.
  */
  private class Element {
    public BooleanOperand pred;
    public Operand op;
    public PrimalOperand modPrim = null;
    
    public Element(BooleanOperand p, Operand o) {
    
      pred = p;
      op = o;
    
    }
    
    /**
    I add this for debugging so I could print collections of elements and get
    something meaningful
    */
    public String toString() {
      return op.toString() + " | " + pred.toString();
    }
  }  //end class Element
  
  /**
  returns all the created kernal store predicate store instructions so they can
  be added to the kernal
  */
  public ArrayList getPredStores() {
    return _predStores;
  }
  
  /**
  returns all the created logic instructions used to evaluate the kernal store 
  predicate store instructions so these instructions can be removed from the
  unrolled copy of the kernal
  */
  public HashMap getPredLogic() {
    return _savedPredSaveStoreInsts;
  }
  
  /**
  This method looks over the kernal, before it has been scheduled and looks for
  stores to primals.  When it finds one, it reads that store instruction's
  predicate, and creates logic instructions to evaluate the predicate and
  creates a store instruction to save the value of the predicate to a primal
  which will be used to transfer the value of the predicate to the epilog.  It
  saves all these instructions so they can be added to the kernal and scheduled
  when the rest of the kernal is scheduled, and they are saved to private
  variables so that they can later be removed, from the unrolled copy of the
  kernal, which will be used to create the epilog and prolog, which
  cannot have these instructions in them.
  */
  public HashMap savePredOps = new HashMap();
  public HashMap saveSuccOps = new HashMap();
  public HashSet getKernalPreds(HashSet kernalInstList, BooleanEquation loopExitPredicate) {
  
    HashSet instsToAdd = new HashSet();
    for (Iterator itin = kernalInstList.iterator(); 
       itin.hasNext(); ) {
      Instruction inst = (Instruction)itin.next();
      if(Store.conforms(inst)) {
        Operand out = Store.getDestination(inst);
	if(out.isPrimal()) {
	System.err.println("out " + out);
	System.err.println("inst " + inst);
          HashSet predLogicInsts = new HashSet();
	  PrimalOperand output = Operand.newPrimal(out.getFullName() + "_predSave");
	  
	  //create the logic instructions to evaluate the predicate:
	  BEqToList eq_list = new BEqToList(loopExitPredicate, new BooleanEquation(true));
	  instsToAdd.addAll(eq_list);
	  predLogicInsts.addAll(eq_list);
	System.err.println("predLogicInsts " + predLogicInsts);
	  _savedPredSaveStoreInsts.put(out, predLogicInsts);
	  BooleanOperand in = eq_list.getResult(); 
	  Instruction resultInst = null;
	  boolean resNotFnd=true;
	  for (Iterator eqit = eq_list.iterator(); 
	     eqit.hasNext() && resNotFnd; ) {
	    Instruction instTmp = (Instruction)eqit.next();
	    System.err.println("instTmp " + instTmp);
	    for(int i=0; i<instTmp.getNumberOfOperands() && resNotFnd; i++) {
	      if(instTmp.getOperand(i)==in) {
	        resNotFnd=false;
	        resultInst=instTmp;
	      }
	    }
	  }
	System.err.println("result " + in);
	  savePredOps.put(inst, in);
	  saveSuccOps.put(resultInst, in);
	System.out.println("result " + in);
	System.out.println("inst " + inst);
	System.out.println("resultInst " + resultInst);
	  
	  //create the store instruction to save the predicate's value:
	  BooleanEquation trueBoolEq = new BooleanEquation(true);
	  Instruction storeInst = Store.create(Operators.STORE, Type.Bool,
          				       output, in, trueBoolEq);
	  predLogicInsts.add(storeInst);
	  _predStores.add(storeInst);
	  _primToPred.put(out, output);
	  _primToOldPred.put(out, inst.getPredicate());
	}
      }
    }    
    return instsToAdd;
  }
  
  /**
  This method performs the second step described above for collapsing the mux
  tree when iterations read from the same modPrim operand.  This method creates
  a new block operand to hold the value in the modPrim operand, and then all
  instructions which use the original block operands defined by loads from that
  modprim, are changed to use this new block operand. 
  */
  public void replaceOpsWithNewMPrimBs(HashSet instSet) {
    for (Iterator itin = instSet.iterator(); 
       itin.hasNext(); ) {
      Instruction inst = (Instruction)itin.next();
      for (Iterator it = _opToElementMap.keySet().iterator(); 
         it.hasNext(); ) {
        Operand op = (Operand)it.next();
	//PrimalOperand modPrim = ((Element)_opToElementMap.get(op)).modPrim;
	HashSet elements = (HashSet)_opToElementMap.get(op);
	for (Iterator it2 = elements.iterator(); 
            it2.hasNext(); ) {
	  Element element = (Element)it2.next();
	  PrimalOperand modPrim = element.modPrim;
	  if(modPrim != null) {
	    //get a block operand specific to this modPrim operand:
	    Operand newOp = getModPrimBlock(modPrim);
	    //replace the original operand used here:
	    inst.replaceOperand(op, newOp);
	  }
	}
      }
    }
  }
  
  /**
  This HashMap was created to help me find an Element associated with a primal
  that is being stored to in the loop (a non modPrim primal).  
  key = primal
  value = element
  */
  private HashMap _opToElementMap = new HashMap();
  /**
  This hashMap saves the Type of a load instruction used to read from a modPrim,
  before saving this into an output primal. This is used, when the laod
  instructions are created to know what type the new load should have.
  key = modPrim operand
  value = Type 
  */
  private HashMap _modPrimToType = new HashMap();
  /**
  This method performs step 1 of the mux tree collapsing.  It looks for
  instances when an iteration wishes to store a value to an output primal that
  it simply loaded from a modPrim.  It also creates a set of loads of the
  modPrims so that they can be deleted.
  */
  public HashSet findModPrimSources(HashSet instSet) {
  
    HashSet instsToDelete = new HashSet();
    for (Iterator itin = instSet.iterator(); 
       itin.hasNext(); ) {
      Instruction inst = (Instruction)itin.next();
      if((Load.conforms(inst))&&
         (_opToElementMap.containsKey(Load.getResult(inst)))) {
	Operand op = Load.getResult(inst);
	//Element element = (Element)_opToElementMap.get(op);
	HashSet elements = (HashSet)_opToElementMap.get(op);
	PrimalOperand modPrim = (PrimalOperand)Load.getSource(inst);
        instsToDelete.add(inst);
	//save this modPrim's type:
	_modPrimToType.put(modPrim, inst.type());
	//tell the element that this iteration's output comes from this modprim:
	for (Iterator it2 = elements.iterator(); 
            it2.hasNext(); ) {
	  Element element = (Element)it2.next();
          element.modPrim = modPrim;
	}
      }
    }
    return instsToDelete;
  }
  
  /**
  This method saves all the inputs necessary for each mux tree.  It should be
  noted that MuxTableGenerator extends HashMap and its keys and values are as
  follows:
  key = operand to store to
  value = an ArrayList of all inputs to the mux tree (each item in this
          ArrayList is an Element object).
  MuxTableGenerator does not actually decide when a mux tree is necessary or
  what its inputs should be.  That is done inside of ModSched.java by its nested
  class MuxAdder.  Also, it is important to note, the epilog is created from the
  last iteration backwards to the first iteration out of the kernal, and
  therefore when MuxAdder tells MuxTableGenerator about the mux tree's inputs it
  tells it in the inverse order we want to select the earliest true iteration,
  and therefore they need to be turned around when they are saved here.
  */
  public void add(Operand primal, BooleanEquation p, Operand o) {
  
    
    if(primal.getFullName().indexOf("_predSave")==-1) {
      //since the predicates used in the stores are our control signals in the 
      //mux tree, and since the muxes need an operand rather than an
      //equation, instructions must be created to evaluate the logic 
      BEqToList eq_list = new BEqToList(p, new BooleanEquation(true));
      _logicHandleInsts.addAll(eq_list);
      //get the result of these logic equations
      BooleanOperand mux_op = eq_list.getResult();

      //get or create the ArrayList for this primal
      if(!this.containsKey(primal))
	this.put(primal, new ArrayList());
      ArrayList elementList = (ArrayList)this.get(primal);
      
      //create a new Element for this output, saving the mux control
      //operand
      Element newElement = new Element(mux_op, o);
      
      //save this to the map used by the mux tree collapsing methods
      HashSet opToElementTmp = new HashSet();
      if(_opToElementMap.containsKey(o))
        opToElementTmp = (HashSet)_opToElementMap.get(o);
      //_opToElementMap.put(o, newElement);
      _opToElementMap.put(o, opToElementTmp);
      opToElementTmp.add(newElement);
      //save the element to ArrayList for this operand:
      elementList.add(0, newElement); //note, a0 is actually the last iteration and a8
                                      //is the earliest going into the epilog
    }
  }
  
  /**
  this hashMap is used to create modPrim specific block operands
  key = modprim operand
  value = block operand for this modPrim
  */
  private HashMap _modPrimToBlockMap = new HashMap();
  /**
  This set saves all the newly created modPrim load instructions
  */
  private HashSet _modPrimLoads = new HashSet();
  /**
  This method returns a block operand specially created for each modPrim
  operand.  It saves those it creates into _modPrimToBlockMap and for future
  references to this modPrim, uses the same block operand.  It also creates the
  a new load instruction to replace all the load instructions used by every
  iteration in the epilog.
  */
  public Operand getModPrimBlock(PrimalOperand op) {
    //if a block operand already exists return it:
    if(_modPrimToBlockMap.containsKey(op))
      return (Operand)_modPrimToBlockMap.get(op);
      
    //else make a new one and save it
    Operand newOp = Operand.nextBlock(op.getFullName());
    _modPrimToBlockMap.put(op, newOp);
    
    //create a new load for this modPrim operand
    BooleanEquation trueBoolEq = new BooleanEquation(true);
    Instruction loadInst = Load.create(Operators.LOAD, 
                                       (Type)_modPrimToType.get(op),
    				       newOp, op, trueBoolEq);
    _modPrimLoads.add(loadInst);
    
    //return the new block Operand
    return newOp;
  }
    
  /**
  This is the main method from this class, and it creates the mux tree and its
  control logic.  The general algorithm used is described at the top of this
  class, so I will mostly put comments inside the method rather than repeat a
  lot here.  
  */
  public ArrayList genMuxTable() {
  
    ArrayList instList = new ArrayList();
    //foreach primal we want to store to:
    for (Iterator itin = ((Set)this.keySet()).iterator(); 
       itin.hasNext(); ) {
      Operand primal = (Operand)itin.next();
      //get the list of inputs to this tree:
      ArrayList elementList = (ArrayList)this.get(primal);

      Operand muxOut = null;
      BooleanEquation trueBoolEq = new BooleanEquation(true);
      Type type = primal.getType();
      
      //if the list has only one element, we only need to store it and no tree
      //is necessary
      if(elementList.size()==1)
        muxOut = ((Element)elementList.get(0)).op;
      else { //but otherwise let's plant ourselves a mux tree and see what we can grow
	while(elementList.size()>1) { //continue until there is only 1 element
	                              //in the list, meaning we are at the top
				      //of the tree (hmmm, a tree that gets
				      //wider as you go towards the base--must
				      //be a Christmas tree)
          //this ArrayList will be used to store the next layer in the mux tree:
	  ArrayList elementListTmpCopy = new ArrayList();
	  
	  //we want to go through the list taking off pairs of inputs and 
	  //creating muxes for them; i is used to count off the two inputs
	  int i = 0;
	  while(i < elementList.size()) {
	    //get the first element in this pair:
            Element element0 = (Element)elementList.get(i);
	    i++;
	    if(i < elementList.size()) { //if there are an odd number of 
	                                 //elements in this list, the last one
					 //will have to be passed directly to 
					 //the next level of the tree,
					 //but in the case that we can get a 
					 //pair of elements, proceed with mux
					 //creation
              //get the second element
	      Element element1 = (Element)elementList.get(i);
	      //increment i to point at the first element of the next pair
	      i++;
              
	      //make new block and boolean operands to hold the output from this
	      //mux and or:
	      muxOut = Operand.nextBlock("muxOut");
              BooleanOperand orOut = Operand.nextBoolean("orOut");
              
              //create a new element to hold it and add it to the next level's
	      //list:
	      Element newElement = new Element(orOut, muxOut);
              elementListTmpCopy.add(newElement);
	      
	      //can we collapse the tree here?  If both inputs read from the
	      //same modprim, then yes.  Save the fact that this mux is actually
	      //not a mux, but instead just a use of this modPrim:
	      if((element0.modPrim != null) &&
	         (element1.modPrim != null) &&
		 (element0.modPrim == element1.modPrim)) {
	        newElement.modPrim = element0.modPrim;
	      } 
	      else { //otherwise we can create a new mux:
		
		//get the inputs:
		Operand muxIn0 = element0.op;
		Operand muxIn1 = element1.op;
		//if either input is a use of a modPrim, use that modPrim's 
		//block operand instead of the original input:
		if(element0.modPrim != null) {
	          muxIn0 = getModPrimBlock(element0.modPrim);
		}
		if(element1.modPrim != null) {
	          muxIn1 = getModPrimBlock(element1.modPrim);
		}
		
		//create the mux!
		Instruction mux = Select.create(Operators.SELECT, type, muxOut, 
        					element0.pred, muxIn0, 
        					muxIn1, trueBoolEq);
        	//save the new mux instruction:
		instList.add(mux);
	      }
	      
	      //create an or instruction of the two predicates from both inputs
              Type orType = Type.Bool;
	      Instruction or = Binary.create(Operators.OR, orType, orOut, 
                			     element0.pred, element1.pred, 
	        			     trueBoolEq);
              //save the new or instruction:
	      instList.add(or);
            }
	    else { //if the last element cannot be paired, just copy it up to
	           //the next level of the tree:
	      elementListTmpCopy.add(element0);
	    }
	  }
	  //over write the original list, with our newly created list for the 
	  //next level of the tree:
	  elementList = new ArrayList(elementListTmpCopy);
	}
      }
      //if the entire tree just uses a modPrim, then we just need to store
      //our modPrim block operand
      if(((Element)elementList.get(0)).modPrim != null) {
        muxOut = getModPrimBlock(((Element)elementList.get(0)).modPrim);
      }

      //create a load instruction to get the kernal store predicate:
      PrimalOperand input = (PrimalOperand)_primToPred.get(primal);
      BooleanOperand output = Operand.newBoolean(primal.getFullName() + 
                                                 "_kernalPred");
      Instruction loadInst = Load.create(Operators.LOAD, Type.Bool,
          				 output, input, trueBoolEq);
      instList.add(loadInst);
      
      //make a new BooleanEquation that is the control for the final mux AND the
      //inverse of the kernal store predicate:
      BooleanEquation newPred = new BooleanEquation(((Element)elementList.get(0)).pred);
      BooleanEquation newerThanNewPred = new BooleanEquation((Bool)output);
      BooleanEquation newestPred = new BooleanEquation(newPred.and(newerThanNewPred.not()));
      BooleanEquation oldPred = (BooleanEquation)_primToOldPred.get(primal);
      
      BooleanEquation storePred = newestPred.and(oldPred);
      
      //we need one operand predicates, so create logic instructions to evaluate
      //this predicate:
      //BEqToList eq_list = new BEqToList(newestPred, new BooleanEquation(true));
      //instList.addAll(eq_list);
      
      //create a predicate from the output from these logic equations:
      //BooleanOperand predOp = eq_list.getResult();
      //BooleanEquation storePred = new BooleanEquation((Bool)predOp);
      
      //create the store the output from the mux tree (or
      //the modPrim block operand) to the primal using this predicate:
      Instruction storeInst = Store.create(Operators.STORE, type,
  					   primal, muxOut, storePred);
      //save the new instruction:
      instList.add(storeInst);
    
    }
    //save the MuxTableGenerator created modPrim loads and the logic
    //instructions used to evaluate the original epilog store predicates (see
    //comments at top on _logicHandleInsts and in add):
    instList.addAll(_modPrimLoads);
    instList.addAll(_logicHandleInsts);
    
    //return all the new instructions so they can be added to the epilog:
    return instList;
  }
}

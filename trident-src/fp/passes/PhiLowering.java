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


package fp.passes;

import java.util.*;

import fp.flowgraph.*;
import fp.graph.Node;
import fp.graph.Edge;


public class PhiLowering extends Pass implements GraphPass, Operators {
  
  /*
  
  */
   
  private LinkedList _pass_schedule;
  
  public PhiLowering(PassManager pm) {
    super(pm);
    _pass_schedule = new LinkedList();
  }
  
  public boolean optimize(BlockGraph graph) {
    int verbose = pm.getVerbose();
    int primCnt = 0;
    for (Iterator vIt = graph.getAllNodes().iterator(); vIt.hasNext();){
      BlockNode node = (BlockNode) vIt.next();
      // find phi statements
      // if phi examine each input pair
      // for P <- Tmp is store
      // for tmp <- P is load
      // for tmp3 <- tmp5 replace and eliminate tmp5 
      // for P <- P ???? 
       
      ArrayList instList = (ArrayList)node.getInstructions().clone();
      for(Iterator instIt = instList.listIterator(); instIt.hasNext(); ) {
        Instruction instruction = (Instruction)instIt.next();
        
        if (Phi.conforms(instruction)) {
          node.removeInstruction(instruction);
          //System.out.println("Removing ... "+instruction);
          Type type = instruction.type();
           
          Operand result = Phi.getResult(instruction);
	  PrimalOperand resultStore = Operand.newPrimal("phiPrimTmp"+primCnt);
	  primCnt++;
	  int op_count = instruction.getNumberOfOperands();
          int def_count = instruction.getNumberOfDefs();
          for(int i = 0; i<(op_count-def_count)/2; i++) {
            LabelOperand label = Phi.getValLabel(instruction, i);
            Operand value = Phi.getValOperand(instruction, i);
            if(value == result) continue; //I found an example where x = phi(x,...) 
            BlockNode destination = findNode(node, label);
            /*boolean destissource = false;
            if(destination == node)
              destissource = true;
            addOperation(result, value, type, destination, graph, destissource);
            */
            if(result.isPrimal())
	      addOperation(result, value, type, destination, graph);
	    else {
              Instruction store = Store.create(STORE, type, resultStore, 
	  				       value);
              destination.addInstruction(store, destination.getInstructions().size()-1);
	    }
          }
	  if(!result.isPrimal()) {
            Instruction load = Load.create(LOAD, type, result, resultStore);
            node.addInstruction(load, instList.indexOf(instruction));
	  }
        } else break;
          // Since all phis must be first we can quit once we don't find
        // them.
      }
    }      
    return true;
  }
  
  BlockNode findNode(BlockNode node, LabelOperand operand) {
    for(Iterator inIter = node.getInEdges().iterator(); inIter.hasNext(); ) {
      Edge edge = (Edge)inIter.next();
      BlockNode source = (BlockNode)edge.getSource(); 
      LabelOperand sourceLabel = source.getLabel();
      if (sourceLabel == operand) 
        return source;
    }
    
    for(Iterator outIter = node.getOutEdges().iterator();outIter.hasNext(); ) {
      Edge edge = (Edge)outIter.next();
      BlockNode sink = (BlockNode)edge.getSink(); 
      LabelOperand sink_label = sink.getLabel();
      if (sink_label == operand) 
        return sink;
    }
    
    return (BlockNode)null;
  }
   
  
  void addOperation(Operand result, Operand source, Type type, 
                    BlockNode node, BlockGraph graph) {
    // BlockNode node, BlockGraph graph, boolean destIsSource) {
      
      if (result.isPrimal()) {
        if (source.isPrimal()) {
          // P2 <- P1
          // tmp <- P1 load
          // P2 <- tmp store
          AddrOperand temp = Operand.nextAddr("phi.tmp");
          Instruction load = Load.create(LOAD, type, temp, source);
          node.addInstruction(load);
          System.out.println("Adding instruction "+load);
          Instruction store = Store.create(STORE, type, result, temp);
          node.addInstruction(store);
          System.out.println("Adding instruction "+store);          
        } else {
          // P <- tmp store
          //System.out.println("primal to tmp copy ....");
          //System.out.println(" result "+result+" "+result.isPrimal());
          //System.out.println(" source "+source+" "+source.isPrimal());	
          Instruction instruction = Store.create(STORE, type, 
                                                (PrimalOperand)result, 
                                                 source);
          System.out.println("Adding instruction "+instruction);
          node.addInstruction(instruction);
        }
      }/* else {
        if (source.isPrimal()) {
          // tmp <- P load
          //System.out.println("tmp to primal copy ....");
          //System.out.println(" result "+result+" "+result.isPrimal());
          //System.out.println(" source "+source+" "+source.isPrimal());	
          Instruction instruction = Load.create(LOAD, type, result, 
                                               (PrimalOperand)source);
          System.out.println("Adding instruction "+instruction);
          phiNode.addInstruction(instruction, 0);
        } else {
          // tmp3 <- tmp5 or const replace 
	  //doing a replace it too difficult.  For example if we had a phi in a loop that said:
	  //x = phi entry 1 loop y
	  //and later in the loop block had 
	  //y = x +1
	  //if we did replacements and moved the x = 1 to entry we'd end up with 
	  //y = y +1 or
	  //x = x + 1
	  //in the loop body, depending on what we decided to replace.  It's easier
	  //just to let primalpromotion fix this, because otherwise we'd have to do a lot
	  //of analyzing of what instructions are using the operand before replacing and all of 
	  //this is already done by primalpromotion
          //Instruction instruction = Store.create(STORE, type, result, source);
          //System.out.println("Adding instruction "+instruction);
          //node.addInstruction(instruction);
	  
          PrimalOperand temp = Operand.nextPrimal("phi.prim.tmp");
          Instruction store = Store.create(STORE, type, temp, source);
          node.addInstruction(store);
          System.out.println("Adding instruction "+store);          
          Instruction load = Load.create(LOAD, type, result, temp);
          phiNode.addInstruction(load, 0);
          System.out.println("Adding instruction "+load);
	  
          /*ArrayList instList = (ArrayList)node.getInstructions().clone();
	  for(Iterator instIt = instList.listIterator(); 
	      instIt.hasNext(); ) {
            Instruction i = (Instruction)instIt.next();
	    Operand newResult = result.getNextNonPrime();
	    i.replaceOperand(result, newResult);
	    if(source.isConstant()) {
              Instruction store = Store.create(STORE, type, result, source);
              node.addInstruction(store);
              System.out.println("Adding instruction "+store);  	
	    }
	    else {
	      for(Iterator opIt = i.getDefs().iterator(); 
		  opIt.hasNext(); ) {
        	Operand def = (Operand)opIt.next();
		if(def == source) {
		  //source = def.getNextNonPrime();
		  System.out.println("replacing "+ result + " with " + source +
	  	    		     " in inst " + i);
		  i.replaceOperand(source, result);
		  System.out.println("resulting inst " + i);
		}
	      }
	    }
	  }  
	  
          //System.exit(-1); // for now.
        }
      }*/
    }
    
    
    
    
    public void add(Pass pass) {
      // check dependencies 
      // see if CFG is consistent 
      // and if this requires it to be.
      // add appropriate passes.
      _pass_schedule.add(pass);
    }
    
    
    public String name() { 
      return "PhiLowering";
    }
    
    
    
  }
  

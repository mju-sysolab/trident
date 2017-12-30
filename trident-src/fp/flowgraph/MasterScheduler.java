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
import java.lang.Math;

import fp.hardware.*;
import fp.graph.*;
import fp.passes.*;
import fp.*;


/** This class was written to do the top level selection and setup
 * for all schedulers.  I wrote it, because, now schedules may be 
 * started from several places, and I didn't want to have to use 
 * copy pasted code, since that would mean any changes would have
 * to be done in all places.  (Now a schedule can be performed in 
 * pass manager (and there are four different classes to start each
 * of the schedulers), the analyze hardware class, who uses this to 
 * do a preliminary scheule, and now also the modulo scheduler, who
 * needs to schedule the epilog and prolog lists.
 * 
 * @author Kris Peterson
 */
public class MasterScheduler extends InstructionList
{
  
  //private BlockNode _node;
  private OperationSelection _opSel = null;
  private ChipDef _chipdef = null;
  private Schedule _schedSave = null;
  private BooleanEquation _oldEdgePred = null;
  private ArrayList _loopCopyList = null;
  private boolean _modScheduled = false;
  
  public MasterScheduler(BlockNode node, OperationSelection opSel, ChipDef chipInfo)  {
    super(node, chipInfo);
    _opSel = opSel;
    _chipdef = chipInfo;
  }
  
  public void schedule(BlockGraph graph) {
  
    
    DependenceFlowGraph dfg = new DependenceFlowGraph(graph.getName() + "_" + 
                                                      _node.getLabel().getFullName());
    resetdfg();
    dfg.generateGraph(_node, this);
    String file_name = dfg.getName();
    
    // we need to get just the file basename and path from it
    int index = file_name.lastIndexOf(System.getProperty("file.separator"));
    String basename = file_name.substring(index+1);
    String path = file_name.substring(0, index+1);
    
    // now create the changed file name
    file_name = path + basename + ".dot";
    dfg.writeDotFile(file_name);
    // this should ask pm about a verbosity level ...
    //um,.. did I add the above comment?  and if so what was I talking about?
    _node.saveDepFlowGraph(dfg);
    resetSuccsList();
    _chipdef.resetPorts();
    
    if((!_node.getIsRecursive())||!GlobalOptions.modSched) {


      if(GlobalOptions.scheduleSelect == GlobalOptions.ASAPSchedSelect) {
    	ASAPSchedule schedule = new ASAPSchedule(_node);
        
	schedule.asapSchedule(new ArrayList(getInstructions()), !GlobalOptions.ignorePreds, 
	                            _chipdef, dfg);
        _schedSave = schedule;
      }
      else if(GlobalOptions.scheduleSelect == GlobalOptions.ALAPSchedSelect) {
    	ALAPSchedule schedule = new ALAPSchedule(_node);
    	schedule.alapSchedule(new ArrayList(getInstructions()), !GlobalOptions.ignorePreds, 
	                            _chipdef, dfg);
        _schedSave = schedule;
      }
      else if(GlobalOptions.scheduleSelect == GlobalOptions.FDSchedSelect) {
        //InstructionList aSAPList = listCopy();
        //InstructionList aLAPList = listCopy();
	
        InstructionList aSAPList = this;
        InstructionList aLAPList = this;

    	FDWindows windowMap = new FDWindows();
	
    	ASAPSchedule asapSchedTmp = new ASAPSchedule(_node);
        _chipdef.resetPorts();
	boolean result = asapSchedTmp.asapSchedule(new ArrayList(aSAPList.getInstructions()), 
	                                           !GlobalOptions.ignorePreds, _chipdef, dfg);
        ArrayList asapInstList = new ArrayList(aSAPList.getInstructions());
        for (Iterator it = getInstructions().iterator(); 
    	    it.hasNext(); ) {
          Instruction instr = (Instruction)it.next();
          windowMap.getASAPTimes(instr, asapInstList);

        }  //end foreach
	if(!result) {
          throw new ScheduleException("Error: Failed to create ASAP schedule " +
	                              "for Force-Directed Schedule");
        }
        _schedSave = asapSchedTmp;
	unSchedule(graph);
       //System.out.println("ASAP schedule ");
	//aSAPList.scheduleToExecTime();
       // aSAPList.printSchedule();
       //System.out.println(" ");
       //System.out.println(" ");
	_chipdef.completeInitialize(); 
	ArrayList alaplist = new ArrayList(aLAPList.getInstructions());
        _chipdef.resetPorts();
        ALAPSchedule alapSchedTmp = new ALAPSchedule(_node);
        result = alapSchedTmp.alapSchedule(alaplist, !GlobalOptions.ignorePreds, _chipdef, dfg);
        ArrayList alapInstList = new ArrayList(aSAPList.getInstructions());
        for (Iterator it = getInstructions().iterator(); 
    	    it.hasNext(); ) {
          Instruction instr = (Instruction)it.next();
          windowMap.getALAPTimes(instr, alapInstList);

        }  //end foreach
        if(!result) {
          throw new ScheduleException("Error: Failed to create ALAP schedule " +
	                              "for Force-Directed Schedule");
        }
        _schedSave = alapSchedTmp;
	unSchedule(graph);
        _chipdef.completeInitialize(); 
        
       //System.out.println("ALAP schedule ");
	//aLAPList.scheduleToExecTime();
	//aLAPList.printSchedule();
       //System.out.println(" ");
       //System.out.println(" ");
	int tryCnt = 0;
        FDSchedule schedule = new FDSchedule(dfg, _node);
        _schedSave = schedule;
        //load initial instruction window sizes
        for (Iterator it = getInstructions().iterator(); 
    	    it.hasNext(); ) {
          Instruction instr = (Instruction)it.next();
          windowMap.setMinMax(instr);

        }  //end foreach
        boolean succeeded = true;
        _chipdef.resetPorts();
        do {
    	  FDWindows windowMapTmp = (FDWindows)windowMap.clone();
          tryCnt++;
    	  System.out.println("trying FD " + tryCnt);
          _chipdef.saveNode(_node);
	  ArrayList list = new ArrayList(getInstructions());
	  Collections.shuffle(list);
          _chipdef.resetPorts();
	  unSchedule(graph);
          succeeded = schedule.fD_Schedule(list, windowMapTmp, 
	                                   !GlobalOptions.ignorePreds, _chipdef);
          schedule.seed((int)System.currentTimeMillis());
	  if(!(succeeded)) {
	    _chipdef.initializeForOneNode(_node);
	    _chipdef.resetPorts();
	  }
        }while(tryCnt<GlobalOptions.maxAttemptsOnFDSched && !succeeded);
        if(tryCnt>=GlobalOptions.maxAttemptsOnFDSched) {
          throw new ScheduleException("Error: Failed to create a Force-Direct" +
	                              "ed Schedule");
        }
      }
      else
    	throw new ScheduleException("invalid schedule selection. (1=ASAP, 2=ALAP, 3=FD)");
    }
    else { //modulo schedule
    
      ModSched modScheduler = new ModSched(_opSel, _node);
      _modScheduled = true;
      _schedSave = modScheduler;
      for (Iterator it = _node.getOutEdges().iterator(); it.hasNext();) {
        BlockEdge outEdge = (BlockEdge)it.next();
        if(outEdge.isBackwardEdge()) {
          _oldEdgePred = (BooleanEquation)outEdge.getPredicate().clone();
        }
      }
      _loopCopyList = new ArrayList(listCopy().getInstructions());
      
      _chipdef.saveNode(_node);
      initOpUseLists(_node, _chipdef);
      _chipdef.resetPorts();
      modScheduler.moduloScheduler(_node, _chipdef, graph);
      modScheduler = null;
    
    }
    
    sort(_node.getInstructions());
  }
  
  public void unSchedule(BlockGraph graph) {
    if(_modScheduled) {
      _node.removeAllInstructions();
      _node.addInstructions(_loopCopyList);
      ArrayList nodesToDelete = new ArrayList();
      for (Iterator vIt = graph.getAllNodes().iterator(); 
        	vIt.hasNext();) {
	BlockNode node = (BlockNode) vIt.next();
	if(node.getName().indexOf("prolog_") >= 0) {

	  ArrayList out_edges = new ArrayList();
	  out_edges.addAll(node.getOutEdges());
	  BlockEdge outEdge = (BlockEdge)out_edges.get(0);
	  BlockNode bNode = (BlockNode)outEdge.getSink();

	  Set in_edges = new HashSet();
	  in_edges.addAll(node.getInEdges());
	  for (Iterator it = in_edges.iterator(); it.hasNext();) {
            BlockEdge inEdge = (BlockEdge)it.next();
            inEdge.setSink(bNode);
	  }
	  nodesToDelete.add(node);
	}
	if(node.getName().indexOf("epilog_") >= 0) {

	  ArrayList in_edges = new ArrayList();
	  in_edges.addAll(node.getInEdges());
	  BlockEdge inEdge = (BlockEdge)in_edges.get(0);
	  BlockNode bNode = (BlockNode)inEdge.getSource();
          BooleanEquation edgePredicate = inEdge.getPredicate();

	  Set out_edges = new HashSet();
	  out_edges.addAll(node.getOutEdges());
	  for (Iterator it = out_edges.iterator(); it.hasNext();) {
            BlockEdge outEdge = (BlockEdge)it.next();
            outEdge.setSource(bNode);
            outEdge.setPredicate(edgePredicate);
	  }
	  nodesToDelete.add(node);
	}
      }
      for (Iterator vIt = nodesToDelete.iterator(); 
        	vIt.hasNext();) {
	BlockNode delNode = (BlockNode) vIt.next();
	graph.removeNode(delNode);
      }
      for (Iterator it = _node.getOutEdges().iterator(); it.hasNext();) {
        BlockEdge outEdge = (BlockEdge)it.next();
        if(outEdge.isBackwardEdge()) {
	  outEdge.setPredicate(_oldEdgePred);
        }
        else {
	  BooleanEquation outEdgePred = (BooleanEquation)_oldEdgePred.clone();
	  outEdge.setPredicate(outEdgePred.not());
        }
      }
    }
    _schedSave.unSchedule();
  }
  
  public void initOpUseLists(BlockNode node, ChipDef chipDef) {
    for (Iterator it = node.getInstructions().iterator(); it.hasNext(); ) {
      Instruction instr = (Instruction)it.next();
      chipDef.initOpUseLists(instr.operator());
    }
  }  

}

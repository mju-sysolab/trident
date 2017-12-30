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


package fp;

import fp.flowgraph.BlockGraph;
import fp.flowgraph.HyperBlockList;
import fp.passes.*;

import fp.hwdesc.Config;
import fp.hwdesc.Library;
import fp.hwdesc.ParseConfig;
import fp.hwdesc.Platform;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.*;

import java.lang.reflect.*;


/**
 * Compile is the Top-Level class that parses the command-line and drives
 * the high level classes in the floating-point compiler.
 * 
 * @author Justin L. Tripp
 * @version $Id: Compile.java 2413 2006-03-14 15:33:24Z jtripp $
 */


public class Compile {
  Config _config;
  
  public Compile(String args[]) {
    parseInitialConfig();
    // parse command-line
    parseCmdLine(args);

    if (GlobalOptions.programFileName == null) {
      System.err.println("No input file name");
      System.exit(-1);
    }

    if (GlobalOptions.functionName == null) {
      System.err.println("No function name");
      System.exit(-1);
    }

    //Hey now, you guys were complaining about my comments but look at this:
    // execute stuff.
    BlockGraph bg = new BlockGraph(GlobalOptions.programFileName, 
                                   GlobalOptions.functionName);

    // this could generate some new command-line options or be influenced ...
    PassManager pm = new PassManager(false);

    //System.out.println("Adding passes");
    pm.setVerbose(PassManager.VERBOSE_L2);
    // don't put anything before NOPGraph ... it needs to be first.
    pm.add(new NOPGraph(pm));

    if (GlobalOptions.hardwareFileName == null) {
      System.err.println("No hardware description file name");
      System.exit(-1);
    }

    // this is actually ran when it is added, so the order is not relative
    // to the other passes.
    pm.add(new LoadHardWareInfoPass(pm, GlobalOptions.hardwareFileName));

    //pm.add(new PrintVariables(pm));
    //pm.add(new PrintDataFlow(pm, _input_file + "_dataflow_"));
    //pm.add(new PrintLoopGraph(pm, "loop_"));

    pm.add(new FixLoadPtrInsts(pm));
    pm.add(new SetNodeOrder(pm));

    pm.add(new CorrectIndVar(pm));

    //pm.add(new VerifyOperands(pm));
    //pm.add(new VerifyBlockGraph(pm));
    pm.add(new PhiLowering(pm));

    // if this is before phi lowering it causes problems.
    pm.add(new EnsureSingleDefs(pm));

    //if you wish to schedule a loop with something other than
    //modulo scheduling, you should pass in true to this as the
    //second argument.  This is because part of the primal
    //promotion job needs to be done by modulo scheduling
    //if it is to be used. (true==no mod scheduling on loops)
    //pm.add(new ConvInterBlockNonPrimToPrimal(pm, true));
    HyperBlockList hyperBlocks = new HyperBlockList();
    pm.add(new CalcHyperBlocks(pm, hyperBlocks));
    pm.add(new ConvInterBlockNonPrimToPrimal(pm, hyperBlocks));
    //pm.add(new Stop(pm));
    pm.add(new CalcDefUseHash(pm));

    pm.add(new SwitchIfConvert(pm));
    pm.add(new SetNodeOrder(pm));
    pm.add(new AddPredicates(pm));
    pm.add(new ControlRemoval(pm));
    pm.add(new MergeBlocks(pm, hyperBlocks)); //the two merges have been merged
                                              //because CalcHyperBlocks calculates
					      //both kinds
    //pm.add(new MergeParallelBlocks(pm));
    //pm.add(new MergeSerialBlocks(pm));
    pm.add(new SetNodeOrder(pm));
    pm.add(new RemoveGlobalPredicates(pm));

    pm.add(new CheckBlockNames(pm));  // Should be after philowering,merging.
    pm.add(new GlobalDeadCode(pm)); 

    pm.add(new CSERemoval(pm));
    

    pm.add(new CallReplace(pm));
    pm.add(new FixFabsInsts(pm));
    pm.add(new FixFSubInstructions(pm));

    pm.add(new CalcDefUseHash(pm));
    pm.add(new ConstantSelect(pm));
    pm.add(new ConvertConstantMultToShiftTree(pm));
    pm.add(new ConstantMath(pm));

    pm.add(new CheckDefUseHash(pm));
    pm.add(new RemoveAliases(pm));
    
    pm.add(new RemoveRem(pm));
    
    //pm.add(new Stop(pm));

    OperationSelection opSel = null;

    opSel = getOperationSelection(pm);
    pm.add(opSel);

    //creating the dependencflowgraph here is no longer necessary
    //as it is done before scheduling as needed:
    //pm.add(new CreateDependenceFlow(pm, GlobalOptions.programFileName));
    
    //hopefully the analyze hardware options will later be read from
    //the options object, but
    //the first parameter to this tells the hardware analyzer which
    //scheduler to use for its preliminary analysis
    //(1==ASAP, 2==ALAP, 3==FD)
    //The second tells it whether to use modulo scheduling on the loops.
    //The third tells it whether to attempt to conserve hardware space
    //by only using the minimum amount of hardware necessary to do all
    //operations.
    pm.add(new CalcDefUseHash(pm));
    pm.add(new AnalyzeHrdWrConstraintsPass(pm, opSel));
    //pm.add(new AnalyzeHrdWrConstraintsPass(pm, 3));
    //pm.add(new AnalyzeHrdWrConstraintsPass(pm, 1, true, true));

    pm.add(new AllocateArraysPass(pm));
    pm.add(new ConvertGepInst(pm, opSel));
    pm.add(new CalcDefUseHash(pm));
    //NOTE:  uncomment the following pass when ConvertGepInst gets uncommented
    pm.add(new ConstantMath(pm));
    // these are to clean up the mess...
    pm.add(new GlobalDeadCode(pm)); 
    pm.add(new CSERemoval(pm));
    pm.add(new CalcDefUseHash(pm));

    pm.add(new AnalyzeLogicSpaceConstraintsPass(pm));

    pm.add(new AddTypeToPrimals(pm));
    pm.add(new CalcIIPass(pm));

    //unfortunately since analyzehardwareconstraints does a
    //preliminary schedule if it does modulo scheduling, the
    //dependence flow graph is messed up and must be recreated
    
    //pm.add(new CreateDependenceFlow(pm, GlobalOptions.programFileName));

    //I only do one scheduling routine now, because if you want to try
    //one of these and modulo scheduling, you cannot run another,
    //because modulo scheduling messes up the loop blocks beyond
    //repair and the unschedule pass cannot return them to a state
    //that another scheduler could use 
    //
    // pm.add(new ASAPSchedulePass(pm,false, true, false));

    /* 
       
    Both one of the normal scheduling routines and Modulo Scheduling
    should be selected. They will know which do do on which blocks
    after the dependence flow graph creation.  Modulo scheduling will
    be performed on blocks with data recursive loops and the other
    (user-selected) scheduling routine will be performed on the other
    blocks. (UnSchedulePass is used, when the user wishes to compare
    the results of different schedules; if it is not performed between
    calls to different schedulers (not including Modulo) the next
    scheduling algorithm called will think all instructions have
    already been scheduled and will do nothing.)

    */

    //if you want to ge a schedule a loop body with a scheduling
    //algorithm other than modulo scheduling then you need to call the
    //scheduling pass like below (and comment out modulo): pm.add(new
    //FDSchedulePass(pm, false, true)); the first "false" tells the
    //scheduler to ignore predicates when scheduling operations,
    //except for writes to primals.  This is done, because, it doesn't
    //matter if operations other than writes to primals are performed
    //all the time.  The "true" tells the scheduler to perform
    //scheduling even on loops with inter-iteration data dependencies.
    //If these two values are not, set the default is false on both
    //(will ignore predicates and will not schedule).
    
    //I now use one scheduler pass, because multiple classes want to schedule
    //the design, this guy, AnalyzeHardwareConstraints creates a preliminary 
    //schedule, and the modulo scheduler schedules its prolog and epilog.  
    //Adding one general schedule Class to set up all the schedulers makes
    //things much easier for me
    
    pm.add(new SchedulerPass(pm, opSel));
    //pm.add(new Stop(pm));
    
    //pm.add(new FDSchedulePass(pm, false, true, false, 50));
    /*if(GlobalOptions.scheduleSelect == GlobalOptions.ASAPSchedSelect) {
      pm.add(new ASAPSchedulePass(pm));
    }
    else if(GlobalOptions.scheduleSelect == GlobalOptions.ALAPSchedSelect){
      pm.add(new ALAPSchedulePass(pm));
    }
    else if(GlobalOptions.scheduleSelect == GlobalOptions.FDSchedSelect){
      pm.add(new FDSchedulePass(pm));
    }
    if(!GlobalOptions.noModSched) 
      pm.add(new ModuloSchedulePass(pm, opSel));*/

    // why isn't the cycle count set by the scheduler pass?
    pm.add(new GenSchedulerStats(pm));
    // this needs cycle count...

    //pm.add(new PrintDataFlow(pm, _input_file + "_dataflow_sched_", true));
    
    // this should be command-line controlled.
    pm.add(new PrintGraph(pm));
    pm.add(new CheckPrimalWrites(pm));

    // So, at the end of the pass manager we either create VHDL as the final
    // pass or we pass the block graph onto something that will do that.
    // not sure which to do, although it probably does not matter...

    if (GlobalOptions.target == null) {
      System.err.println("No target given");
      System.exit(-1);
    }
    
    // this should use the config object ...
    if(GlobalOptions.buildTop)
      pm.add(new GenerateCircuitPass(pm, GlobalOptions.target, "xd1"));
    else
      pm.add(new GenerateCircuitPass(pm, GlobalOptions.target));

    //pm.addPrintGraph();
    //System.out.println("Running passes");
    pm.run(bg);

  }

  private OperationSelection getOperationSelection(PassManager pm) {
    OperationSelection opSel = null;
    
    if (GlobalOptions.library != null) {
      String class_name = GlobalOptions.library.getClassName();

      Class opSelDefinition;

      Class[] argsClass = new Class[] { pm.getClass() };
      Object[] args = new Object[] { pm };
      Constructor argsConstructor;

      try {
        opSelDefinition = Class.forName(class_name);
        argsConstructor = 
            opSelDefinition.getConstructor(argsClass);
        opSel = (OperationSelection) createObject(argsConstructor, args);
      } catch (ClassNotFoundException e) {
          System.out.println(e);
      } catch (NoSuchMethodException e) {
          System.out.println(e);
      }

    } else {
      // this is fall back.
      if ("quixilica".equals(GlobalOptions.libSelect)) {
	opSel = new QXOperationSelection(pm);
      } else if ("aa_fplib".equals(GlobalOptions.libSelect)) {
	opSel = new AAOperationSelection(pm);
      }
    }

    if (opSel == null) {
      System.err.println("Unknown Library Selection "+GlobalOptions.libSelect);
      System.exit(-1);
    } 
    return opSel;
  }

  public Object createObject(Constructor constructor, Object[] arguments) {
    //System.out.println ("Constructor: " + constructor.toString());
    Object object = null;

    try {
      object = constructor.newInstance(arguments);
      //System.out.println ("Object: " + object.toString());
      return object;
    } catch (InstantiationException e) {
      System.out.println(e);
    } catch (IllegalAccessException e) {
      System.out.println(e);
    } catch (IllegalArgumentException e) {
      System.out.println(e);
    } catch (InvocationTargetException e) {
      System.out.println(e);
    }
    return object;
  }

  final private void parseInitialConfig() {
    //System.out.println("fp.Compile:ParseInitialConfig()");
    ParseConfig parse = new ParseConfig("compiler.dat");
    _config = parse.getConfig();

    if (_config.getDefaultConfig() != null) { 
      GlobalOptions.ft.setHWDescPath(_config.getDefaultConfig());
    }

    if (_config.getDefaultPlatform() != null) {
      Platform p = _config.getDefaultPlatform();
      GlobalOptions.platform = p;
      GlobalOptions.hardwareFileName = p.getHWFileName();
      GlobalOptions.hardwareName = p.getName();
    }

    if (_config.getDefaultTarget() != null) {
      GlobalOptions.target = _config.getDefaultTarget();
    }

    if (_config.getDefaultLibrary() != null) {
      Library l = _config.getDefaultLibrary();
      GlobalOptions.library = l;
      GlobalOptions.libSelect = l.getName();
      //System.out.println("Set libSelect "+l.getName());
    }
  }
    
  final private void printHelpMessage(String s) {
    if (s != null) {
      System.err.println("Compile: "+s);
      System.err.println();
    }
    System.err.println("Compile [Options] input_file function_name\n");
    System.err.println("Short Options:");
    System.err.print("-a filename ");
    System.err.println("\t: Filename of architecture (hardware) description");
    System.err.print("-b          ");
    System.err.println("\t: Generate Testbench -- experimental");
    System.err.print("-h          ");      
    System.err.println("\t: Get help -- good luck!");
    System.err.print("-l libname  ");      
    System.err.print("\t: Specify library, supporting ");
    HashMap libs = _config.getLibraries();
	
    for (Iterator iter=libs.values().iterator(); iter.hasNext(); ) {
      Library library = (Library)iter.next();
      System.err.print(library.getName()+", ");
    }
    System.err.println();

    System.err.print("-t format   ");
    System.err.println("\t: Target output.  format can be set to \"dot\" or \"vhdl\"");
    System.err.println();
    System.err.println("Long Options:");
    System.err.println("--sched=sched_list");
    System.err.println("	sched_list is comma-separated list which can include");
    System.err.println("	asap, alap, or fd, and optionally nomod or mod\n");
    System.err.println("	It is case insensitive.  You must choose either asap, alap, or force");
    System.err.println("	directed.  It is default to run modulo scheduling on all loop blocks,");
    System.err.println("	but to turn that off, you can add \"nomod\" within the string.  For");
    System.err.println("	example:\n");
    System.err.println("	--sched=fd,nomod\n");
    System.err.println("	You can also, specify \"mod\", but that is the default.\n");
    System.err.println("--sched.options=option_list");
    System.err.println("	option_list is a comma-separated list which can include");
    System.err.println("	considerpreds (tell schedule not to ignore predicates)");
    System.err.println("	dontpack (don't pack multiple less than 1-clock tick instructions within a single cycle)");
    System.err.println("	conserve_area (conserve area by only using as many logic units as ");
    System.err.println("		necessary so as to not slow down execution of the design)");
    System.err.println("	fd_maxtrycnt (tell the compiler how many times to attempt force-directed ");
    System.err.println("		scheduling before giving up)");
    System.err.println("	ms_maxtrycnt (how many times to attempt modulo scheduling)");
    System.err.println("	cyclelength (set the length of a clock tick (the default is 1.0))\n");
   
    System.err.println("	The options can be set either by using the long opt, \"sched.options\" to set several at");
    System.err.println("	once (like this:\n");
    System.err.println("	--sched.options=considerpreds,dontpack,conserve_area\n");
    System.err.println("	or they can be set individually by saying:\n");
    System.err.println("	--sched.options.considerpreds");
    System.err.println("memory allocation options:");
    System.err.println("	--painThreshhold=(time in millisececonds)");
    System.err.println("	    this time is the amount of time the user");
    System.err.println("	    is willing to use running multiple iterations");
    System.err.println("	    of prescheduling using different memory");
    System.err.println("	    allocations in a search for the ideal");
    System.err.println("	    allocation.");
    System.err.println("	--multiPreAlloc");
    System.err.println("	    This option informs the compiler to attempt");
    System.err.println("	    more than one interation of prescheduling");
    System.err.println("	    and memory allocation based on that ");
    System.err.println("	    preschedule.  This option must be used ");
    System.err.println("	    for the painThreshhold to have any meaning ");
    System.err.println("	--useSlowestMem ");
    System.err.println("	    This option tells the compiler to start by ");
    System.err.println("	    allocating all arrays to the slowest  ");
    System.err.println("	    memory before the first preschedule");
    System.err.println("	    and allocation attempt.");
    System.err.println("	--packArrays ");
    System.err.println("	    This option tells the compiler to attempt");
    System.err.println("	    to place arrays in the same memory word");
    System.err.println("	    space.  This will only be done if all");
    System.err.println("	    accesses to both memories are the same.");
  }

  final private void parseCmdLine(String args[]) {

    if (args.length == 0) {
      printHelpMessage("No input files");
      System.exit(-1);
    }

    if (args.length < 2) {
      printHelpMessage("Must have input file and function name.");
      System.exit(-1);
    }

    GlobalOptions.programFileName = args[args.length - 2];
    GlobalOptions.functionName = args[args.length - 1];

    String arg;
    LongOpt[] longopts = new LongOpt[19];
    StringBuffer sb = new StringBuffer();
    longopts[0] = new LongOpt("nomod", LongOpt.NO_ARGUMENT, 
                               null, 0);
    longopts[1] = new LongOpt("pred", LongOpt.NO_ARGUMENT, 
                               null, 1);
    longopts[2] = new LongOpt("pack", LongOpt.NO_ARGUMENT, 
                               null, 2);
    longopts[3] = new LongOpt("sched.options.fd_maxtrycnt", 
			      LongOpt.REQUIRED_ARGUMENT, null, 3);
    longopts[4] = new LongOpt("sched.options.ms_maxtrycnt", 
			      LongOpt.REQUIRED_ARGUMENT, null, 4);
    longopts[5] = new LongOpt("sched.options.cyclelength", 
			      LongOpt.REQUIRED_ARGUMENT, 
			      null, 5);
    longopts[6] = new LongOpt("ca", LongOpt.NO_ARGUMENT, 
			      null, 6);
    longopts[7] = new LongOpt("sched", LongOpt.REQUIRED_ARGUMENT, 
			      null, 7);
    longopts[8] = new LongOpt("sched.options", LongOpt.REQUIRED_ARGUMENT, 
			      null, 8);
    longopts[9] = new LongOpt("sched.options.considerpreds", 
			      LongOpt.NO_ARGUMENT, null, 9);
    longopts[10] = new LongOpt("sched.options.dontpack", LongOpt.NO_ARGUMENT, 
                               null, 10);
    longopts[11] = new LongOpt("sched.options.conserve_area", 
			       LongOpt.NO_ARGUMENT, null, 11);
    longopts[12] = new LongOpt("top", LongOpt.NO_ARGUMENT, null, 12);
    longopts[13] = new LongOpt("bench", LongOpt.NO_ARGUMENT, null, 13);

    longopts[14] = new LongOpt("bench_input", LongOpt.REQUIRED_ARGUMENT, null, 14);

    longopts[15] = new LongOpt("painThreshhold", LongOpt.REQUIRED_ARGUMENT, 
			       null, 14);
    longopts[16] = new LongOpt("multiPreAlloc", LongOpt.NO_ARGUMENT, 
			       null, 15);
    longopts[17] = new LongOpt("useSlowestMem", LongOpt.NO_ARGUMENT, 
			       null, 16);

    longopts[18] = new LongOpt("packArrays", LongOpt.NO_ARGUMENT, 
			       null, 17);

    //Getopt g = new Getopt("Trident", args, "h:t:s:npb::");
    Getopt g = new Getopt("Trident", args, "-a:ht:l:s:b;", longopts);
    //g.setOpterr(false);    
    int c;
    while ((c = g.getopt()) != -1) {
      switch(c) {

	case 'a':
	  /*
	    What do we do here?  How do I know what the hardware
	    name will be?  Do I pick it up from another switch or 
	    some other file -- must the switches be picked together??

	  */

	  GlobalOptions.hardwareFileName = g.getOptarg();

	  /*
	    // this is just a dumb way to do this ...
	  if(GlobalOptions.hardwareFileName.endsWith("xd1_hw.dat"))
	    GlobalOptions.hardwareName = GlobalOptions.XD1_HW;
	  else if(GlobalOptions.hardwareFileName.endsWith("osiris_hw.dat"))
	    GlobalOptions.hardwareName = GlobalOptions.OSI_HW;
	  else
	    GlobalOptions.hardwareName = GlobalOptions.DEF_HW;
	  */
	  break;

	case 'b':
	  if (GlobalOptions.buildTop) {
	    System.err.println("top-level insertion and testbench building are mutually exclusive.");
	    GlobalOptions.makeTestBench = false;
	  } else {
	    GlobalOptions.makeTestBench = true;
	  }
	  break;

	case 'h':
	  printHelpMessage(null);
          System.exit(-1); // need to exit, too.
	  break; // getopt() already printed an error
	  
      case 'l':
	String lib = g.getOptarg();
	HashMap libs = _config.getLibraries();
	
	boolean match = false;
	for (Iterator iter=libs.values().iterator(); iter.hasNext(); ) {
	  Library library = (Library)iter.next();
	  
	  if (library.getName().equals(lib)) {
	    match = true;
	    GlobalOptions.library = library;
	    GlobalOptions.libSelect = library.getName();
	    break;
	  } else if (library.getShortName().equals(lib)) {
	    match = true;
	    GlobalOptions.library = library;
	    GlobalOptions.libSelect = library.getName();
	    break;
	  }
	}
	
	if (!match) {
	  printHelpMessage("Unknown library "+lib);
	  System.exit(-1);
	}
	break;
      case 't':
	GlobalOptions.target = g.getOptarg();
	break;
	
      case 3:
	Integer fdSchedulAttempts = Integer.valueOf(g.getOptarg());
	GlobalOptions.maxAttemptsOnFDSched = fdSchedulAttempts.intValue();
	break;
	
      case 4:
	Float budgetRatio = Float.valueOf(g.getOptarg());
	GlobalOptions.budgetRatio = budgetRatio.floatValue();
	break;
	
      case 5:
	Float cycleLength = Float.valueOf(g.getOptarg());
	GlobalOptions.cycleLength = cycleLength.floatValue();
	break;
	
      case 6:
	GlobalOptions.conserveArea = true;
	break;
	
      case 7:
	String sched = g.getOptarg();
	String[] schedOpts = sched.split(",");
	for(int i=0; i<schedOpts.length; i++) {
	  String schedOpt = schedOpts[i];
	  schedOpt = schedOpt.trim();
	  int scheduleSelect = 0;
	  if(schedOpt.equalsIgnoreCase("asap"))
	    GlobalOptions.scheduleSelect = GlobalOptions.ASAPSchedSelect;
	  else if(schedOpt.equalsIgnoreCase("alap"))
	    GlobalOptions.scheduleSelect = GlobalOptions.ALAPSchedSelect;
	  else if(schedOpt.equalsIgnoreCase("fd"))
	    GlobalOptions.scheduleSelect = GlobalOptions.FDSchedSelect;
	  else if(schedOpt.equalsIgnoreCase("mod"))
	    GlobalOptions.modSched = true;
	  else if(schedOpt.equalsIgnoreCase("nomod")) //these two lines
	    GlobalOptions.modSched = false;       //are normally not here, 
	  //this is the default
	  else if(!schedOpt.equalsIgnoreCase("mod")) {
	    System.out.println("error, invalid schedule selection");
	    System.exit(1);
	  }
	}
	break;
	
      case 8:
	String sched2 = g.getOptarg();
	String[] schedOpts2 = sched2.split(",");
	for(int i=0; i<schedOpts2.length; i++) {
	  String schedOpt = schedOpts2[i];
	  schedOpt = schedOpt.trim();
	  int scheduleSelect = 0;
	  if(schedOpt.equalsIgnoreCase("considerpreds"))
	    GlobalOptions.ignorePreds = false;
	  else if(schedOpt.equalsIgnoreCase("dontpack"))
	    GlobalOptions.packInstructions = false;
	  else if(schedOpt.equalsIgnoreCase("conserve_area"))
	    GlobalOptions.conserveArea = true;
	  else {
	    System.err.println("error, invalid schedule selection");
	    System.exit(1);
	  }
	}
	break;
	
      case 9:
	GlobalOptions.ignorePreds = false;
	break;
	
      case 10:
	GlobalOptions.packInstructions = true;
	break;
	
      case 11:
	GlobalOptions.conserveArea = true;
	break;
	
      case 12:
	GlobalOptions.buildTop = true;
	if (GlobalOptions.makeTestBench) {
	  System.err.println("top-level insertion and testbench"
			     +" building are mutually exclusive.");
	  GlobalOptions.makeTestBench = false;
	}
	break;
	
      case 13:
	if (GlobalOptions.buildTop) {
	  System.err.println("top-level insertion and testbench"
			     +" building are mutually exclusive.");
	  GlobalOptions.makeTestBench = false;
	} else {
	  GlobalOptions.makeTestBench = true;
	}
	break;
	
      case 14:
	GlobalOptions.testBenchFile = g.getOptarg();
	break;

      case 15:
	Integer painThresh = Integer.valueOf(g.getOptarg());
	GlobalOptions.painThreshHold = painThresh.intValue();
	break;

      case 16:
	GlobalOptions.onePreAlloc = false;
	break;

      case 17:
	GlobalOptions.slowestMem = true;
	break;

      case 18:
	GlobalOptions.packArrays = true;
	break;

      default:
	break;
      }
    }
  }
  

  public static void main(String args[]) {
    Compile compile = new Compile(args);
  }

}

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

//should this really be a pass, or should it be a function call 
//straight from Compile.java?

import java.util.*;
import java.io.File;
import fp.GlobalOptions;
import fp.flowgraph.*;
import fp.hardware.*;
import fp.hwdesc.*;


/** this pass loads and saves all the target FPGA information.  
 * 
 * @author Kris Peterson
 */
public class LoadHardWareInfoPass extends Pass implements UtilPass {
   
  String _filename;
   
  public LoadHardWareInfoPass(PassManager pm, String f) {
    super(pm);
    _filename = f;
  }
  
  /** save properties for a target board including logic space and for memories
   * 
   */
  public boolean optimize() {

    ParseHardware parseHardware = new ParseHardware(_filename);

    //System.out.println(" parsing hardware file "+_filename+"\n >>>  "+parseHardware.getHardware());

    GlobalOptions.hardware = parseHardware.getHardware();

    // get the num slices and percent usage of slices
    int numSlices = 0;
    float percentUsage = 0;
    ArrayList chips = GlobalOptions.hardware.chip;
    ArrayList memList = new ArrayList();
    for (Iterator it = chips.iterator(); it.hasNext(); ) {
      Chip chip = (Chip)it.next();
      if (chip.typeCode == Chip.FPGA_TYPE) {
	numSlices = chip.area;
	percentUsage = chip.area_max - chip.area_min;
      }
      if ((chip.typeCode == Chip.RAM_TYPE) || (chip.typeCode == Chip.ROM_TYPE)) { 
        createALoadsAndAStores(chip.memAccessInst, (Memory)chip);
	((Memory)chip).setMemSizeLeft();
	memList.add(chip);
        
      }
    }
    ChipDef chipDef = new ChipDef(numSlices, percentUsage, memList);
    GlobalOptions.chipDef = chipDef;
     
    return true;
  }
  
  public void createALoadsAndAStores(ArrayList insts, Memory memBlock) {
    for (Iterator iti = insts.iterator(); iti.hasNext(); ) {
      MemAccessInst inst = (MemAccessInst)iti.next();
      String name = inst.name;
      int latency = inst.latency;
      float area = inst.area;
      int sliceCnt = inst.sliceCnt;
      if(inst.typeCode == MemAccessInst.ALOAD) {
    	memBlock.saveALoadOp(Operator.addALoad(latency, area, sliceCnt));
      }
      else if(inst.typeCode == MemAccessInst.ASTORE) {
        Operator aStore = Operator.addAStore(latency, area, sliceCnt);
    	memBlock.saveAStoreOp(aStore);
      }
      OperatorNames.addOpName(name);
      //System.out.println(inst);
    }
    /*for (int i = 0; i<OperatorNames.operatorName.length;i++ ) {
      String name = (String)OperatorNames.operatorName[i];
      System.out.println("name " + name);
      
    }
    for (int i = 0; i<Operator.OperatorArray.length;i++ ) {
      Operator op = (Operator)Operator.OperatorArray[i];
      System.out.println("op " + op);
      
    }
    System.out.println("memBlock.getALoadOp() " + memBlock.getALoadOp());
    System.out.println("memBlock.getAStoreOp() " + memBlock.getAStoreOp());
    System.out.println("Operator.OperatorArray " + Operator.OperatorArray);
    System.out.println("OperatorNames.operatorName " + OperatorNames.operatorName);*/
  }
   
  public String name() { 
    return "LoadHardWareInfoPass";
  }
   
}


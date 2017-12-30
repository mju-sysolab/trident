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

import fp.GlobalOptions;
import fp.flowgraph.Instruction;
import fp.circuit.Operation;
import fp.util.Truth;

public class Library implements Truth {
  
  static final String t = "true";
  static final String f = "false";

  /*
    I think the below is wrong.  Why do I need to map qx or aa anything?
    
  */
  
  //       data is   opname class commutative piplined
  //                                          

  String[][] data = {{"aa_fpadd", "fpadd", t, t },
		     {"aa_dpadd", "fpadd", t, t },
		     {"aa_fpsub", "fpsub", f, t },
		     {"aa_dpsub", "fpsub", f, t },
		     {"aa_fpmul", "fpmul", t, t },
		     {"aa_dpmul", "fpmul", t, t },
		     {"aa_fpdiv", "fpdiv", f, t },
		     {"aa_dpdiv", "fpdiv", f, t },
		     {"aa_fpsqrt", "fpsqrt", f, t },
		     {"aa_dpsqrt", "fpsqrt", f, t },
		     {"aa_fpinv", "fpinv", f, f },
		     {"aa_dpinv", "fpinv", f, f },
		     {"aa_fpabs", "fpabs", f, f },
		     {"aa_dpabs", "fpabs", f, f },
		     {"qx_fpadd", "fpadd", t, t },
		     {"qx_fpsub", "fpsub", f, t },
		     {"qx_fpmul_soft", "fpmul", t, t },
		     {"qx_fpmul_hard", "fpmul", t, t },
		     {"qx_fpdiv", "fpdiv", f, t },
		     {"qx_fpsqrt", "fpsqrt", f, f },
		     {"tr_fpinv", "fpinv", f, f },
		     {"tr_fpabs", "fpabs", f, f },
		     {"qx_dpadd", "fpadd", t, t },
		     {"qx_dpsub", "fpsub", f, t },
		     {"qx_dpmul_soft", "fpmul", t, t },
		     {"qx_dpmul_hard", "fpmul", t, t },
		     {"qx_dpdiv", "fpdiv", f, t },
		     {"qx_dpsqrt", "fpsqrt", f, t },
		     {"tr_dpinv", "dpinv", f, f },
		     {"tr_dpabs", "dpabs", f, f },
		     {"tr_itofp", "i_to_fp", f, t },
		     {"tr_itodp", "i_to_dp", f, t },
		     {"tr_fptodp", "fp_to_dp", f, t },
		     {"tr_dptofp", "dp_to_fp", f, t },
		     {"tr_fptoi", "fp_to_i", f, t },
		     {"tr_dptoi", "dp_to_i", f, t },
		     {"tr_ucast", "ucast", f, f},
		     {"tr_dcast", "dcast", f, f},
		     {"aaa_iadd", "+", t, f },
		     {"aaa_isub", "-", f, f },
		     {"aaa_imul", "*", t, f },
		     {"aaa_idiv", "/", f, f },
		     {"aaa_iinv", "-", f, f },
		     {"aaa_seteq", "seteq", t, f },
		     {"aaa_setne", "setne", t, f },
		     {"aaa_setlt", "setlt", f, f },
		     {"aaa_setgt", "setgt", f, f },
		     {"aaa_setle", "setle", f, f },
		     {"aaa_setge", "setge", f, f },
		     {"aaa_and", "&", t, f },
		     {"aaa_or", "|", t, f },
		     {"aaa_xor", "^", t, f },
		     {"aaa_not", "~", f, f },
		     {"aaa_shl", "<<", f, t },
		     {"aaa_cshl", "<<", f, f },
		     {"aaa_shr", ">>", f, t },
		     {"aaa_cshr", ">>", f, f },
		     {"aaa_load", "load", f, f},
		     {"aaa_store", "store", f, f},
		     {"aaa_astore", "astore", f, f},
		     {"aaa_aload", "aload", f, f},
		     {"aaa_select", "select", f, f},
  };
		     
  
  private OperationSelect _selector;

  public Library(String file) {
    // I should open a file and do this, but for now I will 
    // just bang out values.
    for(int i=0; i < data.length; i++) {
      boolean commutative = data[i][2] == t ? true : false;
      boolean isPipelined = data[i][3] == t ? true : false;
      Operation.add(data[i][0], data[i][1], commutative, isPipelined);
    }

    //System.out.println("GlobalOptions.libSelect "+GlobalOptions.libSelect);


    // pick based on the GlobalOptions
    // this is currently a hack and should reflection ...
    if ("quixilica".equals(GlobalOptions.libSelect)) {
      _selector = new QXOperationSelect();
    } else if ("aa_fplib".equals(GlobalOptions.libSelect)) {
      _selector = new AAOperationSelect();
    } else {
      System.err.println("WARNING: fp.synthesis.Library using default "
			 +"AAAOperationSelection.");
      _selector = new AAAOperationSelect();
    }
  }

  public Operation select(Instruction instruction) {
    return _selector.operationSelect(instruction);
  }
  

}


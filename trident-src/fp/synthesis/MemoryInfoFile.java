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

import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;

public class MemoryInfoFile implements Text {
  
  Writer _writer;
  RegInfoList _regs;
  MemInfoList _mem;

  public MemoryInfoFile(String fileName, RegInfoList regInfo, 
		        MemInfoList memInfo) {
    try {
      _writer = new FileWriter(fileName);
      _regs = regInfo;
      _mem = memInfo;
      
      writeFile(_writer);
      _writer.close();
    } catch (IOException e) {
      throw new SynthesisException("Exception occured while trying to write"+
				   "to file: "+e.getMessage());
    }
  }

  public void writeFile(Writer writer) {
    try {
      String text = toText("");
      writer.write(text,0,text.length());
    } catch (IOException e) {
      throw new SynthesisException("Exception occured while trying to write"+
				   "to file: "+e.getMessage());
    }
  }

  public String toText(String prefix) {
      
    // write regs
    // write arrays
    // anything else ?

    StringBuffer sbuf = new StringBuffer();
    sbuf.append(prefix).append(START);
    sbuf.append("Config").append(SPACE);
    sbuf.append(LINE);
    
    sbuf.append(prefix).append(TAB);
    sbuf.append(START);
    sbuf.append("target").append(SPACE);
    sbuf.append("cray_xd1_v12");
    sbuf.append(END).append(LINE);


    sbuf.append(prefix).append(TAB);
    sbuf.append(START);
    sbuf.append("design").append(SPACE);
    sbuf.append("NAME_HERE");
    sbuf.append(END).append(LINE);

    if (_regs != null) {
      sbuf.append(_regs.toText(prefix+TAB));
    }

    if (_mem != null) {
      sbuf.append(_mem.toText(prefix+TAB));
    }

    sbuf.append(prefix).append(END).append(LINE);
    return sbuf.toString();
  }
  
}
    
      

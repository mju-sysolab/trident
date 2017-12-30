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


package fp.util;

import java.io.*;
import java.util.*;

import java.net.URL;


/**
  * A utility class that provides easy access to the compiler's directory 
  * structure.
  * @author Neil Steiner
  */
public class FileTree {
  /** Base object that constructs the tree at startup. */
  //protected static FileTree tree = new FileTree();

  /** The base path name for the compiler.  This can be used to create 
   * paths or files relative to the compiler's directory location. */
  public String basePathName;

  /** The path name for the fp directory. */
  public String fpPathName;
  /** The path name for the hwdesc directory. */
  public String hwdescPathName;

  boolean in_jar = false;
  Class myClass = null;

  // example of how to declare publicly accessible path names for a 
  // hypothetical data directory inside the hardware directory:
  // 
  //  /** The path name for the hardware directory. */
  //  public static String hardwarePathName;
  //  /** The path name for the data directory. */
  //  public static String dataPathName;
  //  
  // be sure to also initialize these variables as illustrated in the FileTree 
  // constructor comments below

  /**
   * Constructor that initializes the static strings for each path name 
   * that this class defines.
   */
  public FileTree() {

    // first determine where we are, and set up the base path
    
    // look up our directory
    myClass = getClass();
    URL resource = myClass.getResource(".");
    //System.out.println("Url "+resource);
    if (resource == null) {
      in_jar = true;
      resource = myClass.getResource("");
    }
    //System.out.println("Url "+resource);

    File classPathFile = new File(resource.getFile());
    String classPathName = classPathFile.getPath();
    
    // look up the base directory
    File basePathFile = classPathFile.getParentFile().getParentFile();
    basePathName = basePathFile.getPath();
    
    // now begin building any paths of interest starting from the base
    
    // look up the fp directory
    File fpPathFile = new File(basePathFile,"fp");
    fpPathName = fpPathFile.getPath();
    
    // look up the hwdesc directory
    File hwdescPathFile = new File(fpPathFile,"hwdesc");
    hwdescPathName = hwdescPathFile.getPath();
    
  }

  public void setHWDescPath(String path) {
    // check to see if path starts with /, if yes blotto the basePath part.
    // otherwise append -- what about jarishness?
    //
    // I am not sure if this works, but it does not seem to cause problems yet.
    String my_base = basePathName;
    if (path.indexOf('/') == 0) {
      my_base = "";
    }

    
    String result = "";

    String[] split = path.split("/");
    for (int i = 0; i < split.length; i++) {
      result = result + split[i];
      if (i + 1 < split.length) 
	result = result + File.separator;
    }
    
    hwdescPathName = my_base + File.separator + result;
    //System.out.println(" set HWDesc to "+hwdescPathName);
  }


  public InputStream getStream(String file) {
    /* So eventually we could make it so that we look
       in two places -- first in the jar and then in
       some data file.
    */
    InputStream result = null;
    boolean jar = false;

    URL resource = myClass.getResource(".");
    if (resource == null) {
      jar = true;
    }

    if (jar) {
      result = myClass.getResourceAsStream("/fp/hwdesc/"+file);
      if (result == null) {
	String path = hwdescPathName+File.separator+file;
        try {
	  result = new FileInputStream(path);
	} catch (FileNotFoundException e) {
	  System.err.println("Cannot find in hwdesc path "+path);
	}
      }

      if (result == null) {
	try {
	  result = new FileInputStream(file);
	} catch (FileNotFoundException e) {
	  System.err.println("Cannot find in default path");
	}
      }
      
    } else {
      String path = hwdescPathName+File.separator+file;
      try {
	result = new FileInputStream(path);
      } catch (FileNotFoundException e) {
	System.err.println("Cannot find in hwdesc path "+path);
      }

      if (result == null) {
	try {
	  result = new FileInputStream(file);
	} catch (FileNotFoundException e) {
	  System.err.println("Cannot find in default path");
	}

      }

    }
    if (result == null) {
      System.err.println("FileTree: Could not open "+file);
      System.exit(-1);
    }
    
    return result;
  }
  
}

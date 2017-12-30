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


import java.io.File;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;

public class ExecExamples extends Java {


  /** */
  public ExecExamples() {
    super();
    verbosity = Project.MSG_VERBOSE;
    // Always fork the tests:
    super.setFork( true );
  }

  /** Adds a set of files (nested fileset attribute).
   */
  public void addFileset(FileSet set) throws BuildException {
    filesets.addElement(set);
  }

  /** Sets the base package used for converting the file path to a
   * class name */
  public void setBasepackage( String base ) throws BuildException {
    this.basePackage = base;
  }

  /** Sets the base package path used for converting the file path to
   * a class name */
  public void setBasepackagepath( String basepath ) throws BuildException {
    // replace forward slashes with dots (if in a unix-like environment)
    String bPackage = basepath.replace('/','.');
    // replace forward slashes with dots (if in a windows-like environment)
    this.basePackage = bPackage.replace('\\','.');
    //this.basePackage = basepath;
  }

  /** Sets the behavior to exercise if a test fails
   * @param value Appropriate values are failimmediately, failatend,
   * and nofail.
   * @exception BuildException if value is not one of the appropriate
   * values indicated above. */
  public void setOnerror( String value ) throws BuildException {
    if ( "failimmediately".equalsIgnoreCase( value ) ) {
      failImmediately = true;
    } else if ( "failatend".equalsIgnoreCase( value ) ) {
      failImmediately = false;
      failAtEnd = true;
    } else if ( "nofail".equalsIgnoreCase( value ) ) {
      failAtEnd = failImmediately = false;
    }
  }

  /** Used to force verbose output.
   * @param verbose "true" or "on"
   */
  public void setVerbose(boolean verbose) throws BuildException {
    if (verbose) {
      this.verbosity = Project.MSG_INFO;
    } else {
      this.verbosity = Project.MSG_VERBOSE;
    }
  }

  public void setPrintResults(String value) throws BuildException {
    if ("failedtests".equalsIgnoreCase(value)) {
      printFailedTests = true;
    } else if ("passedtests".equalsIgnoreCase(value)) {
      printPassedTests = true;
    } else if ("alltests".equalsIgnoreCase(value)) {
      printPassedTests = true;
      printFailedTests = true;
    }
  }

  public void setCmdLine(String value) throws BuildException {
    cmdLine = value;
  }

  public void setBuildDir(String value) throws BuildException {
    buildDir = value;
  }

  /** Override this method of the superclass */
  public void setFork(boolean s) throws BuildException {
    throw new BuildException( getTaskName()
			      + " does not support the fork attribute",
			      getLocation() );
  }

  /** This is the method executed by Ant when this task is run.
   * @exception BuildException 
   */
  public void execute() throws BuildException {
    // process the files in the filesets
    FileSet examples_fs = (FileSet) filesets.elementAt(0);
    DirectoryScanner ds = examples_fs.getDirectoryScanner(project);
    String[] fileNames = ds.getIncludedFiles();
    int count = fileNames.length;


    /** Tally of the number of tests executed */
    int numTestsRun = 0;
    /** Tally of the number of tests failed */
    int numTestsFailed = 0;
    /** List of the names of failed tests */
    Vector failedTests = new Vector();
    Vector passedTests = new Vector();

    log( "*** Running float compiler on " + count + " examples using options: " + cmdLine, verbosity );

    // extra debugging output
    log( "*** EXAMPLES TO BE EXECUTED:", Project.MSG_DEBUG );
    StringBuffer fileNamesSB = new StringBuffer();
    if ( count > 0 )
      fileNamesSB.append( fileNames[0] );
    for( int fn = 1; fn < count; ++fn ) {
      fileNamesSB.append( ", " );
      fileNamesSB.append( fileNames[fn] );
    }
    log( "*** " + fileNamesSB.toString(), Project.MSG_DEBUG );

    // look for where to start 
    File baseDirectory = examples_fs.getDir( project );
    File file = new File( baseDirectory, "fp/Compile.class");
    try {
      setClassname( file );
    } catch ( BuildException be ) {
      // This means that the class name is not found in the
      // basePackage given, so, even if it does exist, we will not
      // run it, because the user wants us to skip it!
      log( "*** Can't run examples, fp.Compile not found");
      return;
    }


    // Run each example with the current hardware description
    for ( int j = 0; j < count; ++j ) {
      File programFile = new File( baseDirectory, fileNames[j] );
      super.setDir( programFile.getParentFile() );
      log( "*** Trident Compiling " + fileNames[j] + " Options: " + cmdLine, verbosity );
      clearArgs();

      /*
      // jlt - this is currently unnecessary ...
      // pass in build directory
      createArg().setValue("-d");
      createArg().setValue(buildDir);
      */


      // break up cmdLine first, then add each arg
      String[] cmdLineArgs = cmdLine.split(" ");
      for (int i=0; i<cmdLineArgs.length; i++) {
	createArg().setValue(cmdLineArgs[i]);
      }

      createArg().setValue(programFile.getName());
      createArg().setValue("run");

      // execute the command
      int errorVal = executeJava();

      ++numTestsRun;
      if ( 0 != errorVal ) {
	if ( failImmediately ) {
	  throw new BuildException( classname + " test result: "
	      + errorVal, location);
	} else {
	  log( "*** " + classname + " " + fileNames[j] + " Options: " + cmdLine + " test result: " + errorVal, Project.MSG_ERR );
	  failedTests.add( classname + " " + fileNames[j] + " Options: " + cmdLine + ":  Error: " + errorVal);
	}
      } else {
	passedTests.add(classname + " " + fileNames[j] + " Options: " + cmdLine );
      }
    }

    if ( 0 < numTestsRun ) {

      log("*** " +  ( numTestsRun - failedTests.size() ) + " out of "
	  + numTestsRun + " test"
	  + ((numTestsRun>1)?"s":"") + " successful.");

      if (printPassedTests) {
	if ( 0 < passedTests.size() ) {
	  log("*** The successful tests were:", verbosity);
	  for (Enumeration e = passedTests.elements(); e.hasMoreElements(); ) {
	    log( "***      " + e.nextElement(), verbosity);
	  }
	}
      }

      if (printFailedTests) {
	if ( 0 < failedTests.size() ) {
	  log("*** The failed tests were:", verbosity);
	  for (Enumeration e = failedTests.elements(); e.hasMoreElements(); ) {
	    log( "***      " + e.nextElement(), verbosity);
	  }
	}
      }

      if ( (0 < failedTests.size()) && failAtEnd ) {
	throw new BuildException( failedTests.size()
	    + " test"
	    +((1<failedTests.size())?"":"s")
	    +" failed",
	    getLocation() );
      }
    }
  }

  private void setClassname( File file ) throws BuildException {
    if ( null == basePackage ) {
      throw new BuildException( "Base package must be specified",
				getLocation() );
    }

    classname = null;
    classname = file.getAbsolutePath().replace(File.separatorChar, '.');

    try {
      classname = classname.substring( classname.lastIndexOf(basePackage) );
    } catch ( StringIndexOutOfBoundsException sioobe ) {
      throw new BuildException( sioobe );
    }

    classname = classname.substring( 0, classname.lastIndexOf( ".class" ) );
    super.setClassname( classname );
  }

  /** The level of verbosity desired */
  private int verbosity;
  /** The FileSet objects to work on */
  private Vector filesets = new Vector();
  /** The base package used to convert the file path to a classname */
  private String basePackage = null;
  /** Local reference to current classname */
  private String classname = null;
  /** If true, tasks will terminate on a failure even if more tests
   * should be run still.*/
  private boolean failImmediately = false;
  /** If true, tasks will terminate on a failure of any test, but only
   * after all tests have run. */
  private boolean failAtEnd = true;
  private boolean printFailedTests = false;
  private boolean printPassedTests = false;
  private String cmdLine = "";
  private String buildDir = "";
}

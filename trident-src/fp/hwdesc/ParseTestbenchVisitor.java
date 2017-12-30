package fp.hwdesc;

import java.util.*;

import fp.util.sexpr.StackVisitor;
import fp.util.sexpr.Symbol;

class ParseTestbenchVisitor extends StackVisitor {
    
  FabIn _fabin =new FabIn();

  static final String tokens[] = {
    "write",
    "read",
    "wait",
    "fabric.in",
    "run"
  };
	
  static final int WRITE = 0;
  static final int READ = 1;
  static final int WAIT = 2;
  static final int FABIN = 3;
  static final int RUN = 4;

  public void forVector(Vector v) {
    int size = v.size();
    if(size > 0) {
      String s = getLabel(v);
	    
      if (tokens[FABIN].equals(s)) {
	stack.push(parseFabIn(v));}
      else if(tokens[WRITE].equals(s)) {parseWrite(v);}
      else if(tokens[READ].equals(s)) {parseRead(v);}
      else if(tokens[WAIT].equals(s)) {parseWait(v);}
      else if(tokens[RUN].equals(s)) {parseRun(v);}
      else {
	System.err.println("Unknown Token '"+s+"'");
	System.exit(-1);
      }
    }
  }

  public FabIn getFabIn() {return _fabin;}

  protected Object pop(Vector v) {
    String s = getLabel(v);
    if (tokens[FABIN].equals(s))
      return super.pop(v);
    else
      return null;
  }

  Object parseFabIn(Vector v) {
    _fabin = new FabIn();
    _fabin.name = v.elementAt(1).toString();
    return _fabin;
  }

  void parseRun(Vector v) {
    Run _run=new Run();
    _fabin.run=_run;
    _run.time=((Number)v.elementAt(1)).intValue();
    _run.unit=v.elementAt(2).toString();
  }

  void parseWrite(Vector v) {
    Write _write = new Write();
    _write.sector = v.elementAt(1).toString();
    if(v.size() > 3) {
      System.err.println("Sorry but the ParseTestBenchVisitor encountered what looks to be a write to an array.");
      System.err.println("I cannot handle arrays yet so I am skipping this write.");
      return;
    }
    _write.value = v.subList(2,v.size()).toArray();
    _fabin.commands.add(_write);
  }

  void parseRead(Vector v) {
    Read _read = new Read();
    _read.sector = v.elementAt(1).toString();
    _read.value = v.subList(2,v.size()).toArray();
    _fabin.commands.add(_read);
  }


  void parseWait(Vector v) {
    Wait _wait = new Wait();
    if(v.size() == 1) {
      _wait.sector = null;
      _wait.duration = 0;
    }
    else {
      _wait.sector = v.elementAt(1).toString();
      _wait.duration = ((Number)v.elementAt(2)).intValue();
    }
    _fabin.commands.add(_wait);
  }

}



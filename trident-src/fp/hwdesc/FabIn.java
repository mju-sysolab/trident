package fp.hwdesc;

import java.util.ArrayList;

public class FabIn {
  public ArrayList commands;
  public String name=null;
  public Run run=null;
    
  FabIn() {
    commands = new ArrayList();
  }
  
  public String toString() {
    String s="";
    for(int i=0; i<commands.size(); i++) {
      s+=commands.get(i).toString()+"\n";
    }
    return s;
  }
}




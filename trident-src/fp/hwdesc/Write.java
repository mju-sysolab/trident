package fp.hwdesc;

public class Write extends Command{
  public String sector=null;
  public Object[] value;
  
  Write(){
    _type="write";
  }

  public String toString() {
    return "Write "+value+" to "+sector;
    } 
}

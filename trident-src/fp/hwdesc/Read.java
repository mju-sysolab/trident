package fp.hwdesc;

public class Read extends Command{
  public String sector=null;
  public Object[] value;

  Read() {
    _type="read";
  }
  
  public String toString() {
    return "Read "+value+" from "+sector;
  } 
}

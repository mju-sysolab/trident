package fp.hwdesc;

public class Wait extends Command{
    public String sector=null;
    public int duration=0;
    
  Wait(){
    _type="wait";
  }

    public String toString() {
	return "Wait "+duration+" nanoseconds on "+sector;
    }
}

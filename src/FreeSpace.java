
public class FreeSpace {
	int size = 0;
	MemHandle mem;
	public FreeSpace(int s, MemHandle h){
		mem = h;
		size = s;
	}
	
	public int getSize(){
		return size;
	}
	
	public long getBlock(){
		return mem.getBlock();
	}
	
	public MemHandle getHandle(){
		return mem;
	}
	
	public void setSize(int s){
		size = s;
	}
	
	public void setHandle(MemHandle m){
		mem = m;
	}
}

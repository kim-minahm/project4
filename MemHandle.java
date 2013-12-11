
public class MemHandle {
	short pos = -1;
	int block = -1;
	
	public MemHandle(short data){
		setHandle(data);
	}
	
	public void setHandle(short obj){
		pos = obj;
	}	
	
	public short getHandle(){
		return pos;
	}
	
	public int getBlock(){
		return block;
	}
	
	public void setBlock(int b){
		block = b;
	}
}

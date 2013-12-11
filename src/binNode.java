

import java.nio.ByteBuffer;

abstract class binNode {
	public short id;
	public MemHandle handle; //Change this to memhandle 
	public ByteBuffer byteArray;
	abstract boolean isEmpty();
	abstract boolean isLeaf();
	
	
}

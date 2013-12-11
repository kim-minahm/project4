

import java.nio.ByteBuffer;

public class internalNode extends binNode {
	binNode left;
	binNode right;

	public internalNode(binNode l, binNode r){
		super.id = 0; 
		super.byteArray = ByteBuffer.allocate(9);
		super.byteArray.putShort(0, super.id);
		super.byteArray.putShort(1, l.handle.pos);
		super.byteArray.putShort(5, r.handle.pos);
		left = l;
		right = r;
		//TODO: MemManage: Write node
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
	

	@Override
	public boolean isLeaf() {
		return false;
	}
	
	public binNode getLeft(){
		
		return left;
	}
	
	public binNode getRight(){
		return right;
	}
	
	public void setLeft(binNode inLeft, MemManager mm){
		 left = inLeft;
		 super.byteArray.putShort(1, inLeft.handle.pos);
		 mm.insert(super.byteArray.array(), this.handle.pos);

	}
	
	public void setRight(binNode inRight, MemManager mm){
		right = inRight;
		super.byteArray.putShort(5, inRight.handle.pos);
		mm.insert(super.byteArray.array(), this.handle.pos);
	}
	
	public void setLeftNull(MemManager mm){
		mm.releaseNode(this.handle);
		super.byteArray.putShort(1, (short) -1);
		MemHandle temp = super.handle;
		this.handle = mm.insert(this.byteArray.array(), this.byteArray.array().length);
		
	}
	
}
 
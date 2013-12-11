

import java.nio.ByteBuffer;

public class LeafNode extends binNode {

// OLD CONSTRUCTOR
//	public LeafNode(MemManager m, Watcher wIn)
//	{
//		
//		super.id = 1;
//		
//		super.byteArray = ByteBuffer.allocate(5);
//		super.byteArray.putShort(0, super.id);
//		
//		MemHandle inHandle = insert()
//		super.byteArray.putShort(1, inHandle);
//		
//		m.insert(info, length)
//		
//	}
	
	public LeafNode(double[] coords, String key, MemManager m) {
		Watcher inWatch = new Watcher(coords[0], coords[1], key);
		byte[] watchBytes = m.serialize(inWatch);
		
		super.id = 1;

		super.byteArray = ByteBuffer.allocate(5);
		super.byteArray.putShort(0, super.id);
		
		super.handle = m.insert(watchBytes, watchBytes.length);
		super.byteArray.putShort(1, super.handle.pos);
		
		m.insert(super.byteArray.array(), super.byteArray.array().length);
		
	}

	public LeafNode() {
		super.id = -1;
	}

	/**
	 * Returns if 
	 * @return
	 */
	public boolean isEmpty(){
		return super.id == -1;
	}

	public boolean isLeaf() {
		return true;
	}
	
	/**
	 * returns the handle
	 * @return
	 */
	public MemHandle getHandle(){
		return handle;
	}
	
	
	/**
	 * Returns the coordinates in double format following getting by MemManager
	 * @return coordinates
	 */
	public double[] getData(MemManager m)
	{
		byte[] watchData = m.getRecord(handle);
		
		byte[] xArray = new byte[8];
		byte[] yArray = new byte[8];
		
		for(int i = 0; i < 8; i++){
			xArray[i] = watchData[i];
		}
		
		for(int j = 8; j < 16; j++){
			yArray[j] = watchData[j];
		}
		
		double x = ByteBuffer.wrap(xArray).getDouble();
		double y = ByteBuffer.wrap(yArray).getDouble();
		
		double [] retArray = {x, y};
		
		return retArray; //MemManager getByHandle();
	}
	
	public String getKey(MemManager m) {
		byte[] watchData = m.getRecord(super.handle);
		byte[] stringArray = new byte[watchData.length - 16];
		for(int i = 16; i < watchData.length; i++){
			stringArray[i] = watchData[i];
		}
		return new String(stringArray); //GetsKeyByHandle
	}
	

}


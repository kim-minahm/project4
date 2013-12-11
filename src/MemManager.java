
import java.io.IOException;
import java.nio.ByteBuffer;

public class MemManager<T> {
	int numBuff = 0;
	int buffSize = 0;
	int block = 0;
	short fileSize = 0;
	BufferPool buffer;
	int freePos = 0;
	FreeList freeList = new FreeList();

	/**
	 * Makes a MemManager
	 * 
	 * @param nums
	 *            number of buffers
	 * @param sz
	 *            size of each buffer
	 */
	public MemManager(int nums, int sz) {
		buffSize = sz;
		numBuff = nums;
		// what
		try {
			buffer = new BufferPool("p4bin.dat", buffSize, numBuff);
		} catch (IOException e) {
			e.printStackTrace();
		}
		fileSize = (short) buffSize;
	}

	public MemHandle insert(byte[] info, int length) {
		int len = length;

		MemHandle pos = freeList.getFreeSpace(len);
		// no spaces big enough in freeList
		if (pos == null) {
			MemHandle m = new MemHandle((short) (fileSize));
			m.block = fileSize / buffSize;
			freeList.add(m, buffSize);
			buffer.insert(new byte[buffSize], m);
			pos = freeList.getFreeSpace(len);
			buffer.insert(info, pos);
			fileSize = (short) (fileSize + buffSize);
		} else {
			buffer.insert(info, pos);
			pos.block = pos.pos / buffSize;
			freeList.list.remove(freeList.currPos);
		}
		return pos;
	}

	public void releaseWatcher(MemHandle h) {
		buffer.removeWatcher(h);
	}
	
	public void releaseNode(MemHandle h){
		buffer.removeNode(h);
	}

	public byte[] getRecord(MemHandle h) {
		return buffer.getWatcherData(h);
	}

	public byte[] getNode(MemHandle h) {
		return buffer.getNodeData(h);
	}

	public byte[] serialize(Watcher w) {
		// make sure adding the size to the front
		int len = 18 + w.getName().length() * 2;
		ByteBuffer sz = ByteBuffer.allocate(2);
		sz.putShort((short) (len - 2));

		byte[] ret = new byte[len];
		byte[] temp = new byte[2];
		temp = sz.array();
		for (int i = 0; i < 2; i++) {
			ret[i] = temp[i];
		}
		byte[] bytes = new byte[8];

		java.nio.ByteBuffer.wrap(bytes).putDouble(w.getX());
		for (int i = 0; i < 8; i++) {
			ret[i] = bytes[i];
		}
		java.nio.ByteBuffer.wrap(bytes).putDouble(w.getY());
		for (int i = 0; i < 8; i++) {
			ret[10 + i] = bytes[i];
		}
		byte[] stringByte = w.getName().getBytes();
		for (int i = 0; i < stringByte.length; i++) {
			ret[16 + i] = stringByte[i];
		}
		return ret;
	}
}

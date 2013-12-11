import java.nio.ByteBuffer;

	/**
	 * Buffer object
	 * 
	 * @author kimminahm
	 * 
	 */
	public class Buffer {
		ByteBuffer buff;
		int blockNum;
		boolean dirty;

		public Buffer(int size) {
			buff = ByteBuffer.allocate(size);
			dirty = false;
			blockNum = -1;
		}
	}
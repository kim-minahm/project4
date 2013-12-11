import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class BufferPool {
	private int blocksize;

	private int numCacheHits; // stats
	private int numCacheMisses;
	private int numDiskReads;
	private int numDiskWrites;

	// BufferQueue buffers;
	private long filesize; // filesize won't change
	LinkedList<Buffer> file = new LinkedList<Buffer>();

	public BufferPool(String filename, int blockSize, int numberOfBuffers)
			throws IOException {

		blocksize = blockSize;

		// stats
		numCacheHits = 0;
		numCacheMisses = 0;
		numDiskReads = 0;
		numDiskWrites = 0;
	}

	public long fileSize() {
		return filesize;
	}

	public void insert(byte[] space, MemHandle mem) {
		Buffer current = new Buffer(blocksize);
		// checks for block.
		if (mem.block < file.size() - 1) {
			//it fits within the buffer
			if (offset(mem.pos) + space.length < blocksize) {
				current = file.get(mem.block);
				ByteBuffer dat = current.buff;
				dat.position(offset(mem.getHandle()));
				dat.put(space);
			}else{
				current = file.get(mem.block);
				Buffer current2 = file.get(mem.block+1);
				ByteBuffer dat = current.buff;
				ByteBuffer dat2 = current2.buff;
				int diff = offset(mem.pos)+space.length - blocksize;
				dat.position(offset(mem.getHandle()));
				dat.put(space,0,space.length-diff);
				dat2.position(0);
				dat.put(space,space.length-diff,diff);
			}
		} else {
			while (file.size() - 1 <= mem.block) {
				file.add(new Buffer(blocksize));
				file.get(file.size() - 1).blockNum = file.size() - 1;
			}
			current = file.get(mem.block);
			ByteBuffer dat = current.buff;
			dat.position(offset(mem.getHandle()));
			dat.put(space);
		}
	}

	/**
	 * gets the byte array representing a watcher
	 * 
	 * @param mem
	 * @return
	 */
	public byte[] getWatcherData(MemHandle mem) {
		Buffer current = file.get(mem.block);
		byte[] b = new byte[2];
		current.buff.get(b, offset(mem.pos), 2);
		ByteBuffer temp = ByteBuffer.wrap(b);
		int s = temp.getShort();
		byte[] ret = new byte[1000];
		current.buff.get(ret, offset(mem.pos) + 2, s);
		return ret;
	}

	/**
	 * 0 is internal, 1 is leaf, -1 is empty
	 * 
	 * @param m
	 * @return
	 */
	public byte[] getNodeData(MemHandle mem) {
		Buffer current = file.get(mem.block);
		byte[] b = new byte[10];
		current.buff.get(b, offset(mem.pos), 1);
		ByteBuffer temp = ByteBuffer.wrap(b);
		int s = temp.get();
		if (s == 1) {// 5 bytes, 1 sig, 4 handle
			current.buff.get(b, offset(mem.pos), 5);
		} else {// 9 bytes, 1 sig, 4 leftchild, 4 rightchile
			current.buff.get(b, offset(mem.pos), 9);
		}
		return b;
	}

	public void removeWatcher(MemHandle mem) {
		Buffer current = file.get(mem.block);
		byte[] b = new byte[2];
		current.buff.get(b, offset(mem.pos), 2);
		ByteBuffer temp = ByteBuffer.wrap(b);
		int s = temp.getShort();
		byte[] ret = new byte[s];
		current.buff.position(offset(mem.pos));
		current.buff.put(ret, offset(mem.pos), s);
	}

	public void removeNode(MemHandle mem) {
		Buffer current = file.get(mem.block);
		byte[] b = new byte[10];
		current.buff.get(b, offset(mem.pos), 1);
		ByteBuffer temp = ByteBuffer.wrap(b);
		int s = temp.get();
		current.buff.position(offset(mem.pos));
		if (s == 1) {// 5 bytes, 1 sig, 4 handle
			current.buff.put(new byte[5], offset(mem.pos), 5);
		} else {// 9 bytes, 1 sig, 4 leftchild, 4 rightchile
			current.buff.put(new byte[9], offset(mem.pos), 9);
		}
	}

	/**
	 * Write a buffer from memory to disk.
	 * 
	 * @param b
	 *            buffer to write to disk
	 */

	private int offset(short b) {
		return (int) b % blocksize;
	}

	public int getNumCacheHits() {
		return numCacheHits;
	}

	public int getNumCacheMisses() {
		return numCacheMisses;
	}

	public int getNumDiskReads() {
		return numDiskReads;
	}

	public int getNumDiskWrites() {
		return numDiskWrites;
	}
}
